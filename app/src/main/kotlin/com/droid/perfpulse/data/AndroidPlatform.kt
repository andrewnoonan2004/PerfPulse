package com.droid.perfpulse.data

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import android.os.PowerManager
import android.os.Process
import android.os.SystemClock

/**
 * Thin adapter over Android system services. Everything here is a direct platform call so the
 * monitors themselves stay JVM-testable.
 */
class AndroidPlatform(private val context: Context) {
  private val activityManager = context.getSystemService(ActivityManager::class.java)
  private val powerManager = context.getSystemService(PowerManager::class.java)
  private val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
  private val appOps = context.getSystemService(AppOpsManager::class.java)
  private val packageManager = context.packageManager

  private var lastHeadroomAt = 0L
  private var lastHeadroom: Float? = null

  fun socName(): String? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      listOf(Build.SOC_MANUFACTURER, Build.SOC_MODEL)
        .filter { it.isNotBlank() && it != Build.UNKNOWN }
        .joinToString(" ")
        .ifBlank { null }
    } else {
      null
    }

  fun abis(): List<String> = Build.SUPPORTED_ABIS.toList()

  fun systemMemory(): SystemMemoryInfo {
    val info = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(info)
    return SystemMemoryInfo(info.totalMem, info.availMem, info.threshold, info.lowMemory)
  }

  fun thermal(): PlatformThermal {
    val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
      ?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
      ?.takeIf { it != Int.MIN_VALUE }
      ?.let { it / 10f }
    val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) powerManager.currentThermalStatus else null
    return PlatformThermal(battery, status, headroom())
  }

  /** Thermal headroom is rate-limited by the platform; poll at most every few seconds. */
  private fun headroom(): Float? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
    val now = SystemClock.elapsedRealtime()
    if (now - lastHeadroomAt >= 5_000L) {
      lastHeadroomAt = now
      val value = powerManager.getThermalHeadroom(10)
      if (!value.isNaN()) lastHeadroom = value
    }
    return lastHeadroom
  }

  fun processCpuTimeMs(): Long = Process.getElapsedCpuTime()

  fun elapsedRealtimeMs(): Long = SystemClock.elapsedRealtime()

  fun gpuStaticInfo(): GpuStaticInfo {
    val gl = queryGlStrings()
    val vulkan = packageManager.systemAvailableFeatures
      .firstOrNull { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION }
      ?.version
      ?.let { SysParsers.decodeVulkanVersion(it) }
    return GpuStaticInfo(
      renderer = gl?.get(0),
      vendor = gl?.get(1),
      glVersion = gl?.get(2),
      vulkanVersion = vulkan,
      glEsVersion = activityManager.deviceConfigurationInfo?.glEsVersion,
    )
  }

  /** Spins up a throwaway 1x1 pbuffer GLES context to read renderer/vendor/version strings. */
  private fun queryGlStrings(): List<String?>? {
    val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
    if (display == EGL14.EGL_NO_DISPLAY) return null
    val version = IntArray(2)
    if (!EGL14.eglInitialize(display, version, 0, version, 1)) return null
    try {
      val attribs = intArrayOf(
        EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
        EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
        EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8,
        EGL14.EGL_NONE,
      )
      val configs = arrayOfNulls<EGLConfig>(1)
      val count = IntArray(1)
      if (!EGL14.eglChooseConfig(display, attribs, 0, configs, 0, 1, count, 0) || count[0] == 0) return null
      val config = configs[0]
      val ctx = EGL14.eglCreateContext(
        display, config, EGL14.EGL_NO_CONTEXT,
        intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0,
      )
      if (ctx == EGL14.EGL_NO_CONTEXT) return null
      val surface = EGL14.eglCreatePbufferSurface(
        display, config, intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE), 0,
      )
      try {
        if (!EGL14.eglMakeCurrent(display, surface, surface, ctx)) return null
        return listOf(
          GLES20.glGetString(GLES20.GL_RENDERER),
          GLES20.glGetString(GLES20.GL_VENDOR),
          GLES20.glGetString(GLES20.GL_VERSION),
        )
      } finally {
        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
        if (surface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(display, surface)
        EGL14.eglDestroyContext(display, ctx)
      }
    } catch (_: Throwable) {
      return null
    } finally {
      EGL14.eglTerminate(display)
    }
  }

  fun ownProcess(files: SystemFileReader): OwnProcessInfo {
    val mem = Debug.MemoryInfo()
    Debug.getMemoryInfo(mem)
    val javaKb = mem.getMemoryStat("summary.java-heap")?.toLongOrNull() ?: mem.dalvikPss.toLong()
    val nativeKb = mem.getMemoryStat("summary.native-heap")?.toLongOrNull() ?: mem.nativePss.toLong()
    val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) Application.getProcessName() else context.packageName
    return OwnProcessInfo(
      pid = Process.myPid(),
      uid = Process.myUid(),
      processName = name,
      totalPssKb = mem.totalPss.toLong(),
      javaHeapKb = javaKb,
      nativeHeapKb = nativeKb,
      threads = files.read("/proc/self/status")?.let { SysParsers.parseStatusField(it, "Threads") },
      cpuTimeMs = Process.getElapsedCpuTime(),
      uptimeMs = SystemClock.elapsedRealtime() - Process.getStartElapsedRealtime(),
    )
  }

  fun hasUsageAccess(): Boolean {
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    } else {
      @Suppress("DEPRECATION")
      appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    }
    return if (mode == AppOpsManager.MODE_DEFAULT) {
      context.checkSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
    } else {
      mode == AppOpsManager.MODE_ALLOWED
    }
  }

  /** Apps active in the last 24 hours, newest first. Requires usage access. */
  fun recentApps(windowMs: Long = 24 * 60 * 60 * 1000L): List<AppUsage> {
    val end = System.currentTimeMillis()
    val stats = usageStatsManager.queryAndAggregateUsageStats(end - windowMs, end)
    return stats.values
      .filter { it.lastTimeUsed > end - windowMs && it.totalTimeInForeground > 0 }
      .map { s ->
        val appInfo = try {
          packageManager.getApplicationInfo(s.packageName, 0)
        } catch (_: PackageManager.NameNotFoundException) {
          null
        }
        AppUsage(
          packageName = s.packageName,
          label = appInfo?.let { packageManager.getApplicationLabel(it).toString() } ?: s.packageName,
          lastUsedMs = s.lastTimeUsed,
          foregroundMs = s.totalTimeInForeground,
          isSystem = appInfo?.let { it.flags and ApplicationInfo.FLAG_SYSTEM != 0 } ?: false,
        )
      }
      .sortedByDescending { it.lastUsedMs }
  }
}

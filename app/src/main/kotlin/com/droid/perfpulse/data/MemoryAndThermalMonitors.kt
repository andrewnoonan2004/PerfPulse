package com.droid.perfpulse.data

/** Subset of `ActivityManager.MemoryInfo` used by [MemoryMonitor]. */
data class SystemMemoryInfo(
  val totalBytes: Long,
  val availableBytes: Long,
  val thresholdBytes: Long,
  val lowMemory: Boolean,
)

class MemoryMonitor(
  private val files: SystemFileReader,
  private val systemMemory: () -> SystemMemoryInfo,
) {
  fun sample(): MemorySnapshot {
    val am = systemMemory()
    val info = files.read("/proc/meminfo")?.let { SysParsers.parseMemInfo(it) }.orEmpty()
    fun kb(key: String): Long? = info[key]?.times(1024)

    return MemorySnapshot(
      totalBytes = am.totalBytes.takeIf { it > 0 } ?: kb("MemTotal") ?: 0,
      availableBytes = am.availableBytes.takeIf { it > 0 } ?: kb("MemAvailable") ?: 0,
      freeBytes = kb("MemFree"),
      cachedBytes = kb("Cached"),
      buffersBytes = kb("Buffers"),
      activeBytes = kb("Active"),
      inactiveBytes = kb("Inactive"),
      shmemBytes = kb("Shmem"),
      swapTotalBytes = kb("SwapTotal"),
      swapFreeBytes = kb("SwapFree"),
      lowMemoryThresholdBytes = am.thresholdBytes,
      isLowMemory = am.lowMemory,
    )
  }
}

/** Platform thermal signals (PowerManager + battery intent). */
data class PlatformThermal(val batteryCelsius: Float?, val status: Int?, val headroom: Float?)

class ThermalMonitor(
  private val files: SystemFileReader,
  private val platform: () -> PlatformThermal,
) {
  private var zonePaths: List<String>? = null

  fun sample(): ThermalSnapshot {
    val p = platform()
    val paths = zonePaths ?: files.list(ROOT).filter { it.startsWith("thermal_zone") }
      .sortedBy { it.removePrefix("thermal_zone").toIntOrNull() ?: Int.MAX_VALUE }
      .map { "$ROOT/$it" }
      .also { zonePaths = it }

    val zones = paths.mapNotNull { dir ->
      val temp = files.read("$dir/temp")?.toLongOrNull()?.let { SysParsers.normalizeTemperature(it) }
        ?: return@mapNotNull null
      ThermalZone(name = files.read("$dir/type") ?: dir.substringAfterLast('/'), celsius = temp)
    }
    return ThermalSnapshot(p.batteryCelsius, p.status, p.headroom, zones)
  }

  companion object {
    private const val ROOT = "/sys/class/thermal"
  }
}

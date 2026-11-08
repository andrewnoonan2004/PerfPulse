package com.droid.perfpulse

import android.content.Context
import com.droid.perfpulse.data.AndroidPlatform
import com.droid.perfpulse.data.AppUsage
import com.droid.perfpulse.data.CpuMonitor
import com.droid.perfpulse.data.DefaultMonitorRepository
import com.droid.perfpulse.data.FileSystemReader
import com.droid.perfpulse.data.GpuMonitor
import com.droid.perfpulse.data.MemoryMonitor
import com.droid.perfpulse.data.MonitorRepository
import com.droid.perfpulse.data.OwnProcessInfo
import com.droid.perfpulse.data.ProcessSource
import com.droid.perfpulse.data.ThermalMonitor

/** Manual dependency container wiring real platform implementations together. */
class AppContainer(context: Context) {
  private val files = FileSystemReader()
  private val platform = AndroidPlatform(context.applicationContext)

  val repository: MonitorRepository by lazy {
    DefaultMonitorRepository(
      cpu = CpuMonitor(
        files = files,
        platformSocName = platform::socName,
        abis = platform.abis(),
        processCpuTimeMs = platform::processCpuTimeMs,
        elapsedRealtimeMs = platform::elapsedRealtimeMs,
      ),
      gpu = GpuMonitor(files, platform::gpuStaticInfo),
      memory = MemoryMonitor(files, platform::systemMemory),
      thermal = ThermalMonitor(files, platform::thermal),
      processes = object : ProcessSource {
        override fun ownProcess(): OwnProcessInfo = platform.ownProcess(files)
        override fun hasUsageAccess(): Boolean = platform.hasUsageAccess()
        override fun recentApps(): List<AppUsage> = platform.recentApps()
      },
    )
  }
}

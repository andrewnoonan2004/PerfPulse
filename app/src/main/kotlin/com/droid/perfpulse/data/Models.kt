package com.droid.perfpulse.data

/** How the overall CPU figure was obtained. */
enum class CpuUsageSource {
  /** Real utilisation from `/proc/stat` jiffies (only on devices/ROMs that expose it). */
  PROC_STAT,

  /** Estimated from per-core clock scaling — the best non-root signal on Android 8+. */
  FREQUENCY,

  UNAVAILABLE,
}

data class CoreInfo(
  val id: Int,
  val online: Boolean,
  val curFreqKHz: Long?,
  val minFreqKHz: Long?,
  val maxFreqKHz: Long?,
  val governor: String?,
) {
  val loadPercent: Float?
    get() = if (!online) 0f else SysParsers.frequencyLoad(curFreqKHz, minFreqKHz, maxFreqKHz)
}

/** A group of cores sharing the same max frequency (e.g. little / big / prime). */
data class CoreCluster(val maxFreqKHz: Long, val coreIds: List<Int>)

data class CpuSnapshot(
  val socName: String,
  val abis: List<String>,
  val cores: List<CoreInfo>,
  val usagePercent: Float?,
  val source: CpuUsageSource,
  val appCpuPercent: Float?,
) {
  val coreCount: Int get() = cores.size
  val onlineCount: Int get() = cores.count { it.online }

  val clusters: List<CoreCluster>
    get() = cores.filter { it.maxFreqKHz != null }
      .groupBy { it.maxFreqKHz!! }
      .map { (freq, list) -> CoreCluster(freq, list.map { it.id }) }
      .sortedBy { it.maxFreqKHz }

  companion object {
    val Empty = CpuSnapshot("", emptyList(), emptyList(), null, CpuUsageSource.UNAVAILABLE, null)
  }
}

data class GpuStaticInfo(
  val renderer: String?,
  val vendor: String?,
  val glVersion: String?,
  val vulkanVersion: String?,
  val glEsVersion: String?,
) {
  companion object {
    val Unknown = GpuStaticInfo(null, null, null, null, null)
  }
}

data class GpuSnapshot(
  val info: GpuStaticInfo,
  /** Driver-reported busy percent, null when the kernel node is not readable. */
  val utilizationPercent: Float?,
  val curFreqMHz: Long?,
  val minFreqMHz: Long?,
  val maxFreqMHz: Long?,
  /** sysfs node the utilisation came from, for transparency. */
  val utilizationSource: String?,
) {
  /** Utilisation if available, otherwise clock-scaling estimate. */
  val displayLoadPercent: Float?
    get() = utilizationPercent ?: SysParsers.frequencyLoad(curFreqMHz, minFreqMHz, maxFreqMHz)

  val isEstimated: Boolean get() = utilizationPercent == null && displayLoadPercent != null

  companion object {
    val Empty = GpuSnapshot(GpuStaticInfo.Unknown, null, null, null, null, null)
  }
}

data class MemorySnapshot(
  val totalBytes: Long,
  val availableBytes: Long,
  val freeBytes: Long?,
  val cachedBytes: Long?,
  val buffersBytes: Long?,
  val activeBytes: Long?,
  val inactiveBytes: Long?,
  val shmemBytes: Long?,
  val swapTotalBytes: Long?,
  val swapFreeBytes: Long?,
  val lowMemoryThresholdBytes: Long,
  val isLowMemory: Boolean,
) {
  val usedBytes: Long get() = (totalBytes - availableBytes).coerceAtLeast(0)
  val usedPercent: Float get() = if (totalBytes > 0) usedBytes * 100f / totalBytes else 0f
  val swapUsedBytes: Long? get() =
    if (swapTotalBytes != null && swapFreeBytes != null) swapTotalBytes - swapFreeBytes else null
  val swapUsedPercent: Float? get() =
    swapUsedBytes?.let { used -> swapTotalBytes?.takeIf { it > 0 }?.let { used * 100f / it } }

  companion object {
    val Empty = MemorySnapshot(0, 0, null, null, null, null, null, null, null, null, 0, false)
  }
}

data class ThermalZone(val name: String, val celsius: Float)

data class ThermalSnapshot(
  val batteryCelsius: Float?,
  /** PowerManager thermal status: 0 = none … 6 = shutdown. Null below API 29. */
  val status: Int?,
  /** Forecast headroom (1.0 = throttling threshold). Null when unsupported. */
  val headroom: Float?,
  val zones: List<ThermalZone>,
) {
  val hottestZone: ThermalZone? get() = zones.maxByOrNull { it.celsius }

  companion object {
    val Empty = ThermalSnapshot(null, null, null, emptyList())
  }
}

data class OwnProcessInfo(
  val pid: Int,
  val uid: Int,
  val processName: String,
  val totalPssKb: Long,
  val javaHeapKb: Long,
  val nativeHeapKb: Long,
  val threads: Long?,
  val cpuTimeMs: Long,
  val uptimeMs: Long,
)

data class AppUsage(
  val packageName: String,
  val label: String,
  val lastUsedMs: Long,
  val foregroundMs: Long,
  val isSystem: Boolean,
)

data class ProcessSnapshot(
  val own: OwnProcessInfo?,
  val hasUsageAccess: Boolean,
  val apps: List<AppUsage>,
) {
  companion object {
    val Empty = ProcessSnapshot(null, false, emptyList())
  }
}

data class SystemSnapshot(
  val timestampMs: Long,
  val cpu: CpuSnapshot,
  val gpu: GpuSnapshot,
  val memory: MemorySnapshot,
  val thermal: ThermalSnapshot,
  val processes: ProcessSnapshot,
)

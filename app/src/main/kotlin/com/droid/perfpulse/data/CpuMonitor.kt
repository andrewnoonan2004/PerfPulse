package com.droid.perfpulse.data

/**
 * Samples CPU state from sysfs. Stateful: keeps the previous `/proc/stat` and own-process CPU
 * time sample to compute deltas.
 *
 * Note: since Android 8, `/proc/stat` is blocked for apps on most devices, so we fall back to a
 * clock-scaling estimate (how far each core's current frequency sits between its min and max).
 */
class CpuMonitor(
  private val files: SystemFileReader,
  private val platformSocName: () -> String?,
  private val abis: List<String>,
  private val processCpuTimeMs: () -> Long,
  private val elapsedRealtimeMs: () -> Long,
  private val availableProcessors: () -> Int = { Runtime.getRuntime().availableProcessors() },
) {
  private var prevStat: SysParsers.CpuTimes? = null
  private var prevProcCpuMs: Long? = null
  private var prevWallMs: Long? = null
  private var cachedSocName: String? = null
  private val cachedMinMax = mutableMapOf<Int, Pair<Long?, Long?>>()

  fun sample(): CpuSnapshot {
    val coreIds = discoverCores()
    val online = files.read("$CPU_ROOT/online")?.let { SysParsers.parseCpuRange(it).toSet() }
    val cores = coreIds.map { id -> readCore(id, online) }

    val statUsage = files.read("/proc/stat")?.let { SysParsers.parseProcStatTotal(it) }?.let { cur ->
      val usage = prevStat?.let { SysParsers.cpuUsageBetween(it, cur) }
      prevStat = cur
      usage
    }

    val (usage, source) = when {
      statUsage != null -> statUsage to CpuUsageSource.PROC_STAT
      else -> {
        val loads = cores.mapNotNull { it.loadPercent }
        if (loads.isNotEmpty()) (loads.sum() / cores.size) to CpuUsageSource.FREQUENCY
        else null to CpuUsageSource.UNAVAILABLE
      }
    }

    return CpuSnapshot(
      socName = socName(),
      abis = abis,
      cores = cores,
      usagePercent = usage,
      source = source,
      appCpuPercent = sampleOwnCpu(cores.size.coerceAtLeast(1)),
    )
  }

  private fun discoverCores(): List<Int> {
    val present = files.read("$CPU_ROOT/present")?.let { SysParsers.parseCpuRange(it) }
    if (!present.isNullOrEmpty()) return present
    val fromDir = files.list(CPU_ROOT)
      .mapNotNull { name -> CPU_DIR.matchEntire(name)?.groupValues?.get(1)?.toIntOrNull() }
      .sorted()
    if (fromDir.isNotEmpty()) return fromDir
    return (0 until availableProcessors()).toList()
  }

  private fun readCore(id: Int, online: Set<Int>?): CoreInfo {
    val base = "$CPU_ROOT/cpu$id/cpufreq"
    val cur = files.read("$base/scaling_cur_freq")?.toLongOrNull()
      ?: files.read("$base/cpuinfo_cur_freq")?.toLongOrNull()
    val (min, max) = cachedMinMax.getOrPut(id) {
      val mn = files.read("$base/cpuinfo_min_freq")?.toLongOrNull()
        ?: files.read("$base/scaling_min_freq")?.toLongOrNull()
      val mx = files.read("$base/cpuinfo_max_freq")?.toLongOrNull()
        ?: files.read("$base/scaling_max_freq")?.toLongOrNull()
      mn to mx
    }
    val isOnline = online?.contains(id) ?: (cur != null || id == 0)
    return CoreInfo(
      id = id,
      online = isOnline,
      curFreqKHz = if (isOnline) cur else null,
      minFreqKHz = min,
      maxFreqKHz = max,
      governor = files.read("$base/scaling_governor"),
    )
  }

  private fun sampleOwnCpu(cores: Int): Float? {
    val cpu = processCpuTimeMs()
    val wall = elapsedRealtimeMs()
    val prevCpu = prevProcCpuMs
    val prevWall = prevWallMs
    prevProcCpuMs = cpu
    prevWallMs = wall
    if (prevCpu == null || prevWall == null) return null
    val wallDelta = wall - prevWall
    if (wallDelta <= 0) return null
    return ((cpu - prevCpu).toFloat() / (wallDelta * cores) * 100f).coerceIn(0f, 100f)
  }

  private fun socName(): String {
    cachedSocName?.let { return it }
    val name = platformSocName()?.takeIf { it.isNotBlank() && it != "unknown" }
      ?: files.read("/proc/cpuinfo")?.let { SysParsers.parseCpuInfoHardware(it) }
      ?: "Unknown SoC"
    cachedSocName = name
    return name
  }

  companion object {
    const val CPU_ROOT = "/sys/devices/system/cpu"
    private val CPU_DIR = Regex("cpu(\\d+)")
  }
}

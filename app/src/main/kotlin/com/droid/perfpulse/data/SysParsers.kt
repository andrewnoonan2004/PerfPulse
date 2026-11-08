package com.droid.perfpulse.data

/**
 * Pure parsing helpers for Linux procfs / sysfs formats. No Android dependencies.
 */
object SysParsers {

  /** Parses `/proc/meminfo` into a map of key -> value in kB. */
  fun parseMemInfo(text: String): Map<String, Long> =
    text.lineSequence()
      .mapNotNull { line ->
        val idx = line.indexOf(':')
        if (idx <= 0) return@mapNotNull null
        val key = line.substring(0, idx).trim()
        val value = line.substring(idx + 1).trim().split(Regex("\\s+")).firstOrNull()?.toLongOrNull()
        value?.let { key to it }
      }
      .toMap()

  /** Parses CPU range lists such as `0-3,6,8-9` (used by `/sys/devices/system/cpu/online`). */
  fun parseCpuRange(text: String): List<Int> =
    text.trim().split(',').filter { it.isNotBlank() }.flatMap { part ->
      val bounds = part.trim().split('-')
      val start = bounds[0].trim().toIntOrNull() ?: return@flatMap emptyList()
      val end = bounds.getOrNull(1)?.trim()?.toIntOrNull() ?: start
      if (end >= start) (start..end).toList() else emptyList()
    }

  /** Aggregate jiffies from the `cpu ` line of `/proc/stat`. */
  data class CpuTimes(val idle: Long, val total: Long)

  fun parseProcStatTotal(text: String): CpuTimes? {
    val line = text.lineSequence().firstOrNull { it.startsWith("cpu ") } ?: return null
    val values = line.removePrefix("cpu").trim().split(Regex("\\s+")).mapNotNull { it.toLongOrNull() }
    if (values.size < 4) return null
    // user nice system idle iowait irq softirq steal ...
    val idle = values[3] + (values.getOrNull(4) ?: 0L)
    val total = values.take(8).sum()
    return CpuTimes(idle = idle, total = total)
  }

  /** CPU utilisation in percent between two `/proc/stat` samples. */
  fun cpuUsageBetween(prev: CpuTimes, cur: CpuTimes): Float? {
    val totalDelta = cur.total - prev.total
    val idleDelta = cur.idle - prev.idle
    if (totalDelta <= 0) return null
    return ((totalDelta - idleDelta).toFloat() / totalDelta * 100f).coerceIn(0f, 100f)
  }

  /**
   * Estimates load from clock scaling: 0% at min frequency, 100% at max frequency.
   * Returns null when the inputs are incomplete.
   */
  fun frequencyLoad(cur: Long?, min: Long?, max: Long?): Float? {
    if (cur == null || max == null || max <= 0) return null
    val floor = min ?: 0L
    if (max <= floor) return if (cur >= max) 100f else 0f
    return ((cur - floor).toFloat() / (max - floor) * 100f).coerceIn(0f, 100f)
  }

  /**
   * Parses the various GPU busy formats exposed by vendor drivers:
   * - Adreno `gpu_busy_percentage`: `"37 %"`
   * - Adreno `gpubusy`: `"busy total"` (two counters)
   * - Mali / Pixel `utilization` / `gpu_busy`: `"37"`
   * - MediaTek `gpu_utilization`: `"37 0 0"`
   */
  fun parseGpuBusy(text: String, isBusyTotalPair: Boolean = false): Float? {
    val numbers = Regex("-?\\d+(\\.\\d+)?").findAll(text).map { it.value.toDouble() }.toList()
    if (numbers.isEmpty()) return null
    if (isBusyTotalPair) {
      val busy = numbers.getOrNull(0) ?: return null
      val total = numbers.getOrNull(1) ?: return null
      if (total <= 0.0) return 0f
      return (busy / total * 100.0).toFloat().coerceIn(0f, 100f)
    }
    val value = numbers.first()
    return if (value in 0.0..100.0) value.toFloat() else null
  }

  /**
   * GPU drivers report clocks in Hz, kHz or MHz depending on vendor. GPU clocks live in roughly
   * the 100–2000 MHz range, so we infer the unit from magnitude.
   */
  fun normalizeGpuFreqToMHz(raw: Long): Long? = when {
    raw <= 0 -> null
    raw >= 10_000_000L -> raw / 1_000_000L
    raw >= 10_000L -> raw / 1_000L
    else -> raw
  }

  /** Thermal zones usually report milli-degrees Celsius; some report whole degrees. */
  fun normalizeTemperature(raw: Long): Float? {
    val celsius = if (raw > 1000 || raw < -1000) raw / 1000f else raw.toFloat()
    return celsius.takeIf { it in -40f..150f }
  }

  /** Extracts `Hardware` / `model name` from `/proc/cpuinfo`. */
  fun parseCpuInfoHardware(text: String): String? {
    val keys = listOf("Hardware", "model name", "Processor")
    val lines = text.lineSequence().toList()
    for (key in keys) {
      val line = lines.firstOrNull { it.startsWith(key) && it.contains(':') } ?: continue
      val value = line.substringAfter(':').trim()
      if (value.isNotEmpty()) return value
    }
    return null
  }

  /** Reads a numeric field (e.g. `Threads`, `VmRSS`) from `/proc/<pid>/status`. */
  fun parseStatusField(text: String, field: String): Long? =
    text.lineSequence()
      .firstOrNull { it.startsWith("$field:") }
      ?.substringAfter(':')
      ?.trim()
      ?.split(Regex("\\s+"))
      ?.firstOrNull()
      ?.toLongOrNull()

  /** Decodes a Vulkan packed version integer into `major.minor.patch`. */
  fun decodeVulkanVersion(packed: Int): String {
    val major = (packed ushr 22) and 0x7F
    val minor = (packed ushr 12) and 0x3FF
    val patch = packed and 0xFFF
    return "$major.$minor.$patch"
  }
}

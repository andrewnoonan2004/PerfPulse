package com.droid.perfpulse.ui

import java.util.Locale
import kotlin.math.roundToInt

/** Human-readable formatting helpers (pure, unit-tested). */
object Format {
  fun bytes(bytes: Long?): String {
    if (bytes == null || bytes < 0) return "—"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var idx = 0
    while (value >= 1024 && idx < units.lastIndex) {
      value /= 1024
      idx++
    }
    return if (idx <= 1 || value >= 100) "${value.roundToInt()} ${units[idx]}"
    else String.format(Locale.US, "%.1f %s", value, units[idx])
  }

  fun percent(value: Float?): String = value?.let { "${it.roundToInt()}%" } ?: "—"

  fun freqKHz(khz: Long?): String = when {
    khz == null -> "—"
    khz >= 1_000_000 -> String.format(Locale.US, "%.2f GHz", khz / 1_000_000.0)
    else -> "${khz / 1000} MHz"
  }

  fun freqMHz(mhz: Long?): String = mhz?.let { "$it MHz" } ?: "—"

  fun celsius(value: Float?): String = value?.let { String.format(Locale.US, "%.1f°C", it) } ?: "—"

  fun duration(ms: Long): String {
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val seconds = (ms / 1000) % 60
    return when {
      hours > 0 -> "${hours}h ${minutes}m"
      totalMinutes > 0 -> "${minutes}m ${seconds}s"
      else -> "${seconds}s"
    }
  }

  fun relativeTime(thenMs: Long, nowMs: Long): String {
    val diff = (nowMs - thenMs).coerceAtLeast(0)
    return when {
      diff < 60_000 -> "just now"
      diff < 3_600_000 -> "${diff / 60_000} min ago"
      diff < 86_400_000 -> "${diff / 3_600_000} h ago"
      else -> "${diff / 86_400_000} d ago"
    }
  }

  fun thermalStatus(status: Int?): String = when (status) {
    null -> "Unsupported"
    0 -> "Normal"
    1 -> "Light"
    2 -> "Moderate"
    3 -> "Severe"
    4 -> "Critical"
    5 -> "Emergency"
    6 -> "Shutdown"
    else -> "Unknown"
  }
}

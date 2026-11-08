package com.droid.perfpulse.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CpuMonitorTest {

  @Test
  fun sample_discoversCoresAndCalculatesFrequencyLoadWhenProcStatMissing() {
    val fakeFiles = mapOf(
      "/sys/devices/system/cpu/present" to "0-3",
      "/sys/devices/system/cpu/online" to "0-3",
      "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq" to "1500000",
      "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq" to "1000000",
      "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq" to "2000000",
      "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor" to "schedutil",

      "/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq" to "1000000",
      "/sys/devices/system/cpu/cpu1/cpufreq/scaling_min_freq" to "1000000",
      "/sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq" to "2000000",

      "/sys/devices/system/cpu/cpu2/cpufreq/scaling_cur_freq" to "2000000",
      "/sys/devices/system/cpu/cpu2/cpufreq/scaling_min_freq" to "1000000",
      "/sys/devices/system/cpu/cpu2/cpufreq/scaling_max_freq" to "2000000",

      "/sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq" to "1500000",
      "/sys/devices/system/cpu/cpu3/cpufreq/scaling_min_freq" to "1000000",
      "/sys/devices/system/cpu/cpu3/cpufreq/scaling_max_freq" to "2000000",
    )

    val monitor = CpuMonitor(
      files = FakeFileReader(files = fakeFiles),
      platformSocName = { "Snapdragon 8 Gen 3" },
      abis = listOf("arm64-v8a"),
      processCpuTimeMs = { 500L },
      elapsedRealtimeMs = { 10_000L },
    )

    val snapshot = monitor.sample()
    assertEquals(4, snapshot.coreCount)
    assertEquals(4, snapshot.onlineCount)
    assertEquals("Snapdragon 8 Gen 3", snapshot.socName)
    assertEquals(CpuUsageSource.FREQUENCY, snapshot.source)

    // Core 0: 50%, Core 1: 0%, Core 2: 100%, Core 3: 50% -> average = 50%
    assertNotNull(snapshot.usagePercent)
    assertEquals(50.0f, snapshot.usagePercent!!, 0.5f)
  }

  @Test
  fun sample_usesProcStatWhenAvailable() {
    var statContent = "cpu  100 0 50 850 0 0 0 0"
    val fakeFiles = mutableMapOf(
      "/sys/devices/system/cpu/present" to "0-1",
      "/sys/devices/system/cpu/online" to "0-1",
      "/proc/stat" to statContent,
    )

    val monitor = CpuMonitor(
      files = object : SystemFileReader {
        override fun read(path: String): String? = if (path == "/proc/stat") statContent else fakeFiles[path]
        override fun list(path: String): List<String> = emptyList()
      },
      platformSocName = { "Google Tensor G4" },
      abis = listOf("arm64-v8a"),
      processCpuTimeMs = { 100L },
      elapsedRealtimeMs = { 5_000L },
    )

    // First sample establishes baseline
    val first = monitor.sample()
    // Second sample calculates usage delta
    statContent = "cpu  200 0 100 900 0 0 0 0"
    val second = monitor.sample()

    assertEquals(CpuUsageSource.PROC_STAT, second.source)
    assertNotNull(second.usagePercent)
    // total delta = (300+900) - (150+850) = 1200 - 1000 = 200
    // idle delta = 900 - 850 = 50
    // busy = 150 / 200 = 75%
    assertEquals(75.0f, second.usagePercent!!, 0.5f)
  }
}

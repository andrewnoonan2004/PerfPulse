package com.droid.perfpulse.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryAndThermalMonitorsTest {

  @Test
  fun memoryMonitor_samplesAndCalculatesUsedAndSwap() {
    val meminfo = """
      MemTotal:       12000000 kB
      MemFree:         2000000 kB
      MemAvailable:    6000000 kB
      Cached:          3000000 kB
      Buffers:          500000 kB
      Active:          4000000 kB
      Inactive:        2000000 kB
      SwapTotal:       4000000 kB
      SwapFree:        1000000 kB
    """.trimIndent()

    val monitor = MemoryMonitor(
      files = FakeFileReader(files = mapOf("/proc/meminfo" to meminfo)),
      systemMemory = {
        SystemMemoryInfo(
          totalBytes = 12_000_000_000L,
          availableBytes = 6_000_000_000L,
          thresholdBytes = 500_000_000L,
          lowMemory = false,
        )
      },
    )

    val snapshot = monitor.sample()
    assertEquals(12_000_000_000L, snapshot.totalBytes)
    assertEquals(6_000_000_000L, snapshot.availableBytes)
    assertEquals(6_000_000_000L, snapshot.usedBytes)
    assertEquals(50.0f, snapshot.usedPercent, 0.01f)
    assertFalse(snapshot.isLowMemory)

    // Swap: 4000000 kB total, 1000000 kB free -> 3000000 kB used = 75%
    assertEquals(3_000_000L * 1024, snapshot.swapUsedBytes)
    assertEquals(75.0f, snapshot.swapUsedPercent!!, 0.01f)
  }

  @Test
  fun thermalMonitor_discoversZonesAndPlatformSignals() {
    val fakeDirs = mapOf("/sys/class/thermal" to listOf("thermal_zone0", "thermal_zone1"))
    val fakeFiles = mapOf(
      "/sys/class/thermal/thermal_zone0/temp" to "36500",
      "/sys/class/thermal/thermal_zone0/type" to "cpu-thermal",
      "/sys/class/thermal/thermal_zone1/temp" to "41000",
      "/sys/class/thermal/thermal_zone1/type" to "gpu-thermal",
    )

    val monitor = ThermalMonitor(
      files = FakeFileReader(files = fakeFiles, dirs = fakeDirs),
      platform = { PlatformThermal(batteryCelsius = 34.2f, status = 0, headroom = 0.85f) },
    )

    val snapshot = monitor.sample()
    assertEquals(34.2f, snapshot.batteryCelsius!!, 0.01f)
    assertEquals(0, snapshot.status)
    assertEquals(0.85f, snapshot.headroom!!, 0.01f)
    assertEquals(2, snapshot.zones.size)
    assertEquals("gpu-thermal", snapshot.hottestZone?.name)
    assertEquals(41.0f, snapshot.hottestZone?.celsius!!, 0.01f)
  }
}

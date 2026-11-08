package com.droid.perfpulse.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GpuMonitorTest {

  @Test
  fun sample_probesAdrenoKgslNodes() {
    val fakeFiles = mapOf(
      "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage" to "45 %",
      "/sys/class/kgsl/kgsl-3d0/gpuclk" to "650000000",
      "/sys/class/kgsl/kgsl-3d0/max_gpuclk" to "900000000",
      "/sys/class/kgsl/kgsl-3d0/min_clock_mhz" to "220",
    )

    val monitor = GpuMonitor(
      files = FakeFileReader(files = fakeFiles),
      staticInfo = {
        GpuStaticInfo(
          renderer = "Adreno (TM) 750",
          vendor = "Qualcomm",
          glVersion = "OpenGL ES 3.2 V@0615.0",
          vulkanVersion = "1.3.268",
          glEsVersion = "3.2",
        )
      },
    )

    val snapshot = monitor.sample()
    assertEquals("Adreno (TM) 750", snapshot.info.renderer)
    assertEquals(45f, snapshot.utilizationPercent!!, 0.01f)
    assertEquals(650L, snapshot.curFreqMHz)
    assertEquals(900L, snapshot.maxFreqMHz)
    assertEquals(220L, snapshot.minFreqMHz)
    assertEquals("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage", snapshot.utilizationSource)
  }

  @Test
  fun sample_probesMaliPlatformNodes() {
    val fakeDirs = mapOf(
      "/sys/devices/platform" to listOf("1c500000.mali"),
      "/sys/class/devfreq" to listOf("1c500000.mali"),
    )
    val fakeFiles = mapOf(
      "/sys/devices/platform/1c500000.mali/utilization" to "38",
      "/sys/class/devfreq/1c500000.mali/cur_freq" to "850000000",
      "/sys/class/devfreq/1c500000.mali/max_freq" to "1000000000",
      "/sys/class/devfreq/1c500000.mali/min_freq" to "300000000",
    )

    val monitor = GpuMonitor(
      files = FakeFileReader(files = fakeFiles, dirs = fakeDirs),
      staticInfo = {
        GpuStaticInfo(
          renderer = "Mali-G715 Immortalis",
          vendor = "ARM",
          glVersion = "OpenGL ES 3.2",
          vulkanVersion = "1.3.0",
          glEsVersion = "3.2",
        )
      },
    )

    val snapshot = monitor.sample()
    assertEquals("Mali-G715 Immortalis", snapshot.info.renderer)
    assertEquals(38f, snapshot.utilizationPercent!!, 0.01f)
    assertEquals(850L, snapshot.curFreqMHz)
  }

  @Test
  fun sample_gracefullyHandlesRestrictedNodesWithFallback() {
    val monitor = GpuMonitor(
      files = FakeFileReader(),
      staticInfo = {
        GpuStaticInfo(
          renderer = "Qualcomm Adreno",
          vendor = "Qualcomm",
          glVersion = "OpenGL ES 3.2",
          vulkanVersion = null,
          glEsVersion = "3.2",
        )
      },
    )

    val snapshot = monitor.sample()
    assertEquals("Qualcomm Adreno", snapshot.info.renderer)
    assertNull(snapshot.utilizationPercent)
    assertNull(snapshot.curFreqMHz)
    assertNull(snapshot.displayLoadPercent)
  }
}

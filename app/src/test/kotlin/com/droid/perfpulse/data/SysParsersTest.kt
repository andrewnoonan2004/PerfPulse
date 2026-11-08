package com.droid.perfpulse.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SysParsersTest {

  @Test
  fun parseMemInfo_parsesKeysAndValuesInKb() {
    val sample = """
      MemTotal:       16384000 kB
      MemFree:         4096000 kB
      MemAvailable:    8192000 kB
      Cached:          2048000 kB
      SwapTotal:       4000000 kB
      SwapFree:        3000000 kB
    """.trimIndent()

    val map = SysParsers.parseMemInfo(sample)
    assertEquals(16384000L, map["MemTotal"])
    assertEquals(4096000L, map["MemFree"])
    assertEquals(8192000L, map["MemAvailable"])
    assertEquals(2048000L, map["Cached"])
    assertEquals(4000000L, map["SwapTotal"])
    assertEquals(3000000L, map["SwapFree"])
  }

  @Test
  fun parseCpuRange_expandsRangesAndSingleCores() {
    val range = "0-3,5,7-9"
    val cores = SysParsers.parseCpuRange(range)
    assertEquals(listOf(0, 1, 2, 3, 5, 7, 8, 9), cores)

    val single = "0"
    assertEquals(listOf(0), SysParsers.parseCpuRange(single))

    val empty = ""
    assertEquals(emptyList<Int>(), SysParsers.parseCpuRange(empty))
  }

  @Test
  fun parseProcStatTotal_computesIdleAndTotal() {
    val stat = """
      cpu  100 20 50 800 30 10 5 0 0 0
      cpu0 50 10 25 400 15 5 2 0 0 0
    """.trimIndent()

    val times = SysParsers.parseProcStatTotal(stat)
    assertNotNull(times)
    // idle: values[3](800) + values[4](30) = 830
    assertEquals(830L, times!!.idle)
    // total: 100+20+50+800+30+10+5+0 = 1015
    assertEquals(1015L, times.total)
  }

  @Test
  fun cpuUsageBetween_calculatesAccuratePercent() {
    val prev = SysParsers.CpuTimes(idle = 800L, total = 1000L)
    val cur = SysParsers.CpuTimes(idle = 850L, total = 1100L)
    // total delta = 100, idle delta = 50 -> (100-50)/100 = 50%
    val usage = SysParsers.cpuUsageBetween(prev, cur)
    assertNotNull(usage)
    assertEquals(50.0f, usage!!, 0.01f)
  }

  @Test
  fun frequencyLoad_scalesBetweenMinAndMax() {
    assertEquals(50f, SysParsers.frequencyLoad(cur = 1500L, min = 1000L, max = 2000L)!!, 0.01f)
    assertEquals(0f, SysParsers.frequencyLoad(cur = 1000L, min = 1000L, max = 2000L)!!, 0.01f)
    assertEquals(100f, SysParsers.frequencyLoad(cur = 2000L, min = 1000L, max = 2000L)!!, 0.01f)
    assertNull(SysParsers.frequencyLoad(cur = null, min = 1000L, max = 2000L))
  }

  @Test
  fun parseGpuBusy_supportsVariousVendorFormats() {
    // Adreno percentage format: "42 %"
    assertEquals(42f, SysParsers.parseGpuBusy("42 %")!!, 0.01f)

    // Adreno busy total counters: "120 400" -> 120/400 = 30%
    assertEquals(30f, SysParsers.parseGpuBusy("120 400", isBusyTotalPair = true)!!, 0.01f)

    // Mali utilization: "65"
    assertEquals(65f, SysParsers.parseGpuBusy("65")!!, 0.01f)

    // MediaTek format: "78 0 0"
    assertEquals(78f, SysParsers.parseGpuBusy("78 0 0")!!, 0.01f)

    assertNull(SysParsers.parseGpuBusy("unreadable"))
  }

  @Test
  fun normalizeGpuFreqToMHz_normalizesHzAndKhz() {
    assertEquals(800L, SysParsers.normalizeGpuFreqToMHz(800_000_000L)) // Hz -> MHz
    assertEquals(650L, SysParsers.normalizeGpuFreqToMHz(650_000L))     // kHz -> MHz
    assertEquals(585L, SysParsers.normalizeGpuFreqToMHz(585L))         // already MHz
    assertNull(SysParsers.normalizeGpuFreqToMHz(0L))
  }

  @Test
  fun normalizeTemperature_handlesMilliDegreesAndDegrees() {
    assertEquals(42.5f, SysParsers.normalizeTemperature(42500L)!!, 0.01f)
    assertEquals(38.0f, SysParsers.normalizeTemperature(38L)!!, 0.01f)
    assertNull(SysParsers.normalizeTemperature(500_000L)) // unrealistic temp
  }

  @Test
  fun decodeVulkanVersion_decodesPackedVersion() {
    // Vulkan 1.3.260: major=1, minor=3, patch=260
    // (1 shl 22) or (3 shl 12) or 260
    val packed = (1 shl 22) or (3 shl 12) or 260
    assertEquals("1.3.260", SysParsers.decodeVulkanVersion(packed))
  }
}

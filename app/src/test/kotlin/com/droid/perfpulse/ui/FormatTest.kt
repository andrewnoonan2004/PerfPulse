package com.droid.perfpulse.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

  @Test
  fun bytes_formatsAccurateUnits() {
    assertEquals("—", Format.bytes(null))
    assertEquals("500 B", Format.bytes(500L))
    assertEquals("2 KB", Format.bytes(2048L))
    assertEquals("64.0 MB", Format.bytes(64L * 1024 * 1024))
    assertEquals("7.5 GB", Format.bytes((7.5 * 1024 * 1024 * 1024).toLong()))
  }

  @Test
  fun freqKHz_formatsGigaAndMegaHertz() {
    assertEquals("—", Format.freqKHz(null))
    assertEquals("800 MHz", Format.freqKHz(800_000L))
    assertEquals("2.40 GHz", Format.freqKHz(2_400_000L))
    assertEquals("3.19 GHz", Format.freqKHz(3_192_000L))
  }

  @Test
  fun duration_formatsHumanReadableTimes() {
    assertEquals("45s", Format.duration(45_000L))
    assertEquals("12m 30s", Format.duration(12 * 60_000L + 30_000L))
    assertEquals("2h 15m", Format.duration(2 * 3600_000L + 15 * 60_000L))
  }

  @Test
  fun relativeTime_formatsPastMoments() {
    val now = 1_000_000_000L
    assertEquals("just now", Format.relativeTime(now - 15_000L, now))
    assertEquals("5 min ago", Format.relativeTime(now - 5 * 60_000L, now))
    assertEquals("3 h ago", Format.relativeTime(now - 3 * 3600_000L, now))
  }

  @Test
  fun thermalStatus_mapsPlatformStatusCodes() {
    assertEquals("Normal", Format.thermalStatus(0))
    assertEquals("Moderate", Format.thermalStatus(2))
    assertEquals("Critical", Format.thermalStatus(4))
    assertEquals("Unsupported", Format.thermalStatus(null))
  }
}

package com.droid.perfpulse.data

/**
 * Samples GPU load and clocks from vendor-specific kernel nodes (Adreno/kgsl, Mali, PowerVR,
 * MediaTek GED). Which nodes are readable depends on the device's SELinux policy; when none are
 * accessible the snapshot reports null and the UI explains why.
 */
class GpuMonitor(
  private val files: SystemFileReader,
  private val staticInfo: () -> GpuStaticInfo,
) {
  private data class BusyNode(val path: String, val pair: Boolean)

  private var probed = false
  private var busyNode: BusyNode? = null
  private var freqNode: String? = null
  private var minFreqMHz: Long? = null
  private var maxFreqMHz: Long? = null
  private var info: GpuStaticInfo? = null

  fun sample(): GpuSnapshot {
    if (!probed) probe()
    val gpuInfo = info ?: staticInfo().also { info = it }

    val util = busyNode?.let { node ->
      files.read(node.path)?.let { SysParsers.parseGpuBusy(it, node.pair) }
    }
    val cur = freqNode?.let { path ->
      files.read(path)?.let { firstNumber(it) }?.let { SysParsers.normalizeGpuFreqToMHz(it) }
    }

    return GpuSnapshot(
      info = gpuInfo,
      utilizationPercent = util,
      curFreqMHz = cur,
      minFreqMHz = minFreqMHz,
      maxFreqMHz = maxFreqMHz,
      utilizationSource = busyNode?.path?.takeIf { util != null },
    )
  }

  private fun probe() {
    probed = true
    busyNode = busyCandidates().firstOrNull { node ->
      files.read(node.path)?.let { SysParsers.parseGpuBusy(it, node.pair) } != null
    }

    val devfreqDirs = devfreqGpuDirs()
    freqNode = (FIXED_FREQ_NODES + devfreqDirs.map { "$it/cur_freq" })
      .firstOrNull { path -> files.read(path)?.let { firstNumber(it) }?.let { it > 0 } == true }

    val maxCandidates = FIXED_MAX_NODES + devfreqDirs.map { "$it/max_freq" }
    maxFreqMHz = maxCandidates.firstNotNullOfOrNull { path ->
      files.read(path)?.let { firstNumber(it) }?.let { SysParsers.normalizeGpuFreqToMHz(it) }
    } ?: devfreqDirs.firstNotNullOfOrNull { dir ->
      files.read("$dir/available_frequencies")?.let { allNumbers(it).maxOrNull() }
        ?.let { SysParsers.normalizeGpuFreqToMHz(it) }
    }
    minFreqMHz = (FIXED_MIN_NODES + devfreqDirs.map { "$it/min_freq" }).firstNotNullOfOrNull { path ->
      files.read(path)?.let { firstNumber(it) }?.let { SysParsers.normalizeGpuFreqToMHz(it) }
    }
  }

  private fun busyCandidates(): List<BusyNode> {
    val fixed = listOf(
      BusyNode("$KGSL/gpu_busy_percentage", false),
      BusyNode("$KGSL/gpubusy", true),
      BusyNode("/sys/kernel/gpu/gpu_busy", false),
      BusyNode("/sys/class/misc/mali0/device/utilization", false),
      BusyNode("/sys/class/misc/mali0/device/utilisation", false),
      BusyNode("/sys/devices/platform/mali.0/utilization", false),
      BusyNode("/sys/kernel/ged/hal/gpu_utilization", false),
      BusyNode("/sys/module/ged/parameters/gpu_loading", false),
      BusyNode("/sys/module/pvrsrvkm/parameters/gpu_loading", false),
      BusyNode("/proc/mali/utilization", false),
    )
    val platformMali = files.list("/sys/devices/platform")
      .filter { it.contains("mali", ignoreCase = true) || it.contains("gpu", ignoreCase = true) }
      .flatMap { dir ->
        listOf(
          BusyNode("/sys/devices/platform/$dir/utilization", false),
          BusyNode("/sys/devices/platform/$dir/utilisation", false),
        )
      }
    return fixed + platformMali
  }

  private fun devfreqGpuDirs(): List<String> =
    files.list(DEVFREQ)
      .filter { name -> GPU_HINTS.any { name.contains(it, ignoreCase = true) } }
      .map { "$DEVFREQ/$it" } + listOf("$KGSL/devfreq")

  private fun firstNumber(text: String): Long? = allNumbers(text).firstOrNull()

  private fun allNumbers(text: String): List<Long> =
    Regex("\\d+").findAll(text).mapNotNull { it.value.toLongOrNull() }.toList()

  companion object {
    private const val KGSL = "/sys/class/kgsl/kgsl-3d0"
    private const val DEVFREQ = "/sys/class/devfreq"
    private val GPU_HINTS = listOf("gpu", "mali", "kgsl", "3d0", "pvr", "g3d")
    private val FIXED_FREQ_NODES = listOf(
      "$KGSL/gpuclk",
      "/sys/kernel/gpu/gpu_clock",
      "/sys/class/misc/mali0/device/clock",
      "/sys/kernel/ged/hal/current_freqency",
    )
    private val FIXED_MAX_NODES = listOf(
      "$KGSL/max_gpuclk",
      "/sys/kernel/gpu/gpu_max_clock",
    )
    private val FIXED_MIN_NODES = listOf(
      "$KGSL/min_clock_mhz",
      "/sys/kernel/gpu/gpu_min_clock",
    )
  }
}

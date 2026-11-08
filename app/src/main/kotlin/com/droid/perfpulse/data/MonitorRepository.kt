package com.droid.perfpulse.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** Source of a periodically refreshed [SystemSnapshot] stream. */
interface MonitorRepository {
  fun snapshots(intervalMs: Long): Flow<SystemSnapshot>

  /** Forces the next sample to re-check usage access (e.g. after returning from Settings). */
  fun invalidateProcesses()
}

/** Process/app data source, abstracted for tests. */
interface ProcessSource {
  fun ownProcess(): OwnProcessInfo?
  fun hasUsageAccess(): Boolean
  fun recentApps(): List<AppUsage>
}

class DefaultMonitorRepository(
  private val cpu: CpuMonitor,
  private val gpu: GpuMonitor,
  private val memory: MemoryMonitor,
  private val thermal: ThermalMonitor,
  private val processes: ProcessSource,
  private val clock: () -> Long = System::currentTimeMillis,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  /** Process stats are heavier; refresh them every N ticks. */
  private val processEveryTicks: Int = 3,
) : MonitorRepository {

  @Volatile private var processDirty = true

  override fun invalidateProcesses() {
    processDirty = true
  }

  override fun snapshots(intervalMs: Long): Flow<SystemSnapshot> = flow {
    var tick = 0
    var lastProcesses = ProcessSnapshot.Empty
    while (true) {
      if (processDirty || tick % processEveryTicks == 0) {
        processDirty = false
        lastProcesses = sampleProcesses()
      }
      emit(
        SystemSnapshot(
          timestampMs = clock(),
          cpu = cpu.sample(),
          gpu = gpu.sample(),
          memory = memory.sample(),
          thermal = thermal.sample(),
          processes = lastProcesses,
        ),
      )
      tick++
      delay(intervalMs)
    }
  }.flowOn(ioDispatcher)

  private fun sampleProcesses(): ProcessSnapshot {
    val access = runCatching { processes.hasUsageAccess() }.getOrDefault(false)
    return ProcessSnapshot(
      own = runCatching { processes.ownProcess() }.getOrNull(),
      hasUsageAccess = access,
      apps = if (access) runCatching { processes.recentApps() }.getOrDefault(emptyList()) else emptyList(),
    )
  }
}

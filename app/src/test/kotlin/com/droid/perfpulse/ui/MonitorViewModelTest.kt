package com.droid.perfpulse.ui

import com.droid.perfpulse.data.CpuSnapshot
import com.droid.perfpulse.data.CpuUsageSource
import com.droid.perfpulse.data.GpuSnapshot
import com.droid.perfpulse.data.MemorySnapshot
import com.droid.perfpulse.data.MonitorRepository
import com.droid.perfpulse.data.ProcessSnapshot
import com.droid.perfpulse.data.SystemSnapshot
import com.droid.perfpulse.data.ThermalSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MonitorViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun append_accumulatesHistoryWithinCapacity() {
    var state = MonitorUiState()
    for (i in 1..70) {
      val snapshot = createTestSnapshot(cpu = i.toFloat(), memoryUsedPercent = (i % 50).toFloat())
      state = state.append(snapshot, max = 60)
    }

    // Should cap at 60 points
    assertEquals(60, state.cpuHistory.size)
    assertEquals(60, state.memoryHistory.size)
    // Most recent element matches sample #70
    assertEquals(70f, state.cpuHistory.last(), 0.01f)
  }

  @Test
  fun togglePause_togglesPauseState() = runTest {
    val repo = object : MonitorRepository {
      override fun snapshots(intervalMs: Long): Flow<SystemSnapshot> = flow {}
      override fun invalidateProcesses() {}
    }

    val viewModel = MonitorViewModel(repo)
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect()
    }
    testScheduler.runCurrent()
    assertFalse(viewModel.uiState.value.paused)

    viewModel.togglePause()
    testScheduler.runCurrent()
    assertTrue(viewModel.uiState.value.paused)

    viewModel.togglePause()
    testScheduler.runCurrent()
    assertFalse(viewModel.uiState.value.paused)
  }

  @Test
  fun setInterval_updatesIntervalWithinLimits() = runTest {
    val repo = object : MonitorRepository {
      override fun snapshots(intervalMs: Long): Flow<SystemSnapshot> = flow {}
      override fun invalidateProcesses() {}
    }

    val viewModel = MonitorViewModel(repo)
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect()
    }
    testScheduler.runCurrent()
    assertEquals(1000L, viewModel.uiState.value.intervalMs)

    viewModel.setInterval(500L)
    testScheduler.runCurrent()
    assertEquals(500L, viewModel.uiState.value.intervalMs)

    // Coerces to min 250ms
    viewModel.setInterval(50L)
    testScheduler.runCurrent()
    assertEquals(250L, viewModel.uiState.value.intervalMs)
  }

  private fun createTestSnapshot(cpu: Float, memoryUsedPercent: Float): SystemSnapshot {
    return SystemSnapshot(
      timestampMs = System.currentTimeMillis(),
      cpu = CpuSnapshot(
        socName = "Test SoC",
        abis = listOf("arm64-v8a"),
        cores = emptyList(),
        usagePercent = cpu,
        source = CpuUsageSource.FREQUENCY,
        appCpuPercent = 1.5f,
      ),
      gpu = GpuSnapshot.Empty,
      memory = MemorySnapshot(
        totalBytes = 1000L,
        availableBytes = (1000L * (1f - memoryUsedPercent / 100f)).toLong(),
        freeBytes = null,
        cachedBytes = null,
        buffersBytes = null,
        activeBytes = null,
        inactiveBytes = null,
        shmemBytes = null,
        swapTotalBytes = null,
        swapFreeBytes = null,
        lowMemoryThresholdBytes = 100L,
        isLowMemory = false,
      ),
      thermal = ThermalSnapshot.Empty,
      processes = ProcessSnapshot.Empty,
    )
  }
}

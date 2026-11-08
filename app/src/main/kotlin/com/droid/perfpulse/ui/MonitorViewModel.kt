package com.droid.perfpulse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.droid.perfpulse.data.MonitorRepository
import com.droid.perfpulse.data.SystemSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

data class MonitorUiState(
  val snapshot: SystemSnapshot? = null,
  val cpuHistory: List<Float> = emptyList(),
  val gpuHistory: List<Float> = emptyList(),
  val memoryHistory: List<Float> = emptyList(),
  val coreHistory: Map<Int, List<Float>> = emptyMap(),
  val paused: Boolean = false,
  val intervalMs: Long = MonitorViewModel.DEFAULT_INTERVAL_MS,
) {
  /** Appends a new sample to the rolling histories, keeping at most [max] points each. */
  fun append(s: SystemSnapshot, max: Int): MonitorUiState {
    fun List<Float>.push(v: Float?): List<Float> = if (v == null) this else (this + v).takeLast(max)
    val cores = s.cpu.cores.associate { core ->
      core.id to (coreHistory[core.id].orEmpty()).push(core.loadPercent ?: 0f)
    }
    return copy(
      snapshot = s,
      cpuHistory = cpuHistory.push(s.cpu.usagePercent),
      gpuHistory = gpuHistory.push(s.gpu.displayLoadPercent),
      memoryHistory = memoryHistory.push(s.memory.usedPercent.takeIf { s.memory.totalBytes > 0 }),
      coreHistory = cores,
    )
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MonitorViewModel(
  private val repository: MonitorRepository,
  initialState: MonitorUiState = MonitorUiState(),
) : ViewModel() {
  private val paused = MutableStateFlow(false)
  private val interval = MutableStateFlow(DEFAULT_INTERVAL_MS)

  private val snapshotFlow: Flow<SystemSnapshot> =
    combine(paused, interval) { p, i -> p to i }
      .flatMapLatest { (p, i) -> if (p) emptyFlow() else repository.snapshots(i) }

  private val historyFlow: Flow<MonitorUiState> =
    snapshotFlow.scan(initialState) { acc, snap ->
      acc.append(snap, HISTORY_SIZE)
    }

  val uiState: StateFlow<MonitorUiState> =
    combine(historyFlow, paused, interval) { state, p, i ->
      state.copy(paused = p, intervalMs = i)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState)

  fun togglePause() {
    paused.value = !paused.value
  }

  fun setInterval(ms: Long) {
    interval.value = ms.coerceIn(250L, 10_000L)
  }

  fun onReturnedFromSettings() {
    repository.invalidateProcesses()
  }

  class Factory(private val repository: MonitorRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MonitorViewModel(repository) as T
  }

  companion object {
    const val HISTORY_SIZE = 60
    const val DEFAULT_INTERVAL_MS = 1_000L
    val INTERVAL_OPTIONS = listOf(500L, 1_000L, 2_000L)
  }
}

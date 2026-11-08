package com.droid.perfpulse.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.droid.perfpulse.data.CpuSnapshot
import com.droid.perfpulse.data.CpuUsageSource
import com.droid.perfpulse.data.GpuSnapshot
import com.droid.perfpulse.data.GpuStaticInfo
import com.droid.perfpulse.data.MemorySnapshot
import com.droid.perfpulse.data.MonitorRepository
import com.droid.perfpulse.data.OwnProcessInfo
import com.droid.perfpulse.data.ProcessSnapshot
import com.droid.perfpulse.data.SystemSnapshot
import com.droid.perfpulse.data.ThermalSnapshot
import com.droid.perfpulse.ui.theme.PerfPulseTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class PerfPulseUiTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun appRenders_andNavigatesBetweenScreens() {
    val fakeSnapshot = SystemSnapshot(
      timestampMs = 123456789L,
      cpu = CpuSnapshot(
        socName = "Tensor G4",
        abis = listOf("arm64-v8a"),
        cores = emptyList(),
        usagePercent = 42f,
        source = CpuUsageSource.FREQUENCY,
        appCpuPercent = 2.1f,
      ),
      gpu = GpuSnapshot(
        info = GpuStaticInfo("Mali-G715", "ARM", "OpenGL ES 3.2", "1.3.0", "3.2"),
        utilizationPercent = 35f,
        curFreqMHz = 700L,
        minFreqMHz = 300L,
        maxFreqMHz = 900L,
        utilizationSource = "/sys/kernel/gpu/gpu_busy",
      ),
      memory = MemorySnapshot(
        totalBytes = 16_000_000_000L,
        availableBytes = 8_000_000_000L,
        freeBytes = 2_000_000_000L,
        cachedBytes = 4_000_000_000L,
        buffersBytes = 500_000_000L,
        activeBytes = 5_000_000_000L,
        inactiveBytes = 3_000_000_000L,
        shmemBytes = 100_000_000L,
        swapTotalBytes = 4_000_000_000L,
        swapFreeBytes = 2_000_000_000L,
        lowMemoryThresholdBytes = 1_000_000_000L,
        isLowMemory = false,
      ),
      thermal = ThermalSnapshot.Empty,
      processes = ProcessSnapshot(
        own = OwnProcessInfo(
          pid = 1234,
          uid = 10001,
          processName = "com.droid.perfpulse",
          totalPssKb = 35000L,
          javaHeapKb = 15000L,
          nativeHeapKb = 20000L,
          threads = 12L,
          cpuTimeMs = 5000L,
          uptimeMs = 60000L,
        ),
        hasUsageAccess = true,
        apps = emptyList(),
      ),
    )

    val fakeRepository = object : MonitorRepository {
      override fun snapshots(intervalMs: Long): Flow<SystemSnapshot> = flowOf(fakeSnapshot)
      override fun invalidateProcesses() {}
    }

    val viewModel = MonitorViewModel(
      repository = fakeRepository,
      initialState = MonitorUiState(snapshot = fakeSnapshot),
    )

    composeTestRule.setContent {
      PerfPulseTheme(dynamicColor = false) {
        MainApp(viewModel = viewModel)
      }
    }

    // TopAppBar title
    composeTestRule.onNodeWithText("PerfPulse").assertIsDisplayed()

    // Overview screen shows CPU and GPU cards by unique hardware subtitles
    composeTestRule.onNodeWithText("Tensor G4").assertExists()
    composeTestRule.onNodeWithText("Mali-G715").assertExists()

    // Navigate to CPU screen via navigation item
    composeTestRule.onNodeWithContentDescription("CPU", useUnmergedTree = true).performClick()
    composeTestRule.onNodeWithText("Central Processing Unit").assertExists()

    // Navigate to GPU screen via navigation item
    composeTestRule.onNodeWithContentDescription("GPU", useUnmergedTree = true).performClick()
    composeTestRule.onNodeWithText("Graphics Processing Unit").assertExists()

    // Navigate to RAM screen via navigation item
    composeTestRule.onNodeWithContentDescription("RAM", useUnmergedTree = true).performClick()
    composeTestRule.onNodeWithText("Random Access Memory").assertExists()

    // Navigate to Processes screen via navigation item
    composeTestRule.onNodeWithContentDescription("Processes", useUnmergedTree = true).performClick()
    composeTestRule.onNodeWithText("Process Telemetry").assertExists()
    composeTestRule.onNodeWithText("Monitor Runtime Vitals").assertExists()
  }
}

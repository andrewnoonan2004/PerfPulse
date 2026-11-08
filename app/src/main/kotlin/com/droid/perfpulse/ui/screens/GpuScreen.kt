package com.droid.perfpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droid.perfpulse.R
import com.droid.perfpulse.ui.Format
import com.droid.perfpulse.ui.MonitorUiState
import com.droid.perfpulse.ui.components.ExpressiveGaugeCard
import com.droid.perfpulse.ui.components.ExpressiveProgressBar
import com.droid.perfpulse.ui.components.ExpressiveShapeType
import com.droid.perfpulse.ui.components.SparklineChart
import com.droid.perfpulse.ui.components.StatTile
import com.droid.perfpulse.ui.components.StatusBadge

@Composable
fun GpuScreen(
  uiState: MonitorUiState,
  modifier: Modifier = Modifier,
) {
  val gpu = uiState.snapshot?.gpu

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 680.dp
    val gridColumns = if (maxWidth >= 1000.dp) 3 else if (isWideScreen) 2 else 1

    if (gpu == null) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = "Probing GPU telemetry and OpenGL subsystem...",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      return@BoxWithConstraints
    }

    LazyVerticalGrid(
      columns = GridCells.Fixed(gridColumns),
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.fillMaxSize(),
    ) {
      // 1. GPU Header Card
      item(span = { GridItemSpan(gridColumns) }) {
        val renderer = gpu.info.renderer ?: "Hardware Graphics Processor"
        val vendor = gpu.info.vendor ?: "Integrated Vendor"
        ExpressiveGaugeCard(
          title = "Graphics Processing Unit",
          subtitle = "$renderer • $vendor",
          valuePercent = gpu.displayLoadPercent,
          detail = if (gpu.utilizationPercent != null) "Real-time Hardware Utilization (${gpu.utilizationSource})"
          else if (gpu.curFreqMHz != null) "Clock-scaled Dynamic Frequency Load"
          else "OpenGL ES Pipeline Online • Sysfs Telemetry Sandboxed",
          icon = painterResource(R.drawable.ic_nav_gpu),
          accentColor = MaterialTheme.colorScheme.tertiary,
          shapeType = ExpressiveShapeType.Sunny,
        )
      }

      // 2. Frequency Clocks
      item(span = { GridItemSpan(gridColumns) }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "GPU Clock Frequencies",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              if (gpu.curFreqMHz != null) {
                StatusBadge(text = "${gpu.curFreqMHz} MHz", isAlert = false)
              } else {
                StatusBadge(text = "DYNAMIC", isAlert = false)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            ExpressiveProgressBar(
              label = "Operating Frequency",
              valueText = Format.freqMHz(gpu.curFreqMHz),
              progress = gpu.displayLoadPercent,
              color = MaterialTheme.colorScheme.tertiary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              StatTile(
                label = "Minimum Clock",
                value = Format.freqMHz(gpu.minFreqMHz),
                modifier = Modifier.weight(1f),
              )
              StatTile(
                label = "Maximum Clock",
                value = Format.freqMHz(gpu.maxFreqMHz),
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }

      // 3. Live GPU History Chart
      item(span = { GridItemSpan(gridColumns) }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "GPU Activity Trend",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
            SparklineChart(
              history = uiState.gpuHistory,
              modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
              lineColor = MaterialTheme.colorScheme.tertiary,
            )
          }
        }
      }

      // 4. Graphics Subsystem Specs
      item(span = { GridItemSpan(gridColumns) }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Graphics Driver & API Specifications",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              StatTile(
                label = "Renderer",
                value = gpu.info.renderer ?: "Hardware ES",
                secondaryValue = gpu.info.vendor,
                modifier = Modifier.weight(1f),
              )
              StatTile(
                label = "Vulkan Version",
                value = gpu.info.vulkanVersion ?: "Unsupported",
                secondaryValue = "Vulkan Hardware Feature",
                modifier = Modifier.weight(1f),
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              StatTile(
                label = "OpenGL ES Version",
                value = gpu.info.glEsVersion ?: "3.2",
                secondaryValue = gpu.info.glVersion,
                modifier = Modifier.weight(1f),
              )
              StatTile(
                label = "Driver Node",
                value = gpu.utilizationSource?.substringAfterLast('/') ?: "Hardware Pipeline",
                secondaryValue = if (gpu.utilizationSource != null) "Direct Kernel Feed" else "Platform Sandbox",
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }

      // 5. Android Sandbox Note
      item(span = { GridItemSpan(gridColumns) }) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
          ) {
            Icon(
              painter = painterResource(R.drawable.ic_info),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(end = 12.dp, top = 2.dp),
            )
            Column {
              Text(
                text = "Android GPU Telemetry Architecture",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Android enforces strict SELinux sandboxing on non-rooted apps. On Qualcomm Adreno, ARM Mali, and MediaTek devices, PerfPulse reads vendor sysfs nodes (/sys/class/kgsl, devfreq, ged). When vendor kernel nodes are restricted by OEM firmware, PerfPulse estimates activity via frequency governor scaling and GLES pipeline metrics.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }
}

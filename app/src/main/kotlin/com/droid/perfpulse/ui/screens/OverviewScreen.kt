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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droid.perfpulse.R
import com.droid.perfpulse.data.SystemSnapshot
import com.droid.perfpulse.ui.Format
import com.droid.perfpulse.ui.MonitorUiState
import com.droid.perfpulse.ui.components.ExpressiveGaugeCard
import com.droid.perfpulse.ui.components.ExpressiveShapeType
import com.droid.perfpulse.ui.components.SparklineChart
import com.droid.perfpulse.ui.components.StatTile
import com.droid.perfpulse.ui.components.StatusBadge

@Composable
fun OverviewScreen(
  uiState: MonitorUiState,
  onNavigateToCpu: () -> Unit,
  onNavigateToGpu: () -> Unit,
  onNavigateToMemory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val snapshot = uiState.snapshot

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 680.dp
    val gridColumns = if (maxWidth >= 1000.dp) 3 else if (isWideScreen) 2 else 1

    if (snapshot == null) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = "Initializing performance monitors...",
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
      // 1. CPU Card
      item(span = { GridItemSpan(1) }) {
        val cpuLoad = snapshot.cpu.usagePercent
        val online = snapshot.cpu.onlineCount
        val total = snapshot.cpu.coreCount
        ExpressiveGaugeCard(
          title = "CPU",
          subtitle = snapshot.cpu.socName,
          valuePercent = cpuLoad,
          detail = "$online/$total Cores Online • ${snapshot.cpu.source.name.lowercase()}",
          icon = painterResource(R.drawable.ic_nav_cpu),
          accentColor = MaterialTheme.colorScheme.primary,
          shapeType = ExpressiveShapeType.Cookie,
          onClick = onNavigateToCpu,
        )
      }

      // 2. GPU Card
      item(span = { GridItemSpan(1) }) {
        val gpuLoad = snapshot.gpu.displayLoadPercent
        val renderer = snapshot.gpu.info.renderer ?: "Hardware GPU"
        val clockStr = snapshot.gpu.curFreqMHz?.let { "$it MHz" } ?: "Dynamic Clock"
        ExpressiveGaugeCard(
          title = "GPU",
          subtitle = renderer,
          valuePercent = gpuLoad,
          detail = "$clockStr • ${if (snapshot.gpu.isEstimated) "Estimated Load" else "Hardware Telemetry"}",
          icon = painterResource(R.drawable.ic_nav_gpu),
          accentColor = MaterialTheme.colorScheme.tertiary,
          shapeType = ExpressiveShapeType.Sunny,
          onClick = onNavigateToGpu,
        )
      }

      // 3. Memory Card
      item(span = { GridItemSpan(1) }) {
        val mem = snapshot.memory
        val usedPercent = mem.usedPercent
        val usedStr = Format.bytes(mem.usedBytes)
        val totalStr = Format.bytes(mem.totalBytes)
        val swapStr = mem.swapUsedBytes?.let { Format.bytes(it) } ?: "N/A"
        ExpressiveGaugeCard(
          title = "RAM",
          subtitle = "$usedStr / $totalStr used",
          valuePercent = usedPercent,
          detail = "Swap/ZRAM: $swapStr • Free: ${Format.bytes(mem.availableBytes)}",
          icon = painterResource(R.drawable.ic_nav_memory),
          accentColor = MaterialTheme.colorScheme.secondary,
          shapeType = ExpressiveShapeType.Clover,
          onClick = onNavigateToMemory,
        )
      }

      // 4. Live Wave Telemetry Sparklines
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
              Column {
                Text(
                  text = "Live Activity Telemetry",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Real-time rolling history (last 60s)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              if (uiState.paused) {
                StatusBadge(text = "PAUSED", isAlert = true)
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-stream sparkline charts
            Text(
              text = "CPU Load: ${Format.percent(snapshot.cpu.usagePercent)}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            SparklineChart(
              history = uiState.cpuHistory,
              modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
              lineColor = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "GPU Load: ${Format.percent(snapshot.gpu.displayLoadPercent)}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.tertiary,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            SparklineChart(
              history = uiState.gpuHistory,
              modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
              lineColor = MaterialTheme.colorScheme.tertiary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "RAM Usage: ${Format.percent(snapshot.memory.usedPercent)}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.secondary,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            SparklineChart(
              history = uiState.memoryHistory,
              modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
              lineColor = MaterialTheme.colorScheme.secondary,
            )
          }
        }
      }

      // 5. System & Thermal Highlights
      item(span = { GridItemSpan(gridColumns) }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Thermal & System Vitals",
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
                label = "Battery Temp",
                value = Format.celsius(snapshot.thermal.batteryCelsius),
                secondaryValue = "Status: ${Format.thermalStatus(snapshot.thermal.status)}",
                modifier = Modifier.weight(1f),
              )
              StatTile(
                label = "Headroom",
                value = snapshot.thermal.headroom?.let { String.format("%.2fx", it) } ?: "Optimal",
                secondaryValue = "Thermal Limit",
                modifier = Modifier.weight(1f),
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              StatTile(
                label = "Active Apps",
                value = "${snapshot.processes.apps.size} Recent",
                secondaryValue = if (snapshot.processes.hasUsageAccess) "Usage Access Active" else "Permission Needed",
                modifier = Modifier.weight(1f),
              )
              StatTile(
                label = "Monitor Uptime",
                value = snapshot.processes.own?.uptimeMs?.let { Format.duration(it) } ?: "Active",
                secondaryValue = "PID: ${snapshot.processes.own?.pid ?: "—"}",
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }
    }
  }
}

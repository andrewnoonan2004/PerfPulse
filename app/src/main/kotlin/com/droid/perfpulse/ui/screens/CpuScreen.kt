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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.droid.perfpulse.data.CoreInfo
import com.droid.perfpulse.ui.Format
import com.droid.perfpulse.ui.MonitorUiState
import com.droid.perfpulse.ui.components.ExpressiveGaugeCard
import com.droid.perfpulse.ui.components.ExpressiveProgressBar
import com.droid.perfpulse.ui.components.ExpressiveShapeType
import com.droid.perfpulse.ui.components.SparklineChart
import com.droid.perfpulse.ui.components.StatTile
import com.droid.perfpulse.ui.components.StatusBadge

@Composable
fun CpuScreen(
  uiState: MonitorUiState,
  modifier: Modifier = Modifier,
) {
  val snapshot = uiState.snapshot?.cpu

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
          text = "Loading CPU telemetry...",
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
      // 1. CPU Overall Gauge
      item(span = { GridItemSpan(gridColumns) }) {
        ExpressiveGaugeCard(
          title = "Central Processing Unit",
          subtitle = snapshot.socName,
          valuePercent = snapshot.usagePercent,
          detail = "${snapshot.onlineCount} of ${snapshot.coreCount} cores active • Telemetry: ${snapshot.source.name}",
          icon = painterResource(R.drawable.ic_nav_cpu),
          accentColor = MaterialTheme.colorScheme.primary,
          shapeType = ExpressiveShapeType.Cookie,
        )
      }

      // 2. Hardware Architecture & App Consumption
      item(span = { GridItemSpan(gridColumns) }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Processor Architecture",
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
                label = "SoC Model",
                value = snapshot.socName,
                modifier = Modifier.weight(1f),
              )
              StatTile(
                label = "ABIs",
                value = snapshot.abis.firstOrNull() ?: "arm64-v8a",
                secondaryValue = snapshot.abis.drop(1).joinToString(", ").takeIf { it.isNotBlank() },
                modifier = Modifier.weight(1f),
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              StatTile(
                label = "Monitor CPU Share",
                value = Format.percent(snapshot.appCpuPercent),
                secondaryValue = "Self Process Impact",
                modifier = Modifier.weight(1f),
              )
              StatTile(
                label = "Clusters",
                value = "${snapshot.clusters.size} Groups",
                secondaryValue = snapshot.clusters.joinToString(" • ") { "${it.coreIds.size}x ${Format.freqKHz(it.maxFreqKHz)}" },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }

      // 3. Live CPU History Chart
      item(span = { GridItemSpan(gridColumns) }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "CPU Utilization Trend",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
            SparklineChart(
              history = uiState.cpuHistory,
              modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
              lineColor = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }

      // 4. Per-Core Detailed Tiles
      item(span = { GridItemSpan(gridColumns) }) {
        Text(
          text = "Per-Core Frequencies & Governors",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(top = 8.dp),
        )
      }

      items(snapshot.cores, key = { it.id }) { core ->
        CoreCard(core = core)
      }
    }
  }
}

@Composable
private fun CoreCard(core: CoreInfo, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (core.online) MaterialTheme.colorScheme.surfaceContainerHigh
      else MaterialTheme.colorScheme.surfaceContainerLow,
    ),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Core #${core.id}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Spacer(modifier = Modifier.width(8.dp))
          if (core.online) {
            StatusBadge(text = "ONLINE", isAlert = false)
          } else {
            StatusBadge(text = "SLEEPING", isAlert = true)
          }
        }

        Text(
          text = if (core.online) Format.freqKHz(core.curFreqKHz) else "0 MHz",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      ExpressiveProgressBar(
        label = "Core Load",
        valueText = Format.percent(core.loadPercent),
        progress = core.loadPercent,
        color = MaterialTheme.colorScheme.primary,
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "Range: ${Format.freqKHz(core.minFreqKHz)} – ${Format.freqKHz(core.maxFreqKHz)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        core.governor?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
          )
        }
      }
    }
  }
}

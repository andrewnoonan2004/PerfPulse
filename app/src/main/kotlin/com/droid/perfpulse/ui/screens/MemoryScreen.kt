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
fun MemoryScreen(
  uiState: MonitorUiState,
  modifier: Modifier = Modifier,
) {
  val mem = uiState.snapshot?.memory

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 680.dp
    val gridColumns = if (maxWidth >= 1000.dp) 3 else if (isWideScreen) 2 else 1

    if (mem == null) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = "Reading memory subsystem...",
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
      // 1. Memory Header Card
      item(span = { GridItemSpan(gridColumns) }) {
        val usedStr = Format.bytes(mem.usedBytes)
        val totalStr = Format.bytes(mem.totalBytes)
        val availStr = Format.bytes(mem.availableBytes)
        ExpressiveGaugeCard(
          title = "Random Access Memory",
          subtitle = "$usedStr of $totalStr in use",
          valuePercent = mem.usedPercent,
          detail = "$availStr available for active apps • Low Mem Threshold: ${Format.bytes(mem.lowMemoryThresholdBytes)}",
          icon = painterResource(R.drawable.ic_nav_memory),
          accentColor = MaterialTheme.colorScheme.secondary,
          shapeType = ExpressiveShapeType.Clover,
        )
      }

      // 2. Low Memory Condition Alert (if active)
      if (mem.isLowMemory) {
        item(span = { GridItemSpan(gridColumns) }) {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "⚠️ Low Memory Condition Active",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Android LMK (Low Memory Killer) is actively trimming background processes to prevent out-of-memory errors.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
              )
            }
          }
        }
      }

      // 3. RAM Breakdown Bars
      item(span = { GridItemSpan(gridColumns) }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Detailed RAM Allocations",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(14.dp))

            ExpressiveProgressBar(
              label = "Total In-Use RAM",
              valueText = "${Format.bytes(mem.usedBytes)} (${Format.percent(mem.usedPercent)})",
              progress = mem.usedPercent,
              color = MaterialTheme.colorScheme.secondary,
            )

            mem.cachedBytes?.let { cached ->
              val cachedPercent = if (mem.totalBytes > 0) cached * 100f / mem.totalBytes else 0f
              Spacer(modifier = Modifier.height(12.dp))
              ExpressiveProgressBar(
                label = "Page Cache (Cached)",
                valueText = "${Format.bytes(cached)} (${Format.percent(cachedPercent)})",
                progress = cachedPercent,
                color = MaterialTheme.colorScheme.primary,
              )
            }

            mem.activeBytes?.let { active ->
              val activePercent = if (mem.totalBytes > 0) active * 100f / mem.totalBytes else 0f
              Spacer(modifier = Modifier.height(12.dp))
              ExpressiveProgressBar(
                label = "Active Memory",
                valueText = "${Format.bytes(active)} (${Format.percent(activePercent)})",
                progress = activePercent,
                color = MaterialTheme.colorScheme.tertiary,
              )
            }

            mem.inactiveBytes?.let { inactive ->
              val inactivePercent = if (mem.totalBytes > 0) inactive * 100f / mem.totalBytes else 0f
              Spacer(modifier = Modifier.height(12.dp))
              ExpressiveProgressBar(
                label = "Inactive Memory (Reclaimable)",
                valueText = "${Format.bytes(inactive)} (${Format.percent(inactivePercent)})",
                progress = inactivePercent,
                color = MaterialTheme.colorScheme.outline,
              )
            }
          }
        }
      }

      // 4. Swap & ZRAM Section
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
                text = "ZRAM & Swap Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              if (mem.swapTotalBytes != null && mem.swapTotalBytes > 0) {
                StatusBadge(text = "ZRAM ACTIVE", isAlert = false)
              } else {
                StatusBadge(text = "SWAP OFF", isAlert = false)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (mem.swapTotalBytes != null && mem.swapTotalBytes > 0) {
              ExpressiveProgressBar(
                label = "Swap Usage",
                valueText = "${Format.bytes(mem.swapUsedBytes)} / ${Format.bytes(mem.swapTotalBytes)} (${Format.percent(mem.swapUsedPercent)})",
                progress = mem.swapUsedPercent,
                color = MaterialTheme.colorScheme.primary,
              )

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                StatTile(
                  label = "Swap Free",
                  value = Format.bytes(mem.swapFreeBytes),
                  modifier = Modifier.weight(1f),
                )
                StatTile(
                  label = "Buffers / Shmem",
                  value = Format.bytes(mem.buffersBytes ?: mem.shmemBytes),
                  modifier = Modifier.weight(1f),
                )
              }
            } else {
              Text(
                text = "No swap or ZRAM partition detected on this kernel configuration.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      // 5. Live Memory Trend Chart
      item(span = { GridItemSpan(gridColumns) }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Memory Utilization Trend",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
            SparklineChart(
              history = uiState.memoryHistory,
              modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
              lineColor = MaterialTheme.colorScheme.secondary,
            )
          }
        }
      }
    }
  }
}

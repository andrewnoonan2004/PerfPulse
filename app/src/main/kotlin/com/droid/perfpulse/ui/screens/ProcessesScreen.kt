package com.droid.perfpulse.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droid.perfpulse.R
import com.droid.perfpulse.data.AppUsage
import com.droid.perfpulse.ui.Format
import com.droid.perfpulse.ui.MonitorUiState
import com.droid.perfpulse.ui.components.ExpressiveGaugeCard
import com.droid.perfpulse.ui.components.ExpressiveShapeType
import com.droid.perfpulse.ui.components.StatTile
import com.droid.perfpulse.ui.components.StatusBadge

@Composable
fun ProcessesScreen(
  uiState: MonitorUiState,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val processes = uiState.snapshot?.processes
  var searchQuery by remember { mutableStateOf("") }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 680.dp
    val gridColumns = if (maxWidth >= 1000.dp) 3 else if (isWideScreen) 2 else 1

    if (processes == null) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = "Analyzing process tree and application stats...",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      return@BoxWithConstraints
    }

    val own = processes.own
    val filteredApps = remember(processes.apps, searchQuery) {
      if (searchQuery.isBlank()) processes.apps
      else processes.apps.filter {
        it.label.contains(searchQuery, ignoreCase = true) ||
          it.packageName.contains(searchQuery, ignoreCase = true)
      }
    }

    LazyVerticalGrid(
      columns = GridCells.Fixed(gridColumns),
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.fillMaxSize(),
    ) {
      // 1. Process Header
      item(span = { GridItemSpan(gridColumns) }) {
        val pssStr = own?.let { Format.bytes(it.totalPssKb * 1024) } ?: "N/A"
        ExpressiveGaugeCard(
          title = "Process Telemetry",
          subtitle = own?.processName ?: "PerfPulse Process",
          valuePercent = null,
          detail = "PID: ${own?.pid ?: "—"} • Memory Footprint (PSS): $pssStr • Threads: ${own?.threads ?: "—"}",
          icon = painterResource(R.drawable.ic_nav_apps),
          accentColor = MaterialTheme.colorScheme.primary,
          shapeType = ExpressiveShapeType.Pill,
        )
      }

      // 2. Own Process Memory & Execution Vitals
      item(span = { GridItemSpan(gridColumns) }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Monitor Runtime Vitals",
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
                label = "Java / ART Heap",
                value = own?.let { Format.bytes(it.javaHeapKb * 1024) } ?: "—",
                modifier = Modifier.weight(1f),
              )
              StatTile(
                label = "Native Heap",
                value = own?.let { Format.bytes(it.nativeHeapKb * 1024) } ?: "—",
                modifier = Modifier.weight(1f),
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              StatTile(
                label = "Active Threads",
                value = "${own?.threads ?: 0}",
                secondaryValue = "/proc/self/status",
                modifier = Modifier.weight(1f),
              )
              StatTile(
                label = "Process Uptime",
                value = own?.let { Format.duration(it.uptimeMs) } ?: "—",
                secondaryValue = "CPU Time: ${own?.let { Format.duration(it.cpuTimeMs) } ?: "—"}",
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }

      // 3. System-Wide App Usage
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
                  text = "Application Usage Statistics",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Real foreground runtime in the last 24 hours",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              StatusBadge(
                text = if (processes.hasUsageAccess) "PERMISSION GRANTED" else "PERMISSION REQUIRED",
                isAlert = !processes.hasUsageAccess,
              )
            }

            if (!processes.hasUsageAccess) {
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Android requires 'Usage Access' permission to inspect foreground activity and runtime of other apps. Tap below to enable it in System Settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = {
                  val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                  }
                  context.startActivity(intent)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              ) {
                Icon(
                  painter = painterResource(R.drawable.ic_open),
                  contentDescription = null,
                  modifier = Modifier.padding(end = 8.dp),
                )
                Text("Open Usage Access Settings")
              }
            } else {
              Spacer(modifier = Modifier.height(16.dp))
              OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Filter applications...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
              )
            }
          }
        }
      }

      // 4. App List Items
      if (processes.hasUsageAccess) {
        items(filteredApps, key = { it.packageName }) { app ->
          AppUsageCard(app = app)
        }
      }
    }
  }
}

@Composable
private fun AppUsageCard(app: AppUsage, modifier: Modifier = Modifier) {
  val now = remember { System.currentTimeMillis() }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = app.label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = app.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
          )
        }
        if (app.isSystem) {
          StatusBadge(text = "SYSTEM", isAlert = false)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "Foreground: ${Format.duration(app.foregroundMs)}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Active ${Format.relativeTime(app.lastUsedMs, now)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

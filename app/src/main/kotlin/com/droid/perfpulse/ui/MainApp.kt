package com.droid.perfpulse.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.droid.perfpulse.R
import com.droid.perfpulse.ui.screens.CpuScreen
import com.droid.perfpulse.ui.screens.GpuScreen
import com.droid.perfpulse.ui.screens.MemoryScreen
import com.droid.perfpulse.ui.screens.OverviewScreen
import com.droid.perfpulse.ui.screens.ProcessesScreen

enum class AppDestination(val title: String, val iconRes: Int) {
  Overview("Overview", R.drawable.ic_nav_overview),
  Cpu("CPU", R.drawable.ic_nav_cpu),
  Gpu("GPU", R.drawable.ic_nav_gpu),
  Memory("RAM", R.drawable.ic_nav_memory),
  Processes("Processes", R.drawable.ic_nav_apps),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainApp(
  viewModel: MonitorViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Overview) }

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      AppDestination.entries.forEach { destination ->
        item(
          selected = currentDestination == destination,
          onClick = { currentDestination = destination },
          icon = {
            Icon(
              painter = painterResource(destination.iconRes),
              contentDescription = destination.title,
            )
          },
          label = { Text(destination.title) },
        )
      }
    },
    modifier = modifier.fillMaxSize(),
  ) {
    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(MaterialShapes.Cookie12Sided.toShape())
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  painter = painterResource(R.drawable.ic_speed),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(20.dp),
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "PerfPulse",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
              )
            }
          },
          actions = {
            // Sampling interval quick chips (500ms, 1s, 2s)
            Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(end = 8.dp),
            ) {
              MonitorViewModel.INTERVAL_OPTIONS.forEach { ms ->
                val label = if (ms < 1000) "${ms}ms" else "${ms / 1000}s"
                FilterChip(
                  selected = uiState.intervalMs == ms,
                  onClick = { viewModel.setInterval(ms) },
                  label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                  shape = RoundedCornerShape(8.dp),
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  ),
                )
              }

              // Pause / Resume Toggle
              FilledTonalIconButton(
                onClick = { viewModel.togglePause() },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                  containerColor = if (uiState.paused) MaterialTheme.colorScheme.errorContainer
                  else MaterialTheme.colorScheme.secondaryContainer,
                  contentColor = if (uiState.paused) MaterialTheme.colorScheme.onErrorContainer
                  else MaterialTheme.colorScheme.onSecondaryContainer,
                ),
              ) {
                Icon(
                  painter = painterResource(if (uiState.paused) R.drawable.ic_play else R.drawable.ic_pause),
                  contentDescription = if (uiState.paused) "Resume Monitoring" else "Pause Monitoring",
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
          ),
        )
      },
      containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
      AnimatedContent(
        targetState = currentDestination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition",
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
      ) { dest ->
        when (dest) {
          AppDestination.Overview -> OverviewScreen(
            uiState = uiState,
            onNavigateToCpu = { currentDestination = AppDestination.Cpu },
            onNavigateToGpu = { currentDestination = AppDestination.Gpu },
            onNavigateToMemory = { currentDestination = AppDestination.Memory },
          )
          AppDestination.Cpu -> CpuScreen(uiState = uiState)
          AppDestination.Gpu -> GpuScreen(uiState = uiState)
          AppDestination.Memory -> MemoryScreen(uiState = uiState)
          AppDestination.Processes -> ProcessesScreen(uiState = uiState)
        }
      }
    }
  }
}

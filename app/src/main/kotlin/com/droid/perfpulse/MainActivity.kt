package com.droid.perfpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.droid.perfpulse.ui.MainApp
import com.droid.perfpulse.ui.MonitorViewModel
import com.droid.perfpulse.ui.theme.PerfPulseTheme

class MainActivity : ComponentActivity() {

  private val container by lazy { AppContainer(applicationContext) }

  private val viewModel: MonitorViewModel by viewModels {
    MonitorViewModel.Factory(container.repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      // Dynamic wallpaper-adapted color is active by default (Android 12+)
      PerfPulseTheme(dynamicColor = true) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background,
        ) {
          MainApp(viewModel = viewModel)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    // Refresh permissions and process states when returning from System Settings
    viewModel.onReturnedFromSettings()
  }
}

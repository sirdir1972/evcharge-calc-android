package com.evchargecalculator.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evchargecalculator.android.ui.theme.EVChargeCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EVChargeCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EVChargeCalculatorApp()
                }
            }
        }
    }
}

@Composable
fun EVChargeCalculatorApp() {
    var currentScreen by remember { mutableStateOf("main") }
    val settingsManager: SettingsManager = viewModel()
    
    when (currentScreen) {
        "main" -> {
            MainScreen(
                onSettingsClick = { currentScreen = "settings" },
                settingsManager = settingsManager
            )
        }
        "settings" -> {
            SettingsScreen(
                settingsManager = settingsManager,
                onDismiss = { currentScreen = "main" }
            )
        }
    }
}

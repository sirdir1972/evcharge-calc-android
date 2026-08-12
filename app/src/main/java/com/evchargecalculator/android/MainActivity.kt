package com.evchargecalculator.android

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evchargecalculator.android.ui.theme.EVChargeCalculatorTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsManager: SettingsManager = viewModel()
            val selectedLanguage = settingsManager.appLanguage.value
            val context = LocalContext.current

            val targetLocale = remember(selectedLanguage) {
                if (selectedLanguage.isEmpty()) {
                    Locale.getDefault()
                } else {
                    Locale.forLanguageTag(selectedLanguage)
                }
            }

            val localizedContext = remember(targetLocale, context) {
                val config = Configuration(context.resources.configuration)
                config.setLocale(targetLocale)
                config.setLayoutDirection(targetLocale)
                context.createConfigurationContext(config)
            }

            val localizedConfig = remember(targetLocale) {
                Configuration(context.resources.configuration).apply {
                    setLocale(targetLocale)
                    setLayoutDirection(targetLocale)
                }
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfig
            ) {
                EVChargeCalculatorTheme {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        EVChargeCalculatorApp(settingsManager)
                    }
                }
            }
        }
    }
}

@Composable
fun EVChargeCalculatorApp(settingsManager: SettingsManager = viewModel()) {
    var currentScreen by remember { mutableStateOf("main") }
    
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

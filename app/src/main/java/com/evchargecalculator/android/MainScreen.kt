package com.evchargecalculator.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import kotlin.math.ceil

@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    settingsManager: SettingsManager = viewModel()
) {
    // Use persistent SOC values from SettingsManager
    val currentSOC = settingsManager.currentSOC.value
    val targetSOC = settingsManager.targetSOC.value
    var currentSOCText by remember { mutableStateOf(currentSOC.roundToInt().toString()) }
    var targetSOCText by remember { mutableStateOf(targetSOC.roundToInt().toString()) }
    
    // Validation states
    var currentSOCError by remember { mutableStateOf<String?>(null) }
    var targetSOCError by remember { mutableStateOf<String?>(null) }
    val isConfigurationValid = settingsManager.isSOCConfigurationValid()
    val validationMessage = settingsManager.getSOCValidationMessage()

    // go-eCharger states
    val goEChargerApi = remember { GoEChargerApi() }
    val scope = rememberCoroutineScope()
    var pushingLimit by remember { mutableStateOf(false) }
    var pushResult by remember { mutableStateOf<String?>(null) }

    // Clear push result after 5 seconds
    LaunchedEffect(pushResult) {
        if (pushResult != null) {
            kotlinx.coroutines.delay(5000)
            pushResult = null
        }
    }

    // Update text fields when sliders change
    LaunchedEffect(currentSOC) {
        currentSOCText = currentSOC.roundToInt().toString()
    }
    LaunchedEffect(targetSOC) {
        targetSOCText = targetSOC.roundToInt().toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Main calculation card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Current SOC Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.current_charge),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedTextField(
                            value = currentSOCText,
                            onValueChange = { newValue ->
                                currentSOCText = newValue
                                currentSOCError = null // Clear previous error
                                
                                val floatValue = newValue.toFloatOrNull()
                                when {
                                    newValue.isBlank() -> {
                                        currentSOCError = "Required"
                                    }
                                    floatValue == null -> {
                                        currentSOCError = "Invalid number"
                                    }
                                    floatValue < 0f -> {
                                        currentSOCError = "Cannot be negative"
                                    }
                                    floatValue > 100f -> {
                                        currentSOCError = "Cannot exceed 100%"
                                    }
                                    else -> {
                                        settingsManager.setCurrentSOC(floatValue)
                                        currentSOCError = null
                                    }
                                }
                            },
                            modifier = Modifier.width(100.dp),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                color = when {
                                    currentSOCError != null -> Color.Red
                                    currentSOC < 20 -> Color.Red
                                    currentSOC < 50 -> Color(0xFFFF8C00)
                                    else -> Color.Green
                                }
                            ),
                            suffix = { Text(" %", style = MaterialTheme.typography.bodySmall) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = currentSOCError != null,
                            supportingText = currentSOCError?.let { error ->
                                { Text(text = error, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                            }
                        )
                    }

                    Slider(
                        value = currentSOC,
                        onValueChange = { 
                            settingsManager.setCurrentSOC(it)
                            currentSOCError = null // Clear any text field errors when using slider
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = if (currentSOCError != null) Color.Red else Color(0xFFFF8C00),
                            activeTrackColor = if (currentSOCError != null) Color.Red else Color(0xFFFF8C00)
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Target SOC Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.target_charge),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedTextField(
                            value = targetSOCText,
                            onValueChange = { newValue ->
                                targetSOCText = newValue
                                targetSOCError = null // Clear previous error
                                
                                val floatValue = newValue.toFloatOrNull()
                                when {
                                    newValue.isBlank() -> {
                                        targetSOCError = "Required"
                                    }
                                    floatValue == null -> {
                                        targetSOCError = "Invalid number"
                                    }
                                    floatValue < 0f -> {
                                        targetSOCError = "Cannot be negative"
                                    }
                                    floatValue > 100f -> {
                                        targetSOCError = "Cannot exceed 100%"
                                    }
                                    else -> {
                                        settingsManager.setTargetSOC(floatValue)
                                        targetSOCError = null
                                    }
                                }
                            },
                            modifier = Modifier.width(100.dp),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                color = when {
                                    targetSOCError != null -> Color.Red
                                    targetSOC < 20 -> Color.Red
                                    targetSOC < 50 -> Color(0xFFFF8C00)
                                    else -> Color.Green
                                }
                            ),
                            suffix = { Text(" %", style = MaterialTheme.typography.bodySmall) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = targetSOCError != null,
                            supportingText = targetSOCError?.let { error ->
                                { Text(text = error, color = Color.Red, style = MaterialTheme.typography.bodySmall) }
                            }
                        )
                    }

                    Slider(
                        value = targetSOC,
                        onValueChange = { 
                            settingsManager.setTargetSOC(it)
                            targetSOCError = null // Clear any text field errors when using slider
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = if (targetSOCError != null) Color.Red else Color.Green,
                            activeTrackColor = if (targetSOCError != null) Color.Red else Color.Green
                        )
                    )
                }
                
                // Add validation feedback if there's an issue
                if (validationMessage != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3CD) // Light yellow warning background
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Auto-adjusted: $validationMessage",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF856404), // Dark yellow text
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Results Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.charge_required),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )

                if (targetSOC < currentSOC) {
                    Text(
                        text = "Target charge is lower than current charge. Please adjust values.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (targetSOC == currentSOC) {
                    Text(
                        text = "Target charge equals current charge - no charging needed.",
                        color = Color(0xFFFF8C00),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    val socIncrease = targetSOC - currentSOC
                    val energyNeeded = settingsManager.calculateRequiredEnergy(
                        from = currentSOC.toDouble(),
                        to = targetSOC.toDouble()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.energy_needed),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "%.2f kWh".format(energyNeeded),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.soc_increase),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${socIncrease.roundToInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (socIncrease >= 0) Color.Green else Color.Red
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.effective_capacity),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "%.1f kWh".format(settingsManager.effectiveBatteryCapacity),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // go-eCharger Push Limit Section (only if enabled and connected)
                    if (settingsManager.goEChargerEnabled.value &&
                        settingsManager.goEChargerConnectionStatus.value.startsWith("✓") &&
                        targetSOC > currentSOC) {

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "go-eCharger Control",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            val energyNeeded = settingsManager.calculateRequiredEnergy(
                                from = currentSOC.toDouble(),
                                to = targetSOC.toDouble()
                            )

                            // Convert kWh to Wh and round to 100 Wh (0.1 kWh) increments
                            val energyNeededRounded = kotlin.math.ceil(energyNeeded * 10.0) / 10.0
                            val energyNeededWh = energyNeededRounded * 1000

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Energy limit: %.1f kWh".format(energyNeededRounded),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "(${energyNeededWh.toInt()} Wh)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        pushingLimit = true
                                        pushResult = null

                                        // Set energy limit only (don't change current setting)
                                        val energyResult = goEChargerApi.setEnergyLimit(
                                            settingsManager.goEChargerIpAddress.value,
                                            energyNeededWh
                                        )

                                        pushingLimit = false

                                        if (energyResult.success) {
                                            pushResult = "✓ Energy limit set successfully"
                                        } else {
                                            pushResult = "✗ ${energyResult.error ?: "Failed"}"
                                        }
                                    }
                                },
                                enabled = !pushingLimit,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (pushingLimit) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pushing...")
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Set Energy Limit", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Push result status
                            if (pushResult != null) {
                                Text(
                                    text = pushResult!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        pushResult!!.startsWith("✓") -> Color(0xFF4CAF50)
                                        pushResult!!.startsWith("✗") -> Color(0xFFF44336)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.battery_capacity),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "%.1f kWh".format(settingsManager.batteryCapacity.value),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.state_of_health),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "%.1f%%".format(settingsManager.stateOfHealth.value),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                settingsManager.stateOfHealth.value > 90.0 -> Color.Green
                                settingsManager.stateOfHealth.value > 80.0 -> Color(0xFFFF8C00)
                                else -> Color.Red
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.charge_losses),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "%.1f%%".format(settingsManager.chargeLosses.value),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Presets
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_presets),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        settingsManager.setTargetSOC(80f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Daily 80%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedButton(
                    onClick = {
                        settingsManager.setTargetSOC(90f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Top Up 90%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedButton(
                    onClick = {
                        settingsManager.setTargetSOC(100f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Road Trip 100%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
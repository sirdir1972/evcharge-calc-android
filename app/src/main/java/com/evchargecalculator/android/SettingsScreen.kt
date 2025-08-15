package com.evchargecalculator.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onDismiss: () -> Unit
) {
    // State for text fields
    var batteryCapacityText by remember { mutableStateOf("%.1f".format(settingsManager.batteryCapacity.value)) }
    var stateOfHealthText by remember { mutableStateOf("%.1f".format(settingsManager.stateOfHealth.value)) }
    var chargeLossesText by remember { mutableStateOf("%.1f".format(settingsManager.chargeLosses.value)) }
    
    // go-eCharger states
    var goEChargerIpText by remember { mutableStateOf(settingsManager.goEChargerIpAddress.value) }
    val goEChargerApi = remember { GoEChargerApi() }
    val scope = rememberCoroutineScope()
    
    // Update text fields when slider values change
    LaunchedEffect(settingsManager.batteryCapacity.value) {
        batteryCapacityText = "%.1f".format(settingsManager.batteryCapacity.value)
    }
    LaunchedEffect(settingsManager.stateOfHealth.value) {
        stateOfHealthText = "%.1f".format(settingsManager.stateOfHealth.value)
    }
    LaunchedEffect(settingsManager.chargeLosses.value) {
        chargeLossesText = "%.1f".format(settingsManager.chargeLosses.value)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    stringResource(R.string.ev_settings),
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                TextButton(onClick = onDismiss) {
                    Text(
                        stringResource(R.string.done),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Battery Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.battery_configuration),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Battery Capacity
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.battery_capacity),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            OutlinedTextField(
                                value = batteryCapacityText,
                                onValueChange = { newValue ->
                                    batteryCapacityText = newValue
                                    newValue.toDoubleOrNull()?.let { value ->
                                        if (value in 10.0..200.0) {
                                            settingsManager.setBatteryCapacity(value)
                                        }
                                    }
                                },
                                modifier = Modifier.width(120.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.End
                                ),
                                suffix = { Text(" kWh", style = MaterialTheme.typography.bodySmall) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        }
                        
                        Slider(
                            value = settingsManager.batteryCapacity.value.toFloat(),
                            onValueChange = { settingsManager.setBatteryCapacity(it.toDouble()) },
                            valueRange = 10f..200f,
                            steps = 379, // (200-10)*2 for 0.5 step increments
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    
                    // State of Health
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stringResource(R.string.state_of_health)} (SOH)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            OutlinedTextField(
                                value = stateOfHealthText,
                                onValueChange = { newValue ->
                                    stateOfHealthText = newValue
                                    newValue.toDoubleOrNull()?.let { value ->
                                        if (value in 50.0..100.0) {
                                            settingsManager.setStateOfHealth(value)
                                        }
                                    }
                                },
                                modifier = Modifier.width(120.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.End
                                ),
                                suffix = { Text(" %", style = MaterialTheme.typography.bodySmall) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        }
                        
                        Slider(
                            value = settingsManager.stateOfHealth.value.toFloat(),
                            onValueChange = { settingsManager.setStateOfHealth(it.toDouble()) },
                            valueRange = 50f..100f,
                            steps = 99, // (100-50)*2 for 0.5 step increments
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF4CAF50),
                                activeTrackColor = Color(0xFF4CAF50)
                            )
                        )
                    }
                    
                    // Charge Losses
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.charge_losses),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            OutlinedTextField(
                                value = chargeLossesText,
                                onValueChange = { newValue ->
                                    chargeLossesText = newValue
                                    newValue.toDoubleOrNull()?.let { value ->
                                        if (value in 5.0..25.0) {
                                            settingsManager.setChargeLosses(value)
                                        }
                                    }
                                },
                                modifier = Modifier.width(120.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.End
                                ),
                                suffix = { Text(" %", style = MaterialTheme.typography.bodySmall) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        }
                        
                        Slider(
                            value = settingsManager.chargeLosses.value.toFloat(),
                            onValueChange = { settingsManager.setChargeLosses(it.toDouble()) },
                            valueRange = 5f..25f,
                            steps = 39, // (25-5)*2 for 0.5 step increments
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF9800),
                                activeTrackColor = Color(0xFFFF9800)
                            )
                        )
                    }
                }
            }
            
            // Calculated Values Card
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
                        text = stringResource(R.string.calculated_values),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
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
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.usable_capacity),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "%.1f kWh".format(settingsManager.effectiveBatteryCapacity * 0.8),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            
            // go-eCharger Integration Card
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
                        text = "go-eCharger Integration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Enable/Disable Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Enable go-eCharger",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Push charge limits to charger",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = settingsManager.goEChargerEnabled.value,
                            onCheckedChange = { enabled ->
                                settingsManager.setGoEChargerEnabled(enabled)
                                if (!enabled) {
                                    settingsManager.setGoEChargerConnectionStatus("Not tested")
                                }
                            }
                        )
                    }
                    
                    // IP Address Input (only shown when enabled)
                    if (settingsManager.goEChargerEnabled.value) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Charger IP Address",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = goEChargerIpText,
                                    onValueChange = { 
                                        goEChargerIpText = it
                                        settingsManager.setGoEChargerIpAddress(it)
                                        settingsManager.setGoEChargerConnectionStatus("Not tested")
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("192.168.1.100") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                
                                Button(
                                    onClick = {
                                        scope.launch {
                                            settingsManager.setGoEChargerConnectionStatus("Testing...")
                                            val result = goEChargerApi.testConnection(goEChargerIpText)
                                            if (result.success) {
                                                settingsManager.setGoEChargerConnectionStatus(
                                                    "✓ ${result.data ?: "Connected"}"
                                                )
                                            } else {
                                                settingsManager.setGoEChargerConnectionStatus(
                                                    "✗ ${result.error ?: "Failed"}"
                                                )
                                            }
                                        }
                                    },
                                    enabled = goEChargerIpText.isNotBlank()
                                ) {
                                    Text("Test")
                                }
                            }
                            
                            // Connection Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Connection Status:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = settingsManager.goEChargerConnectionStatus.value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        settingsManager.goEChargerConnectionStatus.value.startsWith("✓") -> 
                                            Color(0xFF4CAF50)
                                        settingsManager.goEChargerConnectionStatus.value.startsWith("✗") -> 
                                            Color(0xFFF44336)
                                        settingsManager.goEChargerConnectionStatus.value == "Testing..." -> 
                                            MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_description),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String
)

val supportedLanguages = listOf(
    LanguageOption("", "System Default", "Default"),
    LanguageOption("en", "English", "English"),
    LanguageOption("de", "Deutsch", "German"),
    LanguageOption("fr", "Français", "French"),
    LanguageOption("nl", "Nederlands", "Dutch"),
    LanguageOption("es", "Español", "Spanish"),
    LanguageOption("it", "Italiano", "Italian"),
    LanguageOption("nb", "Norsk (Bokmål)", "Norwegian"),
    LanguageOption("sv", "Svenska", "Swedish"),
    LanguageOption("pt", "Português", "Portuguese")
)

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
                        contentDescription = stringResource(R.string.back)
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
                                    newValue.replace(',', '.').toDoubleOrNull()?.let { value ->
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
                                    newValue.replace(',', '.').toDoubleOrNull()?.let { value ->
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
                                    newValue.replace(',', '.').toDoubleOrNull()?.let { value ->
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
                        text = stringResource(R.string.goe_integration),
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
                                text = stringResource(R.string.enable_goe),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.enable_goe_desc),
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
                                text = stringResource(R.string.charger_ip_address),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = goEChargerIpText,
                                    onValueChange = { newIp ->
                                        goEChargerIpText = newIp
                                        // Only reset connection status if IP actually changed
                                        if (newIp != settingsManager.goEChargerIpAddress.value) {
                                            settingsManager.setGoEChargerConnectionStatus("Not tested")
                                        }
                                        settingsManager.setGoEChargerIpAddress(newIp)
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("192.168.1.100") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
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
                                    Text(stringResource(R.string.test))
                                }
                            }
                            
                            // Connection Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.connection_status),
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
            
            // Language Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    var languageExpanded by remember { mutableStateOf(false) }
                    val currentCode = settingsManager.appLanguage.value
                    val currentOption = supportedLanguages.find { it.code == currentCode } ?: supportedLanguages.first()

                    ExposedDropdownMenuBox(
                        expanded = languageExpanded,
                        onExpandedChange = { languageExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (currentOption.code.isEmpty()) stringResource(R.string.system_default) else currentOption.nativeName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                .fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )

                        ExposedDropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false }
                        ) {
                            supportedLanguages.forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (lang.code.isEmpty()) stringResource(R.string.system_default) else lang.nativeName,
                                                fontWeight = if (lang.code == currentCode) FontWeight.Bold else FontWeight.Normal,
                                                color = if (lang.code == currentCode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (lang.code.isNotEmpty()) {
                                                Text(
                                                    text = lang.englishName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        settingsManager.setAppLanguage(lang.code)
                                        languageExpanded = false
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

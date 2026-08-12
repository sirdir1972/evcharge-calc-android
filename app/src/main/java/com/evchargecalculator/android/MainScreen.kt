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
    val validationError = settingsManager.getSOCValidationError()
    val validationMessage = when (validationError) {
        SettingsManager.SOCValidationError.CURRENT_EXCEEDS_TARGET -> stringResource(R.string.error_current_exceeds_target)
        SettingsManager.SOCValidationError.TARGET_TOO_CLOSE -> stringResource(R.string.error_target_higher_than_current)
        null -> null
    }

    // Localized error strings
    val errorRequired = stringResource(R.string.error_required)
    val errorInvalidNumber = stringResource(R.string.error_invalid_number)
    val errorCannotBeNegative = stringResource(R.string.error_cannot_be_negative)
    val errorCannotExceed100 = stringResource(R.string.error_cannot_exceed_100)

    // go-eCharger states
    val goEChargerApi = remember { GoEChargerApi() }
    val scope = rememberCoroutineScope()
    var pushingLimit by remember { mutableStateOf(false) }
    var pushResult by remember { mutableStateOf<String?>(null) }
    val pushSuccessMsg = stringResource(R.string.goe_limit_success)

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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "%.1f kWh (${"%.0f".format(settingsManager.stateOfHealth.value)}%% SOH)".format(settingsManager.effectiveBatteryCapacity),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Main calculation card (Inputs)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Current SOC Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.current_charge),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        OutlinedTextField(
                            value = currentSOCText,
                            onValueChange = { newValue ->
                                currentSOCText = newValue
                                currentSOCError = null
                                
                                val floatValue = newValue.replace(',', '.').toFloatOrNull()
                                when {
                                    newValue.isBlank() -> {
                                        currentSOCError = errorRequired
                                    }
                                    floatValue == null -> {
                                        currentSOCError = errorInvalidNumber
                                    }
                                    floatValue < 0f -> {
                                        currentSOCError = errorCannotBeNegative
                                    }
                                    floatValue > 100f -> {
                                        currentSOCError = errorCannotExceed100
                                    }
                                    else -> {
                                        settingsManager.setCurrentSOC(floatValue)
                                        currentSOCError = null
                                    }
                                }
                            },
                            modifier = Modifier.width(96.dp),
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                color = when {
                                    currentSOCError != null -> Color.Red
                                    currentSOC < 20 -> Color.Red
                                    currentSOC < 50 -> Color(0xFFFF8C00)
                                    else -> Color(0xFF2E7D32)
                                }
                            ),
                            suffix = { Text("%", style = MaterialTheme.typography.bodySmall) },
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
                            currentSOCError = null
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = if (currentSOCError != null) Color.Red else Color(0xFFFF8C00),
                            activeTrackColor = if (currentSOCError != null) Color.Red else Color(0xFFFF8C00)
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Target SOC Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.target_charge),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        OutlinedTextField(
                            value = targetSOCText,
                            onValueChange = { newValue ->
                                targetSOCText = newValue
                                targetSOCError = null
                                
                                val floatValue = newValue.replace(',', '.').toFloatOrNull()
                                when {
                                    newValue.isBlank() -> {
                                        targetSOCError = errorRequired
                                    }
                                    floatValue == null -> {
                                        targetSOCError = errorInvalidNumber
                                    }
                                    floatValue < 0f -> {
                                        targetSOCError = errorCannotBeNegative
                                    }
                                    floatValue > 100f -> {
                                        targetSOCError = errorCannotExceed100
                                    }
                                    else -> {
                                        settingsManager.setTargetSOC(floatValue)
                                        targetSOCError = null
                                    }
                                }
                            },
                            modifier = Modifier.width(96.dp),
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                color = when {
                                    targetSOCError != null -> Color.Red
                                    targetSOC < 20 -> Color.Red
                                    targetSOC < 50 -> Color(0xFFFF8C00)
                                    else -> Color(0xFF2E7D32)
                                }
                            ),
                            suffix = { Text("%", style = MaterialTheme.typography.bodySmall) },
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
                            targetSOCError = null
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = if (targetSOCError != null) Color.Red else Color(0xFF2E7D32),
                            activeTrackColor = if (targetSOCError != null) Color.Red else Color(0xFF2E7D32)
                        )
                    )

                    // Quick Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { settingsManager.setTargetSOC(80f) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = if (targetSOC == 80f) ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                text = stringResource(R.string.preset_daily_80),
                                fontSize = 12.sp,
                                fontWeight = if (targetSOC == 80f) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { settingsManager.setTargetSOC(90f) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = if (targetSOC == 90f) ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                text = stringResource(R.string.preset_top_up_90),
                                fontSize = 12.sp,
                                fontWeight = if (targetSOC == 90f) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { settingsManager.setTargetSOC(100f) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = if (targetSOC == 100f) ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(
                                text = stringResource(R.string.preset_road_trip_100),
                                fontSize = 12.sp,
                                fontWeight = if (targetSOC == 100f) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                // Add validation feedback if there's an issue
                if (validationMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3CD)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = stringResource(R.string.auto_adjusted_format, validationMessage),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF856404),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Results Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.charge_required),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (targetSOC < currentSOC) {
                    Text(
                        text = stringResource(R.string.target_lower_warning),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (targetSOC == currentSOC) {
                    Text(
                        text = stringResource(R.string.target_equal_info),
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "%.2f kWh".format(energyNeeded),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${currentSOC.roundToInt()}% ➔ ${targetSOC.roundToInt()}% (${stringResource(R.string.incl_losses_format, settingsManager.chargeLosses.value.roundToInt())})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "+${socIncrease.roundToInt()}%",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    // go-eCharger Push Limit Section (only if enabled and connected)
                    if (settingsManager.goEChargerEnabled.value &&
                        settingsManager.goEChargerConnectionStatus.value.startsWith("✓") &&
                        targetSOC > currentSOC) {

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        val energyNeededRounded = kotlin.math.ceil(energyNeeded * 10.0) / 10.0
                        val energyNeededWh = energyNeededRounded * 1000

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.energy_limit_format, energyNeededRounded),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.energy_limit_wh_format, energyNeededWh.toInt()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    pushingLimit = true
                                    pushResult = null

                                    val energyResult = goEChargerApi.setEnergyLimit(
                                        settingsManager.goEChargerIpAddress.value,
                                        energyNeededWh
                                    )

                                    pushingLimit = false

                                    if (energyResult.success) {
                                        pushResult = pushSuccessMsg
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
                                Text(stringResource(R.string.btn_pushing))
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.btn_set_energy_limit), fontWeight = FontWeight.SemiBold)
                            }
                        }

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
}
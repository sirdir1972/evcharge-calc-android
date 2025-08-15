package com.evchargecalculator.android

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

data class BatterySettings(
    val batteryCapacity: Float,
    val stateOfHealth: Float,
    val chargeLosses: Float
)

data class GoEChargerSettings(
    val enabled: Boolean,
    val ipAddress: String,
    val connectionStatus: String
)

class SettingsManager(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("ev_settings", android.content.Context.MODE_PRIVATE)
    
    private val _settings = MutableStateFlow(
        BatterySettings(
            batteryCapacity = preferences.getFloat("batteryCapacity", 75.0f),
            stateOfHealth = preferences.getFloat("stateOfHealth", 0.95f),
            chargeLosses = preferences.getFloat("chargeLosses", 0.10f)
        )
    )
    val settings: StateFlow<BatterySettings> = _settings.asStateFlow()
    
    // go-eCharger settings
    private val _goEChargerSettings = MutableStateFlow(
        GoEChargerSettings(
            enabled = preferences.getBoolean("goEChargerEnabled", false),
            ipAddress = preferences.getString("goEChargerIpAddress", "") ?: "",
            connectionStatus = "Not tested"
        )
    )
    val goEChargerSettings: StateFlow<GoEChargerSettings> = _goEChargerSettings.asStateFlow()
    
    // Legacy compatibility properties for SettingsScreen
    private val _batteryCapacity = mutableStateOf(
        preferences.getFloat("batteryCapacity", 75.0f).toDouble()
    )
    val batteryCapacity: State<Double> = _batteryCapacity
    
    private val _stateOfHealth = mutableStateOf(
        preferences.getFloat("stateOfHealth", 0.95f).toDouble() * 100.0
    )
    val stateOfHealth: State<Double> = _stateOfHealth
    
    private val _chargeLosses = mutableStateOf(
        preferences.getFloat("chargeLosses", 0.10f).toDouble() * 100.0
    )
    val chargeLosses: State<Double> = _chargeLosses
    
    // Legacy compatibility for go-eCharger settings in SettingsScreen
    private val _goEChargerEnabled = mutableStateOf(
        preferences.getBoolean("goEChargerEnabled", false)
    )
    val goEChargerEnabled: State<Boolean> = _goEChargerEnabled
    
    private val _goEChargerIpAddress = mutableStateOf(
        preferences.getString("goEChargerIpAddress", "") ?: ""
    )
    val goEChargerIpAddress: State<String> = _goEChargerIpAddress
    
    private val _goEChargerConnectionStatus = mutableStateOf("Not tested")
    val goEChargerConnectionStatus: State<String> = _goEChargerConnectionStatus
    
    // SOC values persistence
    private val _currentSOC = mutableStateOf(
        preferences.getFloat("currentSOC", 20.0f)
    )
    val currentSOC: State<Float> = _currentSOC
    
    private val _targetSOC = mutableStateOf(
        preferences.getFloat("targetSOC", 80.0f)
    )
    val targetSOC: State<Float> = _targetSOC
    
    fun setBatteryCapacity(capacity: Double) {
        _batteryCapacity.value = capacity
        preferences.edit().putFloat("batteryCapacity", capacity.toFloat()).apply()
        updateStateFlow()
    }
    
    fun setStateOfHealth(soh: Double) {
        _stateOfHealth.value = soh
        // Convert percentage to decimal for StateFlow
        preferences.edit().putFloat("stateOfHealth", (soh / 100.0).toFloat()).apply()
        updateStateFlow()
    }
    
    fun setChargeLosses(losses: Double) {
        _chargeLosses.value = losses
        // Convert percentage to decimal for StateFlow
        preferences.edit().putFloat("chargeLosses", (losses / 100.0).toFloat()).apply()
        updateStateFlow()
    }
    
    private fun updateStateFlow() {
        _settings.value = BatterySettings(
            batteryCapacity = _batteryCapacity.value.toFloat(),
            stateOfHealth = (_stateOfHealth.value / 100.0).toFloat(),
            chargeLosses = (_chargeLosses.value / 100.0).toFloat()
        )
    }
    
    // Calculate effective battery capacity considering SOH
    val effectiveBatteryCapacity: Double
        get() = batteryCapacity.value * (stateOfHealth.value / 100.0)
    
    // Calculate required energy including losses
    fun calculateRequiredEnergy(from: Double, to: Double): Double {
        val socDifference = to - from
        val baseEnergyNeeded = effectiveBatteryCapacity * (socDifference / 100.0)
        val energyWithLosses = baseEnergyNeeded * (1.0 + chargeLosses.value / 100.0)
        return max(0.0, energyWithLosses)
    }
    
    // go-eCharger settings management
    fun setGoEChargerEnabled(enabled: Boolean) {
        _goEChargerEnabled.value = enabled
        preferences.edit().putBoolean("goEChargerEnabled", enabled).apply()
        updateGoEChargerStateFlow()
    }
    
    fun setGoEChargerIpAddress(ipAddress: String) {
        _goEChargerIpAddress.value = ipAddress
        preferences.edit().putString("goEChargerIpAddress", ipAddress).apply()
        updateGoEChargerStateFlow()
    }
    
    fun setGoEChargerConnectionStatus(status: String) {
        _goEChargerConnectionStatus.value = status
        updateGoEChargerStateFlow()
    }
    
    private fun updateGoEChargerStateFlow() {
        _goEChargerSettings.value = GoEChargerSettings(
            enabled = _goEChargerEnabled.value,
            ipAddress = _goEChargerIpAddress.value,
            connectionStatus = _goEChargerConnectionStatus.value
        )
    }
    
    // SOC values management
    fun setCurrentSOC(soc: Float) {
        _currentSOC.value = soc
        preferences.edit().putFloat("currentSOC", soc).apply()
    }
    
    fun setTargetSOC(soc: Float) {
        _targetSOC.value = soc
        preferences.edit().putFloat("targetSOC", soc).apply()
    }
}

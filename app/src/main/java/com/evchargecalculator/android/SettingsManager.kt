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
    
    // Legacy compatibility properties for SettingsScreen
    private val _batteryCapacity = mutableStateOf(
        preferences.getFloat("batteryCapacity", 75.0f).toDouble()
    )
    val batteryCapacity: State<Double> = _batteryCapacity
    
    private val _stateOfHealth = mutableStateOf(
        preferences.getFloat("stateOfHealth", 95.0f).toDouble()
    )
    val stateOfHealth: State<Double> = _stateOfHealth
    
    private val _chargeLosses = mutableStateOf(
        preferences.getFloat("chargeLosses", 10.0f).toDouble()
    )
    val chargeLosses: State<Double> = _chargeLosses
    
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
}

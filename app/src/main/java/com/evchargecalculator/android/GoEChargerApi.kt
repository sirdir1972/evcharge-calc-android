package com.evchargecalculator.android

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.SocketTimeoutException
import java.net.ConnectException

data class GoEChargerStatus(
    val carState: Int?,
    val currentAmpere: Int?,
    val allowCharging: Boolean?,
    val energyLimit: Double?
)

data class GoEChargerResult<T>(
    val success: Boolean,
    val data: T?,
    val error: String?
)

class GoEChargerApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val timeoutMs = 5000 // 5 second timeout
    
    suspend fun testConnection(ipAddress: String): GoEChargerResult<String> = withContext(Dispatchers.IO) {
        if (ipAddress.isBlank()) {
            return@withContext GoEChargerResult(false, null, "IP address is empty")
        }
        
        try {
            val url = URL("http://$ipAddress/api/status?filter=car,typ")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = json.parseToJsonElement(response) as JsonObject
                
                // Check if we got the expected charger response
                val deviceType = jsonResponse["typ"]?.jsonPrimitive?.content
                if (deviceType != null) {
                    GoEChargerResult(true, "Connected to $deviceType", null)
                } else {
                    GoEChargerResult(true, "Connected successfully", null)
                }
            } else {
                GoEChargerResult(false, null, "HTTP error: $responseCode")
            }
        } catch (e: ConnectException) {
            GoEChargerResult(false, null, "Connection refused - check IP address and network")
        } catch (e: SocketTimeoutException) {
            GoEChargerResult(false, null, "Connection timeout - charger not responding")
        } catch (e: IOException) {
            GoEChargerResult(false, null, "Network error: ${e.message}")
        } catch (e: Exception) {
            GoEChargerResult(false, null, "Unexpected error: ${e.message}")
        }
    }
    
    suspend fun getStatus(ipAddress: String): GoEChargerResult<GoEChargerStatus> = withContext(Dispatchers.IO) {
        if (ipAddress.isBlank()) {
            return@withContext GoEChargerResult(false, null, "IP address is empty")
        }
        
        try {
            val url = URL("http://$ipAddress/api/status?filter=car,amp,alw,dwo,acu")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = json.parseToJsonElement(response) as JsonObject
                
                val status = GoEChargerStatus(
                    carState = jsonResponse["car"]?.jsonPrimitive?.content?.toIntOrNull(),
                    currentAmpere = jsonResponse["acu"]?.jsonPrimitive?.content?.toIntOrNull(),
                    allowCharging = jsonResponse["alw"]?.jsonPrimitive?.content?.toBooleanStrictOrNull(),
                    energyLimit = jsonResponse["dwo"]?.jsonPrimitive?.content?.toDoubleOrNull()
                )
                
                GoEChargerResult(true, status, null)
            } else {
                GoEChargerResult(false, null, "HTTP error: $responseCode")
            }
        } catch (e: ConnectException) {
            GoEChargerResult(false, null, "Connection refused")
        } catch (e: SocketTimeoutException) {
            GoEChargerResult(false, null, "Connection timeout")
        } catch (e: IOException) {
            GoEChargerResult(false, null, "Network error: ${e.message}")
        } catch (e: Exception) {
            GoEChargerResult(false, null, "Unexpected error: ${e.message}")
        }
    }
    
    suspend fun setCurrentLimit(ipAddress: String, ampere: Int): GoEChargerResult<String> = withContext(Dispatchers.IO) {
        if (ipAddress.isBlank()) {
            return@withContext GoEChargerResult(false, null, "IP address is empty")
        }
        
        if (ampere < 6 || ampere > 32) {
            return@withContext GoEChargerResult(false, null, "Current must be between 6 and 32 ampere")
        }
        
        try {
            val url = URL("http://$ipAddress/api/set?amp=$ampere")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = json.parseToJsonElement(response) as JsonObject
                
                // Check if the setting was successful
                val ampResult = jsonResponse["amp"]?.jsonPrimitive?.content
                if (ampResult == "true" || ampResult?.toIntOrNull() == ampere) {
                    GoEChargerResult(true, "Current limit set to ${ampere}A", null)
                } else {
                    GoEChargerResult(false, null, "Failed to set current: $ampResult")
                }
            } else {
                GoEChargerResult(false, null, "HTTP error: $responseCode")
            }
        } catch (e: ConnectException) {
            GoEChargerResult(false, null, "Connection refused")
        } catch (e: SocketTimeoutException) {
            GoEChargerResult(false, null, "Connection timeout")
        } catch (e: IOException) {
            GoEChargerResult(false, null, "Network error: ${e.message}")
        } catch (e: Exception) {
            GoEChargerResult(false, null, "Unexpected error: ${e.message}")
        }
    }
    
    suspend fun setEnergyLimit(ipAddress: String, energyWh: Double): GoEChargerResult<String> = withContext(Dispatchers.IO) {
        if (ipAddress.isBlank()) {
            return@withContext GoEChargerResult(false, null, "IP address is empty")
        }
        
        try {
            val url = URL("http://$ipAddress/api/set?dwo=${energyWh.toInt()}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = json.parseToJsonElement(response) as JsonObject
                
                // Check if the setting was successful
                val energyResult = jsonResponse["dwo"]?.jsonPrimitive?.content
                if (energyResult == "true") {
                    GoEChargerResult(true, "Energy limit set to ${energyWh.toInt()} Wh", null)
                } else {
                    GoEChargerResult(false, null, "Failed to set energy limit: $energyResult")
                }
            } else {
                GoEChargerResult(false, null, "HTTP error: $responseCode")
            }
        } catch (e: ConnectException) {
            GoEChargerResult(false, null, "Connection refused")
        } catch (e: SocketTimeoutException) {
            GoEChargerResult(false, null, "Connection timeout")
        } catch (e: IOException) {
            GoEChargerResult(false, null, "Network error: ${e.message}")
        } catch (e: Exception) {
            GoEChargerResult(false, null, "Unexpected error: ${e.message}")
        }
    }
    
    fun getCarStateDescription(carState: Int?): String {
        return when (carState) {
            0 -> "Unknown/Error"
            1 -> "Idle"
            2 -> "Charging"
            3 -> "Wait for car"
            4 -> "Complete"
            5 -> "Error"
            else -> "Unknown"
        }
    }
}

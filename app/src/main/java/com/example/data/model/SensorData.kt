package com.example.data.model

import com.squareup.moshi.Json

data class AquaSustainData(
    @Json(name = "rain") val rain: Boolean = false,
    @Json(name = "rainStatus") val rainStatus: String? = null,
    @Json(name = "soilMoisture") val soilMoisture: Int = 35,
    @Json(name = "waterLevel") val waterLevel: Int = 72,
    @Json(name = "pump") val pump: Boolean = true,
    @Json(name = "pumpStatus") val pumpStatus: String? = null,
    @Json(name = "systemStatus") val systemStatus: String? = null,
    @Json(name = "mode") val mode: String = "auto",
    @Json(name = "deviceId") val deviceId: String = "AQUA-001",
    val updatedAtEpochMs: Long = System.currentTimeMillis()
) {
    val isAutoMode: Boolean
        get() = mode.equals("auto", ignoreCase = true)

    fun sanitized(): AquaSustainData {
        return copy(
            soilMoisture = soilMoisture.coerceIn(0, 100),
            waterLevel = waterLevel.coerceIn(0, 100)
        )
    }

    val isSoilDry: Boolean
        get() = soilMoisture < 50

    val isWaterLow: Boolean
        get() = waterLevel < 20

    val soilStatus: SoilStatus
        get() = if (isSoilDry) SoilStatus.DRY else SoilStatus.WET

    val tankLevelCategory: TankLevelCategory
        get() = when {
            waterLevel < 20 -> TankLevelCategory.LOW
            waterLevel > 80 -> TankLevelCategory.HIGH
            else -> TankLevelCategory.NORMAL
        }

    val rainStatusText: String
        get() = rainStatus ?: if (rain) "Rain Detected" else "No Rain"

    val pumpStatusText: String
        get() = if (pumpStatus != null) "PUMP $pumpStatus" else (if (pump) "PUMP ON" else "PUMP OFF")

    val irrigationSubtext: String
        get() = if (pump) "Irrigation Active" else "Irrigation Inactive"

    /**
     * AquaSustain Irrigation Logic:
     * Priority:
     * 1. Rain protection: IF rain is detected -> Pump OFF
     * 2. Low water protection: ELSE IF water level < 20% -> Pump OFF
     * 3. Dry soil irrigation: ELSE IF soil moisture < 50% -> Pump ON
     * 4. Adequate soil moisture: ELSE -> Pump OFF
     */
    val resolvedSystemStatus: String
        get() = if (!systemStatus.isNullOrBlank()) {
            systemStatus
        } else when {
            rain -> "RAIN DETECTED - IRRIGATION OFF"
            isWaterLow -> "WATER LEVEL < 20% - IRRIGATION OFF (LOW WATER PROTECTION)"
            isSoilDry -> "NO RAIN + SOIL < 50% - IRRIGATION ON"
            else -> "NO RAIN + SOIL >= 50% - IRRIGATION OFF"
        }

    val activeRuleDescription: String
        get() = resolvedSystemStatus

    val activeRuleIndex: Int
        get() = when {
            rain -> 0           // 1. Rain detected -> Pump OFF
            isWaterLow -> 1     // 2. Low water < 20% -> Pump OFF
            isSoilDry -> 2      // 3. Dry soil < 50% -> Pump ON
            else -> 3           // 4. Adequate soil >= 50% -> Pump OFF
        }

    /**
     * Calculates the autonomous target state of the pump based on live sensors
     */
    val targetAutoPumpState: Boolean
        get() = when {
            rain -> false
            isWaterLow -> false
            isSoilDry -> true
            else -> false
        }
}

data class PumpResponse(
    @Json(name = "pump") val pump: Boolean
)

enum class SoilStatus(val label: String) {
    DRY("Dry Soil (<50%)"),
    WET("Wet Soil (≥50%)")
}

enum class TankLevelCategory(val label: String, val advisory: String) {
    LOW("Low Level (<20%)", "Water level low. Underground recharge needed. Pump disabled."),
    NORMAL("Normal Level (20-80%)", "Healthy rainwater storage reserve."),
    HIGH("High Level (>80%)", "Storage tank near maximum collection capacity.")
}

sealed interface ConnectionStatus {
    data object Connected : ConnectionStatus
    data object Disconnected : ConnectionStatus
    data class ConnectionLost(val lastSeenEpochMs: Long, val message: String = "Connection Lost") : ConnectionStatus
}

data class SystemStatusSummary(
    val arduinoStatus: String = "CONNECTED",
    val isArduinoOnline: Boolean = true,
    val wifiStatus: String = "CONNECTED",
    val isWifiOnline: Boolean = true,
    val sensorsStatus: String = "ONLINE",
    val areSensorsOnline: Boolean = true,
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
)

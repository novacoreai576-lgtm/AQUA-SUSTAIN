package com.example.data.model

import com.google.firebase.Timestamp

data class DeviceItem(
    val deviceId: String = "AQUA-001",
    val name: String = "AquaSustain Farm",
    val board: String = "Arduino UNO R4 WiFi",
    val online: Boolean = true,
    val lastSeen: Timestamp? = null
)

data class SensorReading(
    val id: String = "",
    val rain: Boolean = false,
    val soilMoisture: Int = 35,
    val waterLevel: Int = 72,
    val pump: Boolean = false,
    val timestamp: Timestamp? = null,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

data class PumpCommand(
    val id: String = "",
    val command: String = "PUMP_OFF", // "PUMP_ON" or "PUMP_OFF"
    val source: String = "app",
    val createdAt: Timestamp? = null,
    val processed: Boolean = false
)

data class HourlyForecastItemData(
    val time: String = "Now",
    val tempC: Int = 27,
    val rainProb: Int = 20,
    val condition: String = "Partly Cloudy",
    val weatherCode: Int = 2,
    val rainMm: Double = 0.0
)

data class DailyForecastItemData(
    val day: String = "Today",
    val date: String = "",
    val maxTempC: Int = 30,
    val minTempC: Int = 22,
    val rainProb: Int = 30,
    val rainSumMm: Double = 0.0,
    val condition: String = "Partly Cloudy",
    val weatherCode: Int = 2
)

data class GeoLocation(
    val name: String = "Farm Station",
    val country: String = "Local",
    val admin1: String? = null,
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090
) {
    val displayName: String
        get() = if (admin1.isNullOrBlank() || admin1 == name) "$name, $country" else "$name, $admin1, $country"
}

data class WeatherInfo(
    val temperatureC: Int = 27,
    val condition: String = "Partly Cloudy",
    val rainProbabilityPercent: Int = 75,
    val rainPredictionText: String = "Rain likely in next 3 hours",
    val smartRecommendation: String = "High rain probability (75%). Postponing irrigation will conserve underground tank water.",
    val humidityPercent: Int = 68,
    val feelsLikeC: Int = 28,
    val windSpeedKmh: Double = 12.0,
    val precipitationMm: Double = 0.0,
    val locationName: String = "Farm Station",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val lastUpdatedTime: String = "Just now",
    val isLive: Boolean = true,
    val weatherCode: Int = 2,
    val hourlyForecast: List<HourlyForecastItemData> = emptyList(),
    val dailyForecast: List<DailyForecastItemData> = emptyList()
)

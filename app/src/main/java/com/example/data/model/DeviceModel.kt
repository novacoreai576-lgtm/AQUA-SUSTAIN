package com.example.data.model

import com.google.firebase.Timestamp

data class DeviceItem(
    val deviceId: String = "AQUA-001",
    val name: String = "AquaSustain Monitor",
    val board: String = "Arduino UNO R4 WiFi",
    val online: Boolean = false,
    val lastSeen: Timestamp? = null
)

data class SensorReading(
    val id: String = "",
    val rain: Boolean = false,
    val soilMoisture: Int = 0,
    val waterLevel: Int = 0,
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
    val tempC: Int = 0,
    val rainProb: Int = 0,
    val condition: String = "Unavailable",
    val weatherCode: Int = 2,
    val rainMm: Double = 0.0
)

data class DailyForecastItemData(
    val day: String = "Today",
    val date: String = "",
    val maxTempC: Int = 0,
    val minTempC: Int = 0,
    val rainProb: Int = 0,
    val rainSumMm: Double = 0.0,
    val condition: String = "Unavailable",
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
    val temperatureC: Int = 0,
    val condition: String = "Waiting for data connection.",
    val rainProbabilityPercent: Int = 0,
    val rainPredictionText: String = "Waiting for weather data connection.",
    val smartRecommendation: String = "Connect to the internet or refresh weather to view Open-Meteo environmental insights.",
    val humidityPercent: Int = 0,
    val feelsLikeC: Int = 0,
    val windSpeedKmh: Double = 0.0,
    val precipitationMm: Double = 0.0,
    val locationName: String = "Select Location",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val lastUpdatedTime: String = "Not synced",
    val isLive: Boolean = false,
    val hasFetched: Boolean = false,
    val weatherCode: Int = 2,
    val hourlyForecast: List<HourlyForecastItemData> = emptyList(),
    val dailyForecast: List<DailyForecastItemData> = emptyList()
)

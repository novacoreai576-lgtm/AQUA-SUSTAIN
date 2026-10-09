package com.example.data.remote

import android.util.Log
import com.example.data.model.DailyForecastItemData
import com.example.data.model.GeoLocation
import com.example.data.model.HourlyForecastItemData
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * WeatherApiClient handles live atmospheric telemetry and multi-day agricultural
 * precipitation forecasting via the Open-Meteo REST API (zero-configuration, free, no API key required).
 */
class WeatherApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "WeatherApiClient"
        private const val FORECAST_BASE_URL = "https://api.open-meteo.com/v1/forecast"
        private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/v1/search"

        val POPULAR_FARM_LOCATIONS = listOf(
            GeoLocation("New Delhi", "India", "Delhi", 28.6139, 77.2090),
            GeoLocation("Punjab (Ludhiana)", "India", "Punjab", 30.9010, 75.8573),
            GeoLocation("California Valley (Fresno)", "USA", "California", 36.7468, -119.7726),
            GeoLocation("London", "United Kingdom", "England", 51.5074, -0.1278),
            GeoLocation("Tokyo", "Japan", "Tokyo", 35.6762, 139.6503),
            GeoLocation("Nairobi", "Kenya", "Nairobi County", -1.2921, 36.8219),
            GeoLocation("Manila", "Philippines", "Metro Manila", 14.5995, 120.9842),
            GeoLocation("Sydney", "Australia", "New South Wales", -33.8688, 151.2093)
        )
    }

    /**
     * Fetches real-time ambient weather and 24h/7d forecast for given coordinates.
     */
    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        locationName: String = "Farm Station"
    ): Result<WeatherInfo> = withContext(Dispatchers.IO) {
        val url = "$FORECAST_BASE_URL?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,wind_speed_10m" +
                "&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,rain,weather_code" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,rain_sum" +
                "&timezone=auto"

        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "AquaSustain-Android/1.0")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val msg = "Weather API returned HTTP status ${response.code}: ${response.message}"
                    Log.w(TAG, msg)
                    return@withContext Result.failure(Exception(msg))
                }

                val body = response.body?.string()
                if (body.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Empty response body from Weather API"))
                }

                val root = JSONObject(body)
                val current = root.optJSONObject("current")

                val tempC = current?.optDouble("temperature_2m", 27.0)?.roundToInt() ?: 27
                val humidity = current?.optInt("relative_humidity_2m", 65) ?: 65
                val feelsLike = current?.optDouble("apparent_temperature", tempC.toDouble())?.roundToInt() ?: tempC
                val windSpeed = current?.optDouble("wind_speed_10m", 12.0) ?: 12.0
                val precipMm = current?.optDouble("precipitation", 0.0) ?: 0.0
                val weatherCode = current?.optInt("weather_code", 2) ?: 2
                val conditionStr = parseWeatherCode(weatherCode)

                // Parse Hourly Forecast
                val hourlyList = mutableListOf<HourlyForecastItemData>()
                val hourlyObj = root.optJSONObject("hourly")
                var maxRainProbability = 0

                if (hourlyObj != null) {
                    val times = hourlyObj.optJSONArray("time")
                    val temps = hourlyObj.optJSONArray("temperature_2m")
                    val probs = hourlyObj.optJSONArray("precipitation_probability")
                    val codes = hourlyObj.optJSONArray("weather_code")
                    val rains = hourlyObj.optJSONArray("rain")

                    val len = times?.length() ?: 0
                    // Select next 12 hours starting from now
                    for (i in 0 until minOf(len, 24)) {
                        val rawTime = times?.optString(i) ?: ""
                        val formattedTime = formatHourlyTime(rawTime, i)
                        val t = temps?.optDouble(i, 25.0)?.roundToInt() ?: 25
                        val prob = probs?.optInt(i, 0) ?: 0
                        val code = codes?.optInt(i, 1) ?: 1
                        val rainMm = rains?.optDouble(i, 0.0) ?: 0.0

                        if (i in 0..6 && prob > maxRainProbability) {
                            maxRainProbability = prob
                        }

                        hourlyList.add(
                            HourlyForecastItemData(
                                time = formattedTime,
                                tempC = t,
                                rainProb = prob,
                                condition = parseWeatherCode(code),
                                weatherCode = code,
                                rainMm = rainMm
                            )
                        )
                    }
                }

                // Parse Daily Forecast (7 days)
                val dailyList = mutableListOf<DailyForecastItemData>()
                val dailyObj = root.optJSONObject("daily")
                if (dailyObj != null) {
                    val dTimes = dailyObj.optJSONArray("time")
                    val dCodes = dailyObj.optJSONArray("weather_code")
                    val dMaxTemps = dailyObj.optJSONArray("temperature_2m_max")
                    val dMinTemps = dailyObj.optJSONArray("temperature_2m_min")
                    val dProbs = dailyObj.optJSONArray("precipitation_probability_max")
                    val dRains = dailyObj.optJSONArray("rain_sum")

                    val dLen = dTimes?.length() ?: 0
                    for (i in 0 until minOf(dLen, 7)) {
                        val rawDate = dTimes?.optString(i) ?: ""
                        val dayLabel = formatDailyDay(rawDate, i)
                        val code = dCodes?.optInt(i, 2) ?: 2
                        val maxT = dMaxTemps?.optDouble(i, 30.0)?.roundToInt() ?: 30
                        val minT = dMinTemps?.optDouble(i, 22.0)?.roundToInt() ?: 22
                        val prob = dProbs?.optInt(i, 20) ?: 20
                        val rainSum = dRains?.optDouble(i, 0.0) ?: 0.0

                        dailyList.add(
                            DailyForecastItemData(
                                day = dayLabel,
                                date = rawDate,
                                maxTempC = maxT,
                                minTempC = minT,
                                rainProb = prob,
                                rainSumMm = rainSum,
                                condition = parseWeatherCode(code),
                                weatherCode = code
                            )
                        )
                    }
                }

                val rainProbFinal = if (maxRainProbability > 0) maxRainProbability else if (precipMm > 0.0) 85 else 20
                val rainPrediction = when {
                    rainProbFinal >= 70 -> "High rain likelihood in next 3 hours"
                    rainProbFinal >= 40 -> "Scattered rain possible today"
                    precipMm > 0.0 -> "Precipitation currently detected (${precipMm}mm)"
                    else -> "Low precipitation probability"
                }

                val smartRecommendation = when {
                    rainProbFinal >= 65 ->
                        "High rain probability ($rainProbFinal%). Postponing irrigation will conserve underground tank water."
                    tempC >= 33 ->
                        "High ambient temperature (${tempC}°C). Evaporation rate elevated; schedule irrigation during cooler morning/evening hours."
                    humidity <= 40 ->
                        "Low ambient humidity ($humidity%). Monitor soil moisture closely as drying accelerates."
                    else ->
                        "Stable atmospheric conditions. System maintaining automated soil moisture balance."
                }

                val currentTimeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

                val weatherInfo = WeatherInfo(
                    temperatureC = tempC,
                    condition = conditionStr,
                    rainProbabilityPercent = rainProbFinal,
                    rainPredictionText = rainPrediction,
                    smartRecommendation = smartRecommendation,
                    humidityPercent = humidity,
                    feelsLikeC = feelsLike,
                    windSpeedKmh = windSpeed,
                    precipitationMm = precipMm,
                    locationName = locationName,
                    latitude = latitude,
                    longitude = longitude,
                    lastUpdatedTime = currentTimeFormatted,
                    isLive = true,
                    weatherCode = weatherCode,
                    hourlyForecast = hourlyList,
                    dailyForecast = dailyList
                )

                Result.success(weatherInfo)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch weather from $url: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Search global locations by query using Open-Meteo Geocoding API
     */
    suspend fun searchLocations(query: String): Result<List<GeoLocation>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            return@withContext Result.success(POPULAR_FARM_LOCATIONS)
        }

        val encoded = URLEncoder.encode(trimmed, "UTF-8")
        val url = "$GEOCODING_BASE_URL?name=$encoded&count=8&language=en&format=json"

        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "AquaSustain-Android/1.0")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Geocoding failed: ${response.code}"))
                }
                val body = response.body?.string()
                if (body.isNullOrBlank()) {
                    return@withContext Result.success(emptyList())
                }

                val root = JSONObject(body)
                val resultsArr = root.optJSONArray("results") ?: return@withContext Result.success(emptyList())

                val locations = mutableListOf<GeoLocation>()
                for (i in 0 until resultsArr.length()) {
                    val item = resultsArr.optJSONObject(i) ?: continue
                    val name = item.optString("name", "")
                    val country = item.optString("country", "")
                    val admin1 = item.optString("admin1", null)
                    val lat = item.optDouble("latitude", 0.0)
                    val lon = item.optDouble("longitude", 0.0)

                    if (name.isNotBlank()) {
                        locations.add(GeoLocation(name, country, admin1, lat, lon))
                    }
                }
                Result.success(locations)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Search location error: ${e.message}")
            Result.failure(e)
        }
    }

    private fun parseWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61 -> "Slight Rain"
            63 -> "Moderate Rain"
            65 -> "Heavy Rain"
            66, 67 -> "Freezing Rain"
            71 -> "Slight Snow"
            73 -> "Moderate Snow"
            75 -> "Heavy Snow"
            77 -> "Snow Grains"
            80, 81 -> "Rain Showers"
            82 -> "Violent Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Partly Cloudy"
        }
    }

    private fun formatHourlyTime(isoString: String, index: Int): String {
        if (index == 0) return "Now"
        return try {
            val parts = isoString.split("T")
            if (parts.size == 2) {
                val timeParts = parts[1].split(":")
                val hour = timeParts[0].toInt()
                when {
                    hour == 0 -> "12 AM"
                    hour < 12 -> "${hour} AM"
                    hour == 12 -> "12 PM"
                    else -> "${hour - 12} PM"
                }
            } else {
                "+${index}h"
            }
        } catch (_: Exception) {
            "+${index}h"
        }
    }

    private fun formatDailyDay(dateString: String, index: Int): String {
        if (index == 0) return "Today"
        if (index == 1) return "Tomorrow"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(dateString)
            if (date != null) {
                SimpleDateFormat("EEE", Locale.getDefault()).format(date)
            } else {
                "+${index}d"
            }
        } catch (_: Exception) {
            "+${index}d"
        }
    }
}

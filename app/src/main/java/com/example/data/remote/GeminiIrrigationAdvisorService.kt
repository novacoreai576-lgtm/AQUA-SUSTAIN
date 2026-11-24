package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AquaSustainData
import com.example.data.model.IrrigationAdvice
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * GeminiIrrigationAdvisorService connects to Google Gemini API (model: gemini-3.5-flash)
 * to analyze live hardware telemetry and atmospheric weather forecasts.
 *
 * Strictly NO DEMO / NO MOCK MODE: If GEMINI_API_KEY is not configured or hardware
 * telemetry is unavailable, returns a clear Result.failure with setup/connection details.
 */
class GeminiIrrigationAdvisorService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "GeminiAdvisorService"
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

        fun isApiKeyConfigured(): Boolean {
            val key = try {
                BuildConfig.GEMINI_API_KEY
            } catch (_: Exception) {
                ""
            }
            return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        }
    }

    suspend fun generateIrrigationAdvice(
        sensorData: AquaSustainData,
        weather: WeatherInfo
    ): Result<IrrigationAdvice> = withContext(Dispatchers.IO) {
        if (!sensorData.hasLiveData) {
            return@withContext Result.failure(
                IllegalStateException("Device Not Connected. Connect the Arduino hardware to run sensor-based AI irrigation analysis.")
            )
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("GEMINI_API_KEY is not configured. Add GEMINI_API_KEY in the AI Studio Secrets panel to enable Gemini AI irrigation insights.")
            )
        }

        try {
            val prompt = buildAdvisorPrompt(sensorData, weather)
            val jsonBody = buildGeminiRequestBody(prompt)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errCode = response.code
                    val errMsg = response.body?.string() ?: response.message
                    Log.w(TAG, "Gemini API HTTP $errCode: $errMsg")
                    return@withContext Result.failure(
                        Exception("Gemini AI request failed (HTTP $errCode). Check API key and network connectivity.")
                    )
                }

                val respStr = response.body?.string() ?: ""
                val rootJson = JSONObject(respStr)
                val candidates = rootJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                if (text.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Gemini AI returned an empty response."))
                }

                Result.success(parseGeminiResponse(text))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error invoking Gemini API: ${e.message}")
            Result.failure(Exception("Gemini AI request failed: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    private fun buildAdvisorPrompt(sensorData: AquaSustainData, weather: WeatherInfo): String {
        val weatherSection = if (weather.hasFetched) {
            """
            [ATMOSPHERIC WEATHER & PRECIPITATION FORECAST]
            - Location: ${weather.locationName}
            - Current Condition: ${weather.condition}
            - Ambient Temperature: ${weather.temperatureC}°C (Feels like ${weather.feelsLikeC}°C)
            - Ambient Humidity: ${weather.humidityPercent}%
            - Rain Probability: ${weather.rainProbabilityPercent}%
            - Rain Forecast Summary: ${weather.rainPredictionText}
            - Precipitation Current: ${weather.precipitationMm} mm
            """.trimIndent()
        } else {
            "[ATMOSPHERIC WEATHER]: Weather API not fetched yet."
        }

        return """
You are the AquaSustain Smart Rainwater Harvesting & Sustainable Irrigation AI Advisor.
Analyze only the real telemetry and weather data below (do not invent missing numbers):

[REAL-TIME HARDWARE SENSOR READINGS]
- Rain Sensor: ${if (sensorData.rain) "RAIN DETECTED ON SENSOR" else "NO RAIN ON SENSOR"}
- Soil Moisture: ${sensorData.soilMoisture}% (${sensorData.soilStatus.label})
- Underground Water Tank Level: ${sensorData.waterLevel}% (${sensorData.tankLevelCategory.name})
- Current Pump State: ${if (sensorData.pump) "PUMP ACTIVE / RUNNING" else "PUMP IDLE / OFF"}
- Hardware Board: Arduino UNO R4 WiFi

$weatherSection

[CONSTRAINTS & RULES]
1. SAFETY FIRST: The AI provides advice only; pump control is managed safely on hardware.
2. If rain is detected OR rain probability is high (>= 60%), strongly advise POSTPONING or SKIPPING irrigation.
3. If water tank is low (< 20%), warn urgently to conserve water and avoid running the pump dry.
4. If soil is dry (< 50%) with low rain probability and sufficient tank water, recommend irrigation during cooler hours.
5. Provide 2-3 practical water-saving tips grounded in these readings.

Provide raw JSON with exact keys:
{
  "recommendation": "Short 2-4 word status header",
  "shouldIrrigate": false,
  "explanation": "Concise 2-3 sentence explanation based on measured values.",
  "rainWarning": "Short warning if rain is expected, or null",
  "tankWarning": "Short warning if tank level is low (< 25%), or null",
  "waterSavingTips": ["Tip 1", "Tip 2", "Tip 3"]
}
Only output the JSON object without markdown fences.
""".trimIndent()
    }

    private fun buildGeminiRequestBody(promptText: String): JSONObject {
        val part = JSONObject().put("text", promptText)
        val contentObj = JSONObject().put("parts", JSONArray().put(part))
        val genConfig = JSONObject()
            .put("temperature", 0.3)
            .put("responseMimeType", "application/json")

        return JSONObject()
            .put("contents", JSONArray().put(contentObj))
            .put("generationConfig", genConfig)
    }

    private fun parseGeminiResponse(rawText: String): IrrigationAdvice {
        val cleaned = rawText
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = JSONObject(cleaned)
        val recommendation = json.optString("recommendation", "ADVISORY UPDATED")
        val shouldIrrigate = json.optBoolean("shouldIrrigate", false)
        val explanation = json.optString("explanation", "")
        val rainWarning = if (json.isNull("rainWarning")) null else json.optString("rainWarning").ifBlank { null }
        val tankWarning = if (json.isNull("tankWarning")) null else json.optString("tankWarning").ifBlank { null }

        val tipsList = mutableListOf<String>()
        val tipsArr = json.optJSONArray("waterSavingTips")
        if (tipsArr != null) {
            for (i in 0 until tipsArr.length()) {
                val tip = tipsArr.optString(i)
                if (tip.isNotBlank()) tipsList.add(tip)
            }
        }

        return IrrigationAdvice(
            recommendation = recommendation,
            shouldIrrigate = shouldIrrigate,
            explanation = explanation,
            rainWarning = rainWarning,
            tankWarning = tankWarning,
            waterSavingTips = tipsList,
            confidence = "Gemini AI",
            generatedByAi = true,
            timestampMs = System.currentTimeMillis()
        )
    }
}

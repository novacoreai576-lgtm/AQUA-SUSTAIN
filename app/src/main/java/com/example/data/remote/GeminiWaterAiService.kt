package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AquaSustainData
import com.example.data.model.WaterAssistantMessage
import com.example.data.model.WaterQualityAnalysisRecord
import com.example.data.model.WaterQualityInput
import com.example.data.model.WaterUsageSummary
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * GeminiWaterAiService powers real Gemini API calls for:
 * 1. Water Quality Analysis (numerical sensor/lab parameters + optional water image)
 * 2. Multilingual AI Water Assistant (AquaSustain context-aware guidance)
 *
 * Strictly NO DEMO MODE: Never returns fabricated responses if GEMINI_API_KEY
 * is missing or the API call fails.
 */
class GeminiWaterAiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "GeminiWaterAiService"
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

        val SUPPORTED_LANGUAGES = listOf(
            "English",
            "தமிழ் (Tamil)",
            "తెలుగు (Telugu)",
            "മലയാളം (Malayalam)",
            "हिन्दी (Hindi)",
            "Auto-detect User Language"
        )
    }

    fun isConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun analyzeWaterQuality(
        input: WaterQualityInput,
        imageBase64: String? = null,
        imageMimeType: String? = null
    ): Result<WaterQualityAnalysisRecord> = withContext(Dispatchers.IO) {
        if (!input.hasAnyMeasurement() && imageBase64.isNullOrBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Enter at least one water quality measurement (pH, Turbidity, Temp, TDS, EC, or DO) or attach a water image.")
            )
        }

        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Add GEMINI_API_KEY in the AI Studio Secrets panel to run AI water quality analysis.")
            )
        }

        try {
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", buildWaterQualityPrompt(input, !imageBase64.isNullOrBlank())))

            if (!imageBase64.isNullOrBlank()) {
                val inlineData = JSONObject()
                    .put("mimeType", imageMimeType ?: "image/jpeg")
                    .put("data", imageBase64)
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            val contentsArray = JSONArray().put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", partsArray)
            )

            val genConfig = JSONObject()
                .put("temperature", 0.25)
                .put("responseMimeType", "application/json")

            val requestJson = JSONObject()
                .put("contents", contentsArray)
                .put("generationConfig", genConfig)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Log.w(TAG, "Water Quality Gemini HTTP ${response.code}: $rawBody")
                    return@withContext Result.failure(
                        Exception("Gemini API error (HTTP ${response.code}). Verify GEMINI_API_KEY and network connection.")
                    )
                }

                val text = extractCandidateText(rawBody)
                    ?: return@withContext Result.failure(Exception("Gemini AI returned no analysis text."))

                Result.success(parseWaterQualityJson(text, input, !imageBase64.isNullOrBlank()))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Water Quality analysis failed: ${e.message}")
            Result.failure(Exception("Water quality analysis failed: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    suspend fun sendWaterAssistantMessage(
        userMessage: String,
        preferredLanguage: String,
        recentHistory: List<WaterAssistantMessage>,
        sensorData: AquaSustainData,
        weather: WeatherInfo,
        latestQuality: WaterQualityAnalysisRecord?,
        usageSummary: WaterUsageSummary
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanPrompt = userMessage.trim()
        if (cleanPrompt.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a question."))
        }

        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Add GEMINI_API_KEY in the AI Studio Secrets panel to use the AI Water Assistant.")
            )
        }

        try {
            val systemContext = buildAssistantSystemContext(
                preferredLanguage = preferredLanguage,
                sensorData = sensorData,
                weather = weather,
                latestQuality = latestQuality,
                usageSummary = usageSummary
            )

            val contentsArray = JSONArray()
            recentHistory.takeLast(10).forEach { msg ->
                val geminiRole = if (msg.role == "assistant") "model" else "user"
                contentsArray.put(
                    JSONObject()
                        .put("role", geminiRole)
                        .put("parts", JSONArray().put(JSONObject().put("text", msg.content)))
                )
            }
            contentsArray.put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", cleanPrompt)))
            )

            val systemInstruction = JSONObject()
                .put("parts", JSONArray().put(JSONObject().put("text", systemContext)))

            val genConfig = JSONObject()
                .put("temperature", 0.4)

            val requestJson = JSONObject()
                .put("systemInstruction", systemInstruction)
                .put("contents", contentsArray)
                .put("generationConfig", genConfig)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Log.w(TAG, "Assistant Gemini HTTP ${response.code}: $rawBody")
                    return@withContext Result.failure(
                        Exception("Gemini AI error (HTTP ${response.code}). Check GEMINI_API_KEY and network connection.")
                    )
                }

                val text = extractCandidateText(rawBody)
                    ?: return@withContext Result.failure(Exception("AI Water Assistant received an empty response."))

                Result.success(text.trim())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Assistant failed: ${e.message}")
            Result.failure(Exception("AI Water Assistant failed: ${e.localizedMessage ?: "Network error"}"))
        }
    }

    private fun extractCandidateText(rawBody: String): String? {
        val root = JSONObject(rawBody)
        val candidates = root.optJSONArray("candidates") ?: return null
        val first = candidates.optJSONObject(0) ?: return null
        val content = first.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        return parts.optJSONObject(0)?.optString("text")?.ifBlank { null }
    }

    private fun buildWaterQualityPrompt(input: WaterQualityInput, hasImage: Boolean): String {
        return """
You are AquaSustain's Water Quality Assessment AI.

CRITICAL SCIENTIFIC INTEGRITY RULES:
1. NEVER invent, estimate, or hallucinate numerical chemical parameters (pH, TDS, EC, DO, turbidity, heavy metals, pathogens) that were not explicitly provided by the user.
2. An image alone CANNOT measure chemical or microbiological water parameters. If an image is attached, restrict "visualObservations" strictly to visible physical appearance cues (e.g., visible suspended sediment, algae tint, foam, container cleanliness) and clearly note visual limitations.
3. Explicitly state in the disclaimer that field parameters or visual inspection cannot certify safe drinking water without accredited laboratory testing and compliance with WHO/local standards.
4. Distinguish clearly between user-measured parameters and general recommendations.

[USER-PROVIDED / MEASURED INPUTS]
- Water Source Label: ${input.sourceName.ifBlank { "Unspecified water sample" }}
- pH: ${input.ph?.toString() ?: "Not measured"}
- Turbidity (NTU): ${input.turbidityNtu?.toString() ?: "Not measured"}
- Temperature (°C): ${input.temperatureC?.toString() ?: "Not measured"}
- Total Dissolved Solids / TDS (ppm): ${input.tdsPpm?.toString() ?: "Not measured"}
- Electrical Conductivity / EC (µS/cm): ${input.ecUsCm?.toString() ?: "Not measured"}
- Dissolved Oxygen / DO (mg/L): ${input.dissolvedOxygenMgL?.toString() ?: "Not measured"}
- Field Notes: ${input.notes.ifBlank { "None" }}
- Image Attached: ${if (hasImage) "YES (inspect visual context only; do NOT fabricate chemical readings from photo)" else "NO"}

Return raw JSON with these exact keys (no markdown fences):
{
  "statusCategory": "Within Typical Guidelines" | "Attention Recommended" | "Concern / Out of Range" | "Incomplete Measurements",
  "summary": "2-3 sentence scientific summary referencing only the provided parameters and/or visible cues.",
  "measuredFindings": "Concise breakdown comparing each provided numerical measurement against typical irrigation/domestic/WHO reference ranges, or stating 'No numerical parameters supplied' if none were given.",
  "visualObservations": "Brief visual appearance notes if an image was attached (with explicit note that appearance cannot detect dissolved chemicals or pathogens), or null if no image.",
  "warnings": ["Specific alert 1", "Specific alert 2"],
  "recommendations": ["Practical next step 1", "Practical next step 2"],
  "disclaimer": "Clear statement that field measurements or images alone cannot certify potable drinking water safety without laboratory testing."
}
""".trimIndent()
    }

    private fun parseWaterQualityJson(
        rawText: String,
        input: WaterQualityInput,
        hasImage: Boolean
    ): WaterQualityAnalysisRecord {
        val cleaned = rawText
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = JSONObject(cleaned)
        val status = json.optString("statusCategory", "Attention Recommended")
        val summary = json.optString("summary", "")
        val measured = json.optString("measuredFindings", "")
        val visual = if (json.isNull("visualObservations")) null else json.optString("visualObservations").ifBlank { null }

        val warnings = mutableListOf<String>()
        json.optJSONArray("warnings")?.let { arr ->
            for (i in 0 until arr.length()) {
                arr.optString(i).takeIf { it.isNotBlank() }?.let { warnings.add(it) }
            }
        }

        val recommendations = mutableListOf<String>()
        json.optJSONArray("recommendations")?.let { arr ->
            for (i in 0 until arr.length()) {
                arr.optString(i).takeIf { it.isNotBlank() }?.let { recommendations.add(it) }
            }
        }

        val disclaimer = json.optString("disclaimer")
            .ifBlank { WaterQualityAnalysisRecord.DEFAULT_SAFETY_DISCLAIMER }

        return WaterQualityAnalysisRecord(
            id = UUID.randomUUID().toString(),
            sourceName = input.sourceName.ifBlank { "Water Sample" },
            ph = input.ph,
            turbidityNtu = input.turbidityNtu,
            temperatureC = input.temperatureC,
            tdsPpm = input.tdsPpm,
            ecUsCm = input.ecUsCm,
            dissolvedOxygenMgL = input.dissolvedOxygenMgL,
            notes = input.notes,
            hasAttachedImage = hasImage,
            statusCategory = status,
            summary = summary,
            measuredFindings = measured,
            visualObservations = visual,
            warnings = warnings,
            recommendations = recommendations,
            disclaimer = disclaimer,
            analyzedByGemini = true,
            createdAtEpochMs = System.currentTimeMillis()
        )
    }

    private fun buildAssistantSystemContext(
        preferredLanguage: String,
        sensorData: AquaSustainData,
        weather: WeatherInfo,
        latestQuality: WaterQualityAnalysisRecord?,
        usageSummary: WaterUsageSummary
    ): String {
        val hardwareText = if (sensorData.hasLiveData) {
            "CONNECTED (Device ${sensorData.deviceId}): Tank Level=${sensorData.waterLevel}%, Soil Moisture=${sensorData.soilMoisture}%, Rain=${sensorData.rain}, Pump=${if (sensorData.pump) "ON" else "OFF"}, Mode=${sensorData.mode}"
        } else {
            "Device Not Connected (no live Arduino telemetry available)."
        }

        val weatherText = if (weather.hasFetched) {
            "Location=${weather.locationName}, Temp=${weather.temperatureC}°C, Condition=${weather.condition}, Humidity=${weather.humidityPercent}%, Rain Prob=${weather.rainProbabilityPercent}%"
        } else {
            "Weather data not yet fetched."
        }

        val qualityText = if (latestQuality != null) {
            "Latest saved sample '${latestQuality.sourceName}' (${latestQuality.statusCategory}): pH=${latestQuality.ph ?: "N/A"}, Turbidity=${latestQuality.turbidityNtu ?: "N/A"} NTU, TDS=${latestQuality.tdsPpm ?: "N/A"} ppm, EC=${latestQuality.ecUsCm ?: "N/A"} µS/cm, DO=${latestQuality.dissolvedOxygenMgL ?: "N/A"} mg/L"
        } else {
            "No saved water quality analyses yet."
        }

        val usageText = if (usageSummary.totalRecords > 0) {
            "Saved logs=${usageSummary.totalRecords}: Today=${WaterUsageSummary.formatLiters(usageSummary.todayLiters)}, 7-Day=${WaterUsageSummary.formatLiters(usageSummary.weeklyLiters)}, 30-Day=${WaterUsageSummary.formatLiters(usageSummary.monthlyLiters)}"
        } else {
            "No water consumption records saved yet."
        }

        val languageInstruction = if (preferredLanguage == "Auto-detect User Language") {
            "Respond in the exact same language the user writes in (supports English, Tamil, Telugu, Malayalam, Hindi, and other languages)."
        } else {
            "Respond in $preferredLanguage (unless the user explicitly asks for another language in their message)."
        }

        return """
You are the AquaSustain AI Water Assistant — a knowledgeable, concise, scientifically accurate sustainability, rainwater harvesting, water quality, and water conservation advisor.

LANGUAGE RULE:
$languageInstruction

HONESTY & SAFETY RULES:
1. Only cite the live telemetry, weather, quality, or usage numbers below if they are marked available/saved. Never invent sensor readings or fake records.
2. When discussing drinking water safety or potability, clearly state that basic measurements or descriptions alone cannot confirm drinking water is safe without certified laboratory testing (for pathogens, heavy metals, arsenic, fluoride, nitrates, etc.).
3. Do not pretend the Arduino hardware is connected if the status below says Device Not Connected.

CURRENT AQUASUSTAIN CONTEXT:
- Hardware Monitoring: $hardwareText
- Weather: $weatherText
- Water Quality Record: $qualityText
- Water Usage Summary: $usageText
""".trimIndent()
    }
}

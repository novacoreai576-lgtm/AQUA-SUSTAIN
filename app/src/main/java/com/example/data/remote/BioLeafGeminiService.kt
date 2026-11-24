package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.knowledge.OrganicKnowledgeBase
import com.example.data.model.BioLeafChatMessage
import com.example.data.model.DiseaseReportRecord
import com.example.data.model.PestReportRecord
import com.example.data.model.PlantIdRecord
import com.example.data.model.SoilGuidanceInput
import com.example.data.model.SoilGuidanceRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class AssistantReplyResult(
    val text: String,
    val detectedLanguage: String
)

class BioLeafGeminiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val apiKeyProvider: () -> String = {
        try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
    }
) {
    companion object {
        private const val PRIMARY_MODEL = "gemini-3.5-flash"
        private const val FALLBACK_MODEL = "gemini-2.5-flash"
        private const val API_BASE = "https://generativelanguage.googleapis.com/v1beta/models"

        fun encodeBitmapToBase64(bitmap: Bitmap, maxDim: Int = 640, quality: Int = 80): String {
            val scaled = scaleBitmapDown(bitmap, maxDim)
            val stream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        }

        fun encodeThumbnailBase64(bitmap: Bitmap, maxDim: Int = 320, quality: Int = 65): String {
            return encodeBitmapToBase64(bitmap, maxDim = maxDim, quality = quality)
        }

        private fun scaleBitmapDown(bitmap: Bitmap, maxDim: Int): Bitmap {
            val width = bitmap.width
            val height = bitmap.height
            if (width <= maxDim && height <= maxDim) return bitmap
            val ratio = minOf(maxDim.toFloat() / width, maxDim.toFloat() / height)
            val targetW = (width * ratio).toInt().coerceAtLeast(1)
            val targetH = (height * ratio).toInt().coerceAtLeast(1)
            return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        }

        internal fun cleanJsonText(rawText: String): String {
            return rawText
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
        }

        internal fun jsonStringList(json: JSONObject, key: String): List<String> {
            val arr = json.optJSONArray(key) ?: return emptyList()
            return buildList {
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i).trim()
                    if (item.isNotBlank()) add(item)
                }
            }
        }

        internal fun parsePlantIdJson(rawText: String, thumbnailBase64: String? = null): PlantIdRecord {
            val json = JSONObject(cleanJsonText(rawText))
            return PlantIdRecord(
                commonName = json.optString("commonName", "Unidentified Plant").trim(),
                scientificName = json.optString("scientificName", "").trim(),
                plantDescription = json.optString("plantDescription", "").trim(),
                identifyingCharacteristics = jsonStringList(json, "identifyingCharacteristics"),
                identificationExplanation = json.optString("identificationExplanation", "").trim(),
                uncertaintyNote = json.optString("uncertaintyNote", "").trim(),
                imageThumbnailBase64 = thumbnailBase64,
                timestampEpochMs = System.currentTimeMillis()
            )
        }

        internal fun parseDiseaseReportJson(rawText: String, thumbnailBase64: String? = null): DiseaseReportRecord {
            val json = JSONObject(cleanJsonText(rawText))
            val problemValue = json.optString("problem", "").trim()
                .ifBlank { "Unable to verify a specific condition from the image" }
            return DiseaseReportRecord(
                plantName = json.optString("plantName", "Unknown Plant").trim(),
                problem = problemValue,
                severity = json.optString("severity", "Not determinable").trim(),
                visibleSymptoms = json.optString("visibleSymptoms", "").trim(),
                possibleCauses = json.optString("possibleCauses", "").trim(),
                detailedExplanation = json.optString("detailedExplanation", "").trim(),
                recommendedTreatment = jsonStringList(json, "recommendedTreatment"),
                organicTreatment = jsonStringList(json, "organicTreatment"),
                preventionMethods = jsonStringList(json, "preventionMethods"),
                visualObservationNote = json.optString(
                    "visualObservationNote",
                    "Based on visual inspection of the uploaded photograph; not a laboratory diagnostic test."
                ).trim(),
                imageThumbnailBase64 = thumbnailBase64,
                timestampEpochMs = System.currentTimeMillis()
            )
        }

        internal fun parsePestReportJson(rawText: String, thumbnailBase64: String? = null): PestReportRecord {
            val json = JSONObject(cleanJsonText(rawText))
            return PestReportRecord(
                possiblePestIdentification = json.optString("possiblePestIdentification", "Insufficient visual detail").trim(),
                pestFamily = json.optString("pestFamily", "").trim(),
                visibleCharacteristics = json.optString("visibleCharacteristics", "").trim(),
                feedingDamage = json.optString("feedingDamage", "").trim(),
                pestSeverity = json.optString("pestSeverity", "Not determinable").trim(),
                recommendedManagement = jsonStringList(json, "recommendedManagement"),
                organicPestControlMethods = jsonStringList(json, "organicPestControlMethods"),
                prevention = jsonStringList(json, "prevention"),
                uncertaintyNote = json.optString("uncertaintyNote", "").trim(),
                imageThumbnailBase64 = thumbnailBase64,
                timestampEpochMs = System.currentTimeMillis()
            )
        }

        internal fun parseSoilGuidanceJson(rawText: String, input: SoilGuidanceInput): SoilGuidanceRecord {
            val json = JSONObject(cleanJsonText(rawText))
            return SoilGuidanceRecord(
                soilType = input.soilType,
                cropName = input.cropName,
                irrigationFrequency = input.irrigationFrequency,
                wateringPractices = input.wateringPractices,
                soilObservations = input.soilObservations,
                fertilizerPractices = input.fertilizerPractices,
                organicMatterInfo = input.organicMatterInfo,
                soilImprovementRecommendations = jsonStringList(json, "soilImprovementRecommendations"),
                irrigationGuidance = json.optString("irrigationGuidance", "").trim(),
                compostRecommendations = json.optString("compostRecommendations", "").trim(),
                vermicompostGuidance = json.optString("vermicompostGuidance", "").trim(),
                neemCakeGuidance = json.optString("neemCakeGuidance", "").trim(),
                soilConservation = jsonStringList(json, "soilConservation"),
                sustainableFarmingPractices = jsonStringList(json, "sustainableFarmingPractices"),
                timestampEpochMs = System.currentTimeMillis()
            )
        }

        internal fun parseAssistantJson(rawText: String): AssistantReplyResult {
            val cleaned = cleanJsonText(rawText)
            return try {
                val json = JSONObject(cleaned)
                val reply = json.optString("reply", "").trim().ifBlank { cleaned }
                val lang = json.optString("detectedLanguage", "Auto").trim().ifBlank { "Auto" }
                AssistantReplyResult(text = reply, detectedLanguage = lang)
            } catch (_: Exception) {
                AssistantReplyResult(text = cleaned, detectedLanguage = "Auto")
            }
        }
    }

    suspend fun identifyPlant(bitmap: Bitmap): Result<PlantIdRecord> = withContext(Dispatchers.IO) {
        val imageBase64 = encodeBitmapToBase64(bitmap)
        val thumbBase64 = encodeThumbnailBase64(bitmap)
        val cropsList = OrganicKnowledgeBase.supportedCrops.joinToString(", ")
        val prompt = """
You are BIO LEAF AI, a botanical plant identification specialist.
Examine ONLY the provided photograph carefully. Priority crops include: $cropsList, as well as other agricultural plants.
Strict rules:
- Do NOT include any confidence percentage or numeric score.
- Do NOT claim certainty if lighting, blur, angle, or foliage visibility is insufficient; state limitations clearly in "uncertaintyNote".
- Do NOT invent sensor measurements.
- Provide scientificName only when reliably supported by visual botanical traits; otherwise leave "" or note uncertainty.

Respond with a single raw JSON object (no markdown fences) with these exact keys:
{
  "commonName": "Common plant name or 'Unclear / Unverified Plant'",
  "scientificName": "Binomial scientific name when supported, or ''",
  "plantDescription": "Clear 2-3 sentence botanical overview of the plant.",
  "identifyingCharacteristics": ["Trait 1 visible in photo", "Trait 2", "Trait 3"],
  "identificationExplanation": "Detailed visual reasoning linking leaf shape, venation, stem, or fruiting structures in the photo to this identification.",
  "uncertaintyNote": "Honest note if the image is blurry, distant, cropped, or shows overlapping species, or '' if clear."
}
""".trimIndent()

        invokeGemini(prompt = prompt, imageBase64 = imageBase64, expectJson = true)
            .mapCatching { parsePlantIdJson(it, thumbBase64) }
    }

    suspend fun analyzeDisease(bitmap: Bitmap): Result<DiseaseReportRecord> = withContext(Dispatchers.IO) {
        val imageBase64 = encodeBitmapToBase64(bitmap)
        val thumbBase64 = encodeThumbnailBase64(bitmap)
        val prompt = """
You are BIO LEAF AI, an agricultural plant pathology vision assistant.
Examine the uploaded leaf/plant photograph for fungal diseases, bacterial infections, viral symptoms, nutrient deficiencies, leaf spots, yellowing, wilting, discoloration, physical damage, or healthy foliage.

Strict rules:
- NEVER include any confidence score or percentage.
- NEVER invent sensor readings, soil moisture %, pH, NPK, or laboratory numbers.
- Use the exact JSON field "problem" for the main finding (never label it "Possible Problem").
- Distinguish clearly between visual image observation and confirmed laboratory diagnosis in "visualObservationNote".
- If foliage appears healthy, state "Healthy Foliage (No Visible Disease)" in "problem" and "None / Healthy" in "severity".
- Include practical organic options (such as neem oil, neem seed extract, Panchagavya, Trichoderma, sanitation, or compost where appropriate).

Respond with a single raw JSON object (no markdown fences) with these exact keys:
{
  "plantName": "Identified crop/plant name or 'Unidentified Plant'",
  "problem": "Concise name of the problem or 'Healthy Foliage'",
  "severity": "Mild | Moderate | Severe | None / Healthy | Not determinable",
  "visibleSymptoms": "Exact visual symptoms seen on leaves, stems, lesions, chlorosis, or margins.",
  "possibleCauses": "Likely biological or environmental causes behind those visible symptoms.",
  "detailedExplanation": "Clear agronomic explanation connecting visible symptoms to the problem and how it spreads or develops.",
  "recommendedTreatment": ["Step 1", "Step 2", "Step 3"],
  "organicTreatment": ["Organic preparation/method 1", "Organic method 2"],
  "preventionMethods": ["Cultural/hygiene practice 1", "Practice 2", "Practice 3"],
  "visualObservationNote": "Explicit clarification that this assessment is based solely on visual inspection of the uploaded photograph, not laboratory testing."
}
""".trimIndent()

        invokeGemini(prompt = prompt, imageBase64 = imageBase64, expectJson = true)
            .mapCatching { parseDiseaseReportJson(it, thumbBase64) }
    }

    suspend fun identifyPest(bitmap: Bitmap): Result<PestReportRecord> = withContext(Dispatchers.IO) {
        val imageBase64 = encodeBitmapToBase64(bitmap)
        val thumbBase64 = encodeThumbnailBase64(bitmap)
        val prompt = """
You are BIO LEAF AI, an agricultural entomology and integrated pest management specialist.
Examine the uploaded photograph for insects, larvae, eggs, webbing, mites, borers, or characteristic pest feeding damage on leaves, stems, or fruits.

Strict rules:
- Do NOT invent a pest species if the image is blurry or lacks diagnostic features; state clearly what is visible and explain the gap in "uncertaintyNote".
- Do NOT include confidence percentages or invented hardware/sensor numbers.
- Emphasize organic and IPM pest control (neem oil, NSKE 5%, garlic-chilli spray, sticky traps, beneficial predators).

Respond with a single raw JSON object (no markdown fences) with these exact keys:
{
  "possiblePestIdentification": "Best supported pest name/group, or 'Insufficient visual detail for species-level ID'",
  "pestFamily": "Taxonomic family/order if visually supported, or ''",
  "visibleCharacteristics": "Morphology, insect/larvae/webbing/egg traits or absence of visible insect in the photo.",
  "feedingDamage": "Chewing holes, sap-sucking stippling, leaf curling, mining, honeydew/sooty mold, or boring pattern seen.",
  "pestSeverity": "Low | Moderate | High | Not determinable",
  "recommendedManagement": ["IPM step 1", "Step 2", "Step 3"],
  "organicPestControlMethods": ["Organic control 1 (e.g. Neem oil / Garlic-chilli)", "Organic control 2"],
  "prevention": ["Prevention practice 1", "Prevention practice 2"],
  "uncertaintyNote": "Clarification of image limits or confirmation steps (e.g., inspecting leaf undersides with hand lens)."
}
""".trimIndent()

        invokeGemini(prompt = prompt, imageBase64 = imageBase64, expectJson = true)
            .mapCatching { parsePestReportJson(it, thumbBase64) }
    }

    suspend fun generateSoilGuidance(input: SoilGuidanceInput): Result<SoilGuidanceRecord> = withContext(Dispatchers.IO) {
        val prompt = """
You are BIO LEAF AI, a sustainable soil agronomy and organic farming advisor.
Generate practical, software-based soil management guidance using ONLY the farmer's qualitative inputs below.

Farmer Inputs:
- Soil Type: ${input.soilType}
- Crop Name: ${input.cropName}
- Irrigation Frequency: ${input.irrigationFrequency.ifBlank { "Not specified" }}
- Watering Practices: ${input.wateringPractices.ifBlank { "Not specified" }}
- Visual Soil Observations: ${input.soilObservations.ifBlank { "Not specified" }}
- Current Fertilizer Practices: ${input.fertilizerPractices.ifBlank { "Not specified" }}
- Organic Matter / Compost Info: ${input.organicMatterInfo.ifBlank { "Not specified" }}

STRICT SOFTWARE-ONLY CONSTRAINT:
- NEVER invent or fabricate soil moisture percentages, soil pH numbers, EC values, temperature, humidity, or NPK sensor numbers.
- Base guidance strictly on qualitative soil texture, visual soil conditions, crop water/organic-matter needs, and sustainable agronomy.

Respond with a single raw JSON object (no markdown fences) with these exact keys:
{
  "soilImprovementRecommendations": ["Recommendation 1", "Recommendation 2", "Recommendation 3"],
  "irrigationGuidance": "Practical watering schedule & method advice tailored to the stated soil type and crop without inventing sensor numbers.",
  "compostRecommendations": "Actionable guidance on well-decomposed compost timing and incorporation.",
  "vermicompostGuidance": "Guidance on vermicompost usage for root vigor and microbial life.",
  "neemCakeGuidance": "Guidance on neem cake incorporation for slow-release organic nutrition and soil pest suppression.",
  "soilConservation": ["Conservation method 1", "Conservation method 2"],
  "sustainableFarmingPractices": ["Sustainable practice 1", "Sustainable practice 2", "Sustainable practice 3"]
}
""".trimIndent()

        invokeGemini(prompt = prompt, imageBase64 = null, expectJson = true)
            .mapCatching { parseSoilGuidanceJson(it, input) }
    }

    suspend fun askAssistant(
        userMessage: String,
        recentHistory: List<BioLeafChatMessage>,
        attachmentBitmap: Bitmap? = null
    ): Result<AssistantReplyResult> = withContext(Dispatchers.IO) {
        val imageBase64 = attachmentBitmap?.let { encodeBitmapToBase64(it) }
        val historyContext = recentHistory.takeLast(8).joinToString("\n") { msg ->
            val role = if (msg.sender == "assistant") "BIO LEAF AI" else "Farmer"
            "$role: ${msg.text}"
        }
        val prompt = """
You are BIO LEAF AI, an intelligent, sustainable agricultural assistant.
Rules:
1. Automatically detect the language of the farmer's latest message (English, Tamil, Telugu, Malayalam, Hindi, or any other language) and write your ENTIRE reply in that exact same language.
2. Focus strictly on plant health, crop care, organic farming (neem oil, Panchagavya, Jeevamrutham, compost, vermicompost, neem cake, garlic-chilli spray), soil management without hardware sensors, and pest/disease guidance.
3. NEVER invent physical sensor readings, soil moisture %, pH, NPK, or confidence scores.
4. If an image is attached, describe only what you genuinely see and note visual uncertainty honestly.

Recent conversation:
$historyContext

Latest Farmer Message:
$userMessage

Respond with a single raw JSON object (no markdown fences):
{
  "detectedLanguage": "Name of detected language in English (e.g., English, Tamil, Telugu, Malayalam, Hindi)",
  "reply": "Helpful, well-structured response written entirely in the farmer's language."
}
""".trimIndent()

        invokeGemini(prompt = prompt, imageBase64 = imageBase64, expectJson = true)
            .mapCatching { parseAssistantJson(it) }
    }

    suspend fun generateCommunityInsight(
        title: String,
        content: String,
        cropTag: String,
        imageBase64: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val prompt = """
You are BIO LEAF AI contributing a concise, helpful peer insight to a farmer's community post.
Crop Tag: ${cropTag.ifBlank { "General" }}
Post Title: $title
Post Details: $content

Provide a practical, respectful 3-5 sentence agricultural comment highlighting safe visual observations, organic management options (like neem-based care or cultural hygiene), and a reminder to seek local agronomy lab confirmation for severe outbreaks. Do NOT invent sensor measurements or confidence scores. Return plain text only.
""".trimIndent()

        invokeGemini(prompt = prompt, imageBase64 = imageBase64, expectJson = false)
            .map { it.trim() }
    }

    private fun invokeGemini(
        prompt: String,
        imageBase64: String?,
        expectJson: Boolean
    ): Result<String> {
        val apiKey = apiKeyProvider().trim()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return Result.failure(
                IllegalStateException(
                    "Gemini API key is not configured. Add GEMINI_API_KEY in AI Studio Secrets to run real AI analysis."
                )
            )
        }

        val primaryResult = executeModelCall(PRIMARY_MODEL, apiKey, prompt, imageBase64, expectJson)
        val exception = primaryResult.exceptionOrNull()
        if (primaryResult.isFailure && exception is ModelNotFoundException) {
            return executeModelCall(FALLBACK_MODEL, apiKey, prompt, imageBase64, expectJson)
        }
        return primaryResult
    }

    private fun executeModelCall(
        modelName: String,
        apiKey: String,
        prompt: String,
        imageBase64: String?,
        expectJson: Boolean
    ): Result<String> {
        return try {
            val parts = JSONArray().apply {
                put(JSONObject().put("text", prompt))
                if (!imageBase64.isNullOrBlank()) {
                    put(
                        JSONObject().put(
                            "inlineData",
                            JSONObject()
                                .put("mimeType", "image/jpeg")
                                .put("data", imageBase64)
                        )
                    )
                }
            }

            val genConfig = JSONObject().apply {
                put("temperature", 0.25)
                if (expectJson) {
                    put("responseMimeType", "application/json")
                }
            }

            val bodyJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().put("parts", parts)))
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url("$API_BASE/$modelName:generateContent?key=$apiKey")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    if (response.code == 404) {
                        return Result.failure(ModelNotFoundException("Model $modelName unavailable (HTTP 404)."))
                    }
                    val apiMessage = try {
                        JSONObject(rawBody).optJSONObject("error")?.optString("message").orEmpty()
                    } catch (_: Exception) {
                        ""
                    }
                    val detail = apiMessage.ifBlank { "Gemini API request failed (HTTP ${response.code})." }
                    return Result.failure(IllegalStateException(detail))
                }

                val root = JSONObject(rawBody)
                val text = root.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.trim()

                if (text.isNullOrBlank()) {
                    Result.failure(IllegalStateException("AI returned an empty response. Please try again with a clearer input."))
                } else {
                    Result.success(text)
                }
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(
                    e.localizedMessage ?: "Unable to connect to Gemini AI service. Check network connectivity."
                )
            )
        }
    }

    private class ModelNotFoundException(message: String) : Exception(message)
}

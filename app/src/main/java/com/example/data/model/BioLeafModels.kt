package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

enum class BioLeafSection(val title: String, val subtitle: String) {
    HOME("Home", "Bio Leaf AI Overview"),
    PLANT_ID("Plant Identification", "Identify crops & botanical traits"),
    DISEASE_SCANNER("Disease Scanner", "Leaf symptom & pathology report"),
    PEST_SCANNER("Pest Scanner", "Insect, egg & damage analysis"),
    SOIL_GUIDANCE("Soil Guidance", "Software-based soil management"),
    ORGANIC_KNOWLEDGE("Organic Knowledge", "Bio-preparations & crop safety"),
    AI_ASSISTANT("AI Assistant", "Multilingual agricultural chat"),
    COMMUNITY("Community", "Farmer discussions & crop posts"),
    HISTORY_REPORTS("History & Reports", "Saved analyses & report exports"),
    PROFILE("Profile & Settings", "Account & Bio Leaf preferences")
}

@Keep
@IgnoreExtraProperties
data class PlantIdRecord(
    val id: String = "",
    val commonName: String = "",
    val scientificName: String = "",
    val plantDescription: String = "",
    val identifyingCharacteristics: List<String> = emptyList(),
    val identificationExplanation: String = "",
    val uncertaintyNote: String = "",
    val imageThumbnailBase64: String? = null,
    val timestamp: Timestamp? = null,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

@Keep
@IgnoreExtraProperties
data class DiseaseReportRecord(
    val id: String = "",
    val plantName: String = "",
    val problem: String = "",
    val severity: String = "",
    val visibleSymptoms: String = "",
    val possibleCauses: String = "",
    val detailedExplanation: String = "",
    val recommendedTreatment: List<String> = emptyList(),
    val organicTreatment: List<String> = emptyList(),
    val preventionMethods: List<String> = emptyList(),
    val visualObservationNote: String = "",
    val imageThumbnailBase64: String? = null,
    val timestamp: Timestamp? = null,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

@Keep
@IgnoreExtraProperties
data class PestReportRecord(
    val id: String = "",
    val possiblePestIdentification: String = "",
    val pestFamily: String = "",
    val visibleCharacteristics: String = "",
    val feedingDamage: String = "",
    val pestSeverity: String = "",
    val recommendedManagement: List<String> = emptyList(),
    val organicPestControlMethods: List<String> = emptyList(),
    val prevention: List<String> = emptyList(),
    val uncertaintyNote: String = "",
    val imageThumbnailBase64: String? = null,
    val timestamp: Timestamp? = null,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

data class SoilGuidanceInput(
    val soilType: String = "",
    val cropName: String = "",
    val irrigationFrequency: String = "",
    val wateringPractices: String = "",
    val soilObservations: String = "",
    val fertilizerPractices: String = "",
    val organicMatterInfo: String = ""
)

@Keep
@IgnoreExtraProperties
data class SoilGuidanceRecord(
    val id: String = "",
    val soilType: String = "",
    val cropName: String = "",
    val irrigationFrequency: String = "",
    val wateringPractices: String = "",
    val soilObservations: String = "",
    val fertilizerPractices: String = "",
    val organicMatterInfo: String = "",
    val soilImprovementRecommendations: List<String> = emptyList(),
    val irrigationGuidance: String = "",
    val compostRecommendations: String = "",
    val vermicompostGuidance: String = "",
    val neemCakeGuidance: String = "",
    val soilConservation: List<String> = emptyList(),
    val sustainableFarmingPractices: List<String> = emptyList(),
    val timestamp: Timestamp? = null,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

@Keep
@IgnoreExtraProperties
data class BioLeafChatMessage(
    val id: String = "",
    val sender: String = "user", // "user" or "assistant"
    val text: String = "",
    val detectedLanguage: String = "Auto",
    val imageThumbnailBase64: String? = null,
    val timestamp: Timestamp? = null,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

@Keep
@IgnoreExtraProperties
data class CommunityPost(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val cropTag: String = "",
    val title: String = "",
    val content: String = "",
    val imageThumbnailBase64: String? = null,
    val commentCount: Int = 0,
    val timestamp: Timestamp? = null,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

@Keep
@IgnoreExtraProperties
data class CommunityComment(
    val id: String = "",
    val postId: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val text: String = "",
    val isAiAssisted: Boolean = false,
    val timestamp: Timestamp? = null,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

data class OrganicKnowledgeItem(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val preparationAndUsage: List<String>,
    val targetCrops: List<String>,
    val targetIssues: List<String>,
    val safetyPrecautions: List<String>
)

enum class BioLeafReportCategory(val label: String) {
    PLANT_ID("Plant ID"),
    DISEASE("Disease"),
    PEST("Pest"),
    SOIL("Soil Guidance")
}

data class BioLeafSavedReportSummary(
    val id: String,
    val category: BioLeafReportCategory,
    val title: String,
    val subtitle: String,
    val timestampEpochMs: Long,
    val imageThumbnailBase64: String? = null,
    val exportText: String
)

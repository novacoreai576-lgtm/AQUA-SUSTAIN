package com.example

import com.example.data.knowledge.OrganicKnowledgeBase
import com.example.data.model.BioLeafReportCategory
import com.example.data.model.BioLeafReportFormatter
import com.example.data.model.DiseaseReportRecord
import com.example.data.model.PestReportRecord
import com.example.data.model.PlantIdRecord
import com.example.data.model.SoilGuidanceInput
import com.example.data.model.SoilGuidanceRecord
import com.example.data.remote.BioLeafGeminiService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BioLeafLogicTest {

    @Test
    fun organicKnowledgeBase_containsAll12CropsAnd10OrganicTopics() {
        assertEquals(12, OrganicKnowledgeBase.supportedCrops.size)
        val expectedCrops = listOf(
            "Tomato", "Rice", "Maize", "Groundnut", "Brinjal", "Chilli",
            "Cotton", "Banana", "Mango", "Coconut", "Sugarcane", "Neem"
        )
        assertTrue(OrganicKnowledgeBase.supportedCrops.containsAll(expectedCrops))

        assertEquals(10, OrganicKnowledgeBase.knowledgeItems.size)
        val expectedIds = listOf(
            "neem_oil", "neem_preparations", "garlic_chilli", "panchagavya",
            "jeevamrutham", "compost", "vermicompost", "neem_cake",
            "natural_pest_management", "sustainable_crop_protection"
        )
        assertTrue(OrganicKnowledgeBase.knowledgeItems.map { it.id }.containsAll(expectedIds))

        val filtered = OrganicKnowledgeBase.searchKnowledge("Panchagavya")
        assertEquals(1, filtered.size)
        assertEquals("panchagavya", filtered.first().id)
    }

    @Test
    fun geminiService_neverMocksWhenApiKeyMissingOrPlaceholder() = runTest {
        val soilInput = SoilGuidanceInput(soilType = "Red Loam", cropName = "Chilli")

        val placeholderService = BioLeafGeminiService(apiKeyProvider = { "MY_GEMINI_API_KEY" })
        val placeholderResult = placeholderService.generateSoilGuidance(soilInput)
        assertTrue(placeholderResult.isFailure)
        assertTrue(
            placeholderResult.exceptionOrNull()?.message?.contains("Gemini API key", ignoreCase = true) == true
        )

        val emptyKeyService = BioLeafGeminiService(apiKeyProvider = { "" })
        val chatResult = emptyKeyService.askAssistant("Help with tomato leaves", emptyList())
        assertTrue(chatResult.isFailure)
        assertTrue(
            chatResult.exceptionOrNull()?.message?.contains("Gemini API key", ignoreCase = true) == true
        )
    }

    @Test
    fun reportFormatter_usesProblemHeadingAndCombinesReportsInOrder() {
        val disease = DiseaseReportRecord(
            id = "d1",
            plantName = "Brinjal",
            problem = "Leaf Spot",
            severity = "Mild",
            visibleSymptoms = "Small dark circular lesions.",
            possibleCauses = "Fungal splash.",
            detailedExplanation = "Lesions coalesce under humid foliage.",
            recommendedTreatment = listOf("Remove spotted leaves"),
            organicTreatment = listOf("Neem oil 3 ml/L"),
            preventionMethods = listOf("Crop rotation"),
            timestampEpochMs = 2000L
        )
        val formatted = BioLeafReportFormatter.formatDiseaseReport(disease)
        assertTrue(formatted.contains("2. Problem: Leaf Spot"))
        assertFalse(formatted.contains("Possible Problem"))

        val plant = PlantIdRecord(id = "p1", commonName = "Rice", timestampEpochMs = 1000L)
        val pest = PestReportRecord(id = "pe1", possiblePestIdentification = "Whitefly", timestampEpochMs = 3000L)
        val soil = SoilGuidanceRecord(id = "s1", soilType = "Alluvial", cropName = "Banana", timestampEpochMs = 1500L)

        val unified = BioLeafReportFormatter.buildUnifiedReports(
            plants = listOf(plant),
            diseases = listOf(disease),
            pests = listOf(pest),
            soils = listOf(soil)
        )
        assertEquals(4, unified.size)
        assertEquals(BioLeafReportCategory.PEST, unified[0].category)
        assertEquals(BioLeafReportCategory.DISEASE, unified[1].category)
        assertEquals(BioLeafReportCategory.SOIL, unified[2].category)
        assertEquals(BioLeafReportCategory.PLANT_ID, unified[3].category)
    }
}

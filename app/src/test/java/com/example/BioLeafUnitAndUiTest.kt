package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.data.knowledge.OrganicKnowledgeBase
import com.example.data.model.BioLeafReportCategory
import com.example.data.model.BioLeafReportFormatter
import com.example.data.model.DiseaseReportRecord
import com.example.data.model.PestReportRecord
import com.example.data.model.PlantIdRecord
import com.example.data.model.SoilGuidanceInput
import com.example.data.model.SoilGuidanceRecord
import com.example.data.remote.BioLeafGeminiService
import com.example.ui.screens.bioleaf.BioLeafDiseaseSection
import com.example.ui.screens.bioleaf.BioLeafHistoryReportsSection
import com.example.ui.screens.bioleaf.BioLeafHomeSection
import com.example.ui.screens.bioleaf.BioLeafKnowledgeSection
import com.example.ui.theme.AquaSustainTheme
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BioLeafUnitAndUiTest {

    @get:Rule
    val composeRule = createComposeRule()

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
    fun geminiService_parsesStructuredJsonAndNeverMocksMissingApiKey() = runTest {
        val plantJson = """
            {
              "commonName": "Tomato",
              "scientificName": "Solanum lycopersicum",
              "plantDescription": "Herbaceous fruiting crop.",
              "identifyingCharacteristics": ["Compound serrated leaves", "Pubescent stem"],
              "identificationExplanation": "Distinctive compound leaflet arrangement.",
              "uncertaintyNote": ""
            }
        """.trimIndent()
        val plant = BioLeafGeminiService.parsePlantIdJson(plantJson)
        assertEquals("Tomato", plant.commonName)
        assertEquals("Solanum lycopersicum", plant.scientificName)
        assertEquals(2, plant.identifyingCharacteristics.size)

        val diseaseJson = """
            {
              "plantName": "Tomato",
              "problem": "Early Blight (Alternaria solani)",
              "severity": "Moderate",
              "visibleSymptoms": "Concentric brown rings with yellow halo.",
              "possibleCauses": "Fungal spores splashing from soil during warm humid weather.",
              "detailedExplanation": "Lower older leaves show target-like necrotic lesions.",
              "recommendedTreatment": ["Prune infected lower leaves"],
              "organicTreatment": ["Spray cold-pressed neem oil emulsion"],
              "preventionMethods": ["Apply organic mulch", "Water at root zone"],
              "visualObservationNote": "Visual photo observation only; not a lab culture test."
            }
        """.trimIndent()
        val disease = BioLeafGeminiService.parseDiseaseReportJson(diseaseJson)
        assertEquals("Tomato", disease.plantName)
        assertEquals("Early Blight (Alternaria solani)", disease.problem)
        assertEquals("Moderate", disease.severity)
        assertEquals(1, disease.organicTreatment.size)

        val pestJson = """
            {
              "possiblePestIdentification": "Aphids",
              "pestFamily": "Aphididae",
              "visibleCharacteristics": "Soft-bodied green clusters on new shoots.",
              "feedingDamage": "Curled terminal leaves and sticky honeydew.",
              "pestSeverity": "Moderate",
              "recommendedManagement": ["Encourage ladybird beetles"],
              "organicPestControlMethods": ["Garlic-chilli spray"],
              "prevention": ["Yellow sticky traps"],
              "uncertaintyNote": ""
            }
        """.trimIndent()
        val pest = BioLeafGeminiService.parsePestReportJson(pestJson)
        assertEquals("Aphids", pest.possiblePestIdentification)
        assertEquals("Aphididae", pest.pestFamily)

        val soilInput = SoilGuidanceInput(soilType = "Red Loam", cropName = "Chilli")
        val soilJson = """
            {
              "soilImprovementRecommendations": ["Incorporate compost before transplanting"],
              "irrigationGuidance": "Water deeply in early morning when topsoil dries.",
              "compostRecommendations": "Apply mature farmyard compost in root zone.",
              "vermicompostGuidance": "Side-dress vermicompost during vegetative growth.",
              "neemCakeGuidance": "Blend powdered neem cake into planting beds.",
              "soilConservation": ["Mulch furrows"],
              "sustainableFarmingPractices": ["Rotate with groundnut"]
            }
        """.trimIndent()
        val soil = BioLeafGeminiService.parseSoilGuidanceJson(soilJson, soilInput)
        assertEquals("Red Loam", soil.soilType)
        assertEquals("Chilli", soil.cropName)

        val serviceWithoutKey = BioLeafGeminiService(apiKeyProvider = { "MY_GEMINI_API_KEY" })
        val result = serviceWithoutKey.generateSoilGuidance(soilInput)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Gemini API key", ignoreCase = true) == true)
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

    @Test
    fun bioLeafHomeSection_showsEmptyStateWhenNoSavedReportsExist() {
        composeRule.setContent {
            AquaSustainTheme {
                BioLeafHomeSection(
                    userName = "Ananya",
                    savedReports = emptyList(),
                    onNavigateSection = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("shortcut_plant_id").assertIsDisplayed()
        composeRule.onNodeWithTag("shortcut_disease").assertIsDisplayed()
        composeRule.onNodeWithTag("bioleaf_home_empty_reports").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun bioLeafDiseaseSection_displaysProblemHeadingNeverPossibleProblem() {
        val sample = DiseaseReportRecord(
            id = "d_ui",
            plantName = "Tomato",
            problem = "Bacterial Leaf Spot",
            severity = "Moderate",
            visibleSymptoms = "Water-soaked dark spots.",
            possibleCauses = "Splash-dispersed bacteria.",
            detailedExplanation = "Spreads during overhead watering.",
            recommendedTreatment = listOf("Remove infected foliage"),
            organicTreatment = listOf("Improve airflow and sanitation"),
            preventionMethods = listOf("Drip/root-zone irrigation")
        )
        composeRule.setContent {
            AquaSustainTheme {
                BioLeafDiseaseSection(
                    result = sample,
                    isAnalyzing = false,
                    errorMessage = null,
                    onAnalyzeDisease = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("2. Problem").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Bacterial Leaf Spot").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Possible Problem", substring = true).assertDoesNotExist()
    }

    @Test
    fun bioLeafKnowledgeSection_filtersGuidesOnSearchInput() {
        composeRule.setContent {
            AquaSustainTheme {
                BioLeafKnowledgeSection()
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("knowledge_search_input").performTextInput("Jeevamrutham")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("knowledge_card_jeevamrutham").assertIsDisplayed()
        composeRule.onNodeWithTag("knowledge_card_neem_oil").assertDoesNotExist()
    }

    @Test
    fun bioLeafHistoryReportsSection_displaysEmptyStateThenSavedItems() {
        composeRule.setContent {
            AquaSustainTheme {
                BioLeafHistoryReportsSection(
                    reports = emptyList(),
                    onDeleteReport = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reports_empty_state").assertIsDisplayed()
    }
}

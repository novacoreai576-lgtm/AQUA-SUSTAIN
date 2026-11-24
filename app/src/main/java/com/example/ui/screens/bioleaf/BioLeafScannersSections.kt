package com.example.ui.screens.bioleaf

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.knowledge.OrganicKnowledgeBase
import com.example.data.model.DiseaseReportRecord
import com.example.data.model.PestReportRecord
import com.example.data.model.PlantIdRecord
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.EcoOnGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BioLeafPlantIdSection(
    result: PlantIdRecord?,
    isAnalyzing: Boolean,
    errorMessage: String?,
    onAnalyzePlant: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("bioleaf_plant_id_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Plant Identification",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = NeutralDark
        )
        Text(
            text = "Upload a clear photograph of leaves, stems, flowers, or fruits to identify supported crops and botanical traits.",
            style = MaterialTheme.typography.bodySmall,
            color = NeutralMedium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OrganicKnowledgeBase.supportedCrops.forEach { crop ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EcoGreenContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = crop,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = EcoOnGreenContainer
                    )
                }
            }
        }

        BioLeafPhotoUploadCard(
            title = "Plant Photograph",
            subtitle = "Supported: Tomato, Rice, Maize, Groundnut, Brinjal, Chilli, Cotton, Banana, Mango, Coconut, Sugarcane, Neem, and more.",
            selectedBitmap = selectedBitmap,
            isAnalyzing = isAnalyzing,
            analyzeButtonLabel = "Identify Plant",
            testTagPrefix = "plant_id",
            onBitmapSelected = { selectedBitmap = it },
            onAnalyzeClick = { selectedBitmap?.let(onAnalyzePlant) }
        )

        if (errorMessage != null) {
            BioLeafErrorBanner(message = errorMessage, modifier = Modifier.testTag("plant_id_error_banner"))
        }

        if (result != null) {
            BioLeafSectionCard(title = "Common Name & Taxonomy", accentColor = EcoGreen) {
                Text(
                    text = result.commonName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = NeutralDark
                )
                if (result.scientificName.isNotBlank()) {
                    Text(
                        text = result.scientificName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = EcoOnGreenContainer
                    )
                }
            }

            BioLeafSectionCard(title = "Plant Description", accentColor = NeutralDark) {
                Text(
                    text = result.plantDescription.ifBlank { "No description returned." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralDark
                )
            }

            BioLeafSectionCard(title = "Visible Identifying Characteristics", accentColor = EcoGreen) {
                BioLeafBulletList(
                    items = result.identifyingCharacteristics,
                    emptyText = "No specific visual traits isolated."
                )
            }

            BioLeafSectionCard(title = "Identification Explanation", accentColor = NeutralDark) {
                Text(
                    text = result.identificationExplanation.ifBlank { "No visual reasoning returned." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralDark
                )
            }

            if (result.uncertaintyNote.isNotBlank()) {
                BioLeafSectionCard(title = "Visual Uncertainty Note", accentColor = WarningAmber) {
                    Text(
                        text = result.uncertaintyNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun BioLeafDiseaseSection(
    result: DiseaseReportRecord?,
    isAnalyzing: Boolean,
    errorMessage: String?,
    onAnalyzeDisease: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("bioleaf_disease_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Plant Disease Scanner",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = NeutralDark
        )
        Text(
            text = "Inspect leaf spots, fungal/bacterial blight, yellowing, wilting, nutrient symptoms, physical damage, or healthy foliage.",
            style = MaterialTheme.typography.bodySmall,
            color = NeutralMedium
        )

        BioLeafPhotoUploadCard(
            title = "Leaf or Plant Symptom Photo",
            subtitle = "Focus on affected leaves in natural daylight for an accurate visual pathology breakdown.",
            selectedBitmap = selectedBitmap,
            isAnalyzing = isAnalyzing,
            analyzeButtonLabel = "Analyze Plant Disease",
            testTagPrefix = "disease_scanner",
            onBitmapSelected = { selectedBitmap = it },
            onAnalyzeClick = { selectedBitmap?.let(onAnalyzeDisease) }
        )

        if (errorMessage != null) {
            BioLeafErrorBanner(message = errorMessage, modifier = Modifier.testTag("disease_error_banner"))
        }

        if (result != null) {
            BioLeafSectionCard(title = "1. Plant Name", accentColor = EcoGreen) {
                Text(result.plantName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeutralDark)
            }

            BioLeafSectionCard(title = "2. Problem", accentColor = NeutralDark) {
                Text(result.problem, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeutralDark)
            }

            BioLeafSectionCard(title = "3. Severity", accentColor = WarningAmber) {
                Text(result.severity, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = NeutralDark)
            }

            BioLeafSectionCard(title = "4. Visible Symptoms", accentColor = EcoGreen) {
                Text(result.visibleSymptoms, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
            }

            BioLeafSectionCard(title = "5. Possible Causes", accentColor = NeutralDark) {
                Text(result.possibleCauses, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
            }

            BioLeafSectionCard(title = "6. Detailed Explanation", accentColor = NeutralDark) {
                Text(result.detailedExplanation, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
            }

            BioLeafSectionCard(title = "7. Recommended Treatment", accentColor = EcoGreen) {
                BioLeafBulletList(result.recommendedTreatment)
            }

            BioLeafSectionCard(title = "8. Organic Treatment", accentColor = EcoOnGreenContainer) {
                BioLeafBulletList(result.organicTreatment)
            }

            BioLeafSectionCard(title = "9. Prevention Methods", accentColor = NeutralDark) {
                BioLeafBulletList(result.preventionMethods)
            }

            if (result.visualObservationNote.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = result.visualObservationNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun BioLeafPestSection(
    result: PestReportRecord?,
    isAnalyzing: Boolean,
    errorMessage: String?,
    onAnalyzePest: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("bioleaf_pest_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Pest Identification Scanner",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = NeutralDark
        )
        Text(
            text = "Analyze insects, larvae, egg clusters, webbing, mites, and leaf/stem/fruit feeding damage.",
            style = MaterialTheme.typography.bodySmall,
            color = NeutralMedium
        )

        BioLeafPhotoUploadCard(
            title = "Pest or Feeding Damage Photo",
            subtitle = "If the insect is too small or unclear, Bio Leaf AI honestly explains image limitations instead of guessing.",
            selectedBitmap = selectedBitmap,
            isAnalyzing = isAnalyzing,
            analyzeButtonLabel = "Identify Pest & Damage",
            testTagPrefix = "pest_scanner",
            onBitmapSelected = { selectedBitmap = it },
            onAnalyzeClick = { selectedBitmap?.let(onAnalyzePest) }
        )

        if (errorMessage != null) {
            BioLeafErrorBanner(message = errorMessage, modifier = Modifier.testTag("pest_error_banner"))
        }

        if (result != null) {
            BioLeafSectionCard(title = "Possible Pest Identification", accentColor = EcoGreen) {
                Text(
                    text = result.possiblePestIdentification,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
                if (result.pestFamily.isNotBlank()) {
                    Text(
                        text = "Pest Family: ${result.pestFamily}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EcoOnGreenContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "Pest Severity: ${result.pestSeverity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarningAmber,
                    fontWeight = FontWeight.Bold
                )
            }

            BioLeafSectionCard(title = "Visible Characteristics", accentColor = NeutralDark) {
                Text(result.visibleCharacteristics, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
            }

            BioLeafSectionCard(title = "Feeding Damage", accentColor = NeutralDark) {
                Text(result.feedingDamage, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
            }

            BioLeafSectionCard(title = "Recommended Management", accentColor = EcoGreen) {
                BioLeafBulletList(result.recommendedManagement)
            }

            BioLeafSectionCard(title = "Organic Pest-Control Methods", accentColor = EcoOnGreenContainer) {
                BioLeafBulletList(result.organicPestControlMethods)
            }

            BioLeafSectionCard(title = "Prevention", accentColor = NeutralDark) {
                BioLeafBulletList(result.prevention)
            }

            if (result.uncertaintyNote.isNotBlank()) {
                BioLeafSectionCard(title = "Observation & Image Limits", accentColor = WarningAmber) {
                    Text(result.uncertaintyNote, style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

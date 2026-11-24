package com.example.ui.screens.bioleaf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.knowledge.OrganicKnowledgeBase
import com.example.data.model.OrganicKnowledgeItem
import com.example.data.model.SoilGuidanceInput
import com.example.data.model.SoilGuidanceRecord
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.EcoOnGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BioLeafSoilGuidanceSection(
    result: SoilGuidanceRecord?,
    isGenerating: Boolean,
    errorMessage: String?,
    onGenerateGuidance: (SoilGuidanceInput) -> Unit,
    modifier: Modifier = Modifier
) {
    val soilTypes = listOf("Red Loam", "Black Clay", "Sandy Loam", "Alluvial", "Laterite")
    var soilType by remember { mutableStateOf("") }
    var cropName by remember { mutableStateOf("") }
    var irrigationFrequency by remember { mutableStateOf("") }
    var wateringPractices by remember { mutableStateOf("") }
    var soilObservations by remember { mutableStateOf("") }
    var fertilizerPractices by remember { mutableStateOf("") }
    var organicMatterInfo by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("bioleaf_soil_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Soil Condition Guidance",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = NeutralDark
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(EcoGreenContainer)
                .padding(12.dp)
        ) {
            Text(
                text = "100% Software-Based Agronomic Guidance • Based purely on your qualitative farm inputs without physical sensors or invented soil measurements.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = EcoOnGreenContainer
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Describe Your Soil & Crop Conditions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    soilTypes.forEach { preset ->
                        FilterChip(
                            selected = soilType.equals(preset, ignoreCase = true),
                            onClick = { soilType = preset },
                            label = { Text(preset) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EcoGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = soilType,
                    onValueChange = { soilType = it },
                    label = { Text("Soil Type * (e.g., Red Loam, Clay, Sandy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("soil_type_input")
                )
                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    label = { Text("Crop Name * (e.g., Tomato, Rice, Banana)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("soil_crop_input")
                )
                OutlinedTextField(
                    value = irrigationFrequency,
                    onValueChange = { irrigationFrequency = it },
                    label = { Text("Irrigation Frequency (e.g., Every 3 days)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = wateringPractices,
                    onValueChange = { wateringPractices = it },
                    label = { Text("Watering Practices (Drip, furrow, rainfed, basin)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = soilObservations,
                    onValueChange = { soilObservations = it },
                    label = { Text("Visual Soil Observations (Crusting, waterlogging, hardpan)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fertilizerPractices,
                    onValueChange = { fertilizerPractices = it },
                    label = { Text("Current Fertilizer Practices") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = organicMatterInfo,
                    onValueChange = { organicMatterInfo = it },
                    label = { Text("Organic Matter / Mulch / Compost Availability") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onGenerateGuidance(
                            SoilGuidanceInput(
                                soilType = soilType.trim(),
                                cropName = cropName.trim(),
                                irrigationFrequency = irrigationFrequency.trim(),
                                wateringPractices = wateringPractices.trim(),
                                soilObservations = soilObservations.trim(),
                                fertilizerPractices = fertilizerPractices.trim(),
                                organicMatterInfo = organicMatterInfo.trim()
                            )
                        )
                    },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("soil_generate_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating Guidance...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Generate Sustainable Soil Plan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (errorMessage != null) {
            BioLeafErrorBanner(message = errorMessage, modifier = Modifier.testTag("soil_error_banner"))
        }

        if (result != null) {
            BioLeafSectionCard(title = "Soil Improvement Recommendations", accentColor = EcoGreen) {
                BioLeafBulletList(result.soilImprovementRecommendations)
            }
            BioLeafSectionCard(title = "Irrigation Guidance", accentColor = NeutralDark) {
                Text(result.irrigationGuidance, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
            }
            BioLeafSectionCard(title = "Compost Recommendations", accentColor = EcoOnGreenContainer) {
                Text(result.compostRecommendations, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
            }
            BioLeafSectionCard(title = "Vermicompost Guidance", accentColor = EcoGreen) {
                Text(result.vermicompostGuidance, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
            }
            BioLeafSectionCard(title = "Neem Cake Guidance", accentColor = NeutralDark) {
                Text(result.neemCakeGuidance, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
            }
            BioLeafSectionCard(title = "Soil Conservation", accentColor = EcoGreen) {
                BioLeafBulletList(result.soilConservation)
            }
            BioLeafSectionCard(title = "Sustainable Farming Practices", accentColor = NeutralDark) {
                BioLeafBulletList(result.sustainableFarmingPractices)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun BioLeafKnowledgeSection(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedIds by remember { mutableStateOf(setOf("neem_oil")) }
    val filteredItems = remember(searchQuery) { OrganicKnowledgeBase.searchKnowledge(searchQuery) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("bioleaf_knowledge_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Organic Agricultural Knowledge",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = NeutralDark
        )
        Text(
            text = "Verified preparations, crop-specific organic protection, soil biology enhancers, and safety precautions.",
            style = MaterialTheme.typography.bodySmall,
            color = NeutralMedium
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search organic guides") },
            placeholder = { Text("Search neem oil, Panchagavya, tomato, compost...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("knowledge_search_input")
        )

        if (filteredItems.isEmpty()) {
            BioLeafSectionCard(title = "No Matching Guides", accentColor = NeutralDark) {
                Text(
                    text = "Try searching for neem oil, garlic-chilli, Panchagavya, Jeevamrutham, compost, vermicompost, or neem cake.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )
            }
        } else {
            filteredItems.forEach { item ->
                OrganicKnowledgeCard(
                    item = item,
                    expanded = expandedIds.contains(item.id),
                    onToggle = {
                        expandedIds = if (expandedIds.contains(item.id)) {
                            expandedIds - item.id
                        } else {
                            expandedIds + item.id
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun OrganicKnowledgeCard(
    item: OrganicKnowledgeItem,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .testTag("knowledge_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreen
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = NeutralDark
                )
            }

            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium
            )

            if (expanded) {
                Text(
                    text = "Preparation & Application",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
                BioLeafBulletList(item.preparationAndUsage)

                Text(
                    text = "Recommended Crops: ${item.targetCrops.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = EcoOnGreenContainer
                )
                Text(
                    text = "Target Issues: ${item.targetIssues.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )

                Text(
                    text = "Safety Precautions",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = WarningAmber
                )
                BioLeafBulletList(item.safetyPrecautions)
            }
        }
    }
}

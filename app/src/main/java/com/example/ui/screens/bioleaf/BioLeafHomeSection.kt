package com.example.ui.screens.bioleaf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.knowledge.OrganicKnowledgeBase
import com.example.data.model.BioLeafReportFormatter
import com.example.data.model.BioLeafSavedReportSummary
import com.example.data.model.BioLeafSection
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.EcoOnGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder

private data class HomeShortcutItem(
    val section: BioLeafSection,
    val label: String,
    val hint: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun BioLeafHomeSection(
    userName: String?,
    savedReports: List<BioLeafSavedReportSummary>,
    onNavigateSection: (BioLeafSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val shortcuts = listOf(
        HomeShortcutItem(BioLeafSection.PLANT_ID, "Plant ID", "Identify 12+ crops", Icons.Default.LocalFlorist, "shortcut_plant_id"),
        HomeShortcutItem(BioLeafSection.DISEASE_SCANNER, "Disease Scanner", "Leaf pathology report", Icons.Default.Healing, "shortcut_disease"),
        HomeShortcutItem(BioLeafSection.PEST_SCANNER, "Pest Scanner", "Insects & damage", Icons.Default.BugReport, "shortcut_pest"),
        HomeShortcutItem(BioLeafSection.SOIL_GUIDANCE, "Soil Guidance", "Software-based plan", Icons.Default.Landscape, "shortcut_soil"),
        HomeShortcutItem(BioLeafSection.AI_ASSISTANT, "AI Assistant", "Multilingual chat", Icons.AutoMirrored.Filled.Chat, "shortcut_chat"),
        HomeShortcutItem(BioLeafSection.COMMUNITY, "Community", "Farmer discussions", Icons.Default.Forum, "shortcut_community"),
        HomeShortcutItem(BioLeafSection.ORGANIC_KNOWLEDGE, "Organic Library", "Neem, compost & IPM", Icons.AutoMirrored.Filled.MenuBook, "shortcut_knowledge"),
        HomeShortcutItem(BioLeafSection.HISTORY_REPORTS, "Saved Reports", "${savedReports.size} saved records", Icons.Default.Description, "shortcut_reports")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("bioleaf_home_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome & Brand Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(NeutralDark, Color(0xFF14532D), EcoGreen)
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "BIO LEAF AI",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "BIO LEAF AI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "100% Software Crop Intelligence",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFBBF7D0),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "Welcome${userName?.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Intelligent Crop Health Monitoring and Sustainable Agricultural Assistance powered by vision AI and organic agronomy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
        }

        // Core AI Scanners & Tools Grid
        Text(
            text = "AGRICULTURAL AI WORKSPACE",
            style = MaterialTheme.typography.labelSmall,
            color = NeutralMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.9.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            shortcuts.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { item ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onNavigateSection(item.section) }
                                .testTag(item.tag),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(EcoGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = EcoGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark
                                )
                                Text(
                                    text = item.hint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Actual Saved User Reports (Real data only, clean empty state when none exist)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT SAVED ANALYSES & REPORTS",
                style = MaterialTheme.typography.labelSmall,
                color = NeutralMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.9.sp
            )
            if (savedReports.isNotEmpty()) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreen,
                    modifier = Modifier
                        .clickable { onNavigateSection(BioLeafSection.HISTORY_REPORTS) }
                        .padding(4.dp)
                )
            }
        }

        if (savedReports.isEmpty()) {
            BioLeafSectionCard(
                title = "No Saved Analyses Yet",
                accentColor = NeutralDark,
                modifier = Modifier.testTag("bioleaf_home_empty_reports")
            ) {
                Text(
                    text = "Only your actual AI plant identifications, disease reports, pest scans, and soil guidance plans appear here. Start any scanner above to save your first report.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )
            }
        } else {
            savedReports.take(3).forEach { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigateSection(BioLeafSection.HISTORY_REPORTS) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.category.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EcoGreen
                            )
                            Text(
                                text = report.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeutralDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${report.subtitle} • ${BioLeafReportFormatter.formatTimestamp(report.timestampEpochMs)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Useful Organic Insights
        Text(
            text = "SUSTAINABLE AGRICULTURAL INSIGHTS",
            style = MaterialTheme.typography.labelSmall,
            color = NeutralMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.9.sp
        )

        OrganicKnowledgeBase.knowledgeItems.take(2).forEach { insight ->
            BioLeafSectionCard(title = insight.title, accentColor = EcoOnGreenContainer) {
                Text(
                    text = insight.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralDark
                )
                Text(
                    text = "Open Organic Library ->",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreen,
                    modifier = Modifier
                        .clickable { onNavigateSection(BioLeafSection.ORGANIC_KNOWLEDGE) }
                        .padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

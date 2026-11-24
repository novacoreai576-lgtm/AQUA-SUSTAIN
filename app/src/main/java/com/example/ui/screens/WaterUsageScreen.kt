package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WaterUsageRecord
import com.example.data.model.WaterUsageSummary
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaterUsageScreen(
    records: List<WaterUsageRecord>,
    summary: WaterUsageSummary,
    errorMessage: String?,
    saveFeedback: String?,
    onLogUsage: (Double, String, String, String) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onClearFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(WaterUsageSummary.CATEGORIES.first()) }
    var selectedSource by remember { mutableStateOf(WaterUsageSummary.SOURCES.first()) }
    var notesText by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("water_usage_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Water Usage",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AquaPrimary
            )
            Text(
                text = "Track actual water consumption, analyze trends, and detect wastage",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMedium
            )
        }

        // Daily / Weekly / Monthly Real Totals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UsagePeriodCard(
                title = "TODAY",
                value = if (summary.totalRecords == 0) "--" else WaterUsageSummary.formatLiters(summary.todayLiters),
                delta = if (summary.totalRecords == 0) "No logs" else formatDelta(summary.dailyDifferenceLiters, "vs yesterday"),
                isPositiveDelta = summary.dailyDifferenceLiters <= 0.0,
                modifier = Modifier.weight(1f)
            )
            UsagePeriodCard(
                title = "7 DAYS",
                value = if (summary.totalRecords == 0) "--" else WaterUsageSummary.formatLiters(summary.weeklyLiters),
                delta = if (summary.totalRecords == 0) "No logs" else formatDelta(summary.weeklyDifferenceLiters, "vs prev 7d"),
                isPositiveDelta = summary.weeklyDifferenceLiters <= 0.0,
                modifier = Modifier.weight(1f)
            )
            UsagePeriodCard(
                title = "30 DAYS",
                value = if (summary.totalRecords == 0) "--" else WaterUsageSummary.formatLiters(summary.monthlyLiters),
                delta = if (summary.totalRecords == 0) "No logs" else "${summary.totalRecords} total entries",
                isPositiveDelta = summary.monthlyDifferenceLiters <= 0.0,
                modifier = Modifier.weight(1f)
            )
        }

        // Log Consumption Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "LOG WATER CONSUMPTION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeutralMedium,
                    letterSpacing = 0.8.sp
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        localError = null
                        onClearFeedback()
                    },
                    label = { Text("Volume Used (Liters)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("usage_liters_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AquaPrimary,
                        focusedLabelColor = AquaPrimary
                    )
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WaterUsageSummary.CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AquaPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text(
                    text = "Water Source",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WaterUsageSummary.SOURCES.forEach { src ->
                        FilterChip(
                            selected = selectedSource == src,
                            onClick = { selectedSource = src },
                            label = { Text(src, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EcoGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (optional, e.g., Drip zone 1, Cleaning)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AquaPrimary,
                        focusedLabelColor = AquaPrimary
                    )
                )

                val displayError = localError ?: errorMessage
                if (!displayError.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AlertRedContainer)
                            .padding(10.dp)
                    ) {
                        Text(displayError, style = MaterialTheme.typography.bodySmall, color = AlertRed)
                    }
                }

                if (!saveFeedback.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(EcoGreenContainer)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(saveFeedback, style = MaterialTheme.typography.bodySmall, color = EcoGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = {
                        val liters = amountText.trim().toDoubleOrNull()
                        if (liters == null || liters <= 0.0) {
                            localError = "Enter a valid positive number of liters."
                            return@Button
                        }
                        onLogUsage(liters, selectedCategory, selectedSource, notesText.trim())
                        amountText = ""
                        notesText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("usage_submit_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Water Usage Entry", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 7-Day Real Usage Chart (strictly from saved records — NO fake chart)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Opacity, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Last 7 Days Consumption (Saved Data)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                }

                if (summary.totalRecords == 0) {
                    Text(
                        text = "Waiting for data connection or user entries. Log your water usage above to view real daily usage bars and trends.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )
                } else {
                    val maxLiters = summary.dailySeries.maxOfOrNull { it.totalLiters }?.coerceAtLeast(1.0) ?: 1.0
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val barCount = summary.dailySeries.size.coerceAtLeast(1)
                        val slotWidth = size.width / barCount
                        val barWidth = slotWidth * 0.54f
                        val usableHeight = size.height - 12.dp.toPx()

                        summary.dailySeries.forEachIndexed { idx, pt ->
                            val ratio = (pt.totalLiters / maxLiters).toFloat().coerceIn(0f, 1f)
                            val barHeight = (ratio * usableHeight).coerceAtLeast(if (pt.totalLiters > 0) 6.dp.toPx() else 2.dp.toPx())
                            val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                            val top = size.height - barHeight

                            drawRoundRect(
                                color = if (pt.totalLiters > 0) AquaPrimary else Color(0xFFE2E8F0),
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        summary.dailySeries.forEach { pt ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = pt.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark
                                )
                                Text(
                                    text = if (pt.totalLiters > 0) "${pt.totalLiters.toInt()}L" else "0",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeutralMedium,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Breakdown & Wastage Alerts / Conservation Insights
        if (summary.totalRecords > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Wastage Alerts & Conservation Insights",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    }

                    summary.wastageAlerts.forEach { alert ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(WarningAmberContainer)
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(alert, style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                        }
                    }

                    summary.conservationInsights.forEach { insight ->
                        Text("• $insight", style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                    }

                    if (summary.categoryBreakdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "7-DAY CATEGORY BREAKDOWN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeutralMedium
                        )
                        summary.categoryBreakdown.forEach { item ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(item.category, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = NeutralDark)
                                    Text(
                                        "${WaterUsageSummary.formatLiters(item.totalLiters)} (${item.percentage.toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AquaPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = AquaPrimary,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Saved Usage Records List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "LOGGED WATER USAGE ENTRIES (${records.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeutralMedium
                )
                if (records.isEmpty()) {
                    Text(
                        text = "No water usage entries logged yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )
                } else {
                    val df = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                    records.take(25).forEach { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${WaterUsageSummary.formatLiters(rec.amountLiters)} • ${rec.category}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark
                                )
                                Text(
                                    text = "${df.format(Date(rec.usageDateEpochMs))} • ${rec.source}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeutralMedium
                                )
                                if (rec.notes.isNotBlank()) {
                                    Text(
                                        text = rec.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeutralMedium
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteRecord(rec.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete usage entry", tint = AlertRed)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatDelta(diffLiters: Double, suffix: String): String {
    val sign = if (diffLiters > 0) "+" else if (diffLiters < 0) "-" else ""
    val absStr = WaterUsageSummary.formatLiters(kotlin.math.abs(diffLiters))
    return "$sign$absStr $suffix"
}

@Composable
private fun UsagePeriodCard(
    title: String,
    value: String,
    delta: String,
    isPositiveDelta: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = NeutralMedium,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AquaPrimary
        )
        Text(
            text = delta,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPositiveDelta) EcoGreen else WarningAmber,
            fontSize = 10.sp
        )
    }
}

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SensorReading
import com.example.ui.HistoryTimeFilter
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.AquaPrimaryContainer
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TelemetryHistorySection(
    readings: List<SensorReading>,
    selectedFilter: HistoryTimeFilter,
    onFilterSelected: (HistoryTimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val rainCount = readings.count { it.rain }
    val pumpOnCount = readings.count { it.pump }
    val avgMoisture = if (readings.isNotEmpty()) readings.map { it.soilMoisture }.average().toInt() else 0
    val avgTank = if (readings.isNotEmpty()) readings.map { it.waterLevel }.average().toInt() else 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("telemetry_history_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "TELEMETRY HISTORY & TRENDS",
            style = MaterialTheme.typography.labelSmall,
            color = NeutralMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryTimeFilter.entries.forEach { filter ->
                FilterChip(
                    selected = filter == selectedFilter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(filter.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AquaPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryStatPill(
                icon = Icons.Default.Grass,
                iconColor = EcoGreen,
                label = "Avg Soil",
                value = "$avgMoisture%",
                modifier = Modifier.weight(1f)
            )
            HistoryStatPill(
                icon = Icons.Default.Water,
                iconColor = AquaPrimary,
                label = "Avg Tank",
                value = "$avgTank%",
                modifier = Modifier.weight(1f)
            )
            HistoryStatPill(
                icon = Icons.Default.Cloud,
                iconColor = WaterBlue,
                label = "Rain",
                value = "$rainCount",
                modifier = Modifier.weight(1f)
            )
            HistoryStatPill(
                icon = Icons.Default.Power,
                iconColor = AlertRed,
                label = "Pump",
                value = "$pumpOnCount",
                modifier = Modifier.weight(1f)
            )
        }

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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(EcoGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Grass, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Soil Moisture Trend",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeutralDark
                            )
                            Text(
                                text = "Dashed line: 50% dry threshold",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeutralMedium
                            )
                        }
                    }
                    Text(
                        text = "${readings.size} pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HistoryLineChart(
                    data = readings.take(20).reversed().map { it.soilMoisture.toFloat() },
                    lineColor = EcoGreen,
                    fillColor = EcoGreen.copy(alpha = 0.14f),
                    thresholdY = 50f,
                    thresholdColor = AlertRed.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(AquaPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Water, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Water Tank Level Trend",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                        Text(
                            text = "Dashed line: 25% reserve warning",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeutralMedium
                        )
                    }
                }

                HistoryLineChart(
                    data = readings.take(20).reversed().map { it.waterLevel.toFloat() },
                    lineColor = AquaPrimary,
                    fillColor = AquaPrimary.copy(alpha = 0.14f),
                    thresholdY = 25f,
                    thresholdColor = AlertRed.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        }

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
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recent Telemetry Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    }
                    Text(
                        text = "${readings.size} records",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (readings.isEmpty()) {
                    Text(
                        text = "No readings logged yet for this timeframe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        readings.take(8).forEach { reading ->
                            HistoryReadingRow(reading = reading)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryStatPill(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = NeutralDark)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = NeutralMedium, fontSize = 10.sp)
        }
    }
}

@Composable
private fun HistoryLineChart(
    data: List<Float>,
    lineColor: Color,
    fillColor: Color,
    thresholdY: Float? = null,
    thresholdColor: Color = AlertRed,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Insufficient chart data points", color = NeutralMedium, fontSize = 12.sp)
        }
        return
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxVal = 100f

        for (i in 0..4) {
            val y = height * (i.toFloat() / 4f)
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (thresholdY != null) {
            val threshY = height - (thresholdY / maxVal) * height
            drawLine(
                color = thresholdColor,
                start = Offset(0f, threshY),
                end = Offset(width, threshY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        if (data.size == 1) {
            val y = height - (data[0].coerceIn(0f, 100f) / maxVal) * height
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(width / 2f, y))
            return@Canvas
        }

        val stepX = width / (data.size - 1)
        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value.coerceIn(0f, 100f) / maxVal) * height
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            if (index == data.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        drawPath(path = fillPath, color = fillColor)
        drawPath(path = path, color = lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value.coerceIn(0f, 100f) / maxVal) * height
            drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = Offset(x, y))
            drawCircle(color = lineColor, radius = 2.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
private fun HistoryReadingRow(reading: SensorReading) {
    val dateFormatter = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
    val date = reading.timestamp?.toDate() ?: Date(reading.timestampEpochMs)
    val timeText = dateFormatter.format(date)

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
        Column {
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = NeutralMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Soil: ${reading.soilMoisture}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (reading.soilMoisture < 50) AlertRed else EcoGreen
                )
                Text(
                    text = "Tank: ${reading.waterLevel}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AquaPrimary
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (reading.rain) WaterBlueContainer else Color(0xFFF1F5F9))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (reading.rain) "RAIN" else "DRY",
                    color = if (reading.rain) WaterBlue else NeutralMedium,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (reading.pump) EcoGreenContainer else Color(0xFFF1F5F9))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (reading.pump) "PUMP ON" else "PUMP OFF",
                    color = if (reading.pump) EcoGreen else NeutralMedium,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

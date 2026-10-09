package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SensorReading
import com.example.ui.HistoryTimeFilter
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
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
fun HistoryScreen(
    readings: List<SensorReading>,
    selectedFilter: HistoryTimeFilter = HistoryTimeFilter.TODAY,
    onFilterSelected: (HistoryTimeFilter) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val rainCount = readings.count { it.rain }
    val pumpOnCount = readings.count { it.pump }
    val avgMoisture = if (readings.isNotEmpty()) readings.map { it.soilMoisture }.average().toInt() else 0
    val avgTank = if (readings.isNotEmpty()) readings.map { it.waterLevel }.average().toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("history_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title
        Column {
            Text(
                text = "Telemetry History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AquaPrimary
            )
            Text(
                text = "Historical Sensor Readings: sensorHistory/{userId}/records",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMedium
            )
        }

        // Time Range Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = NeutralMedium,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Time Range:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HistoryTimeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = filter == selectedFilter,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AquaPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                    )
                }
            }
        }

        // Summary Statistics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryStatCard(
                icon = Icons.Default.Grass,
                iconColor = EcoGreen,
                label = "Avg Soil",
                value = "$avgMoisture%",
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                icon = Icons.Default.Water,
                iconColor = AquaPrimary,
                label = "Avg Tank",
                value = "$avgTank%",
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                icon = Icons.Default.Cloud,
                iconColor = WaterBlue,
                label = "Rain Events",
                value = "$rainCount",
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                icon = Icons.Default.Power,
                iconColor = AlertRed,
                label = "Pump Cycles",
                value = "$pumpOnCount",
                modifier = Modifier.weight(1f)
            )
        }

        // 1. Soil Moisture Trend Chart Card
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
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(EcoGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Grass, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SOIL MOISTURE OVER TIME (%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeutralMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Red line: 50% dry threshold (Pump activates)",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralDark
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EcoGreenContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Moisture %", color = EcoGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SimpleLineChart(
                    data = readings.take(20).reversed().map { it.soilMoisture.toFloat() },
                    lineColor = EcoGreen,
                    fillColor = EcoGreen.copy(alpha = 0.15f),
                    thresholdY = 50f,
                    thresholdColor = AlertRed.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }

        // 2. Water Tank Level Trend Chart Card
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
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AquaPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Water, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WATER LEVEL OVER TIME (%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeutralMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Underground tank rainwater reserve",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeutralDark
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AquaPrimaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Level %", color = AquaPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SimpleLineChart(
                    data = readings.take(20).reversed().map { it.waterLevel.toFloat() },
                    lineColor = AquaPrimary,
                    fillColor = AquaPrimary.copy(alpha = 0.15f),
                    thresholdY = 25f,
                    thresholdColor = AlertRed.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }

        // 3 & 4. Pump Activity & Rain Events Timeline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pump Activity Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "PUMP ACTIVITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$pumpOnCount Active Cycles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (pumpOnCount > 0) EcoGreen else NeutralDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Activity mini-bar visualizer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        readings.take(12).reversed().forEach { r ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (r.pump) EcoGreen else Color(0xFFE2E8F0))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Recent pump duty cycles",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium,
                        fontSize = 10.sp
                    )
                }
            }

            // Rain Events Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "RAIN EVENTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$rainCount Rain Occurrences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (rainCount > 0) WaterBlue else NeutralDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Rain mini-bar visualizer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        readings.take(12).reversed().forEach { r ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (r.rain) WaterBlue else Color(0xFFE2E8F0))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sensor D2 precipitation",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Detailed Telemetry Log Table
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
                        Icon(Icons.Default.History, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historical Telemetry Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    }

                    Text(
                        text = "${readings.size} records",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (readings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No readings logged yet for this timeframe.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeutralMedium
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        readings.take(25).forEach { reading ->
                            ReadingItemRow(reading = reading)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
private fun SimpleLineChart(
    data: List<Float>,
    lineColor: Color,
    fillColor: Color,
    thresholdY: Float? = null,
    thresholdColor: Color = Color.Red,
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
        val minVal = 0f

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = Color(0xFFF1F5F9),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw threshold line if specified
        if (thresholdY != null) {
            val threshY = height - (thresholdY / (maxVal - minVal)) * height
            drawLine(
                color = thresholdColor,
                start = Offset(0f, threshY),
                end = Offset(width, threshY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        if (data.size == 1) {
            val y = height - (data[0] / maxVal) * height
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(width / 2, y))
            return@Canvas
        }

        val stepX = width / (data.size - 1)
        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value.coerceIn(0f, 100f)) / maxVal) * height

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

        // Draw point dots
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value.coerceIn(0f, 100f)) / maxVal) * height
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(x, y))
            drawCircle(color = lineColor, radius = 2.5.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
private fun ReadingItemRow(reading: SensorReading) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val date = reading.timestamp?.toDate() ?: Date(reading.timestampEpochMs)
    val dateStr = dateFormatter.format(date)
    val timeStr = timeFormatter.format(date)

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
                text = "$dateStr • $timeStr",
                style = MaterialTheme.typography.labelSmall,
                color = NeutralMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🌱 Soil: ${reading.soilMoisture}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (reading.soilMoisture < 50) AlertRed else EcoGreen
                )
                Text(
                    text = "💧 Water: ${reading.waterLevel}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AquaPrimary
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Rain Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (reading.rain) WaterBlueContainer else Color(0xFFF1F5F9))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (reading.rain) "RAIN" else "NO RAIN",
                    color = if (reading.rain) WaterBlue else NeutralMedium,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Pump Status Badge
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

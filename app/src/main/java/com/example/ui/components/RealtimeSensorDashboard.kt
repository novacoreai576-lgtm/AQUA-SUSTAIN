package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AquaSustainData
import com.example.data.model.SoilStatus
import com.example.data.model.TankLevelCategory
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueContainer

/**
 * Real-time Main Sensor Dashboard Hub with circular gauges and cards.
 * Displays real-time telemetry from Arduino UNO R4 WiFi:
 * - Water Storage Tank Gauge (A1 Analog, 0-100%)
 * - Soil Moisture Gauge (A0 Analog, 0-100%)
 * - Rain Drop Status (D2 Digital)
 * - Relay / Water Pump Status (D7 Digital)
 */
@Composable
fun RealtimeSensorDashboard(
    sensorData: AquaSustainData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_sensor_dashboard_hub"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Real-time Telemetry",
                            tint = AquaPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "REAL-TIME SENSOR TELEMETRY",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeutralMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Gauges & Live Readings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NeutralDark
                        )
                    }
                }

                // Live status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(EcoGreenContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(EcoGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = EcoGreen,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Dual Radial Gauge Cluster (Tank Level & Soil Moisture)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Water Tank Radial Gauge Card
                TankGaugeCard(
                    percentage = sensorData.waterLevel,
                    category = sensorData.tankLevelCategory,
                    modifier = Modifier.weight(1f)
                )

                // 2. Soil Moisture Radial Gauge Card
                SoilMoistureGaugeCard(
                    percentage = sensorData.soilMoisture,
                    status = sensorData.soilStatus,
                    modifier = Modifier.weight(1f)
                )
            }

            // Sensor Mini Cards (Rain D2 & Pump Relay D7)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rain Mini Card
                MiniSensorStatusCard(
                    title = "Rain Status",
                    value = if (sensorData.rain) "Rain Active" else "Clear / No Rain",
                    subtext = if (sensorData.rain) "Harvester Collecting" else "D2 Digital Pin Dry",
                    icon = if (sensorData.rain) Icons.Default.Grain else Icons.Default.WbSunny,
                    accentColor = if (sensorData.rain) WaterBlue else WarningAmber,
                    bgColor = if (sensorData.rain) WaterBlueContainer else Color(0xFFFEF3C7),
                    modifier = Modifier.weight(1f),
                    testTag = "mini_rain_status_card"
                )

                // Pump Relay Mini Card
                MiniSensorStatusCard(
                    title = "Pump Relay",
                    value = if (sensorData.pump) "PUMP ACTIVE" else "PUMP IDLE",
                    subtext = if (sensorData.pump) "D7 Low (Irrigating)" else "D7 High (Relay Off)",
                    icon = Icons.Default.PowerSettingsNew,
                    accentColor = if (sensorData.pump) EcoGreen else NeutralMedium,
                    bgColor = if (sensorData.pump) EcoGreenContainer else Color(0xFFF1F5F9),
                    modifier = Modifier.weight(1f),
                    testTag = "mini_pump_status_card"
                )
            }
        }
    }
}

/**
 * Circular Arc Radial Gauge for Water Tank Level
 */
@Composable
private fun TankGaugeCard(
    percentage: Int,
    category: TankLevelCategory,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percentage.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "tank_radial_gauge"
    )

    val gaugeColor = when (category) {
        TankLevelCategory.LOW -> AlertRed
        TankLevelCategory.NORMAL -> WaterBlue
        TankLevelCategory.HIGH -> AquaPrimary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WATER TANK",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.Water,
                    contentDescription = null,
                    tint = gaugeColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gauge Drawing
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val startAngle = 135f
                    val sweepAngle = 270f
                    val currentSweep = (animatedPercent / 100f) * sweepAngle

                    // Track Arc
                    drawArc(
                        color = Color(0xFFE2E8F0),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(Color(0xFF38BDF8), gaugeColor, gaugeColor)
                        ),
                        startAngle = startAngle,
                        sweepAngle = currentSweep,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Centered percentage text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${percentage}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = NeutralDark,
                        fontSize = 20.sp
                    )
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = gaugeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pin A1 • ${category.label}",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Circular Arc Radial Gauge for Soil Moisture
 */
@Composable
private fun SoilMoistureGaugeCard(
    percentage: Int,
    status: SoilStatus,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percentage.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "soil_radial_gauge"
    )

    val isDry = status == SoilStatus.DRY
    val gaugeColor = if (isDry) WarningAmber else EcoGreen

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SOIL MOISTURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.Grass,
                    contentDescription = null,
                    tint = gaugeColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gauge Drawing
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val startAngle = 135f
                    val sweepAngle = 270f
                    val currentSweep = (animatedPercent / 100f) * sweepAngle

                    // Track Arc
                    drawArc(
                        color = Color(0xFFE2E8F0),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(Color(0xFF86EFAC), gaugeColor, gaugeColor)
                        ),
                        startAngle = startAngle,
                        sweepAngle = currentSweep,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Centered percentage text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${percentage}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = NeutralDark,
                        fontSize = 20.sp
                    )
                    Text(
                        text = if (isDry) "DRY" else "WET",
                        style = MaterialTheme.typography.labelSmall,
                        color = gaugeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pin A0 • ${if (isDry) "<50% Dry" else "≥50% Adequate"}",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Compact Status Metric Card for Rain & Pump relay
 */
@Composable
private fun MiniSensorStatusCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NeutralDark
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = NeutralMedium,
                fontSize = 10.5.sp
            )
        }
    }
}

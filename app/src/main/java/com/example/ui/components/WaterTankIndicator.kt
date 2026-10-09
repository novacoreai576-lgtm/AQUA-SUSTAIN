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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TankLevelCategory
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueContainer
import com.example.ui.theme.WaterBlueDark

@Composable
fun WaterTankIndicator(
    waterLevelPercent: Int,
    tankCategory: TankLevelCategory,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = waterLevelPercent.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "water_level_anim"
    )

    val badgeColor = when (tankCategory) {
        TankLevelCategory.LOW -> AlertRed
        TankLevelCategory.NORMAL -> WaterBlue
        TankLevelCategory.HIGH -> AquaPrimary
    }

    val badgeBg = when (tankCategory) {
        TankLevelCategory.LOW -> AlertRedContainer
        TankLevelCategory.NORMAL -> WaterBlueContainer
        TankLevelCategory.HIGH -> Color(0xFFCCFBF1)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("water_tank_indicator"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WaterBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Water,
                            contentDescription = "Water Tank",
                            tint = WaterBlueDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "UNDERGROUND STORAGE TANK",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeutralMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Rainwater Reservoir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    }
                }

                // Level Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(badgeBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tankCategory.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Visual Tank Graphic + Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Interactive / Graphic Tank Cylinder
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                ) {
                    // Water Fluid Fill Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val fillHeight = (animatedPercent / 100f) * height
                        val topY = height - fillHeight

                        val waterBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8),
                                Color(0xFF0284C7),
                                Color(0xFF0369A1)
                            ),
                            startY = topY,
                            endY = height
                        )

                        // Draw fluid rectangle
                        drawRoundRect(
                            brush = waterBrush,
                            topLeft = Offset(0f, topY),
                            size = Size(width, fillHeight),
                            cornerRadius = CornerRadius(0f, 0f)
                        )

                        // Draw guideline markers (25%, 50%, 75%, 100%)
                        val markQuarter = height * 0.75f // 25%
                        val markHalf = height * 0.5f   // 50%
                        val markThreeQuarters = height * 0.25f // 75%

                        drawLine(
                            color = Color(0x6694A3B8),
                            start = Offset(0f, markQuarter),
                            end = Offset(width * 0.25f, markQuarter),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = Color(0x6694A3B8),
                            start = Offset(0f, markHalf),
                            end = Offset(width * 0.35f, markHalf),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = Color(0x6694A3B8),
                            start = Offset(0f, markThreeQuarters),
                            end = Offset(width * 0.25f, markThreeQuarters),
                            strokeWidth = 2f
                        )
                    }

                    // Water Level Label inside tank
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 12.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = "${waterLevelPercent}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (animatedPercent > 35f) Color.White else NeutralDark
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Detail Metrics Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricRow(
                        label = "Sensor Reading",
                        value = "${waterLevelPercent}%",
                        color = badgeColor
                    )
                    MetricRow(
                        label = "Collection Pipe Inflow",
                        value = "Perforated Pipe System",
                        color = NeutralDark
                    )
                    MetricRow(
                        label = "Purification Unit",
                        value = "Nostoc Bio-Filtration",
                        color = AquaPrimary
                    )
                    MetricRow(
                        label = "Pump Feeder Status",
                        value = if (waterLevelPercent < 15) "Low Safety Lock" else "Ready For Irrigation",
                        color = if (waterLevelPercent < 15) AlertRed else EcoGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Advisory Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (tankCategory == TankLevelCategory.LOW) AlertRedContainer else Color(0xFFF8FAFC)
                    )
                    .border(
                        1.dp,
                        if (tankCategory == TankLevelCategory.LOW) AlertRed.copy(alpha = 0.3f) else Color(0xFFE2E8F0),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (tankCategory == TankLevelCategory.LOW) Icons.Default.Warning else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (tankCategory == TankLevelCategory.LOW) AlertRed else WaterBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = tankCategory.advisory,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (tankCategory == TankLevelCategory.LOW) AlertRed else NeutralDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = NeutralMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Power
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AquaSustainData
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.AquaPrimaryContainer
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueContainer

@Composable
fun SystemStatusCard(
    sensorData: AquaSustainData,
    modifier: Modifier = Modifier
) {
    val isPumpOn = sensorData.pump
    val isRain = sensorData.rain
    val statusText = sensorData.resolvedSystemStatus

    val accentColor by animateColorAsState(
        targetValue = when {
            isPumpOn -> EcoGreen
            isRain -> WaterBlue
            else -> AquaPrimary
        },
        animationSpec = tween(300),
        label = "systemStatusColor"
    )

    val containerBg by animateColorAsState(
        targetValue = when {
            isPumpOn -> EcoGreenContainer
            isRain -> WaterBlueContainer
            else -> Color(0xFFF1F5F9)
        },
        animationSpec = tween(300),
        label = "systemStatusContainerBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("system_status_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(18.dp)
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
                            .background(containerBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperBoard,
                            contentDescription = "System Status Icon",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SYSTEM STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeutralMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Live Irrigation Controller",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    }
                }

                // Live status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(containerBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPumpOn) "IRRIGATING" else if (isRain) "RAIN HOLD" else "STANDBY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Status Display Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = "LIVE CONTROLLER STATUS STRING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPumpOn) Color(0xFF4ADE80) else Color(0xFF38BDF8),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Key Factor Breakdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rain Indicator
                StatusPill(
                    icon = Icons.Default.Grain,
                    label = if (isRain) "Rain Detected" else "No Rain",
                    sub = "Sensor D2",
                    isActive = isRain,
                    activeColor = WaterBlue,
                    modifier = Modifier.weight(1f)
                )

                // Soil Moisture Indicator
                StatusPill(
                    icon = Icons.Default.Grass,
                    label = "${sensorData.soilMoisture}%",
                    sub = if (sensorData.isSoilDry) "Dry (<50%)" else "Wet (≥50%)",
                    isActive = sensorData.isSoilDry,
                    activeColor = if (sensorData.isSoilDry) Color(0xFFD97706) else EcoGreen,
                    modifier = Modifier.weight(1f)
                )

                // Pump / Relay Output
                StatusPill(
                    icon = Icons.Default.Power,
                    label = if (isPumpOn) "Pump ON" else "Pump OFF",
                    sub = if (isPumpOn) "Relay LOW" else "Relay HIGH",
                    isActive = isPumpOn,
                    activeColor = EcoGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    sub: String,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) activeColor else NeutralMedium,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeutralDark,
                fontSize = 11.sp
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) activeColor else NeutralMedium,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AquaSustainData
import com.example.data.model.SoilStatus
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueContainer
import com.example.ui.theme.WarningAmber

@Composable
fun RainStatusCard(
    isRainDetected: Boolean,
    modifier: Modifier = Modifier
) {
    val rainColor by animateColorAsState(
        targetValue = if (isRainDetected) WaterBlue else WarningAmber,
        label = "rain_color"
    )

    Card(
        modifier = modifier.testTag("rain_sensor_card"),
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRainDetected) WaterBlueContainer else Color(0xFFFEF3C7)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRainDetected) Icons.Default.Grain else Icons.Default.WbSunny,
                        contentDescription = "Rain Icon",
                        tint = rainColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isRainDetected) WaterBlueContainer else Color(0xFFF1F5F9)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isRainDetected) "PRECIPITATION" else "CLEAR SKY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRainDetected) WaterBlue else NeutralMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "RAIN DROP SENSOR",
                style = MaterialTheme.typography.labelSmall,
                color = NeutralMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isRainDetected) "Rain Detected" else "No Rain",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isRainDetected) WaterBlue else NeutralDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isRainDetected) {
                    "Rain harvesting active. Irrigation pump held OFF."
                } else {
                    "No rain detected. Monitoring soil for irrigation."
                },
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun SoilMoistureCard(
    soilMoisturePercent: Int,
    soilStatus: SoilStatus,
    modifier: Modifier = Modifier
) {
    val isDry = soilStatus == SoilStatus.DRY
    val badgeColor = if (isDry) WarningAmber else EcoGreen
    val badgeBg = if (isDry) Color(0xFFFEF3C7) else EcoGreenContainer

    Card(
        modifier = modifier.testTag("soil_sensor_card"),
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EcoGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Grass,
                        contentDescription = "Soil Icon",
                        tint = EcoGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDry) "DRY SOIL" else "WET / ADEQUATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SOIL MOISTURE SENSOR",
                style = MaterialTheme.typography.labelSmall,
                color = NeutralMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$soilMoisturePercent%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDry) "(Dry: <50%)" else "(Wet: ≥50%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = badgeColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (soilMoisturePercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isDry) WarningAmber else EcoGreen,
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isDry) {
                    "Dry soil detected. Triggers pump ON when No Rain."
                } else {
                    "Optimal root moisture. Irrigation not needed."
                },
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                lineHeight = 16.sp
            )
        }
    }
}

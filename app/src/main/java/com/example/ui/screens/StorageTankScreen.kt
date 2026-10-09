package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AquaSustainData
import com.example.data.model.TankLevelCategory
import com.example.ui.components.WaterTankIndicator
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueContainer

@Composable
fun StorageTankScreen(
    sensorData: AquaSustainData,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("storage_tank_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Column {
            Text(
                text = "Underground Reservoir",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AquaPrimary
            )
            Text(
                text = "Rainwater Harvesting & Underground Storage Monitoring",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMedium
            )
        }

        // Primary Tank Indicator
        WaterTankIndicator(
            waterLevelPercent = sensorData.waterLevel,
            tankCategory = sensorData.tankLevelCategory
        )

        // Storage Level Breakdown Legend
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "STORAGE CAPACITY THRESHOLDS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                ThresholdRow(
                    range = "< 25%",
                    title = "Low Level Warning",
                    desc = "Underground reservoir depleted. Automated pump safety safeguard holds relay if water drops below 15%.",
                    color = AlertRed,
                    bg = AlertRedContainer,
                    isActive = sensorData.tankLevelCategory == TankLevelCategory.LOW
                )

                Spacer(modifier = Modifier.height(8.dp))

                ThresholdRow(
                    range = "25% - 80%",
                    title = "Normal Storage Level",
                    desc = "Healthy water reserve available for scheduled drip irrigation across the sloped field.",
                    color = WaterBlue,
                    bg = WaterBlueContainer,
                    isActive = sensorData.tankLevelCategory == TankLevelCategory.NORMAL
                )

                Spacer(modifier = Modifier.height(8.dp))

                ThresholdRow(
                    range = "> 80%",
                    title = "High Level Capacity",
                    desc = "Optimal monsoon catchment achieved. Surplus routed safely to soil recharge aquifers.",
                    color = AquaPrimary,
                    bg = Color(0xFFCCFBF1),
                    isActive = sensorData.tankLevelCategory == TankLevelCategory.HIGH
                )
            }
        }

        // Rainwater Infiltration & Collection Mechanics
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "RAINWATER HARVESTING WORKFLOW",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProcessStep(
                    icon = Icons.Default.FilterDrama,
                    step = "1. Sloped Field Surface Catchment",
                    detail = "Precipitation collects along the gradient of the sloped agricultural land, guided by impervious waterproof-sheet layers."
                )

                ProcessStep(
                    icon = Icons.Default.AltRoute,
                    step = "2. Perforated Subsurface Piping",
                    detail = "Subsurface perforated pipes channel filtered seepage directly toward the central collection basin, preventing runoff loss."
                )

                ProcessStep(
                    icon = Icons.Default.Shield,
                    step = "3. Nostoc Bio-Unit & Mechanical Filter",
                    detail = "Water passes through multi-stage physical sand-gravel filtration and a Nostoc cyanobacteria bio-unit for natural biological stabilization."
                )

                ProcessStep(
                    icon = Icons.Default.Water,
                    step = "4. Underground Storage & Drip Feed",
                    detail = "Clean rainwater is preserved shielded from sunlight and evaporation, ready for on-demand Arduino drip delivery."
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ThresholdRow(
    range: String,
    title: String,
    desc: String,
    color: Color,
    bg: Color,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) bg else Color(0xFFF8FAFC))
            .border(
                if (isActive) 1.5.dp else 1.dp,
                if (isActive) color else Color(0xFFE2E8F0),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = range,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) color else NeutralDark
                )
                if (isActive) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "● CURRENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ProcessStep(
    icon: ImageVector,
    step: String,
    detail: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2FE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WaterBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = step,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                lineHeight = 16.sp
            )
        }
    }
}

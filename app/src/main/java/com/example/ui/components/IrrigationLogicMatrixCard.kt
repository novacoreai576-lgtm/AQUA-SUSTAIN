package com.example.ui.components

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
import androidx.compose.material.icons.filled.Rule
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AquaSustainData
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium

@Composable
fun IrrigationLogicMatrixCard(
    sensorData: AquaSustainData,
    modifier: Modifier = Modifier
) {
    val activeIndex = sensorData.activeRuleIndex

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("irrigation_logic_matrix"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Rule,
                        contentDescription = "Irrigation Logic",
                        tint = AquaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AQUASUSTAIN IRRIGATION LOGIC",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Hardware Decision Matrix",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Rain detected -> Pump OFF
            LogicRow(
                index = 0,
                ruleNumber = "1",
                condition = "Rain detected",
                detail = "Priority 1: Rain sensor D2 triggered",
                expectedPump = "Pump OFF",
                isPumpActive = false,
                isActiveNow = activeIndex == 0
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Water level < 20% -> Pump OFF
            LogicRow(
                index = 1,
                ruleNumber = "2",
                condition = "Water level < 20%",
                detail = "Priority 2: Low water protection (A1 < 20%)",
                expectedPump = "Pump OFF",
                isPumpActive = false,
                isActiveNow = activeIndex == 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Soil moisture < 50% -> Pump ON
            LogicRow(
                index = 2,
                ruleNumber = "3",
                condition = "No rain + Soil moisture < 50%",
                detail = "Priority 3: Dry soil irrigation (A0 < 50%)",
                expectedPump = "Pump ON",
                isPumpActive = true,
                isActiveNow = activeIndex == 2,
                isMainActivation = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Else -> Pump OFF
            LogicRow(
                index = 3,
                ruleNumber = "4",
                condition = "Adequate soil moisture (≥ 50%)",
                detail = "Moisture satisfied + safe water level",
                expectedPump = "Pump OFF",
                isPumpActive = false,
                isActiveNow = activeIndex == 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Active Low Relay Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Relay (D7) is ACTIVE LOW: LOW = Pump ON, HIGH = Pump OFF.\nMaintained autonomously by Arduino UNO R4 WiFi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralDark,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun LogicRow(
    index: Int,
    ruleNumber: String,
    condition: String,
    detail: String,
    expectedPump: String,
    isPumpActive: Boolean,
    isActiveNow: Boolean,
    isMainActivation: Boolean = false
) {
    val rowBg = if (isActiveNow) {
        if (isPumpActive) EcoGreenContainer else Color(0xFFE2E8F0)
    } else {
        Color(0xFFF8FAFC)
    }

    val borderColor = if (isActiveNow) {
        if (isPumpActive) EcoGreen else NeutralDark
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(rowBg)
            .border(if (isActiveNow) 1.5.dp else 0.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isActiveNow) (if (isPumpActive) EcoGreen else NeutralDark) else Color(0xFFCBD5E1)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ruleNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = condition,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isActiveNow) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isActiveNow) NeutralDark else NeutralMedium
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isMainActivation && isActiveNow) EcoGreen else NeutralMedium
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isPumpActive) EcoGreen else Color(0xFF64748B)
                )
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = expectedPump,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

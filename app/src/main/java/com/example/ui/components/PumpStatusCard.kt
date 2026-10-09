package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue

@Composable
fun PumpStatusCard(
    isPumpOn: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pump_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val cardBgGradient = if (isPumpOn) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFE8F5E9),
                Color(0xFFE0F2FE)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFF8FAFC),
                Color(0xFFF1F5F9)
            )
        )
    }

    val statusBadgeColor by animateColorAsState(
        targetValue = if (isPumpOn) EcoGreen else NeutralMedium,
        label = "status_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pump_status_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgGradient)
                .border(
                    width = 1.5.dp,
                    color = if (isPumpOn) EcoGreen.copy(alpha = 0.4f) else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPumpOn) EcoGreenContainer else Color(0xFFE2E8F0)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Pump Power Icon",
                                tint = statusBadgeColor,
                                modifier = Modifier
                                    .size(28.dp)
                                    .then(if (isPumpOn) Modifier.scale(pulseScale) else Modifier)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "IRRIGATION PUMP RELAY",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeutralMedium,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isPumpOn) "PUMP ON" else "PUMP OFF",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPumpOn) EcoGreen else NeutralDark
                            )
                        }
                    }

                    // Live Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (isPumpOn) EcoGreen else Color(0xFFCBD5E1)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPumpOn) "ACTIVE" else "STANDBY",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Activity Description
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPumpOn) Icons.Default.WaterDrop else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isPumpOn) WaterBlue else NeutralMedium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isPumpOn) {
                            "Irrigation is active — Delivering drip water to agricultural field"
                        } else {
                            "Irrigation is inactive — Pump relay held open by Arduino"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralDark,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Automated by Arduino UNO R4 WiFi relay based on rain and soil sensors. Manual overrides are disabled to prevent crop flooding.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

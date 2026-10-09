package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import com.example.data.model.ConnectionStatus
import com.example.data.model.SystemStatusSummary
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium

@Composable
fun ConnectionStatusBar(
    connectionStatus: ConnectionStatus,
    systemStatus: SystemStatusSummary,
    formattedElapsed: String,
    pumpActive: Boolean,
    systemStatusString: String? = null,
    modifier: Modifier = Modifier
) {
    val isConnectionLost = connectionStatus is ConnectionStatus.ConnectionLost

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("connection_status_bar"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Critical Warning Banner if Connection Lost
        AnimatedVisibility(
            visible = isConnectionLost,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val lostState = connectionStatus as? ConnectionStatus.ConnectionLost
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AlertRedContainer),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AlertRed)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AlertRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Connection Lost",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Connection Lost",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AlertRed
                        )
                        Text(
                            text = "Communication with Arduino UNO R4 WiFi was interrupted. Last successful update was $formattedElapsed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Standard System Status Details Card
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
                    .padding(14.dp)
            ) {
                Text(
                    text = "SYSTEM HARDWARE STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusItemPill(
                        icon = Icons.Default.DeveloperBoard,
                        label = "Arduino",
                        value = if (systemStatus.isArduinoOnline) "CONNECTED" else "DISCONNECTED",
                        isGood = systemStatus.isArduinoOnline
                    )

                    StatusItemPill(
                        icon = Icons.Default.Wifi,
                        label = "Wi-Fi",
                        value = if (systemStatus.isWifiOnline) "CONNECTED" else "DISCONNECTED",
                        isGood = systemStatus.isWifiOnline
                    )

                    StatusItemPill(
                        icon = Icons.Default.Sensors,
                        label = "Sensors",
                        value = if (systemStatus.areSensorsOnline) "ONLINE" else "OFFLINE",
                        isGood = systemStatus.areSensorsOnline
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Last Updated row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = NeutralMedium,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Last Data Received: $formattedElapsed",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Pump: ${if (pumpActive) "ON" else "OFF"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (pumpActive) EcoGreen else NeutralDark
                    )
                }

                if (!systemStatusString.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (pumpActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYSTEM STATUS: ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeutralMedium,
                            fontSize = 10.sp
                        )
                        Text(
                            text = systemStatusString,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (pumpActive) EcoGreen else NeutralDark,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusItemPill(
    icon: ImageVector,
    label: String,
    value: String,
    isGood: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (isGood) EcoGreen else AlertRed)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeutralDark,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralDark,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = if (isGood) EcoGreen else AlertRed,
            fontWeight = FontWeight.Bold
        )
    }
}

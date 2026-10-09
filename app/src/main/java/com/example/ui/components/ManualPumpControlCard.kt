package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AquaSustainData
import com.example.data.repository.CommandStatus
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium

@Composable
fun ManualPumpControlCard(
    sensorData: AquaSustainData,
    commandStatus: CommandStatus,
    onSendPumpCommand: (Boolean) -> Unit,
    onSetMode: (String) -> Unit,
    onClearCommandStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var targetPumpAction by remember { mutableStateOf(true) } // true: ON, false: OFF

    // Confirmation Dialog before dispatching command
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    tint = if (targetPumpAction) EcoGreen else AlertRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (targetPumpAction) "Confirm Pump Activation" else "Confirm Pump Shutdown",
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
            },
            text = {
                Column {
                    Text(
                        text = if (targetPumpAction) {
                            "Are you sure you want to request PUMP ON? A command will be queued in Firestore for your Arduino UNO R4 WiFi to energize the Active LOW relay on pin D7."
                        } else {
                            "Are you sure you want to request PUMP OFF? The relay will be set HIGH (de-energized) on the Arduino controller."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralDark,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Safety Note: The app will await hardware telemetry confirmation before reflecting the state change.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onSendPumpCommand(targetPumpAction)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (targetPumpAction) EcoGreen else AlertRed
                    ),
                    modifier = Modifier.testTag("confirm_pump_command_button")
                ) {
                    Text(if (targetPumpAction) "Yes, Turn ON" else "Yes, Turn OFF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = NeutralMedium)
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pump_control_card"),
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = AquaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "IRRIGATION CONTROL MODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeutralMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (sensorData.isAutoMode) "Automatic Decision Mode" else "Manual Actuator Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    }
                }

                // Mode Selector Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = sensorData.isAutoMode,
                        onClick = { onSetMode("auto") },
                        label = { Text("Auto", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AquaPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("mode_chip_auto")
                    )
                    FilterChip(
                        selected = !sensorData.isAutoMode,
                        onClick = { onSetMode("manual") },
                        label = { Text("Manual", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF7C3AED),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("mode_chip_manual")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation / Guidance
            if (sensorData.isAutoMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AquaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto mode executes firmware rules on Arduino: Rain D2, Soil Moisture A0 (<50%), and Tank Level A1. Relay D7 actuates autonomously.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralDark,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                // Manual Mode Controls
                Column {
                    Text(
                        text = "Manual Actuator Commands (Relay D7):",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NeutralMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                targetPumpAction = true
                                showConfirmDialog = true
                            },
                            enabled = !sensorData.pump && commandStatus !is CommandStatus.WaitingForConfirmation,
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_manual_pump_on")
                        ) {
                            Icon(Icons.Default.Power, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request PUMP ON", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                targetPumpAction = false
                                showConfirmDialog = true
                            },
                            enabled = sensorData.pump && commandStatus !is CommandStatus.WaitingForConfirmation,
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_manual_pump_off")
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request PUMP OFF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Command Status Notification Banner
            AnimatedVisibility(visible = commandStatus !is CommandStatus.Idle) {
                Spacer(modifier = Modifier.height(12.dp))
                when (commandStatus) {
                    is CommandStatus.Sending -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0F2FE))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = AquaPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Queueing command in Firestore...",
                                style = MaterialTheme.typography.bodySmall,
                                color = AquaPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    is CommandStatus.WaitingForConfirmation -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFD97706)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = commandStatus.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    is CommandStatus.Confirmed -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(EcoGreenContainer)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EcoGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = commandStatus.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onClearCommandStatus) {
                                Text("OK", color = Color(0xFF166534), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is CommandStatus.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AlertRedContainer)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = commandStatus.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = AlertRed,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onClearCommandStatus) {
                                Text("Dismiss", color = AlertRed, fontSize = 11.sp)
                            }
                        }
                    }
                    CommandStatus.Idle -> {}
                }
            }
        }
    }
}

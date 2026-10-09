package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceItem
import com.example.data.model.SystemStatusSummary
import com.example.data.model.WeatherInfo
import com.example.data.repository.CommandStatus
import com.example.data.repository.DataSourceMode
import com.example.ui.components.ConnectionStatusBar
import com.example.ui.components.IrrigationLogicMatrixCard
import com.example.ui.components.ManualPumpControlCard
import com.example.ui.components.PumpStatusCard
import com.example.ui.components.RainStatusCard
import com.example.ui.components.SoilMoistureCard
import com.example.ui.components.SystemStatusCard
import com.example.ui.components.WaterTankIndicator
import com.example.ui.components.WeatherAiCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.AquaPrimaryContainer
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium

@Composable
fun DashboardScreen(
    sensorData: AquaSustainData,
    connectionStatus: ConnectionStatus,
    systemStatus: SystemStatusSummary,
    deviceMetadata: DeviceItem,
    weatherInfo: WeatherInfo,
    commandStatus: CommandStatus,
    formattedElapsed: String,
    dataSourceMode: DataSourceMode,
    onSendPumpCommand: (Boolean) -> Unit,
    onSetMode: (String) -> Unit,
    onClearCommandStatus: () -> Unit,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showSignOutConfirm by remember { mutableStateOf(false) }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = {
                Text(
                    text = "Sign Out",
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out from AquaSustain?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    modifier = Modifier.testTag("confirm_dashboard_sign_out_button")
                ) {
                    Text("SIGN OUT", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("CANCEL", color = NeutralMedium, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AquaPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "AquaSustain Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AquaSustain",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = AquaPrimary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Save Rain • Save Water • Sustain Agriculture",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = EcoGreen
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Endpoint & Cloud Settings Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                ) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configure Arduino & Cloud",
                            tint = NeutralDark
                        )
                    }
                }

                // Quick Sign Out / Logout Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                ) {
                    IconButton(
                        onClick = { showSignOutConfirm = true },
                        modifier = Modifier.testTag("dashboard_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = AlertRed
                        )
                    }
                }
            }
        }

        // Connection State Indicator (🟢 Online / 🟠 Connecting / 🔴 Offline)
        DeviceConnectionBanner(
            dataSourceMode = dataSourceMode,
            device = deviceMetadata,
            connectionStatus = connectionStatus,
            formattedElapsed = formattedElapsed,
            onClick = onOpenSettings
        )

        // System Hardware & Network Status Bar
        ConnectionStatusBar(
            connectionStatus = connectionStatus,
            systemStatus = systemStatus,
            formattedElapsed = formattedElapsed,
            pumpActive = sensorData.pump,
            systemStatusString = sensorData.resolvedSystemStatus
        )

        // Live Controller System Decision Status Card
        SystemStatusCard(
            sensorData = sensorData
        )

        // 1. Prominent Pump Status Card
        PumpStatusCard(
            isPumpOn = sensorData.pump
        )

        // 2. Irrigation Control (Auto vs Manual & Pump Commands)
        ManualPumpControlCard(
            sensorData = sensorData,
            commandStatus = commandStatus,
            onSendPumpCommand = onSendPumpCommand,
            onSetMode = onSetMode,
            onClearCommandStatus = onClearCommandStatus
        )

        // 3. Live Sensor Monitoring Grid (Rain D2 & Soil Moisture A0)
        Text(
            text = "LIVE SENSOR MONITORING",
            style = MaterialTheme.typography.labelSmall,
            color = NeutralMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RainStatusCard(
                isRainDetected = sensorData.rain,
                modifier = Modifier.weight(1f)
            )

            SoilMoistureCard(
                soilMoisturePercent = sensorData.soilMoisture,
                soilStatus = sensorData.soilStatus,
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Underground Water Storage Tank Indicator (A1 Analog)
        WaterTankIndicator(
            waterLevelPercent = sensorData.waterLevel,
            tankCategory = sensorData.tankLevelCategory
        )

        // 5. Weather & AI Smart Advisory Section
        WeatherAiCard(
            weather = weatherInfo
        )

        // 6. Existing Irrigation Logic Truth Table Visualizer
        IrrigationLogicMatrixCard(
            sensorData = sensorData
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DeviceConnectionBanner(
    dataSourceMode: DataSourceMode,
    device: DeviceItem,
    connectionStatus: ConnectionStatus,
    formattedElapsed: String,
    onClick: () -> Unit
) {
    val isOnline = when (connectionStatus) {
        is ConnectionStatus.Connected -> device.online
        else -> false
    }

    val (statusDotColor, statusText, statusIcon) = when {
        isOnline -> Triple(
            Color(0xFF22C55E),
            "🟢 Online • ${device.name} (${device.board})",
            Icons.Default.CloudDone
        )
        connectionStatus is ConnectionStatus.Disconnected -> Triple(
            Color(0xFFF59E0B),
            "🟠 Connecting to ${device.deviceId}...",
            Icons.Default.CloudQueue
        )
        else -> Triple(
            AlertRed,
            "🔴 Offline • Last seen $formattedElapsed",
            Icons.Default.CloudOff
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isOnline) AquaPrimaryContainer else AlertRedContainer)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(statusDotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isOnline) Color(0xFF0F766E) else AlertRed
            )
        }

        Text(
            text = "Configure",
            style = MaterialTheme.typography.labelSmall,
            color = NeutralMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

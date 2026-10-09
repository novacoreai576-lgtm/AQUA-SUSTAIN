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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
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
import com.example.data.repository.CommandStatus
import com.example.ui.components.IrrigationLogicMatrixCard
import com.example.ui.components.ManualPumpControlCard
import com.example.ui.components.PumpStatusCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium

@Composable
fun IrrigationScreen(
    sensorData: AquaSustainData,
    commandStatus: CommandStatus,
    onSendPumpCommand: (Boolean) -> Unit,
    onSetMode: (String) -> Unit,
    onClearCommandStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("irrigation_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title
        Column {
            Text(
                text = "Irrigation Control",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AquaPrimary
            )
            Text(
                text = "Active LOW Relay (Pin D7) • Autonomous & Manual Operation",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMedium
            )
        }

        // Prominent Pump Status Card
        PumpStatusCard(isPumpOn = sensorData.pump)

        // Manual vs Auto Pump Control Card
        ManualPumpControlCard(
            sensorData = sensorData,
            commandStatus = commandStatus,
            onSendPumpCommand = onSendPumpCommand,
            onSetMode = onSetMode,
            onClearCommandStatus = onClearCommandStatus
        )

        // Irrigation Decision Rules & Truth Table
        IrrigationLogicMatrixCard(sensorData = sensorData)

        // Relay Hardware Specification Card
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = AlertRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Actuator Relay Specification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AlertRedContainer)
                        .border(1.dp, AlertRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "RELAY IS ACTIVE LOW (Pin D7):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = AlertRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• LOW (0V) -> Relay Energized -> Water Pump ON\n• HIGH (5V) -> Relay De-energized -> Water Pump OFF",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralDark,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

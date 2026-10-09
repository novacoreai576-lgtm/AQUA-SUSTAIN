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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Sensors
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
import com.example.ui.components.RainStatusCard
import com.example.ui.components.SoilMoistureCard
import com.example.ui.components.WaterTankIndicator
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.AquaPrimaryContainer
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue

@Composable
fun SensorsScreen(
    sensorData: AquaSustainData,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("sensors_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title
        Column {
            Text(
                text = "Sensors & Storage",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AquaPrimary
            )
            Text(
                text = "Real-time Telemetry from Arduino UNO R4 WiFi",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMedium
            )
        }

        // Live Sensors Grid
        Text(
            text = "PRIMARY SENSING NODES",
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

        // Underground Water Storage Tank Indicator
        WaterTankIndicator(
            waterLevelPercent = sensorData.waterLevel,
            tankCategory = sensorData.tankLevelCategory
        )

        // Hardware Pinout Reference
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hardware Pin Mapping",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                }

                SensorPinRow(
                    pin = "D2",
                    sensor = "Rain Sensor Module",
                    description = "Digital Input (LOW = Rain Detected, HIGH = No Rain)",
                    color = WaterBlue
                )

                SensorPinRow(
                    pin = "A0",
                    sensor = "Capacitive Soil Moisture",
                    description = "Analog Input (analogRead mapped 1023..0 to 0..100%)",
                    color = EcoGreen
                )

                SensorPinRow(
                    pin = "A1",
                    sensor = "Water Level Sensor",
                    description = "Analog Input (analogRead mapped 0..1023 to 0..100%)",
                    color = AquaPrimary
                )

                SensorPinRow(
                    pin = "D7",
                    sensor = "Relay Module (Water Pump)",
                    description = "Digital Output (LOW = Pump ON, HIGH = Pump OFF)",
                    color = AlertRed
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SensorPinRow(
    pin: String,
    sensor: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = pin, fontWeight = FontWeight.Black, color = color, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = sensor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeutralDark)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = NeutralMedium, fontSize = 11.5.sp)
        }
    }
}

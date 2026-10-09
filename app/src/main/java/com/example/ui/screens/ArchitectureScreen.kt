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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Router
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.AquaPrimaryContainer
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueContainer

@Composable
fun ArchitectureScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("architecture_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Column {
            Text(
                text = "System Architecture",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AquaPrimary
            )
            Text(
                text = "Hardware Pinout • Active LOW Relay • Arduino Firmware",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMedium
            )
        }

        // Prominent "Blynk is NOT used" Callout
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AlertRedContainer),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AlertRed.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AlertRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "No Blynk",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Blynk MUST NOT be used.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = AlertRed
                    )
                    Text(
                        text = "AquaSustain operates via direct, private Wi-Fi communication with the Arduino UNO R4 WiFi. No Blynk Cloud, third-party cloud brokers, or paid IoT subscriptions are involved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralDark,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Hardware Pinout Configuration Card
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
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = AquaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ARDUINO UNO R4 WIFI CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeutralMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Exact Physical Pin Mapping",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                PinItem(
                    pin = "D2",
                    sensor = "Rain Sensor",
                    logic = "Digital Input: LOW = Rain, HIGH = No Rain",
                    color = WaterBlue,
                    bgColor = WaterBlueContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                PinItem(
                    pin = "A0",
                    sensor = "Soil Moisture Sensor",
                    logic = "Analog Input: analogRead(A0), mapped 1023..0 to 0..100%",
                    color = EcoGreen,
                    bgColor = EcoGreenContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                PinItem(
                    pin = "A1",
                    sensor = "Water Level Sensor",
                    logic = "Analog Input: analogRead(A1), mapped 0..1023 to 0..100%",
                    color = AquaPrimary,
                    bgColor = AquaPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                PinItem(
                    pin = "D7",
                    sensor = "Relay / Pump Control",
                    logic = "Digital Output (ACTIVE LOW): LOW = Pump ON, HIGH = Pump OFF",
                    color = AlertRed,
                    bgColor = AlertRedContainer
                )
            }
        }

        // Existing Irrigation Logic Card
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
                    .padding(18.dp)
            ) {
                Text(
                    text = "AQUASUSTAIN IRRIGATION DECISION RULES",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                RuleItem(
                    number = "1",
                    condition = "Rain detected",
                    outcome = "Pump OFF",
                    detail = "Precipitation provides natural moisture; relay held HIGH (OFF)."
                )

                Spacer(modifier = Modifier.height(8.dp))

                RuleItem(
                    number = "2",
                    condition = "No rain + soil moisture < 50%",
                    outcome = "Pump ON",
                    detail = "Dry root zone triggers relay LOW (ON) to activate water pump."
                )

                Spacer(modifier = Modifier.height(8.dp))

                RuleItem(
                    number = "3",
                    condition = "No rain + soil moisture ≥ 50%",
                    outcome = "Pump OFF",
                    detail = "Adequate moisture present; relay held HIGH (OFF) to conserve water."
                )
            }
        }

        // Communication Architecture Pipeline Diagram
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "COMMUNICATION ARCHITECTURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                ArchitectureNode(
                    icon = Icons.Default.Sensors,
                    title = "Sensors Layer",
                    subtitle = "Rain Sensor (D2) + Soil Moisture (A0) + Water Level (A1)",
                    tag = "Hardware Input",
                    color = WaterBlue,
                    bgColor = WaterBlueContainer
                )

                DownwardArrow()

                ArchitectureNode(
                    icon = Icons.Default.DeveloperBoard,
                    title = "Arduino UNO R4 WiFi",
                    subtitle = "Main Controller • Active LOW Relay (D7) • HTTP Server",
                    tag = "Core Controller",
                    color = AquaPrimary,
                    bgColor = AquaPrimaryContainer
                )

                DownwardArrow()

                ArchitectureNode(
                    icon = Icons.Default.Router,
                    title = "Wi-Fi Network",
                    subtitle = "Standard Wi-Fi Router (No Mobile Hotspot Required)",
                    tag = "Transport Layer",
                    color = Color(0xFF7C3AED),
                    bgColor = Color(0xFFEDE9FE)
                )

                DownwardArrow()

                ArchitectureNode(
                    icon = Icons.Default.Cloud,
                    title = "Cloud Firestore (aqua-sustain)",
                    subtitle = "Real-time Telemetry • users/{uid}/devices • Pump Commands",
                    tag = "Cloud Database",
                    color = Color(0xFFF59E0B),
                    bgColor = Color(0xFFFEF3C7)
                )

                DownwardArrow()

                ArchitectureNode(
                    icon = Icons.Default.PhoneAndroid,
                    title = "AquaSustain App",
                    subtitle = "Live Dashboard • Monitoring • Tank & Pump Visualizer",
                    tag = "Presentation",
                    color = EcoGreen,
                    bgColor = EcoGreenContainer
                )
            }
        }

        // Arduino REST API Specification
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = AquaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Arduino REST API Endpoints",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "1. GET /api/status",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Returns live telemetry and current system status string:",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                SelectionContainer {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = """{
  "rain": false,
  "rainStatus": "NO RAIN",
  "soilMoisture": 35,
  "waterLevel": 72,
  "pump": true,
  "pumpStatus": "ON",
  "systemStatus": "NO RAIN + SOIL < 50% - IRRIGATION ON"
}""",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "2. GET /api/ping",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lightweight connectivity health check:",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                SelectionContainer {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = """{
  "status": "pong",
  "device": "Arduino UNO R4 WiFi",
  "online": true
}""",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFFA7F3D0),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Correct Arduino UNO R4 WiFi Firmware Code
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
                    text = "ARDUINO UNO R4 WIFI FIRMWARE CODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Exact sketch using WiFiS3 with active LOW relay on D7, analog water level on A1, soil moisture on A0, and rain on D2:",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )

                Spacer(modifier = Modifier.height(10.dp))

                SelectionContainer {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = """#include "WiFiS3.h"

// Wi-Fi Configuration (Standard Router)
char ssid[] = "Your_Farm_WiFi";
char pass[] = "Your_WiFi_Password";
WiFiServer server(80);

// Hardware Pin Configuration
const int RAIN_PIN  = 2;   // Rain Sensor -> D2
const int SOIL_PIN  = A0;  // Soil Moisture -> A0
const int WATER_PIN = A1;  // Water Level -> A1
const int RELAY_PIN = 7;   // Relay / Pump -> D7

// Relay is ACTIVE LOW:
const int RELAY_ON  = LOW;   // LOW = Pump ON
const int RELAY_OFF = HIGH;  // HIGH = Pump OFF

void setup() {
  Serial.begin(115200);
  pinMode(RAIN_PIN, INPUT);
  pinMode(SOIL_PIN, INPUT);
  pinMode(WATER_PIN, INPUT);
  pinMode(RELAY_PIN, OUTPUT);

  // Initialize Relay in OFF state
  digitalWrite(RELAY_PIN, RELAY_OFF);

  Serial.print("Connecting to Wi-Fi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, pass);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWi-Fi Connected!");
  Serial.print("Arduino IP: ");
  Serial.println(WiFi.localIP());

  server.begin();
}

void loop() {
  // 1. Read Sensors
  // Rain sensor: LOW = Rain, HIGH = No Rain
  bool rainDetected = (digitalRead(RAIN_PIN) == LOW);

  // Soil sensor: analogRead(A0), map 1023..0 to 0..100%
  int soilRaw = analogRead(SOIL_PIN);
  int soilMoisture = map(soilRaw, 1023, 0, 0, 100);
  soilMoisture = constrain(soilMoisture, 0, 100);

  // Water level: analogRead(A1), map 0..1023 to 0..100%
  int waterRaw = analogRead(WATER_PIN);
  int waterLevel = map(waterRaw, 0, 1023, 0, 100);
  waterLevel = constrain(waterLevel, 0, 100);

  // 2. Existing Irrigation Logic
  // 1. Rain detected -> Pump OFF
  // 2. No rain + soil moisture < 50% -> Pump ON
  // 3. No rain + soil moisture >= 50% -> Pump OFF
  bool pumpOn = false;
  String rainStatusStr = rainDetected ? "RAIN DETECTED" : "NO RAIN";
  String pumpStatusStr = "";
  String systemStatus  = "";

  if (rainDetected) {
    pumpOn = false;
    pumpStatusStr = "OFF";
    systemStatus = "RAIN DETECTED - IRRIGATION OFF";
  } else if (soilMoisture < 50) {
    pumpOn = true;
    pumpStatusStr = "ON";
    systemStatus = "NO RAIN + SOIL < 50% - IRRIGATION ON";
  } else {
    pumpOn = false;
    pumpStatusStr = "OFF";
    systemStatus = "NO RAIN + SOIL >= 50% - IRRIGATION OFF";
  }

  // 3. Actuate ACTIVE LOW Relay
  digitalWrite(RELAY_PIN, pumpOn ? RELAY_ON : RELAY_OFF);

  // 4. Handle HTTP Clients (GET /api/status & GET /api/ping)
  WiFiClient client = server.available();
  if (client) {
    String requestLine = "";
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        if (c == '\n') break;
        requestLine += c;
      }
    }

    if (requestLine.indexOf("GET /api/ping") >= 0) {
      client.println("HTTP/1.1 200 OK");
      client.println("Content-Type: application/json");
      client.println("Access-Control-Allow-Origin: *");
      client.println("Connection: close");
      client.println();
      client.println("{\"status\":\"pong\",\"device\":\"Arduino UNO R4 WiFi\",\"online\":true}");
    } else {
      // Default: GET /api/status
      client.println("HTTP/1.1 200 OK");
      client.println("Content-Type: application/json");
      client.println("Access-Control-Allow-Origin: *");
      client.println("Connection: close");
      client.println();
      client.print("{\n");
      client.print("  \"rain\": "); client.print(rainDetected ? "true" : "false"); client.print(",\n");
      client.print("  \"rainStatus\": \""); client.print(rainStatusStr); client.print("\",\n");
      client.print("  \"soilMoisture\": "); client.print(soilMoisture); client.print(",\n");
      client.print("  \"waterLevel\": "); client.print(waterLevel); client.print(",\n");
      client.print("  \"pump\": "); client.print(pumpOn ? "true" : "false"); client.print(",\n");
      client.print("  \"pumpStatus\": \""); client.print(pumpStatusStr); client.print("\",\n");
      client.print("  \"systemStatus\": \""); client.print(systemStatus); client.print("\"\n");
      client.print("}\n");
    }
    client.stop();
  }
}""",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = Color(0xFFA7F3D0),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PinItem(
    pin: String,
    sensor: String,
    logic: String,
    color: Color,
    bgColor: Color
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
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = pin,
                fontWeight = FontWeight.Black,
                color = color,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sensor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            )
            Text(
                text = logic,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                fontSize = 11.5.sp
            )
        }
    }
}

@Composable
private fun RuleItem(
    number: String,
    condition: String,
    outcome: String,
    detail: String
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
                .size(24.dp)
                .clip(CircleShape)
                .background(AquaPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = condition,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (outcome.contains("ON")) EcoGreen else NeutralDark)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = outcome,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                fontSize = 11.5.sp
            )
        }
    }
}

@Composable
private fun ArchitectureNode(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    color: Color,
    bgColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium
            )
        }
    }
}

@Composable
private fun DownwardArrow() {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = "Flows to",
            tint = AquaPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

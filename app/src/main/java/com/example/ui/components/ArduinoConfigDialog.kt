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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionStatus
import com.example.data.model.SystemStatusSummary
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium

@Composable
fun ArduinoConfigDialog(
    currentEndpoint: String,
    isTesting: Boolean,
    testFeedback: String?,
    connectionStatus: ConnectionStatus,
    systemStatus: SystemStatusSummary,
    onSaveEndpoint: (String) -> Unit,
    onConnect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var ipInput by remember { mutableStateOf(currentEndpoint) }
    var showSketchDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isConnected = connectionStatus is ConnectionStatus.Connected

    if (showSketchDialog) {
        ArduinoSketchViewerDialog(onDismiss = { showSketchDialog = false })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AquaPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperBoard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Arduino Connection",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                        Text(
                            text = "Local Wi-Fi REST API (Port 80)",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeutralMedium
                        )
                    }
                }

                // Live status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isConnected) Color(0xFFDCFCE7) else AlertRedContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isConnected) "CONNECTED" else "DISCONNECTED",
                        color = if (isConnected) EcoGreen else AlertRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = null,
                            tint = AquaPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "The phone and Arduino UNO R4 WiFi must be connected to the SAME Wi-Fi network. Find the Arduino IP address in the Arduino IDE Serial Monitor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralDark,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Current System Status Display (Arduino, Wi-Fi, Sensors)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusMiniItem("Arduino", systemStatus.arduinoStatus, systemStatus.isArduinoOnline)
                    StatusMiniItem("Wi-Fi", systemStatus.wifiStatus, systemStatus.isWifiOnline)
                    StatusMiniItem("Sensors", systemStatus.sensorsStatus, systemStatus.areSensorsOnline)
                }

                // Arduino IP Address Field
                Column {
                    Text(
                        text = "Arduino IP Address",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = {
                            ipInput = it
                            onSaveEndpoint(it)
                        },
                        placeholder = { Text("192.168.43.120") },
                        leadingIcon = {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = AquaPrimary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onConnect(ipInput)
                            }
                        ),
                        supportingText = {
                            Text("Example: 192.168.43.120 (Target: http://$ipInput/data)")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("arduino_ip_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AquaPrimary,
                            focusedLabelColor = AquaPrimary
                        )
                    )
                }

                // CONNECT Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onConnect(ipInput)
                    },
                    enabled = !isTesting && ipInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("connect_button")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CONNECTING TO ARDUINO...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CONNECT", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }

                // Test Feedback / Error message banner
                testFeedback?.let { feedback ->
                    val isSuccess = feedback.startsWith("CONNECTED") || feedback.startsWith("Success")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSuccess) Color(0xFFDCFCE7) else AlertRedContainer)
                            .border(1.dp, if (isSuccess) EcoGreen.copy(alpha = 0.4f) else AlertRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isSuccess) EcoGreen else AlertRed,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feedback,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isSuccess) Color(0xFF166534) else AlertRed,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Divider(color = Color(0xFFE2E8F0))

                // Hardware Pins Reference
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ARDUINO PIN ASSIGNMENTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text("• Rain Sensor → Digital Pin D2", style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                    Text("• Soil Moisture Sensor → Analog Pin A0", style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                    Text("• Water Level Sensor → Analog Pin A1", style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                    Text("• Relay Module (Pump) → Digital Pin D7", style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                }

                OutlinedButton(
                    onClick = { showSketchDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Arduino UNO R4 WiFi Code")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveEndpoint(ipInput)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary),
                modifier = Modifier.testTag("save_ip_close_button")
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onConnect(ipInput)
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retry", color = AquaPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun StatusMiniItem(label: String, value: String, isOnline: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = NeutralMedium, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) EcoGreen else AlertRed)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = if (isOnline) EcoGreen else AlertRed,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ArduinoSketchViewerDialog(onDismiss: () -> Unit) {
    val sketchCode = """
// AquaSustain – Arduino UNO R4 WiFi
// Connects to local Wi-Fi and serves /data, /pump/on, /pump/off
#include <WiFiS3.h>

const char* ssid     = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

WiFiServer server(80);

const int PIN_RAIN  = 2;   // D2 - Digital Rain Sensor
const int PIN_SOIL  = A0;  // A0 - Analog Soil Moisture
const int PIN_WATER = A1;  // A1 - Analog Water Level
const int PIN_RELAY = 7;   // D7 - Relay Module (Pump)

bool currentPumpState = false;

void setup() {
  Serial.begin(115200);
  pinMode(PIN_RAIN, INPUT_PULLUP);
  pinMode(PIN_RELAY, OUTPUT);
  digitalWrite(PIN_RELAY, HIGH); // Relay OFF (active LOW)

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi connected!");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());

  server.begin();
}

void loop() {
  WiFiClient client = server.available();
  if (client) {
    String req = "";
    while (client.connected() && client.available()) {
      char c = client.read();
      if (req.length() < 100 && c != '\r' && c != '\n') req += c;
      if (c == '\n') break;
    }
    
    if (req.indexOf("GET /data") >= 0) {
      bool rain = (digitalRead(PIN_RAIN) == LOW);
      int soil = map(analogRead(PIN_SOIL), 850, 350, 0, 100);
      soil = constrain(soil, 0, 100);
      int water = map(analogRead(PIN_WATER), 50, 650, 0, 100);
      water = constrain(water, 0, 100);

      client.println("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nAccess-Control-Allow-Origin: *\r\n");
      client.print("{\"rain\":"); client.print(rain ? "true" : "false");
      client.print(",\"soilMoisture\":"); client.print(soil);
      client.print(",\"waterLevel\":"); client.print(water);
      client.print(",\"pump\":"); client.print(currentPumpState ? "true" : "false");
      client.println("}");
    } else if (req.indexOf("GET /pump/on") >= 0) {
      currentPumpState = true;
      digitalWrite(PIN_RELAY, LOW); // ON
      client.println("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"pump\":true}");
    } else if (req.indexOf("GET /pump/off") >= 0) {
      currentPumpState = false;
      digitalWrite(PIN_RELAY, HIGH); // OFF
      client.println("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"pump\":false}");
    }
    delay(1);
    client.stop();
  }
}
""".trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Arduino UNO R4 WiFi Code", fontWeight = FontWeight.Bold, color = AquaPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Upload this sketch using the Arduino IDE with board 'Arduino UNO R4 WiFi'. Ensure baud rate is 115200 to view your IP in the Serial Monitor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp)
                ) {
                    Text(
                        text = sketchCode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 15.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
            ) {
                Text("Close")
            }
        }
    )
}

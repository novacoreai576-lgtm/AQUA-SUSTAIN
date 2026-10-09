/*
 * =====================================================================
 * AquaSustain – Smart Rainwater & Irrigation Management System
 * Target Hardware: Arduino UNO R4 WiFi
 * =====================================================================
 *
 * Pin Configuration:
 *   - Rain Sensor Digital Output  -> D2
 *   - Soil Moisture Analog Sensor -> A0
 *   - Water Level Analog Sensor   -> A1
 *   - Relay Module (Water Pump)   -> D7
 *
 * HTTP REST Endpoints:
 *   - GET /data     -> Returns sensor readings JSON:
 *                      {"rain": false, "soilMoisture": 43, "waterLevel": 65, "pump": false}
 *   - GET /pump/on  -> Activates relay, returns {"pump": true}
 *   - GET /pump/off -> Deactivates relay, returns {"pump": false}
 *
 * Connects directly to local Wi-Fi router (no cloud/Blynk required).
 * Android app connects directly to http://<ARDUINO_IP>/data
 * =====================================================================
 */

#include "WiFiS3.h"

// ==========================================
// 1. Wi-Fi Configuration
// Replace with your local Wi-Fi credentials
// ==========================================
char ssid[] = "YOUR_WIFI_SSID";         // Your Wi-Fi network SSID
char pass[] = "YOUR_WIFI_PASSWORD";     // Your Wi-Fi password

int status = WL_IDLE_STATUS;
WiFiServer server(80);

// ==========================================
// 2. Hardware Pin Definitions
// ==========================================
const int RAIN_PIN   = 2;   // D2 - Rain Sensor Digital Input
const int SOIL_PIN   = A0;  // A0 - Soil Moisture Analog Input
const int WATER_PIN  = A1;  // A1 - Water Level Analog Input
const int RELAY_PIN  = 7;   // D7 - Relay Module (Pump)

// Set to true if your relay triggers on HIGH, or false if active-LOW
const bool RELAY_ACTIVE_HIGH = true;

// Current actuator state
bool pumpState = false;

// ==========================================
// Helper: Set Relay / Pump State
// ==========================================
void setPump(bool enable) {
  pumpState = enable;
  if (RELAY_ACTIVE_HIGH) {
    digitalWrite(RELAY_PIN, enable ? HIGH : LOW);
  } else {
    digitalWrite(RELAY_PIN, enable ? LOW : HIGH);
  }
  Serial.print("Pump state set to: ");
  Serial.println(pumpState ? "ON" : "OFF");
}

// ==========================================
// Setup
// ==========================================
void setup() {
  Serial.begin(115200);
  while (!Serial) {
    ; // Wait for serial port to connect (optional)
  }

  Serial.println();
  Serial.println("=========================================");
  Serial.println("   AquaSustain - Arduino UNO R4 WiFi     ");
  Serial.println("=========================================");

  // Initialize Hardware Pins
  pinMode(RAIN_PIN, INPUT_PULLUP);
  pinMode(SOIL_PIN, INPUT);
  pinMode(WATER_PIN, INPUT);
  pinMode(RELAY_PIN, OUTPUT);

  // Default pump to OFF on boot
  setPump(false);

  // Check WiFi module
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    while (true); // Halt
  }

  // Attempt to connect to local Wi-Fi
  Serial.print("Connecting to Wi-Fi SSID: ");
  Serial.println(ssid);

  while (status != WL_CONNECTED) {
    status = WiFi.begin(ssid, pass);
    delay(4000);
    Serial.print(".");
  }

  // Connected!
  Serial.println();
  Serial.println("WiFi connected successfully!");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());

  // Start HTTP Server
  server.begin();
  Serial.println("HTTP server started on port 80");
  Serial.println("Ready to receive requests from AquaSustain App!");
  Serial.println("-----------------------------------------");
}

// ==========================================
// Main Loop
// ==========================================
void loop() {
  // Listen for incoming HTTP clients from the AquaSustain Android App
  WiFiClient client = server.available();

  if (client) {
    String currentLine = "";
    String requestLine = "";
    bool isFirstLine = true;

    while (client.connected()) {
      if (client.available()) {
        char c = client.read();

        if (c == '\n') {
          if (isFirstLine) {
            requestLine = currentLine;
            isFirstLine = false;
          }

          // If currentLine is empty, we reached end of HTTP request headers
          if (currentLine.length() == 0) {
            // Process the HTTP Request
            handleHttpRequest(client, requestLine);
            break;
          } else {
            currentLine = "";
          }
        } else if (c != '\r') {
          currentLine += c;
        }
      }
    }

    // Close the connection
    delay(5);
    client.stop();
  }
}

// ==========================================
// HTTP Request Router & Handler
// ==========================================
void handleHttpRequest(WiFiClient &client, String &request) {
  Serial.print("Request: ");
  Serial.println(request);

  // Route 1: GET /data
  if (request.indexOf("GET /data") >= 0) {
    // 1. Read Rain Sensor (D2): Most FC-37 sensors pull LOW when raindrops detected
    int rainRaw = digitalRead(RAIN_PIN);
    bool rainDetected = (rainRaw == LOW);

    // 2. Read Soil Moisture (A0): 0-1023 (calibrate to 0-100%)
    int soilRaw = analogRead(SOIL_PIN);
    // Typical resistive/capacitive: lower raw = wetter, higher raw = dry
    // Or invert depending on your specific probe:
    int soilMoisture = map(soilRaw, 1023, 200, 0, 100);
    soilMoisture = constrain(soilMoisture, 0, 100);

    // 3. Read Water Tank Level (A1): 0-1023 (calibrate to 0-100%)
    int waterRaw = analogRead(WATER_PIN);
    int waterLevel = map(waterRaw, 0, 700, 0, 100);
    waterLevel = constrain(waterLevel, 0, 100);

    // Send HTTP JSON Response
    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: application/json; charset=utf-8");
    client.println("Access-Control-Allow-Origin: *");
    client.println("Connection: close");
    client.println();

    // JSON Payload
    client.print("{");
    client.print("\"rain\":");
    client.print(rainDetected ? "true" : "false");
    client.print(",\"soilMoisture\":");
    client.print(soilMoisture);
    client.print(",\"waterLevel\":");
    client.print(waterLevel);
    client.print(",\"pump\":");
    client.print(pumpState ? "true" : "false");
    client.println("}");

    Serial.print("Sent Telemetry -> Rain: ");
    Serial.print(rainDetected ? "YES" : "NO");
    Serial.print(", Soil: ");
    Serial.print(soilMoisture);
    Serial.print("%, Tank: ");
    Serial.print(waterLevel);
    Serial.print("%, Pump: ");
    Serial.println(pumpState ? "ON" : "OFF");
  }
  // Route 2: GET /pump/on
  else if (request.indexOf("GET /pump/on") >= 0) {
    setPump(true);

    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: application/json; charset=utf-8");
    client.println("Access-Control-Allow-Origin: *");
    client.println("Connection: close");
    client.println();
    client.println("{\"pump\":true}");
  }
  // Route 3: GET /pump/off
  else if (request.indexOf("GET /pump/off") >= 0) {
    setPump(false);

    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: application/json; charset=utf-8");
    client.println("Access-Control-Allow-Origin: *");
    client.println("Connection: close");
    client.println();
    client.println("{\"pump\":false}");
  }
  // Route 4: Fallback / 404
  else {
    client.println("HTTP/1.1 404 Not Found");
    client.println("Content-Type: application/json; charset=utf-8");
    client.println("Connection: close");
    client.println();
    client.println("{\"error\":\"Endpoint not found. Use /data, /pump/on, or /pump/off\"}");
  }
}

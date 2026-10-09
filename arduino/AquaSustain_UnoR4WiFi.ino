/*
 * AquaSustain – Smart Rainwater & Irrigation Management System
 * Target Hardware: Arduino UNO R4 WiFi
 *
 * Hardware Pin Connections:
 * - Rain Sensor         -> Digital Pin D2 (Digital output of FC-37 / LM393 module)
 * - Soil Moisture Sensor-> Analog Pin A0  (Capacitive or resistive analog probe)
 * - Water Level Sensor  -> Analog Pin A1  (Submersible water depth level sensor)
 * - Relay Module (Pump) -> Digital Pin D7 (Controls 5V/12V submersible water pump)
 *
 * Arduino Local HTTP Server Endpoints:
 * - GET /data     -> Returns live JSON with rain, soilMoisture, waterLevel, pump
 * - GET /pump/on  -> Activates relay on D7, returns {"pump": true}
 * - GET /pump/off -> Deactivates relay on D7, returns {"pump": false}
 */

#include <WiFiS3.h>

// ==========================================
// 1. Wi-Fi Configuration
// NOTE: Arduino and Android phone must be on the SAME Wi-Fi network
// ==========================================
const char* ssid     = "YOUR_WIFI_SSID";     // Enter your Wi-Fi SSID
const char* password = "YOUR_WIFI_PASSWORD"; // Enter your Wi-Fi password

WiFiServer server(80);

// ==========================================
// 2. Hardware Pin Assignments (DO NOT CHANGE)
// ==========================================
const int PIN_RAIN  = 2;   // D2 - Digital Rain Sensor
const int PIN_SOIL  = A0;  // A0 - Analog Soil Moisture
const int PIN_WATER = A1;  // A1 - Analog Water Level
const int PIN_RELAY = 7;   // D7 - Relay Module (Water Pump)

// Relay Active State:
// Standard Arduino relay modules are ACTIVE LOW:
// LOW = Relay ON (Pump running), HIGH = Relay OFF (Pump stopped)
const int RELAY_ON  = LOW;
const int RELAY_OFF = HIGH;

bool currentPumpState = false;

// Calibration variables (tune according to physical sensors)
// Soil: dry in air (~800-1023), wet in water (~300-450)
const int SOIL_DRY_RAW = 850;
const int SOIL_WET_RAW = 350;

// Water level: empty (~0-50), full depth (~600-750)
const int WATER_EMPTY_RAW = 50;
const int WATER_FULL_RAW  = 650;

void setup() {
  Serial.begin(115200);
  while (!Serial && millis() < 3000); // Wait for Serial Monitor

  Serial.println("\n==========================================");
  Serial.println("  AquaSustain - Arduino UNO R4 WiFi       ");
  Serial.println("==========================================");

  // Configure Pins
  pinMode(PIN_RAIN, INPUT_PULLUP);
  pinMode(PIN_RELAY, OUTPUT);

  // Initialize relay to OFF (pump stopped)
  digitalWrite(PIN_RELAY, RELAY_OFF);
  currentPumpState = false;

  // Check for WiFi module
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("ERROR: Communication with WiFi module failed!");
    while (true);
  }

  // Connect to Wi-Fi
  Serial.print("Connecting to Wi-Fi network: ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 30) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi connected successfully!");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
    Serial.print("Signal Strength (RSSI): ");
    Serial.print(WiFi.RSSI());
    Serial.println(" dBm");

    // Start HTTP Server
    server.begin();
    Serial.println("HTTP server started on port 80");
    Serial.println("\nAvailable Endpoints:");
    Serial.print("  http://"); Serial.print(WiFi.localIP()); Serial.println("/data");
    Serial.print("  http://"); Serial.print(WiFi.localIP()); Serial.println("/pump/on");
    Serial.print("  http://"); Serial.print(WiFi.localIP()); Serial.println("/pump/off");
    Serial.println("==========================================\n");
  } else {
    Serial.println("\nERROR: Failed to connect to Wi-Fi. Check SSID and Password.");
  }
}

void loop() {
  // Check for incoming HTTP clients
  WiFiClient client = server.available();
  if (client) {
    handleHttpClient(client);
  }
}

// ==========================================
// Read Sensor Values
// ==========================================
bool readRainSensor() {
  // Standard digital rain sensor modules output LOW when water/rain is detected
  int raw = digitalRead(PIN_RAIN);
  return (raw == LOW);
}

int readSoilMoisture() {
  int raw = analogRead(PIN_SOIL);
  // Map raw analog reading to 0-100%
  int percent = map(raw, SOIL_DRY_RAW, SOIL_WET_RAW, 0, 100);
  return constrain(percent, 0, 100);
}

int readWaterLevel() {
  int raw = analogRead(PIN_WATER);
  // Map raw analog reading to 0-100%
  int percent = map(raw, WATER_EMPTY_RAW, WATER_FULL_RAW, 0, 100);
  return constrain(percent, 0, 100);
}

void setPump(bool enable) {
  currentPumpState = enable;
  digitalWrite(PIN_RELAY, enable ? RELAY_ON : RELAY_OFF);
  Serial.print("[PUMP] Command executed. Pump is now: ");
  Serial.println(enable ? "ON" : "OFF");
}

// ==========================================
// HTTP Request Dispatcher
// ==========================================
void handleHttpClient(WiFiClient& client) {
  String requestLine = "";
  boolean currentLineIsBlank = true;

  while (client.connected()) {
    if (client.available()) {
      char c = client.read();
      if (requestLine.length() < 120 && c != '\r' && c != '\n') {
        requestLine += c;
      }

      if (c == '\n' && currentLineIsBlank) {
        // Headers ended, process the HTTP request
        if (requestLine.indexOf("GET /data") >= 0) {
          sendDataResponse(client);
        } else if (requestLine.indexOf("GET /pump/on") >= 0) {
          setPump(true);
          sendPumpResponse(client, true);
        } else if (requestLine.indexOf("GET /pump/off") >= 0) {
          setPump(false);
          sendPumpResponse(client, false);
        } else {
          // Unknown endpoint 404
          client.println("HTTP/1.1 404 Not Found");
          client.println("Content-Type: application/json");
          client.println("Connection: close");
          client.println();
          client.println("{\"error\": \"Endpoint not found. Use /data, /pump/on, /pump/off\"}");
        }
        break;
      }

      if (c == '\n') {
        currentLineIsBlank = true;
      } else if (c != '\r') {
        currentLineIsBlank = false;
      }
    }
  }

  delay(1);
  client.stop();
}

void sendDataResponse(WiFiClient& client) {
  bool rain = readRainSensor();
  int soil = readSoilMoisture();
  int water = readWaterLevel();

  // Send HTTP Header
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: application/json");
  client.println("Access-Control-Allow-Origin: *");
  client.println("Connection: close");
  client.println();

  // Send JSON Body:
  // {"rain": false, "soilMoisture": 43, "waterLevel": 65, "pump": true}
  client.print("{\"rain\":");
  client.print(rain ? "true" : "false");
  client.print(",\"soilMoisture\":");
  client.print(soil);
  client.print(",\"waterLevel\":");
  client.print(water);
  client.print(",\"pump\":");
  client.print(currentPumpState ? "true" : "false");
  client.println("}");

  Serial.print("[DATA] Dispatched: Rain=");
  Serial.print(rain ? "YES" : "NO");
  Serial.print(", Soil=");
  Serial.print(soil);
  Serial.print("%, Water=");
  Serial.print(water);
  Serial.print("%, Pump=");
  Serial.println(currentPumpState ? "ON" : "OFF");
}

void sendPumpResponse(WiFiClient& client, bool state) {
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: application/json");
  client.println("Access-Control-Allow-Origin: *");
  client.println("Connection: close");
  client.println();

  client.print("{\"pump\":");
  client.print(state ? "true" : "false");
  client.println("}");
}

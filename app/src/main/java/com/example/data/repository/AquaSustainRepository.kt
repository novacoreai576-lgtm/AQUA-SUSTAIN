package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.firebase.FirebaseManager
import com.example.data.model.AquaSustainData
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceItem
import com.example.data.model.GeoLocation
import com.example.data.model.SensorReading
import com.example.data.model.SystemStatusSummary
import com.example.data.model.WeatherInfo
import com.example.data.remote.ArduinoHttpClient
import com.example.data.remote.WeatherApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class DataSourceMode {
    LOCAL_ARDUINO_WIFI,
    FIREBASE_CLOUD
}

sealed interface CommandStatus {
    data object Idle : CommandStatus
    data class Sending(val command: String) : CommandStatus
    data class WaitingForConfirmation(val command: String, val message: String = "Sending HTTP request to Arduino...") : CommandStatus
    data class Confirmed(val command: String, val message: String) : CommandStatus
    data class Error(val message: String) : CommandStatus
}

class AquaSustainRepository(
    context: Context? = null,
    val firebaseManager: FirebaseManager = FirebaseManager(),
    private val httpClient: ArduinoHttpClient = ArduinoHttpClient(),
    private val weatherApiClient: WeatherApiClient = WeatherApiClient(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    companion object {
        private const val PREFS_NAME = "aquasustain_arduino_prefs"
        private const val KEY_ARDUINO_IP = "saved_arduino_ip"
        const val DEFAULT_ARDUINO_IP = "192.168.43.120"

        private const val KEY_WEATHER_LAT = "weather_latitude"
        private const val KEY_WEATHER_LON = "weather_longitude"
        private const val KEY_WEATHER_LOC_NAME = "weather_location_name"
        const val DEFAULT_WEATHER_LAT = 28.6139
        const val DEFAULT_WEATHER_LON = 77.2090
        const val DEFAULT_WEATHER_LOC_NAME = "New Delhi (Farm Station)"
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Saved Arduino IP / Endpoint
    private val _endpointUrl = MutableStateFlow(
        prefs?.getString(KEY_ARDUINO_IP, DEFAULT_ARDUINO_IP) ?: DEFAULT_ARDUINO_IP
    )
    val endpointUrl: StateFlow<String> = _endpointUrl.asStateFlow()

    // Active Data Source Mode
    private val _dataSourceMode = MutableStateFlow(DataSourceMode.LOCAL_ARDUINO_WIFI)
    val dataSourceMode: StateFlow<DataSourceMode> = _dataSourceMode.asStateFlow()

    // Polling Interval: 2.5 seconds (every 2-3 seconds per user request)
    private val _pollingIntervalMs = MutableStateFlow(2500L)
    val pollingIntervalMs: StateFlow<Long> = _pollingIntervalMs.asStateFlow()

    // Live Telemetry Stream
    private val _sensorData = MutableStateFlow(
        AquaSustainData(
            rain = false,
            rainStatus = "No Rain",
            soilMoisture = 43,
            waterLevel = 65,
            pump = false,
            pumpStatus = "OFF",
            systemStatus = "CONNECTING TO ARDUINO...",
            mode = "auto"
        )
    )
    val sensorData: StateFlow<AquaSustainData> = _sensorData.asStateFlow()

    // Connection Status
    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    // System Status Summary matching required labels:
    // Arduino: CONNECTED / DISCONNECTED
    // Wi-Fi: CONNECTED / DISCONNECTED
    // Sensors: ONLINE / OFFLINE
    private val _systemStatus = MutableStateFlow(
        SystemStatusSummary(
            arduinoStatus = "DISCONNECTED",
            isArduinoOnline = false,
            wifiStatus = "DISCONNECTED",
            isWifiOnline = false,
            sensorsStatus = "OFFLINE",
            areSensorsOnline = false,
            lastUpdatedEpochMs = 0L
        )
    )
    val systemStatus: StateFlow<SystemStatusSummary> = _systemStatus.asStateFlow()

    // Device Metadata (AQUA-001)
    private val _deviceMetadata = MutableStateFlow(
        DeviceItem(
            deviceId = "AQUA-001",
            name = "AquaSustain Farm",
            board = "Arduino UNO R4 WiFi",
            online = false
        )
    )
    val deviceMetadata: StateFlow<DeviceItem> = _deviceMetadata.asStateFlow()

    // Historical Sensor Readings (from Firebase Firestore)
    private val _sensorHistory = MutableStateFlow<List<SensorReading>>(emptyList())
    val sensorHistory: StateFlow<List<SensorReading>> = _sensorHistory.asStateFlow()

    // Weather & AI Recommendations
    private val _weatherInfo = MutableStateFlow(
        WeatherInfo(
            temperatureC = 28,
            condition = "Partly Cloudy",
            rainProbabilityPercent = 35,
            rainPredictionText = "Low precipitation probability",
            smartRecommendation = "Stable atmospheric conditions. System maintaining automated soil moisture balance.",
            humidityPercent = 65,
            locationName = prefs?.getString(KEY_WEATHER_LOC_NAME, DEFAULT_WEATHER_LOC_NAME) ?: DEFAULT_WEATHER_LOC_NAME
        )
    )
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()

    private val _isRefreshingWeather = MutableStateFlow(false)
    val isRefreshingWeather: StateFlow<Boolean> = _isRefreshingWeather.asStateFlow()

    private val _weatherSearchResults = MutableStateFlow<List<GeoLocation>>(WeatherApiClient.POPULAR_FARM_LOCATIONS)
    val weatherSearchResults: StateFlow<List<GeoLocation>> = _weatherSearchResults.asStateFlow()

    private val _isSearchingWeatherLocation = MutableStateFlow(false)
    val isSearchingWeatherLocation: StateFlow<Boolean> = _isSearchingWeatherLocation.asStateFlow()

    private val _weatherSearchQuery = MutableStateFlow("")
    val weatherSearchQuery: StateFlow<String> = _weatherSearchQuery.asStateFlow()

    private val _weatherErrorMessage = MutableStateFlow<String?>(null)
    val weatherErrorMessage: StateFlow<String?> = _weatherErrorMessage.asStateFlow()

    // Pump Command State
    private val _commandStatus = MutableStateFlow<CommandStatus>(CommandStatus.Idle)
    val commandStatus: StateFlow<CommandStatus> = _commandStatus.asStateFlow()

    // Error messages
    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    private var pollingJob: Job? = null
    private var firestoreSyncJob: Job? = null
    private var lastSuccessfulSyncMs: Long = 0L

    init {
        // Initial live weather fetch & periodic refresh
        refreshWeather()
        scope.launch {
            while (isActive) {
                delay(20 * 60 * 1000L) // every 20 minutes
                refreshWeather()
            }
        }

        // Observe Firebase Auth state changes
        scope.launch {
            firebaseManager.currentUser.collect { user ->
                if (user != null) {
                    // Start observing user history & weather from Firestore
                    startFirestoreObservation()
                    // Refresh weather on login
                    refreshWeather()
                    // Automatic connection to Arduino on startup/login
                    startPolling()
                } else {
                    stopPolling()
                    stopFirestoreObservation()
                    disconnectSession()
                }
            }
        }
    }

    private var consecutiveFailures = 0

    /**
     * Updates and locally saves the Arduino IP address.
     */
    fun saveArduinoIp(ip: String) {
        val cleanIp = ip.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .removeSuffix("/data")
            .removeSuffix("/")
        _endpointUrl.value = cleanIp
        prefs?.edit()?.putString(KEY_ARDUINO_IP, cleanIp)?.apply()
        consecutiveFailures = 0
    }

    /**
     * Automatic Connection & Polling Loop:
     * Polls the Arduino every 2–3 seconds via GET http://<IP>/data.
     * Backs off to 15 seconds if consecutive failures occur (e.g. device offline/different Wi-Fi).
     */
    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = scope.launch {
            while (isActive) {
                executeArduinoPoll()
                val nextDelay = if (consecutiveFailures >= 3) {
                    15_000L
                } else {
                    _pollingIntervalMs.value
                }
                delay(nextDelay)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Executes single HTTP poll to Arduino UNO R4 WiFi at GET http://<IP>/data
     */
    private suspend fun executeArduinoPoll() {
        val ip = _endpointUrl.value
        val result = httpClient.fetchData(ip)

        result.onSuccess { rawData ->
            consecutiveFailures = 0
            val data = rawData.sanitized()
            lastSuccessfulSyncMs = System.currentTimeMillis()
            _sensorData.value = data.copy(updatedAtEpochMs = lastSuccessfulSyncMs)
            _connectionStatus.value = ConnectionStatus.Connected
            _lastErrorMessage.value = null

            // Update Device Status & Hardware Summary
            _deviceMetadata.value = _deviceMetadata.value.copy(online = true)
            _systemStatus.value = SystemStatusSummary(
                arduinoStatus = "CONNECTED",
                isArduinoOnline = true,
                wifiStatus = "CONNECTED",
                isWifiOnline = true,
                sensorsStatus = "ONLINE",
                areSensorsOnline = true,
                lastUpdatedEpochMs = lastSuccessfulSyncMs
            )

            // Autonomous Irrigation Logic:
            // IF rain is detected: Pump OFF
            // ELSE IF water level < 20%: Pump OFF
            // ELSE IF soil moisture < 50%: Pump ON
            // ELSE: Pump OFF
            if (data.isAutoMode) {
                val targetPump = data.targetAutoPumpState
                if (data.pump != targetPump) {
                    httpClient.setPump(ip, targetPump)
                }
            }

            // Cloud Firestore Sync:
            // Store sensor history in sensorHistory/{currentUser.uid}/records/{recordId}
            if (firebaseManager.isUserLoggedIn) {
                firebaseManager.saveLiveSensorData(data)
            }

            // Handle pending pump command confirmations
            val curCmd = _commandStatus.value
            if (curCmd is CommandStatus.WaitingForConfirmation) {
                val expected = curCmd.command == "PUMP_ON"
                if (data.pump == expected) {
                    _commandStatus.value = CommandStatus.Confirmed(
                        curCmd.command,
                        "Arduino confirmed: Relay is ${if (expected) "ON" else "OFF"}"
                    )
                }
            }
        }.onFailure { error ->
            consecutiveFailures++
            _connectionStatus.value = ConnectionStatus.ConnectionLost(
                lastSeenEpochMs = lastSuccessfulSyncMs,
                message = "Arduino unreachable at $ip: ${error.localizedMessage}"
            )
            _lastErrorMessage.value = error.message
            _deviceMetadata.value = _deviceMetadata.value.copy(online = false)

            _systemStatus.value = SystemStatusSummary(
                arduinoStatus = "DISCONNECTED",
                isArduinoOnline = false,
                wifiStatus = "DISCONNECTED",
                isWifiOnline = false,
                sensorsStatus = "OFFLINE",
                areSensorsOnline = false,
                lastUpdatedEpochMs = lastSuccessfulSyncMs
            )

            // Do not show old sensor data as if it were live
            _sensorData.value = _sensorData.value.copy(
                pumpStatus = "DISCONNECTED",
                systemStatus = "ARDUINO DISCONNECTED - CHECK LOCAL WI-FI"
            )
        }
    }

    /**
     * Tests connectivity when user presses CONNECT button in Settings:
     * 1. Test http://<IP>/data
     * 2. If successful -> Arduino = CONNECTED
     * 3. If unsuccessful -> Arduino = DISCONNECTED
     */
    suspend fun testAndConnectArduino(ipOrUrl: String): Result<AquaSustainData> {
        val result = httpClient.fetchData(ipOrUrl)
        return if (result.isSuccess) {
            val data = result.getOrThrow()
            saveArduinoIp(ipOrUrl)
            lastSuccessfulSyncMs = System.currentTimeMillis()
            _sensorData.value = data.copy(updatedAtEpochMs = lastSuccessfulSyncMs)
            _connectionStatus.value = ConnectionStatus.Connected
            _lastErrorMessage.value = null
            _deviceMetadata.value = _deviceMetadata.value.copy(online = true)

            _systemStatus.value = SystemStatusSummary(
                arduinoStatus = "CONNECTED",
                isArduinoOnline = true,
                wifiStatus = "CONNECTED",
                isWifiOnline = true,
                sensorsStatus = "ONLINE",
                areSensorsOnline = true,
                lastUpdatedEpochMs = lastSuccessfulSyncMs
            )

            // Save to Firestore if user logged in
            if (firebaseManager.isUserLoggedIn) {
                firebaseManager.saveLiveSensorData(data)
            }

            // Ensure polling is active
            stopPolling()
            startPolling()

            Result.success(data)
        } else {
            _connectionStatus.value = ConnectionStatus.Disconnected
            _deviceMetadata.value = _deviceMetadata.value.copy(online = false)
            _systemStatus.value = SystemStatusSummary(
                arduinoStatus = "DISCONNECTED",
                isArduinoOnline = false,
                wifiStatus = "DISCONNECTED",
                isWifiOnline = false,
                sensorsStatus = "OFFLINE",
                areSensorsOnline = false,
                lastUpdatedEpochMs = lastSuccessfulSyncMs
            )
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to connect to Arduino"))
        }
    }

    /**
     * PUMP CONTROL:
     * Sends GET http://<IP>/pump/on or GET http://<IP>/pump/off to Arduino.
     */
    suspend fun requestPumpCommand(targetPumpOn: Boolean): Result<Unit> {
        val command = if (targetPumpOn) "PUMP_ON" else "PUMP_OFF"
        _commandStatus.value = CommandStatus.Sending(command)

        val ip = _endpointUrl.value
        _commandStatus.value = CommandStatus.WaitingForConfirmation(
            command,
            "Sending GET /pump/${if (targetPumpOn) "on" else "off"} to Arduino..."
        )

        val result = httpClient.setPump(ip, targetPumpOn)
        return if (result.isSuccess) {
            val actualPumpState = result.getOrThrow()

            // Update local state
            val current = _sensorData.value
            val updated = current.copy(
                pump = actualPumpState,
                pumpStatus = if (actualPumpState) "ON" else "OFF",
                mode = "manual",
                systemStatus = if (actualPumpState) "MANUAL OVERRIDE: PUMP ON" else "MANUAL OVERRIDE: PUMP OFF",
                updatedAtEpochMs = System.currentTimeMillis()
            )
            _sensorData.value = updated

            // Save event to Firebase Firestore
            if (firebaseManager.isUserLoggedIn) {
                firebaseManager.updatePumpControl(
                    targetPumpOn = actualPumpState,
                    mode = "manual",
                    currentRain = current.rain,
                    currentSoil = current.soilMoisture,
                    currentWater = current.waterLevel
                )
            }

            _commandStatus.value = CommandStatus.Confirmed(
                command,
                "Arduino confirmed: Relay is ${if (actualPumpState) "ON" else "OFF"}"
            )
            Result.success(Unit)
        } else {
            val err = result.exceptionOrNull()?.localizedMessage ?: "Failed to send command to Arduino"
            _commandStatus.value = CommandStatus.Error(err)
            Result.failure(Exception(err))
        }
    }

    suspend fun setSystemMode(mode: String): Result<Unit> {
        _sensorData.value = _sensorData.value.copy(mode = mode)
        if (firebaseManager.isUserLoggedIn) {
            firebaseManager.updatePumpControl(
                targetPumpOn = _sensorData.value.pump,
                mode = mode,
                currentRain = _sensorData.value.rain,
                currentSoil = _sensorData.value.soilMoisture,
                currentWater = _sensorData.value.waterLevel
            )
        }
        return Result.success(Unit)
    }

    fun clearCommandStatus() {
        _commandStatus.value = CommandStatus.Idle
    }

    fun disconnectSession() {
        _connectionStatus.value = ConnectionStatus.Disconnected
        _deviceMetadata.value = _deviceMetadata.value.copy(online = false)
        _systemStatus.value = SystemStatusSummary(
            arduinoStatus = "DISCONNECTED",
            isArduinoOnline = false,
            wifiStatus = "DISCONNECTED",
            isWifiOnline = false,
            sensorsStatus = "OFFLINE",
            areSensorsOnline = false,
            lastUpdatedEpochMs = lastSuccessfulSyncMs
        )
    }

    private fun startFirestoreObservation() {
        firestoreSyncJob?.cancel()
        firestoreSyncJob = scope.launch {
            // Observe sensorHistory/{currentUser.uid}/records
            launch {
                firebaseManager.observeSensorHistory().collect { history ->
                    if (history.isNotEmpty()) {
                        _sensorHistory.value = history
                    }
                }
            }

            // Observe weather/{currentUser.uid}
            launch {
                firebaseManager.observeWeather().collect { weather ->
                    if (!_weatherInfo.value.isLive || _weatherInfo.value.hourlyForecast.isEmpty()) {
                        _weatherInfo.value = weather
                    }
                }
            }
        }
    }

    private fun stopFirestoreObservation() {
        firestoreSyncJob?.cancel()
        firestoreSyncJob = null
    }

    // ==========================================
    // Weather API Operations
    // ==========================================

    fun refreshWeather() {
        scope.launch {
            _isRefreshingWeather.value = true
            _weatherErrorMessage.value = null
            val lat = prefs?.getString(KEY_WEATHER_LAT, "$DEFAULT_WEATHER_LAT")?.toDoubleOrNull() ?: DEFAULT_WEATHER_LAT
            val lon = prefs?.getString(KEY_WEATHER_LON, "$DEFAULT_WEATHER_LON")?.toDoubleOrNull() ?: DEFAULT_WEATHER_LON
            val name = prefs?.getString(KEY_WEATHER_LOC_NAME, DEFAULT_WEATHER_LOC_NAME) ?: DEFAULT_WEATHER_LOC_NAME

            val result = weatherApiClient.fetchWeather(lat, lon, name)
            result.onSuccess { liveWeather ->
                val enrichedRec = synthesizeAdvisory(liveWeather, _sensorData.value)
                val finalWeather = liveWeather.copy(smartRecommendation = enrichedRec)
                _weatherInfo.value = finalWeather
                // Push updated weather to cloud Firestore
                firebaseManager.saveWeather(finalWeather)
            }.onFailure { error ->
                Log.w("AquaSustainRepository", "Weather API note: ${error.message}")
                _weatherErrorMessage.value = "Weather update: ${error.localizedMessage ?: "Using cached forecast"}"
            }
            _isRefreshingWeather.value = false
        }
    }

    fun selectWeatherLocation(location: GeoLocation) {
        prefs?.edit()?.apply {
            putString(KEY_WEATHER_LAT, location.latitude.toString())
            putString(KEY_WEATHER_LON, location.longitude.toString())
            putString(KEY_WEATHER_LOC_NAME, location.displayName)
            apply()
        }
        refreshWeather()
    }

    fun searchWeatherLocation(query: String) {
        _weatherSearchQuery.value = query
        scope.launch {
            _isSearchingWeatherLocation.value = true
            val result = weatherApiClient.searchLocations(query)
            result.onSuccess { locations ->
                _weatherSearchResults.value = if (locations.isNotEmpty()) locations else WeatherApiClient.POPULAR_FARM_LOCATIONS
            }.onFailure {
                _weatherSearchResults.value = WeatherApiClient.POPULAR_FARM_LOCATIONS
            }
            _isSearchingWeatherLocation.value = false
        }
    }

    fun clearWeatherSearch() {
        _weatherSearchQuery.value = ""
        _weatherSearchResults.value = WeatherApiClient.POPULAR_FARM_LOCATIONS
    }

    private fun synthesizeAdvisory(weather: WeatherInfo, sensorData: AquaSustainData): String {
        return when {
            sensorData.rain ->
                "🌧️ Rain sensor active on-site. Irrigation deactivated; maximizing rainwater collection into storage."
            weather.rainProbabilityPercent >= 65 || weather.hourlyForecast.take(4).any { it.rainProb >= 65 } ->
                "🌧️ High precipitation probability (${weather.rainProbabilityPercent}%). Delaying irrigation will conserve underground tank water."
            sensorData.waterLevel < 20 ->
                "⚠️ Underground water level critical (${sensorData.waterLevel}%). Pump locked to prevent cavitation damage."
            sensorData.soilMoisture < 50 && weather.rainProbabilityPercent < 30 ->
                "💧 Soil is dry (${sensorData.soilMoisture}%) with clear forecast (${weather.condition}). Optimal conditions for automated irrigation."
            sensorData.soilMoisture < 50 ->
                "🌦️ Soil moisture low (${sensorData.soilMoisture}%), light showers possible (${weather.rainProbabilityPercent}%). System closely monitoring."
            weather.temperatureC >= 33 ->
                "☀️ Ambient temperature high (${weather.temperatureC}°C). Evaporation elevated; schedule watering early morning or late evening."
            else ->
                "✅ Optimal atmospheric conditions. Soil moisture balanced at ${sensorData.soilMoisture}%."
        }
    }
}

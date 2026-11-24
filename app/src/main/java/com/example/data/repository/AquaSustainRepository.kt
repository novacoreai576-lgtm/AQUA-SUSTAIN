package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.firebase.FirebaseManager
import com.example.data.model.AquaSustainData
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceItem
import com.example.data.model.GeoLocation
import com.example.data.model.IrrigationAdvice
import com.example.data.model.SensorReading
import com.example.data.model.SystemStatusSummary
import com.example.data.model.WaterAssistantMessage
import com.example.data.model.WaterQualityAnalysisRecord
import com.example.data.model.WaterQualityInput
import com.example.data.model.WaterUsageRecord
import com.example.data.model.WaterUsageSummary
import com.example.data.model.WeatherInfo
import com.example.data.remote.ArduinoHttpClient
import com.example.data.remote.GeminiIrrigationAdvisorService
import com.example.data.remote.GeminiWaterAiService
import com.example.data.remote.WeatherApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

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
    private val geminiAdvisorService: GeminiIrrigationAdvisorService = GeminiIrrigationAdvisorService(),
    private val geminiWaterAiService: GeminiWaterAiService = GeminiWaterAiService(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    companion object {
        private const val TAG = "AquaSustainRepository"
        private const val PREFS_NAME = "aquasustain_arduino_prefs"
        private const val KEY_ARDUINO_IP = "saved_arduino_ip"
        const val DEFAULT_ARDUINO_IP = "192.168.43.120"

        private const val KEY_WEATHER_LAT = "weather_latitude"
        private const val KEY_WEATHER_LON = "weather_longitude"
        private const val KEY_WEATHER_LOC_NAME = "weather_location_name"
        const val DEFAULT_WEATHER_LAT = 28.6139
        const val DEFAULT_WEATHER_LON = 77.2090
        const val DEFAULT_WEATHER_LOC_NAME = "New Delhi (Station)"

        fun disconnectedSensorState(): AquaSustainData = AquaSustainData(
            rain = false,
            rainStatus = "Unavailable",
            soilMoisture = 0,
            waterLevel = 0,
            pump = false,
            pumpStatus = "DISCONNECTED",
            systemStatus = "Device Not Connected.",
            mode = "auto",
            deviceId = "AQUA-001",
            updatedAtEpochMs = 0L,
            hasLiveData = false
        )
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val isGeminiConfigured: Boolean
        get() = geminiWaterAiService.isConfigured()

    private val _endpointUrl = MutableStateFlow(
        prefs?.getString(KEY_ARDUINO_IP, DEFAULT_ARDUINO_IP) ?: DEFAULT_ARDUINO_IP
    )
    val endpointUrl: StateFlow<String> = _endpointUrl.asStateFlow()

    private val _dataSourceMode = MutableStateFlow(DataSourceMode.LOCAL_ARDUINO_WIFI)
    val dataSourceMode: StateFlow<DataSourceMode> = _dataSourceMode.asStateFlow()

    private val _pollingIntervalMs = MutableStateFlow(2500L)
    val pollingIntervalMs: StateFlow<Long> = _pollingIntervalMs.asStateFlow()

    // Live Telemetry Stream — starts explicitly disconnected with no invented readings
    private val _sensorData = MutableStateFlow(disconnectedSensorState())
    val sensorData: StateFlow<AquaSustainData> = _sensorData.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

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

    private val _deviceMetadata = MutableStateFlow(
        DeviceItem(
            deviceId = "AQUA-001",
            name = "AquaSustain Monitor",
            board = "Arduino UNO R4 WiFi",
            online = false
        )
    )
    val deviceMetadata: StateFlow<DeviceItem> = _deviceMetadata.asStateFlow()

    private val _sensorHistory = MutableStateFlow<List<SensorReading>>(emptyList())
    val sensorHistory: StateFlow<List<SensorReading>> = _sensorHistory.asStateFlow()

    // Water Quality Analysis state
    private val _waterQualityHistory = MutableStateFlow<List<WaterQualityAnalysisRecord>>(emptyList())
    val waterQualityHistory: StateFlow<List<WaterQualityAnalysisRecord>> = _waterQualityHistory.asStateFlow()

    private val _currentQualityResult = MutableStateFlow<WaterQualityAnalysisRecord?>(null)
    val currentQualityResult: StateFlow<WaterQualityAnalysisRecord?> = _currentQualityResult.asStateFlow()

    private val _isAnalyzingWaterQuality = MutableStateFlow(false)
    val isAnalyzingWaterQuality: StateFlow<Boolean> = _isAnalyzingWaterQuality.asStateFlow()

    private val _waterQualityError = MutableStateFlow<String?>(null)
    val waterQualityError: StateFlow<String?> = _waterQualityError.asStateFlow()

    // Water Usage state
    private val _waterUsageRecords = MutableStateFlow<List<WaterUsageRecord>>(emptyList())
    val waterUsageRecords: StateFlow<List<WaterUsageRecord>> = _waterUsageRecords.asStateFlow()

    val waterUsageSummary: StateFlow<WaterUsageSummary> = _waterUsageRecords
        .map { records -> WaterUsageSummary.compute(records) }
        .stateIn(scope, SharingStarted.Eagerly, WaterUsageSummary())

    private val _waterUsageError = MutableStateFlow<String?>(null)
    val waterUsageError: StateFlow<String?> = _waterUsageError.asStateFlow()

    // AI Water Assistant Chat state
    private val _waterAssistantMessages = MutableStateFlow<List<WaterAssistantMessage>>(emptyList())
    val waterAssistantMessages: StateFlow<List<WaterAssistantMessage>> = _waterAssistantMessages.asStateFlow()

    private val _isSendingAssistantMessage = MutableStateFlow(false)
    val isSendingAssistantMessage: StateFlow<Boolean> = _isSendingAssistantMessage.asStateFlow()

    private val _assistantError = MutableStateFlow<String?>(null)
    val assistantError: StateFlow<String?> = _assistantError.asStateFlow()

    // Weather & AI Recommendations — starts un-fetched until real Open-Meteo or saved doc returns
    private val _weatherInfo = MutableStateFlow(
        WeatherInfo(
            locationName = prefs?.getString(KEY_WEATHER_LOC_NAME, DEFAULT_WEATHER_LOC_NAME) ?: DEFAULT_WEATHER_LOC_NAME,
            hasFetched = false,
            isLive = false
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

    private val _irrigationAdvice = MutableStateFlow<IrrigationAdvice?>(null)
    val irrigationAdvice: StateFlow<IrrigationAdvice?> = _irrigationAdvice.asStateFlow()

    private val _isAiAnalyzing = MutableStateFlow(false)
    val isAiAnalyzing: StateFlow<Boolean> = _isAiAnalyzing.asStateFlow()

    private val _aiAdvisorError = MutableStateFlow<String?>(null)
    val aiAdvisorError: StateFlow<String?> = _aiAdvisorError.asStateFlow()

    private val _commandStatus = MutableStateFlow<CommandStatus>(CommandStatus.Idle)
    val commandStatus: StateFlow<CommandStatus> = _commandStatus.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    private var pollingJob: Job? = null
    private var firestoreSyncJob: Job? = null
    private var lastSuccessfulSyncMs: Long = 0L
    private var consecutiveFailures = 0

    init {
        refreshWeather()
        scope.launch {
            while (isActive) {
                delay(20 * 60 * 1000L)
                refreshWeather()
            }
        }

        scope.launch {
            firebaseManager.currentUser.collect { user ->
                if (user != null) {
                    startFirestoreObservation()
                    refreshWeather()
                    startPolling()
                } else {
                    stopPolling()
                    stopFirestoreObservation()
                    disconnectSession()
                    clearUserScopedLists()
                }
            }
        }
    }

    private fun clearUserScopedLists() {
        _sensorHistory.value = emptyList()
        _waterQualityHistory.value = emptyList()
        _waterUsageRecords.value = emptyList()
        _waterAssistantMessages.value = emptyList()
        _currentQualityResult.value = null
        _irrigationAdvice.value = null
    }

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

    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = scope.launch {
            while (isActive) {
                executeArduinoPoll()
                val nextDelay = if (consecutiveFailures >= 3) 15_000L else _pollingIntervalMs.value
                delay(nextDelay)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun executeArduinoPoll() {
        val ip = _endpointUrl.value
        val result = httpClient.fetchData(ip)

        result.onSuccess { rawData ->
            consecutiveFailures = 0
            val data = rawData.sanitized()
            lastSuccessfulSyncMs = System.currentTimeMillis()
            _sensorData.value = data.copy(updatedAtEpochMs = lastSuccessfulSyncMs, hasLiveData = true)
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

            if (data.isAutoMode) {
                val targetPump = data.targetAutoPumpState
                if (data.pump != targetPump) {
                    httpClient.setPump(ip, targetPump)
                }
            }

            if (firebaseManager.isUserLoggedIn) {
                firebaseManager.saveLiveSensorData(data.copy(hasLiveData = true))
            }

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
            _connectionStatus.value = if (lastSuccessfulSyncMs > 0L) {
                ConnectionStatus.ConnectionLost(
                    lastSeenEpochMs = lastSuccessfulSyncMs,
                    message = "Device Not Connected: Arduino unreachable at $ip"
                )
            } else {
                ConnectionStatus.Disconnected
            }
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

            _sensorData.value = disconnectedSensorState().copy(
                mode = _sensorData.value.mode,
                updatedAtEpochMs = lastSuccessfulSyncMs
            )
        }
    }

    suspend fun testAndConnectArduino(ipOrUrl: String): Result<AquaSustainData> {
        val result = httpClient.fetchData(ipOrUrl)
        return if (result.isSuccess) {
            val data = result.getOrThrow().sanitized().copy(hasLiveData = true)
            saveArduinoIp(ipOrUrl)
            lastSuccessfulSyncMs = System.currentTimeMillis()
            val liveData = data.copy(updatedAtEpochMs = lastSuccessfulSyncMs)
            _sensorData.value = liveData
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

            if (firebaseManager.isUserLoggedIn) {
                firebaseManager.saveLiveSensorData(liveData)
            }

            stopPolling()
            startPolling()

            Result.success(liveData)
        } else {
            _connectionStatus.value = ConnectionStatus.Disconnected
            _deviceMetadata.value = _deviceMetadata.value.copy(online = false)
            _sensorData.value = disconnectedSensorState().copy(mode = _sensorData.value.mode)
            _systemStatus.value = SystemStatusSummary(
                arduinoStatus = "DISCONNECTED",
                isArduinoOnline = false,
                wifiStatus = "DISCONNECTED",
                isWifiOnline = false,
                sensorsStatus = "OFFLINE",
                areSensorsOnline = false,
                lastUpdatedEpochMs = lastSuccessfulSyncMs
            )
            Result.failure(result.exceptionOrNull() ?: Exception("Device Not Connected."))
        }
    }

    suspend fun requestPumpCommand(targetPumpOn: Boolean): Result<Unit> {
        if (!_sensorData.value.hasLiveData) {
            val msg = "Device Not Connected. Connect Arduino UNO R4 WiFi before controlling the pump relay."
            _commandStatus.value = CommandStatus.Error(msg)
            return Result.failure(IllegalStateException(msg))
        }

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
            val current = _sensorData.value
            val updated = current.copy(
                pump = actualPumpState,
                pumpStatus = if (actualPumpState) "ON" else "OFF",
                mode = "manual",
                systemStatus = if (actualPumpState) "MANUAL OVERRIDE: PUMP ON" else "MANUAL OVERRIDE: PUMP OFF",
                updatedAtEpochMs = System.currentTimeMillis(),
                hasLiveData = true
            )
            _sensorData.value = updated

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
        if (_sensorData.value.hasLiveData && firebaseManager.isUserLoggedIn) {
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
        _sensorData.value = disconnectedSensorState()
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
            launch {
                firebaseManager.observeSensorHistory().collect { history ->
                    _sensorHistory.value = history
                }
            }
            launch {
                firebaseManager.observeWaterQualityAnalyses().collect { records ->
                    _waterQualityHistory.value = records
                }
            }
            launch {
                firebaseManager.observeWaterUsageRecords().collect { records ->
                    _waterUsageRecords.value = records
                }
            }
            launch {
                firebaseManager.observeWaterChatMessages().collect { messages ->
                    _waterAssistantMessages.value = messages
                }
            }
            launch {
                firebaseManager.observeWeather().collect { savedWeather ->
                    if (savedWeather != null && !_weatherInfo.value.isLive) {
                        _weatherInfo.value = savedWeather
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
    // Weather & Gemini Irrigation Advisor
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
                val finalWeather = liveWeather.copy(smartRecommendation = enrichedRec, hasFetched = true)
                _weatherInfo.value = finalWeather
                firebaseManager.saveWeather(finalWeather)
                if (_sensorData.value.hasLiveData && isGeminiConfigured) {
                    generateAiIrrigationAdvice(finalWeather, _sensorData.value)
                }
            }.onFailure { error ->
                Log.w(TAG, "Weather API error: ${error.message}")
                _weatherErrorMessage.value = "Weather data unavailable: ${error.localizedMessage ?: "Network error"}"
            }
            _isRefreshingWeather.value = false
        }
    }

    fun triggerAiAdvisor() {
        scope.launch {
            generateAiIrrigationAdvice(_weatherInfo.value, _sensorData.value)
        }
    }

    private suspend fun generateAiIrrigationAdvice(
        weather: WeatherInfo,
        sensorData: AquaSustainData
    ) {
        _isAiAnalyzing.value = true
        _aiAdvisorError.value = null
        val result = geminiAdvisorService.generateIrrigationAdvice(sensorData, weather)
        result.onSuccess { advice ->
            _irrigationAdvice.value = advice
        }.onFailure { err ->
            _aiAdvisorError.value = err.message ?: "Unable to generate AI irrigation advice."
        }
        _isAiAnalyzing.value = false
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
        if (!sensorData.hasLiveData) {
            return when {
                weather.rainProbabilityPercent >= 65 ->
                    "High rain probability (${weather.rainProbabilityPercent}%). Connect Arduino hardware to coordinate irrigation holds."
                weather.temperatureC >= 33 ->
                    "High ambient temperature (${weather.temperatureC}°C). Evaporation risk elevated; connect monitoring hardware for live soil tracking."
                else ->
                    "Forecast: ${weather.condition}, ${weather.temperatureC}°C, ${weather.rainProbabilityPercent}% rain likelihood. Hardware monitoring offline."
            }
        }
        return when {
            sensorData.rain ->
                "Rain sensor active on-site. Irrigation deactivated to conserve tank water."
            weather.rainProbabilityPercent >= 65 || weather.hourlyForecast.take(4).any { it.rainProb >= 65 } ->
                "High precipitation probability (${weather.rainProbabilityPercent}%). Delaying irrigation will conserve underground tank water."
            sensorData.waterLevel < 20 ->
                "Underground water level critical (${sensorData.waterLevel}%). Pump locked to prevent dry-run damage."
            sensorData.soilMoisture < 50 && weather.rainProbabilityPercent < 30 ->
                "Soil is dry (${sensorData.soilMoisture}%) with clear skies (${weather.condition}). Conditions support scheduled irrigation."
            sensorData.soilMoisture < 50 ->
                "Soil moisture low (${sensorData.soilMoisture}%), showers possible (${weather.rainProbabilityPercent}%). Monitoring closely."
            weather.temperatureC >= 33 ->
                "High ambient temperature (${weather.temperatureC}°C). Schedule irrigation during early morning or evening."
            else ->
                "Atmospheric and soil moisture balanced (${sensorData.soilMoisture}%)."
        }
    }

    // ==========================================
    // Water Quality Analysis Operations
    // ==========================================

    suspend fun analyzeWaterQuality(
        input: WaterQualityInput,
        imageBase64: String? = null,
        imageMimeType: String? = null
    ): Result<WaterQualityAnalysisRecord> {
        _isAnalyzingWaterQuality.value = true
        _waterQualityError.value = null
        val result = geminiWaterAiService.analyzeWaterQuality(input, imageBase64, imageMimeType)
        result.onSuccess { record ->
            _currentQualityResult.value = record
        }.onFailure { err ->
            _waterQualityError.value = err.message ?: "Water quality analysis failed."
        }
        _isAnalyzingWaterQuality.value = false
        return result
    }

    suspend fun saveWaterQualityRecord(record: WaterQualityAnalysisRecord): Result<String> {
        val result = firebaseManager.saveWaterQualityAnalysis(record)
        result.onFailure { err ->
            _waterQualityError.value = "Could not save analysis record: ${err.message}"
        }
        return result
    }

    suspend fun deleteWaterQualityRecord(recordId: String): Result<Unit> {
        return firebaseManager.deleteWaterQualityAnalysis(recordId)
    }

    fun clearWaterQualityError() {
        _waterQualityError.value = null
    }

    fun clearCurrentQualityResult() {
        _currentQualityResult.value = null
    }

    // ==========================================
    // Water Usage Operations
    // ==========================================

    suspend fun logWaterUsage(
        amountLiters: Double,
        category: String,
        source: String,
        notes: String,
        usageDateEpochMs: Long = System.currentTimeMillis()
    ): Result<String> {
        _waterUsageError.value = null
        if (amountLiters <= 0.0) {
            val err = Exception("Water usage amount must be greater than 0 liters.")
            _waterUsageError.value = err.message
            return Result.failure(err)
        }
        val record = WaterUsageRecord(
            id = UUID.randomUUID().toString(),
            amountLiters = amountLiters,
            category = category.ifBlank { "Irrigation" },
            source = source.ifBlank { "Underground Tank" },
            notes = notes.trim(),
            usageDateEpochMs = usageDateEpochMs
        )
        val result = firebaseManager.saveWaterUsageRecord(record)
        result.onFailure { e ->
            _waterUsageError.value = "Failed to save water usage record: ${e.message}"
        }
        return result
    }

    suspend fun deleteWaterUsageRecord(recordId: String): Result<Unit> {
        return firebaseManager.deleteWaterUsageRecord(recordId)
    }

    fun clearWaterUsageError() {
        _waterUsageError.value = null
    }

    suspend fun deleteSensorHistoryRecord(recordId: String): Result<Unit> {
        return firebaseManager.deleteSensorHistoryRecord(recordId)
    }

    // ==========================================
    // AI Water Assistant Chat Operations
    // ==========================================

    suspend fun sendAssistantMessage(userText: String, preferredLanguage: String): Result<Unit> {
        val cleanText = userText.trim()
        if (cleanText.isEmpty()) return Result.failure(IllegalArgumentException("Message cannot be empty."))

        _isSendingAssistantMessage.value = true
        _assistantError.value = null

        val userMessage = WaterAssistantMessage(
            id = UUID.randomUUID().toString(),
            role = "user",
            content = cleanText,
            language = preferredLanguage,
            timestampEpochMs = System.currentTimeMillis()
        )

        // Check Gemini configuration first so we don't write orphaned user prompts when unconfigured
        if (!geminiWaterAiService.isConfigured()) {
            _isSendingAssistantMessage.value = false
            val msg = "Gemini API key is not configured. Add GEMINI_API_KEY in the AI Studio Secrets panel to use the AI Water Assistant."
            _assistantError.value = msg
            return Result.failure(IllegalStateException(msg))
        }

        val currentHistory = _waterAssistantMessages.value
        val aiResult = geminiWaterAiService.sendWaterAssistantMessage(
            userMessage = cleanText,
            preferredLanguage = preferredLanguage,
            recentHistory = currentHistory,
            sensorData = _sensorData.value,
            weather = _weatherInfo.value,
            latestQuality = _waterQualityHistory.value.firstOrNull() ?: _currentQualityResult.value,
            usageSummary = waterUsageSummary.value
        )

        return if (aiResult.isSuccess) {
            val replyText = aiResult.getOrThrow()
            val assistantMsg = WaterAssistantMessage(
                id = UUID.randomUUID().toString(),
                role = "assistant",
                content = replyText,
                language = preferredLanguage,
                timestampEpochMs = System.currentTimeMillis() + 1L
            )
            // Persist both real messages to Firestore
            firebaseManager.saveWaterChatMessage(userMessage)
            firebaseManager.saveWaterChatMessage(assistantMsg)
            _isSendingAssistantMessage.value = false
            Result.success(Unit)
        } else {
            val errMsg = aiResult.exceptionOrNull()?.message ?: "AI Water Assistant failed to respond."
            _assistantError.value = errMsg
            _isSendingAssistantMessage.value = false
            Result.failure(Exception(errMsg))
        }
    }

    suspend fun clearAssistantChat(): Result<Unit> {
        _assistantError.value = null
        return firebaseManager.clearWaterChatHistory()
    }

    fun clearAssistantError() {
        _assistantError.value = null
    }
}

package com.example.ui

import android.app.Application
import android.net.Uri
import android.util.Base64
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AquaSustainData
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceItem
import com.example.data.model.GeoLocation
import com.example.data.model.SensorReading
import com.example.data.model.SystemStatusSummary
import com.example.data.model.UserProfile
import com.example.data.model.WaterAssistantMessage
import com.example.data.model.WaterQualityAnalysisRecord
import com.example.data.model.WaterQualityInput
import com.example.data.model.WaterUsageRecord
import com.example.data.model.WaterUsageSummary
import com.example.data.model.WeatherInfo
import com.example.data.remote.GeminiWaterAiService
import com.example.data.repository.AquaSustainRepository
import com.example.data.repository.CommandStatus
import com.example.data.repository.DataSourceMode
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

enum class HistoryTimeFilter(val label: String) {
    TODAY("Today"),
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days")
}

class AquaSustainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: AquaSustainRepository = AquaSustainRepository(
        context = application.applicationContext
    )

    val isGeminiConfigured: Boolean
        get() = repository.isGeminiConfigured

    // Sensor & System Telemetry
    val sensorData: StateFlow<AquaSustainData> = repository.sensorData
    val connectionStatus: StateFlow<ConnectionStatus> = repository.connectionStatus
    val systemStatus: StateFlow<SystemStatusSummary> = repository.systemStatus
    val deviceMetadata: StateFlow<DeviceItem> = repository.deviceMetadata
    val sensorHistory: StateFlow<List<SensorReading>> = repository.sensorHistory
    val weatherInfo: StateFlow<WeatherInfo> = repository.weatherInfo
    val isRefreshingWeather: StateFlow<Boolean> = repository.isRefreshingWeather
    val weatherSearchResults: StateFlow<List<GeoLocation>> = repository.weatherSearchResults
    val isSearchingWeatherLocation: StateFlow<Boolean> = repository.isSearchingWeatherLocation
    val weatherSearchQuery: StateFlow<String> = repository.weatherSearchQuery
    val weatherErrorMessage: StateFlow<String?> = repository.weatherErrorMessage
    val dataSourceMode: StateFlow<DataSourceMode> = repository.dataSourceMode
    val endpointUrl: StateFlow<String> = repository.endpointUrl
    val pollingIntervalMs: StateFlow<Long> = repository.pollingIntervalMs
    val lastErrorMessage: StateFlow<String?> = repository.lastErrorMessage
    val commandStatus: StateFlow<CommandStatus> = repository.commandStatus

    // AI Weather & Irrigation Advisor
    val irrigationAdvice = repository.irrigationAdvice
    val isAiAnalyzing = repository.isAiAnalyzing
    val aiAdvisorError: StateFlow<String?> = repository.aiAdvisorError

    // Water Quality Analysis
    val waterQualityHistory: StateFlow<List<WaterQualityAnalysisRecord>> = repository.waterQualityHistory
    val currentQualityResult: StateFlow<WaterQualityAnalysisRecord?> = repository.currentQualityResult
    val isAnalyzingWaterQuality: StateFlow<Boolean> = repository.isAnalyzingWaterQuality
    val waterQualityError: StateFlow<String?> = repository.waterQualityError

    private val _qualitySaveFeedback = MutableStateFlow<String?>(null)
    val qualitySaveFeedback: StateFlow<String?> = _qualitySaveFeedback.asStateFlow()

    // Water Usage Monitoring
    val waterUsageRecords: StateFlow<List<WaterUsageRecord>> = repository.waterUsageRecords
    val waterUsageSummary: StateFlow<WaterUsageSummary> = repository.waterUsageSummary
    val waterUsageError: StateFlow<String?> = repository.waterUsageError

    private val _usageSaveFeedback = MutableStateFlow<String?>(null)
    val usageSaveFeedback: StateFlow<String?> = _usageSaveFeedback.asStateFlow()

    // AI Water Assistant Chat
    val waterAssistantMessages: StateFlow<List<WaterAssistantMessage>> = repository.waterAssistantMessages
    val isSendingAssistantMessage: StateFlow<Boolean> = repository.isSendingAssistantMessage
    val assistantError: StateFlow<String?> = repository.assistantError

    private val _selectedAssistantLanguage = MutableStateFlow(GeminiWaterAiService.SUPPORTED_LANGUAGES.first())
    val selectedAssistantLanguage: StateFlow<String> = _selectedAssistantLanguage.asStateFlow()

    // Firebase Auth States
    val currentUser: StateFlow<FirebaseUser?> = repository.firebaseManager.currentUser
    val userProfile: StateFlow<UserProfile?> = repository.firebaseManager.userProfile
    val isAuthReady: StateFlow<Boolean> = repository.firebaseManager.isAuthReady

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // History filter
    private val _historyFilter = MutableStateFlow(HistoryTimeFilter.TODAY)
    val historyFilter: StateFlow<HistoryTimeFilter> = _historyFilter.asStateFlow()

    val filteredSensorHistory: StateFlow<List<SensorReading>> = combine(
        sensorHistory,
        _historyFilter
    ) { readings, filter ->
        val now = System.currentTimeMillis()
        val cutoffMs = when (filter) {
            HistoryTimeFilter.TODAY -> now - 24 * 60 * 60 * 1000L
            HistoryTimeFilter.LAST_7_DAYS -> now - 7 * 24 * 60 * 60 * 1000L
            HistoryTimeFilter.LAST_30_DAYS -> now - 30 * 24 * 60 * 60 * 1000L
        }
        readings.filter { it.timestampEpochMs >= cutoffMs }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHistoryFilter(filter: HistoryTimeFilter) {
        _historyFilter.value = filter
    }

    // Testing Connection feedback
    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _testFeedback = MutableStateFlow<String?>(null)
    val testFeedback: StateFlow<String?> = _testFeedback.asStateFlow()

    // ==========================================
    // Authentication Operations
    // ==========================================

    fun signIn(email: String, password: String) {
        if (_authUiState.value is AuthUiState.Loading) return
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Please enter both email and password")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _authUiState.value = AuthUiState.Error("Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val result = repository.firebaseManager.signIn(cleanEmail, password)
            result.onSuccess {
                _authUiState.value = AuthUiState.Success("Welcome back, ${it.email}!")
                repository.startPolling()
            }.onFailure { error ->
                _authUiState.value = AuthUiState.Error(error.localizedMessage ?: "Failed to sign in")
            }
        }
    }

    fun signUp(name: String, email: String, password: String, confirmPass: String) {
        if (_authUiState.value is AuthUiState.Loading) return
        val cleanEmail = email.trim()
        val cleanName = name.trim()
        if (cleanName.isBlank() || cleanEmail.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Please fill out all registration fields")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _authUiState.value = AuthUiState.Error("Please enter a valid email address")
            return
        }
        if (password != confirmPass) {
            _authUiState.value = AuthUiState.Error("Passwords do not match")
            return
        }
        if (password.length < 6) {
            _authUiState.value = AuthUiState.Error("Password must be at least 6 characters long")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val result = repository.firebaseManager.signUp(cleanName, cleanEmail, password)
            result.onSuccess {
                _authUiState.value = AuthUiState.Success("Account created successfully!")
                repository.startPolling()
            }.onFailure { error ->
                val msg = error.localizedMessage ?: "Registration failed"
                if (msg.contains("already in use", ignoreCase = true) || msg.contains("already exists", ignoreCase = true)) {
                    _authUiState.value = AuthUiState.Error("An account with $cleanEmail already exists. Please sign in instead.")
                } else {
                    _authUiState.value = AuthUiState.Error(msg)
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (_authUiState.value is AuthUiState.Loading) return
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _authUiState.value = AuthUiState.Error("Please enter a valid account email address")
            return
        }
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val result = repository.firebaseManager.sendPasswordReset(cleanEmail)
            result.onSuccess {
                _authUiState.value = AuthUiState.Success("Password reset email sent. Please check your inbox.")
            }.onFailure { error ->
                _authUiState.value = AuthUiState.Error(error.localizedMessage ?: "Could not send reset email")
            }
        }
    }

    fun signOut() {
        repository.stopPolling()
        repository.disconnectSession()
        repository.firebaseManager.signOut()
        _authUiState.value = AuthUiState.Idle
    }

    fun clearAuthError() {
        if (_authUiState.value is AuthUiState.Error || _authUiState.value is AuthUiState.Success) {
            _authUiState.value = AuthUiState.Idle
        }
    }

    // ==========================================
    // Actuator & Arduino Configuration
    // ==========================================

    fun requestPumpAction(pumpOn: Boolean) {
        viewModelScope.launch {
            repository.requestPumpCommand(pumpOn)
        }
    }

    fun clearCommandFeedback() {
        repository.clearCommandStatus()
    }

    fun setSystemMode(mode: String) {
        viewModelScope.launch {
            repository.setSystemMode(mode)
        }
    }

    fun saveArduinoIp(ip: String) {
        repository.saveArduinoIp(ip)
    }

    fun connectToArduino(ip: String) {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _testFeedback.value = "Connecting to Arduino at http://$ip/data..."
            val result = repository.testAndConnectArduino(ip)
            _isTestingConnection.value = false
            result.onSuccess { data ->
                _testFeedback.value = "CONNECTED! Live data received: Rain=${if (data.rain) "Detected" else "No Rain"}, Soil=${data.soilMoisture}%, Water=${data.waterLevel}%, Pump=${if (data.pump) "ON" else "OFF"}"
            }.onFailure { error ->
                _testFeedback.value = "Device Not Connected: ${error.localizedMessage ?: "Could not reach Arduino. Verify IP and Wi-Fi."}"
            }
        }
    }

    fun clearFeedback() {
        _testFeedback.value = null
    }

    // ==========================================
    // Weather & AI Advisor
    // ==========================================

    fun refreshWeather() {
        repository.refreshWeather()
    }

    fun selectWeatherLocation(location: GeoLocation) {
        repository.selectWeatherLocation(location)
    }

    fun searchWeatherLocation(query: String) {
        repository.searchWeatherLocation(query)
    }

    fun clearWeatherSearch() {
        repository.clearWeatherSearch()
    }

    fun triggerAiAdvisor() {
        repository.triggerAiAdvisor()
    }

    // ==========================================
    // Water Quality Analysis
    // ==========================================

    fun analyzeWaterQuality(input: WaterQualityInput, imageUri: Uri?) {
        viewModelScope.launch {
            _qualitySaveFeedback.value = null
            var imageBase64: String? = null
            var mimeType: String? = null

            if (imageUri != null) {
                val encoded = encodeUriToBase64(imageUri)
                if (encoded != null) {
                    imageBase64 = encoded.first
                    mimeType = encoded.second
                }
            }

            repository.analyzeWaterQuality(input, imageBase64, mimeType)
        }
    }

    fun saveCurrentWaterQualityAnalysis() {
        val current = currentQualityResult.value ?: return
        viewModelScope.launch {
            val result = repository.saveWaterQualityRecord(current)
            result.onSuccess {
                _qualitySaveFeedback.value = "Analysis report saved to your cloud records."
            }.onFailure { err ->
                _qualitySaveFeedback.value = "Failed to save report: ${err.message}"
            }
        }
    }

    fun deleteWaterQualityRecord(recordId: String) {
        viewModelScope.launch {
            repository.deleteWaterQualityRecord(recordId)
        }
    }

    fun clearWaterQualityFeedback() {
        repository.clearWaterQualityError()
        _qualitySaveFeedback.value = null
    }

    private suspend fun encodeUriToBase64(uri: Uri): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            val resolver = getApplication<Application>().contentResolver
            val mime = resolver.getType(uri) ?: "image/jpeg"
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            b64 to mime
        } catch (_: Exception) {
            null
        }
    }

    // ==========================================
    // Water Usage Monitoring
    // ==========================================

    fun logWaterUsage(
        amountLiters: Double,
        category: String,
        source: String,
        notes: String,
        usageDateEpochMs: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            _usageSaveFeedback.value = null
            val result = repository.logWaterUsage(amountLiters, category, source, notes, usageDateEpochMs)
            result.onSuccess {
                _usageSaveFeedback.value = "Logged ${WaterUsageSummary.formatLiters(amountLiters)} under $category."
            }
        }
    }

    fun deleteWaterUsageRecord(recordId: String) {
        viewModelScope.launch {
            repository.deleteWaterUsageRecord(recordId)
        }
    }

    fun clearUsageFeedback() {
        repository.clearWaterUsageError()
        _usageSaveFeedback.value = null
    }

    fun deleteSensorHistoryRecord(recordId: String) {
        viewModelScope.launch {
            repository.deleteSensorHistoryRecord(recordId)
        }
    }

    // ==========================================
    // AI Water Assistant Chat
    // ==========================================

    fun setAssistantLanguage(language: String) {
        _selectedAssistantLanguage.value = language
    }

    fun sendAssistantMessage(text: String) {
        viewModelScope.launch {
            repository.sendAssistantMessage(text, _selectedAssistantLanguage.value)
        }
    }

    fun clearAssistantChat() {
        viewModelScope.launch {
            repository.clearAssistantChat()
        }
    }

    fun clearAssistantError() {
        repository.clearAssistantError()
    }

    // ==========================================
    // Formatting & Report Builders (Real Data Only)
    // ==========================================

    fun getFormattedElapsed(epochMs: Long): String {
        if (epochMs <= 0L) return "Not connected yet"
        val diffSec = ((System.currentTimeMillis() - epochMs) / 1000).coerceAtLeast(0)
        return when {
            diffSec < 5 -> "Just now"
            diffSec < 60 -> "$diffSec seconds ago"
            diffSec < 3600 -> "${diffSec / 60}m ago"
            else -> "${diffSec / 3600}h ago"
        }
    }

    fun buildTextSummaryReport(): String {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val nowStr = df.format(Date())
        val dev = deviceMetadata.value
        val sensor = sensorData.value
        val usage = waterUsageSummary.value
        val qualityList = waterQualityHistory.value
        val readings = sensorHistory.value
        val weather = weatherInfo.value

        return buildString {
            appendLine("====================================================")
            appendLine("AQUASUSTAIN WATER SUSTAINABILITY REPORT")
            appendLine("Generated: $nowStr")
            appendLine("User: ${userProfile.value?.name ?: "Authenticated User"} (${userProfile.value?.email ?: ""})")
            appendLine("====================================================")
            appendLine()
            appendLine("1. HARDWARE & MONITORING STATUS")
            appendLine("   Device ID: ${dev.deviceId} (${dev.board})")
            appendLine("   Connection Status: ${if (sensor.hasLiveData) "Connected" else "Device Not Connected."}")
            if (sensor.hasLiveData) {
                appendLine("   Tank Water Level: ${sensor.waterLevel}%")
                appendLine("   Soil Moisture: ${sensor.soilMoisture}%")
                appendLine("   Rain Sensor: ${if (sensor.rain) "Rain Detected" else "Clear"}")
                appendLine("   Pump State: ${if (sensor.pump) "ON" else "OFF"} (${sensor.mode})")
            } else {
                appendLine("   Live Sensor Readings: Unavailable (Waiting for data connection.)")
            }
            appendLine()
            appendLine("2. ENVIRONMENTAL WEATHER SNAPSHOT")
            if (weather.hasFetched) {
                appendLine("   Location: ${weather.locationName}")
                appendLine("   Condition: ${weather.condition}, ${weather.temperatureC}°C (Humidity ${weather.humidityPercent}%)")
                appendLine("   Rain Probability: ${weather.rainProbabilityPercent}%")
            } else {
                appendLine("   Weather Status: Waiting for data connection.")
            }
            appendLine()
            appendLine("3. WATER USAGE SUMMARY (${usage.totalRecords} saved records)")
            if (usage.totalRecords == 0) {
                appendLine("   No water usage records saved yet.")
            } else {
                appendLine("   Today: ${WaterUsageSummary.formatLiters(usage.todayLiters)}")
                appendLine("   Last 7 Days: ${WaterUsageSummary.formatLiters(usage.weeklyLiters)}")
                appendLine("   Last 30 Days: ${WaterUsageSummary.formatLiters(usage.monthlyLiters)}")
                usage.categoryBreakdown.forEach { cat ->
                    appendLine("   - ${cat.category}: ${WaterUsageSummary.formatLiters(cat.totalLiters)} (${cat.percentage.toInt()}%)")
                }
            }
            appendLine()
            appendLine("4. WATER QUALITY ANALYSES (${qualityList.size} saved records)")
            if (qualityList.isEmpty()) {
                appendLine("   No water quality analyses saved yet.")
            } else {
                qualityList.take(20).forEach { q ->
                    appendLine("   - [${df.format(Date(q.createdAtEpochMs))}] ${q.sourceName} | Status: ${q.statusCategory}")
                    appendLine("     pH=${q.ph ?: "N/A"}, Turbidity=${q.turbidityNtu ?: "N/A"} NTU, Temp=${q.temperatureC ?: "N/A"} °C, TDS=${q.tdsPpm ?: "N/A"} ppm, EC=${q.ecUsCm ?: "N/A"} µS/cm, DO=${q.dissolvedOxygenMgL ?: "N/A"} mg/L")
                    if (q.summary.isNotBlank()) appendLine("     Summary: ${q.summary}")
                }
            }
            appendLine()
            appendLine("5. TELEMETRY HISTORY LOGS (${readings.size} saved records)")
            if (readings.isEmpty()) {
                appendLine("   No telemetry records saved yet.")
            } else {
                readings.take(30).forEach { r ->
                    appendLine("   - ${df.format(Date(r.timestampEpochMs))} | Soil: ${r.soilMoisture}% | Tank: ${r.waterLevel}% | Rain: ${r.rain} | Pump: ${if (r.pump) "ON" else "OFF"}")
                }
            }
        }
    }

    fun buildWaterQualityCsv(): String {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val rows = waterQualityHistory.value
        return buildString {
            appendLine("Timestamp,Source,Status,pH,Turbidity_NTU,Temp_C,TDS_ppm,EC_uScm,DO_mgL,HasImage,Summary")
            rows.forEach { r ->
                val summaryClean = r.summary.replace("\"", "\"\"").replace("\n", " ")
                val sourceClean = r.sourceName.replace("\"", "\"\"")
                appendLine(
                    "\"${df.format(Date(r.createdAtEpochMs))}\",\"$sourceClean\",\"${r.statusCategory}\",${r.ph ?: ""},${r.turbidityNtu ?: ""},${r.temperatureC ?: ""},${r.tdsPpm ?: ""},${r.ecUsCm ?: ""},${r.dissolvedOxygenMgL ?: ""},${r.hasAttachedImage},\"$summaryClean\""
                )
            }
        }
    }

    fun buildWaterUsageCsv(): String {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val rows = waterUsageRecords.value
        return buildString {
            appendLine("Timestamp,Amount_Liters,Category,Source,Notes")
            rows.forEach { r ->
                val notesClean = r.notes.replace("\"", "\"\"").replace("\n", " ")
                appendLine(
                    "\"${df.format(Date(r.usageDateEpochMs))}\",${r.amountLiters},\"${r.category}\",\"${r.source}\",\"$notesClean\""
                )
            }
        }
    }
}

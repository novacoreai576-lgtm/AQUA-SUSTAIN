package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AquaSustainData
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceItem
import com.example.data.model.GeoLocation
import com.example.data.model.SensorReading
import com.example.data.model.SystemStatusSummary
import com.example.data.model.UserProfile
import com.example.data.model.WeatherInfo
import com.example.data.repository.AquaSustainRepository
import com.example.data.repository.CommandStatus
import com.example.data.repository.DataSourceMode
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
        val filtered = readings.filter { it.timestampEpochMs >= cutoffMs }
        if (filtered.isNotEmpty()) filtered else readings // fallback to available if empty
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
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Please enter both email and password")
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
        val cleanEmail = email.trim()
        val cleanName = name.trim()
        if (cleanName.isBlank() || cleanEmail.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState.Error("Please fill out all registration fields")
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
                    // In case the user previously registered this account, attempt sign in with this password
                    val signInResult = repository.firebaseManager.signIn(cleanEmail, password)
                    signInResult.onSuccess { user ->
                        _authUiState.value = AuthUiState.Success("Account recognized. Welcome back, ${user.email}!")
                        repository.startPolling()
                    }.onFailure {
                        _authUiState.value = AuthUiState.Error("An account with $cleanEmail already exists. Please sign in instead.")
                    }
                } else {
                    _authUiState.value = AuthUiState.Error(msg)
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _authUiState.value = AuthUiState.Error("Please enter your account email address")
            return
        }
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val result = repository.firebaseManager.sendPasswordReset(email)
            result.onSuccess {
                _authUiState.value = AuthUiState.Success("Password reset email sent. Please check your inbox.")
            }.onFailure { error ->
                _authUiState.value = AuthUiState.Error(error.localizedMessage ?: "Could not send reset email")
            }
        }
    }

    /**
     * Sign Out Function:
     * 1. Stop Arduino polling
     * 2. Disconnect app session
     * 3. Sign out from Firebase
     * 4. Return to Login screen
     */
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
    // Actuator Commands (Pump ON / Pump OFF)
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

    // ==========================================
    // Arduino Local Wi-Fi Configuration
    // ==========================================

    fun saveArduinoIp(ip: String) {
        repository.saveArduinoIp(ip)
    }

    /**
     * CONNECT action in Arduino Connection screen:
     * 1. Test http://<IP>/data
     * 2. If successful: Arduino = CONNECTED
     * 3. If unsuccessful: Arduino = DISCONNECTED with clear error
     */
    fun connectToArduino(ip: String) {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _testFeedback.value = "Connecting to Arduino at http://$ip/data..."
            val result = repository.testAndConnectArduino(ip)
            _isTestingConnection.value = false
            result.onSuccess { data ->
                _testFeedback.value = "CONNECTED! Live data received: Rain=${if (data.rain) "Detected" else "No Rain"}, Soil=${data.soilMoisture}%, Water=${data.waterLevel}%, Pump=${if (data.pump) "ON" else "OFF"}"
            }.onFailure { error ->
                _testFeedback.value = "DISCONNECTED: ${error.localizedMessage ?: "Could not reach Arduino. Verify IP and Wi-Fi."}"
            }
        }
    }

    fun clearFeedback() {
        _testFeedback.value = null
    }

    // ==========================================
    // Weather API Controls
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

    fun getFormattedElapsed(epochMs: Long): String {
        if (epochMs <= 0L) return "Never"
        val diffSec = ((System.currentTimeMillis() - epochMs) / 1000).coerceAtLeast(0)
        return when {
            diffSec < 5 -> "Just now"
            diffSec < 60 -> "$diffSec seconds ago"
            diffSec < 3600 -> "${diffSec / 60}m ago"
            else -> "${diffSec / 3600}h ago"
        }
    }
}

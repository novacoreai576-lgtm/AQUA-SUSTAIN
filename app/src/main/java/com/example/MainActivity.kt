package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.DataSourceMode
import com.example.ui.AquaSustainViewModel
import com.example.ui.HistoryTimeFilter
import com.example.ui.components.ArduinoConfigDialog
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.ArchitectureScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.IrrigationScreen
import com.example.ui.screens.SensorsScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.AquaSustainTheme
import com.example.ui.theme.EcoGreen

enum class AppDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    SENSORS("Sensors", Icons.Filled.Sensors, Icons.Outlined.Sensors, "nav_sensors"),
    IRRIGATION("Irrigation", Icons.Filled.WaterDrop, Icons.Outlined.WaterDrop, "nav_irrigation"),
    WEATHER("Weather & AI", Icons.Filled.Cloud, Icons.Outlined.Cloud, "nav_weather"),
    HISTORY("History", Icons.Filled.Timeline, Icons.Outlined.Timeline, "nav_history"),
    ACCOUNT("Account", Icons.Filled.Person, Icons.Outlined.Person, "nav_account")
}

class MainActivity : ComponentActivity() {
    private val viewModel: AquaSustainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AquaSustainTheme {
                AquaSustainRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AquaSustainRoot(
    viewModel: AquaSustainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthReady by viewModel.isAuthReady.collectAsStateWithLifecycle()
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()

    // 1. Initial Splash / Auth Check
    if (!isAuthReady) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AquaPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "AquaSustain",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AquaSustain",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = AquaPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(
                    color = AquaPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            }
        }
        return
    }

    // 2. Authentication Check:
    // If NOT authenticated -> Show Login screen.
    // If authenticated -> Skip Login and open AquaSustain Dashboard.
    // User must never see the Dashboard before authentication.
    if (currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            authUiState = authUiState,
            modifier = modifier
        )
    } else {
        AquaSustainApp(
            viewModel = viewModel,
            onSignOut = { viewModel.signOut() },
            modifier = modifier
        )
    }
}

@Composable
fun AquaSustainApp(
    viewModel: AquaSustainViewModel,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sensorData by viewModel.sensorData.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val systemStatus by viewModel.systemStatus.collectAsStateWithLifecycle()
    val deviceMetadata by viewModel.deviceMetadata.collectAsStateWithLifecycle()
    val filteredHistory by viewModel.filteredSensorHistory.collectAsStateWithLifecycle()
    val historyFilter by viewModel.historyFilter.collectAsStateWithLifecycle()
    val weatherInfo by viewModel.weatherInfo.collectAsStateWithLifecycle()
    val isRefreshingWeather by viewModel.isRefreshingWeather.collectAsStateWithLifecycle()
    val weatherSearchResults by viewModel.weatherSearchResults.collectAsStateWithLifecycle()
    val isSearchingWeatherLocation by viewModel.isSearchingWeatherLocation.collectAsStateWithLifecycle()
    val weatherSearchQuery by viewModel.weatherSearchQuery.collectAsStateWithLifecycle()
    val weatherErrorMessage by viewModel.weatherErrorMessage.collectAsStateWithLifecycle()
    val dataSourceMode by viewModel.dataSourceMode.collectAsStateWithLifecycle()
    val endpointUrl by viewModel.endpointUrl.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val testFeedback by viewModel.testFeedback.collectAsStateWithLifecycle()
    val commandStatus by viewModel.commandStatus.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showArchitectureScreen by remember { mutableStateOf(false) }

    val formattedElapsed = viewModel.getFormattedElapsed(systemStatus.lastUpdatedEpochMs)

    if (showSettingsDialog) {
        ArduinoConfigDialog(
            currentEndpoint = endpointUrl,
            isTesting = isTesting,
            testFeedback = testFeedback,
            connectionStatus = connectionStatus,
            systemStatus = systemStatus,
            onSaveEndpoint = { viewModel.saveArduinoIp(it) },
            onConnect = { viewModel.connectToArduino(it) },
            onDismiss = {
                viewModel.clearFeedback()
                showSettingsDialog = false
            }
        )
    }

    if (showArchitectureScreen) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "System Architecture",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary
                    )
                    IconButton(onClick = { showArchitectureScreen = false }) {
                        Text("Close", fontWeight = FontWeight.Bold, color = AquaPrimary, fontSize = 14.sp)
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                ArchitectureScreen()
            }
        }
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 700.dp

        if (isWideScreen) {
            // Adaptive Tablet / Desktop Layout with Navigation Rail
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                NavigationRail(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    header = {
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .testTag("rail_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = AquaPrimary
                            )
                        }
                    }
                ) {
                    AppDestination.entries.forEach { destination ->
                        val selected = destination == currentDestination
                        NavigationRailItem(
                            selected = selected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(destination.title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = AquaPrimary,
                                indicatorColor = AquaPrimary
                            ),
                            modifier = Modifier.testTag(destination.testTag)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 1000.dp)
                    ) {
                        AppContent(
                            destination = currentDestination,
                            sensorData = sensorData,
                            connectionStatus = connectionStatus,
                            systemStatus = systemStatus,
                            deviceMetadata = deviceMetadata,
                            filteredHistory = filteredHistory,
                            selectedFilter = historyFilter,
                            weatherInfo = weatherInfo,
                            isRefreshingWeather = isRefreshingWeather,
                            weatherErrorMessage = weatherErrorMessage,
                            weatherSearchResults = weatherSearchResults,
                            isSearchingWeatherLocation = isSearchingWeatherLocation,
                            weatherSearchQuery = weatherSearchQuery,
                            commandStatus = commandStatus,
                            formattedElapsed = formattedElapsed,
                            dataSourceMode = dataSourceMode,
                            userProfile = userProfile,
                            onFilterSelected = { viewModel.setHistoryFilter(it) },
                            onSendPumpCommand = { viewModel.requestPumpAction(it) },
                            onSetMode = { viewModel.setSystemMode(it) },
                            onClearCommandStatus = { viewModel.clearCommandFeedback() },
                            onRefreshWeather = { viewModel.refreshWeather() },
                            onSelectWeatherLocation = { viewModel.selectWeatherLocation(it) },
                            onSearchWeatherLocation = { viewModel.searchWeatherLocation(it) },
                            onClearWeatherSearch = { viewModel.clearWeatherSearch() },
                            onOpenSettings = { showSettingsDialog = true },
                            onOpenArchitecture = { showArchitectureScreen = true },
                            onSignOut = onSignOut
                        )
                    }
                }
            }
        } else {
            // Mobile Phone Layout with Bottom Navigation Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        AppDestination.entries.forEach { destination ->
                            val selected = destination == currentDestination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = AquaPrimary,
                                    indicatorColor = AquaPrimary
                                ),
                                modifier = Modifier.testTag(destination.testTag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AppContent(
                        destination = currentDestination,
                        sensorData = sensorData,
                        connectionStatus = connectionStatus,
                        systemStatus = systemStatus,
                        deviceMetadata = deviceMetadata,
                        filteredHistory = filteredHistory,
                        selectedFilter = historyFilter,
                        weatherInfo = weatherInfo,
                        isRefreshingWeather = isRefreshingWeather,
                        weatherErrorMessage = weatherErrorMessage,
                        weatherSearchResults = weatherSearchResults,
                        isSearchingWeatherLocation = isSearchingWeatherLocation,
                        weatherSearchQuery = weatherSearchQuery,
                        commandStatus = commandStatus,
                        formattedElapsed = formattedElapsed,
                        dataSourceMode = dataSourceMode,
                        userProfile = userProfile,
                        onFilterSelected = { viewModel.setHistoryFilter(it) },
                        onSendPumpCommand = { viewModel.requestPumpAction(it) },
                        onSetMode = { viewModel.setSystemMode(it) },
                        onClearCommandStatus = { viewModel.clearCommandFeedback() },
                        onRefreshWeather = { viewModel.refreshWeather() },
                        onSelectWeatherLocation = { viewModel.selectWeatherLocation(it) },
                        onSearchWeatherLocation = { viewModel.searchWeatherLocation(it) },
                        onClearWeatherSearch = { viewModel.clearWeatherSearch() },
                        onOpenSettings = { showSettingsDialog = true },
                        onOpenArchitecture = { showArchitectureScreen = true },
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}

@Composable
private fun AppContent(
    destination: AppDestination,
    sensorData: com.example.data.model.AquaSustainData,
    connectionStatus: com.example.data.model.ConnectionStatus,
    systemStatus: com.example.data.model.SystemStatusSummary,
    deviceMetadata: com.example.data.model.DeviceItem,
    filteredHistory: List<com.example.data.model.SensorReading>,
    selectedFilter: HistoryTimeFilter,
    weatherInfo: com.example.data.model.WeatherInfo,
    isRefreshingWeather: Boolean,
    weatherErrorMessage: String?,
    weatherSearchResults: List<com.example.data.model.GeoLocation>,
    isSearchingWeatherLocation: Boolean,
    weatherSearchQuery: String,
    commandStatus: com.example.data.repository.CommandStatus,
    formattedElapsed: String,
    dataSourceMode: DataSourceMode,
    userProfile: com.example.data.model.UserProfile?,
    onFilterSelected: (HistoryTimeFilter) -> Unit,
    onSendPumpCommand: (Boolean) -> Unit,
    onSetMode: (String) -> Unit,
    onClearCommandStatus: () -> Unit,
    onRefreshWeather: () -> Unit,
    onSelectWeatherLocation: (com.example.data.model.GeoLocation) -> Unit,
    onSearchWeatherLocation: (String) -> Unit,
    onClearWeatherSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenArchitecture: () -> Unit,
    onSignOut: () -> Unit
) {
    Crossfade(targetState = destination, label = "screen_crossfade") { target ->
        when (target) {
            AppDestination.HOME -> DashboardScreen(
                sensorData = sensorData,
                connectionStatus = connectionStatus,
                systemStatus = systemStatus,
                deviceMetadata = deviceMetadata,
                weatherInfo = weatherInfo,
                commandStatus = commandStatus,
                formattedElapsed = formattedElapsed,
                dataSourceMode = dataSourceMode,
                onSendPumpCommand = onSendPumpCommand,
                onSetMode = onSetMode,
                onClearCommandStatus = onClearCommandStatus,
                onOpenSettings = onOpenSettings,
                onSignOut = onSignOut
            )
            AppDestination.SENSORS -> SensorsScreen(
                sensorData = sensorData
            )
            AppDestination.IRRIGATION -> IrrigationScreen(
                sensorData = sensorData,
                commandStatus = commandStatus,
                onSendPumpCommand = onSendPumpCommand,
                onSetMode = onSetMode,
                onClearCommandStatus = onClearCommandStatus
            )
            AppDestination.WEATHER -> WeatherScreen(
                weather = weatherInfo,
                isRefreshing = isRefreshingWeather,
                weatherErrorMessage = weatherErrorMessage,
                searchResults = weatherSearchResults,
                isSearching = isSearchingWeatherLocation,
                searchQuery = weatherSearchQuery,
                onRefreshWeather = onRefreshWeather,
                onSelectLocation = onSelectWeatherLocation,
                onSearchLocation = onSearchWeatherLocation,
                onClearSearch = onClearWeatherSearch
            )
            AppDestination.HISTORY -> HistoryScreen(
                readings = filteredHistory,
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            )
            AppDestination.ACCOUNT -> AccountScreen(
                userProfile = userProfile,
                onSignOut = onSignOut,
                onOpenSettings = onOpenSettings,
                onOpenArchitecture = onOpenArchitecture
            )
        }
    }
}

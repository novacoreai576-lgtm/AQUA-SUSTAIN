package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyForecastItemData
import com.example.data.model.GeoLocation
import com.example.data.model.HourlyForecastItemData
import com.example.data.model.WeatherInfo
import com.example.data.remote.WeatherApiClient
import com.example.ui.components.WeatherAiCard
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherScreen(
    weather: WeatherInfo,
    isRefreshing: Boolean = false,
    weatherErrorMessage: String? = null,
    searchResults: List<GeoLocation> = emptyList(),
    isSearching: Boolean = false,
    searchQuery: String = "",
    onRefreshWeather: () -> Unit = {},
    onSelectLocation: (GeoLocation) -> Unit = {},
    onSearchLocation: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showLocationDialog by remember { mutableStateOf(false) }

    // Location Picker Dialog
    if (showLocationDialog) {
        LocationPickerDialog(
            currentLocationName = weather.locationName,
            searchQuery = searchQuery,
            isSearching = isSearching,
            searchResults = if (searchResults.isNotEmpty()) searchResults else WeatherApiClient.POPULAR_FARM_LOCATIONS,
            onSearchQueryChanged = { onSearchLocation(it) },
            onSelectLocation = { loc ->
                onSelectLocation(loc)
                showLocationDialog = false
            },
            onDismiss = {
                onClearSearch()
                showLocationDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("weather_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title & Refresh Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Weather & AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AquaPrimary
                )
                Text(
                    text = "Live Forecast & Automated Irrigation Horizon",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralMedium
                )
            }

            // Refresh & Location Action Icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showLocationDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .testTag("weather_location_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Change Location",
                        tint = AquaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val infiniteTransition = rememberInfiniteTransition(label = "rotation")
                val rotationAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = LinearEasing)
                    ),
                    label = "spin"
                )

                IconButton(
                    onClick = onRefreshWeather,
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .testTag("weather_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Weather",
                        tint = if (isRefreshing) EcoGreen else AquaPrimary,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(if (isRefreshing) rotationAngle else 0f)
                    )
                }
            }
        }

        // Live Provider & Location Status Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EcoGreen.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showLocationDialog = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EcoGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open-Meteo REST API • ${weather.locationName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Updated: ${weather.lastUpdatedTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontSize = 10.sp
                )
            }
        }

        // Error message banner if any
        if (weatherErrorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = weatherErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB91C1C),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRefreshWeather) {
                        Text("Retry", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }
            }
        }

        // Main Weather & AI Card
        WeatherAiCard(weather = weather)

        // 24-Hour Precipitation & Temperature Horizon
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
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = WaterBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "24-Hour Atmospheric Horizon",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaterBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hourly items row
                val hourlyItems = if (weather.hourlyForecast.isNotEmpty()) {
                    weather.hourlyForecast
                } else {
                    listOf(
                        HourlyForecastItemData("Now", weather.temperatureC, weather.rainProbabilityPercent, weather.condition),
                        HourlyForecastItemData("+2h", (weather.temperatureC - 1), (weather.rainProbabilityPercent + 5).coerceAtMost(100), "Cloudy"),
                        HourlyForecastItemData("+4h", (weather.temperatureC - 2), (weather.rainProbabilityPercent - 10).coerceAtLeast(0), "Partly Cloudy"),
                        HourlyForecastItemData("+6h", (weather.temperatureC - 3), 20, "Clear Sky")
                    )
                }

                val horizontalScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hourlyItems.take(12).forEach { item ->
                        HourlyForecastCard(item = item)
                    }
                }
            }
        }

        // 7-Day Multi-Day Atmospheric Outlook
        if (weather.dailyForecast.isNotEmpty()) {
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
                        Icon(Icons.Default.Thermostat, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "7-Day Farm Precipitation Outlook",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    weather.dailyForecast.forEach { daily ->
                        DailyForecastRow(daily = daily)
                    }
                }
            }
        }

        // Sustainable Water Strategy & AI Climate Adaptation
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
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sustainable Water Strategy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                }

                Text(
                    text = "AquaSustain combines live Open-Meteo precipitation horizons with physical Arduino UNO R4 WiFi sensors (Rain D2, Soil Moisture A0, Water Level A1). When precipitation probability exceeds 65%, irrigation is automatically withheld to maximize natural soil absorption and preserve storage reservoir volume.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium,
                    lineHeight = 18.sp
                )

                // Decision Priority Rules
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "AUTOMATED IRRIGATION PRIORITIES:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AquaPrimary,
                            fontSize = 10.sp
                        )
                        Text(text = "1. Rain Protection: Active rain shuts down pump.", fontSize = 11.sp, color = NeutralDark)
                        Text(text = "2. Low Water Protection: Water < 20% locks pump to prevent dry run.", fontSize = 11.sp, color = NeutralDark)
                        Text(text = "3. Soil Irrigation: Soil < 50% turns pump ON unless rain is forecast.", fontSize = 11.sp, color = NeutralDark)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HourlyForecastCard(item: HourlyForecastItemData) {
    val icon = getWeatherIcon(item.weatherCode)
    val iconTint = getWeatherIconTint(item.weatherCode)

    Box(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.time,
                style = MaterialTheme.typography.labelSmall,
                color = NeutralMedium,
                fontWeight = FontWeight.Medium
            )

            Icon(
                imageVector = icon,
                contentDescription = item.condition,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = "${item.tempC}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = WaterBlue,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${item.rainProb}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WaterBlue
                )
            }
        }
    }
}

@Composable
private fun DailyForecastRow(daily: DailyForecastItemData) {
    val icon = getWeatherIcon(daily.weatherCode)
    val iconTint = getWeatherIconTint(daily.weatherCode)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1.2f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = daily.condition,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = daily.day,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
                Text(
                    text = daily.condition,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontSize = 11.sp
                )
            }
        }

        // Rain Probability & Accumulation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WaterBlue, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "${daily.rainProb}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = WaterBlue
            )
            if (daily.rainSumMm > 0.0) {
                Text(
                    text = " (${daily.rainSumMm}mm)",
                    fontSize = 11.sp,
                    color = NeutralMedium
                )
            }
        }

        // Temp range: High / Low
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${daily.maxTempC}°",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            )
            Text(
                text = " / ${daily.minTempC}°",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LocationPickerDialog(
    currentLocationName: String,
    searchQuery: String,
    isSearching: Boolean,
    searchResults: List<GeoLocation>,
    onSearchQueryChanged: (String) -> Unit,
    onSelectLocation: (GeoLocation) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AquaPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Farm Location", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Live weather forecast will be fetched directly from Open-Meteo REST API for your selected coordinates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("location_search_input"),
                    placeholder = { Text("Search city, district, or region...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AquaPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = NeutralMedium)
                            }
                        }
                    },
                    singleLine = true
                )

                if (isSearching) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AquaPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Searching global weather stations...", fontSize = 12.sp, color = NeutralMedium)
                    }
                }

                Text(
                    text = if (searchQuery.isNotBlank()) "Search Results" else "Popular Agricultural Hubs",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    searchResults.forEach { loc ->
                        val isSelected = loc.displayName == currentLocationName || loc.name == currentLocationName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFE0F2FE) else Color(0xFFF8FAFC))
                                .clickable { onSelectLocation(loc) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = loc.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) AquaPrimary else NeutralDark
                                )
                                Text(
                                    text = "${loc.latitude.toString().take(6)}°, ${loc.longitude.toString().take(6)}°",
                                    fontSize = 10.sp,
                                    color = NeutralMedium
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = AquaPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AquaPrimary)
            }
        }
    )
}

private fun getWeatherIcon(code: Int): ImageVector {
    return when (code) {
        0 -> Icons.Default.WbSunny
        1, 2 -> Icons.Default.Cloud
        3 -> Icons.Default.Cloud
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Default.WaterDrop
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.Cloud
    }
}

private fun getWeatherIconTint(code: Int): Color {
    return when (code) {
        0 -> Color(0xFFF59E0B)
        1, 2 -> Color(0xFF38BDF8)
        3 -> Color(0xFF94A3B8)
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> WaterBlue
        95, 96, 99 -> Color(0xFF7C3AED)
        else -> Color(0xFF64748B)
    }
}

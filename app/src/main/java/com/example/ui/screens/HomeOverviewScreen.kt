package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppDestination
import com.example.R
import com.example.data.model.AquaSustainData
import com.example.data.model.ConnectionStatus
import com.example.data.model.DeviceItem
import com.example.data.model.WaterQualityAnalysisRecord
import com.example.data.model.WaterUsageSummary
import com.example.data.model.WeatherInfo
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.AquaPrimaryContainer
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySlate
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.theme.WaterBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeOverviewScreen(
    sensorData: AquaSustainData,
    connectionStatus: ConnectionStatus,
    deviceMetadata: DeviceItem,
    endpointIp: String,
    formattedElapsed: String,
    weatherInfo: WeatherInfo,
    isRefreshingWeather: Boolean,
    latestAnalyses: List<WaterQualityAnalysisRecord>,
    usageSummary: WaterUsageSummary,
    isGeminiConfigured: Boolean,
    onNavigate: (AppDestination) -> Unit,
    onOpenDeviceSettings: () -> Unit,
    onRefreshWeather: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isHardwareConnected = sensorData.hasLiveData && connectionStatus is ConnectionStatus.Connected
    val latestQuality = latestAnalyses.firstOrNull()

    // Real system notifications strictly derived from actual state (never invented)
    val notifications = buildRealNotifications(
        isHardwareConnected = isHardwareConnected,
        sensorData = sensorData,
        latestQuality = latestQuality,
        usageSummary = usageSummary,
        isGeminiConfigured = isGeminiConfigured,
        weatherInfo = weatherInfo
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("home_overview_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Header Card (Single AquaSustain Mode Identity)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDeep),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.img_aquasustain_hero),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = 0.22f,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(NavyDeep.copy(alpha = 0.94f), NavySlate.copy(alpha = 0.78f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AquaPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "AquaSustain",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AquaSustain",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Intelligent Water Sustainability Platform",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF93C5FD)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isHardwareConnected) EcoGreen.copy(alpha = 0.28f)
                                    else AlertRed.copy(alpha = 0.28f)
                                )
                                .border(
                                    1.dp,
                                    if (isHardwareConnected) Color(0xFF4ADE80) else Color(0xFFFCA5A5),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isHardwareConnected) "CONNECTED" else "DISCONNECTED",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Text(
                        text = if (isHardwareConnected) {
                            "Live telemetry active from ${deviceMetadata.deviceId}. Monitoring tank reserve, soil moisture, rain detection, and pump state."
                        } else {
                            "Device Not Connected. Connect your Arduino UNO R4 WiFi monitoring node or use Water Quality, Water Usage, and AI Water Assistant tools."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(AppDestination.WATER_QUALITY) },
                            colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("home_cta_quality")
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quality Check", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenDeviceSettings,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF60A5FA)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("home_cta_connect_device")
                        ) {
                            Icon(Icons.Default.DeveloperBoard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHardwareConnected) "Device Setup" else "Connect Device",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Current Water Monitoring Status Card
        HomeSectionCard(
            title = "CURRENT WATER MONITORING STATUS",
            subtitle = if (isHardwareConnected) "Live Arduino Telemetry (${deviceMetadata.deviceId})" else "Hardware Telemetry",
            icon = Icons.Default.Sensors,
            iconTint = if (isHardwareConnected) EcoGreen else AlertRed,
            actionLabel = "Water Dashboard",
            onAction = { onNavigate(AppDestination.WATER_DASHBOARD) }
        ) {
            if (!isHardwareConnected) {
                EmptyConnectionNotice(
                    headline = "Device Not Connected.",
                    detail = "Waiting for data connection from $endpointIp. Last sync: $formattedElapsed.",
                    buttonLabel = "Configure Connection",
                    onClick = onOpenDeviceSettings
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeStatTile(
                        label = "Tank Level",
                        value = "${sensorData.waterLevel}%",
                        sub = sensorData.tankLevelCategory.label,
                        accent = WaterBlue,
                        modifier = Modifier.weight(1f)
                    )
                    HomeStatTile(
                        label = "Soil Moisture",
                        value = "${sensorData.soilMoisture}%",
                        sub = if (sensorData.isSoilDry) "Dry" else "Adequate",
                        accent = EcoGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeStatTile(
                        label = "Rain Sensor",
                        value = if (sensorData.rain) "Rain" else "Clear",
                        sub = "Pin D2",
                        accent = if (sensorData.rain) WaterBlue else WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    HomeStatTile(
                        label = "Pump Relay",
                        value = if (sensorData.pump) "ON" else "OFF",
                        sub = sensorData.mode.uppercase(),
                        accent = if (sensorData.pump) EcoGreen else NeutralMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Water Quality Status + Water Consumption Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Water Quality Summary Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(AppDestination.WATER_QUALITY) }
                    .testTag("home_quality_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WATER QUALITY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeutralMedium
                        )
                    }
                    if (latestQuality == null) {
                        Text(
                            text = "No Records",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NeutralDark
                        )
                        Text(
                            text = "Waiting for water quality measurements.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = latestQuality.statusCategory,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = when {
                                latestQuality.statusCategory.contains("Concern", true) -> AlertRed
                                latestQuality.statusCategory.contains("Within", true) -> EcoGreen
                                else -> WarningAmber
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${latestQuality.sourceName} • pH ${latestQuality.ph ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Water Consumption Summary Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(AppDestination.WATER_USAGE) }
                    .testTag("home_usage_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Opacity, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WATER USAGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeutralMedium
                        )
                    }
                    if (usageSummary.totalRecords == 0) {
                        Text(
                            text = "0 Logged",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NeutralDark
                        )
                        Text(
                            text = "Waiting for usage entries to compute totals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = WaterUsageSummary.formatLiters(usageSummary.todayLiters),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = AquaPrimary
                        )
                        Text(
                            text = "7d: ${WaterUsageSummary.formatLiters(usageSummary.weeklyLiters)} (${usageSummary.totalRecords} logs)",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. Available Monitoring Devices
        HomeSectionCard(
            title = "AVAILABLE MONITORING DEVICES",
            subtitle = "Registered Sensor Nodes",
            icon = Icons.Default.DeveloperBoard,
            iconTint = AquaPrimary,
            actionLabel = "Configure",
            onAction = onOpenDeviceSettings
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${deviceMetadata.deviceId} • ${deviceMetadata.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                    Text(
                        text = "${deviceMetadata.board} • http://$endpointIp/data",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )
                    Text(
                        text = "Last sync: $formattedElapsed",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isHardwareConnected) EcoGreenContainer else AlertRedContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isHardwareConnected) "ONLINE" else "OFFLINE",
                        color = if (isHardwareConnected) EcoGreen else AlertRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // 5. Recent Analysis Records
        HomeSectionCard(
            title = "RECENT ANALYSIS RECORDS",
            subtitle = "${latestAnalyses.size} Saved Water Quality Reports",
            icon = Icons.Default.Analytics,
            iconTint = AquaPrimary,
            actionLabel = "Analyze / View All",
            onAction = { onNavigate(AppDestination.WATER_QUALITY) }
        ) {
            if (latestAnalyses.isEmpty()) {
                Text(
                    text = "Waiting for data connection or user analysis submission. No saved water quality records yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )
            } else {
                val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    latestAnalyses.take(3).forEach { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .clickable { onNavigate(AppDestination.WATER_QUALITY) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rec.sourceName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark
                                )
                                Text(
                                    text = "${df.format(Date(rec.createdAtEpochMs))} • pH ${rec.ph ?: "N/A"} • TDS ${rec.tdsPpm ?: "N/A"} ppm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeutralMedium
                                )
                            }
                            Text(
                                text = rec.statusCategory,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AquaPrimary
                            )
                        }
                    }
                }
            }
        }

        // 6. Environmental Insights
        HomeSectionCard(
            title = "ENVIRONMENTAL INSIGHTS",
            subtitle = if (weatherInfo.hasFetched) weatherInfo.locationName else "Open-Meteo Weather Feed",
            icon = Icons.Default.Cloud,
            iconTint = WaterBlue,
            actionLabel = if (isRefreshingWeather) "Syncing..." else "Refresh",
            onAction = onRefreshWeather
        ) {
            if (!weatherInfo.hasFetched) {
                EmptyConnectionNotice(
                    headline = "Waiting for data connection.",
                    detail = "Live atmospheric and precipitation forecasts will appear once Open-Meteo responds.",
                    buttonLabel = "Fetch Live Weather",
                    onClick = onRefreshWeather
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${weatherInfo.temperatureC}°C • ${weatherInfo.condition} • Rain Likelihood ${weatherInfo.rainProbabilityPercent}% • Humidity ${weatherInfo.humidityPercent}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )
                    Text(
                        text = weatherInfo.smartRecommendation,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // 7. Important Notifications
        HomeSectionCard(
            title = "IMPORTANT NOTIFICATIONS",
            subtitle = "${notifications.size} Active System Notices",
            icon = Icons.Default.Notifications,
            iconTint = WarningAmber,
            actionLabel = "AI Assistant",
            onAction = { onNavigate(AppDestination.AI_ASSISTANT) }
        ) {
            if (notifications.isEmpty()) {
                Text(
                    text = "All connected services are operating normally with no alerts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EcoGreen,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    notifications.forEach { notice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (notice.isCritical) AlertRedContainer else WarningAmberContainer)
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (notice.isCritical) Icons.Default.WifiOff else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (notice.isCritical) AlertRed else WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = notice.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (notice.isCritical) AlertRed else Color(0xFF92400E)
                                )
                                Text(
                                    text = notice.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralDark,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class HomeNotice(
    val title: String,
    val message: String,
    val isCritical: Boolean
)

private fun buildRealNotifications(
    isHardwareConnected: Boolean,
    sensorData: AquaSustainData,
    latestQuality: WaterQualityAnalysisRecord?,
    usageSummary: WaterUsageSummary,
    isGeminiConfigured: Boolean,
    weatherInfo: WeatherInfo
): List<HomeNotice> {
    val list = mutableListOf<HomeNotice>()
    if (!isHardwareConnected) {
        list.add(
            HomeNotice(
                title = "Device Not Connected",
                message = "Arduino UNO R4 WiFi monitoring hardware is offline. Live tank and soil telemetry cannot be streamed until connected.",
                isCritical = true
            )
        )
    } else {
        if (sensorData.isWaterLow) {
            list.add(
                HomeNotice(
                    title = "Low Reservoir Warning (${sensorData.waterLevel}%)",
                    message = "Underground tank level is below 20%. Pump safety interlock is active.",
                    isCritical = true
                )
            )
        }
        if (sensorData.rain) {
            list.add(
                HomeNotice(
                    title = "Rain Detected on Sensor D2",
                    message = "Automated irrigation is paused to harvest rainfall efficiently.",
                    isCritical = false
                )
            )
        }
    }

    if (!isGeminiConfigured) {
        list.add(
            HomeNotice(
                title = "Gemini AI API Key Needed",
                message = "Add GEMINI_API_KEY in the AI Studio Secrets panel to enable Water Quality AI analysis and AI Water Assistant.",
                isCritical = false
            )
        )
    }

    if (latestQuality != null && latestQuality.warnings.isNotEmpty()) {
        list.add(
            HomeNotice(
                title = "Water Quality Notice (${latestQuality.sourceName})",
                message = latestQuality.warnings.first(),
                isCritical = latestQuality.statusCategory.contains("Concern", true)
            )
        )
    }

    usageSummary.wastageAlerts.forEach { alert ->
        list.add(
            HomeNotice(
                title = "Water Usage Spike Alert",
                message = alert,
                isCritical = false
            )
        )
    }

    if (weatherInfo.hasFetched && weatherInfo.rainProbabilityPercent >= 65) {
        list.add(
            HomeNotice(
                title = "High Rain Likelihood (${weatherInfo.rainProbabilityPercent}%)",
                message = "Incoming precipitation forecast for ${weatherInfo.locationName}. Delaying irrigation saves stored water.",
                isCritical = false
            )
        )
    }

    return list
}

@Composable
private fun HomeSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    actionLabel: String,
    onAction: () -> Unit,
    content: @Composable () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeutralMedium,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun EmptyConnectionNotice(
    headline: String,
    detail: String,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = headline,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AlertRed
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = NeutralMedium,
            lineHeight = 16.sp
        )
        TextButton(onClick = onClick) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(buttonLabel, color = AquaPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeStatTile(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = NeutralMedium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = accent
        )
        Text(
            text = sub,
            style = MaterialTheme.typography.labelSmall,
            color = NeutralDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

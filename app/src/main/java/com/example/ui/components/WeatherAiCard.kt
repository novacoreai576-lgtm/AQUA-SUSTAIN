package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IrrigationAdvice
import com.example.data.model.WeatherInfo
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue

@Composable
fun WeatherAiCard(
    weather: WeatherInfo,
    advice: IrrigationAdvice? = null,
    isAiAnalyzing: Boolean = false,
    isRefreshingWeather: Boolean = false,
    weatherErrorMessage: String? = null,
    onRefreshWeather: (() -> Unit)? = null,
    onOpenLocationPicker: (() -> Unit)? = null,
    onRefreshAiAdvice: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var areTipsExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_ai_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            // Top Header: Title, Live Tag, AI Status & Refresh
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
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF10B981))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI WEATHER & IRRIGATION ADVISOR",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeutralMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            if (weather.isLive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EcoGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE FORECAST",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = if (onOpenLocationPicker != null) {
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onOpenLocationPicker() }
                                    .padding(vertical = 2.dp)
                                    .testTag("weather_location_button")
                            } else {
                                Modifier
                            }
                        ) {
                            Text(
                                text = weather.locationName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeutralDark,
                                maxLines = 1
                            )
                            if (onOpenLocationPicker != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Change farm location",
                                    tint = AquaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // AI Badge & Refresh Action
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val aiBadgeText = if (advice?.generatedByAi == true) "GEMINI AI" else "SMART ADVISOR"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEDE9FE))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = aiBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    if (onRefreshAiAdvice != null || onRefreshWeather != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        val isBusy = isAiAnalyzing || isRefreshingWeather
                        IconButton(
                            onClick = {
                                onRefreshWeather?.invoke()
                                onRefreshAiAdvice?.invoke()
                            },
                            enabled = !isBusy,
                            modifier = Modifier.testTag("refresh_ai_advice_button")
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF6D28D9)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Weather and AI Advice",
                                    tint = NeutralMedium,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!weatherErrorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                WarningBanner(
                    icon = Icons.Default.Warning,
                    iconTint = AlertRed,
                    bgColor = Color(0xFFFEF2F2),
                    borderColor = Color(0xFFFECACA),
                    title = "Weather Sync Warning",
                    message = weatherErrorMessage
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weather Metrics Grid (Row 1: Temperature & Rain Probability)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WeatherMetricPill(
                    icon = Icons.Default.Thermostat,
                    iconTint = Color(0xFFF97316),
                    label = "Temperature",
                    value = "${weather.temperatureC}°C",
                    sub = "Feels ${weather.feelsLikeC}°C • ${weather.condition}",
                    modifier = Modifier.weight(1f)
                )

                WeatherMetricPill(
                    icon = Icons.Default.WaterDrop,
                    iconTint = WaterBlue,
                    label = "Rain Probability",
                    value = "${weather.rainProbabilityPercent}%",
                    sub = weather.rainPredictionText,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weather Metrics Grid (Row 2: Humidity & Wind / Precipitation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WeatherMetricPill(
                    icon = Icons.Default.Cloud,
                    iconTint = Color(0xFF64748B),
                    label = "Ambient Humidity",
                    value = "${weather.humidityPercent}%",
                    sub = if (weather.humidityPercent > 70) "High humidity" else "Normal range",
                    modifier = Modifier.weight(1f)
                )

                WeatherMetricPill(
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFF0EA5E9),
                    label = "Wind & Precip",
                    value = "${weather.windSpeedKmh} km/h",
                    sub = if (weather.precipitationMm > 0) "${weather.precipitationMm} mm rain" else "No active rain",
                    modifier = Modifier.weight(1f)
                )
            }

            // Warnings Section: Expected Rain & Low Water Tank Warning
            val rainWarning = advice?.rainWarning
            val tankWarning = advice?.tankWarning

            if (!rainWarning.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                WarningBanner(
                    icon = Icons.Default.Cloud,
                    iconTint = WaterBlue,
                    bgColor = Color(0xFFF0F9FF),
                    borderColor = Color(0xFFBAE6FD),
                    title = "Rain Forecast Advisory",
                    message = rainWarning
                )
            }

            if (!tankWarning.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                WarningBanner(
                    icon = Icons.Default.WaterDamage,
                    iconTint = AlertRed,
                    bgColor = Color(0xFFFEF2F2),
                    borderColor = Color(0xFFFECACA),
                    title = "Water Tank Low Warning",
                    message = tankWarning
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary AI Irrigation Recommendation Callout
            val displayRecommendation = advice?.recommendation ?: "AI IRRIGATION ADVICE"
            val displayExplanation = advice?.explanation ?: weather.smartRecommendation
            val isActionRequired = advice?.shouldIrrigate == true

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isActionRequired) Color(0xFFF0FDF4) else Color(0xFFF8FAFC))
                    .border(
                        1.dp,
                        if (isActionRequired) EcoGreen.copy(alpha = 0.4f) else Color(0xFFE2E8F0),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isActionRequired) EcoGreen else Color(0xFF64748B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayRecommendation,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isActionRequired) Color(0xFF166534) else NeutralDark,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Advice Only • Safety Locked",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeutralMedium,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = displayExplanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralDark,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Water-Saving Tips Accordion
            val tips = advice?.waterSavingTips.orEmpty()
            if (tips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEFCE8))
                        .border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(12.dp))
                        .clickable { areTipsExpanded = !areTipsExpanded }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFCA8A04),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "💡 Water-Saving Tips (${tips.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF854D0E)
                            )
                        }
                        Icon(
                            imageVector = if (areTipsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF854D0E),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AnimatedVisibility(visible = areTipsExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tips.forEach { tip ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = "• ",
                                        color = Color(0xFF854D0E),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF713F12),
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WarningBanner(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    bgColor: Color,
    borderColor: Color,
    title: String,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = iconTint,
                    fontSize = 11.sp
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralDark,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun WeatherMetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = NeutralDark
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeoLocation
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium

@Composable
fun WeatherLocationPickerDialog(
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
                Text(
                    text = "Select Farm Location",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
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
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = NeutralMedium)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = AquaPrimary,
                        focusedLabelColor = AquaPrimary,
                        cursorColor = Color.Black
                    )
                )

                if (isSearching) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = AquaPrimary
                        )
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
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                                .testTag("location_option_${loc.name.lowercase().replace(" ", "_")}"),
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
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = AquaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AquaPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

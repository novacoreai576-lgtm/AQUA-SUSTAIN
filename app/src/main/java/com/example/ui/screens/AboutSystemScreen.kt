package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DeviceItem
import com.example.data.model.UserProfile
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.AquaPrimaryContainer
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueContainer

@Composable
fun AboutSystemScreen(
    userProfile: UserProfile?,
    deviceMetadata: DeviceItem?,
    isLoggedIn: Boolean,
    onSignOut: () -> Unit,
    onOpenAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("about_system_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Column {
            Text(
                text = "About AquaSustain",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AquaPrimary
            )
            Text(
                text = "Smart Rainwater Harvesting & Sustainable Irrigation",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMedium
            )
        }

        // User Account & Device Profile Card
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AquaPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = AquaPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "USER ACCOUNT & CLOUD SYNC",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeutralMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = userProfile?.name ?: "AquaSustain User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeutralDark
                            )
                        }
                    }

                    // Role Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EcoGreenContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = userProfile?.role?.uppercase() ?: "USER",
                            color = EcoGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Email: ${userProfile?.email ?: "Synced"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralDark
                    )
                    Text(
                        text = "UID: ${userProfile?.uid ?: "Registered"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Active Device: ${deviceMetadata?.name ?: "AquaSustain Farm"} (${deviceMetadata?.board ?: "Arduino UNO R4 WiFi"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = AquaPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onSignOut,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logout_button")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Hero Image
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.img_aquasustain_hero),
                    contentDescription = "AquaSustain Rainwater Harvesting Farm Illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Integrated Sloped-Field Catchment & Bio-Filtration",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Sustainable Development Goals (SDG 6 and SDG 12)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "UN SUSTAINABLE DEVELOPMENT GOALS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SdgCard(
                        sdgNumber = "SDG 6",
                        title = "Clean Water & Sanitation",
                        description = "Maximizes freshwater collection, underground preservation, and bio-filtration.",
                        color = WaterBlue,
                        bgColor = WaterBlueContainer,
                        modifier = Modifier.weight(1f)
                    )

                    SdgCard(
                        sdgNumber = "SDG 12",
                        title = "Responsible Consumption",
                        description = "Zero-waste drip irrigation eliminates over-watering and soil salinization.",
                        color = EcoGreen,
                        bgColor = EcoGreenContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Core System Pillars List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "AQUASUSTAIN SYSTEM ELEMENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                PillarItem(
                    title = "Rainwater Harvesting",
                    desc = "Captures seasonal precipitation directly on-site to build self-sustaining farm water reserves."
                )

                PillarItem(
                    title = "Sloped Agricultural Field",
                    desc = "Engineered terrain elevation allows natural gravity-fed water drainage into collection channels."
                )

                PillarItem(
                    title = "Underground Perforated Pipe Collection",
                    desc = "Subsurface perforated pipes gather filtered soil seepage without eroding vital topsoil."
                )

                PillarItem(
                    title = "Waterproof-Sheet Collection",
                    desc = "Geomembrane impervious lining prevents unmanaged subsoil seepage and guides water to storage."
                )

                PillarItem(
                    title = "Underground Water Storage",
                    desc = "Subterranean reservoir shields collected rainwater from sun exposure, algae, and evaporation."
                )

                PillarItem(
                    title = "Filtration & Nostoc Biological Unit",
                    desc = "Physical filtration combined with symbiotic Nostoc cyanobacteria bio-conditioning for water stabilization."
                )

                PillarItem(
                    title = "Smart Drip Irrigation",
                    desc = "Low-pressure drip emitter network delivers precise moisture straight to root zones."
                )

                PillarItem(
                    title = "Arduino UNO R4 WiFi",
                    desc = "Embedded 32-bit ARM IoT microcontroller reading rain, soil moisture, and water level sensors autonomously."
                )

                PillarItem(
                    title = "Sensor-Based Irrigation",
                    desc = "Irrigation activates strictly when No Rain is detected AND soil is dry (<50%), preventing wastage."
                )

                PillarItem(
                    title = "Sustainable Agriculture",
                    desc = "Empowers farmers with resilient crop growth through climate adaptation and water security."
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SdgCard(
    sdgNumber: String,
    title: String,
    description: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = sdgNumber,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun PillarItem(
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EcoGreen,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeutralDark
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralMedium,
                lineHeight = 16.sp
            )
        }
    }
}

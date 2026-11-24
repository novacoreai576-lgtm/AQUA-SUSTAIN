package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AquaSustainData
import com.example.data.model.WaterAssistantMessage
import com.example.data.remote.GeminiWaterAiService
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.AquaPrimaryContainer
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer

@Composable
fun AiWaterAssistantScreen(
    messages: List<WaterAssistantMessage>,
    selectedLanguage: String,
    isSending: Boolean,
    errorMessage: String?,
    isGeminiConfigured: Boolean,
    sensorData: AquaSustainData,
    onSelectLanguage: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val starterPrompts = listOf(
        "How should I interpret pH, TDS, and turbidity for rainwater harvesting?",
        "Give me practical water conservation steps based on my current AquaSustain data.",
        "தண்ணீர் தரம் மற்றும் சேமிப்பு குறித்து தமிழில் ஆலோசனை வழங்கவும்.",
        "నీటి నాణ్యత మరియు ఆదా గురించి తెలుగులో సలహా ఇవ్వండి."
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("ai_water_assistant_screen")
    ) {
        // Header + Clear button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Water Assistant",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AquaPrimary
                )
                Text(
                    text = if (sensorData.hasLiveData) {
                        "Context: Connected (${sensorData.deviceId} • Tank ${sensorData.waterLevel}%)"
                    } else {
                        "Context: Hardware Not Connected • Multilingual Gemini Assistant"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )
            }

            if (messages.isNotEmpty()) {
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.testTag("assistant_clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear conversation",
                        tint = NeutralMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Language Selector Bar (English, Tamil, Telugu, Malayalam, Hindi, Auto)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Translate, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(16.dp))
            GeminiWaterAiService.SUPPORTED_LANGUAGES.forEach { lang ->
                FilterChip(
                    selected = selectedLanguage == lang,
                    onClick = { onSelectLanguage(lang) },
                    label = { Text(lang, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AquaPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (!isGeminiConfigured) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarningAmberContainer)
                    .border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GEMINI_API_KEY is not configured. Add GEMINI_API_KEY in the AI Studio Secrets panel to chat with the AI Water Assistant.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralDark
                )
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AlertRedContainer)
                    .padding(10.dp)
            ) {
                Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = AlertRed)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Transcript Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AquaPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AquaPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ask AquaSustain AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeutralDark
                    )
                    Text(
                        text = "Ask about water quality, irrigation, rainwater harvesting, or conservation in English, Tamil, Telugu, Malayalam, Hindi, and more.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        starterPrompts.forEach { prompt ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isSending) {
                                        onClearError()
                                        onSendMessage(prompt)
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id.ifBlank { "${it.timestampEpochMs}_${it.role}" } }) { msg ->
                        val isUser = msg.role == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 560.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        )
                                    )
                                    .background(if (isUser) NavyDeep else Color.White)
                                    .border(
                                        1.dp,
                                        if (isUser) NavyDeep else Color(0xFFE2E8F0),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                if (!isUser) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = AquaPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AquaSustain AI",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AquaPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(
                                    text = msg.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) Color.White else NeutralDark,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    if (isSending) {
                        item {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AquaPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Thinking with Gemini AI...", style = MaterialTheme.typography.bodySmall, color = NeutralMedium)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Safety Disclaimer
        Text(
            text = "Advisory Notice: AI guidance cannot certify drinking water safety without certified laboratory testing.",
            style = MaterialTheme.typography.labelSmall,
            color = NeutralMedium,
            fontSize = 10.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Message Composer
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    onClearError()
                },
                placeholder = { Text("Ask about water quality, usage, or conservation...") },
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .testTag("assistant_message_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AquaPrimary,
                    focusedLabelColor = AquaPrimary
                )
            )

            IconButton(
                onClick = {
                    val toSend = inputText.trim()
                    if (toSend.isNotEmpty() && !isSending) {
                        inputText = ""
                        onSendMessage(toSend)
                    }
                },
                enabled = inputText.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank() && !isSending) AquaPrimary else Color(0xFFCBD5E1))
                    .testTag("assistant_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    tint = Color.White
                )
            }
        }
    }
}

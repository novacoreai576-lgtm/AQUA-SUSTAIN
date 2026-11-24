package com.example.ui.screens.bioleaf

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.BioLeafChatMessage
import com.example.data.model.BioLeafReportFormatter
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.EcoOnGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder

@Composable
fun BioLeafAssistantSection(
    messages: List<BioLeafChatMessage>,
    isResponding: Boolean,
    errorMessage: String?,
    onSendMessage: (String, Bitmap?) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var draftMessage by remember { mutableStateOf("") }
    var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var permissionHint by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            permissionHint = null
            attachedBitmap = decodeBitmapFromUri(context, uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            permissionHint = null
            attachedBitmap = bmp
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionHint = null
            cameraLauncher.launch(null)
        } else {
            permissionHint = "Camera permission denied. Use Gallery to attach a crop photo."
        }
    }

    LaunchedEffect(messages.size, isResponding) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val starterPrompts = listOf(
        "My tomato leaves have yellow spots. How can I manage this organically?",
        "தக்காளி இலையில் வெள்ளை பூச்சி உள்ளது. இயற்கை தீர்வு என்ன?",
        "टमाटर की फसल के लिए नीम के तेल का उपयोग कैसे करें?",
        "How should I apply Panchagavya and vermicompost for banana?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("bioleaf_assistant_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Agricultural Assistant",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = NeutralDark
                )
                Text(
                    text = "Multilingual Auto-Detect: English • தமிழ் • తెలుగు • മലയാളം • हिंदी",
                    style = MaterialTheme.typography.labelSmall,
                    color = EcoGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (messages.isNotEmpty()) {
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.size(48.dp).testTag("chat_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear conversation",
                        tint = NeutralMedium
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(EcoGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Eco, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Ask in any language or attach a plant photo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeutralDark
                            )
                        }
                        Text(
                            text = "BIO LEAF AI automatically detects your language and answers in the same language with organic & sustainable guidance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                        starterPrompts.forEachIndexed { idx, prompt ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EcoGreenContainer.copy(alpha = 0.5f))
                                    .clickable { draftMessage = prompt }
                                    .padding(12.dp)
                                    .testTag("chat_starter_$idx")
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EcoOnGreenContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                items(messages, key = { it.id.ifBlank { "${it.sender}_${it.timestampEpochMs}" } }) { msg ->
                    val isUser = msg.sender == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 310.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    )
                                )
                                .background(if (isUser) NeutralDark else Color(0xFFF1F5F9))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val bmp = remember(msg.imageThumbnailBase64) {
                                decodeBase64Thumbnail(msg.imageThumbnailBase64)
                            }
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Chat photo attachment",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) Color.White else NeutralDark
                            )
                            Text(
                                text = buildString {
                                    if (!isUser && msg.detectedLanguage.isNotBlank() && msg.detectedLanguage != "Auto") {
                                        append("${msg.detectedLanguage} • ")
                                    }
                                    append(BioLeafReportFormatter.formatTimestamp(msg.timestampEpochMs))
                                },
                                fontSize = 10.sp,
                                color = if (isUser) Color.White.copy(alpha = 0.7f) else NeutralMedium
                            )
                        }
                    }
                }
            }

            if (isResponding) {
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(EcoGreenContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = EcoGreen, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BIO LEAF AI is thinking...",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = EcoOnGreenContainer
                        )
                    }
                }
            }
        }

        if (permissionHint != null) {
            BioLeafErrorBanner(message = permissionHint!!)
        }
        if (errorMessage != null) {
            BioLeafErrorBanner(message = errorMessage, modifier = Modifier.testTag("chat_error_banner"))
        }

        if (attachedBitmap != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(EcoGreenContainer)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        bitmap = attachedBitmap!!.asImageBitmap(),
                        contentDescription = "Pending attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Photo attached to message",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = EcoOnGreenContainer
                    )
                }
                IconButton(onClick = { attachedBitmap = null }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove attachment", tint = EcoOnGreenContainer)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.size(48.dp).testTag("chat_gallery_button")
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Attach gallery photo", tint = EcoGreen)
            }
            IconButton(
                onClick = {
                    val hasCam = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasCam) cameraLauncher.launch(null)
                    else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.size(48.dp).testTag("chat_camera_button")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Capture crop photo", tint = NeutralDark)
            }

            OutlinedTextField(
                value = draftMessage,
                onValueChange = { draftMessage = it },
                placeholder = { Text("Ask about crops, pests, or soil...") },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).testTag("chat_message_input")
            )

            IconButton(
                onClick = {
                    val text = draftMessage
                    val bmp = attachedBitmap
                    draftMessage = ""
                    attachedBitmap = null
                    onSendMessage(text, bmp)
                },
                enabled = !isResponding && (draftMessage.isNotBlank() || attachedBitmap != null),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (!isResponding && (draftMessage.isNotBlank() || attachedBitmap != null)) EcoGreen
                        else EcoGreen.copy(alpha = 0.35f)
                    )
                    .testTag("chat_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message", tint = Color.White)
            }
        }
    }
}

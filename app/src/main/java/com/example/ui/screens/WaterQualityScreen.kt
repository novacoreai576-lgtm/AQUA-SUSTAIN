package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WaterQualityAnalysisRecord
import com.example.data.model.WaterQualityInput
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.AquaPrimaryContainer
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WaterQualityScreen(
    currentResult: WaterQualityAnalysisRecord?,
    savedRecords: List<WaterQualityAnalysisRecord>,
    isAnalyzing: Boolean,
    errorMessage: String?,
    saveFeedback: String?,
    isGeminiConfigured: Boolean,
    onAnalyze: (WaterQualityInput, Uri?) -> Unit,
    onSaveCurrentResult: () -> Unit,
    onDeleteRecord: (String) -> Unit,
    onClearFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var sourceName by remember { mutableStateOf("") }
    var phText by remember { mutableStateOf("") }
    var turbidityText by remember { mutableStateOf("") }
    var temperatureText by remember { mutableStateOf("") }
    var tdsText by remember { mutableStateOf("") }
    var ecText by remember { mutableStateOf("") }
    var doText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var inspectedRecord by remember { mutableStateOf<WaterQualityAnalysisRecord?>(null) }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            validationError = null
        }
    }

    val previewBitmap = remember(selectedImageUri) {
        selectedImageUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    if (inspectedRecord != null) {
        WaterQualityDetailDialog(
            record = inspectedRecord!!,
            onDismiss = { inspectedRecord = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("water_quality_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Water Quality Analysis",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AquaPrimary
            )
            Text(
                text = "Enter sensor or laboratory measurements for AI-assisted water interpretation",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMedium
            )
        }

        // Scientific Integrity Disclaimer Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AquaPrimaryContainer)
                .border(1.dp, AquaPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "An image alone cannot measure chemical water parameters (pH, TDS, EC, or Dissolved Oxygen). Enter actual sensor or laboratory measurements for quantitative evaluation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0C4A6E),
                    lineHeight = 18.sp
                )
            }
        }

        if (!isGeminiConfigured) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(WarningAmberContainer)
                    .border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "GEMINI_API_KEY is not configured. Add GEMINI_API_KEY in the AI Studio Secrets panel to enable real AI water quality analysis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralDark,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Measurement Form Card
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "MEASUREMENT INPUTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeutralMedium,
                    letterSpacing = 0.8.sp
                )

                QualityTextField(
                    value = sourceName,
                    onValueChange = { sourceName = it },
                    label = "Water Source (e.g., Underground Tank, Borewell)",
                    keyboardType = KeyboardType.Text,
                    testTag = "wq_source_input"
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QualityTextField(
                        value = phText,
                        onValueChange = { phText = it },
                        label = "pH (0 - 14)",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "wq_ph_input"
                    )
                    QualityTextField(
                        value = turbidityText,
                        onValueChange = { turbidityText = it },
                        label = "Turbidity (NTU)",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "wq_turbidity_input"
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QualityTextField(
                        value = temperatureText,
                        onValueChange = { temperatureText = it },
                        label = "Temp (°C)",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "wq_temp_input"
                    )
                    QualityTextField(
                        value = tdsText,
                        onValueChange = { tdsText = it },
                        label = "TDS (ppm)",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "wq_tds_input"
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QualityTextField(
                        value = ecText,
                        onValueChange = { ecText = it },
                        label = "EC (µS/cm)",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "wq_ec_input"
                    )
                    QualityTextField(
                        value = doText,
                        onValueChange = { doText = it },
                        label = "DO (mg/L)",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        testTag = "wq_do_input"
                    )
                }

                QualityTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = "Sample Observations / Notes (optional)",
                    keyboardType = KeyboardType.Text,
                    singleLine = false,
                    testTag = "wq_notes_input"
                )

                // Image Attachment via Android Photo Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("wq_attach_image_button")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedImageUri == null) "Attach Water Image (Optional)" else "Change Water Image",
                            color = AquaPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    if (selectedImageUri != null) {
                        IconButton(onClick = { selectedImageUri = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove image", tint = AlertRed)
                        }
                    }
                }

                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Attached water sample image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    )
                }

                val activeErr = validationError ?: errorMessage
                if (!activeErr.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AlertRedContainer)
                            .padding(12.dp)
                    ) {
                        Text(text = activeErr, style = MaterialTheme.typography.bodySmall, color = AlertRed)
                    }
                }

                Button(
                    onClick = {
                        validationError = null
                        onClearFeedback()

                        val ph = parseOptionalDouble(phText, "pH", 0.0, 14.0) { validationError = it; return@Button }
                        val turbidity = parseOptionalDouble(turbidityText, "Turbidity", 0.0, 10000.0) { validationError = it; return@Button }
                        val temp = parseOptionalDouble(temperatureText, "Temperature", -5.0, 100.0) { validationError = it; return@Button }
                        val tds = parseOptionalDouble(tdsText, "TDS", 0.0, 50000.0) { validationError = it; return@Button }
                        val ec = parseOptionalDouble(ecText, "EC", 0.0, 100000.0) { validationError = it; return@Button }
                        val doVal = parseOptionalDouble(doText, "Dissolved Oxygen", 0.0, 30.0) { validationError = it; return@Button }

                        val input = WaterQualityInput(
                            sourceName = sourceName.trim(),
                            ph = ph,
                            turbidityNtu = turbidity,
                            temperatureC = temp,
                            tdsPpm = tds,
                            ecUsCm = ec,
                            dissolvedOxygenMgL = doVal,
                            notes = notesText.trim()
                        )

                        if (!input.hasAnyMeasurement() && selectedImageUri == null) {
                            validationError = "Enter at least one measurement (pH, Turbidity, Temp, TDS, EC, DO) or attach an image."
                            return@Button
                        }

                        onAnalyze(input, selectedImageUri)
                    },
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("wq_submit_analysis_button")
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analyzing via Gemini AI...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Water Quality", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Latest Analysis Output Card
        if (currentResult != null) {
            WaterQualityResultCard(
                record = currentResult,
                saveFeedback = saveFeedback,
                onSaveReport = onSaveCurrentResult
            )
        }

        // Saved Analysis Records
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
                Text(
                    text = "SAVED WATER QUALITY REPORTS (${savedRecords.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeutralMedium,
                    letterSpacing = 0.8.sp
                )

                if (savedRecords.isEmpty()) {
                    Text(
                        text = "No water quality reports saved yet. Submit an analysis above and tap Save Analysis Report.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralMedium
                    )
                } else {
                    val df = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                    savedRecords.forEach { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .clickable { inspectedRecord = rec }
                                .padding(12.dp),
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
                                    text = "${df.format(Date(rec.createdAtEpochMs))} • ${rec.statusCategory}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AquaPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "pH: ${rec.ph ?: "N/A"} | Turbidity: ${rec.turbidityNtu ?: "N/A"} | TDS: ${rec.tdsPpm ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralMedium
                                )
                            }

                            IconButton(onClick = { onDeleteRecord(rec.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete record", tint = AlertRed)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private inline fun parseOptionalDouble(
    raw: String,
    fieldName: String,
    min: Double,
    max: Double,
    onError: (String) -> Unit
): Double? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    val parsed = trimmed.toDoubleOrNull()
    if (parsed == null) {
        onError("$fieldName must be a valid number.")
        return null
    }
    if (parsed < min || parsed > max) {
        onError("$fieldName must be between $min and $max.")
        return null
    }
    return parsed
}

@Composable
private fun QualityTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AquaPrimary,
            focusedLabelColor = AquaPrimary
        )
    )
}

@Composable
private fun WaterQualityResultCard(
    record: WaterQualityAnalysisRecord,
    saveFeedback: String?,
    onSaveReport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wq_result_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, AquaPrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.sourceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = NeutralDark
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AquaPrimaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = record.statusCategory,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary
                    )
                }
            }

            if (record.summary.isNotBlank()) {
                Text(text = record.summary, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)
            }

            if (record.measuredFindings.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "MEASURED PARAMETERS INTERPRETATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = record.measuredFindings, style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                }
            }

            if (!record.visualObservations.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0F9FF))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "VISUAL IMAGE OBSERVATIONS (NON-CHEMICAL)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AquaPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = record.visualObservations, style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                }
            }

            if (record.warnings.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Warnings:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AlertRed)
                    record.warnings.forEach { w ->
                        Text("• $w", style = MaterialTheme.typography.bodySmall, color = AlertRed)
                    }
                }
            }

            if (record.recommendations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recommendations:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = EcoGreen)
                    record.recommendations.forEach { r ->
                        Text("• $r", style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(WarningAmberContainer)
                    .padding(10.dp)
            ) {
                Text(
                    text = record.disclaimer,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF78350F),
                    lineHeight = 16.sp
                )
            }

            if (!saveFeedback.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EcoGreenContainer)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = saveFeedback, style = MaterialTheme.typography.bodySmall, color = EcoGreen, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = onSaveReport,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("wq_save_report_button")
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Analysis Report", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WaterQualityDetailDialog(
    record: WaterQualityAnalysisRecord,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "${record.sourceName} (${record.statusCategory})", fontWeight = FontWeight.Bold, color = AquaPrimary)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "pH: ${record.ph ?: "N/A"} | Turbidity: ${record.turbidityNtu ?: "N/A"} NTU | Temp: ${record.temperatureC ?: "N/A"} °C\nTDS: ${record.tdsPpm ?: "N/A"} ppm | EC: ${record.ecUsCm ?: "N/A"} µS/cm | DO: ${record.dissolvedOxygenMgL ?: "N/A"} mg/L",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
                if (record.summary.isNotBlank()) {
                    Text(text = record.summary, style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                }
                if (record.measuredFindings.isNotBlank()) {
                    Text(text = "Findings: ${record.measuredFindings}", style = MaterialTheme.typography.bodySmall, color = NeutralMedium)
                }
                if (!record.visualObservations.isNullOrBlank()) {
                    Text(text = "Visual Notes: ${record.visualObservations}", style = MaterialTheme.typography.bodySmall, color = NeutralMedium)
                }
                if (record.recommendations.isNotEmpty()) {
                    Text(text = "Recommendations:\n" + record.recommendations.joinToString("\n") { "• $it" }, style = MaterialTheme.typography.bodySmall, color = EcoGreen)
                }
                Text(text = record.disclaimer, style = MaterialTheme.typography.labelSmall, color = WarningAmber)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold, color = AquaPrimary)
            }
        }
    )
}

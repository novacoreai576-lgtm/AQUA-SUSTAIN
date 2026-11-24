package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class ReportFormat(
    val label: String,
    val mimeType: String,
    val extension: String
) {
    SUMMARY_TXT("Full Summary (.txt)", "text/plain", "txt"),
    QUALITY_CSV("Water Quality (.csv)", "text/csv", "csv"),
    USAGE_CSV("Water Usage (.csv)", "text/csv", "csv")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
    telemetryCount: Int,
    qualityCount: Int,
    usageCount: Int,
    buildSummaryReport: () -> String,
    buildQualityCsv: () -> String,
    buildUsageCsv: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedFormat by remember { mutableStateOf(ReportFormat.SUMMARY_TXT) }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var isExportError by remember { mutableStateOf(false) }

    val reportContent = remember(selectedFormat, telemetryCount, qualityCount, usageCount) {
        when (selectedFormat) {
            ReportFormat.SUMMARY_TXT -> buildSummaryReport()
            ReportFormat.QUALITY_CSV -> buildQualityCsv()
            ReportFormat.USAGE_CSV -> buildUsageCsv()
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(selectedFormat.mimeType)
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(reportContent.toByteArray(Charsets.UTF_8))
                }
                isExportError = false
                exportStatus = "Report saved successfully to device storage."
            } catch (e: Exception) {
                isExportError = true
                exportStatus = "Export failed: ${e.localizedMessage}"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Reports & Export",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AquaPrimary
            )
            Text(
                text = "Generate and download sustainability, water quality, and usage reports from saved records",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralMedium
            )
        }

        // Real Record Counts Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReportStatPill(label = "Telemetry Logs", count = telemetryCount, modifier = Modifier.weight(1f))
            ReportStatPill(label = "Quality Records", count = qualityCount, modifier = Modifier.weight(1f))
            ReportStatPill(label = "Usage Entries", count = usageCount, modifier = Modifier.weight(1f))
        }

        // Format Selector + Export Actions
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
                    text = "SELECT REPORT TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeutralMedium
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportFormat.entries.forEach { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = {
                                selectedFormat = format
                                exportStatus = null
                            },
                            label = { Text(format.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AquaPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (exportStatus != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isExportError) AlertRedContainer else EcoGreenContainer)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isExportError) AlertRed else EcoGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exportStatus!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExportError) AlertRed else EcoGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            val fileName = "aquasustain_${selectedFormat.name.lowercase()}_$stamp.${selectedFormat.extension}"
                            createDocumentLauncher.launch(fileName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("reports_download_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "AquaSustain Sustainability Report")
                                putExtra(Intent.EXTRA_TEXT, reportContent)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share AquaSustain Report"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("reports_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", color = AquaPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Report Preview
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
                    Icon(Icons.Default.Description, contentDescription = null, tint = AquaPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REPORT PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeutralMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .padding(14.dp)
                ) {
                    Text(
                        text = reportContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ReportStatPill(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = AquaPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NeutralMedium
        )
    }
}

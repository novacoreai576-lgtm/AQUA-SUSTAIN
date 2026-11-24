package com.example.ui.screens.bioleaf

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BioLeafReportCategory
import com.example.data.model.BioLeafReportFormatter
import com.example.data.model.BioLeafSavedReportSummary
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.EcoOnGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder

@Composable
fun BioLeafHistoryReportsSection(
    reports: List<BioLeafSavedReportSummary>,
    onDeleteReport: (BioLeafSavedReportSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<BioLeafReportCategory?>(null) }
    var viewingReport by remember { mutableStateOf<BioLeafSavedReportSummary?>(null) }
    var pendingExportReport by remember { mutableStateOf<BioLeafSavedReportSummary?>(null) }
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        val target = pendingExportReport
        pendingExportReport = null
        if (uri != null && target != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(target.exportText.toByteArray(Charsets.UTF_8))
                }
                exportStatusMessage = "Saved report successfully."
            } catch (e: Exception) {
                exportStatusMessage = "Could not export report: ${e.localizedMessage}"
            }
        }
    }

    val filteredReports = remember(reports, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        reports.filter { item ->
            val catMatch = selectedCategory == null || item.category == selectedCategory
            val qMatch = q.isBlank() ||
                item.title.lowercase().contains(q) ||
                item.subtitle.lowercase().contains(q) ||
                item.exportText.lowercase().contains(q)
            catMatch && qMatch
        }
    }

    if (viewingReport != null) {
        val report = viewingReport!!
        AlertDialog(
            onDismissRequest = { viewingReport = null },
            title = {
                Text(
                    text = report.title,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val bmp = remember(report.imageThumbnailBase64) {
                        decodeBase64Thumbnail(report.imageThumbnailBase64)
                    }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Report thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    Text(
                        text = report.exportText,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingExportReport = report
                        val fileName = "bioleaf_${report.category.name.lowercase()}_${report.timestampEpochMs}.txt"
                        saveFileLauncher.launch(fileName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "BIO LEAF AI Report: ${report.title}")
                                putExtra(Intent.EXTRA_TEXT, report.exportText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Report"))
                        }
                    ) {
                        Text("Share", color = EcoGreen, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { viewingReport = null }) {
                        Text("Close", color = NeutralMedium)
                    }
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("bioleaf_history_reports_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "History & Saved Reports",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = NeutralDark
        )
        Text(
            text = "Search, filter, inspect full details, download, or delete your persisted agricultural records.",
            style = MaterialTheme.typography.bodySmall,
            color = NeutralMedium
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search saved reports") },
            placeholder = { Text("Search plant, disease, pest, or soil reports...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("reports_search_input")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All (${reports.size})") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreen, selectedLabelColor = Color.White)
            )
            BioLeafReportCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreen, selectedLabelColor = Color.White)
                )
            }
        }

        if (exportStatusMessage != null) {
            Text(
                text = exportStatusMessage!!,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = EcoGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(EcoGreenContainer)
                    .padding(10.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredReports.isEmpty()) {
                item {
                    BioLeafSectionCard(
                        title = "No Saved Reports Found",
                        accentColor = NeutralDark,
                        modifier = Modifier.testTag("reports_empty_state")
                    ) {
                        Text(
                            text = "No saved reports match your current filter. Run Plant Identification, Disease Scanner, Pest Scanner, or Soil Guidance to save real reports here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }
                }
            } else {
                items(filteredReports, key = { "${it.category.name}_${it.id}_${it.timestampEpochMs}" }) { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewingReport = report },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = report.category.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EcoOnGreenContainer
                                )
                                Text(
                                    text = report.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = report.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = BioLeafReportFormatter.formatTimestamp(report.timestampEpochMs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeutralMedium
                                )
                            }

                            IconButton(
                                onClick = {
                                    pendingExportReport = report
                                    saveFileLauncher.launch("bioleaf_${report.category.name.lowercase()}_${report.timestampEpochMs}.txt")
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download report", tint = EcoGreen)
                            }
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "BIO LEAF AI Report: ${report.title}")
                                        putExtra(Intent.EXTRA_TEXT, report.exportText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Report"))
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share report", tint = NeutralDark)
                            }
                            IconButton(
                                onClick = { onDeleteReport(report) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete report", tint = AlertRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

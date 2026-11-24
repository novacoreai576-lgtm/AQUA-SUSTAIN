package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BioLeafReportFormatter {

    fun formatTimestamp(epochMs: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(epochMs))
    }

    fun formatPlantIdReport(record: PlantIdRecord): String = buildString {
        appendLine("BIO LEAF AI - PLANT IDENTIFICATION REPORT")
        appendLine("Generated: ${formatTimestamp(record.timestampEpochMs)}")
        appendLine("Common Name: ${record.commonName}")
        if (record.scientificName.isNotBlank()) {
            appendLine("Scientific Name: ${record.scientificName}")
        }
        appendLine()
        appendLine("Plant Description:")
        appendLine(record.plantDescription.ifBlank { "Not provided." })
        appendLine()
        appendLine("Visible Identifying Characteristics:")
        if (record.identifyingCharacteristics.isEmpty()) {
            appendLine("- Visual details limited")
        } else {
            record.identifyingCharacteristics.forEach { appendLine("- $it") }
        }
        appendLine()
        appendLine("Identification Explanation:")
        appendLine(record.identificationExplanation.ifBlank { "Not provided." })
        if (record.uncertaintyNote.isNotBlank()) {
            appendLine()
            appendLine("Observation Note:")
            appendLine(record.uncertaintyNote)
        }
    }.trim()

    fun formatDiseaseReport(record: DiseaseReportRecord): String = buildString {
        appendLine("BIO LEAF AI - PLANT DISEASE ANALYSIS REPORT")
        appendLine("Generated: ${formatTimestamp(record.timestampEpochMs)}")
        appendLine()
        appendLine("1. Plant Name: ${record.plantName}")
        appendLine("2. Problem: ${record.problem}")
        appendLine("3. Severity: ${record.severity}")
        appendLine()
        appendLine("4. Visible Symptoms:")
        appendLine(record.visibleSymptoms.ifBlank { "None recorded." })
        appendLine()
        appendLine("5. Possible Causes:")
        appendLine(record.possibleCauses.ifBlank { "None recorded." })
        appendLine()
        appendLine("6. Detailed Explanation:")
        appendLine(record.detailedExplanation.ifBlank { "None recorded." })
        appendLine()
        appendLine("7. Recommended Treatment:")
        if (record.recommendedTreatment.isEmpty()) appendLine("- None listed")
        else record.recommendedTreatment.forEach { appendLine("- $it") }
        appendLine()
        appendLine("8. Organic Treatment:")
        if (record.organicTreatment.isEmpty()) appendLine("- None listed")
        else record.organicTreatment.forEach { appendLine("- $it") }
        appendLine()
        appendLine("9. Prevention Methods:")
        if (record.preventionMethods.isEmpty()) appendLine("- None listed")
        else record.preventionMethods.forEach { appendLine("- $it") }
        if (record.visualObservationNote.isNotBlank()) {
            appendLine()
            appendLine("Diagnostic Note: ${record.visualObservationNote}")
        }
    }.trim()

    fun formatPestReport(record: PestReportRecord): String = buildString {
        appendLine("BIO LEAF AI - PEST IDENTIFICATION REPORT")
        appendLine("Generated: ${formatTimestamp(record.timestampEpochMs)}")
        appendLine()
        appendLine("Possible Pest Identification: ${record.possiblePestIdentification}")
        if (record.pestFamily.isNotBlank()) {
            appendLine("Pest Family: ${record.pestFamily}")
        }
        appendLine("Pest Severity: ${record.pestSeverity}")
        appendLine()
        appendLine("Visible Characteristics:")
        appendLine(record.visibleCharacteristics.ifBlank { "Not specified." })
        appendLine()
        appendLine("Feeding Damage:")
        appendLine(record.feedingDamage.ifBlank { "Not specified." })
        appendLine()
        appendLine("Recommended Management:")
        record.recommendedManagement.forEach { appendLine("- $it") }
        appendLine()
        appendLine("Organic Pest-Control Methods:")
        record.organicPestControlMethods.forEach { appendLine("- $it") }
        appendLine()
        appendLine("Prevention:")
        record.prevention.forEach { appendLine("- $it") }
        if (record.uncertaintyNote.isNotBlank()) {
            appendLine()
            appendLine("Observation Note: ${record.uncertaintyNote}")
        }
    }.trim()

    fun formatSoilGuidanceReport(record: SoilGuidanceRecord): String = buildString {
        appendLine("BIO LEAF AI - SOIL CONDITION GUIDANCE REPORT (SOFTWARE-ONLY)")
        appendLine("Generated: ${formatTimestamp(record.timestampEpochMs)}")
        appendLine("Soil Type: ${record.soilType} | Crop: ${record.cropName}")
        if (record.irrigationFrequency.isNotBlank()) {
            appendLine("Irrigation Frequency: ${record.irrigationFrequency}")
        }
        appendLine()
        appendLine("Soil Improvement Recommendations:")
        record.soilImprovementRecommendations.forEach { appendLine("- $it") }
        appendLine()
        appendLine("Irrigation Guidance:")
        appendLine(record.irrigationGuidance)
        appendLine()
        appendLine("Compost Recommendations:")
        appendLine(record.compostRecommendations)
        appendLine()
        appendLine("Vermicompost Guidance:")
        appendLine(record.vermicompostGuidance)
        appendLine()
        appendLine("Neem Cake Guidance:")
        appendLine(record.neemCakeGuidance)
        appendLine()
        appendLine("Soil Conservation:")
        record.soilConservation.forEach { appendLine("- $it") }
        appendLine()
        appendLine("Sustainable Farming Practices:")
        record.sustainableFarmingPractices.forEach { appendLine("- $it") }
    }.trim()

    fun buildUnifiedReports(
        plants: List<PlantIdRecord>,
        diseases: List<DiseaseReportRecord>,
        pests: List<PestReportRecord>,
        soils: List<SoilGuidanceRecord>
    ): List<BioLeafSavedReportSummary> {
        val list = mutableListOf<BioLeafSavedReportSummary>()
        plants.forEach {
            list += BioLeafSavedReportSummary(
                id = it.id,
                category = BioLeafReportCategory.PLANT_ID,
                title = it.commonName.ifBlank { "Plant Identification" },
                subtitle = it.scientificName.ifBlank { "Botanical identification" },
                timestampEpochMs = it.timestampEpochMs,
                imageThumbnailBase64 = it.imageThumbnailBase64,
                exportText = formatPlantIdReport(it)
            )
        }
        diseases.forEach {
            list += BioLeafSavedReportSummary(
                id = it.id,
                category = BioLeafReportCategory.DISEASE,
                title = "${it.plantName}: ${it.problem}",
                subtitle = "Severity: ${it.severity}",
                timestampEpochMs = it.timestampEpochMs,
                imageThumbnailBase64 = it.imageThumbnailBase64,
                exportText = formatDiseaseReport(it)
            )
        }
        pests.forEach {
            list += BioLeafSavedReportSummary(
                id = it.id,
                category = BioLeafReportCategory.PEST,
                title = it.possiblePestIdentification.ifBlank { "Pest Assessment" },
                subtitle = "Pest Severity: ${it.pestSeverity}",
                timestampEpochMs = it.timestampEpochMs,
                imageThumbnailBase64 = it.imageThumbnailBase64,
                exportText = formatPestReport(it)
            )
        }
        soils.forEach {
            list += BioLeafSavedReportSummary(
                id = it.id,
                category = BioLeafReportCategory.SOIL,
                title = "${it.cropName} (${it.soilType})",
                subtitle = "Software soil management plan",
                timestampEpochMs = it.timestampEpochMs,
                imageThumbnailBase64 = null,
                exportText = formatSoilGuidanceReport(it)
            )
        }
        return list.sortedByDescending { it.timestampEpochMs }
    }
}

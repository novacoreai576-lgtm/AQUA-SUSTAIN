package com.example.data.model

import java.util.Calendar
import java.util.Locale

data class WaterQualityInput(
    val sourceName: String = "",
    val ph: Double? = null,
    val turbidityNtu: Double? = null,
    val temperatureC: Double? = null,
    val tdsPpm: Double? = null,
    val ecUsCm: Double? = null,
    val dissolvedOxygenMgL: Double? = null,
    val notes: String = ""
) {
    fun hasAnyMeasurement(): Boolean {
        return ph != null ||
            turbidityNtu != null ||
            temperatureC != null ||
            tdsPpm != null ||
            ecUsCm != null ||
            dissolvedOxygenMgL != null
    }
}

data class WaterQualityAnalysisRecord(
    val id: String = "",
    val sourceName: String = "",
    val ph: Double? = null,
    val turbidityNtu: Double? = null,
    val temperatureC: Double? = null,
    val tdsPpm: Double? = null,
    val ecUsCm: Double? = null,
    val dissolvedOxygenMgL: Double? = null,
    val notes: String = "",
    val hasAttachedImage: Boolean = false,
    val statusCategory: String = "Pending",
    val summary: String = "",
    val measuredFindings: String = "",
    val visualObservations: String? = null,
    val warnings: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val disclaimer: String = DEFAULT_SAFETY_DISCLAIMER,
    val analyzedByGemini: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_SAFETY_DISCLAIMER =
            "Note: Numerical field parameters and visual inspection alone cannot certify drinking water safety or microbiological/chemical compliance. Confirm potability through an accredited laboratory."
    }
}

data class WaterUsageRecord(
    val id: String = "",
    val amountLiters: Double = 0.0,
    val category: String = "Irrigation",
    val source: String = "Underground Tank",
    val notes: String = "",
    val usageDateEpochMs: Long = System.currentTimeMillis()
)

data class DailyUsagePoint(
    val label: String,
    val dateKey: String,
    val totalLiters: Double
)

data class CategoryUsageTotal(
    val category: String,
    val totalLiters: Double,
    val percentage: Float
)

data class WaterUsageSummary(
    val totalRecords: Int = 0,
    val todayLiters: Double = 0.0,
    val yesterdayLiters: Double = 0.0,
    val weeklyLiters: Double = 0.0,
    val previousWeekLiters: Double = 0.0,
    val monthlyLiters: Double = 0.0,
    val previousMonthLiters: Double = 0.0,
    val dailySeries: List<DailyUsagePoint> = emptyList(),
    val categoryBreakdown: List<CategoryUsageTotal> = emptyList(),
    val wastageAlerts: List<String> = emptyList(),
    val conservationInsights: List<String> = emptyList()
) {
    val dailyDifferenceLiters: Double
        get() = todayLiters - yesterdayLiters

    val weeklyDifferenceLiters: Double
        get() = weeklyLiters - previousWeekLiters

    val monthlyDifferenceLiters: Double
        get() = monthlyLiters - previousMonthLiters

    companion object {
        val CATEGORIES = listOf(
            "Irrigation",
            "Household",
            "Drinking & Cooking",
            "Livestock / Utility",
            "Rainwater Harvested"
        )

        val SOURCES = listOf(
            "Underground Tank",
            "Municipal Supply",
            "Borewell / Well",
            "Rainwater Catchment",
            "Other"
        )

        fun compute(records: List<WaterUsageRecord>, nowMs: Long = System.currentTimeMillis()): WaterUsageSummary {
            if (records.isEmpty()) {
                return WaterUsageSummary()
            }

            val dayMs = 24L * 60L * 60L * 1000L
            val startOfToday = startOfDayMillis(nowMs)
            val startOfYesterday = startOfToday - dayMs
            val startOfWeek = startOfToday - 6L * dayMs
            val startOfPrevWeek = startOfWeek - 7L * dayMs
            val startOfMonth = startOfToday - 29L * dayMs
            val startOfPrevMonth = startOfMonth - 30L * dayMs

            var todayTotal = 0.0
            var yesterdayTotal = 0.0
            var weekTotal = 0.0
            var prevWeekTotal = 0.0
            var monthTotal = 0.0
            var prevMonthTotal = 0.0

            val categoryMap = linkedMapOf<String, Double>()
            val dailyBuckets = linkedMapOf<Long, Double>()
            for (offset in 6 downTo 0) {
                dailyBuckets[startOfToday - offset * dayMs] = 0.0
            }

            records.forEach { rec ->
                val amount = rec.amountLiters.coerceAtLeast(0.0)
                val ts = rec.usageDateEpochMs
                val bucketDay = startOfDayMillis(ts)

                if (ts >= startOfToday && ts < startOfToday + dayMs) {
                    todayTotal += amount
                } else if (ts >= startOfYesterday && ts < startOfToday) {
                    yesterdayTotal += amount
                }

                if (ts >= startOfWeek && ts < startOfToday + dayMs) {
                    weekTotal += amount
                    categoryMap[rec.category] = (categoryMap[rec.category] ?: 0.0) + amount
                } else if (ts >= startOfPrevWeek && ts < startOfWeek) {
                    prevWeekTotal += amount
                }

                if (ts >= startOfMonth && ts < startOfToday + dayMs) {
                    monthTotal += amount
                } else if (ts >= startOfPrevMonth && ts < startOfMonth) {
                    prevMonthTotal += amount
                }

                if (dailyBuckets.containsKey(bucketDay)) {
                    dailyBuckets[bucketDay] = (dailyBuckets[bucketDay] ?: 0.0) + amount
                }
            }

            val cal = Calendar.getInstance()
            val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val series = dailyBuckets.map { (dayStart, liters) ->
                cal.timeInMillis = dayStart
                val dow = cal.get(Calendar.DAY_OF_WEEK) - 1
                val dom = cal.get(Calendar.DAY_OF_MONTH)
                val label = dayNames.getOrElse(dow) { "" }
                val dateKey = String.format(Locale.US, "%s %d", label, dom)
                DailyUsagePoint(label = label, dateKey = dateKey, totalLiters = liters)
            }

            val totalForBreakdown = categoryMap.values.sum().coerceAtLeast(0.0001)
            val breakdown = categoryMap.entries
                .sortedByDescending { it.value }
                .map { (cat, sum) ->
                    CategoryUsageTotal(
                        category = cat,
                        totalLiters = sum,
                        percentage = ((sum / totalForBreakdown) * 100.0).toFloat()
                    )
                }

            val alerts = mutableListOf<String>()
            val insights = mutableListOf<String>()

            val nonZeroDays = series.filter { it.totalLiters > 0.0 }
            val avgDaily7d = if (nonZeroDays.isNotEmpty()) nonZeroDays.map { it.totalLiters }.average() else 0.0

            if (todayTotal > 0.0 && avgDaily7d > 0.0 && todayTotal > avgDaily7d * 1.35) {
                val pctAbove = (((todayTotal - avgDaily7d) / avgDaily7d) * 100).toInt()
                alerts.add("Today's consumption (${formatLiters(todayTotal)}) is $pctAbove% above your recent daily average (${formatLiters(avgDaily7d)}). Check valves and drip lines for leakage or over-watering.")
            }

            if (prevWeekTotal > 0.0 && weekTotal > prevWeekTotal * 1.2) {
                val diffPct = (((weekTotal - prevWeekTotal) / prevWeekTotal) * 100).toInt()
                alerts.add("Weekly water usage increased by $diffPct% compared to the prior 7-day period.")
            }

            if (weekTotal > 0.0) {
                val topCategory = breakdown.firstOrNull()
                if (topCategory != null) {
                    insights.add("${topCategory.category} accounts for ${topCategory.percentage.toInt()}% of your last 7 days' recorded volume (${formatLiters(topCategory.totalLiters)}).")
                }
                if (prevWeekTotal > 0.0 && weekTotal < prevWeekTotal) {
                    val saved = prevWeekTotal - weekTotal
                    insights.add("You saved ${formatLiters(saved)} over the past 7 days compared to the prior week.")
                }
            } else {
                insights.add("Log real daily water consumption to unlock trend deltas and automated conservation feedback.")
            }

            return WaterUsageSummary(
                totalRecords = records.size,
                todayLiters = todayTotal,
                yesterdayLiters = yesterdayTotal,
                weeklyLiters = weekTotal,
                previousWeekLiters = prevWeekTotal,
                monthlyLiters = monthTotal,
                previousMonthLiters = prevMonthTotal,
                dailySeries = series,
                categoryBreakdown = breakdown,
                wastageAlerts = alerts,
                conservationInsights = insights
            )
        }

        private fun startOfDayMillis(epochMs: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = epochMs
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun formatLiters(liters: Double): String {
            return if (liters >= 1000.0) {
                String.format(Locale.US, "%.1f kL", liters / 1000.0)
            } else if (liters == liters.toLong().toDouble()) {
                "${liters.toLong()} L"
            } else {
                String.format(Locale.US, "%.1f L", liters)
            }
        }
    }
}

data class WaterAssistantMessage(
    val id: String = "",
    val role: String = "user", // "user" or "assistant"
    val content: String = "",
    val language: String = "English",
    val timestampEpochMs: Long = System.currentTimeMillis()
)

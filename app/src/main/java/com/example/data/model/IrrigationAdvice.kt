package com.example.data.model

data class IrrigationAdvice(
    val recommendation: String, // "IRRIGATE NOW", "POSTPONE IRRIGATION", "DO NOT IRRIGATE", "CONSERVE WATER"
    val shouldIrrigate: Boolean,
    val explanation: String,
    val rainWarning: String? = null,
    val tankWarning: String? = null,
    val waterSavingTips: List<String> = emptyList(),
    val confidence: String = "High",
    val generatedByAi: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis()
)

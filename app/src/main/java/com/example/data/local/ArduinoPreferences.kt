package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class ArduinoPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "aquasustain_arduino_prefs"
        private const val KEY_ARDUINO_IP = "key_arduino_ip"
        const val DEFAULT_IP = "192.168.43.120"
    }

    var arduinoIp: String
        get() = prefs.getString(KEY_ARDUINO_IP, DEFAULT_IP)?.trim() ?: DEFAULT_IP
        set(value) {
            val cleaned = cleanIpInput(value)
            prefs.edit().putString(KEY_ARDUINO_IP, cleaned).apply()
        }

    fun cleanIpInput(input: String): String {
        return input.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .removeSuffix("/data")
            .removeSuffix("/")
            .trim()
    }

    fun getFullDataUrl(): String {
        return "http://${arduinoIp}/data"
    }
}

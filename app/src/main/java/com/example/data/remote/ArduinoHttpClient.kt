package com.example.data.remote

import android.util.Log
import com.example.data.model.AquaSustainData
import com.example.data.model.PumpResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class ArduinoHttpClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2500, TimeUnit.MILLISECONDS)
        .readTimeout(2500, TimeUnit.MILLISECONDS)
        .writeTimeout(2500, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(false)
        .build(),
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
) {
    companion object {
        private const val TAG = "ArduinoHttpClient"
    }

    private val dataAdapter = moshi.adapter(AquaSustainData::class.java)
    private val pumpAdapter = moshi.adapter(PumpResponse::class.java)

    /**
     * Resolves and normalizes an IP or URL into a clean base URL.
     * Examples:
     * "192.168.43.120"           -> "http://192.168.43.120"
     * "http://192.168.43.120/data"-> "http://192.168.43.120"
     * "192.168.1.150:8080"       -> "http://192.168.1.150:8080"
     */
    fun normalizeBaseUrl(ipOrUrl: String): String {
        var trimmed = ipOrUrl.trim()
        if (trimmed.isEmpty()) {
            return "http://192.168.43.120"
        }
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://$trimmed"
        }
        // Remove trailing paths like /data, /pump/on, /
        val clean = trimmed
            .removeSuffix("/data")
            .removeSuffix("/data/")
            .removeSuffix("/pump/on")
            .removeSuffix("/pump/off")
            .removeSuffix("/")
        return clean
    }

    /**
     * Connects to Arduino UNO R4 WiFi at GET http://<ARDUINO_IP>/data
     * Expected JSON response:
     * {
     *   "rain": false,
     *   "soilMoisture": 43,
     *   "waterLevel": 65,
     *   "pump": true
     * }
     */
    suspend fun fetchData(ipOrUrl: String): Result<AquaSustainData> = withContext(Dispatchers.IO) {
        val baseUrl = normalizeBaseUrl(ipOrUrl)
        val dataUrl = "$baseUrl/data"

        try {
            val request = Request.Builder()
                .url(dataUrl)
                .addHeader("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val msg = "Arduino returned HTTP status: ${response.code} (${response.message})"
                    Log.w(TAG, "$msg from $dataUrl")
                    return@withContext Result.failure(IOException(msg))
                }

                val bodyString = response.body?.string()
                if (bodyString.isNullOrBlank()) {
                    return@withContext Result.failure(IOException("Empty response body from $dataUrl"))
                }

                val parsed = dataAdapter.fromJson(bodyString)
                    ?: return@withContext Result.failure(IOException("Failed to parse JSON: $bodyString"))

                Result.success(parsed.sanitized())
            }
        } catch (e: Exception) {
            Log.d(TAG, "Could not reach Arduino at $dataUrl: ${e.message}")
            Result.failure(
                IOException("Could not reach Arduino at $dataUrl. Ensure phone and Arduino are on the same Wi-Fi: ${e.localizedMessage}")
            )
        }
    }

    /**
     * Controls the water pump relay on the Arduino:
     * GET http://<ARDUINO_IP>/pump/on
     * GET http://<ARDUINO_IP>/pump/off
     * Expected response:
     * {"pump": true} or {"pump": false}
     */
    suspend fun setPump(ipOrUrl: String, turnOn: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        val baseUrl = normalizeBaseUrl(ipOrUrl)
        val endpoint = if (turnOn) "$baseUrl/pump/on" else "$baseUrl/pump/off"

        try {
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Arduino returned HTTP ${response.code} on pump command")
                    )
                }

                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val pumpResp = runCatching { pumpAdapter.fromJson(body) }.getOrNull()
                    if (pumpResp != null) {
                        return@withContext Result.success(pumpResp.pump)
                    }
                }
                Result.success(turnOn)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Could not execute pump command on $endpoint: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Tests connectivity to the Arduino by requesting /data.
     */
    suspend fun testConnection(ipOrUrl: String): Result<AquaSustainData> {
        return fetchData(ipOrUrl)
    }
}

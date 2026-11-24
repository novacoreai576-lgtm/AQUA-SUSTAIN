package com.example.data.firebase

import android.util.Log
import com.example.data.model.AquaSustainData
import com.example.data.model.SensorReading
import com.example.data.model.UserProfile
import com.example.data.model.WaterAssistantMessage
import com.example.data.model.WaterQualityAnalysisRecord
import com.example.data.model.WaterUsageRecord
import com.example.data.model.WeatherInfo
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.math.abs

class FirebaseManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FirebaseManager"
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isAuthReady = MutableStateFlow(false)
    val isAuthReady: StateFlow<Boolean> = _isAuthReady.asStateFlow()

    private var lastRecordedSoil = -1
    private var lastRecordedWater = -1
    private var lastRecordedRain: Boolean? = null
    private var lastRecordedPump: Boolean? = null
    private var lastRecordTimestampMs = 0L

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            _isAuthReady.value = true
            if (user != null) {
                fetchUserProfile(user.uid)
                ensureUserProfileDocument(user.uid, user.displayName ?: "AquaSustain User", user.email ?: "")
            } else {
                _userProfile.value = null
            }
        }
    }

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw Exception("Authentication returned null user")
            _currentUser.value = user
            fetchUserProfile(user.uid)
            Result.success(user)
        } catch (e: Exception) {
            Log.w(TAG, "Sign in failed: ${e.message}")
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    /**
     * Creates user account and authentic profile document ONLY.
     * Strictly NO fake sensor/weather/history seeding.
     */
    suspend fun signUp(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val cleanName = name.trim()
            val cleanEmail = email.trim()
            val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = result.user ?: throw Exception("Failed to register user")
            _currentUser.value = user
            val uid = user.uid

            try {
                user.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()
                ).await()
            } catch (e: Exception) {
                Log.w(TAG, "Display name update note: ${e.message}")
            }

            try {
                val profileData = hashMapOf(
                    "name" to cleanName,
                    "email" to cleanEmail,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(uid).set(profileData).await()
            } catch (e: Exception) {
                Log.w(TAG, "Initial profile write note: ${e.message}")
            }

            fetchUserProfile(uid)
            Result.success(user)
        } catch (e: Exception) {
            Log.w(TAG, "Sign up note: ${e.message}")
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _userProfile.value = null
        lastRecordedSoil = -1
        lastRecordedWater = -1
        lastRecordedRain = null
        lastRecordedPump = null
        lastRecordTimestampMs = 0L
    }

    fun fetchUserProfile(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: (auth.currentUser?.displayName ?: "AquaSustain User")
                    val email = doc.getString("email") ?: (auth.currentUser?.email ?: "")
                    val createdAt = doc.getTimestamp("createdAt")
                    _userProfile.value = UserProfile(
                        uid = uid,
                        name = name,
                        email = email,
                        createdAt = createdAt
                    )
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "Could not fetch users/$uid: ${it.message}")
            }
    }

    private fun ensureUserProfileDocument(uid: String, name: String, email: String) {
        firestore.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val profile = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(uid).set(profile)
            }
        }
    }

    // ========================================================
    // Telemetry & Pump State
    // ========================================================

    suspend fun saveLiveSensorData(data: AquaSustainData) {
        val uid = currentUserId ?: return
        if (!data.hasLiveData) return
        try {
            val sensorDoc = hashMapOf(
                "rain" to data.rain,
                "soilMoisture" to data.soilMoisture.coerceIn(0, 100),
                "waterLevel" to data.waterLevel.coerceIn(0, 100),
                "pump" to data.pump,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("sensorData").document(uid).set(sensorDoc).await()

            val now = System.currentTimeMillis()
            val stateChanged = (data.rain != lastRecordedRain) ||
                (data.pump != lastRecordedPump) ||
                (abs(data.soilMoisture - lastRecordedSoil) >= 3) ||
                (abs(data.waterLevel - lastRecordedWater) >= 3) ||
                (now - lastRecordTimestampMs >= 120_000L)

            if (stateChanged) {
                recordSensorHistory(
                    uid = uid,
                    rain = data.rain,
                    soilMoisture = data.soilMoisture,
                    waterLevel = data.waterLevel,
                    pump = data.pump
                )
                lastRecordedRain = data.rain
                lastRecordedPump = data.pump
                lastRecordedSoil = data.soilMoisture
                lastRecordedWater = data.waterLevel
                lastRecordTimestampMs = now
            }
        } catch (e: Exception) {
            Log.w(TAG, "Note updating sensorData/$uid: ${e.message}")
        }
    }

    suspend fun recordSensorHistory(
        uid: String,
        rain: Boolean,
        soilMoisture: Int,
        waterLevel: Int,
        pump: Boolean
    ) {
        try {
            val record = hashMapOf(
                "rain" to rain,
                "soilMoisture" to soilMoisture.coerceIn(0, 100),
                "waterLevel" to waterLevel.coerceIn(0, 100),
                "pump" to pump,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("sensorHistory").document(uid)
                .collection("records")
                .add(record)
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error adding to sensorHistory/$uid/records: ${e.message}")
        }
    }

    fun observeSensorHistory(limit: Long = 100): Flow<List<SensorReading>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = firestore.collection("sensorHistory").document(uid)
            .collection("records")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.w(TAG, "History query error for sensorHistory/$uid/records: ${error?.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            val readings = snapshot.documents.map { doc ->
                val rain = doc.getBoolean("rain") ?: false
                val soil = doc.getLong("soilMoisture")?.toInt()?.coerceIn(0, 100) ?: 0
                val water = doc.getLong("waterLevel")?.toInt()?.coerceIn(0, 100) ?: 0
                val pump = doc.getBoolean("pump") ?: false
                val timestamp = doc.getTimestamp("timestamp")
                val epochMs = timestamp?.toDate()?.time ?: System.currentTimeMillis()

                SensorReading(
                    id = doc.id,
                    rain = rain,
                    soilMoisture = soil,
                    waterLevel = water,
                    pump = pump,
                    timestamp = timestamp,
                    timestampEpochMs = epochMs
                )
            }
            trySend(readings)
        }

        awaitClose { registration.remove() }
    }

    suspend fun deleteSensorHistoryRecord(recordId: String): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("User is not signed in"))
        return try {
            firestore.collection("sensorHistory").document(uid)
                .collection("records").document(recordId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePumpControl(
        targetPumpOn: Boolean,
        mode: String = "manual",
        currentRain: Boolean = false,
        currentSoil: Int = 0,
        currentWater: Int = 0
    ): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("User is not signed in"))
        return try {
            val command = if (targetPumpOn) "PUMP_ON" else "PUMP_OFF"
            val pumpDoc = hashMapOf(
                "pump" to targetPumpOn,
                "mode" to mode,
                "command" to command,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("pumpControl").document(uid).set(pumpDoc).await()

            val sensorDoc = hashMapOf(
                "rain" to currentRain,
                "soilMoisture" to currentSoil.coerceIn(0, 100),
                "waterLevel" to currentWater.coerceIn(0, 100),
                "pump" to targetPumpOn,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("sensorData").document(uid).set(sensorDoc).await()

            recordSensorHistory(
                uid = uid,
                rain = currentRain,
                soilMoisture = currentSoil,
                waterLevel = currentWater,
                pump = targetPumpOn
            )

            lastRecordedPump = targetPumpOn
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Note updating pumpControl/$uid: ${e.message}")
            Result.failure(e)
        }
    }

    fun observeWeather(): Flow<WeatherInfo?> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = firestore.collection("weather").document(uid)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                trySend(null)
                return@addSnapshotListener
            }

            val temp = snapshot.getLong("temperature")?.toInt() ?: return@addSnapshotListener
            val rainProb = snapshot.getLong("rainProbability")?.toInt() ?: 0
            val cond = snapshot.getString("condition") ?: "Unknown"
            val rec = snapshot.getString("recommendation") ?: ""
            val humidity = snapshot.getLong("humidity")?.toInt() ?: 0
            val feelsLike = snapshot.getLong("feelsLike")?.toInt() ?: temp
            val windSpeed = snapshot.getDouble("windSpeed") ?: 0.0
            val precip = snapshot.getDouble("precipitation") ?: 0.0
            val loc = snapshot.getString("location") ?: "Saved Location"
            val lat = snapshot.getDouble("latitude") ?: 28.6139
            val lon = snapshot.getDouble("longitude") ?: 77.2090

            trySend(
                WeatherInfo(
                    temperatureC = temp,
                    rainProbabilityPercent = rainProb,
                    condition = cond,
                    smartRecommendation = rec,
                    rainPredictionText = if (rainProb >= 60) "High rain probability ($rainProb%)" else "Low rain probability ($rainProb%)",
                    humidityPercent = humidity,
                    feelsLikeC = feelsLike,
                    windSpeedKmh = windSpeed,
                    precipitationMm = precip,
                    locationName = loc,
                    latitude = lat,
                    longitude = lon,
                    lastUpdatedTime = "Synced",
                    isLive = false,
                    hasFetched = true
                )
            )
        }

        awaitClose { registration.remove() }
    }

    fun saveWeather(weather: WeatherInfo) {
        val uid = currentUserId ?: return
        if (!weather.hasFetched) return
        val weatherDoc = hashMapOf(
            "temperature" to weather.temperatureC,
            "rainProbability" to weather.rainProbabilityPercent,
            "condition" to weather.condition,
            "recommendation" to weather.smartRecommendation,
            "humidity" to weather.humidityPercent,
            "feelsLike" to weather.feelsLikeC,
            "windSpeed" to weather.windSpeedKmh,
            "precipitation" to weather.precipitationMm,
            "location" to weather.locationName,
            "latitude" to weather.latitude,
            "longitude" to weather.longitude,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("weather").document(uid).set(weatherDoc, SetOptions.merge())
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to save weather to Firestore: ${e.message}")
            }
    }

    // ========================================================
    // Water Quality Analyses (water_quality_analyses/{uid}/records)
    // ========================================================

    suspend fun saveWaterQualityAnalysis(record: WaterQualityAnalysisRecord): Result<String> {
        val uid = currentUserId ?: return Result.failure(Exception("User is not signed in"))
        return try {
            val docId = record.id.ifBlank { firestore.collection("water_quality_analyses").document(uid).collection("records").document().id }
            val payload = hashMapOf<String, Any?>(
                "sourceName" to record.sourceName,
                "ph" to record.ph,
                "turbidityNtu" to record.turbidityNtu,
                "temperatureC" to record.temperatureC,
                "tdsPpm" to record.tdsPpm,
                "ecUsCm" to record.ecUsCm,
                "dissolvedOxygenMgL" to record.dissolvedOxygenMgL,
                "notes" to record.notes,
                "hasAttachedImage" to record.hasAttachedImage,
                "statusCategory" to record.statusCategory,
                "summary" to record.summary,
                "measuredFindings" to record.measuredFindings,
                "visualObservations" to record.visualObservations,
                "warnings" to record.warnings,
                "recommendations" to record.recommendations,
                "disclaimer" to record.disclaimer,
                "analyzedByGemini" to record.analyzedByGemini,
                "createdAtEpochMs" to record.createdAtEpochMs,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("water_quality_analyses").document(uid)
                .collection("records").document(docId).set(payload).await()
            Result.success(docId)
        } catch (e: Exception) {
            Log.w(TAG, "Error saving water quality analysis: ${e.message}")
            Result.failure(e)
        }
    }

    fun observeWaterQualityAnalyses(limit: Long = 50): Flow<List<WaterQualityAnalysisRecord>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = firestore.collection("water_quality_analyses").document(uid)
            .collection("records")
            .orderBy("createdAtEpochMs", Query.Direction.DESCENDING)
            .limit(limit)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.w(TAG, "Water quality query error: ${error?.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            val list = snapshot.documents.map { doc ->
                @Suppress("UNCHECKED_CAST")
                val warnings = (doc.get("warnings") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val recommendations = (doc.get("recommendations") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                WaterQualityAnalysisRecord(
                    id = doc.id,
                    sourceName = doc.getString("sourceName") ?: "Water Sample",
                    ph = doc.getDouble("ph"),
                    turbidityNtu = doc.getDouble("turbidityNtu"),
                    temperatureC = doc.getDouble("temperatureC"),
                    tdsPpm = doc.getDouble("tdsPpm"),
                    ecUsCm = doc.getDouble("ecUsCm"),
                    dissolvedOxygenMgL = doc.getDouble("dissolvedOxygenMgL"),
                    notes = doc.getString("notes") ?: "",
                    hasAttachedImage = doc.getBoolean("hasAttachedImage") ?: false,
                    statusCategory = doc.getString("statusCategory") ?: "Saved",
                    summary = doc.getString("summary") ?: "",
                    measuredFindings = doc.getString("measuredFindings") ?: "",
                    visualObservations = doc.getString("visualObservations"),
                    warnings = warnings,
                    recommendations = recommendations,
                    disclaimer = doc.getString("disclaimer") ?: WaterQualityAnalysisRecord.DEFAULT_SAFETY_DISCLAIMER,
                    analyzedByGemini = doc.getBoolean("analyzedByGemini") ?: false,
                    createdAtEpochMs = doc.getLong("createdAtEpochMs") ?: System.currentTimeMillis()
                )
            }
            trySend(list)
        }

        awaitClose { registration.remove() }
    }

    suspend fun deleteWaterQualityAnalysis(recordId: String): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("User is not signed in"))
        return try {
            firestore.collection("water_quality_analyses").document(uid)
                .collection("records").document(recordId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================
    // Water Usage Records (water_usage/{uid}/records)
    // ========================================================

    suspend fun saveWaterUsageRecord(record: WaterUsageRecord): Result<String> {
        val uid = currentUserId ?: return Result.failure(Exception("User is not signed in"))
        return try {
            val docId = record.id.ifBlank { firestore.collection("water_usage").document(uid).collection("records").document().id }
            val payload = hashMapOf<String, Any?>(
                "amountLiters" to record.amountLiters,
                "category" to record.category,
                "source" to record.source,
                "notes" to record.notes,
                "usageDateEpochMs" to record.usageDateEpochMs,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("water_usage").document(uid)
                .collection("records").document(docId).set(payload).await()
            Result.success(docId)
        } catch (e: Exception) {
            Log.w(TAG, "Error saving water usage: ${e.message}")
            Result.failure(e)
        }
    }

    fun observeWaterUsageRecords(limit: Long = 200): Flow<List<WaterUsageRecord>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = firestore.collection("water_usage").document(uid)
            .collection("records")
            .orderBy("usageDateEpochMs", Query.Direction.DESCENDING)
            .limit(limit)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.w(TAG, "Water usage query error: ${error?.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            val list = snapshot.documents.map { doc ->
                WaterUsageRecord(
                    id = doc.id,
                    amountLiters = doc.getDouble("amountLiters") ?: 0.0,
                    category = doc.getString("category") ?: "Irrigation",
                    source = doc.getString("source") ?: "Underground Tank",
                    notes = doc.getString("notes") ?: "",
                    usageDateEpochMs = doc.getLong("usageDateEpochMs") ?: System.currentTimeMillis()
                )
            }
            trySend(list)
        }

        awaitClose { registration.remove() }
    }

    suspend fun deleteWaterUsageRecord(recordId: String): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("User is not signed in"))
        return try {
            firestore.collection("water_usage").document(uid)
                .collection("records").document(recordId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================
    // AI Water Assistant Chat History (ai_water_chat/{uid}/records)
    // ========================================================

    suspend fun saveWaterChatMessage(message: WaterAssistantMessage): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("User is not signed in"))
        return try {
            val docId = message.id.ifBlank { firestore.collection("ai_water_chat").document(uid).collection("records").document().id }
            val payload = hashMapOf<String, Any?>(
                "role" to message.role,
                "content" to message.content,
                "language" to message.language,
                "timestampEpochMs" to message.timestampEpochMs,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("ai_water_chat").document(uid)
                .collection("records").document(docId).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Error saving chat message: ${e.message}")
            Result.failure(e)
        }
    }

    fun observeWaterChatMessages(limit: Long = 100): Flow<List<WaterAssistantMessage>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = firestore.collection("ai_water_chat").document(uid)
            .collection("records")
            .orderBy("timestampEpochMs", Query.Direction.ASCENDING)
            .limit(limit)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.w(TAG, "Water chat query error: ${error?.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            val list = snapshot.documents.map { doc ->
                WaterAssistantMessage(
                    id = doc.id,
                    role = doc.getString("role") ?: "user",
                    content = doc.getString("content") ?: "",
                    language = doc.getString("language") ?: "English",
                    timestampEpochMs = doc.getLong("timestampEpochMs") ?: System.currentTimeMillis()
                )
            }
            trySend(list)
        }

        awaitClose { registration.remove() }
    }

    suspend fun clearWaterChatHistory(): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("User is not signed in"))
        return try {
            val docs = firestore.collection("ai_water_chat").document(uid)
                .collection("records").get().await()
            val batch = firestore.batch()
            docs.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun translateAuthError(e: Throwable): String {
        val msg = e.message ?: ""
        return when {
            e is FirebaseAuthInvalidUserException || msg.contains("user-not-found", ignoreCase = true) ->
                "No account found with this email address. Please create an account or check your email."
            e is FirebaseAuthInvalidCredentialsException ||
                msg.contains("wrong-password", ignoreCase = true) ||
                msg.contains("invalid-credential", ignoreCase = true) ||
                msg.contains("supplied auth credential", ignoreCase = true) ||
                msg.contains("malformed", ignoreCase = true) ||
                msg.contains("expired", ignoreCase = true) ||
                msg.contains("invalid email", ignoreCase = true) ->
                "Incorrect email or password, or no account exists for this email yet. Check your credentials, create a new account, or reset your password."
            e is FirebaseAuthUserCollisionException || msg.contains("already in use", ignoreCase = true) || msg.contains("already exists", ignoreCase = true) ->
                "An account with this email address already exists. Please sign in instead."
            e is FirebaseAuthWeakPasswordException || msg.contains("weak-password", ignoreCase = true) ->
                "Password is too weak. Please use at least 6 characters."
            msg.contains("too-many-requests", ignoreCase = true) || msg.contains("unusual activity", ignoreCase = true) ->
                "Too many unsuccessful sign-in attempts. Please wait a moment or reset your password."
            msg.contains("operation-not-allowed", ignoreCase = true) ->
                "Email/password sign-in is not enabled for this Firebase project. Enable Email/Password in Firebase Console."
            e is FirebaseNetworkException || msg.contains("network", ignoreCase = true) ->
                "Network error. Please check your internet connection."
            e is FirebaseException -> e.localizedMessage ?: "Unable to connect to AquaSustain cloud services."
            else -> e.localizedMessage ?: "An unexpected error occurred. Please try again."
        }
    }
}

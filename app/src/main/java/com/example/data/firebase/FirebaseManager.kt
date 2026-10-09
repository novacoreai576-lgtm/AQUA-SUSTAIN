package com.example.data.firebase

import android.util.Log
import com.example.data.model.AquaSustainData
import com.example.data.model.SensorReading
import com.example.data.model.UserProfile
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    // Deduplication tracking for sensorHistory writes
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
                ensureInitialUserDocuments(user.uid, user.displayName ?: "AquaSustain User", user.email ?: "")
            } else {
                _userProfile.value = null
            }
        }
    }

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Sign In with Email & Password
     */
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
     * Create Account & Initialize Firestore Profile:
     * users/{userId}
     */
    suspend fun signUp(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw Exception("Failed to register user")
            _currentUser.value = user
            val uid = user.uid

            // 1. users/{userId}
            val profileData = hashMapOf(
                "name" to name.trim(),
                "email" to email.trim(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid).set(profileData).await()

            // 2. sensorData/{userId}
            val initialSensorData = hashMapOf(
                "rain" to false,
                "soilMoisture" to 35,
                "waterLevel" to 72,
                "pump" to true,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("sensorData").document(uid).set(initialSensorData).await()

            // 3. pumpControl/{userId}
            val initialPumpControl = hashMapOf(
                "pump" to true,
                "mode" to "auto",
                "command" to "PUMP_ON",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("pumpControl").document(uid).set(initialPumpControl).await()

            // 4. weather/{userId}
            val initialWeather = hashMapOf(
                "temperature" to 30,
                "rainProbability" to 80,
                "condition" to "Cloudy",
                "recommendation" to "Rain is likely. Consider delaying irrigation.",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("weather").document(uid).set(initialWeather).await()

            // 5. settings/{userId}
            val initialSettings = hashMapOf(
                "arduinoEndpoint" to "http://192.168.1.150/api/status",
                "board" to "Arduino UNO R4 WiFi",
                "soilThreshold" to 50,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("settings").document(uid).set(initialSettings).await()

            // 6. Seed initial history records in sensorHistory/{userId}/records/{recordId}
            seedInitialHistoryRecords(uid)

            fetchUserProfile(uid)
            Result.success(user)
        } catch (e: Exception) {
            Log.w(TAG, "Sign up note: ${e.message}")
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    /**
     * Send Password Reset Email
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    /**
     * Sign Out:
     * Clears user session from Firebase Auth and resets local profile state
     */
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _userProfile.value = null
        lastRecordedSoil = -1
        lastRecordedWater = -1
        lastRecordedRain = null
        lastRecordedPump = null
    }

    /**
     * Fetch user profile: users/{userId}
     */
    fun fetchUserProfile(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: "AquaSustain User"
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

    /**
     * Ensure initial user docs exist if user existed previously
     */
    private fun ensureInitialUserDocuments(uid: String, name: String, email: String) {
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

        firestore.collection("sensorData").document(uid).get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val sensorDoc = hashMapOf(
                    "rain" to false,
                    "soilMoisture" to 35,
                    "waterLevel" to 72,
                    "pump" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("sensorData").document(uid).set(sensorDoc)
                seedInitialHistoryRecords(uid)
            }
        }

        firestore.collection("pumpControl").document(uid).get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val pumpDoc = hashMapOf(
                    "pump" to true,
                    "mode" to "auto",
                    "command" to "PUMP_ON",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("pumpControl").document(uid).set(pumpDoc)
            }
        }

        firestore.collection("weather").document(uid).get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val weatherDoc = hashMapOf(
                    "temperature" to 30,
                    "rainProbability" to 80,
                    "condition" to "Cloudy",
                    "recommendation" to "Rain is likely. Consider delaying irrigation.",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("weather").document(uid).set(weatherDoc)
            }
        }
    }

    /**
     * OBSERVE LIVE SENSOR DATA:
     * sensorData/{currentUser.uid}
     */
    fun observeLiveSensorData(): Flow<AquaSustainData> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(AquaSustainData())
            close()
            return@callbackFlow
        }

        val docRef = firestore.collection("sensorData").document(uid)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen error for sensorData/$uid: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val rain = snapshot.getBoolean("rain") ?: false
                val soil = snapshot.getLong("soilMoisture")?.toInt() ?: 35
                val water = snapshot.getLong("waterLevel")?.toInt() ?: 72
                val pump = snapshot.getBoolean("pump") ?: false
                val timestamp = snapshot.getTimestamp("updatedAt")
                val epochMs = timestamp?.toDate()?.time ?: System.currentTimeMillis()

                val data = AquaSustainData(
                    rain = rain,
                    rainStatus = if (rain) "RAIN DETECTED" else "NO RAIN",
                    soilMoisture = soil.coerceIn(0, 100),
                    waterLevel = water.coerceIn(0, 100),
                    pump = pump,
                    pumpStatus = if (pump) "ON" else "OFF",
                    updatedAtEpochMs = epochMs
                )
                trySend(data)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * SAVE LIVE SENSOR DATA & STORE SENSOR HISTORY:
     * 1. Updates sensorData/{userId}
     * 2. Automatically stores reading into sensorHistory/{userId}/records/{recordId}
     * Avoids duplicate records when data is unchanged.
     */
    suspend fun saveLiveSensorData(data: AquaSustainData) {
        val uid = currentUserId ?: return
        try {
            // Update sensorData/{userId}
            val sensorDoc = hashMapOf(
                "rain" to data.rain,
                "soilMoisture" to data.soilMoisture.coerceIn(0, 100),
                "waterLevel" to data.waterLevel.coerceIn(0, 100),
                "pump" to data.pump,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("sensorData").document(uid).set(sensorDoc).await()

            // Check if record should be added to history (deduplication check)
            val now = System.currentTimeMillis()
            val stateChanged = (data.rain != lastRecordedRain) ||
                    (data.pump != lastRecordedPump) ||
                    (abs(data.soilMoisture - lastRecordedSoil) >= 3) ||
                    (abs(data.waterLevel - lastRecordedWater) >= 3) ||
                    (now - lastRecordTimestampMs >= 120_000L) // at least record every 2 min if continuous

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

    /**
     * STORE SENSOR HISTORY:
     * sensorHistory/{userId}/records/{recordId}
     * {
     *   "rain": false,
     *   "soilMoisture": 35,
     *   "waterLevel": 72,
     *   "pump": false,
     *   "timestamp": serverTimestamp()
     * }
     */
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

    /**
     * OBSERVE SENSOR HISTORY:
     * Reads from sensorHistory/{currentUser.uid}/records
     */
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

            val readings = snapshot.documents.mapNotNull { doc ->
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

        awaitClose {
            registration.remove()
        }
    }

    /**
     * PUMP CONTROL & PUMP STATE CHANGE HISTORY:
     * Whenever pump changes state: ON -> OFF or OFF -> ON, save event to user's history
     * and update pumpControl/{userId}.
     */
    suspend fun updatePumpControl(
        targetPumpOn: Boolean,
        mode: String = "manual",
        currentRain: Boolean = false,
        currentSoil: Int = 35,
        currentWater: Int = 72
    ): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("User is not signed in"))
        return try {
            val command = if (targetPumpOn) "PUMP_ON" else "PUMP_OFF"

            // 1. Update pumpControl/{userId}
            val pumpDoc = hashMapOf(
                "pump" to targetPumpOn,
                "mode" to mode,
                "command" to command,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("pumpControl").document(uid).set(pumpDoc).await()

            // 2. Update sensorData/{userId}
            val sensorDoc = hashMapOf(
                "rain" to currentRain,
                "soilMoisture" to currentSoil.coerceIn(0, 100),
                "waterLevel" to currentWater.coerceIn(0, 100),
                "pump" to targetPumpOn,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("sensorData").document(uid).set(sensorDoc).await()

            // 3. Save event to sensorHistory/{userId}/records/{recordId}
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

    /**
     * OBSERVE WEATHER:
     * weather/{currentUser.uid}
     */
    fun observeWeather(): Flow<WeatherInfo> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(WeatherInfo())
            close()
            return@callbackFlow
        }

        val docRef = firestore.collection("weather").document(uid)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                trySend(WeatherInfo())
                return@addSnapshotListener
            }

            val temp = snapshot.getLong("temperature")?.toInt() ?: 27
            val rainProb = snapshot.getLong("rainProbability")?.toInt() ?: 75
            val cond = snapshot.getString("condition") ?: "Partly Cloudy"
            val rec = snapshot.getString("recommendation") ?: "Stable atmospheric conditions."
            val humidity = snapshot.getLong("humidity")?.toInt() ?: 68
            val feelsLike = snapshot.getLong("feelsLike")?.toInt() ?: temp
            val windSpeed = snapshot.getDouble("windSpeed") ?: 12.0
            val precip = snapshot.getDouble("precipitation") ?: 0.0
            val loc = snapshot.getString("location") ?: "Farm Station"
            val lat = snapshot.getDouble("latitude") ?: 28.6139
            val lon = snapshot.getDouble("longitude") ?: 77.2090

            trySend(
                WeatherInfo(
                    temperatureC = temp,
                    rainProbabilityPercent = rainProb,
                    condition = cond,
                    smartRecommendation = rec,
                    rainPredictionText = if (rainProb >= 60) "Rain likely in next 3 hours" else "Low rain probability",
                    humidityPercent = humidity,
                    feelsLikeC = feelsLike,
                    windSpeedKmh = windSpeed,
                    precipitationMm = precip,
                    locationName = loc,
                    latitude = lat,
                    longitude = lon,
                    isLive = true
                )
            )
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * SAVES LIVE WEATHER TO FIRESTORE:
     * weather/{currentUser.uid}
     */
    fun saveWeather(weather: WeatherInfo) {
        val uid = currentUserId ?: return
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

    /**
     * Seed initial history records for a new user
     */
    private fun seedInitialHistoryRecords(uid: String) {
        val recordsRef = firestore.collection("sensorHistory").document(uid).collection("records")
        val now = System.currentTimeMillis()

        val samplePoints = listOf(
            Triple(false, 35, 72) to true,
            Triple(false, 38, 74) to true,
            Triple(false, 42, 75) to true,
            Triple(true, 65, 80) to false,
            Triple(true, 70, 85) to false,
            Triple(false, 55, 82) to false,
            Triple(false, 48, 80) to true
        )

        samplePoints.forEachIndexed { i, pair ->
            val (rain, soil, water) = pair.first
            val pump = pair.second
            val time = Timestamp(Date(now - (samplePoints.size - i) * 30 * 60 * 1000L))
            val rec = hashMapOf(
                "rain" to rain,
                "soilMoisture" to soil,
                "waterLevel" to water,
                "pump" to pump,
                "timestamp" to time
            )
            recordsRef.add(rec)
        }
    }

    private fun translateAuthError(e: Throwable): String {
        val msg = e.message ?: ""
        return when {
            e is FirebaseAuthInvalidUserException || msg.contains("user-not-found", ignoreCase = true) ->
                "No account found with this email address. Please register or check your email."
            e is FirebaseAuthInvalidCredentialsException || msg.contains("wrong-password", ignoreCase = true) || msg.contains("invalid-credential", ignoreCase = true) ->
                "Incorrect email or password. Please verify your credentials or reset your password."
            e is FirebaseAuthUserCollisionException || msg.contains("already in use", ignoreCase = true) || msg.contains("already exists", ignoreCase = true) ->
                "An account with this email address already exists. Please sign in instead."
            e is FirebaseAuthWeakPasswordException || msg.contains("weak-password", ignoreCase = true) ->
                "Password is too weak. Please use at least 6 characters."
            e is FirebaseNetworkException || msg.contains("network", ignoreCase = true) ->
                "Network error. Please check your internet connection."
            e is FirebaseException -> e.localizedMessage ?: "Unable to connect to AquaSustain services."
            else -> e.localizedMessage ?: "An unexpected error occurred. Please try again."
        }
    }
}

package com.example.data.firebase

import com.example.data.model.BioLeafChatMessage
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityPost
import com.example.data.model.DiseaseReportRecord
import com.example.data.model.PestReportRecord
import com.example.data.model.PlantIdRecord
import com.example.data.model.SoilGuidanceRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BioLeafFirestoreRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Please sign in to access Bio Leaf cloud records.")

    fun currentUidOrNull(): String? = auth.currentUser?.uid

    fun observePlantIdentifications(): Flow<List<PlantIdRecord>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("bioleaf_plant_identifications")
            .document(uid)
            .collection("records")
            .orderBy("timestampEpochMs", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PlantIdRecord::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observeDiseaseReports(): Flow<List<DiseaseReportRecord>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("bioleaf_disease_reports")
            .document(uid)
            .collection("records")
            .orderBy("timestampEpochMs", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DiseaseReportRecord::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observePestReports(): Flow<List<PestReportRecord>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("bioleaf_pest_reports")
            .document(uid)
            .collection("records")
            .orderBy("timestampEpochMs", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PestReportRecord::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observeSoilGuidance(): Flow<List<SoilGuidanceRecord>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("bioleaf_soil_guidance")
            .document(uid)
            .collection("records")
            .orderBy("timestampEpochMs", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SoilGuidanceRecord::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observeChatMessages(): Flow<List<BioLeafChatMessage>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("bioleaf_chat_messages")
            .document(uid)
            .collection("records")
            .orderBy("timestampEpochMs", Query.Direction.ASCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(BioLeafChatMessage::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observeCommunityPosts(): Flow<List<CommunityPost>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("bioleaf_community_posts")
            .orderBy("timestampEpochMs", Query.Direction.DESCENDING)
            .limit(60)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CommunityPost::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observePostComments(postId: String): Flow<List<CommunityComment>> = callbackFlow {
        if (auth.currentUser == null || postId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("bioleaf_community_posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestampEpochMs", Query.Direction.ASCENDING)
            .limit(80)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CommunityComment::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observeSavedPostIds(): Flow<Set<String>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("bioleaf_saved_posts")
            .document(uid)
            .collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptySet())
                    return@addSnapshotListener
                }
                val ids = snapshot?.documents?.map { it.id }?.toSet().orEmpty()
                trySend(ids)
            }
        awaitClose { registration.remove() }
    }

    suspend fun savePlantIdentification(record: PlantIdRecord): Result<String> = runCatching {
        val uid = requireUid()
        val ref = firestore.collection("bioleaf_plant_identifications")
            .document(uid).collection("records").document()
        val payload = hashMapOf(
            "commonName" to record.commonName,
            "scientificName" to record.scientificName,
            "plantDescription" to record.plantDescription,
            "identifyingCharacteristics" to record.identifyingCharacteristics,
            "identificationExplanation" to record.identificationExplanation,
            "uncertaintyNote" to record.uncertaintyNote,
            "imageThumbnailBase64" to record.imageThumbnailBase64,
            "timestamp" to FieldValue.serverTimestamp(),
            "timestampEpochMs" to record.timestampEpochMs
        )
        ref.set(payload).await()
        ref.id
    }

    suspend fun saveDiseaseReport(record: DiseaseReportRecord): Result<String> = runCatching {
        val uid = requireUid()
        val ref = firestore.collection("bioleaf_disease_reports")
            .document(uid).collection("records").document()
        val payload = hashMapOf(
            "plantName" to record.plantName,
            "problem" to record.problem,
            "severity" to record.severity,
            "visibleSymptoms" to record.visibleSymptoms,
            "possibleCauses" to record.possibleCauses,
            "detailedExplanation" to record.detailedExplanation,
            "recommendedTreatment" to record.recommendedTreatment,
            "organicTreatment" to record.organicTreatment,
            "preventionMethods" to record.preventionMethods,
            "visualObservationNote" to record.visualObservationNote,
            "imageThumbnailBase64" to record.imageThumbnailBase64,
            "timestamp" to FieldValue.serverTimestamp(),
            "timestampEpochMs" to record.timestampEpochMs
        )
        ref.set(payload).await()
        ref.id
    }

    suspend fun savePestReport(record: PestReportRecord): Result<String> = runCatching {
        val uid = requireUid()
        val ref = firestore.collection("bioleaf_pest_reports")
            .document(uid).collection("records").document()
        val payload = hashMapOf(
            "possiblePestIdentification" to record.possiblePestIdentification,
            "pestFamily" to record.pestFamily,
            "visibleCharacteristics" to record.visibleCharacteristics,
            "feedingDamage" to record.feedingDamage,
            "pestSeverity" to record.pestSeverity,
            "recommendedManagement" to record.recommendedManagement,
            "organicPestControlMethods" to record.organicPestControlMethods,
            "prevention" to record.prevention,
            "uncertaintyNote" to record.uncertaintyNote,
            "imageThumbnailBase64" to record.imageThumbnailBase64,
            "timestamp" to FieldValue.serverTimestamp(),
            "timestampEpochMs" to record.timestampEpochMs
        )
        ref.set(payload).await()
        ref.id
    }

    suspend fun saveSoilGuidance(record: SoilGuidanceRecord): Result<String> = runCatching {
        val uid = requireUid()
        val ref = firestore.collection("bioleaf_soil_guidance")
            .document(uid).collection("records").document()
        val payload = hashMapOf(
            "soilType" to record.soilType,
            "cropName" to record.cropName,
            "irrigationFrequency" to record.irrigationFrequency,
            "wateringPractices" to record.wateringPractices,
            "soilObservations" to record.soilObservations,
            "fertilizerPractices" to record.fertilizerPractices,
            "organicMatterInfo" to record.organicMatterInfo,
            "soilImprovementRecommendations" to record.soilImprovementRecommendations,
            "irrigationGuidance" to record.irrigationGuidance,
            "compostRecommendations" to record.compostRecommendations,
            "vermicompostGuidance" to record.vermicompostGuidance,
            "neemCakeGuidance" to record.neemCakeGuidance,
            "soilConservation" to record.soilConservation,
            "sustainableFarmingPractices" to record.sustainableFarmingPractices,
            "timestamp" to FieldValue.serverTimestamp(),
            "timestampEpochMs" to record.timestampEpochMs
        )
        ref.set(payload).await()
        ref.id
    }

    suspend fun deleteRecord(collectionRoot: String, recordId: String): Result<Unit> = runCatching {
        val uid = requireUid()
        firestore.collection(collectionRoot)
            .document(uid)
            .collection("records")
            .document(recordId)
            .delete()
            .await()
    }

    suspend fun saveChatMessage(message: BioLeafChatMessage): Result<String> = runCatching {
        val uid = requireUid()
        val ref = firestore.collection("bioleaf_chat_messages")
            .document(uid).collection("records").document()
        val payload = hashMapOf(
            "sender" to message.sender,
            "text" to message.text,
            "detectedLanguage" to message.detectedLanguage,
            "imageThumbnailBase64" to message.imageThumbnailBase64,
            "timestamp" to FieldValue.serverTimestamp(),
            "timestampEpochMs" to message.timestampEpochMs
        )
        ref.set(payload).await()
        ref.id
    }

    suspend fun clearChatHistory(): Result<Unit> = runCatching {
        val uid = requireUid()
        val col = firestore.collection("bioleaf_chat_messages").document(uid).collection("records")
        val snap = col.get().await()
        val batch = firestore.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    suspend fun createCommunityPost(
        authorName: String,
        cropTag: String,
        title: String,
        content: String,
        imageThumbnailBase64: String?
    ): Result<String> = runCatching {
        val uid = requireUid()
        val ref = firestore.collection("bioleaf_community_posts").document()
        val payload = hashMapOf(
            "authorUid" to uid,
            "authorName" to authorName.ifBlank { "Farmer" },
            "cropTag" to cropTag.trim(),
            "title" to title.trim(),
            "content" to content.trim(),
            "imageThumbnailBase64" to imageThumbnailBase64,
            "commentCount" to 0,
            "timestamp" to FieldValue.serverTimestamp(),
            "timestampEpochMs" to System.currentTimeMillis()
        )
        ref.set(payload).await()
        ref.id
    }

    suspend fun deleteCommunityPost(postId: String): Result<Unit> = runCatching {
        requireUid()
        firestore.collection("bioleaf_community_posts").document(postId).delete().await()
    }

    suspend fun addCommunityComment(
        postId: String,
        authorName: String,
        text: String,
        isAiAssisted: Boolean = false
    ): Result<String> = runCatching {
        val uid = requireUid()
        val ref = firestore.collection("bioleaf_community_posts")
            .document(postId)
            .collection("comments")
            .document()
        val payload = hashMapOf(
            "postId" to postId,
            "authorUid" to uid,
            "authorName" to authorName.ifBlank { "Farmer" },
            "text" to text.trim(),
            "isAiAssisted" to isAiAssisted,
            "timestamp" to FieldValue.serverTimestamp(),
            "timestampEpochMs" to System.currentTimeMillis()
        )
        ref.set(payload).await()
        ref.id
    }

    suspend fun toggleSavePost(postId: String, currentlySaved: Boolean): Result<Unit> = runCatching {
        val uid = requireUid()
        val docRef = firestore.collection("bioleaf_saved_posts")
            .document(uid).collection("items").document(postId)
        if (currentlySaved) {
            docRef.delete().await()
        } else {
            docRef.set(
                hashMapOf(
                    "postId" to postId,
                    "savedAtEpochMs" to System.currentTimeMillis()
                )
            ).await()
        }
    }

    suspend fun reportCommunityPost(postId: String, reason: String): Result<Unit> = runCatching {
        val uid = requireUid()
        firestore.collection("bioleaf_post_reports").add(
            hashMapOf(
                "postId" to postId,
                "reporterUid" to uid,
                "reason" to reason.trim(),
                "timestamp" to FieldValue.serverTimestamp(),
                "timestampEpochMs" to System.currentTimeMillis()
            )
        ).await()
    }
}

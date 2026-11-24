package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.BioLeafFirestoreRepository
import com.example.data.model.BioLeafChatMessage
import com.example.data.model.BioLeafReportCategory
import com.example.data.model.BioLeafReportFormatter
import com.example.data.model.BioLeafSavedReportSummary
import com.example.data.model.BioLeafSection
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityPost
import com.example.data.model.DiseaseReportRecord
import com.example.data.model.PestReportRecord
import com.example.data.model.PlantIdRecord
import com.example.data.model.SoilGuidanceInput
import com.example.data.model.SoilGuidanceRecord
import com.example.data.remote.BioLeafGeminiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BioLeafViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val geminiService = BioLeafGeminiService()
    private val repository = BioLeafFirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _activeSection = MutableStateFlow(BioLeafSection.HOME)
    val activeSection: StateFlow<BioLeafSection> = _activeSection.asStateFlow()

    // Active results
    private val _currentPlantResult = MutableStateFlow<PlantIdRecord?>(null)
    val currentPlantResult: StateFlow<PlantIdRecord?> = _currentPlantResult.asStateFlow()

    private val _currentDiseaseResult = MutableStateFlow<DiseaseReportRecord?>(null)
    val currentDiseaseResult: StateFlow<DiseaseReportRecord?> = _currentDiseaseResult.asStateFlow()

    private val _currentPestResult = MutableStateFlow<PestReportRecord?>(null)
    val currentPestResult: StateFlow<PestReportRecord?> = _currentPestResult.asStateFlow()

    private val _currentSoilResult = MutableStateFlow<SoilGuidanceRecord?>(null)
    val currentSoilResult: StateFlow<SoilGuidanceRecord?> = _currentSoilResult.asStateFlow()

    // Loading flags
    private val _isAnalyzingPlant = MutableStateFlow(false)
    val isAnalyzingPlant: StateFlow<Boolean> = _isAnalyzingPlant.asStateFlow()

    private val _isAnalyzingDisease = MutableStateFlow(false)
    val isAnalyzingDisease: StateFlow<Boolean> = _isAnalyzingDisease.asStateFlow()

    private val _isAnalyzingPest = MutableStateFlow(false)
    val isAnalyzingPest: StateFlow<Boolean> = _isAnalyzingPest.asStateFlow()

    private val _isGeneratingSoil = MutableStateFlow(false)
    val isGeneratingSoil: StateFlow<Boolean> = _isGeneratingSoil.asStateFlow()

    private val _isChatResponding = MutableStateFlow(false)
    val isChatResponding: StateFlow<Boolean> = _isChatResponding.asStateFlow()

    private val _isPostingCommunity = MutableStateFlow(false)
    val isPostingCommunity: StateFlow<Boolean> = _isPostingCommunity.asStateFlow()

    // Error messages
    private val _plantError = MutableStateFlow<String?>(null)
    val plantError: StateFlow<String?> = _plantError.asStateFlow()

    private val _diseaseError = MutableStateFlow<String?>(null)
    val diseaseError: StateFlow<String?> = _diseaseError.asStateFlow()

    private val _pestError = MutableStateFlow<String?>(null)
    val pestError: StateFlow<String?> = _pestError.asStateFlow()

    private val _soilError = MutableStateFlow<String?>(null)
    val soilError: StateFlow<String?> = _soilError.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    private val _communityError = MutableStateFlow<String?>(null)
    val communityError: StateFlow<String?> = _communityError.asStateFlow()

    // Firestore-backed streams
    private val _plantHistory = MutableStateFlow<List<PlantIdRecord>>(emptyList())
    val plantHistory: StateFlow<List<PlantIdRecord>> = _plantHistory.asStateFlow()

    private val _diseaseHistory = MutableStateFlow<List<DiseaseReportRecord>>(emptyList())
    val diseaseHistory: StateFlow<List<DiseaseReportRecord>> = _diseaseHistory.asStateFlow()

    private val _pestHistory = MutableStateFlow<List<PestReportRecord>>(emptyList())
    val pestHistory: StateFlow<List<PestReportRecord>> = _pestHistory.asStateFlow()

    private val _soilHistory = MutableStateFlow<List<SoilGuidanceRecord>>(emptyList())
    val soilHistory: StateFlow<List<SoilGuidanceRecord>> = _soilHistory.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<BioLeafChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<BioLeafChatMessage>> = _chatMessages.asStateFlow()

    private val _communityPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

    private val _savedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val savedPostIds: StateFlow<Set<String>> = _savedPostIds.asStateFlow()

    private val _commentsByPostId = MutableStateFlow<Map<String, List<CommunityComment>>>(emptyMap())
    val commentsByPostId: StateFlow<Map<String, List<CommunityComment>>> = _commentsByPostId.asStateFlow()

    val savedReports: StateFlow<List<BioLeafSavedReportSummary>> = combine(
        _plantHistory,
        _diseaseHistory,
        _pestHistory,
        _soilHistory
    ) { plants, diseases, pests, soils ->
        BioLeafReportFormatter.buildUnifiedReports(plants, diseases, pests, soils)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var observationJobs = mutableListOf<Job>()
    private val commentJobs = mutableMapOf<String, Job>()
    private var observedUid: String? = null

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (uid != observedUid) {
            observedUid = uid
            stopAllObservations()
            if (uid != null) {
                startObservations()
            } else {
                clearInMemoryUserState()
            }
        }
    }

    init {
        auth.addAuthStateListener(authListener)
        auth.currentUser?.uid?.let { uid ->
            observedUid = uid
            startObservations()
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        stopAllObservations()
    }

    private fun startObservations() {
        observationJobs += viewModelScope.launch {
            repository.observePlantIdentifications().collect { _plantHistory.value = it }
        }
        observationJobs += viewModelScope.launch {
            repository.observeDiseaseReports().collect { _diseaseHistory.value = it }
        }
        observationJobs += viewModelScope.launch {
            repository.observePestReports().collect { _pestHistory.value = it }
        }
        observationJobs += viewModelScope.launch {
            repository.observeSoilGuidance().collect { _soilHistory.value = it }
        }
        observationJobs += viewModelScope.launch {
            repository.observeChatMessages().collect { _chatMessages.value = it }
        }
        observationJobs += viewModelScope.launch {
            repository.observeCommunityPosts().collect { _communityPosts.value = it }
        }
        observationJobs += viewModelScope.launch {
            repository.observeSavedPostIds().collect { _savedPostIds.value = it }
        }
    }

    private fun stopAllObservations() {
        observationJobs.forEach { it.cancel() }
        observationJobs.clear()
        commentJobs.values.forEach { it.cancel() }
        commentJobs.clear()
    }

    private fun clearInMemoryUserState() {
        _plantHistory.value = emptyList()
        _diseaseHistory.value = emptyList()
        _pestHistory.value = emptyList()
        _soilHistory.value = emptyList()
        _chatMessages.value = emptyList()
        _communityPosts.value = emptyList()
        _savedPostIds.value = emptySet()
        _commentsByPostId.value = emptyMap()
        _currentPlantResult.value = null
        _currentDiseaseResult.value = null
        _currentPestResult.value = null
        _currentSoilResult.value = null
    }

    fun selectSection(section: BioLeafSection) {
        _activeSection.value = section
    }

    fun clearErrors() {
        _plantError.value = null
        _diseaseError.value = null
        _pestError.value = null
        _soilError.value = null
        _chatError.value = null
        _communityError.value = null
    }

    fun identifyPlant(bitmap: Bitmap) {
        if (_isAnalyzingPlant.value) return
        _plantError.value = null
        _isAnalyzingPlant.value = true
        viewModelScope.launch {
            geminiService.identifyPlant(bitmap)
                .onSuccess { record ->
                    _currentPlantResult.value = record
                    repository.savePlantIdentification(record)
                        .onFailure { _plantError.value = "Analyzed locally, but cloud save failed: ${it.localizedMessage}" }
                }
                .onFailure { _plantError.value = it.localizedMessage ?: "Plant identification failed." }
            _isAnalyzingPlant.value = false
        }
    }

    fun analyzeDisease(bitmap: Bitmap) {
        if (_isAnalyzingDisease.value) return
        _diseaseError.value = null
        _isAnalyzingDisease.value = true
        viewModelScope.launch {
            geminiService.analyzeDisease(bitmap)
                .onSuccess { record ->
                    _currentDiseaseResult.value = record
                    repository.saveDiseaseReport(record)
                        .onFailure { _diseaseError.value = "Analyzed locally, but cloud save failed: ${it.localizedMessage}" }
                }
                .onFailure { _diseaseError.value = it.localizedMessage ?: "Disease analysis failed." }
            _isAnalyzingDisease.value = false
        }
    }

    fun identifyPest(bitmap: Bitmap) {
        if (_isAnalyzingPest.value) return
        _pestError.value = null
        _isAnalyzingPest.value = true
        viewModelScope.launch {
            geminiService.identifyPest(bitmap)
                .onSuccess { record ->
                    _currentPestResult.value = record
                    repository.savePestReport(record)
                        .onFailure { _pestError.value = "Analyzed locally, but cloud save failed: ${it.localizedMessage}" }
                }
                .onFailure { _pestError.value = it.localizedMessage ?: "Pest identification failed." }
            _isAnalyzingPest.value = false
        }
    }

    fun generateSoilGuidance(input: SoilGuidanceInput) {
        if (_isGeneratingSoil.value) return
        if (input.soilType.isBlank() || input.cropName.isBlank()) {
            _soilError.value = "Please enter both soil type and crop name."
            return
        }
        _soilError.value = null
        _isGeneratingSoil.value = true
        viewModelScope.launch {
            geminiService.generateSoilGuidance(input)
                .onSuccess { record ->
                    _currentSoilResult.value = record
                    repository.saveSoilGuidance(record)
                        .onFailure { _soilError.value = "Generated guidance, but cloud save failed: ${it.localizedMessage}" }
                }
                .onFailure { _soilError.value = it.localizedMessage ?: "Unable to generate soil guidance." }
            _isGeneratingSoil.value = false
        }
    }

    fun sendChatMessage(messageText: String, attachmentBitmap: Bitmap? = null) {
        val cleanText = messageText.trim()
        if (_isChatResponding.value || (cleanText.isBlank() && attachmentBitmap == null)) return
        val effectivePrompt = cleanText.ifBlank { "Please inspect this crop photograph and advise." }
        val thumb = attachmentBitmap?.let { BioLeafGeminiService.encodeThumbnailBase64(it) }
        _chatError.value = null
        _isChatResponding.value = true
        val priorHistory = _chatMessages.value

        viewModelScope.launch {
            val userMsg = BioLeafChatMessage(
                sender = "user",
                text = effectivePrompt,
                detectedLanguage = "Auto",
                imageThumbnailBase64 = thumb,
                timestampEpochMs = System.currentTimeMillis()
            )
            repository.saveChatMessage(userMsg)
                .onFailure { _chatError.value = "Failed to save chat message: ${it.localizedMessage}" }

            geminiService.askAssistant(effectivePrompt, priorHistory, attachmentBitmap)
                .onSuccess { reply ->
                    val aiMsg = BioLeafChatMessage(
                        sender = "assistant",
                        text = reply.text,
                        detectedLanguage = reply.detectedLanguage,
                        timestampEpochMs = System.currentTimeMillis()
                    )
                    repository.saveChatMessage(aiMsg)
                        .onFailure { _chatError.value = "Reply received, but cloud save failed: ${it.localizedMessage}" }
                }
                .onFailure { err ->
                    _chatError.value = err.localizedMessage ?: "AI Assistant failed to respond."
                }
            _isChatResponding.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
                .onFailure { _chatError.value = it.localizedMessage }
        }
    }

    fun observeCommentsForPost(postId: String) {
        if (postId.isBlank() || commentJobs.containsKey(postId)) return
        commentJobs[postId] = viewModelScope.launch {
            repository.observePostComments(postId).collect { list ->
                _commentsByPostId.value = _commentsByPostId.value + (postId to list)
            }
        }
    }

    fun createCommunityPost(
        authorName: String,
        cropTag: String,
        title: String,
        content: String,
        attachmentBitmap: Bitmap?,
        onCreated: () -> Unit = {}
    ) {
        if (_isPostingCommunity.value) return
        if (title.isBlank() || content.isBlank()) {
            _communityError.value = "Please enter both a post title and description."
            return
        }
        _communityError.value = null
        _isPostingCommunity.value = true
        val thumb = attachmentBitmap?.let { BioLeafGeminiService.encodeThumbnailBase64(it, maxDim = 400, quality = 68) }
        viewModelScope.launch {
            repository.createCommunityPost(authorName, cropTag, title, content, thumb)
                .onSuccess { onCreated() }
                .onFailure { _communityError.value = it.localizedMessage ?: "Failed to publish post." }
            _isPostingCommunity.value = false
        }
    }

    fun addCommunityComment(postId: String, authorName: String, text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            repository.addCommunityComment(postId, authorName, clean, isAiAssisted = false)
                .onFailure { _communityError.value = it.localizedMessage ?: "Failed to post comment." }
        }
    }

    fun requestAiCommentOnPost(post: CommunityPost) {
        if (_isPostingCommunity.value) return
        _communityError.value = null
        _isPostingCommunity.value = true
        viewModelScope.launch {
            geminiService.generateCommunityInsight(
                title = post.title,
                content = post.content,
                cropTag = post.cropTag,
                imageBase64 = post.imageThumbnailBase64
            ).onSuccess { insight ->
                repository.addCommunityComment(
                    postId = post.id,
                    authorName = "BIO LEAF AI",
                    text = insight,
                    isAiAssisted = true
                ).onFailure { _communityError.value = it.localizedMessage }
            }.onFailure {
                _communityError.value = it.localizedMessage ?: "AI insight unavailable."
            }
            _isPostingCommunity.value = false
        }
    }

    fun toggleSavePost(postId: String) {
        val isSaved = _savedPostIds.value.contains(postId)
        viewModelScope.launch {
            repository.toggleSavePost(postId, isSaved)
                .onFailure { _communityError.value = it.localizedMessage }
        }
    }

    fun deleteOwnPost(postId: String) {
        viewModelScope.launch {
            repository.deleteCommunityPost(postId)
                .onFailure { _communityError.value = it.localizedMessage }
        }
    }

    fun reportPost(postId: String, reason: String) {
        if (reason.isBlank()) return
        viewModelScope.launch {
            repository.reportCommunityPost(postId, reason)
                .onFailure { _communityError.value = it.localizedMessage }
        }
    }

    fun deleteSavedReport(report: BioLeafSavedReportSummary) {
        val collectionRoot = when (report.category) {
            BioLeafReportCategory.PLANT_ID -> "bioleaf_plant_identifications"
            BioLeafReportCategory.DISEASE -> "bioleaf_disease_reports"
            BioLeafReportCategory.PEST -> "bioleaf_pest_reports"
            BioLeafReportCategory.SOIL -> "bioleaf_soil_guidance"
        }
        viewModelScope.launch {
            repository.deleteRecord(collectionRoot, report.id)
        }
    }
}

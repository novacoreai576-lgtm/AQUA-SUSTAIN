package com.example.ui.screens.bioleaf

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BioLeafSection
import com.example.data.model.UserProfile
import com.example.ui.BioLeafViewModel
import com.example.ui.screens.AccountScreen
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.EcoOnGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import kotlinx.coroutines.launch

@Composable
fun BioLeafHubScreen(
    viewModel: BioLeafViewModel,
    userProfile: UserProfile?,
    currentUid: String?,
    onSignOut: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenArchitecture: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSection by viewModel.activeSection.collectAsStateWithLifecycle()
    val plantResult by viewModel.currentPlantResult.collectAsStateWithLifecycle()
    val diseaseResult by viewModel.currentDiseaseResult.collectAsStateWithLifecycle()
    val pestResult by viewModel.currentPestResult.collectAsStateWithLifecycle()
    val soilResult by viewModel.currentSoilResult.collectAsStateWithLifecycle()

    val isAnalyzingPlant by viewModel.isAnalyzingPlant.collectAsStateWithLifecycle()
    val isAnalyzingDisease by viewModel.isAnalyzingDisease.collectAsStateWithLifecycle()
    val isAnalyzingPest by viewModel.isAnalyzingPest.collectAsStateWithLifecycle()
    val isGeneratingSoil by viewModel.isGeneratingSoil.collectAsStateWithLifecycle()
    val isChatResponding by viewModel.isChatResponding.collectAsStateWithLifecycle()
    val isPostingCommunity by viewModel.isPostingCommunity.collectAsStateWithLifecycle()

    val plantError by viewModel.plantError.collectAsStateWithLifecycle()
    val diseaseError by viewModel.diseaseError.collectAsStateWithLifecycle()
    val pestError by viewModel.pestError.collectAsStateWithLifecycle()
    val soilError by viewModel.soilError.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()
    val communityError by viewModel.communityError.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val communityPosts by viewModel.communityPosts.collectAsStateWithLifecycle()
    val savedPostIds by viewModel.savedPostIds.collectAsStateWithLifecycle()
    val commentsByPostId by viewModel.commentsByPostId.collectAsStateWithLifecycle()
    val savedReports by viewModel.savedReports.collectAsStateWithLifecycle()

    BackHandler(enabled = activeSection != BioLeafSection.HOME) {
        viewModel.selectSection(BioLeafSection.HOME)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val userName = userProfile?.name?.ifBlank { "Farmer" } ?: "Farmer"

    val contentBody: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCanvas)
                .testTag("bioleaf_hub_container")
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val showTopMenuButton = maxWidth < 840.dp
                Column(modifier = Modifier.fillMaxSize()) {
                    // Compact ChatGPT-inspired header bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard)
                            .border(0.5.dp, SurfaceCardBorder)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (showTopMenuButton) {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.size(48.dp).testTag("bioleaf_drawer_toggle")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open Bio Leaf navigation sidebar",
                                        tint = NeutralDark
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(EcoGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "BIO LEAF AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NeutralDark
                                )
                                Text(
                                    text = activeSection.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EcoGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Crossfade(
                        targetState = activeSection,
                        label = "bioleaf_section_crossfade",
                        modifier = Modifier.weight(1f)
                    ) { section ->
                        when (section) {
                            BioLeafSection.HOME -> BioLeafHomeSection(
                                userName = userProfile?.name,
                                savedReports = savedReports,
                                onNavigateSection = { viewModel.selectSection(it) }
                            )
                            BioLeafSection.PLANT_ID -> BioLeafPlantIdSection(
                                result = plantResult,
                                isAnalyzing = isAnalyzingPlant,
                                errorMessage = plantError,
                                onAnalyzePlant = { viewModel.identifyPlant(it) }
                            )
                            BioLeafSection.DISEASE_SCANNER -> BioLeafDiseaseSection(
                                result = diseaseResult,
                                isAnalyzing = isAnalyzingDisease,
                                errorMessage = diseaseError,
                                onAnalyzeDisease = { viewModel.analyzeDisease(it) }
                            )
                            BioLeafSection.PEST_SCANNER -> BioLeafPestSection(
                                result = pestResult,
                                isAnalyzing = isAnalyzingPest,
                                errorMessage = pestError,
                                onAnalyzePest = { viewModel.identifyPest(it) }
                            )
                            BioLeafSection.SOIL_GUIDANCE -> BioLeafSoilGuidanceSection(
                                result = soilResult,
                                isGenerating = isGeneratingSoil,
                                errorMessage = soilError,
                                onGenerateGuidance = { viewModel.generateSoilGuidance(it) }
                            )
                            BioLeafSection.ORGANIC_KNOWLEDGE -> BioLeafKnowledgeSection()
                            BioLeafSection.AI_ASSISTANT -> BioLeafAssistantSection(
                                messages = chatMessages,
                                isResponding = isChatResponding,
                                errorMessage = chatError,
                                onSendMessage = { msg, bmp -> viewModel.sendChatMessage(msg, bmp) },
                                onClearChat = { viewModel.clearChat() }
                            )
                            BioLeafSection.COMMUNITY -> BioLeafCommunitySection(
                                currentUid = currentUid,
                                currentUserName = userName,
                                posts = communityPosts,
                                savedPostIds = savedPostIds,
                                commentsByPostId = commentsByPostId,
                                isBusy = isPostingCommunity,
                                errorMessage = communityError,
                                onCreatePost = { tag, title, content, bmp, done ->
                                    viewModel.createCommunityPost(userName, tag, title, content, bmp, done)
                                },
                                onObserveComments = { viewModel.observeCommentsForPost(it) },
                                onAddComment = { postId, text -> viewModel.addCommunityComment(postId, userName, text) },
                                onRequestAiInsight = { viewModel.requestAiCommentOnPost(it) },
                                onToggleSavePost = { viewModel.toggleSavePost(it) },
                                onDeletePost = { viewModel.deleteOwnPost(it) },
                                onReportPost = { postId, reason -> viewModel.reportPost(postId, reason) }
                            )
                            BioLeafSection.HISTORY_REPORTS -> BioLeafHistoryReportsSection(
                                reports = savedReports,
                                onDeleteReport = { viewModel.deleteSavedReport(it) }
                            )
                            BioLeafSection.PROFILE -> AccountScreen(
                                userProfile = userProfile,
                                onSignOut = onSignOut,
                                onOpenSettings = onOpenSettings,
                                onOpenArchitecture = onOpenArchitecture
                            )
                        }
                    }
                }
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideTablet = maxWidth >= 840.dp
        if (isWideTablet) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(268.dp),
                        drawerContainerColor = SurfaceCard
                    ) {
                        BioLeafSidebarContent(
                            activeSection = activeSection,
                            userName = userName,
                            onSelectSection = { viewModel.selectSection(it) }
                        )
                    }
                },
                content = contentBody
            )
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(280.dp),
                        drawerContainerColor = SurfaceCard
                    ) {
                        BioLeafSidebarContent(
                            activeSection = activeSection,
                            userName = userName,
                            onSelectSection = { section ->
                                viewModel.selectSection(section)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                },
                content = contentBody
            )
        }
    }
}

@Composable
private fun BioLeafSidebarContent(
    activeSection: BioLeafSection,
    userName: String,
    onSelectSection: (BioLeafSection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeutralDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Eco, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("BIO LEAF AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = NeutralDark)
                Text(userName, style = MaterialTheme.typography.labelSmall, color = NeutralMedium)
            }
        }

        HorizontalDivider(color = SurfaceCardBorder)
        Spacer(modifier = Modifier.height(4.dp))

        BioLeafSection.entries.forEach { section ->
            val selected = section == activeSection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) EcoGreenContainer else Color.Transparent)
                    .clickable { onSelectSection(section) }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .testTag("sidebar_${section.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = sectionIcon(section),
                    contentDescription = section.title,
                    tint = if (selected) EcoGreen else NeutralDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) EcoOnGreenContainer else NeutralDark
                    )
                    Text(
                        text = section.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium
                    )
                }
            }
        }
    }
}

private fun sectionIcon(section: BioLeafSection): ImageVector = when (section) {
    BioLeafSection.HOME -> Icons.Default.Home
    BioLeafSection.PLANT_ID -> Icons.Default.LocalFlorist
    BioLeafSection.DISEASE_SCANNER -> Icons.Default.Healing
    BioLeafSection.PEST_SCANNER -> Icons.Default.BugReport
    BioLeafSection.SOIL_GUIDANCE -> Icons.Default.Landscape
    BioLeafSection.ORGANIC_KNOWLEDGE -> Icons.AutoMirrored.Filled.MenuBook
    BioLeafSection.AI_ASSISTANT -> Icons.AutoMirrored.Filled.Chat
    BioLeafSection.COMMUNITY -> Icons.Default.Forum
    BioLeafSection.HISTORY_REPORTS -> Icons.Default.Description
    BioLeafSection.PROFILE -> Icons.Default.Person
}

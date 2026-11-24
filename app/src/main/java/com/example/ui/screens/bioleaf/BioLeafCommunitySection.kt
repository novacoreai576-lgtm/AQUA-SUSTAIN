package com.example.ui.screens.bioleaf

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BioLeafReportFormatter
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityPost
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenContainer
import com.example.ui.theme.EcoOnGreenContainer
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder

@Composable
fun BioLeafCommunitySection(
    currentUid: String?,
    currentUserName: String,
    posts: List<CommunityPost>,
    savedPostIds: Set<String>,
    commentsByPostId: Map<String, List<CommunityComment>>,
    isBusy: Boolean,
    errorMessage: String?,
    onCreatePost: (cropTag: String, title: String, content: String, bitmap: Bitmap?, onDone: () -> Unit) -> Unit,
    onObserveComments: (String) -> Unit,
    onAddComment: (postId: String, text: String) -> Unit,
    onRequestAiInsight: (CommunityPost) -> Unit,
    onToggleSavePost: (String) -> Unit,
    onDeletePost: (String) -> Unit,
    onReportPost: (postId: String, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showSavedOnly by remember { mutableStateOf(false) }
    var showComposer by remember { mutableStateOf(false) }
    var expandedPostId by remember { mutableStateOf<String?>(null) }
    var reportingPost by remember { mutableStateOf<CommunityPost?>(null) }
    var reportReason by remember { mutableStateOf("") }

    var draftTitle by remember { mutableStateOf("") }
    var draftCropTag by remember { mutableStateOf("") }
    var draftContent by remember { mutableStateOf("") }
    var draftBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            draftBitmap = decodeBitmapFromUri(context, uri)
        }
    }

    if (reportingPost != null) {
        AlertDialog(
            onDismissRequest = { reportingPost = null },
            title = { Text("Report Inappropriate Post", fontWeight = FontWeight.Bold, color = NeutralDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Help keep the farming community safe and accurate.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Reason (spam, misleading, offensive)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reportingPost?.let { onReportPost(it.id, reportReason.ifBlank { "Inappropriate content" }) }
                        reportReason = ""
                        reportingPost = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportingPost = null }) { Text("Cancel") }
            }
        )
    }

    val filteredPosts = remember(posts, savedPostIds, searchQuery, showSavedOnly) {
        posts.filter { post ->
            val matchesSaved = !showSavedOnly || savedPostIds.contains(post.id)
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isBlank() ||
                post.title.lowercase().contains(q) ||
                post.content.lowercase().contains(q) ||
                post.cropTag.lowercase().contains(q)
            matchesSaved && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("bioleaf_community_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Farmer Community",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = NeutralDark
                )
                Text(
                    text = "Share field observations, crop photos, and sustainable solutions",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeutralMedium
                )
            }
            Button(
                onClick = { showComposer = !showComposer },
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp).testTag("community_new_post_button")
            ) {
                Icon(if (showComposer) Icons.Default.Close else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showComposer) "Close" else "Post", fontWeight = FontWeight.Bold)
            }
        }

        if (showComposer) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Create Community Post", fontWeight = FontWeight.Bold, color = NeutralDark)
                    OutlinedTextField(
                        value = draftTitle,
                        onValueChange = { draftTitle = it },
                        label = { Text("Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("community_title_input")
                    )
                    OutlinedTextField(
                        value = draftCropTag,
                        onValueChange = { draftCropTag = it },
                        label = { Text("Crop Tag (e.g., Tomato, Rice)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draftContent,
                        onValueChange = { draftContent = it },
                        label = { Text("Share question or observation *") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("community_content_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (draftBitmap == null) "Add Photo" else "Change Photo", color = NeutralDark)
                        }
                        Button(
                            onClick = {
                                onCreatePost(draftCropTag, draftTitle, draftContent, draftBitmap) {
                                    draftTitle = ""
                                    draftCropTag = ""
                                    draftContent = ""
                                    draftBitmap = null
                                    showComposer = false
                                }
                            },
                            enabled = !isBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
                            modifier = Modifier.testTag("community_publish_button")
                        ) {
                            Text("Publish")
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            BioLeafErrorBanner(message = errorMessage)
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search posts") },
            placeholder = { Text("Search posts by crop or keyword...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("community_search_input")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !showSavedOnly,
                onClick = { showSavedOnly = false },
                label = { Text("All Posts (${posts.size})") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreen, selectedLabelColor = Color.White)
            )
            FilterChip(
                selected = showSavedOnly,
                onClick = { showSavedOnly = true },
                label = { Text("Saved (${savedPostIds.size})") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreen, selectedLabelColor = Color.White)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredPosts.isEmpty()) {
                item {
                    BioLeafSectionCard(
                        title = if (showSavedOnly) "No Saved Posts" else "No Community Posts Yet",
                        accentColor = NeutralDark,
                        modifier = Modifier.testTag("community_empty_state")
                    ) {
                        Text(
                            text = if (showSavedOnly) {
                                "Bookmark any post in the feed to revisit useful agricultural advice here."
                            } else {
                                "No real user posts match this view yet. Tap 'Post' above to start the first discussion."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedium
                        )
                    }
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    val isExpanded = expandedPostId == post.id
                    val isSaved = savedPostIds.contains(post.id)
                    val comments = commentsByPostId[post.id].orEmpty()

                    LaunchedEffect(isExpanded, post.id) {
                        if (isExpanded) onObserveComments(post.id)
                    }

                    CommunityPostCard(
                        post = post,
                        isOwner = post.authorUid == currentUid,
                        isSaved = isSaved,
                        isExpanded = isExpanded,
                        comments = comments,
                        isBusy = isBusy,
                        onToggleSave = { onToggleSavePost(post.id) },
                        onToggleComments = {
                            expandedPostId = if (isExpanded) null else post.id
                        },
                        onAddComment = { commentText -> onAddComment(post.id, commentText) },
                        onRequestAiInsight = { onRequestAiInsight(post) },
                        onDelete = { onDeletePost(post.id) },
                        onReport = { reportingPost = post }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    isOwner: Boolean,
    isSaved: Boolean,
    isExpanded: Boolean,
    comments: List<CommunityComment>,
    isBusy: Boolean,
    onToggleSave: () -> Unit,
    onToggleComments: () -> Unit,
    onAddComment: (String) -> Unit,
    onRequestAiInsight: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit
) {
    var commentDraft by remember { mutableStateOf("") }
    val postBmp = remember(post.imageThumbnailBase64) { decodeBase64Thumbnail(post.imageThumbnailBase64) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = NeutralDark)
                    Text(BioLeafReportFormatter.formatTimestamp(post.timestampEpochMs), style = MaterialTheme.typography.labelSmall, color = NeutralMedium)
                }
                if (post.cropTag.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EcoGreenContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(post.cropTag, style = MaterialTheme.typography.labelSmall, color = EcoOnGreenContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeutralDark)
            Text(post.content, style = MaterialTheme.typography.bodyMedium, color = NeutralDark)

            if (postBmp != null) {
                Image(
                    bitmap = postBmp.asImageBitmap(),
                    contentDescription = "Community post crop photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onToggleComments) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (comments.isNotEmpty()) "Comments (${comments.size})" else "Comments",
                        color = NeutralDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row {
                    IconButton(onClick = onToggleSave, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove bookmark" else "Save post",
                            tint = if (isSaved) EcoGreen else NeutralMedium
                        )
                    }
                    if (isOwner) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete post", tint = AlertRed)
                        }
                    } else {
                        IconButton(onClick = onReport, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Flag, contentDescription = "Report post", tint = NeutralMedium)
                        }
                    }
                }
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRequestAiInsight,
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = EcoGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Get BIO LEAF AI Insight Comment", color = EcoGreen, fontWeight = FontWeight.Bold)
                    }

                    if (comments.isEmpty()) {
                        Text("No comments yet.", style = MaterialTheme.typography.bodySmall, color = NeutralMedium)
                    } else {
                        comments.forEach { comment ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (comment.isAiAssisted) EcoGreenContainer.copy(alpha = 0.55f) else Color.White)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (comment.isAiAssisted) EcoOnGreenContainer else NeutralDark
                                )
                                Text(comment.text, style = MaterialTheme.typography.bodySmall, color = NeutralDark)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = { commentDraft = it },
                            placeholder = { Text("Write a comment...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (commentDraft.isNotBlank()) {
                                    onAddComment(commentDraft)
                                    commentDraft = ""
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post comment", tint = EcoGreen)
                        }
                    }
                }
            }
        }
    }
}

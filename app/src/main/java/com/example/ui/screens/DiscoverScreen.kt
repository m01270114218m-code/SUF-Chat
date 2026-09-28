package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.models.DiscoverMomentPost
import com.example.models.GiftCatalogItem

@Composable
fun DiscoverScreen(
    posts: List<DiscoverMomentPost>,
    giftsCatalog: List<GiftCatalogItem>,
    onToggleLike: (String) -> Unit,
    onAddComment: (String, String) -> Unit,
    onPublishPost: (String, String) -> Unit,
    onSendGiftToAuthor: (GiftCatalogItem, String) -> Unit,
    onPublishPostWithImage: ((String, String, String?) -> Unit)? = null,
    onInspectAuthorProfile: (DiscoverMomentPost) -> Unit = {}
) {
    // 0 = اللحظات (Moments), 1 = الأصدقاء (Friends)
    var selectedSubTab by rememberSaveable { mutableIntStateOf(0) }
    var showCreatePostDialog by rememberSaveable { mutableStateOf(false) }
    var newPostContent by rememberSaveable { mutableStateOf("") }
    var selectedNewPostImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var commentingPost by remember { mutableStateOf<DiscoverMomentPost?>(null) }
    var commentDraft by rememberSaveable { mutableStateOf("") }
    var showNotificationsSheet by rememberSaveable { mutableStateOf(false) }

    val customPostImages = remember { mutableStateMapOf<String, String>() }

    val postImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedNewPostImageUri = uri.toString()
        }
    }

    // Ensure the top post matches Screenshot 1 ("مـيـار", "هاي بنات شباب اني جديده بل برنامج ماكو ترحيب", "2025-07-04 06:57", 65 likes, 22 comments)
    val displayPosts = remember(posts, selectedSubTab) {
        val screenshotFeaturedPost = DiscoverMomentPost(
            id = "mayar_featured",
            authorName = "مـيـار",
            authorId = "8829410",
            authorAvatarType = "PRINCESS",
            authorVipTier = 3,
            authorWealthLevel = 18,
            timeAgoAr = "2025-07-04 06:57",
            contentAr = "هاي بنات شباب اني جديده بل برنامج ماكو ترحيب",
            attachedRoomTitle = "غرفة ميار",
            likesCount = 65,
            commentsCount = 22,
            isLikedByMe = false
        )
        val merged = listOf(screenshotFeaturedPost) + posts.filter { it.authorName != "مـيـار" }
        if (selectedSubTab == 1) merged.filterIndexed { index, _ -> index % 2 == 0 } else merged
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // =========================================================================
                // 1. TOP HEADER BAR (Matches Screenshot 1: Bell on left, "الأصدقاء" & "اللحظات" on right)
                // =========================================================================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Left: Purple ringing bell icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { showNotificationsSheet = true }
                            .testTag("discover_notifications_bell")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = "الإشعارات",
                            tint = Color(0xFFB358F7),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Right: "الأصدقاء" and "اللحظات" tabs
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        // Unselected/Selected "الأصدقاء"
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedSubTab = 1 }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "الأصدقاء",
                                color = if (selectedSubTab == 1) Color(0xFF1C1C28) else Color(0xFF8B90A0),
                                fontSize = if (selectedSubTab == 1) 20.sp else 15.sp,
                                fontWeight = if (selectedSubTab == 1) FontWeight.ExtraBold else FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (selectedSubTab == 1) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(3.5.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFB358F7), Color(0xFFEC4899))
                                            )
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.height(3.5.dp))
                            }
                        }

                        // Selected "اللحظات"
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedSubTab = 0 }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "اللحظات",
                                color = if (selectedSubTab == 0) Color(0xFF1C1C28) else Color(0xFF8B90A0),
                                fontSize = if (selectedSubTab == 0) 20.sp else 15.sp,
                                fontWeight = if (selectedSubTab == 0) FontWeight.ExtraBold else FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (selectedSubTab == 0) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(3.5.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFB358F7), Color(0xFFEC4899))
                                            )
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.height(3.5.dp))
                            }
                        }
                    }
                }

                // =========================================================================
                // 2. MOMENTS FEED LIST (Matches Screenshot 1)
                // =========================================================================
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(displayPosts, key = { _, post -> post.id }) { index, post ->
                        MomentFeedItemCard(
                            post = post,
                            index = index,
                            customImageUri = post.postImageUri?.takeIf { it.isNotBlank() } ?: customPostImages[post.id],
                            onAuthorClick = { onInspectAuthorProfile(post) },
                            onLikeClick = { onToggleLike(post.id) },
                            onCommentClick = { commentingPost = post },
                            onShareClick = { onAddComment(post.id, "تمت مشاركة اللحظة") }
                        )
                        HorizontalDivider(
                            color = Color(0xFFF3F4F8),
                            thickness = 6.dp
                        )
                    }
                }
            }

            // =========================================================================
            // 3. FLOATING PURPLE-PINK "+" BUTTON ON BOTTOM LEFT (Matches Screenshot 1)
            // =========================================================================
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 24.dp)
                    .size(56.dp)
                    .shadow(10.dp, CircleShape, spotColor = Color(0xFFB358F7))
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFA855F7),
                                Color(0xFFD946EF),
                                Color(0xFFEC4899)
                            )
                        )
                    )
                    .clickable { showCreatePostDialog = true }
                    .testTag("publish_moment_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "نشر لحظة جديدة",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Create Moment Dialog
            if (showCreatePostDialog) {
                AlertDialog(
                    onDismissRequest = { showCreatePostDialog = false },
                    containerColor = Color.White,
                    title = {
                        Text(
                            text = "نشر لحظة جديدة",
                            color = Color(0xFF1C1C28),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = newPostContent,
                                onValueChange = { newPostContent = it },
                                label = { Text("اكتب لحظتك هنا...") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF5F0FF))
                                    .border(1.dp, Color(0xFFB358F7), RoundedCornerShape(12.dp))
                                    .clickable {
                                        postImagePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFFB358F7),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedNewPostImageUri != null) "تم اختيار صورة من الجهاز ✓" else "اختيار صورة من الجهاز",
                                    color = Color(0xFF7E22CE),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newPostContent.isNotBlank()) {
                                    if (onPublishPostWithImage != null) {
                                        onPublishPostWithImage(newPostContent, "", selectedNewPostImageUri)
                                    } else {
                                        onPublishPost(newPostContent, "")
                                    }
                                    newPostContent = ""
                                    selectedNewPostImageUri = null
                                    showCreatePostDialog = false
                                }
                            }
                        ) {
                            Text("نشر", color = Color(0xFFB358F7), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreatePostDialog = false }) {
                            Text("إلغاء", color = Color(0xFF8B90A0))
                        }
                    }
                )
            }

            // Comment Dialog
            commentingPost?.let { targetPost ->
                AlertDialog(
                    onDismissRequest = { commentingPost = null },
                    containerColor = Color.White,
                    title = {
                        Text(
                            text = "إضافة تعليق على ${targetPost.authorName}",
                            color = Color(0xFF1C1C28),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    },
                    text = {
                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = { commentDraft = it },
                            label = { Text("اكتب تعليقك...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (commentDraft.isNotBlank()) {
                                    onAddComment(targetPost.id, commentDraft)
                                    commentDraft = ""
                                    commentingPost = null
                                }
                            }
                        ) {
                            Text("إرسال", color = Color(0xFFB358F7), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { commentingPost = null }) {
                            Text("إلغاء", color = Color(0xFF8B90A0))
                        }
                    }
                )
            }

            if (showNotificationsSheet) {
                AlertDialog(
                    onDismissRequest = { showNotificationsSheet = false },
                    containerColor = Color.White,
                    title = {
                        Text(
                            text = "إشعارات اللحظات",
                            color = Color(0xFF1C1C28),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    },
                    text = {
                        Text(
                            text = "لا توجد إشعارات جديدة حالياً.",
                            color = Color(0xFF6E7485),
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showNotificationsSheet = false }) {
                            Text("حسناً", color = Color(0xFFB358F7), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MomentFeedItemCard(
    post: DiscoverMomentPost,
    index: Int,
    customImageUri: String?,
    onAuthorClick: () -> Unit = {},
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 1. Top User Row: Left = "..." menu, Right = Name + Level/Gender Badge + Real Circular Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left: Three horizontal dots
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "المزيد",
                tint = Color(0xFFA0A5B5),
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onShareClick() }
            )

            // Right: Name & Level/Gender badge + Real Account Avatar (Tappable to open Account Profile)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable { onAuthorClick() }
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = post.authorName,
                        color = Color(0xFF222230),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (post.authorLevel > 0) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFC084FC), Color(0xFF9333EA))
                                        )
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = "LV.${post.authorLevel} 💎",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        if (post.authorAvatarType == "PRINCESS") {
                                            listOf(Color(0xFFFF6B9D), Color(0xFFFF477E))
                                        } else {
                                            listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                        }
                                    )
                                )
                                .padding(horizontal = 7.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = if (post.authorAvatarType == "PRINCESS") "♀ 18" else "♂ 24",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Circular Avatar on the far right (Displays real account photo set by account owner)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAE4F2))
                ) {
                    if (!post.authorCustomAvatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = Uri.parse(post.authorCustomAvatarUri),
                            contentDescription = post.authorName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        PortraitMomentAvatarCanvas(index = index)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Post Text (Right-aligned)
        Text(
            text = post.contentAr,
            color = Color(0xFF1F1F2E),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Large Rounded Post Image (Right-aligned like Screenshot 1)
        Box(
            modifier = Modifier
                .width(235.dp)
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEDE8F5))
        ) {
            if (!customImageUri.isNullOrBlank()) {
                AsyncImage(
                    model = Uri.parse(customImageUri),
                    contentDescription = post.contentAr,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CatInHandsArtworkCanvas(index = index)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Timestamp Row (Right-aligned)
        Text(
            text = post.timeAgoAr,
            color = Color(0xFFA6AAB8),
            fontSize = 12.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Bottom Action Row: Share icon on far left, Comment + Like on the right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left: Share icon
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "مشاركة",
                tint = Color(0xFF8E92A4),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onShareClick() }
            )

            // Right: Comment count + Like count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Comment button + count (22)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .clickable { onCommentClick() }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = post.commentsCount.toString(),
                        color = Color(0xFF8E92A4),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "تعليقات",
                        tint = Color(0xFF8E92A4),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Like button + count (65)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .clickable { onLikeClick() }
                        .padding(vertical = 4.dp)
                        .testTag("moment_like_button_${post.id}")
                ) {
                    Text(
                        text = post.likesCount.toString(),
                        color = if (post.isLikedByMe) Color(0xFFFF477E) else Color(0xFF8E92A4),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "إعجاب",
                        tint = if (post.isLikedByMe) Color(0xFFFF477E) else Color(0xFF8E92A4),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PortraitMomentAvatarCanvas(index: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.verticalGradient(
                if (index == 0) listOf(Color(0xFF3A3542), Color(0xFF1E1B24))
                else listOf(Color(0xFF6A4C93), Color(0xFF2B193D))
            )
        )
        // Soft portrait silhouette matching Screenshot 1 avatar
        drawCircle(
            color = Color(0xFFF3D6C6),
            radius = w * 0.24f,
            center = Offset(w * 0.5f, h * 0.40f)
        )
        drawCircle(
            color = Color(0xFF2B2630),
            radius = w * 0.34f,
            center = Offset(w * 0.5f, h * 0.86f)
        )
    }
}

@Composable
private fun CatInHandsArtworkCanvas(index: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Soft warm grey/taupe background matching Screenshot 1's photo of a cute kitten held in hands
        drawRect(
            brush = Brush.verticalGradient(
                colors = if (index == 0) {
                    listOf(
                        Color(0xFFDCD8D5),
                        Color(0xFFB8B2AE),
                        Color(0xFF918984)
                    )
                } else {
                    listOf(
                        Color(0xFFE6DADA),
                        Color(0xFFC9B6BE),
                        Color(0xFF9A8490)
                    )
                }
            )
        )

        // Subtle bokeh background circles
        drawCircle(
            color = Color.White.copy(alpha = 0.22f),
            radius = w * 0.35f,
            center = Offset(w * 0.25f, h * 0.2f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.16f),
            radius = w * 0.28f,
            center = Offset(w * 0.78f, h * 0.28f)
        )

        // Kitten ears
        val leftEar = Path().apply {
            moveTo(w * 0.26f, h * 0.42f)
            lineTo(w * 0.19f, h * 0.18f)
            lineTo(w * 0.41f, h * 0.30f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(w * 0.74f, h * 0.42f)
            lineTo(w * 0.81f, h * 0.18f)
            lineTo(w * 0.59f, h * 0.30f)
            close()
        }
        drawPath(leftEar, color = Color(0xFFB5ABA4))
        drawPath(rightEar, color = Color(0xFFB5ABA4))

        // Inner pink ears
        val leftInnerEar = Path().apply {
            moveTo(w * 0.28f, h * 0.39f)
            lineTo(w * 0.23f, h * 0.23f)
            lineTo(w * 0.38f, h * 0.32f)
            close()
        }
        val rightInnerEar = Path().apply {
            moveTo(w * 0.72f, h * 0.39f)
            lineTo(w * 0.77f, h * 0.23f)
            lineTo(w * 0.62f, h * 0.32f)
            close()
        }
        drawPath(leftInnerEar, color = Color(0xFFE8B4B8))
        drawPath(rightInnerEar, color = Color(0xFFE8B4B8))

        // Kitten body
        drawCircle(
            color = Color(0xFFDAD4CE),
            radius = w * 0.31f,
            center = Offset(w * 0.50f, h * 0.72f)
        )

        // Kitten round fluffy head
        drawCircle(
            color = Color(0xFFE5E0DC),
            radius = w * 0.29f,
            center = Offset(w * 0.50f, h * 0.47f)
        )

        // Forehead tabby stripes
        drawLine(
            color = Color(0xFF8C827A),
            start = Offset(w * 0.50f, h * 0.27f),
            end = Offset(w * 0.50f, h * 0.36f),
            strokeWidth = 5f
        )
        drawLine(
            color = Color(0xFF8C827A),
            start = Offset(w * 0.43f, h * 0.29f),
            end = Offset(w * 0.45f, h * 0.36f),
            strokeWidth = 4f
        )
        drawLine(
            color = Color(0xFF8C827A),
            start = Offset(w * 0.57f, h * 0.29f),
            end = Offset(w * 0.55f, h * 0.36f),
            strokeWidth = 4f
        )

        // Big expressive dark eyes with white catchlights
        drawCircle(
            color = Color(0xFF2B2623),
            radius = w * 0.065f,
            center = Offset(w * 0.38f, h * 0.46f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.022f,
            center = Offset(w * 0.365f, h * 0.445f)
        )

        drawCircle(
            color = Color(0xFF2B2623),
            radius = w * 0.065f,
            center = Offset(w * 0.62f, h * 0.46f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.022f,
            center = Offset(w * 0.605f, h * 0.445f)
        )

        // Cute pink nose
        val nosePath = Path().apply {
            moveTo(w * 0.475f, h * 0.51f)
            lineTo(w * 0.525f, h * 0.51f)
            lineTo(w * 0.50f, h * 0.535f)
            close()
        }
        drawPath(nosePath, color = Color(0xFFE58F96))

        // Paws in front
        drawCircle(
            color = Color(0xFFF2EFE9),
            radius = w * 0.075f,
            center = Offset(w * 0.41f, h * 0.65f)
        )
        drawCircle(
            color = Color(0xFFF2EFE9),
            radius = w * 0.075f,
            center = Offset(w * 0.59f, h * 0.65f)
        )

        // Warm hands gently holding the kitten at the bottom left & right
        drawCircle(
            color = Color(0xFFD9AE94),
            radius = w * 0.18f,
            center = Offset(w * 0.25f, h * 0.67f)
        )
        drawCircle(
            color = Color(0xFFD9AE94),
            radius = w * 0.18f,
            center = Offset(w * 0.75f, h * 0.67f)
        )

        // Soft vignette border
        drawRect(
            color = Color.Black.copy(alpha = 0.05f),
            style = Stroke(width = 2f)
        )
    }
}

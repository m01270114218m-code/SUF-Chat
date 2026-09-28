package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.models.StandaloneSubScreen
import com.example.models.UserProfile
import com.example.ui.components.Ornate3DAvatarWithFrame

/**
 * Unified Account Detail Page (`الصفحة الرئيسية للحسابات من الداخل`):
 * 100% Unified with the 4 Main Outer Screens (`HomeLobbyScreen`, `DiscoverScreen`, `MessagesScreen`, `ProfileScreen`):
 * - Soft Lavender-Purple & Pearl-White surface (`#F9F7FD`, `#EDE0FC`)
 * - Crisp White 3D Cards with soft Lavender borders (`#E9D5FF`) and Royal Violet/Pink accents (`#8B5CF6`, `#EC4899`)
 * - Supports inspecting any member's profile from a voice room or managing one's own account.
 */
@Composable
fun StandaloneInnerAccountDetailScreen(
    accountProfile: UserProfile,
    isMyOwnAccount: Boolean = true,
    onPickAvatarFromDeviceStorage: (String) -> Unit = {},
    onPickCoverFromDeviceStorage: (String) -> Unit = {},
    onCopyIdNotice: (String) -> Unit,
    onOpenSubScreen: (StandaloneSubScreen) -> Unit,
    onPreviewEntryMount: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val clipboardManager = LocalClipboardManager.current
    var selectedSectionTab by remember { mutableIntStateOf(0) } // 0 = بيانات الحساب, 1 = الإكسسوارات والصيغ, 2 = الشارات والهدايا
    var isFollowingOtherUser by remember { mutableStateOf(false) }

    val avatarPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onPickAvatarFromDeviceStorage(uri.toString())
    }

    val coverPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onPickCoverFromDeviceStorage(uri.toString())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F7FD))
    ) {
        // Unified Soft Lavender-Purple Top Backdrop matching the 4 Main Screens
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE5D1FA),
                            Color(0xFFF2E8FD),
                            Color(0xFFF9F7FD)
                        )
                    )
                )
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Unified Top Header Bar + Hero Cover Card
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Action Bar matching the 4 Main Screens
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .shadow(4.dp, CircleShape, spotColor = Color(0xFF9333EA).copy(alpha = 0.25f))
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.2.dp, Color(0xFFD8B4FE), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = Color(0xFF6B21A8)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isMyOwnAccount) "ملفي الشخصي الملكي 👤" else "بطاقة ملف ${accountProfile.nickname}",
                                    color = Color(0xFF1E1B4B),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "تصميم موحد مطابق للواجهات الرئيسية الأربع ✨",
                                    color = Color(0xFF7C3AED),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (isMyOwnAccount) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFD8B4FE), RoundedCornerShape(50))
                                        .clickable {
                                            coverPhotoPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "تغيير الغلاف",
                                            tint = Color(0xFF7C3AED),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "الغلاف",
                                            color = Color(0xFF6B21A8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                            )
                                        )
                                        .clickable { onOpenSubScreen(StandaloneSubScreen.ACCOUNT_SETTINGS) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "تعديل",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "تعديل",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        } else {
                            // Follow Button when inspecting another user's profile
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isFollowingOtherUser) {
                                            Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
                                        }
                                    )
                                    .clickable { isFollowingOtherUser = !isFollowingOtherUser }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = if (isFollowingOtherUser) "✓ متابَع" else "➕ متابعة",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Unified White & Pastel-Lavender Profile Hero Card matching ProfileScreen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF8B5CF6).copy(alpha = 0.2f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFFE9D5FF), RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Top Cover Banner inside Hero Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(112.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF8B5CF6),
                                                Color(0xFFA855F7),
                                                Color(0xFFEC4899)
                                            )
                                        )
                                    )
                            ) {
                                if (!accountProfile.customCoverUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = Uri.parse(accountProfile.customCoverUri),
                                        contentDescription = "Cover",
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.72f,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_live_party_banner),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.28f,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.16f),
                                        radius = size.height * 0.7f,
                                        center = Offset(size.width * 0.85f, size.height * 0.25f)
                                    )
                                }

                                // Country & Special ID Tag in top corner
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White.copy(alpha = 0.90f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = accountProfile.countryFlag, fontSize = 13.sp)
                                    Text(
                                        text = accountProfile.countryNameAr,
                                        color = Color(0xFF1E1B4B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            // Overlapping Circular Avatar with Frame (supports all formats)
                            Box(
                                contentAlignment = Alignment.BottomEnd,
                                modifier = Modifier.offset(y = (-42).dp)
                            ) {
                                Ornate3DAvatarWithFrame(
                                    avatarType = accountProfile.avatarType,
                                    frameStyle = accountProfile.frameStyle,
                                    size = 90.dp,
                                    isSpeaking = true,
                                    showCrown = true,
                                    customImageUri = accountProfile.customAvatarUri,
                                    onClick = {
                                        if (isMyOwnAccount) {
                                            avatarPhotoPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    }
                                )
                                if (isMyOwnAccount) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF7C3AED))
                                            .border(2.dp, Color.White, CircleShape)
                                            .clickable {
                                                avatarPhotoPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "تغيير الصورة",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            // User Name, Verified Badge, Copyable ID, and Role / Level Pills
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .offset(y = (-30).dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = accountProfile.nickname,
                                        color = Color(0xFF1E1B4B),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "موثق",
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Copyable ID Pill matching ProfileScreen
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFF5EEFE))
                                        .border(1.dp, Color(0xFFD8B4FE), RoundedCornerShape(50))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(accountProfile.displayId))
                                            onCopyIdNotice(accountProfile.displayId)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "👑 ID: ${accountProfile.displayId}",
                                        color = Color(0xFF6B21A8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "نسخ",
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Badges Row: VIP Tier, Wealth Level, Charisma Level
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UnifiedProfilePillBadge(
                                        text = "👑 VIP ${accountProfile.vipTier}",
                                        bgStart = Color(0xFFF59E0B),
                                        bgEnd = Color(0xFFD97706)
                                    )
                                    UnifiedProfilePillBadge(
                                        text = "⚜️ ثروة Lv.${accountProfile.wealthLevel}",
                                        bgStart = Color(0xFF8B5CF6),
                                        bgEnd = Color(0xFF6D28D9)
                                    )
                                    UnifiedProfilePillBadge(
                                        text = "💖 جاذبية Lv.${accountProfile.charismaLevel}",
                                        bgStart = Color(0xFFEC4899),
                                        bgEnd = Color(0xFFDB2777)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = accountProfile.bioTextAr,
                                    color = Color(0xFF4B5563),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // 4 Social Stats Row matching ProfileScreen
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFF9F7FD))
                                        .border(1.dp, Color(0xFFEDE9FE), RoundedCornerShape(16.dp))
                                        .padding(vertical = 10.dp)
                                ) {
                                    UnifiedStatColumn("${accountProfile.friendsCount}", "الأصدقاء")
                                    UnifiedVerticalDivider()
                                    UnifiedStatColumn("${accountProfile.followingCount}", "المتابَعون")
                                    UnifiedVerticalDivider()
                                    UnifiedStatColumn("${accountProfile.fansCount}", "المعجبون")
                                    UnifiedVerticalDivider()
                                    UnifiedStatColumn("${accountProfile.visitorsCount}", "الزوار")
                                }
                            }
                        }
                    }
                }
            }

            // 2. Unified Section Tabs (`بيانات الحساب` | `الإطارات والدخوليات` | `الشارات والهدايا`)
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    listOf(
                        0 to "📊 بيانات ومستويات",
                        1 to "⭕ الإطارات والدخوليات",
                        2 to "🏅 الشارات والهدايا"
                    ).forEach { (idx, label) ->
                        val selected = selectedSectionTab == idx
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) {
                                        Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
                                    } else {
                                        Brush.horizontalGradient(listOf(Color.White, Color.White))
                                    }
                                )
                                .border(
                                    width = 1.2.dp,
                                    color = if (selected) Color.Transparent else Color(0xFFE9D5FF),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedSectionTab = idx }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color.White else Color(0xFF4C1D95),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 3. Tab Content Cards (All in Crisp White & Lavender 3D Card Style matching the 4 Main Screens)
            when (selectedSectionTab) {
                0 -> {
                    // Wealth & Charisma Progression Card
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF8B5CF6).copy(alpha = 0.15f))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.2.dp, Color(0xFFE9D5FF), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "⚜️ مستويات الحساب والخبرة التفاعلية",
                                color = Color(0xFF1E1B4B),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )

                            // Wealth Level Progress
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "👑 مستوى الثروة (Lv.${accountProfile.wealthLevel})",
                                        color = Color(0xFF6B21A8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${accountProfile.wealthXpCurrent} / ${accountProfile.wealthXpTarget} XP",
                                        color = Color(0xFF7C3AED),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = {
                                        (accountProfile.wealthXpCurrent.toFloat() / accountProfile.wealthXpTarget.toFloat())
                                            .coerceIn(0f, 1f)
                                    },
                                    color = Color(0xFF8B5CF6),
                                    trackColor = Color(0xFFF3E8FF),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(9.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            }

                            // Charisma Level Progress
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "💖 مستوى الجاذبية (Lv.${accountProfile.charismaLevel})",
                                        color = Color(0xFFBE185D),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${accountProfile.charismaXpCurrent} / ${accountProfile.charismaXpTarget} XP",
                                        color = Color(0xFFEC4899),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = {
                                        (accountProfile.charismaXpCurrent.toFloat() / accountProfile.charismaXpTarget.toFloat())
                                            .coerceIn(0f, 1f)
                                    },
                                    color = Color(0xFFEC4899),
                                    trackColor = Color(0xFFFCE7F3),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(9.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            }
                        }
                    }

                    // CP Partner & Royal Clan Card
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                        ) {
                            // CP Partner Card
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFEC4899).copy(alpha = 0.18f))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.verticalGradient(listOf(Color.White, Color(0xFFFDF2F8)))
                                    )
                                    .border(1.2.dp, Color(0xFFFBCFE8), RoundedCornerShape(20.dp))
                                    .clickable { onOpenSubScreen(StandaloneSubScreen.CP_RELATIONSHIPS) }
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "💍 شريك الـ CP الملكي",
                                    color = Color(0xFFBE185D),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = accountProfile.cpPartnerName,
                                    color = Color(0xFF1E1B4B),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "نقاط الألفة: ${accountProfile.cpIntimacyScore} 💖",
                                    color = Color(0xFFDB2777),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Family Clan Card
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF8B5CF6).copy(alpha = 0.18f))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.verticalGradient(listOf(Color.White, Color(0xFFF5F3FF)))
                                    )
                                    .border(1.2.dp, Color(0xFFDDD6FE), RoundedCornerShape(20.dp))
                                    .clickable { onOpenSubScreen(StandaloneSubScreen.FAMILY_CLAN) }
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "🦁 القبيلة والعائلة",
                                    color = Color(0xFF6D28D9),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = accountProfile.clanNameAr,
                                    color = Color(0xFF1E1B4B),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "الرتبة: ${accountProfile.clanRoleAr}",
                                    color = Color(0xFF7C3AED),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Universal Format Frames & Entry Mounts Showcase
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF8B5CF6).copy(alpha = 0.15f))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.2.dp, Color(0xFFE9D5FF), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "⭕ الإطار والدخولية المُفعّلة (يدعم جميع الصيغ المتحركة والثابتة)",
                                color = Color(0xFF1E1B4B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "الصيغ المدعومة: SVGA • GIF • WebP • PNG • MP4 • 7D Vector",
                                color = Color(0xFF7C3AED),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Equipped Circular Frame Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF9F7FD))
                                    .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Ornate3DAvatarWithFrame(
                                        avatarType = accountProfile.avatarType,
                                        frameStyle = accountProfile.frameStyle,
                                        size = 58.dp,
                                        isSpeaking = true,
                                        showCrown = true,
                                        customImageUri = accountProfile.customAvatarUri
                                    )
                                    Column {
                                        Text(
                                            text = accountProfile.frameStyle.nameAr,
                                            color = Color(0xFF1E1B4B),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "إطار مايك وصورة شخصية (متحرك + ثابت)",
                                            color = Color(0xFF6B21A8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                if (isMyOwnAccount) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFEDE9FE))
                                            .clickable { onOpenSubScreen(StandaloneSubScreen.BAG_WARDROBE) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "تغيير 🎒",
                                            color = Color(0xFF6B21A8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            // Equipped Moving Entry Mount Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF9F7FD))
                                    .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color(0xFFF3E8FF), Color(0xFFD8B4FE))
                                                )
                                            )
                                            .border(1.5.dp, Color(0xFF8B5CF6), CircleShape)
                                    ) {
                                        Text(text = "🏎️", fontSize = 26.sp)
                                    }
                                    Column {
                                        Text(
                                            text = accountProfile.entryWelcomeName.ifBlank { "تنين الإمبراطور الذهبي 7D" },
                                            color = Color(0xFF1E1B4B),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "دخولية متحركة تظهر وتتحرك عند دخول الغرفة",
                                            color = Color(0xFF6B21A8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
                                        )
                                        .clickable {
                                            onPreviewEntryMount(
                                                accountProfile.entryWelcomeName.ifBlank { "تنين الإمبراطور الذهبي 7D" }
                                            )
                                        }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircleFilled,
                                            contentDescription = "معاينة",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "معاينة الحركة",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            // Chat Bubble Frame Status Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF9F7FD))
                                    .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (accountProfile.equippedChatBubbleName.isBlank()) {
                                            "💬 الكتابة في الغرف: شفافة بدون إطار (الوضع الافتراضي)"
                                        } else {
                                            "💬 إطار الدردشة المُفعّل: ${accountProfile.equippedChatBubbleName}"
                                        },
                                        color = Color(0xFF1E1B4B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "يمكن شراء وتفعيل إطار يضاعف قاعة الدردشة من المتجر الملكي",
                                        color = Color(0xFF7C3AED),
                                        fontSize = 10.sp
                                    )
                                }
                                if (isMyOwnAccount) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFEDE9FE))
                                            .clickable { onOpenSubScreen(StandaloneSubScreen.STORE) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "المتجر 🛍️",
                                            color = Color(0xFF6B21A8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Badges & Gift Showcase Card
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF8B5CF6).copy(alpha = 0.15f))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.2.dp, Color(0xFFE9D5FF), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "🏅 خزانة الشارات والأوسمة الملكية (${accountProfile.badges.size})",
                                color = Color(0xFF1E1B4B),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            accountProfile.badges.forEach { badge ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF9F7FD))
                                        .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(14.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = badge.iconEmoji, fontSize = 24.sp)
                                        Column {
                                            Text(
                                                text = badge.titleAr,
                                                color = Color(0xFF1E1B4B),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = badge.categoryAr,
                                                color = Color(0xFF7C3AED),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (badge.isUnlocked) "مُفعّل ✓" else "مغلق 🔒",
                                        color = if (badge.isUnlocked) Color(0xFF10B981) else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnifiedProfilePillBadge(
    text: String,
    bgStart: Color,
    bgEnd: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(bgStart, bgEnd)))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun UnifiedStatColumn(
    valueText: String,
    labelAr: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valueText,
            color = Color(0xFF1E1B4B),
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = labelAr,
            color = Color(0xFF6B21A8),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun UnifiedVerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(26.dp)
            .background(Color(0xFFE9D5FF))
    )
}

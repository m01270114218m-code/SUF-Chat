package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.models.AvatarFrameStyle
import com.example.models.StandaloneSubScreen
import com.example.models.UserProfile
import com.example.viewmodel.AgencyAuthorizationUiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    agencyAuthState: AgencyAuthorizationUiState = AgencyAuthorizationUiState(),
    onOpenSubScreen: (StandaloneSubScreen) -> Unit,
    onOpenInnerAccountDetails: () -> Unit = { onOpenSubScreen(StandaloneSubScreen.INNER_ACCOUNT_DETAIL) },
    onPickAccountPhotoFromStorage: (String) -> Unit = {},
    onCopyIdNotice: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickAccountPhotoFromStorage(uri.toString())
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F9))
        ) {
            // Soft lavender-purple top header gradient matching Screenshot 3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE7D6FA),
                                Color(0xFFF2EAFC),
                                Color(0xFFF6F6F9)
                            )
                        )
                    )
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 100.dp,
                    start = 14.dp,
                    end = 14.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // =====================================================================
                // 1. TOP BAR: Left = Settings & Edit Icons (Matches Screenshot 3)
                // =====================================================================
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "الإعدادات",
                                tint = Color(0xFF232533),
                                modifier = Modifier
                                    .size(25.dp)
                                    .clickable { onOpenSubScreen(StandaloneSubScreen.ACCOUNT_SETTINGS) }
                                    .testTag("open_settings_button")
                            )

                            Icon(
                                imageVector = Icons.Outlined.DriveFileRenameOutline,
                                contentDescription = "تعديل الملف الشخصي وتفاصيل الحساب",
                                tint = Color(0xFF232533),
                                modifier = Modifier
                                    .size(25.dp)
                                    .clickable { onOpenInnerAccountDetails() }
                                    .testTag("open_inner_account_button")
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                // =====================================================================
                // 2. USER PROFILE HEADER ROW (Matches Screenshot 3: Sayed, Flag, Badges, ID:2261830, Avatar on Right)
                //    - Double-tap on photo opens Inner Account Detail Screen
                //    - Camera button picks photo from device storage
                //    - Store outer frame wraps avatar if purchased/equipped
                //    - NO VIP mark on the avatar itself
                // =====================================================================
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        // Left of Avatar: User Name, Badges Row, ID Row
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 14.dp)
                        ) {
                            Text(
                                text = userProfile.nickname.ifBlank { "محمد" },
                                color = Color(0xFF1C1C28),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.clickable {
                                    onOpenSubScreen(StandaloneSubScreen.ACCOUNT_SETTINGS)
                                }
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Badges Row: Golden Charm, Purple Wealth, Gender/Age, Country Flag (Synchronized with userProfile)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Golden Charm Pill
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                                            )
                                        )
                                        .clickable { onOpenSubScreen(StandaloneSubScreen.LEVELS) }
                                        .padding(horizontal = 7.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = "${userProfile.charismaLevel} ✨",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Purple Wealth Pill
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFC084FC), Color(0xFF9333EA))
                                            )
                                        )
                                        .clickable { onOpenSubScreen(StandaloneSubScreen.LEVELS) }
                                        .padding(horizontal = 7.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = "${userProfile.wealthLevel} 💎",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Blue Gender/Age Pill
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFF38BDF8))
                                        .padding(horizontal = 7.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = if (userProfile.avatarType == "PRINCESS") "♀ 24" else "♂ 27",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Country Flag
                                Text(
                                    text = userProfile.countryFlag.ifBlank { "🇪🇬" },
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // ID Row with Copy Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(userProfile.displayId))
                                        onCopyIdNotice()
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "نسخ ID",
                                    tint = Color(0xFF7B8092),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "ID:${userProfile.displayId}",
                                    color = Color(0xFF6E7385),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Far Right: Circular Avatar (Double-tap opens Inner Account Details, Camera badge picks from Device Storage)
                        Box(contentAlignment = Alignment.BottomEnd) {
                            val hasPurchasedOuterFrame = userProfile.frameStyle != AvatarFrameStyle.GOLD_ROYAL
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(78.dp)
                                    .then(
                                        if (hasPurchasedOuterFrame) {
                                            Modifier
                                                .border(
                                                    width = 3.dp,
                                                    brush = Brush.sweepGradient(
                                                        listOf(
                                                            userProfile.frameStyle.primaryColor,
                                                            userProfile.frameStyle.secondaryColor,
                                                            Color(0xFFFFD700),
                                                            userProfile.frameStyle.primaryColor
                                                        )
                                                    ),
                                                    shape = CircleShape
                                                )
                                                .padding(4.dp)
                                        } else {
                                            Modifier.border(2.dp, Color.White, CircleShape)
                                        }
                                    )
                                    .clip(CircleShape)
                                    .background(Color(0xFF1F1C24))
                                    .combinedClickable(
                                        onClick = { onOpenInnerAccountDetails() },
                                        onDoubleClick = { onOpenInnerAccountDetails() }
                                    )
                                    .testTag("profile_avatar_double_tap")
                            ) {
                                if (!userProfile.customAvatarUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = Uri.parse(userProfile.customAvatarUri),
                                        contentDescription = "صورة الحساب",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(
                                            id = if (userProfile.avatarType == "PRINCESS") R.drawable.img_avatar_princess else R.drawable.img_avatar_prince
                                        ),
                                        contentDescription = "صورة الحساب",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            // Device Storage Photo Picker Badge
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFD1D5DB), CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .testTag("pick_profile_photo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "اختيار صورة من الجهاز",
                                    tint = Color(0xFF4B5563),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                // =====================================================================
                // 3. SOCIAL COUNTERS ROW (Synchronized with userProfile)
                // =====================================================================
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenInnerAccountDetails() }
                            .padding(vertical = 6.dp)
                    ) {
                        ScreenshotProfileStatColumn(count = userProfile.visitorsCount.toString(), label = "الزوار")
                        ScreenshotProfileStatColumn(count = userProfile.followersCount.toString(), label = "المعجبين")
                        ScreenshotProfileStatColumn(count = userProfile.followingCount.toString(), label = "متابعة")
                        ScreenshotProfileStatColumn(count = userProfile.friendsCount.toString(), label = "الأصدقاء")
                    }
                }

                // =====================================================================
                // 4. DARK LUXURY VIP1 BANNER CARD (Matches Screenshot 3 + VIP Badge on Account Page)
                // =====================================================================
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF322C2B),
                                        Color(0xFF231F20),
                                        Color(0xFF2C2523)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF6B573A),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onOpenSubScreen(StandaloneSubScreen.VIP_NOBILITY) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("profile_page_vip_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Left: Golden "فتح" button + Winged VIP emblem
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFFDE68A), Color(0xFFD97706))
                                            )
                                        )
                                        .padding(horizontal = 16.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "فتح",
                                        color = Color(0xFF3B2305),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                // Golden winged crown emblem
                                Text(
                                    text = "👑",
                                    fontSize = 22.sp
                                )
                            }

                            // Right: "VIP1" + "افتح امتيازات VIP الحصرية الآن"
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "VIP${userProfile.vipTier.coerceAtLeast(1)}",
                                    color = Color(0xFFFCD34D),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "افتح امتيازات VIP الحصرية الآن",
                                    color = Color(0xFFD6B88A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // =====================================================================
                // 5. DUAL WALLET CARDS ROW: Left = عملة (Coins Only), Right = الماس (Diamonds Only)
                // =====================================================================
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Left Card: عملة (Gold Coins ONLY)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFF4C8),
                                            Color(0xFFFFFBEB)
                                        )
                                    )
                                )
                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(16.dp))
                                .clickable { onOpenSubScreen(StandaloneSubScreen.COINS_ONLY) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                .testTag("open_coins_only_card")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Left: 3D Gold Coin Icon
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color(0xFFFDE047), Color(0xFFF59E0B))
                                            )
                                        )
                                        .border(2.dp, Color(0xFFFEF08A), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Right: "عملة", balance, and "إعادة الشحن" button
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "العملة",
                                        color = Color(0xFF453214),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "%,d".format(userProfile.goldCoins),
                                        color = Color(0xFF1F1F2E),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))
                                                )
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "إعادة الشحن",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Right Card: الألماس (Diamonds ONLY)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFCE7F3),
                                            Color(0xFFFDF2F8)
                                        )
                                    )
                                )
                                .border(1.dp, Color(0xFFFBCFE8), RoundedCornerShape(16.dp))
                                .clickable { onOpenSubScreen(StandaloneSubScreen.DIAMONDS_ONLY) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                .testTag("open_diamonds_only_card")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Left: 3D Pink Diamond Icon
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color(0xFFF472B6), Color(0xFFDB2777))
                                            )
                                        )
                                        .border(2.dp, Color(0xFFFBCFE8), CircleShape)
                                ) {
                                    Text(
                                        text = "💎",
                                        fontSize = 21.sp
                                    )
                                }

                                // Right: "الألماس", balance, and "التفاصيل" button
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "الألماس",
                                        color = Color(0xFF4A1D34),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "%,d".format(userProfile.crystalDiamonds),
                                        color = Color(0xFF1F1F2E),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFFE879F9), Color(0xFFD946EF))
                                                )
                                            )
                                            .padding(horizontal = 14.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "التفاصيل",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // =====================================================================
                // 6. 4-ICON QUICK BAR CARD: الحقيبة | المتجر | المستوى | الشارة
                // =====================================================================
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(vertical = 14.dp, horizontal = 8.dp)
                    ) {
                        // 1. الحقيبة (Bag: Owned Frames & Entry Mounts)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onOpenSubScreen(StandaloneSubScreen.BAG_WARDROBE) }
                                .padding(horizontal = 8.dp)
                                .testTag("open_accessories_card")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFFBCFE8), Color(0xFFF472B6))
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Redeem,
                                    contentDescription = "الحقيبة",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "الحقيبة",
                                color = Color(0xFF2D2E3E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 2. المتجر (Store: Frames, Entry Mounts, Special IDs)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onOpenSubScreen(StandaloneSubScreen.STORE) }
                                .padding(horizontal = 8.dp)
                                .testTag("open_store_card")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFE9D5FF), Color(0xFFA855F7))
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "المتجر",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "المتجر",
                                color = Color(0xFF2D2E3E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 3. المستوى (Levels)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onOpenSubScreen(StandaloneSubScreen.LEVELS) }
                                .padding(horizontal = 8.dp)
                                .testTag("open_levels_card")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFFDE68A), Color(0xFFF59E0B))
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "المستوى",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "المستوى",
                                color = Color(0xFF2D2E3E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 4. الشارة (Owned Badges)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onOpenSubScreen(StandaloneSubScreen.BADGES) }
                                .padding(horizontal = 8.dp)
                                .testTag("open_badges_card")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFFDE047), Color(0xFFD97706))
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "الشارة",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "الشارة",
                                color = Color(0xFF2D2E3E),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // =====================================================================
                // 7. MENU LIST CARD: العائلة | اللغة | خدمة العملاء | دعوة الأصدقاء
                // =====================================================================
                item {
                    val showHostAgencyCard = agencyAuthState.shouldShowHostAgencyCenter ||
                        (userProfile.isHostAgent && com.example.config.MasterAppDatabaseTable.isFeatureVisible("screen_host_agency_center"))
                    val showChargeAgencyCard = agencyAuthState.shouldShowChargeAgencyCenter ||
                        (userProfile.isChargeAgent && com.example.config.MasterAppDatabaseTable.isFeatureVisible("screen_charge_agency_center"))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x14000000))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(vertical = 4.dp)
                    ) {
                        // Row 1: العائلة (تعرض العائلة المنضم إليها)
                        ScreenshotProfileMenuRow(
                            title = "العائلة",
                            trailingValue = userProfile.familyNameAr,
                            icon = Icons.Default.Shield,
                            iconTint = Color(0xFFF59E0B),
                            onClick = { onOpenSubScreen(StandaloneSubScreen.FAMILY_CLAN) },
                            modifier = Modifier.testTag("open_family_row")
                        )
                        HorizontalDivider(color = Color(0xFFF4F5F8), modifier = Modifier.padding(horizontal = 16.dp))

                        // Row 2: اللغة (تعرض لغات عديدة للتطبيق)
                        ScreenshotProfileMenuRow(
                            title = "اللغة",
                            trailingValue = userProfile.appLanguageNameAr,
                            icon = Icons.Default.Language,
                            iconTint = Color(0xFFA855F7),
                            onClick = { onOpenSubScreen(StandaloneSubScreen.LANGUAGE_SETTINGS) },
                            modifier = Modifier.testTag("open_language_row")
                        )
                        HorizontalDivider(color = Color(0xFFF4F5F8), modifier = Modifier.padding(horizontal = 16.dp))

                        // Row 3: خدمة العملاء
                        ScreenshotProfileMenuRow(
                            title = "خدمة العملاء",
                            trailingValue = "متصل 24/7",
                            icon = Icons.Default.HeadsetMic,
                            iconTint = Color(0xFF6366F1),
                            onClick = { onOpenSubScreen(StandaloneSubScreen.CUSTOMER_SERVICE) },
                            modifier = Modifier.testTag("open_customer_service_row")
                        )
                        HorizontalDivider(color = Color(0xFFF4F5F8), modifier = Modifier.padding(horizontal = 16.dp))

                        // Row 4: دعوة الأصدقاء (للحصول على مكافآت إطارات وعملات ذهبية)
                        ScreenshotProfileMenuRow(
                            title = "دعوة الأصدقاء",
                            trailingValue = "مكافآت إطارات وعملات 🎁",
                            icon = Icons.Default.CardGiftcard,
                            iconTint = Color(0xFF10B981),
                            onClick = { onOpenSubScreen(StandaloneSubScreen.INVITE_FRIENDS) },
                            modifier = Modifier.testTag("open_invite_friends_row")
                        )

                        // Server-authorized Agency Portals (when enabled via AgencyAuthorizationViewModel)
                        if (showHostAgencyCard) {
                            HorizontalDivider(color = Color(0xFFF4F5F8), modifier = Modifier.padding(horizontal = 16.dp))
                            ScreenshotProfileMenuRow(
                                title = "مركز الوكالة",
                                trailingValue = null,
                                icon = Icons.Default.EmojiEvents,
                                iconTint = Color(0xFF8B5CF6),
                                onClick = { onOpenSubScreen(StandaloneSubScreen.AGENCIES) },
                                modifier = Modifier.testTag("open_agencies_card")
                            )
                        }

                        if (showChargeAgencyCard) {
                            HorizontalDivider(color = Color(0xFFF4F5F8), modifier = Modifier.padding(horizontal = 16.dp))
                            ScreenshotProfileMenuRow(
                                title = "وكالة الشحن",
                                trailingValue = null,
                                icon = Icons.Default.Star,
                                iconTint = Color(0xFFF59E0B),
                                onClick = { onOpenSubScreen(StandaloneSubScreen.CHARGE_AGENCY) },
                                modifier = Modifier.testTag("open_charge_agency_card")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenshotProfileStatColumn(
    count: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = Color(0xFF1C1C28),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color(0xFF8B90A0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun ScreenshotProfileMenuRow(
    title: String,
    trailingValue: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Left: "<" Arrow + Optional trailing value ("العربية")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = Color(0xFFC0C4D0),
                modifier = Modifier.size(20.dp)
            )
            if (trailingValue != null) {
                Text(
                    text = trailingValue,
                    color = Color(0xFF9EA3B4),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Right: Title + Colored Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF1F1F2E),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

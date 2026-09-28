package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.config.MasterAppDatabaseTable
import com.example.models.FrameStyle3D
import com.example.models.HomeBannerSlideItem
import com.example.models.RoomPermissionRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.models.StandaloneSubScreen
import com.example.models.VoiceRoom
import com.example.ui.components.Ornate3DAvatarWithFrame

/**
 * Screen 1: Home / Party Lobby (`حزب` | `متعلق`)
 * - Removes `بث مباشر` completely as requested.
 * - `حزب`: Displays open rooms formatted as 4 square rooms per screen (2x2 square grid), scrollable upwards.
 * - `متعلق`: Displays My Room (`غرفتي`), Rooms where I am Admin (`الغرف التي أكون فيها أدمن`), and Joined Rooms (`الغرف المنضم إليها`).
 */
@Composable
fun HomeLobbyScreen(
    rooms: List<VoiceRoom>,
    selectedCountryFilter: String,
    onSelectCountryFilter: (String) -> Unit,
    onEnterRoom: (VoiceRoom) -> Unit,
    onCreateRoom: (String, String, String, String?) -> Unit,
    onOpenSubScreen: (StandaloneSubScreen) -> Unit,
    onClaimFirstRecharge: () -> Unit,
    databaseBanners: List<HomeBannerSlideItem> = emptyList()
) {
    var topSubTab by remember { mutableStateOf("حزب") } // حزب | متعلق
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var newRoomTitle by remember { mutableStateOf("") }
    var newRoomCategory by remember { mutableStateOf("دردشة") }
    var newRoomAnnouncement by remember { mutableStateOf("") }
    var newRoomCustomCoverUri by remember { mutableStateOf<String?>(null) }

    val createRoomPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            newRoomCustomCoverUri = uri.toString()
        }
    }

    val filteredRooms = remember(rooms, selectedCountryFilter, searchQuery) {
        rooms.filter { r ->
            val matchesSearch = searchQuery.isBlank() ||
                r.title.contains(searchQuery, ignoreCase = true) ||
                r.displayCode.contains(searchQuery) ||
                r.hostName.contains(searchQuery)
            val matchesCountry = selectedCountryFilter == "رائج 🔥" ||
                selectedCountryFilter == "الكل" ||
                selectedCountryFilter == "ALL" ||
                r.countryFlag == selectedCountryFilter ||
                r.categoryAr == selectedCountryFilter
            matchesSearch && matchesCountry
        }
    }

    val myOwnedRooms = remember(rooms) {
        rooms.filter { it.myRoleInRoom == RoomPermissionRole.OWNER }
    }
    val myAdminRooms = remember(rooms) {
        rooms.filter { it.myRoleInRoom == RoomPermissionRole.ADMIN }
    }
    val myJoinedRooms = remember(rooms) {
        rooms.filter { it.myRoleInRoom == RoomPermissionRole.MEMBER }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFDF7FF),
                        Color(0xFFFAF8FC),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("home_rooms_lazy_column"),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // =====================================================================
            // 1. TOP HEADER ROW (Search on Left | ONLY "حزب" & "متعلق" on Right — "بث مباشر" removed)
            // =====================================================================
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Left: Search Icon
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = Color(0xFF1C1C1E),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { showSearchDialog = true }
                            .testTag("search_rooms_button")
                    )

                    // Right: ONLY ("حزب" | "متعلق") — "بث مباشر" removed
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("حزب", "متعلق").forEach { tab ->
                            val isSelected = topSubTab == tab
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clickable { topSubTab = tab }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag(if (tab == "متعلق") "tab_related_rooms" else "tab_party_rooms")
                            ) {
                                if (isSelected) {
                                    // Purple/Pink abstract splash shape behind active tab
                                    Canvas(
                                        modifier = Modifier
                                            .size(width = 56.dp, height = 30.dp)
                                            .offset(x = (-8).dp, y = (-2).dp)
                                    ) {
                                        drawCircle(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFD555FF),
                                                    Color(0xFFF296FF).copy(alpha = 0.55f)
                                                )
                                            ),
                                            radius = size.minDimension * 0.48f,
                                            center = Offset(size.width * 0.35f, size.height * 0.48f)
                                        )
                                        drawCircle(
                                            color = Color(0xFFE066FF).copy(alpha = 0.45f),
                                            radius = size.minDimension * 0.28f,
                                            center = Offset(size.width * 0.62f, size.height * 0.65f)
                                        )
                                    }
                                }
                                Text(
                                    text = tab,
                                    color = if (isSelected) Color(0xFF141416) else Color(0xFF8E8E93),
                                    fontSize = if (isSelected) 22.sp else 17.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            if (topSubTab == "متعلق") {
                // =====================================================================
                // RELATED TAB (`متعلق`):
                // Displays:
                // 1) غرفتي (My Room + Create/Manage button)
                // 2) الغرف التي أكون فيها أدمن (Rooms where I am Admin)
                // 3) الغرف المنضم إليها (Rooms I have joined)
                // =====================================================================
                item {
                    RelatedRoomsSectionContent(
                        myOwnedRooms = myOwnedRooms,
                        myAdminRooms = myAdminRooms,
                        myJoinedRooms = myJoinedRooms,
                        onEnterRoom = onEnterRoom,
                        onCreateRoomClick = { showCreateRoomDialog = true }
                    )
                }
            } else {
                // =====================================================================
                // PARTY TAB (`حزب`):
                // Country Filter + Banners + 4 Square Rooms per screen (2x2 Square Grid) scrollable upwards
                // =====================================================================
                item {
                    val countryPills = listOf(
                        Triple("🇪🇬", "EGY", "🇪🇬"),
                        Triple("🇦🇪", "ARE", "🇦🇪"),
                        Triple("🇶🇦", "QAT", "🇶🇦"),
                        Triple("🇾🇪", "YEM", "🇾🇪")
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        countryPills.forEach { (flagEmoji, codeText, filterVal) ->
                            val isSelected = selectedCountryFilter == filterVal
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Color(0xFFF3E5F5) else Color.Transparent
                                    )
                                    .clickable {
                                        if (selectedCountryFilter == filterVal) {
                                            onSelectCountryFilter("الكل")
                                        } else {
                                            onSelectCountryFilter(filterVal)
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(text = flagEmoji, fontSize = 18.sp)
                                Text(
                                    text = codeText,
                                    color = Color(0xFF6C6C70),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(width = 14.dp, height = 18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFD896A7), Color(0xFFF3DCE2))
                                    )
                                )
                                .clickable { onSelectCountryFilter("الكل") }
                        )
                    }
                }

                // GOLDEN "مكافآت الشحن" BANNER CAROUSEL (Database-Assigned Only)
                item {
                    RechargeRewardsHeroBanner(
                        databaseBanners = databaseBanners,
                        onBannerTargetScreen = { targetScreenCode ->
                            val sub = when (targetScreenCode) {
                                "STORE" -> StandaloneSubScreen.STORE
                                "VIP", "VIP_NOBILITY" -> StandaloneSubScreen.VIP_NOBILITY
                                "LEVELS" -> StandaloneSubScreen.LEVELS
                                "LEADERBOARD" -> StandaloneSubScreen.LEADERBOARD
                                "EVENT_CENTER" -> StandaloneSubScreen.EVENT_CENTER
                                else -> StandaloneSubScreen.COINS_ONLY
                            }
                            onOpenSubScreen(sub)
                        },
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("recharge_banner")
                    )
                }

                // 3 FEATURED PODIUM CARDS
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        PinkCpPodiumFeatureCard(
                            onClick = { onOpenSubScreen(StandaloneSubScreen.CP_RELATIONSHIPS) },
                            modifier = Modifier
                                .weight(1f)
                                .height(148.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(148.dp)
                        ) {
                            PurpleShieldPodiumCard(
                                onClick = { onOpenSubScreen(StandaloneSubScreen.FAMILY_CLAN) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )

                            VioletTrophyPodiumCard(
                                onClick = { onOpenSubScreen(StandaloneSubScreen.LEADERBOARD) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("ranking_trophy_button")
                            )
                        }
                    }
                }

                // Section Header for Currently Open Rooms (4 square rooms per screen, scrollable upwards)
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${filteredRooms.size} غرفة متصلة الآن 🔥",
                            color = Color(0xFF9333EA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الرومات المفتوحة حالياً",
                            color = Color(0xFF1C1C28),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // 2-COLUMN SQUARE VOICE ROOMS GRID (4 square rooms per screen, scrollable upwards)
                val roomPairs = filteredRooms.chunked(2)
                items(roomPairs.size) { rowIndex ->
                    val pair = roomPairs[rowIndex]
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        pair.forEachIndexed { colIdx, room ->
                            val globalIdx = rowIndex * 2 + colIdx
                            FanCPartyGridRoomCard(
                                room = room,
                                cardIndex = globalIdx,
                                onClick = { onEnterRoom(room) },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }

        // Create New Voice Room Dialog (with Device Storage Room Cover Picker)
        if (showCreateRoomDialog) {
            AlertDialog(
                onDismissRequest = { showCreateRoomDialog = false },
                containerColor = Color.White,
                title = {
                    Text(
                        text = "🎙️ إنشاء غرفتك الصوتية",
                        color = Color(0xFF1C1C1E),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newRoomTitle,
                            onValueChange = { newRoomTitle = it },
                            placeholder = { Text("اسم الغرفة", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newRoomAnnouncement,
                            onValueChange = { newRoomAnnouncement = it },
                            placeholder = { Text("إعلان الترحيب داخل الغرفة...", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF5F0FF))
                                .border(1.dp, Color(0xFFD555FF), RoundedCornerShape(14.dp))
                                .clickable {
                                    createRoomPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "صورة الغرفة من الجهاز",
                                    tint = Color(0xFFD555FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (!newRoomCustomCoverUri.isNullOrBlank()) "✓ تم تحديد صورة الغرفة من الجهاز" else "📷 اختيار صورة الغرفة من تخزين الجهاز",
                                    color = Color(0xFF1C1C1E),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (!newRoomCustomCoverUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = Uri.parse(newRoomCustomCoverUri),
                                    contentDescription = "معاينة صورة الغرفة",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showCreateRoomDialog = false
                        onCreateRoom(newRoomTitle, newRoomCategory, newRoomAnnouncement, newRoomCustomCoverUri)
                    }) {
                        Text("إطلاق الغرفة", color = Color(0xFFD555FF), fontWeight = FontWeight.ExtraBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateRoomDialog = false }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            )
        }

        // Search Rooms Dialog
        if (showSearchDialog) {
            AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                containerColor = Color.White,
                title = {
                    Text(
                        text = "🔍 البحث عن غرفة أو ID",
                        color = Color(0xFF1C1C1E),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("اكتب اسم الغرفة أو ID مثل 6858895", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1C1C1E),
                            unfocusedTextColor = Color(0xFF1C1C1E),
                            focusedBorderColor = Color(0xFFD555FF),
                            unfocusedBorderColor = Color.LightGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showSearchDialog = false }) {
                        Text("بحث", color = Color(0xFFD555FF), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        searchQuery = ""
                        showSearchDialog = false
                    }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            )
        }
    }
}

/**
 * Swipeable & Database-Assigned Home Screen Hero Banners (`RechargeRewardsHeroBanner`)
 * Banners are strictly configured from the Room Database (`home_banners_table`) — no in-app admin controls.
 */
@Composable
private fun RechargeRewardsHeroBanner(
    databaseBanners: List<HomeBannerSlideItem>,
    onBannerTargetScreen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fallbackBanners by MasterAppDatabaseTable.homeBannersTable.collectAsState()
    val sourceBanners = if (databaseBanners.isNotEmpty()) databaseBanners else fallbackBanners
    val activeBanners = remember(sourceBanners) {
        sourceBanners.filter { it.isActive }.sortedBy { it.orderIndex }
    }
    val pageCount = activeBanners.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pageCount) {
        if (pageCount > 1) {
            while (true) {
                delay(4200L)
                val next = (pagerState.currentPage + 1) % pageCount
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF9333EA).copy(alpha = 0.28f))
            .clip(RoundedCornerShape(18.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIdx ->
            val banner = activeBanners.getOrNull(pageIdx) ?: HomeBannerSlideItem(
                id = "banner_default",
                titleAr = "مكافآت الشحن الملكية",
                subtitleAr = "اشحن الآن واحصل على سيارات 7D وإطارات ذهبية 🎁🏎️",
                badgeTextAr = "HOT 🔥",
                iconEmoji = "🎁"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                banner.gradientStartColor,
                                banner.gradientCenterColor,
                                banner.gradientEndColor
                            )
                        )
                    )
                    .border(1.2.dp, Color(0xFFE9D5FF), RoundedCornerShape(18.dp))
                    .clickable { onBannerTargetScreen(banner.targetSubScreen.name) }
            ) {
                if (!banner.customImageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(banner.customImageUri),
                        contentDescription = banner.titleEn,
                        contentScale = ContentScale.Crop,
                        alpha = 0.45f,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(
                            id = if (pageIdx % 2 == 0) R.drawable.img_live_party_banner else R.drawable.img_palace_night_bg
                        ),
                        contentDescription = banner.titleEn,
                        contentScale = ContentScale.Crop,
                        alpha = 0.32f,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.28f))
                                .border(1.dp, Color(0xFFFFF59D).copy(alpha = 0.7f), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = banner.badgeTagAr,
                                color = Color(0xFFFFF59D),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = banner.titleEn,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = banner.subtitleAr,
                            color = Color(0xFFFFF59D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Right Visual Emblem
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                            .border(1.5.dp, Color(0xFFFFF59D), CircleShape)
                    ) {
                        Text(text = banner.iconEmoji, fontSize = 30.sp)
                    }
                }
            }
        }

        // Interactive Pager Indicator Dots (Swipeable & Tappable)
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.25f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            for (i in 0 until pageCount) {
                val isSelected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .size(width = if (isSelected) 16.dp else 6.dp, height = 5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.5f))
                        .clickable {
                            coroutineScope.launch { pagerState.animateScrollToPage(i) }
                        }
                )
            }
        }
    }
}

/**
 * Left Large Pink CP Podium Card matching Screenshot 2:
 * - Hot pink gradient with floating hearts
 * - Top-right intertwined gender/CP badge + 3 arrows
 * - Two avatars with icy blue wings & pink phoenix wings + center golden winged heart
 * - 3D pink hexagonal podium at the bottom
 */
@Composable
private fun PinkCpPodiumFeatureCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFFF4081))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF46B5),
                        Color(0xFFFF6EC7),
                        Color(0xFFFF9CE3)
                    )
                )
            )
            .border(1.5.dp, Color(0xFFFFD8F0), RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        // Decorative hearts & 3D podium drawn on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft glowing circles/hearts in background
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = w * 0.16f,
                center = Offset(w * 0.14f, h * 0.16f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = w * 0.12f,
                center = Offset(w * 0.32f, h * 0.24f)
            )

            // 3D Pink Hexagonal Podium at the bottom
            val topFace = Path().apply {
                moveTo(w * 0.20f, h * 0.68f)
                lineTo(w * 0.80f, h * 0.68f)
                lineTo(w * 0.90f, h * 0.78f)
                lineTo(w * 0.10f, h * 0.78f)
                close()
            }
            drawPath(topFace, color = Color(0xFFFF85CE))

            val frontFace = Path().apply {
                moveTo(w * 0.10f, h * 0.78f)
                lineTo(w * 0.90f, h * 0.78f)
                lineTo(w * 0.90f, h * 1.0f)
                lineTo(w * 0.10f, h * 1.0f)
                close()
            }
            drawPath(
                frontFace,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF66BE), Color(0xFFFF47B0))
                )
            )
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(w * 0.10f, h * 0.78f),
                end = Offset(w * 0.90f, h * 0.78f),
                strokeWidth = 2f
            )
        }

        // Top-Right CP Intertwined Symbol + "▸▸▸"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(bottomStart = 14.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "⚤",
                color = Color(0xFF00E5FF),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "▸▸▸",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Center-Middle: Two Winged Avatars + Center Golden Winged Heart
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = (-6).dp)
        ) {
            Ornate3DAvatarWithFrame(
                avatarType = "PRINCE",
                frameStyle = FrameStyle3D.CRYSTAL_DRAGON_ICE,
                size = 52.dp,
                isSpeaking = false,
                showCrown = false,
                badgeText = null
            )
            Text(
                text = "💝",
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            Ornate3DAvatarWithFrame(
                avatarType = "PRINCESS",
                frameStyle = FrameStyle3D.ROSE_GOLD_PHOENIX,
                size = 52.dp,
                isSpeaking = false,
                showCrown = false,
                badgeText = null
            )
        }

        // Small heart outline on front of podium
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.45f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .size(16.dp)
        )
    }
}

/**
 * Right Top Horizontal Purple Podium Card matching Screenshot 2:
 * - Purple/magenta gradient, center avatar with fiery ring, left/right white sofa circles, 1-2-3 podium, top-right shield + "▸▸▸".
 */
@Composable
private fun PurpleShieldPodiumCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFE040FB))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFDF38FF),
                        Color(0xFFE667FF),
                        Color(0xFFF3A6FF)
                    )
                )
            )
            .border(1.2.dp, Color(0xFFF8D0FF), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        // Top-Right Shield + "▸▸▸"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFFD580FF),
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "▸▸▸",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp
            )
        }

        // Left & Center Avatars/Sofas on 2-1-3 Podium
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp, top = 6.dp)
        ) {
            // Left Sofa Circle (2)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.28f))
            ) {
                Icon(
                    imageVector = Icons.Default.Weekend,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Center Flaming Ring Avatar (1)
            Ornate3DAvatarWithFrame(
                avatarType = "SULTAN",
                frameStyle = FrameStyle3D.IMPERIAL_GOLD_WINGS,
                size = 42.dp,
                isSpeaking = true,
                showCrown = false,
                badgeText = null
            )

            // Right Sofa Circle (3)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.28f))
            ) {
                Icon(
                    imageVector = Icons.Default.Weekend,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Right Bottom Horizontal Violet Trophy Leaderboard Card matching Screenshot 2:
 * - Soft violet-lavender gradient, 3 circular avatars on 2-1-3 podium, top-right gold trophy + "▸▸▸".
 */
@Composable
private fun VioletTrophyPodiumCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF9575CD))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF8E68F5),
                        Color(0xFFA98BFF),
                        Color(0xFFD0C2FF)
                    )
                )
            )
            .border(1.2.dp, Color(0xFFE6DEFF), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        // Top-Right Gold Trophy + "▸▸▸"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "▸▸▸",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp
            )
        }

        // 3 Circular Avatars on Podium
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp, top = 6.dp)
        ) {
            Ornate3DAvatarWithFrame(
                avatarType = "PRINCE",
                frameStyle = FrameStyle3D.ROYAL_VIOLET_AURA,
                size = 32.dp,
                isSpeaking = false,
                showCrown = false,
                badgeText = null
            )
            Ornate3DAvatarWithFrame(
                avatarType = "PRINCE",
                frameStyle = FrameStyle3D.IMPERIAL_GOLD_WINGS,
                size = 42.dp,
                isSpeaking = true,
                showCrown = false,
                badgeText = null
            )
            Ornate3DAvatarWithFrame(
                avatarType = "PRINCESS",
                frameStyle = FrameStyle3D.ROSE_GOLD_PHOENIX,
                size = 32.dp,
                isSpeaking = false,
                showCrown = false,
                badgeText = null
            )
        }
    }
}

/**
 * Content for the `متعلق` (Related) Tab at the top of the Home Screen:
 * Displays:
 * 1. غرفتي (My Room)
 * 2. الغرف التي أكون فيها أدمن (Rooms where I am Admin)
 * 3. الغرف المنضم إليها (Rooms I have joined)
 */
@Composable
private fun RelatedRoomsSectionContent(
    myOwnedRooms: List<VoiceRoom>,
    myAdminRooms: List<VoiceRoom>,
    myJoinedRooms: List<VoiceRoom>,
    onEnterRoom: (VoiceRoom) -> Unit,
    onCreateRoomClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("related_rooms_section")
    ) {
        // 1. غرفتي الخاصة (My Room)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF8E7), Color(0xFFFFF3D1))
                    )
                )
                .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF9333EA), Color(0xFFEC4899))
                            )
                        )
                        .clickable { onCreateRoomClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("create_my_room_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "إنشاء غرفة جديدة",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = "👑 غرفتي الخاصة",
                    color = Color(0xFF92400E),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            myOwnedRooms.forEachIndexed { idx, room ->
                RelatedRoomHorizontalCard(
                    room = room,
                    roleBadgeText = "صاحب الغرفة 👑",
                    roleBadgeColor = Color(0xFFD97706),
                    cardIndex = idx,
                    onClick = { onEnterRoom(room) }
                )
            }
        }

        // 2. الغرف التي أكون فيها أدمن (Admin Rooms)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF3E8FF))
                .border(1.5.dp, Color(0xFFC084FC), RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${myAdminRooms.size} غرف",
                    color = Color(0xFF7E22CE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "🛡️ الغرف التي أكون فيها أدمن",
                    color = Color(0xFF581C87),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            val adminPairs = myAdminRooms.chunked(2)
            adminPairs.forEachIndexed { rowIdx, pair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pair.forEachIndexed { colIdx, room ->
                        FanCPartyGridRoomCard(
                            room = room,
                            cardIndex = rowIdx * 2 + colIdx + 1,
                            onClick = { onEnterRoom(room) },
                            roleOverrideBadge = "أدمن الغرفة 🛡️",
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }

        // 3. الغرف المنضم إليها (Joined Rooms)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFECFDF5))
                .border(1.5.dp, Color(0xFF6EE7B7), RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${myJoinedRooms.size} غرف",
                    color = Color(0xFF047857),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "🎙️ الغرف المنضم إليها",
                    color = Color(0xFF065F46),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            val joinedPairs = myJoinedRooms.chunked(2)
            joinedPairs.forEachIndexed { rowIdx, pair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pair.forEachIndexed { colIdx, room ->
                        FanCPartyGridRoomCard(
                            room = room,
                            cardIndex = rowIdx * 2 + colIdx + 2,
                            onClick = { onEnterRoom(room) },
                            roleOverrideBadge = "منضم 🎙️",
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedRoomHorizontalCard(
    room: VoiceRoom,
    roleBadgeText: String,
    roleBadgeColor: Color,
    cardIndex: Int,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.5.dp, roleBadgeColor.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        // Left: Enter Button + Role Badge
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(roleBadgeColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = roleBadgeText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "دخول الغرفة الآن ⬅️",
                color = Color(0xFF9333EA),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Right: Room Title + ID + Cover Image
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = room.title,
                    color = Color(0xFF1C1C28),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "ID: ${room.displayCode} • ${room.onlineCount} متصل",
                    color = Color(0xFF6B7280),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            val roomRealPhotoUri = room.customCoverImageUri?.takeIf { it.isNotBlank() }
                ?: room.hostCustomAvatarUri?.takeIf { it.isNotBlank() }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.5.dp, roleBadgeColor, RoundedCornerShape(14.dp))
            ) {
                if (!roomRealPhotoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(roomRealPhotoUri),
                        contentDescription = room.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(
                            id = if (cardIndex % 2 == 0) R.drawable.img_live_party_banner else R.drawable.img_avatar_princess
                        ),
                        contentDescription = room.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * 2-Column Square Room Card (`أربع رومات مربعة في كل شاشة، قابلة للتمرير لأعلى`):
 * - Square aspect ratio (`aspectRatio(1f)`) with gold/silver/bronze thin border.
 * - Full-bleed cover image (supports device storage `customCoverImageUri` or high-res room cover).
 * - Optional purple top-left tag pill ("🪐 دردشة" or role badge).
 * - Bottom overlay: Flag 🇪🇬 + Room Title + Overlapping mini speaker avatars + Equalizer bars count.
 */
@Composable
private fun FanCPartyGridRoomCard(
    room: VoiceRoom,
    cardIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    roleOverrideBadge: String? = null
) {
    val borderColor = when (cardIndex % 4) {
        0 -> Color(0xFFE6C665) // Gold border
        1 -> Color(0xFF90A4AE) // Silver-blue border
        2 -> Color(0xFFD4AF37) // Warm gold border
        else -> Color(0xFFE0A98B) // Rose-bronze border
    }

    val fallbackDrawable = when (cardIndex % 4) {
        0 -> R.drawable.img_avatar_princess
        1 -> R.drawable.img_neon_voice_room_bg
        2 -> R.drawable.img_live_party_banner
        else -> R.drawable.img_avatar_prince
    }

    // Always display the real room name and real room photo set by the room owner (`ولكل غرفة اسمها وصورتها الحقيقية المعينة من صاحب الغرفة`)
    val displayTitle = room.titleAr.ifBlank { room.title }
    val roomRealCoverUri = room.customCoverImageUri?.takeIf { it.isNotBlank() }
        ?: room.hostCustomAvatarUri?.takeIf { it.isNotBlank() }
    val displayHeat = room.onlineCount
    val activeSeatedUsers = remember(room.seats) { room.seats.filter { !it.isEmpty } }
    val badgeSeatCount = activeSeatedUsers.size.coerceAtLeast(1)

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E1B2E))
            .border(2.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("room_card_${room.roomId}")
    ) {
        // Full-bleed Real Room Cover Photo set by the Room Owner (or rich fallback if not yet uploaded)
        if (!roomRealCoverUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(roomRealCoverUri),
                contentDescription = displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = fallbackDrawable),
                contentDescription = displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Subtle dark gradient at the bottom for crisp white text legibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x88000000),
                            Color(0xCC000000)
                        )
                    )
                )
        )

        // Top-Left Purple Tag Pill ("🪐 دردشة" or role badge)
        if (roleOverrideBadge != null || cardIndex % 2 == 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 8.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, bottomEnd = 10.dp, topEnd = 4.dp, bottomStart = 4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF7B61FF), Color(0xFFB855FF))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                if (roleOverrideBadge == null) {
                    Text(text = "🪐", fontSize = 10.sp)
                }
                Text(
                    text = roleOverrideBadge ?: "دردشة",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom Content: Country Flag + Title + Overlapping Mini Avatars + Equalizer Bars Count
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            // Country Flag + Room Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Text(text = room.countryFlag.ifBlank { "🇪🇬" }, fontSize = 13.sp)
                }
                Text(
                    text = displayTitle,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom Row: Overlapping Speaker Mini Avatars on Left | Audio Bars + Count on Right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Overlapping mini avatars of real seated users + count circle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val seatsToShow = activeSeatedUsers.take(3)
                    seatsToShow.forEachIndexed { i, seat ->
                        if (!seat.occupantCustomAvatarUri.isNullOrBlank()) {
                            AsyncImage(
                                model = Uri.parse(seat.occupantCustomAvatarUri),
                                contentDescription = seat.occupantName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .offset(x = (-4 * i).dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(
                                    id = if (seat.occupantAvatarType == "PRINCESS") R.drawable.img_avatar_princess else R.drawable.img_avatar_prince
                                ),
                                contentDescription = seat.occupantName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .offset(x = (-4 * i).dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        }
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset(x = (-4 * seatsToShow.size).dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xAA4E342E))
                            .border(1.dp, Color.White, CircleShape)
                    ) {
                        Text(
                            text = "$badgeSeatCount",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Equalizer Bars + Heat Count (e.g., "ılı 1536")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Canvas(modifier = Modifier.size(width = 12.dp, height = 11.dp)) {
                        val barW = 2.4.dp.toPx()
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(0f, size.height * 0.45f),
                            size = Size(barW, size.height * 0.55f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(size.width * 0.42f, size.height * 0.10f),
                            size = Size(barW, size.height * 0.90f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(size.width * 0.82f, size.height * 0.30f),
                            size = Size(barW, size.height * 0.70f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                    Text(
                        text = "$displayHeat",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

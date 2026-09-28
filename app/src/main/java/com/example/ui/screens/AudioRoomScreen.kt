package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.models.AvatarFrameStyle
import com.example.models.CasualGameItem
import com.example.models.ChatBubbleFrameStyle
import com.example.models.GiftCatalogItem
import com.example.models.MicSeatState
import com.example.models.RoomBackgroundTheme
import com.example.models.RoomChatMessage
import com.example.models.RoomMicShapeStyle
import com.example.models.RoomUserRole
import com.example.models.StoreCatalogItem
import com.example.models.StoreItemCategory
import com.example.models.UserProfile
import com.example.models.VoiceRoomItem
import com.example.ui.components.AnimatedSeatEmojiOverlay

// Unified Outer-4-Screens Palette for all Room Modals, Cards, and Sheets
private val UnifiedModalSurface = Color(0xFFF9F7FD)
private val UnifiedModalCardBg = Color(0xFFFFFFFF)
private val UnifiedModalLavenderHeader = Color(0xFFEDE0FC)
private val UnifiedModalLavenderSoft = Color(0xFFF3E8FF)
private val UnifiedModalBorder = Color(0xFFE9D5FF)
private val UnifiedTextPrimary = Color(0xFF1E1035)
private val UnifiedTextSecondary = Color(0xFF6B5B85)
private val UnifiedRoyalPurple = Color(0xFF7C3AED)
private val UnifiedRoyalPink = Color(0xFFEC4899)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioRoomScreen(
    room: VoiceRoomItem,
    currentUser: UserProfile,
    messages: List<RoomChatMessage>,
    selectedChatFilter: String,
    isMyMicMuted: Boolean,
    giftsCatalog: List<GiftCatalogItem>,
    gamesCatalog: List<CasualGameItem>,
    storeItems: List<StoreCatalogItem> = emptyList(),
    onSelectChatFilter: (String) -> Unit,
    onToggleMyMic: () -> Unit,
    onTakeOrLeaveSeat: (Int) -> Unit,
    onSendChatMessage: (String) -> Unit,
    onSendGift: (GiftCatalogItem, String, Int) -> Unit,
    onPlayMiniGame: (CasualGameItem) -> Unit = {},
    onTogglePkBattle: () -> Unit = {},
    onSupportPkTeam: (Boolean) -> Unit = {},
    onClaimLuckyBag: () -> Unit = {},
    onDropLuckyBag: (Long) -> Unit = {},
    onResetMicCounters: () -> Unit,
    onToggleLockSeat: (Int) -> Unit,
    onChangeRoomBackground: (String) -> Unit,
    onChangeRoomMicShape: (String) -> Unit = {},
    onBuyOrEquipRoomTheme: (StoreCatalogItem) -> Unit = {},
    onChangeMyRoomRole: (RoomUserRole) -> Unit,
    onEditRoomNameAndPhoto: (String, String, String?) -> Unit = { _, _, _ -> },
    onUpdateRoomCustomCoverPhoto: (String) -> Unit = {},
    onInspectSeatInnerAccount: (MicSeatState) -> Unit = {},
    onOpenStoreFromRoom: () -> Unit = {},
    onFollowRoomUser: (String, String) -> Unit = { _, _ -> },
    onKickUserFromSeat: (Int, String) -> Unit = { _, _ -> },
    onMuteUserOnSeat: (Int, String) -> Unit = { _, _ -> },
    onBanUserFromRoom: (Int, String, String) -> Unit = { _, _, _ -> },
    onAssignRoomAdmin: (String, String) -> Unit = { _, _ -> },
    onSendSeatEmojiReaction: (String, String) -> Unit = { _, _ -> },
    onRollDiceInRoom: () -> Unit = {},
    onLeaveRoom: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    var showToolsSheet by rememberSaveable { mutableStateOf(false) }
    var showGiftSheet by rememberSaveable { mutableStateOf(false) }
    var showEmojiPickerSheet by rememberSaveable { mutableStateOf(false) }
    var showChatInputDialog by rememberSaveable { mutableStateOf(false) }
    var showEditRoomDialog by rememberSaveable { mutableStateOf(false) }
    var showThemeSelectorSheet by rememberSaveable { mutableStateOf(false) }
    var chatDraft by rememberSaveable { mutableStateOf("") }
    var selectedGiftCombo by rememberSaveable { mutableIntStateOf(1) }
    var selectedGiftReceiver by rememberSaveable { mutableStateOf(room.hostName) }
    var clickedSeat by remember { mutableStateOf<MicSeatState?>(null) }
    var clickedEmptySeatForAdmin by remember { mutableStateOf<MicSeatState?>(null) }
    var editRoomTitleDraft by rememberSaveable(room.title) { mutableStateOf(room.title) }
    var editRoomAnnouncementDraft by rememberSaveable(room.announcementAr) { mutableStateOf(room.announcementAr) }
    var editRoomCustomUriDraft by rememberSaveable(room.customCoverImageUri) {
        mutableStateOf(room.customCoverImageUri)
    }

    // Automatic Role Detection (`الغرفه تتعرف تلقائيا على من هو ادمن ومن هو مستخدم ومن هو صاحب الغرفه`)
    val myRole = remember(room.myRoleInRoom, room.hostUserId, room.adminUserIds, currentUser.displayId) {
        when {
            room.hostUserId == currentUser.displayId || room.roomDisplayId == currentUser.displayId -> RoomUserRole.OWNER
            room.adminUserIds.contains(currentUser.displayId) -> RoomUserRole.ADMIN
            else -> room.myRoleInRoom
        }
    }
    val isRoomOwner = myRole == RoomUserRole.OWNER
    val isRoomAdminOrOwner = myRole == RoomUserRole.OWNER || myRole == RoomUserRole.ADMIN

    val activeRoomTheme = remember(room.backgroundStyleId) {
        RoomBackgroundTheme.fromId(room.backgroundStyleId)
    }
    val activeMicShapeStyle = remember(room.micShapeStyleId) {
        RoomMicShapeStyle.fromId(room.micShapeStyleId)
    }

    val roomCoverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val uriString = uri.toString()
            editRoomCustomUriDraft = uriString
            onUpdateRoomCustomCoverPhoto(uriString)
        }
    }

    BackHandler {
        when {
            showThemeSelectorSheet -> showThemeSelectorSheet = false
            showEmojiPickerSheet -> showEmojiPickerSheet = false
            showToolsSheet -> showToolsSheet = false
            showGiftSheet -> showGiftSheet = false
            clickedSeat != null -> clickedSeat = null
            clickedEmptySeatForAdmin != null -> clickedEmptySeatForAdmin = null
            showEditRoomDialog -> showEditRoomDialog = false
            else -> onLeaveRoom()
        }
    }

    // Check if the current user is seated on any mic
    val isCurrentUserSeated = remember(room.micSeats, currentUser.displayId, currentUser.nickname) {
        room.micSeats.any {
            it.occupantId == currentUser.displayId ||
                it.occupantName.equals(currentUser.nickname, ignoreCase = true) ||
                it.occupantName.equals("Sayed", ignoreCase = true)
        }
    }

    // Build exact 10 seats (1..10) in 2 rows of 5
    val tenSeats = remember(room.micSeats) {
        (1..10).map { number ->
            val existing = room.micSeats.getOrNull(number - 1)
            existing?.copy(seatIndex = number - 1) ?: MicSeatState(
                seatIndex = number - 1,
                occupantName = null
            )
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(activeRoomTheme.bottomColor)
        ) {
            // =========================================================================
            // 1. ANIMATED, HIGH-QUALITY, AND TRANSPARENT CHATROOM BACKGROUND THEME
            // =========================================================================
            AnimatedTransparentRoomThemeCanvas(
                theme = activeRoomTheme,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // =====================================================================
                // 2. TOP BAR: Left = Power Exit + Online Viewers | Right = Room Pill
                // =====================================================================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Left Side: Power exit button + Viewer avatar + count pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onLeaveRoom,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .testTag("leave_room_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "مغادرة الغرفة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Small circular viewer avatar
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F2437))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        ) {
                            if (!currentUser.customAvatarUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = Uri.parse(currentUser.customAvatarUri),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Viewer Count Pill
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.38f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${tenSeats.count { it.occupantName != null }.coerceAtLeast(2)}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Right Side: Room Info Pill ("+" button | Room Name & ID | Room Cover Photo)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.36f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
                            .clickable {
                                if (isRoomOwner) {
                                    showEditRoomDialog = true
                                } else {
                                    onFollowRoomUser(room.id, room.hostName)
                                }
                            }
                            .padding(start = 8.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                            .testTag("room_header_info_pill")
                    ) {
                        // Pink "+" Follow Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFEC4899), Color(0xFFA855F7))
                                    )
                                )
                                .clickable { onFollowRoomUser(room.id, room.hostName) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "متابعة الغرفة",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Room Name & ID
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = room.title.ifBlank { "السلطان" },
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.clickable {
                                    clipboardManager.setText(AnnotatedString(room.roomDisplayId.ifBlank { "1070029" }))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.65f),
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "ID:${room.roomDisplayId.ifBlank { "1070029" }}",
                                    color = Color.White.copy(alpha = 0.72f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Room Square Avatar (Room Owner can change photo from Device Storage)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1F2437))
                                .clickable {
                                    if (isRoomOwner) {
                                        roomCoverPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                }
                        ) {
                            val roomHeaderCoverUri = room.customCoverImageUri?.takeIf { it.isNotBlank() }
                                ?: room.hostCustomAvatarUri?.takeIf { it.isNotBlank() }
                                ?: (if (isRoomOwner) currentUser.customAvatarUri else null)
                            if (!roomHeaderCoverUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = Uri.parse(roomHeaderCoverUri),
                                    contentDescription = "صورة الغرفة",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(
                                        id = if (room.hostName.contains("شهد") || room.hostName.contains("ريم") || room.hostName.contains("نور")) {
                                            R.drawable.img_avatar_princess
                                        } else {
                                            R.drawable.img_avatar_prince
                                        }
                                    ),
                                    contentDescription = "صورة الغرفة",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // =====================================================================
                // 3. TROPHY COUNTER PILL + AUTOMATIC ROLE DETECTION BADGE
                //    + THEME SELECTOR QUICK BUTTON (FOR OWNER ONLY) — NO STORE BUTTON!
                // =====================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Trophy Counter Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.42f))
                                .border(0.8.dp, Color(0xFFFBBF24).copy(alpha = 0.45f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${tenSeats.sumOf { it.giftCoinsCounter }}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Room Owner Background Theme Selector Pill (`محدد ثيمات الخلفية المتحركة والشفافة`)
                        if (isRoomOwner) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                activeRoomTheme.accentGlowColor.copy(alpha = 0.36f),
                                                activeRoomTheme.secondaryGlowColor.copy(alpha = 0.28f)
                                            )
                                        )
                                    )
                                    .border(0.9.dp, activeRoomTheme.accentGlowColor, RoundedCornerShape(50))
                                    .clickable { showThemeSelectorSheet = true }
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                                    .testTag("open_room_theme_selector_pill")
                            ) {
                                Text(
                                    text = "${activeRoomTheme.iconEmoji} ثيم الخلفية",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Automatic Role Detection Pill (`الغرفه تتعرف تلقائيا على من هو ادمن ومن هو مستخدم ومن هو صاحب الغرفه`)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                when (myRole) {
                                    RoomUserRole.OWNER -> Brush.horizontalGradient(
                                        listOf(Color(0xFF9333EA).copy(alpha = 0.75f), Color(0xFFEC4899).copy(alpha = 0.75f))
                                    )
                                    RoomUserRole.ADMIN -> Brush.horizontalGradient(
                                        listOf(Color(0xFF0284C7).copy(alpha = 0.75f), Color(0xFF6366F1).copy(alpha = 0.75f))
                                    )
                                    RoomUserRole.MEMBER -> Brush.horizontalGradient(
                                        listOf(Color.Black.copy(alpha = 0.36f), Color.Black.copy(alpha = 0.36f))
                                    )
                                }
                            )
                            .border(
                                0.8.dp,
                                when (myRole) {
                                    RoomUserRole.OWNER -> Color(0xFFFBBF24)
                                    RoomUserRole.ADMIN -> Color(0xFF38BDF8)
                                    RoomUserRole.MEMBER -> Color.White.copy(alpha = 0.22f)
                                },
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("auto_detected_room_role_badge")
                    ) {
                        Text(
                            text = when (myRole) {
                                RoomUserRole.OWNER -> "👑 صاحب الغرفة"
                                RoomUserRole.ADMIN -> "🛡️ أدمن الغرفة"
                                RoomUserRole.MEMBER -> "👤 مستخدم"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // =====================================================================
                // 4. 10-MIC GRID (2 ROWS x 5 MICS, RTL ORDERED 1..5 AND 6..10)
                //    - Supports Database-Customizable Mic Shapes (`activeMicShapeStyle`)
                //    - Renders Animated 3D Emoji Above Avatars (`AnimatedSeatEmojiOverlay`)
                //    - Occupied Seat Tap -> Opens Account Info & Role Permissions Card
                //    - Empty Seat Tap -> Member climbs/leaves mic; Admin/Owner can climb or lock/unlock mic
                // =====================================================================
                val firstRowSeats = tenSeats.subList(0, 5).reversed() // RTL: 1 on right, 5 on left
                val secondRowSeats = tenSeats.subList(5, 10).reversed() // RTL: 6 on right, 10 on left

                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {
                    // Row 1 (Seats 5, 4, 3, 2, 1 from left to right)
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        firstRowSeats.forEach { seat ->
                            ScreenshotMicSeatItem(
                                seat = seat,
                                displayNumber = seat.seatIndex + 1,
                                currentUser = currentUser,
                                micShapeStyle = activeMicShapeStyle,
                                isAdminInRoom = room.adminUserIds.contains(seat.occupantId ?: ""),
                                isHostOfRoom = seat.seatIndex == 0 || seat.occupantId == room.hostUserId,
                                onSeatSingleTap = {
                                    if (seat.occupantName != null) {
                                        clickedSeat = seat
                                    } else if (isRoomAdminOrOwner) {
                                        clickedEmptySeatForAdmin = seat
                                    } else {
                                        onTakeOrLeaveSeat(seat.seatIndex)
                                    }
                                },
                                onSeatDoubleTap = {
                                    if (seat.occupantName != null) {
                                        onInspectSeatInnerAccount(seat)
                                    } else {
                                        onTakeOrLeaveSeat(seat.seatIndex)
                                    }
                                }
                            )
                        }
                    }

                    // Row 2 (Seats 10, 9, 8, 7, 6 from left to right)
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        secondRowSeats.forEach { seat ->
                            ScreenshotMicSeatItem(
                                seat = seat,
                                displayNumber = seat.seatIndex + 1,
                                currentUser = currentUser,
                                micShapeStyle = activeMicShapeStyle,
                                isAdminInRoom = room.adminUserIds.contains(seat.occupantId ?: ""),
                                isHostOfRoom = seat.seatIndex == 0 || seat.occupantId == room.hostUserId,
                                onSeatSingleTap = {
                                    if (seat.occupantName != null) {
                                        clickedSeat = seat
                                    } else if (isRoomAdminOrOwner) {
                                        clickedEmptySeatForAdmin = seat
                                    } else {
                                        onTakeOrLeaveSeat(seat.seatIndex)
                                    }
                                },
                                onSeatDoubleTap = {
                                    if (seat.occupantName != null) {
                                        onInspectSeatInnerAccount(seat)
                                    } else {
                                        onTakeOrLeaveSeat(seat.seatIndex)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // =====================================================================
                // 5. CHAT FILTER TABS ROW (Right-Aligned: الغرفة | الدردشة | الكل)
                //    (NO PK Bar & NO Floating Games Icon as requested!)
                // =====================================================================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val tabs = listOf("الغرفة", "الدردشة", "الكل")
                    tabs.forEach { titleAr ->
                        val isSelected = selectedChatFilter == titleAr
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .background(Color.White.copy(alpha = 0.12f))
                                            .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(50))
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { onSelectChatFilter(titleAr) }
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = titleAr,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // =====================================================================
                // 7. TRANSPARENT IN-ROOM CHAT MESSAGES STREAM
                // (`دائما الكتابه في الغرف تكون شفافه ولا يظهر حولها اطار الاطار هذا يضاعف قاعه الدردشه ويمكن شرائه من المتجر`)
                // - Default chat messages have NO border and a completely transparent background.
                // - Store-purchased Chat Bubble Frames (`msg.chatBubbleStyle != TRANSPARENT_DEFAULT`)
                //   display their luxury glowing frame around the message!
                // =====================================================================
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Transparent System Notice Text (No heavy box frame)
                        item {
                            Text(
                                text = "✨ أهلاً بك في الغرفة الصوتية! يرجى احترام الجميع والدردشة بلطف.",
                                color = Color(0xFFFFF59D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Right,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.85f),
                                        offset = Offset(0f, 1.5f),
                                        blurRadius = 4f
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        // Transparent User Join Line ("انضم إلى الغرفة")
                        item {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "انضم إلى الغرفة ✨",
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        style = TextStyle(
                                            shadow = Shadow(
                                                color = Color.Black.copy(alpha = 0.85f),
                                                offset = Offset(0f, 1.5f),
                                                blurRadius = 4f
                                            )
                                        )
                                    )
                                    Text(
                                        text = currentUser.nickname.ifBlank { "Sayed" },
                                        color = Color(0xFFFFD700),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TextStyle(
                                            shadow = Shadow(
                                                color = Color.Black.copy(alpha = 0.85f),
                                                offset = Offset(0f, 1.5f),
                                                blurRadius = 4f
                                            )
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFF0284C7).copy(alpha = 0.85f))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "♂ ${currentUser.wealthLevel}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFF9333EA).copy(alpha = 0.85f))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "VIP ${currentUser.vipTier}",
                                            color = Color(0xFFFFF59D),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        // Chat Messages Stream (Always transparent without frame by default;
                        // only shows an ornate frame if equipped from the Store!)
                        val filteredMessages = messages.filter { msg ->
                            when (selectedChatFilter) {
                                "الدردشة" -> !msg.isSystemWelcome && !msg.isGiftAnnouncement
                                "الغرفة" -> msg.isSystemWelcome || msg.isGiftAnnouncement
                                else -> true
                            }
                        }

                        items(filteredMessages, key = { it.id }) { msg ->
                            val bubbleStyle = msg.chatBubbleStyle
                            val hasStoreChatFrame = bubbleStyle.hasBorderFrame
                            val colorSeed = kotlin.math.abs(msg.id.hashCode()) % 4

                            val senderNameColor = when {
                                msg.isGiftAnnouncement -> Color(0xFFFFD700)
                                msg.isSystemWelcome -> Color(0xFF6EE7B7)
                                hasStoreChatFrame -> bubbleStyle.borderPrimaryColor
                                colorSeed == 0 -> Color(0xFF7DD3FC)
                                colorSeed == 1 -> Color(0xFFF9A8D4)
                                colorSeed == 2 -> Color(0xFF5EEAD4)
                                else -> Color(0xFFFDE047)
                            }

                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .then(
                                            if (hasStoreChatFrame) {
                                                // Store-Purchased Chat Bubble Frame (`إطار فقاعة الدردشة من المتجر`)
                                                Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(
                                                                bubbleStyle.backgroundTint,
                                                                bubbleStyle.borderSecondaryColor.copy(alpha = 0.22f)
                                                            )
                                                        )
                                                    )
                                                    .border(
                                                        width = 1.2.dp,
                                                        brush = Brush.horizontalGradient(
                                                            listOf(
                                                                bubbleStyle.borderPrimaryColor,
                                                                bubbleStyle.borderSecondaryColor
                                                            )
                                                        ),
                                                        shape = RoundedCornerShape(14.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                            } else {
                                                // Default Transparent Writing WITHOUT any border frame (`دائما الكتابه في الغرف تكون شفافه ولا يظهر حولها اطار`)
                                                Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            }
                                        )
                                        .clickable {
                                            // Touching any account in chat opens their Account Card with Role Permissions
                                            val matchingSeat = tenSeats.firstOrNull {
                                                it.occupantDisplayId == msg.senderDisplayId ||
                                                    it.occupantName == msg.senderName
                                            } ?: MicSeatState(
                                                seatIndex = 0,
                                                occupantUserId = msg.senderUserId,
                                                occupantName = msg.senderName,
                                                occupantDisplayId = msg.senderDisplayId,
                                                occupantAvatarType = msg.senderAvatarType,
                                                occupantCustomAvatarUri = msg.senderCustomAvatarUri,
                                                occupantRole = msg.senderRole,
                                                occupantRoomRole = msg.senderRoomRole,
                                                occupantVip = msg.senderVip,
                                                occupantWealthLevel = msg.senderWealthLevel,
                                                occupantCharismaLevel = msg.senderCharismaLevel
                                            )
                                            clickedSeat = matchingSeat
                                        }
                                ) {
                                    if (hasStoreChatFrame) {
                                        Text(
                                            text = bubbleStyle.badgeEmoji,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = msg.contentAr,
                                        color = if (msg.isGiftAnnouncement) Color(0xFFFFF59D) else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Right,
                                        style = TextStyle(
                                            shadow = Shadow(
                                                color = Color.Black.copy(alpha = 0.90f),
                                                offset = Offset(0f, 1.5f),
                                                blurRadius = 4f
                                            )
                                        )
                                    )
                                    Text(
                                        text = "${msg.senderName}:",
                                        color = senderNameColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = TextStyle(
                                            shadow = Shadow(
                                                color = Color.Black.copy(alpha = 0.90f),
                                                offset = Offset(0f, 1.5f),
                                                blurRadius = 4f
                                            )
                                        )
                                    )
                                    // Level pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.Black.copy(alpha = 0.35f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "LV.${msg.senderWealthLevel}",
                                            color = senderNameColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // =====================================================================
                // 8. BOTTOM BAR (Tools Grid | Broadcast | Gift Box | Animated Emoji | Mic | Chat Input)
                // =====================================================================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Left Side Icons: Tools Grid | Megaphone | Pink Gift Box | Animated Emoji | Mic
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        // 1. Tools Grid Button ("الأدوات")
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .clickable { showToolsSheet = true }
                                .testTag("room_tools_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = "الأدوات",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 2. Megaphone / Broadcast Horn Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .clickable { showChatInputDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "بث رسالة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 3. Pink Gift Box Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFF472B6), Color(0xFFDB2777))
                                    )
                                )
                                .clickable { showGiftSheet = true }
                                .testTag("open_gift_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "الهدايا",
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 4. Animated 3D Emoji Above Mic Button (`والاموجي المتحرك يظهر فوق الصور يكون متحرك ومميز جدا`)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .clickable { showEmojiPickerSheet = true }
                                .testTag("open_emoji_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEmotions,
                                contentDescription = "إيموجي متحرك فوق المايك",
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 5. Mic Mute/Unmute Toggle Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .clickable { onToggleMyMic() }
                                .testTag("toggle_mic_button")
                        ) {
                            Icon(
                                imageVector = if (isMyMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "المايك",
                                tint = if (isMyMicMuted) Color(0xFFFF6B6B) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Right Side: Chat Input Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable { showChatInputDialog = true }
                            .padding(
                                horizontal = 14.dp,
                                vertical = 9.dp
                            )
                            .testTag("open_room_chat_input_button")
                    ) {
                        Text(
                            text = "Hi",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "دردشة",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // =========================================================================
            // MODAL 1: ROOM TOOLS BOTTOM SHEET (Unified Outer-4-Screens Light Lavender Card Style!)
            // Automatic Role Enforcement:
            // - Normal Member: sees only Emoji Reaction & Music
            // - Admin: sees Clean All + Emoji + Music
            // - Owner: sees Clean All + Background Themes + Mic Shapes & Room Settings + Music
            // =========================================================================
            AnimatedVisibility(
                visible = showToolsSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.40f))
                        .clickable { showToolsSheet = false }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(UnifiedModalLavenderHeader, UnifiedModalSurface, UnifiedModalCardBg)
                                )
                            )
                            .border(
                                1.2.dp,
                                UnifiedModalBorder,
                                RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                            )
                            .clickable(enabled = false) {}
                            .navigationBarsPadding()
                            .padding(vertical = 18.dp, horizontal = 20.dp)
                    ) {
                        Text(
                            text = "أدوات الغرفة (${myRole.badgeEmoji} ${myRole.titleAr})",
                            color = UnifiedTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = when (myRole) {
                                RoomUserRole.OWNER -> "تم التعرف تلقائياً: أنت صاحب الغرفة ولديك كافة الصلاحيات"
                                RoomUserRole.ADMIN -> "تم التعرف تلقائياً: أنت أدمن الغرفة"
                                RoomUserRole.MEMBER -> "تم التعرف تلقائياً: مستخدم (صعود مايك، دردشة، هدايا، متابعة)"
                            },
                            color = UnifiedRoyalPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1. إيموجي متحرك فوق المايك (Available to all)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        showToolsSheet = false
                                        showEmojiPickerSheet = true
                                    }
                                    .testTag("tool_animated_emoji")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFF59E0B), Color(0xFFEC4899))
                                            )
                                        )
                                ) {
                                    Text(text = "🤩", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "إيموجي متحرك",
                                    color = UnifiedTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 2. موسيقى (Available to all)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        onSendChatMessage("🎵 تم تشغيل موسيقى الغرفة")
                                        showToolsSheet = false
                                    }
                                    .testTag("tool_music")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF38BDF8), Color(0xFF6366F1))
                                            )
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Album,
                                        contentDescription = "موسيقى",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "موسيقى",
                                    color = UnifiedTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 3. تنظيف الكل (Only for Admin & Owner)
                            if (isRoomAdminOrOwner) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            onResetMicCounters()
                                            showToolsSheet = false
                                        }
                                        .testTag("tool_clean_all")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
                                                )
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CleaningServices,
                                            contentDescription = "تنظيف الكل",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "تصفير المايكات",
                                        color = UnifiedTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // 4. ثيمات الخلفية المتحركة (Only for Owner)
                            if (isRoomOwner) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            showToolsSheet = false
                                            showThemeSelectorSheet = true
                                        }
                                        .testTag("tool_room_themes")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFFA855F7), Color(0xFFEC4899))
                                                )
                                            )
                                    ) {
                                        Text(text = "🌌", fontSize = 22.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "ثيمات الخلفية",
                                        color = UnifiedTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // 5. إعدادات الغرفة وأشكال المايكات (Only for Owner)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            showToolsSheet = false
                                            showEditRoomDialog = true
                                        }
                                        .testTag("tool_room_settings")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF10B981), Color(0xFF059669))
                                                )
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "إعدادات",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "إعدادات الغرفة",
                                        color = UnifiedTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // =========================================================================
            // MODAL 1B: ANIMATED 3D EMOJI REACTION PICKER SHEET (`الاموجي المتحرك يظهر فوق الصور يكون متحرك ومميز جدا`)
            // =========================================================================
            AnimatedVisibility(
                visible = showEmojiPickerSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                val animatedEmojis = listOf(
                    "😂" to "ضحك هستيري",
                    "😍" to "إعجاب ملكي",
                    "🔥" to "حماس ناري",
                    "👑" to "تاج السلطان",
                    "💖" to "قلوب متطايرة",
                    "👏" to "تصفيق حار",
                    "🥳" to "احتفال",
                    "😎" to "هيبة وفخامة",
                    "🌹" to "وردة حمراء",
                    "💎" to "ألماسة متألقة",
                    "🚀" to "انطلاق صاروخي",
                    "🤩" to "انبهار نجوم"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.38f))
                        .clickable { showEmojiPickerSheet = false }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(UnifiedModalLavenderHeader, UnifiedModalSurface, UnifiedModalCardBg)
                                )
                            )
                            .border(
                                1.2.dp,
                                UnifiedModalBorder,
                                RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                            )
                            .clickable(enabled = false) {}
                            .navigationBarsPadding()
                            .padding(vertical = 18.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "✨ الإيموجي التفاعلي المتحرك فوق المايك ✨",
                            color = UnifiedTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "اختر إيموجي ليظهر متحركاً ومميزاً فوق صورتك على المايك",
                            color = UnifiedTextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(230.dp)
                        ) {
                            items(animatedEmojis, key = { it.first }) { (emojiChar, emojiTitle) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(UnifiedModalLavenderSoft)
                                        .border(1.dp, UnifiedModalBorder, RoundedCornerShape(16.dp))
                                        .clickable {
                                            onSendSeatEmojiReaction(emojiChar, emojiTitle)
                                            showEmojiPickerSheet = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 6.dp)
                                ) {
                                    Text(text = emojiChar, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = emojiTitle,
                                        color = UnifiedTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // MODAL 2: GIFT DRAWER SHEET (Unified Outer-4-Screens Light Lavender Card Style!)
            // =========================================================================
            if (showGiftSheet) {
                AlertDialog(
                    onDismissRequest = { showGiftSheet = false },
                    containerColor = UnifiedModalSurface,
                    title = {
                        Text(
                            text = "إرسال هدية إلى $selectedGiftReceiver 🎁",
                            color = UnifiedTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(230.dp)
                            ) {
                                items(giftsCatalog, key = { it.id }) { gift ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(UnifiedModalCardBg)
                                            .border(1.dp, UnifiedModalBorder, RoundedCornerShape(14.dp))
                                            .clickable {
                                                onSendGift(gift, selectedGiftReceiver, selectedGiftCombo)
                                                showGiftSheet = false
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Text(text = gift.iconEmoji, fontSize = 26.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = gift.titleAr,
                                            color = UnifiedTextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${gift.coinPrice} 🪙",
                                            color = Color(0xFFD97706),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showGiftSheet = false }) {
                            Text("إغلاق", color = UnifiedRoyalPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // =========================================================================
            // MODAL 2B: EMPTY SEAT ACTION FOR ADMIN & ROOM OWNER (Climb Mic OR Lock/Unlock Mic)
            // =========================================================================
            clickedEmptySeatForAdmin?.let { emptySeat ->
                AlertDialog(
                    onDismissRequest = { clickedEmptySeatForAdmin = null },
                    containerColor = UnifiedModalSurface,
                    title = {
                        Text(
                            text = "إدارة المايك رقم #${emptySeat.seatIndex + 1} (${myRole.badgeEmoji} ${myRole.titleAr})",
                            color = UnifiedTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1. Climb onto Mic
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(UnifiedRoyalPurple, UnifiedRoyalPink)
                                        )
                                    )
                                    .clickable {
                                        onTakeOrLeaveSeat(emptySeat.seatIndex)
                                        clickedEmptySeatForAdmin = null
                                    }
                                    .padding(vertical = 11.dp)
                            ) {
                                Text(
                                    text = "🎙️ صعود على المايك رقم #${emptySeat.seatIndex + 1}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 2. Lock / Unlock Mic (Admin & Owner permission)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(UnifiedModalLavenderSoft)
                                    .border(1.dp, UnifiedRoyalPurple, RoundedCornerShape(12.dp))
                                    .clickable {
                                        onToggleLockSeat(emptySeat.seatIndex)
                                        clickedEmptySeatForAdmin = null
                                    }
                                    .padding(vertical = 11.dp)
                            ) {
                                Text(
                                    text = if (emptySeat.isLocked) "🔓 فتح قفل المايك رقم #${emptySeat.seatIndex + 1}"
                                    else "🔒 قفل المايك رقم #${emptySeat.seatIndex + 1}",
                                    color = UnifiedRoyalPurple,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { clickedEmptySeatForAdmin = null }) {
                            Text("إغلاق", color = UnifiedTextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // =========================================================================
            // MODAL 3: USER ACCOUNT INFO CARD & ROLE-BASED PERMISSIONS
            // Unified Outer-4-Screens Pastel-Lavender & Crisp White 3D Card Design!
            // (`تحسين مظهر بطاقات المعلومات والصفحه الرئيسيه للحسابات تشبه نفس التصميم الخاص بالتطبيق الخارجي مطابقه للصفحات الاربعه`)
            // (`وعند لمس المستخدم على حسابه يظهر له فقط النزول من على المايك وارسال هدية.`)
            // =========================================================================
            clickedSeat?.let { seat ->
                val targetName = seat.occupantName ?: currentUser.nickname.ifBlank { "Sayed" }
                val targetId = seat.occupantId ?: currentUser.displayId
                val isFollowingTarget = room.followingUserIds.contains(targetId)
                val isTargetAdmin = room.adminUserIds.contains(targetId)
                val isMyOwnSeat = targetId == currentUser.displayId ||
                    targetName.equals(currentUser.nickname, ignoreCase = true) ||
                    targetName.equals("Sayed", ignoreCase = true)

                AlertDialog(
                    onDismissRequest = { clickedSeat = null },
                    containerColor = UnifiedModalSurface,
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(UnifiedModalLavenderHeader, UnifiedModalCardBg)
                                    )
                                )
                                .border(1.dp, UnifiedModalBorder, RoundedCornerShape(20.dp))
                                .padding(vertical = 14.dp, horizontal = 12.dp)
                        ) {
                            // Circular Avatar Preview on Account Card with Store Frame
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(UnifiedModalLavenderSoft)
                                    .border(
                                        2.4.dp,
                                        Brush.linearGradient(
                                            listOf(UnifiedRoyalPurple, UnifiedRoyalPink, Color(0xFFF59E0B))
                                        ),
                                        CircleShape
                                    )
                                    .clickable {
                                        clickedSeat = null
                                        onInspectSeatInnerAccount(seat)
                                    }
                            ) {
                                val cardAvatarUri = if (isMyOwnSeat) {
                                    currentUser.customAvatarUri ?: seat.occupantCustomAvatarUri
                                } else {
                                    seat.occupantCustomAvatarUri
                                }
                                if (!cardAvatarUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = Uri.parse(cardAvatarUri),
                                        contentDescription = targetName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(
                                            id = if (seat.occupantAvatarType == "PRINCESS") R.drawable.img_avatar_princess else R.drawable.img_avatar_prince
                                        ),
                                        contentDescription = targetName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = targetName,
                                color = UnifiedTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Account Details Badges Row (VIP, ID, Wealth Level, Room Role)
                            val cardVip = if (isMyOwnSeat) currentUser.vipTier else seat.occupantVipTier
                            val cardWealthLv = if (isMyOwnSeat) currentUser.wealthLevel else seat.occupantWealthLevel
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFFEF3C7))
                                        .border(0.8.dp, Color(0xFFF59E0B), RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "VIP $cardVip",
                                        color = Color(0xFFB45309),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFE0F2FE))
                                        .border(0.8.dp, Color(0xFF0284C7), RoundedCornerShape(50))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(targetId))
                                        }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ID: $targetId",
                                        color = Color(0xFF0369A1),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(UnifiedModalLavenderSoft)
                                        .border(0.8.dp, UnifiedRoyalPurple, RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isTargetAdmin) "🛡️ أدمن • LV.$cardWealthLv" else "LV.$cardWealthLv",
                                        color = UnifiedRoyalPurple,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // =========================================================
                            // CASE A: WHEN USER TOUCHES THEIR OWN ACCOUNT ON THE MIC
                            // (`وعند لمس المستخدم على حسابه يظهر له فقط النزول من على المايك وارسال هدية.`)
                            // =========================================================
                            if (isMyOwnSeat) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // 1. النزول من على المايك
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(14.dp))
                                            .clickable {
                                                onTakeOrLeaveSeat(seat.seatIndex)
                                                clickedSeat = null
                                            }
                                            .padding(vertical = 12.dp)
                                            .testTag("leave_my_mic_seat_button")
                                    ) {
                                        Text(
                                            text = "⬇️ النزول من المايك",
                                            color = Color(0xFFB91C1C),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    // 2. إرسال هدية
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(UnifiedRoyalPink, UnifiedRoyalPurple)
                                                )
                                            )
                                            .clickable {
                                                selectedGiftReceiver = targetName
                                                clickedSeat = null
                                                showGiftSheet = true
                                            }
                                            .padding(vertical = 12.dp)
                                            .testTag("card_send_gift_button")
                                    ) {
                                        Text(
                                            text = "🎁 إرسال هدية",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            } else {
                                // =========================================================
                                // CASE B: WHEN TOUCHING ANOTHER USER'S ACCOUNT
                                // 1. صلاحيات المستخدم الأساسية (متابعة + ملف شخصي + إرسال هدايا)
                                // =========================================================
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // متابعة الحساب
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isFollowingTarget) Color(0xFFD1FAE5)
                                                else UnifiedModalLavenderSoft
                                            )
                                            .border(
                                                1.dp,
                                                if (isFollowingTarget) Color(0xFF10B981) else UnifiedRoyalPurple,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                onFollowRoomUser(targetId, targetName)
                                            }
                                            .padding(vertical = 10.dp)
                                            .testTag("card_follow_user_button")
                                    ) {
                                        Text(
                                            text = if (isFollowingTarget) "✓ متابَع" else "➕ متابعة",
                                            color = if (isFollowingTarget) Color(0xFF065F46) else UnifiedRoyalPurple,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    // إرسال هدايا
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(UnifiedRoyalPink, UnifiedRoyalPurple)
                                                )
                                            )
                                            .clickable {
                                                selectedGiftReceiver = targetName
                                                clickedSeat = null
                                                showGiftSheet = true
                                            }
                                            .padding(vertical = 10.dp)
                                            .testTag("card_send_gift_button")
                                    ) {
                                        Text(
                                            text = "🎁 إرسال هدايا",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                // دخول الملف الشخصي المطابق للصفحات الأربع الخارجية
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(UnifiedModalCardBg)
                                        .border(1.dp, UnifiedModalBorder, RoundedCornerShape(12.dp))
                                        .clickable {
                                            clickedSeat = null
                                            onInspectSeatInnerAccount(seat)
                                        }
                                        .padding(vertical = 10.dp)
                                        .testTag("inspect_seat_inner_account_button")
                                ) {
                                    Text(
                                        text = "👤 عرض الملف الشخصي الكامل",
                                        color = UnifiedTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                // =========================================================
                                // 2. صلاحيات الأدمن وصاحب الغرفة (قفل مايك + طرد مستخدم)
                                // =========================================================
                                if (isRoomAdminOrOwner) {
                                    Text(
                                        text = "🛡️ صلاحيات الإدارة (أدمن / صاحب الغرفة):",
                                        color = UnifiedRoyalPurple,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // قفل / فتح المايك
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFE0F2FE))
                                                .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    onToggleLockSeat(seat.seatIndex)
                                                    clickedSeat = null
                                                }
                                                .padding(vertical = 9.dp)
                                                .testTag("admin_lock_mic_button")
                                        ) {
                                            Text(
                                                text = if (seat.isLocked) "🔓 فتح المايك" else "🔒 قفل المايك",
                                                color = Color(0xFF0369A1),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }

                                        // طرد مستخدم
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFEE2E2))
                                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    onKickUserFromSeat(seat.seatIndex, targetName)
                                                    clickedSeat = null
                                                }
                                                .padding(vertical = 9.dp)
                                                .testTag("admin_kick_user_button")
                                        ) {
                                            Text(
                                                text = "🚫 طرد مستخدم",
                                                color = Color(0xFFB91C1C),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }

                                // =========================================================
                                // 3. صلاحيات صاحب الغرفة الحصرية (تعيين أدمن + كتم + حظر + تعديل الغرفة)
                                // =========================================================
                                if (isRoomOwner) {
                                    Text(
                                        text = "👑 صلاحيات صاحب الغرفة الكاملة:",
                                        color = Color(0xFFD97706),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // تعيين / إلغاء أدمن
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(UnifiedModalLavenderSoft)
                                                .border(1.dp, UnifiedRoyalPurple, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    onAssignRoomAdmin(targetId, targetName)
                                                    clickedSeat = null
                                                }
                                                .padding(vertical = 9.dp)
                                                .testTag("owner_assign_admin_button")
                                        ) {
                                            Text(
                                                text = if (isTargetAdmin) "🛡️ إلغاء الأدمن" else "🛡️ تعيين أدمن",
                                                color = UnifiedRoyalPurple,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }

                                        // كتم / فك كتم المايك
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFEF3C7))
                                                .border(1.dp, Color(0xFFD97706), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    onMuteUserOnSeat(seat.seatIndex, targetName)
                                                    clickedSeat = null
                                                }
                                                .padding(vertical = 9.dp)
                                                .testTag("owner_mute_user_button")
                                        ) {
                                            Text(
                                                text = if (seat.isMuted) "🔊 فك الكتم" else "🔇 كتم المايك",
                                                color = Color(0xFFB45309),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // حظر من الغرفة
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFFE4E6))
                                                .border(1.dp, Color(0xFFE11D48), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    onBanUserFromRoom(seat.seatIndex, targetId, targetName)
                                                    clickedSeat = null
                                                }
                                                .padding(vertical = 9.dp)
                                                .testTag("owner_ban_user_button")
                                        ) {
                                            Text(
                                                text = "⛔ حظر نهائي",
                                                color = Color(0xFFBE123C),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }

                                        // تعديل صورة واسم وبيانات الغرفة
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFD1FAE5))
                                                .border(1.dp, Color(0xFF059669), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    clickedSeat = null
                                                    showEditRoomDialog = true
                                                }
                                                .padding(vertical = 9.dp)
                                                .testTag("owner_edit_room_from_card_button")
                                        ) {
                                            Text(
                                                text = "⚙️ بيانات الغرفة",
                                                color = Color(0xFF065F46),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { clickedSeat = null }) {
                            Text("إغلاق", color = UnifiedRoyalPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // =========================================================================
            // MODAL 4: ROOM OWNER SETTINGS — CHANGE ROOM PHOTO, NAME, MIC SHAPES & THEME
            // (`ويمكن تغيير شكل المايكات واي شكل واي شيء في الغرفه من قاعده البيانات لاحقا`)
            // =========================================================================
            if (showEditRoomDialog) {
                AlertDialog(
                    onDismissRequest = { showEditRoomDialog = false },
                    containerColor = UnifiedModalSurface,
                    title = {
                        Text(
                            text = "👑 إعدادات الغرفة وأشكال المايكات والثيمات",
                            color = UnifiedTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = editRoomTitleDraft,
                                onValueChange = { editRoomTitleDraft = it },
                                label = { Text("اسم الغرفة", color = UnifiedTextSecondary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = editRoomAnnouncementDraft,
                                onValueChange = { editRoomAnnouncementDraft = it },
                                label = { Text("إعلان وقوانين الغرفة", color = UnifiedTextSecondary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Mic Shape Selector (`تغيير شكل المايكات في الغرفة`)
                            Text(
                                text = "🎙️ شكل المايكات في الغرفة:",
                                color = UnifiedTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                RoomMicShapeStyle.entries.forEach { micStyle ->
                                    val isSelectedShape = activeMicShapeStyle == micStyle
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isSelectedShape) UnifiedModalLavenderHeader
                                                else UnifiedModalCardBg
                                            )
                                            .border(
                                                width = if (isSelectedShape) 1.8.dp else 1.dp,
                                                color = if (isSelectedShape) UnifiedRoyalPurple else UnifiedModalBorder,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                onChangeRoomMicShape(micStyle.id)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = micStyle.iconEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = micStyle.titleAr,
                                            color = UnifiedTextPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelectedShape) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Pick Room Cover Photo from Device Storage
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(UnifiedModalLavenderSoft)
                                    .border(1.dp, UnifiedRoyalPurple, RoundedCornerShape(12.dp))
                                    .clickable {
                                        roomCoverPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("pick_room_cover_from_storage_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = UnifiedRoyalPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تغيير صورة الغرفة من تخزين الجهاز",
                                    color = UnifiedRoyalPurple,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            // Open Animated & Transparent Chatroom Background Theme Selector
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(UnifiedRoyalPurple, UnifiedRoyalPink)
                                        )
                                    )
                                    .clickable {
                                        showEditRoomDialog = false
                                        showThemeSelectorSheet = true
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("open_theme_selector_from_settings_button")
                            ) {
                                Text(
                                    text = "🌌 اختيار أو شراء ثيم خلفية الغرفة المتحرك والشفاف",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onEditRoomNameAndPhoto(
                                    editRoomTitleDraft.ifBlank { "السلطان" },
                                    room.coverFrameType,
                                    editRoomCustomUriDraft
                                )
                                showEditRoomDialog = false
                            }
                        ) {
                            Text("حفظ التغييرات", color = UnifiedRoyalPurple, fontWeight = FontWeight.ExtraBold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditRoomDialog = false }) {
                            Text("إلغاء", color = UnifiedTextSecondary)
                        }
                    }
                )
            }

            // =========================================================================
            // MODAL 5: CHAT MESSAGE INPUT DIALOG (Unified Outer-4-Screens Light Lavender Card Style!)
            // =========================================================================
            if (showChatInputDialog) {
                AlertDialog(
                    onDismissRequest = { showChatInputDialog = false },
                    containerColor = UnifiedModalSurface,
                    title = {
                        Text(
                            text = "إرسال رسالة للغرفة 💬",
                            color = UnifiedTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = chatDraft,
                                onValueChange = { chatDraft = it },
                                placeholder = { Text("اكتب رسالتك...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("room_chat_input_field")
                            )
                            Text(
                                text = if (currentUser.equippedChatBubbleStyle.hasBorderFrame) {
                                    "✨ إطار الدردشة المفعّل من المتجر: ${currentUser.equippedChatBubbleStyle.titleAr}"
                                } else {
                                    "💡 الكتابة شفافة بدون إطار افتراضياً (يمكنك شراء إطار دردشة مضاعف من المتجر)"
                                },
                                color = UnifiedRoyalPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        IconButton(
                            onClick = {
                                if (chatDraft.isNotBlank()) {
                                    onSendChatMessage(chatDraft)
                                    chatDraft = ""
                                    showChatInputDialog = false
                                }
                            },
                            modifier = Modifier.testTag("send_room_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال",
                                tint = UnifiedRoyalPurple
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showChatInputDialog = false }) {
                            Text("إلغاء", color = UnifiedTextSecondary)
                        }
                    }
                )
            }

            // =========================================================================
            // MODAL 6: CHATROOM BACKGROUND THEME SELECTOR (ANIMATED, HIGH-QUALITY & TRANSPARENT)
            // Unified Outer-4-Screens Light Lavender Header & Crisp Card Container!
            // =========================================================================
            if (showThemeSelectorSheet) {
                val roomThemeStoreItems = remember(storeItems) {
                    storeItems.filter { it.category == StoreItemCategory.ROOM_THEMES }
                }

                AlertDialog(
                    onDismissRequest = { showThemeSelectorSheet = false },
                    containerColor = UnifiedModalSurface,
                    title = {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🌌 ثيمات وخلفيات الغرفة المتحركة والشفافة",
                                color = UnifiedTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Right
                            )
                            Text(
                                text = "اختر أو اشترِ ثيم خلفية متحرك وشفاف عالي الجودة لغرفتك • رصيدك: %,d 🪙".format(currentUser.goldCoins),
                                color = UnifiedRoyalPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                        }
                    },
                    text = {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                        ) {
                            items(RoomBackgroundTheme.entries, key = { it.id }) { themeOption ->
                                val storeMatch = roomThemeStoreItems.firstOrNull { it.roomThemeId == themeOption.id }
                                val isOwned = themeOption.priceCoins == 0L || (storeMatch?.isOwned == true)
                                val isEquipped = activeRoomTheme.id == themeOption.id

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    themeOption.topColor,
                                                    themeOption.midColor,
                                                    themeOption.bottomColor
                                                )
                                            )
                                        )
                                        .border(
                                            width = if (isEquipped) 2.dp else 1.dp,
                                            color = if (isEquipped) Color(0xFF34D399) else themeOption.accentGlowColor.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            if (storeMatch != null && !isOwned) {
                                                onBuyOrEquipRoomTheme(storeMatch)
                                            } else {
                                                onChangeRoomBackground(themeOption.id)
                                            }
                                            showThemeSelectorSheet = false
                                        }
                                        .padding(12.dp)
                                        .testTag("select_room_theme_${themeOption.id.lowercase()}")
                                ) {
                                    // Action Button (Left)
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                when {
                                                    isEquipped -> Brush.horizontalGradient(
                                                        listOf(Color(0xFF059669), Color(0xFF10B981))
                                                    )
                                                    isOwned -> Brush.horizontalGradient(
                                                        listOf(Color(0xFF9333EA), Color(0xFFEC4899))
                                                    )
                                                    else -> Brush.horizontalGradient(
                                                        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                                    )
                                                }
                                            )
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                isEquipped -> "مُفعّل ✓"
                                                isOwned -> "تفعيل الآن ✨"
                                                else -> "شراء ${themeOption.priceCoins} 🪙"
                                            },
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Theme Info & Animated Preview Icon (Right)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = themeOption.titleAr,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Right,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = themeOption.subtitleAr,
                                                color = Color.White.copy(alpha = 0.78f),
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Right,
                                                maxLines = 2
                                            )
                                            Text(
                                                text = if (isOwned) "ثيم شفاف متحرك HD • مملوك ✓"
                                                else "ثيم متجر شفاف متحرك • %,d 🪙".format(themeOption.priceCoins),
                                                color = themeOption.accentGlowColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(themeOption.accentGlowColor.copy(alpha = 0.22f))
                                                .border(1.2.dp, themeOption.accentGlowColor, RoundedCornerShape(12.dp))
                                        ) {
                                            Text(text = themeOption.iconEmoji, fontSize = 22.sp)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeSelectorSheet = false }) {
                            Text("إغلاق", color = UnifiedRoyalPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScreenshotMicSeatItem(
    seat: MicSeatState,
    displayNumber: Int,
    currentUser: UserProfile,
    micShapeStyle: RoomMicShapeStyle = RoomMicShapeStyle.ROYAL_CIRCLE,
    isAdminInRoom: Boolean = false,
    isHostOfRoom: Boolean = false,
    onSeatSingleTap: () -> Unit,
    onSeatDoubleTap: () -> Unit
) {
    val isOccupied = seat.occupantName != null
    val infiniteTransition = rememberInfiniteTransition(label = "mic_speaking_anim")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(680, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_pulse"
    )
    val outerWaveRadius by infiniteTransition.animateFloat(
        initialValue = 1.04f,
        targetValue = 1.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "speaking_outer_wave"
    )
    val rotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "speaking_ring_rotation"
    )

    val isMySeat = isOccupied && (
        seat.occupantId == currentUser.uuid ||
            seat.occupantId == currentUser.displayId ||
            seat.occupantDisplayId == currentUser.displayId ||
            seat.occupantName == currentUser.nickname
        )
    val customUri = if (isMySeat) {
        currentUser.customAvatarUri ?: seat.occupantCustomAvatarUri
    } else {
        seat.occupantCustomAvatarUri
    }
    val realOccupantName = if (isMySeat) {
        currentUser.nickname
    } else {
        seat.occupantName ?: "المايك $displayNumber"
    }
    val activeFrameStyle = if (isMySeat) {
        currentUser.frameStyle
    } else {
        seat.occupantFrame
    }

    val frameCrestEmoji = when {
        isHostOfRoom && isOccupied -> "👑"
        isAdminInRoom && isOccupied -> "🛡️"
        activeFrameStyle == AvatarFrameStyle.IMPERIAL_GOLD_WINGS -> "👑"
        activeFrameStyle == AvatarFrameStyle.CRYSTAL_DRAGON_ICE -> "🐉"
        activeFrameStyle == AvatarFrameStyle.ROSE_GOLD_PHOENIX -> "🦅"
        activeFrameStyle == AvatarFrameStyle.EMERALD_SULTAN_CROWN -> "💚"
        activeFrameStyle == AvatarFrameStyle.ROYAL_VIOLET_AURA -> "🔮"
        else -> micShapeStyle.iconEmoji
    }

    // Customizable Mic Shape from Database / Room Settings (`ويمكن تغيير شكل المايكات`)
    val seatShape: Shape = remember(micShapeStyle) {
        when (micShapeStyle) {
            RoomMicShapeStyle.CIRCLE,
            RoomMicShapeStyle.ROYAL_CIRCLE,
            RoomMicShapeStyle.CROWN_RING,
            RoomMicShapeStyle.CYBER_NEON_RING -> CircleShape
            RoomMicShapeStyle.ROYAL_HEXAGON,
            RoomMicShapeStyle.IMPERIAL_HEXAGON -> CutCornerShape(22)
            RoomMicShapeStyle.DIAMOND_CREST,
            RoomMicShapeStyle.DIAMOND_CRYSTAL -> CutCornerShape(32)
            RoomMicShapeStyle.ROUNDED_SHIELD,
            RoomMicShapeStyle.GOLDEN_CROWN_THRONE -> RoundedCornerShape(16.dp)
            RoomMicShapeStyle.LOTUS_BLOSSOM -> RoundedCornerShape(20.dp)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(66.dp)
            .combinedClickable(
                onClick = onSeatSingleTap,
                onDoubleClick = onSeatDoubleTap
            )
            .testTag("mic_seat_${seat.seatIndex}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(62.dp)
        ) {
            // 1. Multi-Ring Animated Sound-Wave Rings when speaking or active
            if (isOccupied && !seat.isMuted) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val baseRadius = (size.minDimension / 2f) * 0.84f
                    if (seat.isSpeaking) {
                        // Outer expanding sonar ripple ring
                        drawCircle(
                            color = activeFrameStyle.primaryColor.copy(alpha = 0.24f),
                            radius = baseRadius * outerWaveRadius,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Middle pulsing neon-gold ring
                        drawCircle(
                            color = micShapeStyle.ringPrimaryColor.copy(alpha = 0.42f),
                            radius = baseRadius * pulseRadius,
                            style = Stroke(width = 3.2.dp.toPx())
                        )
                    }
                    // Rotating multi-color sound energy ring
                    rotate(rotationDeg) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(
                                    activeFrameStyle.primaryColor,
                                    micShapeStyle.ringPrimaryColor,
                                    micShapeStyle.ringSecondaryColor,
                                    Color(0xFFFFD700),
                                    activeFrameStyle.primaryColor
                                )
                            ),
                            radius = baseRadius,
                            style = Stroke(width = if (seat.isSpeaking) 2.8.dp.toPx() else 1.8.dp.toPx())
                        )
                    }
                }
            } else if (!isOccupied && !seat.isLocked) {
                // Subtle interactive breathing ring on open mic slots tinted with micShapeStyle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val baseRadius = (size.minDimension / 2f) * 0.84f
                    drawCircle(
                        color = micShapeStyle.ringPrimaryColor.copy(alpha = 0.24f),
                        radius = baseRadius * (0.95f + (pulseRadius - 1f) * 0.35f),
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                }
            }

            // 2. Dynamic Store-Purchased Outer Avatar Frame (`إطارات المتجر الديناميكية حول المايك`)
            if (isOccupied) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val outerFrameR = size.minDimension / 2f - 2.5.dp.toPx()
                    rotate(-rotationDeg * 0.5f) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(
                                    activeFrameStyle.primaryColor,
                                    activeFrameStyle.secondaryColor,
                                    Color(0xFFFFF59D),
                                    activeFrameStyle.primaryColor
                                )
                            ),
                            radius = outerFrameR,
                            style = Stroke(width = 3.2.dp.toPx())
                        )
                    }
                    // 4 Corner Store Frame Gem Sparkles
                    val gemOffsets = listOf(
                        Offset(size.width * 0.16f, size.height * 0.16f),
                        Offset(size.width * 0.84f, size.height * 0.16f),
                        Offset(size.width * 0.16f, size.height * 0.84f),
                        Offset(size.width * 0.84f, size.height * 0.84f)
                    )
                    gemOffsets.forEach { pt ->
                        drawCircle(
                            color = Color(0xFFFFF59D),
                            radius = 2.6.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = activeFrameStyle.primaryColor,
                            radius = 1.5.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // 3. Main Mic Container using Database-Assignable Shape (`seatShape`)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clip(seatShape)
                    .background(
                        if (isOccupied) Color(0xFF191D2C)
                        else micShapeStyle.ringPrimaryColor.copy(alpha = 0.16f)
                    )
                    .border(
                        width = 1.4.dp,
                        brush = Brush.linearGradient(
                            if (isOccupied) {
                                listOf(activeFrameStyle.primaryColor, activeFrameStyle.secondaryColor)
                            } else {
                                listOf(
                                    micShapeStyle.ringPrimaryColor.copy(alpha = 0.65f),
                                    micShapeStyle.ringSecondaryColor.copy(alpha = 0.45f)
                                )
                            }
                        ),
                        shape = seatShape
                    )
            ) {
                if (isOccupied) {
                    if (!customUri.isNullOrBlank()) {
                        AsyncImage(
                            model = Uri.parse(customUri),
                            contentDescription = realOccupantName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                id = if (seat.occupantAvatarType == "PRINCESS") R.drawable.img_avatar_princess else R.drawable.img_avatar_prince
                            ),
                            contentDescription = realOccupantName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else if (seat.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "مقفل",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "مايك $displayNumber",
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Store Frame / Role Top Crest Badge on Occupied Seat
            if (isOccupied) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1035))
                        .border(0.8.dp, activeFrameStyle.primaryColor, CircleShape)
                ) {
                    Text(text = frameCrestEmoji, fontSize = 9.sp)
                }
            }

            // Muted Mic Badge on Occupied Seat
            if (isOccupied && seat.isMuted) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626))
                        .border(1.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "مكتوم",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            // 4. Animated 3D Emoji Above User Photo (`والاموجي المتحرك يظهر فوق الصور يكون متحرك ومميز جدا`)
            val activeEmoji = seat.activeReactionEmoji
            if (!activeEmoji.isNullOrBlank()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-14).dp)
                ) {
                    AnimatedSeatEmojiOverlay(
                        emoji = activeEmoji,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 5. Label below circle:
        if (isOccupied) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.52f))
                    .border(0.6.dp, activeFrameStyle.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "🪙",
                    fontSize = 8.sp
                )
                Text(
                    text = seat.giftCoinsCounter.toString(),
                    color = Color(0xFFFBBF24),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = realOccupantName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = displayNumber.toString(),
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Animated, High-Quality, and Transparent Chatroom Background Renderer (`خلفيات وثيمات الغرف المتحركة والشفافة عالية الجودة`).
 * Supports all 6 `RoomBackgroundTheme` styles with smooth particle animations, aurora waves, crescent moon, and translucent glass depth.
 */
@Composable
private fun AnimatedTransparentRoomThemeCanvas(
    theme: RoomBackgroundTheme,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "room_theme_anim")
    val floatPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "theme_float_phase"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "theme_glow_pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. High-Quality Deep Atmospheric Gradient Base
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    theme.topColor,
                    theme.midColor,
                    theme.bottomColor
                )
            )
        )

        // 2. Translucent Glassmorphic Aurora & Nebula Waves
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    theme.accentGlowColor.copy(alpha = 0.26f * glowPulse),
                    theme.secondaryGlowColor.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(w * 0.50f, h * 0.15f),
                radius = w * 0.55f
            ),
            center = Offset(w * 0.50f, h * 0.15f),
            radius = w * 0.55f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    theme.secondaryGlowColor.copy(alpha = 0.18f * glowPulse),
                    Color.Transparent
                ),
                center = Offset(w * (0.25f + 0.5f * floatPhase), h * 0.35f),
                radius = w * 0.42f
            ),
            center = Offset(w * (0.25f + 0.5f * floatPhase), h * 0.35f),
            radius = w * 0.42f
        )

        // 3. Theme-Specific Centerpiece (Crescent Moon / Aurora / Imperial Crown Aura / Sapphire Starlight)
        val moonCenter = Offset(w * 0.50f, h * 0.145f)
        val moonRadius = w * 0.085f
        drawCircle(
            color = if (theme == RoomBackgroundTheme.PALACE_NIGHT || theme == RoomBackgroundTheme.ROYAL_GOLD_PALACE) {
                Color(0xFFFFF3C4)
            } else {
                theme.accentGlowColor.copy(alpha = 0.85f)
            },
            radius = moonRadius,
            center = moonCenter
        )
        drawCircle(
            color = theme.topColor,
            radius = moonRadius * 0.86f,
            center = Offset(moonCenter.x - moonRadius * 0.36f, moonCenter.y - moonRadius * 0.18f)
        )

        // 4. Soft Translucent Glass Clouds around top stage
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    theme.accentGlowColor.copy(alpha = 0.20f),
                    Color.Transparent
                ),
                center = Offset(w * 0.32f, h * 0.18f),
                radius = w * 0.28f
            ),
            center = Offset(w * 0.32f, h * 0.18f),
            radius = w * 0.28f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    theme.secondaryGlowColor.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(w * 0.68f, h * 0.17f),
                radius = w * 0.26f
            ),
            center = Offset(w * 0.68f, h * 0.17f),
            radius = w * 0.26f
        )

        // 5. Animated Floating Starlight & Transparent Crystal Orbs
        for (i in 0 until 18) {
            val seedX = ((i * 41) % 100) / 100f
            val baseSeedY = ((i * 59) % 100) / 100f
            val animY = ((baseSeedY - floatPhase + 1f) % 1f)
            val px = w * seedX
            val py = h * animY
            val particleColor = if (i % 2 == 0) theme.accentGlowColor else theme.secondaryGlowColor
            drawCircle(
                color = particleColor.copy(alpha = 0.45f * glowPulse),
                radius = if (i % 3 == 0) 4.2f else 2.5f,
                center = Offset(px, py)
            )
        }
    }
}

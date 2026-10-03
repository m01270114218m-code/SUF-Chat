package com.voicerooms.app.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.voicerooms.app.data.model.Gift
import com.voicerooms.app.data.model.RoomMember
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.GiftAnimationOverlay
import com.voicerooms.app.ui.components.GiftSheet
import com.voicerooms.app.ui.components.LoadingBox
import com.voicerooms.app.ui.components.RoleBadge
import com.voicerooms.app.ui.components.UserAvatar
import com.voicerooms.app.ui.components.formatNumber
import com.voicerooms.app.ui.theme.Amber
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.Border
import com.voicerooms.app.ui.theme.CardDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.Green
import com.voicerooms.app.ui.theme.Purple
import com.voicerooms.app.ui.theme.Red
import com.voicerooms.app.ui.theme.SurfaceDark
import com.voicerooms.app.ui.theme.TextHint
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.AuthViewModel
import com.voicerooms.app.ui.viewmodel.RoomViewModel
import com.voicerooms.app.voice.VoiceRoomController
import com.voicerooms.app.voice.VoiceService
import kotlin.math.ceil

/**
 * شاشة الغرفة الصوتية الكاملة:
 *  - شبكة المقاعد مع أدوار المستخدمين (مالك/مشرف/عضو) ومؤشرات الكتم والتحدّث ورفع اليد.
 *  - الشات اللحظي داخل الغرفة.
 *  - أزرار التحكم: كتم/فتح الميكروفون، رفع/إنزال اليد، مكبر الصوت، الهدايا، الخروج.
 *  - أدوات المشرف: كتم إجباري، طرد، حظر، تغيير الدور، قفل الغرفة، قفل الميكروفون.
 *  - الصوت الفعلي عبر WebRTC ([VoiceRoomController]) + خدمة أمامية ([VoiceService]).
 *  - عرض أنيميشن الهدايا المتحركة SVGA ([GiftAnimationOverlay]).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RoomScreen(
    roomId: String,
    onBack: () -> Unit,
) {
    val vm: RoomViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val authVm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    val authState by authVm.state.collectAsStateWithLifecycle()

    val myId = remember { authVm.currentUserId ?: "" }
    val myName = authState.user?.name ?: "أنا"

    // ---- إذن الميكروفون + الإشعارات ----
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val notifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null
    LaunchedEffect(Unit) {
        if (!micPermission.status.isGranted) micPermission.launchPermissionRequest()
        notifPermission?.let { if (!it.status.isGranted) it.launchPermissionRequest() }
    }

    // ---- محرّك الصوت ----
    var controller by remember { mutableStateOf<VoiceRoomController?>(null) }
    var peerCount by remember { mutableIntStateOf(0) }
    var speakerOn by remember { mutableStateOf(true) }
    var exited by remember { mutableStateOf(false) }

    // ---- حوارات ----
    var showPasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var adminTarget by remember { mutableStateOf<RoomMember?>(null) }
    var showRoomSettings by remember { mutableStateOf(false) }

    // ---- الدخول للغرفة ----
    LaunchedEffect(roomId) {
        vm.enter(roomId, myId, null) { showPasswordDialog = true }
    }

    // ---- بدء جلسة الصوت بعد تحميل الغرفة ----
    LaunchedEffect(state.room?.id) {
        val room = state.room ?: return@LaunchedEffect
        if (controller == null && !exited) {
            val c = VoiceRoomController(context.applicationContext, roomId, myId)
            c.onPeerCount = { peerCount = it }
            c.start()
            controller = c
            VoiceService.start(context, room.name)
        }
    }

    // ---- مزامنة الكتم مع المحرّك ----
    LaunchedEffect(state.isMuted) { controller?.setMuted(state.isMuted) }

    // ---- إظهار الأخطاء ----
    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearError()
        }
    }

    // ---- الخروج ----
    val exit: () -> Unit = {
        if (!exited) {
            exited = true
            controller?.stop()
            controller = null
            VoiceService.stop(context)
            vm.leave { onBack() }
        }
    }
    BackHandler { exit() }

    DisposableEffect(Unit) {
        onDispose {
            controller?.stop()
            controller = null
            VoiceService.stop(context)
        }
    }

    val isAdmin = state.myMember?.isAdmin == true || state.room?.ownerId == myId

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        if (state.loading && state.room == null) {
            LoadingBox()
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                RoomTopBar(
                    roomName = state.room?.name ?: "غرفة صوتية",
                    listeners = state.room?.listeners ?: 0,
                    peerCount = peerCount,
                    isLocked = state.room?.isLocked == true,
                    isAdmin = isAdmin,
                    onBack = exit,
                    onSettings = { showRoomSettings = true },
                )

                SeatsGrid(
                    maxSeats = state.room?.maxSeats ?: 8,
                    members = state.members,
                    myId = myId,
                    isAdmin = isAdmin,
                    micLocked = state.room?.micLocked == true,
                    onEmptySeatClick = { seat -> if (!state.onSeat) vm.takeSeat(seat) },
                    onMemberClick = { member ->
                        if (isAdmin && member.userId != myId) adminTarget = member
                    },
                )

                ChatList(
                    messages = state.messages,
                    modifier = Modifier.weight(1f),
                )

                ChatInput(
                    onSend = { vm.sendMessage(it) },
                )

                RoomControls(
                    onSeat = state.onSeat,
                    isMuted = state.isMuted,
                    isHandUp = state.isHandUp,
                    speakerOn = speakerOn,
                    onToggleMute = { vm.toggleMute() },
                    onToggleHand = { vm.toggleHand() },
                    onToggleSpeaker = {
                        speakerOn = !speakerOn
                        controller?.setSpeaker(speakerOn)
                    },
                    onGift = { vm.showGifts(true) },
                    onLeave = exit,
                    onTakeSeat = { vm.takeSeat(firstFreeSeat(state.members)) },
                    onLeaveSeat = { vm.leaveSeat() },
                )
            }
        }

        // ---- أنيميشن الهدية المتحركة (SVGA) ----
        GiftAnimationOverlay(
            visible = state.activeGiftAnim != null,
            animationUrl = state.activeGiftAnim?.animationUrl,
            senderName = myName,
            receiverName = state.room?.owner?.name,
            giftName = state.activeGiftAnim?.name,
            onFinished = { vm.clearGiftAnim() },
        )
    }

    // ---- لوحة الهدايا ----
    if (state.showGiftSheet) {
        GiftSheet(
            gifts = state.gifts,
            coins = authState.user?.coins ?: 0L,
            receiverName = state.room?.owner?.name ?: "المالك",
            onSelect = { gift: Gift -> vm.sendGift(gift, state.room?.ownerId ?: "") },
            onDismiss = { vm.showGifts(false) },
        )
    }

    // ---- أدوات المشرف على عضو ----
    adminTarget?.let { target ->
        AdminActionsSheet(
            target = target,
            onMute = { mute -> vm.adminMute(target.userId, mute); adminTarget = null },
            onKick = { vm.kick(target.userId); adminTarget = null },
            onBan = { vm.ban(target.userId); adminTarget = null },
            onSetRole = { role -> vm.setRole(target.userId, role); adminTarget = null },
            onDismiss = { adminTarget = null },
        )
    }

    // ---- إعدادات الغرفة (للمشرف) ----
    if (showRoomSettings) {
        RoomSettingsSheet(
            isLocked = state.room?.isLocked == true,
            micLocked = state.room?.micLocked == true,
            onToggleLock = { vm.toggleRoomLock() },
            onToggleMicLock = { vm.toggleMicLock() },
            onDismiss = { showRoomSettings = false },
        )
    }

    // ---- حوار كلمة مرور الغرفة ----
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            containerColor = SurfaceDark,
            title = { Text("غرفة محمية", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("هذه الغرفة تتطلب كلمة مرور للدخول", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = Border,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = Cyan,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    vm.enter(roomId, myId, password) { showPasswordDialog = true }
                }) { Text("دخول", color = Cyan) }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false; onBack() }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
        )
    }
}

/** أول مقعد فارغ متاح. */
private fun firstFreeSeat(members: List<RoomMember>): Int {
    val taken = members.filter { it.seatIndex != null }.mapNotNull { it.seatIndex }.toSet()
    var i = 0
    while (taken.contains(i)) i++
    return i
}

// ====================================================================== الشريط العلوي
@Composable
private fun RoomTopBar(
    roomName: String,
    listeners: Long,
    peerCount: Int,
    isLocked: Boolean,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Purple.copy(alpha = 0.35f), CardDark)))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(roomName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (isLocked) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Amber, modifier = Modifier.size(14.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Headset, contentDescription = null, tint = Cyan, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "متصل صوتيًا: $peerCount · مستمعون: ${formatNumber(listeners)}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }
        if (isAdmin) {
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "إعدادات الغرفة", tint = Cyan)
            }
        }
    }
}

// ====================================================================== شبكة المقاعد
@Composable
private fun SeatsGrid(
    maxSeats: Int,
    members: List<RoomMember>,
    myId: String,
    isAdmin: Boolean,
    micLocked: Boolean,
    onEmptySeatClick: (Int) -> Unit,
    onMemberClick: (RoomMember) -> Unit,
) {
    val cols = 4
    val rows = ceil(maxSeats / cols.toDouble()).toInt()
    val bySeat = members.filter { it.seatIndex != null }.associateBy { it.seatIndex!! }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (c in 0 until cols) {
                    val index = r * cols + c
                    if (index < maxSeats) {
                        val member = bySeat[index]
                        if (member != null) {
                            SeatItem(
                                member = member,
                                isMe = member.userId == myId,
                                onClick = { onMemberClick(member) },
                            )
                        } else {
                            EmptySeat(
                                seatNumber = index + 1,
                                enabled = !micLocked,
                                onClick = { onEmptySeatClick(index) },
                            )
                        }
                    } else {
                        Spacer(Modifier.width(64.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SeatItem(member: RoomMember, isMe: Boolean, onClick: () -> Unit) {
    val ringColor = when {
        member.isSpeaking -> Green
        member.isMuted -> TextHint
        else -> Cyan
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(2.dp, ringColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                UserAvatar(
                    url = member.profile?.avatarUrl,
                    name = member.profile?.name ?: "?",
                    size = 48.dp,
                )
            }
            if (member.isMuted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Red),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.MicOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
            if (member.isHandUp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Amber),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.PanTool, contentDescription = null, tint = Color.Black, modifier = Modifier.size(11.dp))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isMe) "أنت" else (member.profile?.name ?: "عضو"),
                color = if (isMe) Cyan else TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        RoleBadge(member.role)
    }
}

@Composable
private fun EmptySeat(seatNumber: Int, enabled: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(CardDark)
                .border(1.dp, Border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "مقعد فارغ",
                tint = if (enabled) TextHint else Border,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text("مقعد $seatNumber", color = TextHint, fontSize = 11.sp)
    }
}

// ====================================================================== الشات
@Composable
private fun ChatList(messages: List<com.voicerooms.app.data.model.RoomMessage>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(vertical = 6.dp),
    ) {
        items(messages) { msg ->
            Row(verticalAlignment = Alignment.Top) {
                UserAvatar(
                    url = msg.profile?.avatarUrl,
                    name = msg.profile?.name ?: "?",
                    size = 26.dp,
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = msg.profile?.name ?: "عضو",
                        color = Cyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardDark)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(msg.content ?: "", color = TextPrimary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInput(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("اكتب رسالة…", color = TextHint) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send, keyboardType = KeyboardType.Text),
            keyboardActions = KeyboardActions(onSend = {
                if (text.isNotBlank()) { onSend(text); text = "" }
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple,
                unfocusedBorderColor = Border,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Cyan,
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark,
            ),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(Purple, Cyan)))
                .clickable { if (text.isNotBlank()) { onSend(text); text = "" } },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

// ====================================================================== أزرار التحكم
@Composable
private fun RoomControls(
    onSeat: Boolean,
    isMuted: Boolean,
    isHandUp: Boolean,
    speakerOn: Boolean,
    onToggleMute: () -> Unit,
    onToggleHand: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onGift: () -> Unit,
    onLeave: () -> Unit,
    onTakeSeat: () -> Unit,
    onLeaveSeat: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlButton(
            icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
            label = if (isMuted) "كتم" else "صوت",
            active = !isMuted,
            onClick = onToggleMute,
        )
        ControlButton(
            icon = Icons.Filled.PanTool,
            label = "اليد",
            active = isHandUp,
            onClick = onToggleHand,
        )
        ControlButton(
            icon = if (speakerOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
            label = "المكبر",
            active = speakerOn,
            onClick = onToggleSpeaker,
        )
        ControlButton(
            icon = Icons.Filled.CardGiftcard,
            label = "هدية",
            active = false,
            onClick = onGift,
        )
        if (onSeat) {
            ControlButton(
                icon = Icons.Filled.PersonRemove,
                label = "نزول",
                active = false,
                onClick = onLeaveSeat,
            )
        } else {
            ControlButton(
                icon = Icons.Filled.Person,
                label = "صعود",
                active = false,
                onClick = onTakeSeat,
            )
        }
        ControlButton(
            icon = Icons.Filled.CallEnd,
            label = "خروج",
            active = false,
            tint = Red,
            onClick = onLeave,
        )
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    tint: Color = Cyan,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (active) Brush.horizontalGradient(listOf(Purple, Cyan)) else Brush.linearGradient(listOf(CardDark, CardDark)))
                .border(1.dp, if (active) Color.Transparent else Border, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = if (active) Color.White else tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(3.dp))
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

// ====================================================================== لوحة أدوات المشرف
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminActionsSheet(
    target: RoomMember,
    onMute: (Boolean) -> Unit,
    onKick: () -> Unit,
    onBan: () -> Unit,
    onSetRole: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(url = target.profile?.avatarUrl, name = target.profile?.name ?: "?", size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(target.profile?.name ?: "عضو", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("الدور الحالي: ", color = TextSecondary, fontSize = 12.sp)
                        RoleBadge(target.role)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            AdminRow(Icons.Filled.MicOff, if (target.isMuted) "إلغاء الكتم" else "كتم إجباري") {
                onMute(!target.isMuted)
            }
            AdminRow(Icons.Filled.AdminPanelSettings, "ترقية إلى مشرف") { onSetRole("admin") }
            AdminRow(Icons.Filled.Star, "تعيين كمشرف عام") { onSetRole("moderator") }
            AdminRow(Icons.Filled.Person, "إرجاع كعضو عادي") { onSetRole("member") }
            AdminRow(Icons.Filled.PersonRemove, "طرد من الغرفة", danger = true) { onKick() }
            AdminRow(Icons.Filled.Block, "حظر نهائي", danger = true) { onBan() }
        }
    }
}

@Composable
private fun AdminRow(icon: ImageVector, text: String, danger: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (danger) Red else Cyan, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = if (danger) Red else TextPrimary, fontSize = 15.sp)
    }
}

// ====================================================================== إعدادات الغرفة
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomSettingsSheet(
    isLocked: Boolean,
    micLocked: Boolean,
    onToggleLock: () -> Unit,
    onToggleMicLock: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text("إعدادات الغرفة", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))
            AdminRow(
                icon = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                text = if (isLocked) "الغرفة مقفلة (اضغط للفتح)" else "قفل الغرفة",
            ) { onToggleLock() }
            AdminRow(
                icon = if (micLocked) Icons.Filled.MicOff else Icons.Filled.Mic,
                text = if (micLocked) "الميكروفونات مقفلة (اضغط للفتح)" else "قفل الميكروفونات",
            ) { onToggleMicLock() }
        }
    }
}

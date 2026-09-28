package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.DynamicThemeManager
import com.example.config.MasterAppDatabaseTable
import com.example.config.glossy3DButton
import com.example.config.ornateGolden3DContainer
import com.example.models.ChargeAgentRewardItem
import com.example.models.ChargeAgentShipmentLog
import com.example.models.DailyTaskItem
import com.example.models.HostAgencyMemberRecord
import com.example.models.LeaderboardEntry
import com.example.models.StoreItemCategory
import com.example.models.UserProfile
import com.example.ui.components.ColoredUserNameText
import com.example.ui.components.Metallic3DBadgeTag
import com.example.ui.components.Ornate3DAvatarWithFrame
import com.example.ui.components.PalaceNightAnimatedBackground

/**
 * 1. Standalone Full-Screen Daily Tasks & 3D Lucky Wheel (`المهام اليومية وعجلة الحظ المستقلة 🎯🎡`)
 */
@Composable
fun StandaloneDailyTasksScreen(
    userProfile: UserProfile,
    dailyTasks: List<DailyTaskItem>,
    onClaimCheckIn: () -> Unit,
    onClaimTask: (DailyTaskItem) -> Unit,
    onSpinWheel: (Long) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var wheelRotationTarget by remember { mutableFloatStateOf(0f) }
    val animatedWheelAngle by animateFloatAsState(
        targetValue = wheelRotationTarget,
        animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
        label = "wheel_spin"
    )
    val prizes = listOf(500L, 1000L, 2500L, 5000L, 8888L, 15000L)

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.46f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "المهام اليومية وعجلة الحظ 🎡",
                subtitleAr = "تسجيل الحضور • عجلة الذهب 3D • مهام الغرف الصوتية",
                onBack = onBack
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 7-Day Check-In Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 22.dp)
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "📅 تسجيل الحضور الملكي (${userProfile.checkInStreakDays}/7 أيام)",
                                color = Color(0xFFFFF59D),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Box(
                                modifier = Modifier
                                    .glossy3DButton(cornerRadius = 50.dp)
                                    .clickable(enabled = !userProfile.isCheckedInToday) { onClaimCheckIn() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (userProfile.isCheckedInToday) "تم الاستلام اليوم ✓" else "استلام +1,500 🪙",
                                    color = Color(0xFF1A0736),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // 3D Lucky Spin Wheel
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 22.dp)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "🎡 عجلة الحظ الإمبراطورية 7D",
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(150.dp)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(animatedWheelAngle)
                            ) {
                                val sweep = 60f
                                val colors = listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFF8E24AA),
                                    Color(0xFF00E5FF),
                                    Color(0xFFFF4081),
                                    Color(0xFF00E676),
                                    Color(0xFFFF9100)
                                )
                                for (i in 0 until 6) {
                                    drawArc(
                                        color = colors[i],
                                        startAngle = i * sweep,
                                        sweepAngle = sweep,
                                        useCenter = true
                                    )
                                }
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A0736))
                                    .border(2.dp, Color(0xFFFFD700), CircleShape)
                            ) {
                                Text("👑", fontSize = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glossy3DButton(cornerRadius = 50.dp)
                                .clickable {
                                    wheelRotationTarget += 720f + (0..300).random()
                                    onSpinWheel(prizes.random())
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "تدوير عجلة الحظ الذهبية مجاناً 🎰",
                                color = Color(0xFF1A0736),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Daily Tasks List
                items(dailyTasks, key = { it.id }) { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF230A48))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(task.iconEmoji, fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(task.titleAr, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                Text(task.subtitleAr, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                                Text(
                                    text = "المكافأة: +${task.rewardCoins} 🪙  •  +${task.rewardExp} EXP",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .glossy3DButton(cornerRadius = 50.dp)
                                .clickable(enabled = !task.isClaimed) { onClaimTask(task) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = if (task.isClaimed) "مكتمل ✓" else "استلام 🎁",
                                color = Color(0xFF1A0736),
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

/**
 * 2. Standalone Full-Screen My Agency & Host Data Screen (`وكالتي: مركز الوكالة للوكيل أو بيانات المضيف للمضيف 🏢🎙️`)
 * - For Host Agent (`وكيل المضيفين`):
 *   - Can send an invitation by User ID (`إرسال دعوة إلى أي مستخدم ليصبح مضيفاً في وكالته`)
 *   - Displays every host's live status on the mic & inside the app, total gifts value, and hours (`تظهر في مركز الوكالة للوكيل بيانات المستخدم على المايك وداخل التطبيق وقيمة الهدايا والساعات`)
 * - For Host (`المضيف`):
 *   - Displays ONLY the Host's own personal stats card (`تظهر للمضيف بيانات المضيف وليس وكالة وبداخلها معلوماته`)
 */
@Composable
fun StandaloneAgenciesScreen(
    userProfile: UserProfile,
    hostMembers: List<HostAgencyMemberRecord>,
    onInviteUserToAgencyById: (userId: String, hostName: String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    // View mode: 0 = مركز الوكالة للوكيل (Agent Center), 1 = بيانات المضيف فقط (Host Personal Data)
    var selectedViewMode by remember {
        mutableIntStateOf(if (userProfile.isHostAgent) 0 else 1)
    }
    var inviteUserIdInput by remember { mutableStateOf("") }
    var inviteHostNameInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.48f, showFireworks = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = if (selectedViewMode == 0) "مركز الوكالة للوكيل 🏢" else "بيانات المضيف الرسمية 🎙️",
                subtitleAr = if (selectedViewMode == 0) {
                    "${userProfile.agencyName} • دعوة المضيفين ومتابعة الساعات والهدايا"
                } else {
                    "معلومات المضيف الشخصية • الساعات على المايك • قيمة الهدايا"
                },
                onBack = onBack
            )

            // Role View Switcher: Shown ONLY if the database granted this user BOTH Host Agent and Approved Host roles.
            // If the user is ONLY a Host (`isApprovedHost && !isHostAgent`), they see strictly their Host Personal Data (`بيانات المضيف وليس وكالة`).
            if (userProfile.isHostAgent && userProfile.isApprovedHost) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selectedViewMode == 0) DynamicThemeManager.goldVertical3DBrush
                                else Brush.verticalGradient(listOf(Color(0xFF260E52), Color(0xFF160634)))
                            )
                            .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                            .clickable { selectedViewMode = 0 }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "🏢 مركز الوكالة (للوكيل)",
                            color = if (selectedViewMode == 0) Color(0xFF1A0736) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selectedViewMode == 1) DynamicThemeManager.goldVertical3DBrush
                                else Brush.verticalGradient(listOf(Color(0xFF260E52), Color(0xFF160634)))
                            )
                            .border(1.2.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp))
                            .clickable { selectedViewMode = 1 }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "🎙️ بيانات المضيف (للمضيف)",
                            color = if (selectedViewMode == 1) Color(0xFF1A0736) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            if (selectedViewMode == 0) {
                // =========================================================================
                // HOST AGENT VIEW: Invite Hosts by ID + Live Telemetry of Agency Hosts
                // =========================================================================
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 1. Invite Any User by ID to become a Host in this Agency
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .ornateGolden3DContainer(cornerRadius = 22.dp)
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "إرسال دعوة لمستخدم ليصبح مضيفاً في وكالتك",
                                    color = Color(0xFFFFF59D),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "أدخل الـ ID الخاص بالمستخدم لإرسال دعوة وكالة رسمية وربطه بمركز وكالتك:",
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = inviteUserIdInput,
                                    onValueChange = { inviteUserIdInput = it },
                                    label = { Text("آي دي المستخدم (ID)", color = Color(0xFFFFF59D)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFD700),
                                        unfocusedBorderColor = Color(0xFF7E57C2)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = inviteHostNameInput,
                                    onValueChange = { inviteHostNameInput = it },
                                    label = { Text("اسم المضيف (اختياري)", color = Color(0xFFFFF59D)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFFD700),
                                        unfocusedBorderColor = Color(0xFF7E57C2)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glossy3DButton(cornerRadius = 50.dp)
                                    .clickable {
                                        if (inviteUserIdInput.isNotBlank()) {
                                            onInviteUserToAgencyById(
                                                inviteUserIdInput.trim(),
                                                inviteHostNameInput.trim()
                                            )
                                            inviteUserIdInput = ""
                                            inviteHostNameInput = ""
                                        }
                                    }
                                    .padding(vertical = 11.dp)
                            ) {
                                Text(
                                    text = "إرسال دعوة انضمام للوكالة الآن 📨",
                                    color = Color(0xFF1A0736),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // 2. Agency Summary Telemetry Banner
                    item {
                        val totalGifts = hostMembers.sumOf { it.totalGiftDiamondsReceived }
                        val totalHours = hostMembers.sumOf { it.totalMicHours }
                        val activeOnMicCount = hostMembers.count { it.isOnMicNow }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AgencyMetricBox(
                                label = "مضيفين على المايك الآن",
                                value = "$activeOnMicCount / ${hostMembers.size} 🎙️",
                                accentColor = Color(0xFF00E676),
                                modifier = Modifier.weight(1f)
                            )
                            AgencyMetricBox(
                                label = "إجمالي هدايا المضيفين",
                                value = "%,d 💎".format(totalGifts),
                                accentColor = Color(0xFF00E5FF),
                                modifier = Modifier.weight(1f)
                            )
                            AgencyMetricBox(
                                label = "إجمالي ساعات المايك",
                                value = "$totalHours ساعة ⏱️",
                                accentColor = Color(0xFFFFD700),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Text(
                            text = "📋 بيانات مضيفي الوكالة (على المايك • داخل التطبيق • الهدايا • الساعات):",
                            color = Color(0xFFFFF59D),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    items(hostMembers, key = { it.userId }) { member ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF220A47))
                                .border(
                                    width = 1.4.dp,
                                    color = if (member.isOnMicNow) Color(0xFF00E676) else Color(0xFFFFD700).copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Ornate3DAvatarWithFrame(
                                        avatarType = member.avatarType,
                                        frameStyle = member.frameStyle,
                                        size = 56.dp,
                                        isSpeaking = member.isOnMicNow,
                                        showCrown = true
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        ColoredUserNameText(
                                            name = member.nickname,
                                            primaryColor = member.frameStyle.primaryColor,
                                            secondaryColor = Color(0xFF00E5FF),
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "ID: ${member.userId}",
                                            color = Color(0xFFFFF59D),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (member.isOnMicNow) "🎙️ ${member.currentRoomNameAr}" else "🟢 ${member.currentRoomNameAr}",
                                            color = if (member.isOnMicNow) Color(0xFF00E676) else Color(0xFF80D8FF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Metallic3DBadgeTag(
                                    text = if (member.isOnMicNow) "على المايك 🎙️" else "متصل بالتطبيق",
                                    primaryColor = if (member.isOnMicNow) Color(0xFF00E676) else Color(0xFF00E5FF),
                                    secondaryColor = Color(0xFF004D40)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Telemetry Row: Gift Value + Mic Hours + Active Days + Expected Salary
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                HostStatPill(
                                    title = "قيمة الهدايا",
                                    value = "%,d 💎".format(member.totalGiftDiamondsReceived),
                                    color = Color(0xFF80D8FF),
                                    modifier = Modifier.weight(1f)
                                )
                                HostStatPill(
                                    title = "ساعات المايك",
                                    value = "${member.totalMicHours} ساعة ⏱️",
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.weight(1f)
                                )
                                HostStatPill(
                                    title = "الأيام / الراتب",
                                    value = "${member.activeMicDays} يوم • $${member.expectedSalaryUsd}",
                                    color = Color(0xFF00E676),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            } else {
                // =========================================================================
                // HOST PERSONAL DATA VIEW (`تظهر للمضيف بيانات المضيف وليس وكالة وبداخلها معلوماته`)
                // =========================================================================
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .ornateGolden3DContainer(cornerRadius = 24.dp)
                                .padding(18.dp)
                        ) {
                            Metallic3DBadgeTag(
                                text = "بطاقة بيانات المضيف الرسمية 🎙️",
                                iconEmoji = "✨",
                                primaryColor = Color(0xFF00E5FF),
                                secondaryColor = Color(0xFF01579B)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Ornate3DAvatarWithFrame(
                                avatarType = userProfile.avatarType,
                                frameStyle = userProfile.frameStyle,
                                size = 84.dp,
                                isSpeaking = true,
                                showCrown = true,
                                badgeText = "مضيف معتمد",
                                customImageUri = userProfile.customAvatarUri
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ColoredUserNameText(
                                name = userProfile.nickname,
                                primaryColor = userProfile.frameStyle.primaryColor,
                                secondaryColor = Color(0xFF00E5FF),
                                fontSize = 19.sp
                            )

                            Text(
                                text = "معرف المضيف (ID): ${userProfile.displayId}",
                                color = Color(0xFFFFF59D),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "الوكالة التابع لها: ${userProfile.agencyName}",
                                color = Color(0xFF80D8FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Host's Own Detailed Stats Grid
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AgencyMetricBox(
                                    label = "ساعاتي على المايك",
                                    value = "${userProfile.hostMicHours} ساعة ⏱️",
                                    accentColor = Color(0xFFFFD700),
                                    modifier = Modifier.weight(1f)
                                )
                                AgencyMetricBox(
                                    label = "أيام العمل النشطة",
                                    value = "${userProfile.hostActiveDays} يوم 📅",
                                    accentColor = Color(0xFF00E676),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AgencyMetricBox(
                                    label = "قيمة الهدايا المستلمة",
                                    value = "%,d 💎".format(userProfile.hostGiftsDiamonds),
                                    accentColor = Color(0xFF00E5FF),
                                    modifier = Modifier.weight(1f)
                                )
                                AgencyMetricBox(
                                    label = "الراتب المستحق حالياً",
                                    value = "$${(userProfile.hostGiftsDiamonds / 500)} USD 💵",
                                    accentColor = Color(0xFFFF4081),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF220A47))
                                .border(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "📌 تفاصيل حالة المضيف على المايك وداخل التطبيق:",
                                color = Color(0xFFFFF59D),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• حالة الاتصال داخل التطبيق: متصل ونشط الآن 🟢\n" +
                                    "• حالة المايك الصوتي: جاهز للبث الصوتي واستقبال الهدايا 🎙️\n" +
                                    "• التارجت الشهري المطلوب: 80 ساعة / 20 يوم (تم إنجاز 100% ✓)\n" +
                                    "• يتم تحديث عداد الساعات وقيمة الهدايا تلقائياً أثناء جلوسك على المايك في الغرف.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. NEW Standalone Full-Screen Charge Agency Center (`مركز وكالة الشحن المستقل 🪙⚡`)
 * - Appears when the user is activated from the database as a Charge Agent (`وكيل شحن`).
 * - Shows the Charge Agent's available coin pool (`بعدد العملات الموجودة فيه`).
 * - Allows shipping coins to ANY user by their `ID` (`ويمكن الشحن لأي مستخدم عن طريق الآي دي الخاص به`).
 * - Allows the Charge Agent to send complimentary Agent Rewards (`مكافآت يرسلها إلى أي مستخدم مثل الإطارات والدخوليات من مركز وكالة الشحن`).
 */
@Composable
fun StandaloneChargeAgencyScreen(
    userProfile: UserProfile,
    shipmentLogs: List<ChargeAgentShipmentLog>,
    onShipCoinsToUserById: (targetUserId: String, coinsAmount: Long) -> Unit,
    onSendAgentRewardById: (targetUserId: String, rewardItem: ChargeAgentRewardItem) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var activeTab by remember { mutableIntStateOf(0) } // 0 = شحن العملات بالـ ID, 1 = مكافآت الوكيل (إطارات ودخوليات), 2 = سجل شحن الوكيل
    var targetUserIdInput by remember { mutableStateOf(userProfile.displayId) }
    var customCoinAmountInput by remember { mutableStateOf("50000") }
    val quickAmounts = listOf(10000L, 50000L, 100000L, 250000L, 500000L, 1000000L)
    val agentRewards = remember { MasterAppDatabaseTable.chargeAgentRewardsCatalog }

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.5f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "مركز وكالة الشحن المعتمدة 🪙⚡",
                subtitleAr = "شحن العملات بالـ ID • إرسال مكافآت الإطارات والدخوليات للمستخدمين",
                onBack = onBack
            )

            // Charge Agency Coin Balance Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .ornateGolden3DContainer(cornerRadius = 22.dp)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Metallic3DBadgeTag(
                            text = "وكيل شحن معتمد 👑",
                            iconEmoji = "🪙"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "رصيد عملات وكالة الشحن المتاح:",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "%,d 🪙".format(userProfile.chargeAgentCoinsBalance),
                            color = Color(0xFFFFD700),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Ornate3DAvatarWithFrame(
                        avatarType = userProfile.avatarType,
                        frameStyle = userProfile.frameStyle,
                        size = 66.dp,
                        isSpeaking = true,
                        showCrown = true,
                        badgeText = "AGENT",
                        customImageUri = userProfile.customAvatarUri
                    )
                }
            }

            // 3 Tabs inside Charge Agency Center
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                listOf(
                    "شحن بالـ ID ⚡",
                    "مكافآت الوكيل 🎁",
                    "سجل الشحن 📜"
                ).forEachIndexed { idx, title ->
                    val selected = activeTab == idx
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) DynamicThemeManager.goldVertical3DBrush
                                else Brush.verticalGradient(listOf(Color(0xFF281054), Color(0xFF170736)))
                            )
                            .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                            .clickable { activeTab = idx }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = title,
                            color = if (selected) Color(0xFF1A0736) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (activeTab == 0) {
                    // TAB 0: Ship Coins to Any User by ID
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .ornateGolden3DContainer(cornerRadius = 22.dp)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "⚡ شحن العملات الذهبية لأي مستخدم عن طريق الـ ID",
                                color = Color(0xFFFFF59D),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = targetUserIdInput,
                                onValueChange = { targetUserIdInput = it },
                                label = { Text("آي دي المستخدم المستلم (ID)", color = Color(0xFFFFF59D)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color(0xFF7E57C2)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = customCoinAmountInput,
                                onValueChange = { customCoinAmountInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("كمية العملات المراد شحنها 🪙", color = Color(0xFFFFF59D)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color(0xFF7E57C2)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "أو اختر باقة شحن سريعة:",
                                color = Color(0xFF80D8FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick Amount Grid (2 rows of 3)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                quickAmounts.chunked(3).forEach { rowAmounts ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        rowAmounts.forEach { amt ->
                                            val isSelected = customCoinAmountInput == amt.toString()
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) Color(0xFFFFD700) else Color(0xFF1F083D))
                                                    .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                                                    .clickable { customCoinAmountInput = amt.toString() }
                                                    .padding(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "%,d 🪙".format(amt),
                                                    color = if (isSelected) Color(0xFF1A0736) else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glossy3DButton(cornerRadius = 50.dp)
                                    .clickable {
                                        val amount = customCoinAmountInput.toLongOrNull() ?: 0L
                                        if (targetUserIdInput.isNotBlank() && amount > 0L) {
                                            onShipCoinsToUserById(targetUserIdInput.trim(), amount)
                                        }
                                    }
                                    .padding(vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color(0xFF1A0736),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "شحن العملات فوراً إلى ID: $targetUserIdInput",
                                        color = Color(0xFF1A0736),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                } else if (activeTab == 1) {
                    // TAB 1: Charge Agent Complimentary Rewards (`مكافآت يرسلها الوكيل لأي مستخدم مثل الإطارات والدخوليات`)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .ornateGolden3DContainer(cornerRadius = 22.dp)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "🎁 إرسال مكافآت وكيل الشحن (إطارات دائرية ودخوليات متحركة)",
                                color = Color(0xFFFFF59D),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "حدد الـ ID الخاص بالمستخدم ثم اختر الإطار الدائري أو الدخولية لإرسالها كمكافأة شحن فورية:",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = targetUserIdInput,
                                onValueChange = { targetUserIdInput = it },
                                label = { Text("آي دي المستخدم المستلم للمكافأة (ID)", color = Color(0xFFFFF59D)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color(0xFF7E57C2)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    items(agentRewards, key = { it.id }) { reward ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF230A48))
                                .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (reward.category == StoreItemCategory.FRAMES && reward.frameStyle != null) {
                                    Ornate3DAvatarWithFrame(
                                        avatarType = userProfile.avatarType,
                                        frameStyle = reward.frameStyle,
                                        size = 52.dp,
                                        isSpeaking = true,
                                        showCrown = true
                                    )
                                } else {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF341269))
                                            .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
                                    ) {
                                        Text(reward.iconEmoji, fontSize = 26.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = reward.nameAr,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = if (reward.category == StoreItemCategory.FRAMES) {
                                            "⭕ إطار دائري على المايك • صلاحية ${reward.durationDays} يوم"
                                        } else {
                                            "🏎️ دخولية غرفة متحركة • صلاحية ${reward.durationDays} يوم"
                                        },
                                        color = Color(0xFF80D8FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .glossy3DButton(cornerRadius = 50.dp)
                                    .clickable {
                                        if (targetUserIdInput.isNotBlank()) {
                                            onSendAgentRewardById(targetUserIdInput.trim(), reward)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "إرسال بالـ ID 🎁",
                                    color = Color(0xFF1A0736),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                } else {
                    // TAB 2: Charge Agent Shipment & Rewards Log
                    items(shipmentLogs, key = { it.id }) { log ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF230A48))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.descriptionAr,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "المستلم ID: ${log.targetUserId}  •  ${log.timestampText}",
                                    color = Color(0xFFFFF59D),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = if (log.coinsShipped > 0) "%,d 🪙".format(log.coinsShipped) else (log.rewardNameAr ?: "مكافأة 🎁"),
                                color = Color(0xFF00E676),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgencyMetricBox(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E083E))
            .border(1.2.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Text(
            text = value,
            color = accentColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HostStatPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF160530))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
        Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    }
}

/**
 * 4. Standalone Full-Screen Global Leaderboard (`قائمة الشرف والترتيب العالمي المستقلة 🏆`)
 */
@Composable
fun StandaloneLeaderboardScreen(
    entries: List<LeaderboardEntry>,
    onBack: () -> Unit,
    onOpenUserProfile: (LeaderboardEntry) -> Unit = {}
) {
    BackHandler { onBack() }

    var selectedCategory by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.5f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "قائمة الشرف والترتيب العالمي 🏆",
                subtitleAr = "ملوك الدعم • نجوم الجاذبية • الغرف الصوتية",
                onBack = onBack
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                listOf("ملوك الثروة 🪙", "نجوم الجاذبية 💎", "أقوى الغرف 🎙️").forEachIndexed { idx, tab ->
                    val selected = selectedCategory == idx
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) DynamicThemeManager.goldVertical3DBrush
                                else Brush.verticalGradient(listOf(Color(0xFF280F54), Color(0xFF170736)))
                            )
                            .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                            .clickable { selectedCategory = idx }
                            .padding(vertical = 9.dp)
                    ) {
                        Text(
                            text = tab,
                            color = if (selected) Color(0xFF1A0736) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Top 3 Podium
            if (entries.size >= 3) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    PodiumMemberColumn(entry = entries[1], medal = "🥈", avatarSize = 64, onClick = { onOpenUserProfile(entries[1]) })
                    PodiumMemberColumn(entry = entries[0], medal = "🥇", avatarSize = 80, onClick = { onOpenUserProfile(entries[0]) })
                    PodiumMemberColumn(entry = entries[2], medal = "🥉", avatarSize = 60, onClick = { onOpenUserProfile(entries[2]) })
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(entries, key = { it.rank }) { entry ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF230A48))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .clickable { onOpenUserProfile(entry) }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${entry.rank}",
                                color = Color(0xFFFFD700),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.width(32.dp)
                            )
                            Ornate3DAvatarWithFrame(
                                avatarType = entry.avatarType,
                                frameStyle = entry.frameStyle,
                                size = 48.dp,
                                isSpeaking = entry.rank <= 3,
                                showCrown = entry.rank == 1,
                                customImageUri = entry.customAvatarUri
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                ColoredUserNameText(
                                    name = "${entry.nameAr} ${entry.countryFlag}",
                                    primaryColor = entry.frameStyle.primaryColor,
                                    secondaryColor = Color(0xFF00E5FF),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "ID: ${entry.displayId} • Lv.${entry.level}",
                                    color = Color(0xFFFFF59D),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Text(
                            text = entry.scoreLabel,
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumMemberColumn(
    entry: LeaderboardEntry,
    medal: String,
    avatarSize: Int,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(text = medal, fontSize = 22.sp)
        Ornate3DAvatarWithFrame(
            avatarType = entry.avatarType,
            frameStyle = entry.frameStyle,
            size = avatarSize.dp,
            isSpeaking = true,
            showCrown = true,
            badgeText = "Lv.${entry.level}",
            customImageUri = entry.customAvatarUri
        )
        Spacer(modifier = Modifier.height(4.dp))
        ColoredUserNameText(
            name = entry.nameAr,
            primaryColor = entry.frameStyle.primaryColor,
            secondaryColor = Color(0xFF00E5FF),
            fontSize = 12.sp
        )
        Text(
            text = entry.scoreLabel,
            color = Color(0xFFFFF59D),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 5. Standalone Full-Screen Account Settings (`إعدادات الحساب والخصوصية المستقلة ⚙️`)
 */
@Composable
fun StandaloneAccountSettingsScreen(
    userProfile: UserProfile,
    onSaveProfileInfo: (String, String, String, String) -> Unit,
    onToggleLinkAccount: (String) -> Unit,
    onOpenAppPolicy: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit,
    onPickAvatarFromDeviceStorage: (String) -> Unit = {}
) {
    BackHandler { onBack() }

    var nicknameInput by remember(userProfile.nickname) { mutableStateOf(userProfile.nickname) }
    var bioInput by remember(userProfile.bio) { mutableStateOf(userProfile.bio) }
    var selectedCountryPair by remember(userProfile.countryFlag, userProfile.countryNameAr) {
        mutableStateOf(userProfile.countryFlag to userProfile.countryNameAr)
    }
    var svgaHighQuality by remember { mutableStateOf(true) }
    var confirmDeleteDialog by remember { mutableStateOf(false) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.toString()?.let { onPickAvatarFromDeviceStorage(it) }
    }

    val countries = listOf(
        "🇪🇬" to "مصر",
        "🇸🇦" to "السعودية",
        "🇦🇪" to "الإمارات",
        "🇰🇼" to "الكويت",
        "🇲🇦" to "المغرب",
        "🇯🇴" to "الأردن"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.45f, showFireworks = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "إعدادات الحساب والخصوصية ⚙️",
                subtitleAr = "تعديل الملف الشخصي • ربط الحساب • سياسة التطبيق • الأمان",
                onBack = onBack
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 22.dp)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "👤 تعديل بيانات وصورة الملف الشخصي",
                            color = Color(0xFFFFF59D),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Ornate3DAvatarWithFrame(
                                avatarType = userProfile.avatarType,
                                frameStyle = userProfile.frameStyle,
                                size = 64.dp,
                                isSpeaking = false,
                                showCrown = true,
                                customImageUri = userProfile.customAvatarUri,
                                onClick = {
                                    avatarPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF281054))
                                    .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp))
                                    .clickable {
                                        avatarPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "📷 تغيير الصورة الشخصية الحقيقية للحساب",
                                    color = Color(0xFFFFF59D),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nicknameInput,
                            onValueChange = { nicknameInput = it },
                            label = { Text("اسم الحساب الملكي", color = Color(0xFFFFF59D)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFF7E57C2)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bioInput,
                            onValueChange = { bioInput = it },
                            label = { Text("النبذة التعريفية (Bio)", color = Color(0xFFFFF59D)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFF7E57C2)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            countries.forEach { pair ->
                                val active = selectedCountryPair.first == pair.first
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (active) Color(0xFFFFD700) else Color(0xFF281054))
                                        .clickable { selectedCountryPair = pair }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "${pair.first} ${pair.second}",
                                        color = if (active) Color(0xFF1A0736) else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glossy3DButton(cornerRadius = 50.dp)
                                .clickable {
                                    onSaveProfileInfo(
                                        nicknameInput,
                                        bioInput,
                                        selectedCountryPair.first,
                                        selectedCountryPair.second
                                    )
                                }
                                .padding(vertical = 9.dp)
                        ) {
                            Text("حفظ التعديلات ✅", color = Color(0xFF1A0736), fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 22.dp)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "🔐 ربط الحساب وحماية الآي دي (${userProfile.displayId})",
                            color = Color(0xFFFFF59D),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("ربط البريد الإلكتروني الرسمي", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(userProfile.email, color = Color(0xFF80D8FF), fontSize = 11.sp)
                            }
                            Switch(
                                checked = userProfile.isAccountLinkedGoogle,
                                onCheckedChange = { onToggleLinkAccount("GOOGLE") }
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("ربط رقم الهاتف المحمول (OTP)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    if (userProfile.isAccountLinkedPhone) "مرتبط وموثق ✓" else "غير مرتبط حالياً",
                                    color = Color(0xFFFFF59D),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = userProfile.isAccountLinkedPhone,
                                onCheckedChange = { onToggleLinkAccount("PHONE") }
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("تشغيل مؤثرات الدخوليات والهدايا 7D SVGA", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("عرض الدخوليات تلقائياً عند دخول الغرفة", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            }
                            Switch(
                                checked = svgaHighQuality,
                                onCheckedChange = { svgaHighQuality = it }
                            )
                        }
                    }
                }

                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF281054))
                            .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(18.dp))
                            .clickable { onOpenAppPolicy() }
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "📜 سياسة التطبيق وشروط الاستخدام",
                            color = Color(0xFF80D8FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF3E1678))
                                .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp))
                                .clickable { onLogout() }
                                .padding(vertical = 14.dp)
                                .testTag("logout_button")
                        ) {
                            Text(
                                text = "🚪 تسجيل الخروج",
                                color = Color(0xFFFFF59D),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF4A0E24))
                                .border(1.5.dp, Color(0xFFFF1744), RoundedCornerShape(18.dp))
                                .clickable { confirmDeleteDialog = true }
                                .padding(vertical = 14.dp)
                                .testTag("delete_account_button")
                        ) {
                            Text(
                                text = "🗑️ حذف الحساب",
                                color = Color(0xFFFF8A80),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        if (confirmDeleteDialog) {
            AlertDialog(
                onDismissRequest = { confirmDeleteDialog = false },
                containerColor = Color(0xFF24093A),
                title = {
                    Text("⚠️ تأكيد حذف الحساب نهائياً", color = Color(0xFFFF5252), fontWeight = FontWeight.ExtraBold)
                },
                text = {
                    Text(
                        "هل أنت متأكد من رغبتك في حذف حسابك وجميع العملات والشارات؟ لا يمكن التراجع عن هذا الإجراء.",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            confirmDeleteDialog = false
                            onDeleteAccount()
                        }
                    ) {
                        Text("تأكيد الحذف النهائي", color = Color(0xFFFF5252), fontWeight = FontWeight.ExtraBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDeleteDialog = false }) {
                        Text("إلغاء", color = Color(0xFFFFD700))
                    }
                }
            )
        }
    }
}

/**
 * 6. Standalone Full-Screen App Policy & Terms (`سياسة التطبيق وشروط الاستخدام المستقلة 📜`)
 */
@Composable
fun StandaloneAppPolicyScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val policySections = listOf(
        "1. ميثاق الشرف وآداب الغرف الصوتية" to "يلتزم جميع الأعضاء والمضيفين والداعمين في Zadira Live باحترام الذوق العام وآداب الحوار داخل الغرف الصوتية واللحظات والرسائل الخاصة.",
        "2. نظام العملات الذهبية والألماس والهدايا 7D" to "العملات الذهبية تُستخدم لشراء الهدايا الافتراضية، الإطارات الدائرية، الدخوليات المتحركة، والآي دي المميز. الألماس الكريستالي يُكتسب من الهدايا المستلمة على المايك.",
        "3. صلاحيات الغرف الصوتية" to "صاحب الغرفة يمتلك صلاحيات الإدارة الكاملة لغرفته (تغيير الاسم والصورة، تعيين الأدمن، الطرد، الكتم، والحظر). أدمن الغرفة يمتلك صلاحية قفل وفتح المايك والطرد والكتم لحفظ النظام.",
        "4. الخصوصية وحماية الحساب" to "نحترم خصوصيتك بالكامل. يتم حفظ حسابك بمعرفك الخاص المرتبط ببريدك الإلكتروني وكلمة المرور، ويحق لك طلب حذف الحساب في أي وقت من الإعدادات."
    )

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.45f, showFireworks = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "سياسة التطبيق وشروط الاستخدام 📜",
                subtitleAr = "القوانين الرسمية لمنصة زاديرا لايف للدردشة الصوتية",
                onBack = onBack
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(policySections) { (heading, body) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 20.dp)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = heading,
                            color = Color(0xFFFFF59D),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = body,
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 7. Standalone Full-Screen Royal Event Center & Festivals (`مركز الأحداث والمهرجانات الملكية المستقلة 🎪`)
 */
@Composable
fun StandaloneEventCenterScreen(
    onClaimEventBonus: (Long) -> Unit,
    onPreviewDragonSvga: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val festivals = listOf(
        Triple(
            "🐉 مهرجان فرسان التنين الذهبي 7D",
            "أرسل هدايا التنين الإمبراطوري أو القصور الملكية داخل الغرف الصوتية وتصدّر التصنيف للحصول على إطار التنين الدائري + 25,000 عملة ذهبية!",
            2500L
        ),
        Triple(
            "⚔️ بطولة تحدي الـ PK الإمبراطوري",
            "شارك في جولات الـ PK المباشرة بين الغرف وحقق 5 انتصارات متتالية لفتح وسام البطولة الذهبي المشتعل!",
            1500L
        ),
        Triple(
            "💑 كرنفال العشاق وثنائي الـ CP الماسي",
            "ارفع نقاط الألفة مع شريك الـ CP عبر خواتم الألماس 7D واحصل على دخولية العربة الملكية المشتركة!",
            1800L
        ),
        Triple(
            "🦁 حرب القبائل والعائلات الكبرى",
            "اجمع نقاط الهيبة مع أعضاء قبيلتك الملكية لفتح صندوق الكنز الأسبوعي للقبيلة!",
            2000L
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.5f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "مركز الأحداث والمهرجانات 🎪",
                subtitleAr = "مسابقات موسمية • جوائز 7D SVGA • مكافآت فورية",
                onBack = onBack
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(festivals) { (title, desc, bonusCoins) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 22.dp)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = title,
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = desc,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF2B1056))
                                    .border(1.2.dp, Color(0xFF00E5FF), RoundedCornerShape(50))
                                    .clickable { onPreviewDragonSvga(title) }
                                    .padding(vertical = 9.dp)
                            ) {
                                Text(
                                    text = "معاينة عرض 7D 🎬",
                                    color = Color(0xFF80D8FF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .glossy3DButton(cornerRadius = 50.dp)
                                    .clickable { onClaimEventBonus(bonusCoins) }
                                    .padding(vertical = 9.dp)
                            ) {
                                Text(
                                    text = "استلام هدية الحدث +$bonusCoins 🪙",
                                    color = Color(0xFF1A0736),
                                    fontSize = 12.sp,
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

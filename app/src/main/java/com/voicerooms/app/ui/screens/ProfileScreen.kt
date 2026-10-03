package com.voicerooms.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.AppBottomBar
import com.voicerooms.app.ui.components.CoinsChip
import com.voicerooms.app.ui.components.GradientButton
import com.voicerooms.app.ui.components.LevelBadge
import com.voicerooms.app.ui.components.RoomListTile
import com.voicerooms.app.ui.components.SectionHeader
import com.voicerooms.app.ui.components.UserAvatar
import com.voicerooms.app.ui.theme.Amber
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.CardDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.GradientEnd
import com.voicerooms.app.ui.theme.GradientStart
import com.voicerooms.app.ui.theme.Pink
import com.voicerooms.app.ui.theme.Purple
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.AuthViewModel
import com.voicerooms.app.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenGifts: () -> Unit,
    onOpenAgency: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRoom: (String) -> Unit,
    onLogout: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenExplore: () -> Unit,
    onOpenMessages: () -> Unit,
) {
    val vm: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val authVm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.loadMine()
        vm.loadMyRooms()
        vm.loadAgency()
    }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            AppBottomBar(currentRoute = "profile", onNavigate = { route ->
                when (route) {
                    "home" -> onOpenHome()
                    "explore" -> onOpenExplore()
                    "messages" -> onOpenMessages()
                }
            })
        },
    ) { padding ->
        val profile = state.profile
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    UserAvatar(
                        url = profile?.avatarUrl,
                        name = profile?.name ?: "?",
                        size = 96.dp,
                        modifier = Modifier.padding(top = 0.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(profile?.name ?: "—", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        LevelBadge(profile?.level ?: 1)
                    }
                    Text("@" + (profile?.username ?: ""), color = TextSecondary, fontSize = 13.sp)
                    profile?.bio?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(it, color = TextSecondary, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatItem("${state.followersCount}", "متابعون")
                        StatItem("${state.followingCount}", "يتابع")
                        StatItem("${profile?.xp ?: 0}", "XP")
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        GradientButton(
                            text = "تعديل الملف",
                            icon = Icons.Filled.Edit,
                            onClick = onEditProfile,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardDark)
                                .clickable { onOpenWallet() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Cyan)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CoinsChip(coins = profile?.coins ?: 0L, modifier = Modifier.weight(1f))
                    CoinsChip(coins = profile?.diamonds ?: 0L, modifier = Modifier.weight(1f))
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MenuRow("المحفظة", "شحن ورصيد العملات", Icons.Filled.AccountBalanceWallet, onOpenWallet)
                    Spacer(Modifier.height(8.dp))
                    MenuRow("الهدايا", "كل الهدايا المتحركة", Icons.Filled.CardGiftcard, onOpenGifts)
                    Spacer(Modifier.height(8.dp))
                    MenuRow("الوكالة", state.agency?.name ?: "أنشئ وكالتك الخاصة", Icons.Filled.Groups, onOpenAgency)
                    Spacer(Modifier.height(8.dp))
                    MenuRow("الإعدادات", "المستويات والإعدادات العامة", Icons.Filled.Settings, onOpenSettings)
                    Spacer(Modifier.height(8.dp))
                    MenuRow("تسجيل الخروج", "الخروج من الحساب", Icons.AutoMirrored.Filled.Logout, {
                        authVm.logout { onLogout() }
                    }, danger = true)
                }
            }

            if (state.myRooms.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "غرفي (${state.myRooms.size})",
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                items(state.myRooms) { room ->
                    RoomListTile(
                        room = room,
                        onClick = { onOpenRoom(room.id) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun MenuRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, danger: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (danger) Pink.copy(alpha = 0.15f) else Purple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (danger) Pink else Cyan)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = if (danger) Pink else TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

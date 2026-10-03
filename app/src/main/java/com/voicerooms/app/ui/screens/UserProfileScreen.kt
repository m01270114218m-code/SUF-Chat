package com.voicerooms.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.CoinsChip
import com.voicerooms.app.ui.components.ErrorBox
import com.voicerooms.app.ui.components.GradientBackground
import com.voicerooms.app.ui.components.GradientButton
import com.voicerooms.app.ui.components.LevelBadge
import com.voicerooms.app.ui.components.LoadingBox
import com.voicerooms.app.ui.components.UserAvatar
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.GradientEnd
import com.voicerooms.app.ui.theme.GradientStart
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.ProfileViewModel

@Composable
fun UserProfileScreen(userId: String, onBack: () -> Unit) {
    val vm: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(userId) { vm.load(userId) }

    GradientBackground {
        when {
            state.loading -> LoadingBox()
            state.error != null && state.profile == null -> ErrorBox(state.error!!) { vm.load(userId) }
            else -> {
                val profile = state.profile
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        UserAvatar(url = profile?.avatarUrl, name = profile?.name ?: "?", size = 96.dp)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(profile?.name ?: "—", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            LevelBadge(profile?.level ?: 1)
                        }
                        Text("@" + (profile?.username ?: ""), color = TextSecondary, fontSize = 13.sp)
                        profile?.bio?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = TextSecondary, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            StatBlock("${state.followersCount}", "متابعون")
                            StatBlock("${state.followingCount}", "يتابع")
                            StatBlock("${profile?.xp ?: 0}", "XP")
                        }
                        Spacer(Modifier.height(16.dp))
                        if (!state.isMe) {
                            GradientButton(
                                text = if (state.isFollowing) "إلغاء المتابعة" else "متابعة",
                                onClick = { vm.toggleFollow() },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            CoinsChip(coins = profile?.coins ?: 0L)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

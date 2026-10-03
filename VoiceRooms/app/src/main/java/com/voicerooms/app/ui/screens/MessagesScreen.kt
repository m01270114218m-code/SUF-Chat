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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.AppBottomBar
import com.voicerooms.app.ui.components.AppTextField
import com.voicerooms.app.ui.components.EmptyState
import com.voicerooms.app.ui.components.LevelBadge
import com.voicerooms.app.ui.components.UserAvatar
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.CardDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.SurfaceDark
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.SocialViewModel

@Composable
fun MessagesScreen(
    onOpenHome: () -> Unit,
    onOpenExplore: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenUser: (String) -> Unit,
) {
    val vm: SocialViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            AppBottomBar(currentRoute = "messages", onNavigate = { route ->
                when (route) {
                    "home" -> onOpenHome()
                    "explore" -> onOpenExplore()
                    "profile" -> onOpenProfile()
                }
            })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                "التواصل",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = tab,
                containerColor = BgDark,
                contentColor = Cyan,
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("الإشعارات") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("ابحث عن أصدقاء") })
            }

            if (tab == 0) {
                if (state.notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) { EmptyState("لا توجد إشعارات حتى الآن") }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.notifications) { notif ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CardDark)
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceDark),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Cyan, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        notif.title ?: "إشعار",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                    )
                                    notif.body?.let {
                                        Text(it, color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    AppTextField(
                        value = state.query,
                        onValueChange = { vm.search(it) },
                        label = "ابحث بالاسم...",
                        leadingIcon = Icons.Filled.Search,
                        keyboardType = KeyboardType.Text,
                    )
                    Spacer(Modifier.height(12.dp))
                    if (state.searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize()) { EmptyState("اكتب اسمًا للبحث") }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.searchResults) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(CardDark)
                                        .clickable { onOpenUser(user.id) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    UserAvatar(url = user.avatarUrl, name = user.name, size = 44.dp, showOnline = true, isOnline = user.isOnline)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(user.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text("@" + (user.username ?: ""), color = TextSecondary, fontSize = 12.sp)
                                    }
                                    LevelBadge(user.level)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

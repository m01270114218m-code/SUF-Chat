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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.AppBottomBar
import com.voicerooms.app.ui.components.AppTextField
import com.voicerooms.app.ui.components.CoinsChip
import com.voicerooms.app.ui.components.EmptyState
import com.voicerooms.app.ui.components.LoadingBox
import com.voicerooms.app.ui.components.Pill
import com.voicerooms.app.ui.components.RoomCard
import com.voicerooms.app.ui.components.RoomListTile
import com.voicerooms.app.ui.components.SectionHeader
import com.voicerooms.app.ui.components.UserAvatar
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.GradientEnd
import com.voicerooms.app.ui.theme.GradientStart
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.AuthViewModel
import com.voicerooms.app.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenRoom: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenExplore: () -> Unit,
    onOpenMessages: () -> Unit,
) {
    val vm: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val authVm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()
    val auth by authVm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            AppBottomBar(currentRoute = "home", onNavigate = { route ->
                when (route) {
                    "explore" -> onOpenExplore()
                    "messages" -> onOpenMessages()
                    "profile" -> onOpenProfile()
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRoom,
                containerColor = Cyan,
                contentColor = Color.Black,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إنشاء غرفة")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ---- الشريط العلوي ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserAvatar(
                    url = auth.user?.avatarUrl,
                    name = auth.user?.name ?: "?",
                    size = 42.dp,
                    showOnline = true,
                    isOnline = true,
                    modifier = Modifier.clickable { onOpenProfile() },
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("مرحبًا 👋", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        auth.user?.name ?: "زائر",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                CoinsChip(
                    coins = auth.user?.coins ?: 0L,
                    modifier = Modifier.clickable { onOpenWallet() },
                )
            }

            // ---- البحث ----
            AppTextField(
                value = state.query,
                onValueChange = { vm.search(it) },
                label = "ابحث عن غرفة...",
                leadingIcon = Icons.Filled.Search,
                keyboardType = KeyboardType.Text,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(12.dp))

            // ---- التصنيفات ----
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.categories) { cat ->
                    Pill(
                        text = cat.name,
                        selected = state.selectedCategory == cat.id,
                        onClick = { vm.selectCategory(cat.id) },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (state.loading && state.rooms.isEmpty()) {
                LoadingBox()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // ---- بانر ----
                    if (state.banners.isNotEmpty()) {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(state.banners) { banner ->
                                    Box(
                                        modifier = Modifier
                                            .width(300.dp)
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                                    ) {
                                        if (!banner.imageUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = banner.imageUrl,
                                                contentDescription = banner.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        }
                                        banner.title?.let {
                                            Text(
                                                it,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(14.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ---- غرف مميزة ----
                    if (state.featured.isNotEmpty()) {
                        item {
                            Column {
                                SectionHeader(
                                    title = "غرف مميزة",
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                                Spacer(Modifier.height(10.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(state.featured) { room ->
                                        RoomCard(room = room, onClick = { onOpenRoom(room.id) })
                                    }
                                }
                            }
                        }
                    }

                    // ---- كل الغرف ----
                    item {
                        SectionHeader(
                            title = "الغرف الصوتية",
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    if (state.rooms.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                                EmptyState("لا توجد غرف مطابقة")
                            }
                        }
                    } else {
                        items(state.rooms) { room ->
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
    }
}

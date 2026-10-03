package com.voicerooms.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.voicerooms.app.ui.components.LoadingBox
import com.voicerooms.app.ui.components.Pill
import com.voicerooms.app.ui.components.RoomCard
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.viewmodel.HomeViewModel

@Composable
fun ExploreScreen(
    onOpenRoom: (String) -> Unit,
    onOpenHome: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val vm: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            AppBottomBar(currentRoute = "explore", onNavigate = { route ->
                when (route) {
                    "home" -> onOpenHome()
                    "messages" -> onOpenMessages()
                    "profile" -> onOpenProfile()
                }
            })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Text("استكشف الغرف", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            AppTextField(
                value = state.query,
                onValueChange = { vm.search(it) },
                label = "ابحث...",
                leadingIcon = Icons.Filled.Search,
                keyboardType = KeyboardType.Text,
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            } else if (state.rooms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) { EmptyState("لا توجد غرف") }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.rooms) { room ->
                        RoomCard(room = room, onClick = { onOpenRoom(room.id) })
                    }
                }
            }
        }
    }
}

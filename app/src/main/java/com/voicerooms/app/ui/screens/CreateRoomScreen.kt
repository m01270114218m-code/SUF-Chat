package com.voicerooms.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.AppTextField
import com.voicerooms.app.ui.components.GradientBackground
import com.voicerooms.app.ui.components.GradientButton
import com.voicerooms.app.ui.components.Pill
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.AuthViewModel
import com.voicerooms.app.ui.viewmodel.CreateRoomViewModel

@Composable
fun CreateRoomScreen(onBack: () -> Unit, onCreated: (String) -> Unit) {
    val vm: CreateRoomViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val authVm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var welcome by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Int?>(null) }
    var roomType by remember { mutableStateOf("public") }
    var seats by remember { mutableFloatStateOf(8f) }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); vm.clearError() }
    }

    Scaffold(
        containerColor = BgDark,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        GradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
                    }
                    Text("إنشاء غرفة صوتية", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))

                AppTextField(name, { name = it }, "اسم الغرفة")
                Spacer(Modifier.height(14.dp))
                AppTextField(description, { description = it }, "وصف الغرفة", maxLines = 2)
                Spacer(Modifier.height(14.dp))
                AppTextField(welcome, { welcome = it }, "رسالة الترحيب", maxLines = 2)

                Spacer(Modifier.height(18.dp))
                Text("التصنيف", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.categories.size) { index ->
                        val cat = state.categories[index]
                        Pill(cat.name, categoryId == cat.id, onClick = { categoryId = cat.id })
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text("نوع الغرفة", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("عامة", roomType == "public", onClick = { roomType = "public" })
                    Pill("خاصة (بكلمة سر)", roomType == "private", onClick = { roomType = "private" })
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("عدد المقاعد", color = TextSecondary, fontSize = 13.sp)
                    Text("${seats.toInt()}", color = Cyan, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = seats,
                    onValueChange = { seats = it },
                    valueRange = 2f..16f,
                    steps = 13,
                )

                Spacer(Modifier.height(28.dp))
                GradientButton(
                    text = "إنشاء الغرفة",
                    onClick = {
                        val uid = authVm.currentUserId ?: return@GradientButton
                        vm.create(
                            userId = uid,
                            name = name,
                            description = description,
                            categoryId = categoryId,
                            roomType = roomType,
                            maxSeats = seats.toInt(),
                            welcomeMsg = welcome,
                            onCreated = { room -> onCreated(room.id) },
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    loading = state.creating,
                )
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

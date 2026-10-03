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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.AppTextField
import com.voicerooms.app.ui.components.GradientBackground
import com.voicerooms.app.ui.components.GradientButton
import com.voicerooms.app.ui.components.SectionHeader
import com.voicerooms.app.ui.components.UserAvatar
import com.voicerooms.app.ui.theme.Amber
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.CardDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.GradientEnd
import com.voicerooms.app.ui.theme.GradientStart
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.AgencyViewModel
import com.voicerooms.app.ui.viewmodel.AuthViewModel

@Composable
fun AgencyScreen(onBack: () -> Unit) {
    val vm: AgencyViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val authVm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var newName by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authVm.currentUserId?.let { vm.load(it) }
    }
    LaunchedEffect(state.message, state.error) {
        state.message?.let { snackbar.showSnackbar(it); vm.clearMessages() }
        state.error?.let { snackbar.showSnackbar(it); vm.clearMessages() }
    }

    Scaffold(
        containerColor = BgDark,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        GradientBackground {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
                        }
                        Text("الوكالات", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (state.myAgency == null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(CardDark)
                                .padding(16.dp),
                        ) {
                            Text("أنشئ وكالتك", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(10.dp))
                            AppTextField(newName, { newName = it }, "اسم الوكالة")
                            Spacer(Modifier.height(10.dp))
                            AppTextField(newDesc, { newDesc = it }, "وصف الوكالة", maxLines = 2)
                            Spacer(Modifier.height(14.dp))
                            GradientButton(
                                text = "إنشاء الوكالة",
                                onClick = {
                                    val uid = authVm.currentUserId ?: return@GradientButton
                                    vm.createAgency(uid, newName, newDesc, null)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                loading = state.creating,
                            )
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                                .padding(18.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(state.myAgency!!.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("${state.myAgency!!.membersCount} عضو • عمولة ${state.myAgency!!.commission}%", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                                    Text("الأرباح: ${state.myAgency!!.totalEarnings} 🪙", color = Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        SectionHeader(title = "أعضاء الوكالة", modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            AppTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it; vm.searchUsers(it) },
                                label = "ابحث عن عضو لإضافته",
                                leadingIcon = Icons.Filled.Search,
                                keyboardType = KeyboardType.Text,
                            )
                        }
                    }
                    items(state.searchResults) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardDark)
                                .clickable { vm.addMember(user.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            UserAvatar(url = user.avatarUrl, name = user.name, size = 42.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("@" + (user.username ?: ""), color = TextSecondary, fontSize = 12.sp)
                            }
                            Icon(Icons.Filled.Add, contentDescription = "إضافة", tint = Cyan)
                        }
                    }
                }

                item {
                    SectionHeader(title = "كل الوكالات", modifier = Modifier.padding(horizontal = 16.dp))
                }
                items(state.agencies) { agency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardDark)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(agency.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(agency.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("${agency.membersCount} عضو", color = TextSecondary, fontSize = 12.sp)
                        }
                        Text("${agency.totalEarnings} 🪙", color = Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

package com.voicerooms.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicerooms.app.data.model.ProfileUpdate
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.AppTextField
import com.voicerooms.app.ui.components.GradientBackground
import com.voicerooms.app.ui.components.GradientButton
import com.voicerooms.app.ui.components.Pill
import com.voicerooms.app.ui.components.UserAvatar
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val vm: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val ctx = LocalContext.current

    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("other") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadMine() }

    LaunchedEffect(state.profile) {
        val p = state.profile
        if (p != null && !initialized) {
            displayName = p.displayName ?: ""
            username = p.username ?: ""
            bio = p.bio ?: ""
            country = p.country ?: ""
            gender = p.gender
            initialized = true
        }
    }

    LaunchedEffect(state.message, state.error) {
        state.message?.let { snackbar.showSnackbar(it); vm.clearMessages() }
        state.error?.let { snackbar.showSnackbar(it); vm.clearMessages() }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bytes = ctx.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
            if (bytes != null) vm.uploadAvatar(bytes)
        }
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
                    Text("تعديل الملف الشخصي", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(modifier = Modifier.clickable { pickImage.launch("image/*") }) {
                        UserAvatar(url = state.profile?.avatarUrl, name = state.profile?.name ?: "?", size = 110.dp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("اضغط لتغيير الصورة", color = Cyan, fontSize = 12.sp)
                }

                Spacer(Modifier.height(20.dp))
                AppTextField(displayName, { displayName = it }, "الاسم الظاهر", leadingIcon = Icons.Filled.Badge)
                Spacer(Modifier.height(14.dp))
                AppTextField(username, { username = it }, "اسم المستخدم", leadingIcon = Icons.Filled.Person)
                Spacer(Modifier.height(14.dp))
                AppTextField(bio, { bio = it }, "نبذة عنك", maxLines = 3)
                Spacer(Modifier.height(14.dp))
                AppTextField(country, { country = it }, "الدولة", leadingIcon = Icons.Filled.Public)

                Spacer(Modifier.height(16.dp))
                Text("الجنس", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("ذكر", gender == "male") { gender = "male" }
                    Pill("أنثى", gender == "female") { gender = "female" }
                    Pill("آخر", gender == "other") { gender = "other" }
                }

                Spacer(Modifier.height(28.dp))
                GradientButton(
                    text = "حفظ التغييرات",
                    onClick = {
                        vm.updateProfile(
                            ProfileUpdate(
                                displayName = displayName.ifBlank { null },
                                username = username.ifBlank { null },
                                bio = bio.ifBlank { null },
                                country = country.ifBlank { null },
                                gender = gender,
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    loading = state.saving,
                )
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

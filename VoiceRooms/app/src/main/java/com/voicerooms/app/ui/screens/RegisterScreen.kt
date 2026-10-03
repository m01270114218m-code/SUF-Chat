package com.voicerooms.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.Purple
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(onRegistered: () -> Unit, onGoLogin: () -> Unit) {
    val vm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        GradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(60.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Purple, Cyan))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
                }
                Spacer(Modifier.height(18.dp))
                Text("إنشاء حساب جديد", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("انضم وابدأ بإنشاء غرفك الصوتية", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(28.dp))

                AppTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = "الاسم الظاهر",
                    leadingIcon = Icons.Filled.Badge,
                )
                Spacer(Modifier.height(14.dp))
                AppTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "اسم المستخدم (بالإنجليزية)",
                    leadingIcon = Icons.Filled.Person,
                )
                Spacer(Modifier.height(14.dp))
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "البريد الإلكتروني",
                    leadingIcon = Icons.Filled.Email,
                    keyboardType = KeyboardType.Email,
                )
                Spacer(Modifier.height(14.dp))
                AppTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "كلمة المرور (6 أحرف على الأقل)",
                    leadingIcon = Icons.Filled.Lock,
                    isPassword = true,
                )
                Spacer(Modifier.height(24.dp))

                GradientButton(
                    text = "إنشاء الحساب",
                    onClick = { vm.register(email, password, username, displayName) { onRegistered() } },
                    modifier = Modifier.fillMaxWidth(),
                    loading = state.loading,
                )
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("لديك حساب بالفعل؟", color = TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "سجّل الدخول",
                        color = Cyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onGoLogin() },
                    )
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

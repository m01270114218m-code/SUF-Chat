package com.voicerooms.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.GradientBackground
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.Purple
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: (Boolean) -> Unit) {
    val vm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.checkSession() }
    LaunchedEffect(state.initialized) {
        if (state.initialized) {
            delay(700)
            onDone(state.isLoggedIn)
        }
    }

    GradientBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Brush.linearGradient(listOf(Purple, Cyan))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("غرف صوتية", color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("دردشة صوتية • هدايا • وكالات", color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator(color = Cyan)
        }
    }
}

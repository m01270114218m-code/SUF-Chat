package com.voicerooms.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.viewmodel.AuthViewModel

/**
 * بوابة الجلسة فقط — لا تعرض واجهة تجريبية.
 * عند وجود جلسة Supabase صالحة ينتقل المستخدم مباشرة إلى الواجهة الرئيسية.
 */
@Composable
fun SplashScreen(onDone: (Boolean) -> Unit) {
    val vm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.checkSession()
    }

    LaunchedEffect(state.initialized) {
        if (state.initialized) {
            onDone(state.isLoggedIn)
        }
    }

    Box(modifier = Modifier.fillMaxSize())
}

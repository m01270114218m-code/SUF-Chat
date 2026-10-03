package com.voicerooms.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.voicerooms.app.ui.screens.AgencyScreen
import com.voicerooms.app.ui.screens.CreateRoomScreen
import com.voicerooms.app.ui.screens.EditProfileScreen
import com.voicerooms.app.ui.screens.ExploreScreen
import com.voicerooms.app.ui.screens.GiftsScreen
import com.voicerooms.app.ui.screens.HomeScreen
import com.voicerooms.app.ui.screens.LoginScreen
import com.voicerooms.app.ui.screens.MessagesScreen
import com.voicerooms.app.ui.screens.ProfileScreen
import com.voicerooms.app.ui.screens.RegisterScreen
import com.voicerooms.app.ui.screens.RoomScreen
import com.voicerooms.app.ui.screens.SettingsScreen
import com.voicerooms.app.ui.screens.SplashScreen
import com.voicerooms.app.ui.screens.UserProfileScreen
import com.voicerooms.app.ui.screens.WalletScreen

/**
 * الرسم البياني للتنقل — يربط كل شاشات التطبيق عبر [Routes].
 * نقطة البداية: شاشة البداية (Splash) التي تفحص الجلسة ثم توجّه للمناسب.
 */
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        // ---------------------------------------------------- البداية والمصادقة
        composable(Routes.SPLASH) {
            SplashScreen(onDone = { loggedIn ->
                val dest = if (loggedIn) Routes.HOME else Routes.LOGIN
                navController.navigate(dest) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoRegister = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onGoLogin = { navController.popBackStack() },
            )
        }

        // ---------------------------------------------------- الرئيسية
        composable(Routes.HOME) {
            HomeScreen(
                onOpenRoom = { navController.navigate(Routes.room(it)) },
                onCreateRoom = { navController.navigate(Routes.CREATE_ROOM) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenWallet = { navController.navigate(Routes.WALLET) },
                onOpenExplore = { navController.navigate(Routes.EXPLORE) },
                onOpenMessages = { navController.navigate(Routes.MESSAGES) },
            )
        }

        composable(Routes.EXPLORE) {
            ExploreScreen(
                onOpenRoom = { navController.navigate(Routes.room(it)) },
                onOpenHome = { navController.navigate(Routes.HOME) },
                onOpenMessages = { navController.navigate(Routes.MESSAGES) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
            )
        }

        composable(Routes.MESSAGES) {
            MessagesScreen(
                onOpenHome = { navController.navigate(Routes.HOME) },
                onOpenExplore = { navController.navigate(Routes.EXPLORE) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenUser = { navController.navigate(Routes.user(it)) },
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onOpenWallet = { navController.navigate(Routes.WALLET) },
                onOpenGifts = { navController.navigate(Routes.GIFTS) },
                onOpenAgency = { navController.navigate(Routes.AGENCY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenRoom = { navController.navigate(Routes.room(it)) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOpenHome = { navController.navigate(Routes.HOME) },
                onOpenExplore = { navController.navigate(Routes.EXPLORE) },
                onOpenMessages = { navController.navigate(Routes.MESSAGES) },
            )
        }

        // ---------------------------------------------------- الغرف
        composable(Routes.CREATE_ROOM) {
            CreateRoomScreen(
                onBack = { navController.popBackStack() },
                onCreated = { roomId ->
                    navController.navigate(Routes.room(roomId)) {
                        popUpTo(Routes.CREATE_ROOM) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.ROOM,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
        ) { entry ->
            val roomId = entry.arguments?.getString("roomId").orEmpty()
            RoomScreen(
                roomId = roomId,
                onBack = { navController.popBackStack() },
            )
        }

        // ---------------------------------------------------- الملفات الشخصية
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.USER_PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType }),
        ) { entry ->
            val userId = entry.arguments?.getString("userId").orEmpty()
            UserProfileScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
            )
        }

        // ---------------------------------------------------- الاقتصاد والوكالات والإعدادات
        composable(Routes.WALLET) {
            WalletScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.GIFTS) {
            GiftsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.AGENCY) {
            AgencyScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}

package com.voicerooms.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicerooms.app.data.model.Level
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.LoadingBox
import com.voicerooms.app.ui.components.SectionHeader
import com.voicerooms.app.ui.theme.Amber
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.Border
import com.voicerooms.app.ui.theme.CardDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.Purple
import com.voicerooms.app.ui.theme.Red
import com.voicerooms.app.ui.theme.TextHint
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val vm: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(containerColor = BgDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ---- الشريط العلوي ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = TextPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onBack() }
                        .padding(6.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("الإعدادات", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            if (state.loading) {
                LoadingBox()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // ---- إعدادات عامة من قاعدة البيانات ----
                    item {
                        SectionHeader(title = "عام")
                        Spacer(Modifier.height(4.dp))
                    }
                    item {
                        SettingRow(
                            icon = Icons.Filled.Notifications,
                            title = "الإشعارات",
                            subtitle = "استقبال إشعارات الهدايا والرسائل",
                            value = if (vm.bool("notifications_enabled", true)) "مُفعّل" else "مُعطّل",
                        )
                    }
                    item {
                        SettingRow(
                            icon = Icons.Filled.Palette,
                            title = "المظهر",
                            subtitle = "الوضع الداكن (افتراضي)",
                            value = vm.value("theme_mode", "dark").let { if (it == "dark") "داكن" else "فاتح" },
                        )
                    }
                    item {
                        SettingRow(
                            icon = Icons.Filled.Star,
                            title = "اللغة",
                            subtitle = "لغة التطبيق",
                            value = vm.value("language", "ar").let { if (it == "ar") "العربية" else "English" },
                        )
                    }
                    item {
                        SettingRow(
                            icon = Icons.Filled.PrivacyTip,
                            title = "الخصوصية",
                            subtitle = "من يمكنه مراسلتك ودعوتك",
                            value = vm.value("privacy_default", "everyone").let {
                                when (it) {
                                    "friends" -> "الأصدقاء"
                                    "nobody" -> "لا أحد"
                                    else -> "الجميع"
                                }
                            },
                        )
                    }

                    // ---- المستويات ----
                    if (state.levels.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            SectionHeader(title = "المستويات")
                            Spacer(Modifier.height(4.dp))
                        }
                        items(state.levels) { level -> LevelRow(level) }
                    }

                    // ---- عن التطبيق ----
                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionHeader(title = "عن التطبيق")
                        Spacer(Modifier.height(4.dp))
                    }
                    item {
                        SettingRow(
                            icon = Icons.Filled.Info,
                            title = vm.value("app_name", "غرف صوتية"),
                            subtitle = vm.value("app_description", "تطبيق الغرف الصوتية"),
                            value = "v${vm.value("app_version", "1.0.0")}",
                        )
                    }

                    // ---- تسجيل الخروج ----
                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardDark)
                                .border(1.dp, Red.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable { onLogout() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = null, tint = Red)
                            Spacer(Modifier.width(12.dp))
                            Text("تسجيل الخروج", color = Red, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Purple.copy(alpha = 0.25f), Cyan.copy(alpha = 0.25f)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Cyan, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextHint, fontSize = 12.sp)
        }
        Text(value, color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun LevelRow(level: Level) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(Purple, Cyan))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.MilitaryTech, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "المستوى ${level.level}${level.title?.let { " · $it" } ?: ""}",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text("يبدأ من ${level.minXp} نقطة خبرة", color = TextHint, fontSize = 12.sp)
        }
        if (level.rewardCoins > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Amber, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${level.rewardCoins}", color = Amber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

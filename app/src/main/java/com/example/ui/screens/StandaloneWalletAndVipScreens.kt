package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.DynamicThemeManager
import com.example.config.MasterAppDatabaseTable
import com.example.config.glossy3DButton
import com.example.config.ornateGolden3DContainer
import com.example.models.UserProfile
import com.example.models.WalletTransactionItem
import com.example.ui.components.ColoredUserNameText
import com.example.ui.components.Glossy3DDiamondAndCoinBoards
import com.example.ui.components.Metallic3DBadgeTag
import com.example.ui.components.Ornate3DAvatarWithFrame
import com.example.ui.components.PalaceNightAnimatedBackground

/**
 * 1. Standalone Full-Screen Gold Coins Only (`العملة: تعرض العملة فقط 🪙`)
 */
@Composable
fun StandaloneWalletScreen(
    userProfile: UserProfile,
    transactions: List<WalletTransactionItem>,
    onRechargeCoins: (Long) -> Unit,
    onExchangeDiamonds: (Long) -> Unit,
    onOpenAgencies: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var activeTab by remember { mutableIntStateOf(0) } // 0 = شحن العملة الذهبية, 1 = سجل العملة

    val rechargePackages = listOf(
        Triple(10000L, "$1.99", "+1,000 عملة بونص 🎁"),
        Triple(50000L, "$8.99", "+7,500 عملة بونص 🎁"),
        Triple(150000L, "$24.99", "+25,000 عملة بونص 🔥"),
        Triple(500000L, "$74.99", "+100,000 عملة بونص 👑"),
        Triple(1500000L, "$199.99", "+350,000 عملة بونص 🐉")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.46f, showFireworks = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Header (Coins Only - Unified with Main Interfaces style)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEDE0FC), Color(0xFFF7F2FD))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.2.dp, Color(0xFFB358F7), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color(0xFF6B21A8)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "محفظة العملة الذهبية 🪙",
                        color = Color(0xFF1C1C28),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "تعرض العملة الذهبية وباقات شحن العملة فقط",
                        color = Color(0xFF7E22CE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Dedicated Gold Coin Balance Hero Card ONLY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .ornateGolden3DContainer(cornerRadius = 22.dp)
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFF59D), Color(0xFFFFB300), Color(0xFFE65100))
                                    )
                                )
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Text(text = "🪙", fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "رصيد العملة الذهبية الحالي",
                                color = Color(0xFFFFF59D),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "%,d 🪙".format(userProfile.goldCoins),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .glossy3DButton(cornerRadius = 50.dp)
                            .clickable { onRechargeCoins(10000L) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "+ شحن سريع",
                            color = Color(0xFF1A0736),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // 2 Tabs (Coins Only)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                listOf("باقات شحن العملة 🪙", "سجل العملة الذهبية 📜").forEachIndexed { idx, label ->
                    val selected = activeTab == idx
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) DynamicThemeManager.goldVertical3DBrush
                                else Brush.verticalGradient(listOf(Color(0xFF281054), Color(0xFF170736)))
                            )
                            .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                            .clickable { activeTab = idx }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (selected) Color(0xFF1A0736) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                when (activeTab) {
                    0 -> {
                        items(rechargePackages) { (coins, priceUsd, bonusLabel) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .ornateGolden3DContainer(cornerRadius = 20.dp)
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🪙", fontSize = 30.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "%,d عملة ذهبية".format(coins),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = bonusLabel,
                                            color = Color(0xFF00E676),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .glossy3DButton(cornerRadius = 50.dp)
                                        .clickable { onRechargeCoins(coins) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "شحن $priceUsd",
                                        color = Color(0xFF1A0736),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        items(transactions, key = { it.id }) { tx ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF230A48))
                                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.titleAr,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = tx.dateText,
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = tx.amountText,
                                    color = if (tx.isPositive) Color(0xFF00E676) else Color(0xFFFF5252),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 1B. Standalone Full-Screen Diamonds Only (`الألماس: تعرض الألماس فقط 💎`)
 */
@Composable
fun StandaloneDiamondsOnlyScreen(
    userProfile: UserProfile,
    onExchangeDiamonds: (Long) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var activeTab by remember { mutableIntStateOf(0) } // 0 = تفاصيل واستبدال الألماس, 1 = سجل الألماس

    val diamondPackages = listOf(
        Triple(10000L, "رصيد ألماس الهدايا المستلمة", "+12,000 مكافأة تحويل ✨"),
        Triple(25000L, "باقة ألماس المضيف الفضي", "+30,000 مكافأة تحويل ✨"),
        Triple(50000L, "باقة ألماس المضيف الذهبي", "+62,000 مكافأة تحويل 🔥"),
        Triple(100000L, "باقة ألماس الإمبراطور الملكي", "+128,000 مكافأة تحويل 👑")
    )

    val diamondHistoryLogs = listOf(
        Triple("استلام هدية تنين الإمبراطور 7D على المايك", "+25,000 💎", "اليوم • 10:15 م"),
        Triple("مكافأة نشاط المايك اليومي في الغرفة", "+8,500 💎", "أمس • 09:40 م"),
        Triple("استلام هدية قصر السلاطين 7D", "+50,000 💎", "أمس • 07:12 م")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.46f, showFireworks = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Header (Diamonds Only - Unified with Main Interfaces style)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEDE0FC), Color(0xFFF7F2FD))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.2.dp, Color(0xFFB358F7), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color(0xFF6B21A8)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "خزانة الألماس الملكي 💎",
                        color = Color(0xFF1C1C28),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "تعرض رصيد الألماس وتفاصيل الألماس فقط",
                        color = Color(0xFF7E22CE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Dedicated Diamonds Balance Hero Card ONLY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF311042), Color(0xFF190A38), Color(0xFF0E294B))
                        )
                    )
                    .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFF80D8FF), Color(0xFF00B0FF), Color(0xFF6A1B9A))
                                    )
                                )
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Text(text = "💎", fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "إجمالي رصيد الألماس المملوك",
                                color = Color(0xFF80D8FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "%,d 💎".format(userProfile.crystalDiamonds),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Metallic3DBadgeTag(
                        text = "ألماس نقي 💎",
                        primaryColor = Color(0xFF00E5FF),
                        secondaryColor = Color(0xFFE040FB)
                    )
                }
            }

            // 2 Tabs (Diamonds Only)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                listOf("خيارات الألماس 💎", "سجل الألماس الوارد 📜").forEachIndexed { idx, label ->
                    val selected = activeTab == idx
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF9C27B0)))
                                else Brush.verticalGradient(listOf(Color(0xFF281054), Color(0xFF170736)))
                            )
                            .border(1.2.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp))
                            .clickable { activeTab = idx }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                when (activeTab) {
                    0 -> {
                        items(diamondPackages) { (diamondsCost, subtitleAr, bonusText) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF1E0B3B))
                                    .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text("💎", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "%,d ألماسة ملكية".format(diamondsCost),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = subtitleAr,
                                            color = Color(0xFF80D8FF),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = bonusText,
                                            color = Color(0xFFF48FB1),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF00E5FF), Color(0xFFD500F9))
                                            )
                                        )
                                        .clickable { onExchangeDiamonds(diamondsCost) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "استبدال 💎",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        items(diamondHistoryLogs) { (title, amount, dateText) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF1E0B3B))
                                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = dateText,
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = amount,
                                    color = Color(0xFF00E5FF),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Standalone Full-Screen VIP 5 Sections (`الـ VIP يعرض خمس أقسام يتم ضبطهم لاحقاً من قاعدة البيانات 👑`)
 */
@Composable
fun StandaloneVipNobilityScreen(
    userProfile: UserProfile,
    onUpgradeVip: (Int, Long) -> Unit,
    onPreviewVipMount: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val vipFiveSections = remember { MasterAppDatabaseTable.getVipFiveSectionsTable() }
    var selectedSectionIndex by remember {
        mutableIntStateOf((userProfile.vipTier - 1).coerceIn(0, (vipFiveSections.size - 1).coerceAtLeast(0)))
    }
    val activeVipSection = vipFiveSections.getOrElse(selectedSectionIndex) { vipFiveSections.first() }

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.5f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar (Unified with Main Interfaces style)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEDE0FC), Color(0xFFF7F2FD))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.2.dp, Color(0xFFB358F7), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color(0xFF6B21A8)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "نادي الـ VIP الملكي (5 أقسام) 👑",
                            color = Color(0xFF1C1C28),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "خمسة أقسام VIP متكاملة بالإطارات الدائرية والدخوليات والاسم الملون",
                            color = Color(0xFF7E22CE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Metallic3DBadgeTag(
                    text = "رتبتك: VIP ${userProfile.vipTier}",
                    iconEmoji = "👑"
                )
            }

            // 5 VIP Section Tabs (`VIP 1` | `VIP 2` | `VIP 3` | `VIP 4` | `VIP 5`)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                vipFiveSections.forEachIndexed { idx, section ->
                    val isSelected = selectedSectionIndex == idx
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) DynamicThemeManager.goldVertical3DBrush
                                else Brush.verticalGradient(listOf(Color(0xFF280F54), Color(0xFF170736)))
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFF59D) else Color(0xFF7E57C2),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedSectionIndex = idx }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "VIP ${section.vipTier}",
                            color = if (isSelected) Color(0xFF1A0736) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Showcase Card for the Selected VIP Section (out of the 5 sections)
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 24.dp)
                            .padding(18.dp)
                    ) {
                        Metallic3DBadgeTag(
                            text = activeVipSection.badgeTitleAr,
                            iconEmoji = "👑",
                            primaryColor = activeVipSection.grantedFrameStyle.primaryColor,
                            secondaryColor = activeVipSection.grantedFrameStyle.accentColor
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Circular Frame + Colored Soundwave Preview on the Avatar
                        Ornate3DAvatarWithFrame(
                            avatarType = userProfile.avatarType,
                            frameStyle = activeVipSection.grantedFrameStyle,
                            size = 88.dp,
                            isSpeaking = true,
                            showCrown = true,
                            badgeText = "VIP ${activeVipSection.vipTier}"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Colored Name Preview (`الاسم ملون`)
                        ColoredUserNameText(
                            name = "${userProfile.nickname} • ${activeVipSection.badgeTitleAr}",
                            primaryColor = activeVipSection.grantedFrameStyle.primaryColor,
                            secondaryColor = Color(0xFF00E5FF),
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = activeVipSection.titleAr,
                            color = Color(0xFFFFF59D),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Included Circular Frame & Entry Mount Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1F083D))
                                    .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                                    .padding(10.dp)
                            ) {
                                Text("⭕ الإطار الدائري", color = Color(0xFFFFF59D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = activeVipSection.grantedFrameStyle.nameAr,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1F083D))
                                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp))
                                    .clickable { onPreviewVipMount(activeVipSection.grantedEntryMountNameAr) }
                                    .padding(10.dp)
                            ) {
                                Text("🏎️ الدخولية المتحركة (اضغط للمعاينة)", color = Color(0xFF80D8FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = activeVipSection.grantedEntryMountNameAr,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Activate / Upgrade VIP Section Button
                        val isCurrentOrLower = userProfile.vipTier >= activeVipSection.vipTier
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glossy3DButton(cornerRadius = 50.dp)
                                .clickable {
                                    onUpgradeVip(activeVipSection.vipTier, activeVipSection.priceCoins)
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = if (isCurrentOrLower) {
                                    "تفعيل امتيازات وإطار VIP ${activeVipSection.vipTier} مجدداً ✨"
                                } else {
                                    "ترقية وتفعيل VIP ${activeVipSection.vipTier} مقابل %,d 🪙".format(activeVipSection.priceCoins)
                                },
                                color = Color(0xFF1A0736),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Perks List for the Selected VIP Section
                item {
                    Text(
                        text = "✨ مزايا وصلاحيات ${activeVipSection.titleAr}:",
                        color = Color(0xFFFFF59D),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                items(activeVipSection.perksAr) { perkText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF230A48))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = perkText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.DynamicThemeManager
import com.example.config.glossy3DButton
import com.example.config.ornateGolden3DContainer
import com.example.models.FrameStyle3D
import com.example.models.GiftItem
import com.example.models.SvgaEffectType
import com.example.models.UserProfile
import com.example.ui.components.Metallic3DBadgeTag
import com.example.ui.components.Ornate3DAvatarWithFrame
import com.example.ui.components.PalaceNightAnimatedBackground

/**
 * 1. Standalone Full-Screen Account Levels (`المستوى: تعرض مستويات حسابي ⚜️💎`)
 */
@Composable
fun StandaloneLevelsScreen(
    userProfile: UserProfile,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableStateOf("WEALTH") } // WEALTH or CHARISMA

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.45f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "مستويات حسابي ⚜️",
                subtitleAr = "تعرض مستويات حسابي (مستوى الثروة ومستوى الجاذبية)",
                onBack = onBack
            )

            // Dual Tab Switcher
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                listOf(
                    "WEALTH" to "👑 مستوى الثروة (Lv.${userProfile.wealthLevel})",
                    "CHARISMA" to "💎 مستوى الجاذبية (Lv.${userProfile.charismaLevel})"
                ).forEach { (key, label) ->
                    val active = selectedTab == key
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (active) DynamicThemeManager.goldMetallicBrush
                                else Brush.horizontalGradient(listOf(Color(0xFF281054), Color(0xFF1A0736)))
                            )
                            .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(50))
                            .clickable { selectedTab = key }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (active) Color(0xFF1A0736) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val isWealth = selectedTab == "WEALTH"
            val currentLvl = if (isWealth) userProfile.wealthLevel else userProfile.charismaLevel
            val xpCurrent = if (isWealth) userProfile.wealthXpCurrent else userProfile.charismaXpCurrent
            val xpTarget = if (isWealth) userProfile.wealthXpTarget else userProfile.charismaXpTarget
            val accentColor = if (isWealth) Color(0xFFFFD700) else Color(0xFF00E5FF)

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 24.dp)
                            .padding(18.dp)
                    ) {
                        Ornate3DAvatarWithFrame(
                            avatarType = userProfile.avatarType,
                            frameStyle = userProfile.frameStyle,
                            size = 86.dp,
                            isSpeaking = true,
                            showCrown = true,
                            badgeText = "Lv.$currentLvl",
                            customImageUri = userProfile.customAvatarUri
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isWealth) "رتبة الثروة الحالية: الأمير الذهبي Lv.$currentLvl" else "رتبة الجاذبية الحالية: نجم السهرة Lv.$currentLvl",
                            color = accentColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (xpCurrent.toFloat() / xpTarget.toFloat()).coerceIn(0f, 1f) },
                            color = accentColor,
                            trackColor = Color(0xFF160530),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "نقاط الخبرة الحالية: $xpCurrent / $xpTarget XP للوصول إلى المستوى ${currentLvl + 1}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Text(
                        text = "📈 كيف ترفع مستواك بسرعة؟",
                        color = Color(0xFFFFF59D),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                val xpMethods = if (isWealth) {
                    listOf(
                        Triple("🎁", "إرسال الهدايا الملكية في الغرف", "+100 XP لكل 100 عملة ذهبية"),
                        Triple("🧧", "إطلاق حقائب الحظ الذهبية للجمهور", "+500 XP لكل حقيبة حظ"),
                        Triple("👑", "تفعيل أو تجديد عضوية الـ VIP", "+2,000 XP فورية"),
                        Triple("🎰", "اللعب والفوز في مركز الألعاب", "+250 XP لكل جولة رابحة")
                    )
                } else {
                    listOf(
                        Triple("💎", "استلام الهدايا من الداعمين على المايك", "+150 XP لكل هدية مستلمة"),
                        Triple("🎙️", "استضافة السهرات والتحدث في الغرفة", "+300 XP لكل ساعة بث"),
                        Triple("⚔️", "الفوز في جولات تحدي الـ PK", "+600 XP لكل فوز PK"),
                        Triple("✨", "التفاعل على لحظاتك الصوتية في اكتشف", "+100 XP لكل 10 إعجابات")
                    )
                }

                items(xpMethods) { (emoji, title, reward) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF250B4E))
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(emoji, fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Metallic3DBadgeTag(
                            text = reward,
                            primaryColor = accentColor,
                            secondaryColor = Color(0xFF4A148C)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Standalone Full-Screen Owned Badges (`الشارة: تعرض الشارات المملوكة 🎖️`)
 */
@Composable
fun StandaloneBadgesScreen(
    userProfile: UserProfile,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val ownedBadges = remember(userProfile.badges) {
        userProfile.badges.filter { it.isUnlocked }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.45f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "الشارات المملوكة 🎖️",
                subtitleAr = "تعرض الشارات المملوكة في حسابك فقط (${ownedBadges.size} شارة مملوكة)",
                onBack = onBack
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(ownedBadges, key = { it.id }) { badge ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 20.dp)
                            .padding(14.dp)
                    ) {
                        Metallic3DBadgeTag(
                            text = badge.categoryAr,
                            primaryColor = badge.primaryColor,
                            secondaryColor = badge.secondaryColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(12.dp, CircleShape, spotColor = badge.primaryColor)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(badge.primaryColor, badge.secondaryColor, Color(0xFF1A0736))
                                    )
                                )
                                .border(2.dp, Color(0xFFFFF59D), CircleShape)
                        ) {
                            Text(badge.iconEmoji, fontSize = 36.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = badge.titleAr,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = badge.descriptionAr,
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Metallic3DBadgeTag(
                            text = "مُجهّز على البروفايل (${badge.levelText})",
                            primaryColor = Color(0xFF00E676),
                            secondaryColor = Color(0xFF1B5E20)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Standalone Full-Screen Gift Atlas Museum (`أطلس الهدايا الملكي المستقل 🔮`)
 */
@Composable
fun StandaloneGiftAtlasScreen(
    giftsCatalog: List<GiftItem>,
    onPreviewGiftSvga: (String, SvgaEffectType) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val totalReceived = remember(giftsCatalog) { giftsCatalog.sumOf { it.receivedCount } }

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.48f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "أطلس الهدايا الملكي المضاء 🔮",
                subtitleAr = "إجمالي الهدايا الفاخرة المستلمة: $totalReceived هدية 7D",
                onBack = onBack
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(giftsCatalog, key = { it.id }) { gift ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 20.dp)
                            .clickable { onPreviewGiftSvga(gift.nameAr, gift.effectType) }
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Metallic3DBadgeTag(
                                text = gift.badgeTag,
                                primaryColor = gift.primaryColor,
                                secondaryColor = gift.secondaryColor
                            )
                            Text(
                                text = "مستلم X${gift.receivedCount}",
                                color = Color(0xFFFFF59D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(12.dp, CircleShape, spotColor = gift.primaryColor)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(gift.primaryColor.copy(alpha = 0.65f), Color(0xFF1F0A44))
                                    )
                                )
                                .border(2.dp, Color(0xFFFFF59D), CircleShape)
                        ) {
                            Text(gift.iconSymbol, fontSize = 38.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = gift.nameAr,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "القيمة: ${gift.priceCoins} 🪙 • +${gift.diamondValue} 💎",
                            color = Color(0xFF80D8FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF38146B))
                                .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(50))
                                .padding(vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "معاينة",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "معاينة المؤثر 7D",
                                color = Color(0xFFFFF59D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. Standalone Full-Screen CP & Royal Relationships Hall (`قاعة العلاقات الملكية وشريك الـ CP المستقلة 💖💍`)
 */
@Composable
fun StandaloneCpRelationshipsScreen(
    userProfile: UserProfile,
    onBoostCpIntimacy: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.5f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "قاعة العلاقات الملكية والـ CP 💖",
                subtitleAr = "شريك القلب (CP) • الحراس الملكيون • الأصدقاء المقربون",
                onBack = onBack
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 1. Centerpiece 3D CP Couple Stage
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(
                                cornerRadius = 26.dp,
                                backgroundBrush = Brush.verticalGradient(
                                    listOf(Color(0xFF560A3E), Color(0xFF2B0A4E), Color(0xFF180632))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Metallic3DBadgeTag(
                            text = "💍 العلاقة الملكية الموثقة • CP المستوى ${userProfile.cpIntimacyLevel}",
                            primaryColor = Color(0xFFFF4081),
                            secondaryColor = Color(0xFF880E4F)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Ornate3DAvatarWithFrame(
                                    avatarType = userProfile.avatarType,
                                    frameStyle = userProfile.frameStyle,
                                    size = 78.dp,
                                    isSpeaking = true,
                                    showCrown = true,
                                    customImageUri = userProfile.customAvatarUri
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(userProfile.nickname, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                Text("ID: ${userProfile.displayId}", color = Color(0xFFFFF59D), fontSize = 10.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💖💍💖", fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "%,d نقطة حب".format(userProfile.cpPoints),
                                    color = Color(0xFFFF80AB),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Ornate3DAvatarWithFrame(
                                    avatarType = "PRINCESS",
                                    frameStyle = FrameStyle3D.ROSE_GOLD_PHOENIX,
                                    size = 78.dp,
                                    isSpeaking = true,
                                    showCrown = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(userProfile.cpPartnerName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                Text("ID: ${userProfile.cpPartnerId}", color = Color(0xFFFFF59D), fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glossy3DButton(cornerRadius = 50.dp)
                                .clickable { onBoostCpIntimacy() }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "إرسال خاتم العشاق الملكي 7D وترقية الـ CP (1,000 🪙)",
                                color = Color(0xFF1A0736),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // 2. Royal Guardians & Besties List
                item {
                    Text(
                        text = "🛡️ الحراس الملكيون وشركاء الصداقة المقربون:",
                        color = Color(0xFFFFF59D),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                val relationsList = listOf(
                    Triple("السلطان فهد 🇦🇪", "🛡️ الحارس الإمبراطوري الأعلى", "المستوى 12 • 38,000 نقطة حماية"),
                    Triple("نور القمر 🇲🇦", "🌟 الصديق المقرب (Bestie)", "المستوى 9 • 19,400 نقطة صداقة"),
                    Triple("الأمير نواف 🇰🇼", "⚔️ حليف المعارك الملكي", "المستوى 8 • 14,200 نقطة تحالف")
                )

                items(relationsList) { (name, roleTitle, pointsText) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF240B4B))
                            .border(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(roleTitle, color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            Text(pointsText, color = Color(0xFFFFF59D), fontSize = 11.sp)
                        }
                        Text("👑", fontSize = 28.sp)
                    }
                }
            }
        }
    }
}

/**
 * 5. Standalone Full-Screen Royal Family & Clan System (`العائلة والقبيلة الملكية المستقلة 🛡️🦁`)
 */
@Composable
fun StandaloneFamilyClanScreen(
    userProfile: UserProfile,
    onEnterFamilyRoom: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val familyMembers = listOf(
        Triple("${userProfile.nickname} ${userProfile.countryFlag}", "شيخ القبيلة / القائد الأعلى 👑", "مساهمة: 480,000 🛡️"),
        Triple("السلطان فهد 🇦🇪", "نائب القائد والـ BD 💎", "مساهمة: 410,000 🛡️"),
        Triple("الأميرة شهد 🇸🇦", "وزيرة الحفلات والمضيفين 🎙️", "مساهمة: 355,000 🛡️"),
        Triple("الأمير نواف 🇰🇼", "قائد فرسان الـ PK ⚔️", "مساهمة: 290,000 🛡️"),
        Triple("نور القمر 🇲🇦", "نجمة القبيلة الملكية ✨", "مساهمة: 215,000 🛡️")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.48f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "العائلة والقبيلة الملكية 🦁",
                subtitleAr = "${userProfile.familyNameAr} • رتبتك: ${userProfile.familyRankAr}",
                onBack = onBack
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Family Shield Banner
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 24.dp)
                            .padding(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(82.dp)
                                .shadow(14.dp, CircleShape, spotColor = Color(0xFFFFD700))
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFB26A00), Color(0xFF280A50)))
                                )
                                .border(2.5.dp, Color(0xFFFFF59D), CircleShape)
                        ) {
                            Text("🦁", fontSize = 42.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = userProfile.familyNameAr,
                            color = Color(0xFFFFF59D),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "المستوى 15 • المركز #1 في الترتيب العالمي للعائلات 🏆",
                            color = Color(0xFF00E676),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glossy3DButton(cornerRadius = 50.dp)
                                .clickable { onEnterFamilyRoom() }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "دخول المقر الصوتي الرسمي للعائلة 🎙️🦁",
                                color = Color(0xFF1A0736),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "👑 قادة وفرسان العائلة الملكية (${familyMembers.size}/150):",
                        color = Color(0xFFFFF59D),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                items(familyMembers) { (name, role, contribution) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF240B4B))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            Text(role, color = Color(0xFF80D8FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Metallic3DBadgeTag(
                            text = contribution,
                            primaryColor = Color(0xFFFFD700),
                            secondaryColor = Color(0xFFB26A00)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun StandaloneTopHeader(
    titleAr: String,
    subtitleAr: String,
    onBack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFEDE0FC), Color(0xFFF7F2FD))
                )
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, Color(0xFFB358F7), CircleShape)
                .clickable { onBack() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "رجوع",
                tint = Color(0xFF6B21A8),
                modifier = Modifier.size(20.dp)
            )
        }

        Column {
            Text(
                text = titleAr,
                color = Color(0xFF1C1C28),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitleAr,
                color = Color(0xFF7E22CE),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 6. Standalone Full-Screen Language Settings (`اللغة: تعرض لغات عديدة للتطبيق 🌐`)
 */
@Composable
fun StandaloneLanguageSettingsScreen(
    currentLanguageAr: String,
    onSelectLanguage: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val availableLanguages = listOf(
        Triple("🇸🇦", "العربية", "Arabic • اللغة الرسمية"),
        Triple("🇺🇸", "English", "الإنجليزية • Global"),
        Triple("🇹🇷", "Türkçe", "التركية • Türkiye"),
        Triple("🇫🇷", "Français", "الفرنسية • France"),
        Triple("🇪🇸", "Español", "الإسبانية • España"),
        Triple("🇩🇪", "Deutsch", "الألمانية • Deutschland"),
        Triple("🇷🇺", "Русский", "الروسية • Россия"),
        Triple("🇮🇷", "فارسی", "الفارسية • Persian"),
        Triple("🇵🇰", "اردو", "الأردية • Urdu"),
        Triple("🇮🇳", "हिन्दी", "الهندية • Hindi"),
        Triple("🇮🇩", "Bahasa Indonesia", "الإندونيسية • Indonesia"),
        Triple("🇨🇳", "中文 (简体)", "الصينية • Chinese")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.45f, showFireworks = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "لغات التطبيق المتعددة 🌐",
                subtitleAr = "اللغة الحالية: $currentLanguageAr • اختر لغتك المفضلة",
                onBack = onBack
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(availableLanguages) { (flag, langName, subtitle) ->
                    val isSelected = currentLanguageAr == langName
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4A148C), Color(0xFF311B92))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF210A44), Color(0xFF160530))
                                    )
                                }
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFD700) else Color(0xFF7E57C2),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onSelectLanguage(langName) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = flag, fontSize = 26.sp)
                            Column {
                                Text(
                                    text = langName,
                                    color = if (isSelected) Color(0xFFFFF59D) else Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = subtitle,
                                    color = Color(0xFF80D8FF),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isSelected) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "مُفعّلة",
                                    color = Color(0xFF00E676),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF2E1263))
                                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "اختيار",
                                    color = Color(0xFFFFF59D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
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
 * 7. Standalone Full-Screen Customer Service (`خدمة العملاء 🎧`)
 */
@Composable
fun StandaloneCustomerServiceScreen(
    userProfile: UserProfile,
    onSubmitTicket: (String, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val categories = listOf(
        "شحن العملات والألماس 🪙",
        "المتجر والإطارات والدخوليات 🛍️",
        "الغرف الصوتية والمايكات 🎙️",
        "الحساب والآي دي المميز 🆔"
    )
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var ticketMessage by remember { mutableStateOf("") }

    val faqs = listOf(
        "كيف أشتري وأركب إطار أو دخولية من المتجر؟" to "افتح قسم المتجر، اختر قسم الإطارات أو قسم الدخوليات أو قسم الآي دي، واضغط شراء وتفعيل، أو افتح الحقيبة لتفعيل ما تمتلكه.",
        "كيف أحصل على مكافآت دعوة الأصدقاء؟" to "من قسم دعوة الأصدقاء، انسخ كود الدعوة الخاص بك وشاركه مع أصدقائك لتحصل على إطارات ملكية وعملات ذهبية مجانية.",
        "كيف أغير اسم وصورة غرفتي الصوتية؟" to "إذا كنت صاحب الغرفة (OWNER)، اضغط على زر تعديل الغرفة أعلى الغرفة الصوتية لاختيار صورة من جهازك وتعديل اسم الغرفة."
    )

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.45f, showFireworks = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "مركز خدمة العملاء الملكي 🎧",
                subtitleAr = "دعم فني مباشر على مدار الساعة 24/7 لحسابك ID: ${userProfile.displayId}",
                onBack = onBack
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Live Support Banner
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 22.dp)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFF00E5FF), Color(0xFF2979FF))
                                        )
                                    )
                                    .border(2.dp, Color.White, CircleShape)
                            ) {
                                Text("🎧", fontSize = 26.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "فريق الدعم الملكي متصل الآن",
                                    color = Color(0xFFFFF59D),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "متوسط وقت الاستجابة: أقل من 3 دقائق ⚡",
                                    color = Color(0xFF00E676),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Metallic3DBadgeTag(
                            text = "VIP 24/7",
                            primaryColor = Color(0xFF00E676),
                            secondaryColor = Color(0xFF00B0FF)
                        )
                    }
                }

                // Ticket Form Card
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF210A46))
                            .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "📩 أرسل تذكرة مباشرة لخدمة العملاء:",
                            color = Color(0xFFFFF59D),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        // Category Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.chunked(2).forEach { pair ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    pair.forEach { cat ->
                                        val isSelected = selectedCategory == cat
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) DynamicThemeManager.goldVertical3DBrush
                                                    else Brush.verticalGradient(listOf(Color(0xFF2D125E), Color(0xFF190738)))
                                                )
                                                .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                                                .clickable { selectedCategory = cat }
                                                .padding(vertical = 8.dp, horizontal = 6.dp)
                                        ) {
                                            Text(
                                                text = cat,
                                                color = if (isSelected) Color(0xFF1A0736) else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = ticketMessage,
                            onValueChange = { ticketMessage = it },
                            placeholder = {
                                Text(
                                    "اكتب رسالتك أو استفسارك لخدمة العملاء هنا...",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFF7E57C2)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(105.dp)
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glossy3DButton(cornerRadius = 50.dp)
                                .clickable {
                                    onSubmitTicket(selectedCategory, ticketMessage)
                                    ticketMessage = ""
                                }
                                .padding(vertical = 11.dp)
                        ) {
                            Text(
                                text = "إرسال الطلب لخدمة العملاء الآن 🚀",
                                color = Color(0xFF1A0736),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "💡 الأسئلة الشائعة والمساعدة الفورية:",
                        color = Color(0xFFFFF59D),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                items(faqs) { (question, answer) ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1D093E))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "❓ $question",
                            color = Color(0xFF80D8FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = answer,
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 8. Standalone Full-Screen Invite Friends (`دعوة الأصدقاء للحصول على مكافآت إطارات وعملات ذهبية 🎁⭕🪙`)
 */
private data class InviteRewardMilestone(
    val friendsRequired: Int,
    val titleAr: String,
    val coinsReward: Long,
    val frameReward: FrameStyle3D
)

@Composable
fun StandaloneInviteFriendsScreen(
    userProfile: UserProfile,
    onInviteNewFriend: () -> Unit,
    onClaimMilestoneReward: (Int, Long, FrameStyle3D?) -> Unit,
    onCopyInviteCode: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val clipboardManager = LocalClipboardManager.current
    val inviteCode = "ROYAL-${userProfile.displayId}"

    val milestones = listOf(
        InviteRewardMilestone(
            friendsRequired = 1,
            titleAr = "دعوة صديق واحد (1)",
            coinsReward = 5000L,
            frameReward = FrameStyle3D.ROYAL_VIOLET_AURA
        ),
        InviteRewardMilestone(
            friendsRequired = 3,
            titleAr = "دعوة 3 أصدقاء ملكيين",
            coinsReward = 15000L,
            frameReward = FrameStyle3D.EMERALD_SULTAN_CROWN
        ),
        InviteRewardMilestone(
            friendsRequired = 5,
            titleAr = "دعوة 5 أصدقاء ملكيين",
            coinsReward = 35000L,
            frameReward = FrameStyle3D.ROSE_GOLD_PHOENIX
        ),
        InviteRewardMilestone(
            friendsRequired = 10,
            titleAr = "دعوة 10 أصدقاء (المكافأة الكبرى 👑)",
            coinsReward = 100000L,
            frameReward = FrameStyle3D.IMPERIAL_GOLD_WINGS
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.48f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            StandaloneTopHeader(
                titleAr = "دعوة الأصدقاء والمكافآت الملكية 🎁",
                subtitleAr = "ادعُ أصدقاءك واحصل على إطارات دائرية فاخرة وعملات ذهبية",
                onBack = onBack
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Hero Invite Code & Stats Card
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .ornateGolden3DContainer(cornerRadius = 24.dp)
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "👑 كود الدعوة الملكي الخاص بك",
                            color = Color(0xFFFFF59D),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1A0736))
                                .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(inviteCode))
                                    onCopyInviteCode(inviteCode)
                                }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ كود الدعوة",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = inviteCode,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "عدد الأصدقاء المدعوين حالياً: ${userProfile.invitedFriendsCount} أصدقاء 🎉",
                            color = Color(0xFF00E676),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glossy3DButton(cornerRadius = 50.dp)
                                .clickable { onInviteNewFriend() }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "مشاركة الرابط ودعوة صديق جديد الآن (+1) 📲",
                                color = Color(0xFF1A0736),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "🎁 جوائز ومكافآت دعوة الأصدقاء (إطارات + عملات ذهبية):",
                        color = Color(0xFFFFF59D),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                items(milestones) { milestone ->
                    val isClaimed = userProfile.claimedInviteMilestones.contains(milestone.friendsRequired)
                    val isUnlocked = userProfile.invitedFriendsCount >= milestone.friendsRequired

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        milestone.frameReward.primaryColor.copy(alpha = 0.22f),
                                        Color(0xFF210A46),
                                        Color(0xFF14052E)
                                    )
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isClaimed) Color(0xFF00E676) else milestone.frameReward.primaryColor,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Live preview of the Frame Reward
                                Ornate3DAvatarWithFrame(
                                    avatarType = userProfile.avatarType,
                                    frameStyle = milestone.frameReward,
                                    size = 62.dp,
                                    isSpeaking = true,
                                    showCrown = true
                                )
                                Column {
                                    Text(
                                        text = milestone.titleAr,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "🪙 +%,d عملة ذهبية".format(milestone.coinsReward),
                                        color = Color(0xFFFFD700),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "⭕ + ${milestone.frameReward.nameAr}",
                                        color = Color(0xFF80D8FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Metallic3DBadgeTag(
                                text = "${userProfile.invitedFriendsCount}/${milestone.friendsRequired}",
                                primaryColor = if (isUnlocked) Color(0xFF00E676) else Color(0xFFFF9100),
                                secondaryColor = Color(0xFF280F54)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when {
                                        isClaimed -> Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)))
                                        isUnlocked -> DynamicThemeManager.goldVertical3DBrush
                                        else -> Brush.horizontalGradient(listOf(Color(0xFF311B92), Color(0xFF4A148C)))
                                    }
                                )
                                .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(50))
                                .clickable {
                                    onClaimMilestoneReward(
                                        milestone.friendsRequired,
                                        milestone.coinsReward,
                                        milestone.frameReward
                                    )
                                }
                                .padding(vertical = 9.dp)
                        ) {
                            Text(
                                text = when {
                                    isClaimed -> "تم استلام الإطار والعملات الذهبية ✓"
                                    isUnlocked -> "استلام المكافأة (إطار + عملات ذهبية) 🎁"
                                    else -> "ادعُ المزيد من الأصدقاء لفتح المكافأة 🔒"
                                },
                                color = if (isUnlocked && !isClaimed) Color(0xFF1A0736) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}



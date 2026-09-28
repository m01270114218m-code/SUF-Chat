package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.DynamicThemeManager
import com.example.config.glossy3DButton
import com.example.config.ornateGolden3DContainer
import com.example.models.StoreCatalogItem
import com.example.models.StoreItemCategory
import com.example.models.UserProfile
import com.example.ui.components.ColoredUserNameText
import com.example.ui.components.Metallic3DBadgeTag
import com.example.ui.components.Ornate3DAvatarWithFrame
import com.example.ui.components.PalaceNightAnimatedBackground

/**
 * 1. Standalone Full-Screen Royal Store (`المتجر الملكي المستقل 7D`)
 * Displays strictly ONLY the 3 categories requested for sale:
 * - الإطارات الدائرية (`StoreItemCategory.FRAMES`)
 * - الدخوليات المتحركة (`StoreItemCategory.ENTRY_MOUNTS`)
 * - الآي دي المميز (`StoreItemCategory.SPECIAL_IDS`)
 */
@Composable
fun StandaloneStoreScreen(
    userProfile: UserProfile,
    storeItems: List<StoreCatalogItem>,
    onBuyOrEquipItem: (StoreCatalogItem) -> Unit,
    onGiftStoreItemToFriend: (StoreCatalogItem, String) -> Unit,
    onPreviewEntryMount7D: (String) -> Unit,
    onOpenMyAccessories: () -> Unit,
    onOpenWallet: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val storeCategories = listOf(
        StoreItemCategory.FRAMES,
        StoreItemCategory.ENTRY_MOUNTS,
        StoreItemCategory.SPECIAL_IDS,
        StoreItemCategory.CHAT_BUBBLES,
        StoreItemCategory.ROOM_THEMES
    )
    var selectedCategory by remember { mutableStateOf(StoreItemCategory.FRAMES) }
    var selectedDurationMultiplier by remember { mutableIntStateOf(1) } // 1 = 7d, 2 = 30d, 4 = Permanent
    var previewedItem by remember(selectedCategory, storeItems) {
        mutableStateOf(storeItems.firstOrNull { it.category == selectedCategory } ?: storeItems.firstOrNull())
    }
    var giftDialogTargetItem by remember { mutableStateOf<StoreCatalogItem?>(null) }
    var friendTargetId by remember { mutableStateOf("") }

    val filteredItems = remember(storeItems, selectedCategory) {
        storeItems.filter { it.category == selectedCategory }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.48f, showFireworks = true)

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
                            .testTag("store_back_button")
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
                            text = "المتجر الملكي 🛍️",
                            color = Color(0xFF1C1C28),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "الإطارات • الدخوليات • الآي دي • ثيمات الغرف الشفافة",
                            color = Color(0xFF7E22CE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Shortcut to My Bag (`الحقيبة`)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF9333EA), Color(0xFFEC4899))
                                )
                            )
                            .clickable { onOpenMyAccessories() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "الحقيبة",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "الحقيبة",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Gold Coin Balance Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF2A0F54))
                            .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(50))
                            .clickable { onOpenWallet() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("🪙", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%,d".format(userProfile.goldCoins),
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Live 3D Circular Stage Showcase
            val currentPreview = previewedItem ?: filteredItems.firstOrNull()
            if (currentPreview != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .ornateGolden3DContainer(cornerRadius = 22.dp)
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Circular Avatar + Circular Frame + Colored Soundwave Preview
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(105.dp)
                        ) {
                            Ornate3DAvatarWithFrame(
                                avatarType = userProfile.avatarType,
                                frameStyle = currentPreview.frameStyle ?: userProfile.frameStyle,
                                size = 72.dp,
                                isSpeaking = true,
                                showCrown = true,
                                badgeText = currentPreview.dimensionTag
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ColoredUserNameText(
                                name = userProfile.nickname,
                                primaryColor = currentPreview.primaryColor,
                                secondaryColor = Color(0xFF00E5FF),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "ID: ${currentPreview.specialIdValue ?: userProfile.displayId}",
                                color = Color(0xFFFFF59D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Item Details & Duration Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(currentPreview.iconEmoji, fontSize = 22.sp)
                                Text(
                                    text = currentPreview.nameAr,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = currentPreview.subtitleAr,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Duration Selector (7 Days | 30 Days | Permanent)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    1 to "7 أيام",
                                    2 to "30 يوم",
                                    4 to "دائم 👑"
                                ).forEach { (mult, label) ->
                                    val selected = selectedDurationMultiplier == mult
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (selected) Color(0xFFFFD700) else Color(0xFF1D083B))
                                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(50))
                                            .clickable { selectedDurationMultiplier = mult }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (selected) Color(0xFF1A0736) else Color(0xFFFFF59D),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (currentPreview.category == StoreItemCategory.ENTRY_MOUNTS) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFF281054))
                                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(50))
                                            .clickable { onPreviewEntryMount7D(currentPreview.nameAr) }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircleFilled,
                                            contentDescription = null,
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "معاينة الدخولية 7D",
                                            color = Color(0xFF80D8FF),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFF3E1054))
                                        .border(1.dp, Color(0xFFFF4081), RoundedCornerShape(50))
                                        .clickable { giftDialogTargetItem = currentPreview }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = Color(0xFFFF80AB),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "إهداء بالـ ID",
                                        color = Color(0xFFFF80AB),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Store Category Tabs: الإطارات | الدخوليات | الآي دي | إطارات الدردشة | ثيمات الغرف
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                storeCategories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) DynamicThemeManager.goldVertical3DBrush
                                else Brush.verticalGradient(listOf(Color(0xFF6D28D9), Color(0xFF4C1D95)))
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFF59D) else Color(0xFFD8B4FE),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectedCategory = cat
                                previewedItem = storeItems.firstOrNull { it.category == cat }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "${cat.iconEmoji} ${cat.titleAr}",
                            color = if (isSelected) Color(0xFF1A0736) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }

            // Grid of Store Items
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val isSelectedPreview = previewedItem?.id == item.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = item.primaryColor)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        item.primaryColor.copy(alpha = 0.22f),
                                        Color(0xFF210A46),
                                        Color(0xFF14052E)
                                    )
                                )
                            )
                            .border(
                                width = if (isSelectedPreview) 2.2.dp else 1.2.dp,
                                color = if (isSelectedPreview) Color(0xFFFFD700) else item.primaryColor.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { previewedItem = item }
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Metallic3DBadgeTag(
                                text = item.dimensionTag,
                                primaryColor = item.primaryColor,
                                secondaryColor = item.secondaryColor
                            )
                            if (item.isOwned) {
                                Text(
                                    text = "مملوك ✓",
                                    color = Color(0xFF00E676),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Circular Preview for Frames, or 3D Icon for Mounts / Special IDs
                        if (item.category == StoreItemCategory.FRAMES && item.frameStyle != null) {
                            Ornate3DAvatarWithFrame(
                                avatarType = userProfile.avatarType,
                                frameStyle = item.frameStyle,
                                size = 60.dp,
                                isSpeaking = true,
                                showCrown = true
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(66.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(item.primaryColor.copy(alpha = 0.45f), Color(0xFF1A0736))
                                        )
                                    )
                                    .border(2.dp, item.primaryColor, CircleShape)
                            ) {
                                Text(text = item.iconEmoji, fontSize = 30.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.nameAr,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.subtitleAr,
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "%,d 🪙".format(item.priceCoins),
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Buy or Equip Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glossy3DButton(cornerRadius = 50.dp)
                                .clickable { onBuyOrEquipItem(item) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = when {
                                    item.isEquipped -> "مُفعّل حالياً ✓"
                                    item.isOwned -> "تفعيل الآن ✨"
                                    else -> "شراء وتفعيل 🛍️"
                                },
                                color = Color(0xFF1A0736),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Gift Item to Friend by ID Dialog
        if (giftDialogTargetItem != null) {
            val targetItem = giftDialogTargetItem!!
            AlertDialog(
                onDismissRequest = { giftDialogTargetItem = null },
                containerColor = Color(0xFF210942),
                title = {
                    Text(
                        text = "🎁 إهداء ${targetItem.nameAr}",
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "أدخل الآي دي (ID) الخاص بالمستخدم لإرسال هذا العنصر إليه فوراً:",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = friendTargetId,
                            onValueChange = { friendTargetId = it },
                            label = { Text("آي دي المستلم (ID)", color = Color(0xFFFFF59D)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFF7E57C2)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (friendTargetId.isNotBlank()) {
                                onGiftStoreItemToFriend(targetItem, friendTargetId.trim())
                                friendTargetId = ""
                                giftDialogTargetItem = null
                            }
                        }
                    ) {
                        Text("إرسال الهدية (${targetItem.priceCoins} 🪙)", color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { giftDialogTargetItem = null }) {
                        Text("إلغاء", color = Color.White)
                    }
                }
            )
        }
    }
}

/**
 * 2. Standalone Full-Screen Bag (`الحقيبة: تعرض ما أمتلكه من إطارات ودخوليات`)
 * Displays strictly ONLY the 2 owned categories requested:
 * - قسم الإطارات المملوكة (`StoreItemCategory.FRAMES`)
 * - قسم الدخوليات المملوكة (`StoreItemCategory.ENTRY_MOUNTS`)
 */
@Composable
fun StandaloneBagWardrobeScreen(
    userProfile: UserProfile,
    storeItems: List<StoreCatalogItem>,
    onEquipOwnedItem: (StoreCatalogItem) -> Unit,
    onPreviewEntryMount7D: (String) -> Unit,
    onOpenStore: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val accessoriesCategories = listOf(
        StoreItemCategory.FRAMES to "الإطارات ⭕",
        StoreItemCategory.ENTRY_MOUNTS to "الدخوليات 🏎️",
        StoreItemCategory.CHAT_BUBBLES to "إطارات الدردشة 💬",
        StoreItemCategory.ROOM_THEMES to "ثيمات الغرف 🌌"
    )
    var selectedCategory by remember { mutableStateOf(StoreItemCategory.FRAMES) }

    val ownedItemsInCategory = remember(storeItems, selectedCategory) {
        storeItems.filter { it.category == selectedCategory && it.isOwned }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.48f, showFireworks = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Header (Unified with Main Interfaces style)
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
                            text = "الحقيبة الملكية 🎒",
                            color = Color(0xFF1C1C28),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "ما أمتلكه من إطارات ودخوليات وثيمات غرف شفافة",
                            color = Color(0xFF7E22CE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF9333EA), Color(0xFFEC4899))
                            )
                        )
                        .clickable { onOpenStore() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "المتجر 🛍️",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Live Equipped Frame & Entry Mount Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .ornateGolden3DContainer(cornerRadius = 22.dp)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Ornate3DAvatarWithFrame(
                        avatarType = userProfile.avatarType,
                        frameStyle = userProfile.frameStyle,
                        size = 72.dp,
                        isSpeaking = true,
                        showCrown = true,
                        badgeText = "VIP ${userProfile.vipTier}"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        ColoredUserNameText(
                            name = userProfile.nickname,
                            primaryColor = userProfile.frameStyle.primaryColor,
                            secondaryColor = Color(0xFF00E5FF),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⭕ الإطار المُفعّل: ${userProfile.frameStyle.nameAr}",
                            color = Color(0xFFFFF59D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🏎️ الدخولية المُفعّلة: ${userProfile.entryWelcomeName}",
                            color = Color(0xFF80D8FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3 Owned Accessories Category Tabs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                accessoriesCategories.forEach { (cat, labelAr) ->
                    val isSelected = selectedCategory == cat
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) DynamicThemeManager.goldVertical3DBrush
                                else Brush.verticalGradient(listOf(Color(0xFF280F54), Color(0xFF170736)))
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFF59D) else Color(0xFF7E57C2),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = labelAr,
                            color = if (isSelected) Color(0xFF1A0736) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }

            // Grid of Owned Accessories
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(ownedItemsInCategory, key = { it.id }) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        item.primaryColor.copy(alpha = 0.25f),
                                        Color(0xFF210A46),
                                        Color(0xFF14052E)
                                    )
                                )
                            )
                            .border(
                                width = if (item.isEquipped) 2.2.dp else 1.2.dp,
                                color = if (item.isEquipped) Color(0xFF00E676) else item.primaryColor,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Metallic3DBadgeTag(
                                text = item.dimensionTag,
                                primaryColor = item.primaryColor,
                                secondaryColor = item.secondaryColor
                            )
                            if (item.isEquipped) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "مُفعّل",
                                        color = Color(0xFF00E676),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (item.category == StoreItemCategory.FRAMES && item.frameStyle != null) {
                            Ornate3DAvatarWithFrame(
                                avatarType = userProfile.avatarType,
                                frameStyle = item.frameStyle,
                                size = 60.dp,
                                isSpeaking = true,
                                showCrown = true
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(item.primaryColor.copy(alpha = 0.45f), Color(0xFF1A0736))
                                        )
                                    )
                                    .border(2.dp, item.primaryColor, CircleShape)
                            ) {
                                Text(text = item.iconEmoji, fontSize = 28.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.nameAr,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.subtitleAr,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (item.category == StoreItemCategory.ENTRY_MOUNTS) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF281054))
                                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(50))
                                    .clickable { onPreviewEntryMount7D(item.nameAr) }
                                    .padding(vertical = 5.dp)
                            ) {
                                Text(
                                    text = "معاينة حركة الدخولية 🎬",
                                    color = Color(0xFF80D8FF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .glossy3DButton(cornerRadius = 50.dp)
                                .clickable { onEquipOwnedItem(item) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = if (item.isEquipped) "مُفعّل على حسابك ✓" else "ارتداء وتفعيل الآن ✨",
                                color = Color(0xFF1A0736),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

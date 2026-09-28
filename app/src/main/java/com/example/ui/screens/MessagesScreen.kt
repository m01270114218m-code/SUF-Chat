package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.models.DirectChatMessage
import com.example.models.GiftCatalogItem
import com.example.models.MessageThread

@Composable
fun MessagesScreen(
    conversations: List<MessageThread>,
    activeConversationId: String?,
    giftsCatalog: List<GiftCatalogItem>,
    onOpenConversation: (String) -> Unit,
    onCloseConversation: () -> Unit,
    onSendDirectMessage: (String, String) -> Unit,
    onSendGiftInChat: (GiftCatalogItem, String) -> Unit,
    onInspectFriendProfile: (MessageThread) -> Unit = {}
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var officialUnread by rememberSaveable { mutableIntStateOf(1) }
    var familyUnread by rememberSaveable { mutableIntStateOf(7) }
    var showAddFriendDialog by rememberSaveable { mutableStateOf(false) }
    var addFriendIdInput by rememberSaveable { mutableStateOf("") }
    var openedSystemChatTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var messageInputText by rememberSaveable { mutableStateOf("") }

    val activeThread = conversations.firstOrNull { it.id == activeConversationId }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        if (activeThread != null || openedSystemChatTitle != null) {
            val chatTitle = activeThread?.friendName ?: openedSystemChatTitle ?: "الرسائل الرسمية"
            val chatMessages = activeThread?.messages ?: listOf(
                DirectChatMessage(
                    id = "sys_1",
                    senderName = chatTitle,
                    textAr = if (chatTitle == "الرسائل الرسمية") {
                        "فعالية سفينة الفضاء قادمة بقوة! انضم الآن واربح مكافآت وهدايا حصرية داخل الغرف الصوتية."
                    } else {
                        "[أخبار العائلة] انضم بنجاح إلى العائلة الملكية وتفاعل مع الأعضاء الآن!"
                    },
                    timeAr = "09-14",
                    isFromMe = false
                )
            )

            DirectChatDetailView(
                title = chatTitle,
                messages = chatMessages,
                messageInputText = messageInputText,
                onMessageInputChange = { messageInputText = it },
                onSend = {
                    if (messageInputText.isNotBlank()) {
                        onSendDirectMessage(activeThread?.id ?: "sys", messageInputText)
                        messageInputText = ""
                    }
                },
                onSendQuickGift = {
                    giftsCatalog.firstOrNull()?.let { gift ->
                        onSendGiftInChat(gift, chatTitle)
                    }
                },
                onBack = {
                    openedSystemChatTitle = null
                    onCloseConversation()
                }
            )
            return@CompositionLocalProvider
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF6F1FC),
                            Color(0xFFFCFBFD),
                            Color.White
                        ),
                        endY = 500f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // =========================================================================
                // 1. TOP HEADER BAR (Matches Screenshot 4: Add friend + Broom on left, "الرسائل" on right)
                // =========================================================================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Left icons: Add user icon + Clean/Broom icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAddAlt,
                            contentDescription = "إضافة صديق",
                            tint = Color(0xFF232533),
                            modifier = Modifier
                                .size(25.dp)
                                .clickable { showAddFriendDialog = true }
                                .testTag("messages_add_friend_button")
                        )

                        Icon(
                            imageVector = Icons.Outlined.CleaningServices,
                            contentDescription = "تنظيف الإشعارات غير المقروءة",
                            tint = Color(0xFF232533),
                            modifier = Modifier
                                .size(23.dp)
                                .clickable {
                                    officialUnread = 0
                                    familyUnread = 0
                                }
                                .testTag("messages_clear_unread_button")
                        )
                    }

                    // Right title: "الرسائل" with purple underline bar
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الرسائل",
                            color = Color(0xFF1C1C28),
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(3.5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFB358F7), Color(0xFFEC4899))
                                    )
                                )
                        )
                    }
                }

                // =========================================================================
                // 2. SEARCH INPUT BAR ("البحث عن المستخدمين")
                // =========================================================================
                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF3F4F8))
                        .padding(horizontal = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterEnd,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "البحث عن المستخدمين",
                                    color = Color(0xFFA8ADB8),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color(0xFF1F1F2E),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Right
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("messages_search_input")
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = Color(0xFFA8ADB8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // =========================================================================
                // 3. MESSAGE ROWS (Official Messages, Family Center, and Persisted Friend Conversations)
                // =========================================================================
                val filteredThreads = remember(conversations, searchQuery) {
                    if (searchQuery.isBlank()) conversations
                    else conversations.filter {
                        it.friendName.contains(searchQuery, ignoreCase = true) ||
                            it.friendId.contains(searchQuery, ignoreCase = true)
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Row 1: الرسائل الرسمية (Official Messages)
                    item {
                        ScreenshotMessageRowItem(
                            title = "الرسائل الرسمية",
                            subtitle = "فعالية سفينة الفضاء قادمة بقوة! انضم الآن وار...",
                            dateText = "09-14",
                            unreadCount = officialUnread,
                            isOfficialBell = true,
                            onClick = {
                                officialUnread = 0
                                openedSystemChatTitle = "الرسائل الرسمية"
                            },
                            modifier = Modifier.testTag("official_messages_row")
                        )
                    }

                    // Row 2: مركز العائلة (Family Center)
                    item {
                        ScreenshotMessageRowItem(
                            title = "مركز العائلة",
                            subtitle = "[أخبار العائلة] انضم بنجاح إلى العائلة ال...",
                            dateText = "06-21",
                            unreadCount = familyUnread,
                            isOfficialBell = false,
                            onClick = {
                                familyUnread = 0
                                openedSystemChatTitle = "مركز العائلة"
                            },
                            modifier = Modifier.testTag("family_center_messages_row")
                        )
                    }

                    // Persisted Friend Conversations with Real Names & Photos
                    items(filteredThreads, key = { it.id }) { thread ->
                        ScreenshotMessageRowItem(
                            title = thread.friendName,
                            subtitle = thread.lastMessageAr,
                            dateText = thread.lastTimeAr,
                            unreadCount = thread.unreadCount,
                            isOfficialBell = true,
                            friendAvatarType = thread.friendAvatarType,
                            friendCustomAvatarUri = thread.friendCustomAvatarUri,
                            onClick = { onOpenConversation(thread.id) },
                            onAvatarClick = { onInspectFriendProfile(thread) }
                        )
                    }
                }
            }

            if (showAddFriendDialog) {
                AlertDialog(
                    onDismissRequest = { showAddFriendDialog = false },
                    containerColor = Color.White,
                    title = {
                        Text(
                            text = "إضافة صديق بالـ ID",
                            color = Color(0xFF1C1C28),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    },
                    text = {
                        OutlinedTextField(
                            value = addFriendIdInput,
                            onValueChange = { addFriendIdInput = it },
                            label = { Text("أدخل معرف المستخدم ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showAddFriendDialog = false
                                if (conversations.isNotEmpty()) {
                                    onOpenConversation(conversations.first().id)
                                }
                            }
                        ) {
                            Text("بدء محادثة", color = Color(0xFFB358F7), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddFriendDialog = false }) {
                            Text("إلغاء", color = Color(0xFF8B90A0))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ScreenshotMessageRowItem(
    title: String,
    subtitle: String,
    dateText: String,
    unreadCount: Int,
    isOfficialBell: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    friendAvatarType: String? = null,
    friendCustomAvatarUri: String? = null,
    onAvatarClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Left Column: Date on top, Red Unread Badge below
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = dateText,
                color = Color(0xFFB4B8C6),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )

            if (unreadCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4D5E))
                ) {
                    Text(
                        text = unreadCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }
        }

        // Right Row: Text column (Title + Subtitle) + Circular Icon on far right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    color = Color(0xFF1C1C28),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF9EA3B4),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Right
                )
            }

            // Far Right Circular Icon (Friend Real Avatar, Purple Bell, or Golden Family Shield)
            if (friendAvatarType != null || !friendCustomAvatarUri.isNullOrBlank()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDE9FE))
                        .border(1.5.dp, Color(0xFFC084FC), CircleShape)
                        .clickable { onAvatarClick?.invoke() ?: onClick() }
                ) {
                    if (!friendCustomAvatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = Uri.parse(friendCustomAvatarUri),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                id = if (friendAvatarType == "PRINCESS") R.drawable.img_avatar_princess else R.drawable.img_avatar_prince
                            ),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else if (isOfficialBell) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFD07FF9),
                                    Color(0xFF9B3DF2)
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFCD34D),
                                    Color(0xFFF59E0B)
                                )
                            )
                        )
                ) {
                    // White shield + crown emblem inside golden circle
                    Canvas(modifier = Modifier.size(28.dp)) {
                        val w = size.width
                        val h = size.height
                        val shieldPath = Path().apply {
                            moveTo(w * 0.5f, h * 0.08f)
                            lineTo(w * 0.88f, h * 0.24f)
                            lineTo(w * 0.80f, h * 0.68f)
                            lineTo(w * 0.5f, h * 0.92f)
                            lineTo(w * 0.20f, h * 0.68f)
                            lineTo(w * 0.12f, h * 0.24f)
                            close()
                        }
                        drawPath(shieldPath, color = Color.White)
                        drawCircle(
                            color = Color(0xFFF59E0B),
                            radius = w * 0.16f,
                            center = Offset(w * 0.5f, h * 0.48f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectChatDetailView(
    title: String,
    messages: List<DirectChatMessage>,
    messageInputText: String,
    onMessageInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSendQuickGift: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
            .statusBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = Color(0xFF1C1C28)
                )
            }
            Text(
                text = title,
                color = Color(0xFF1C1C28),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onSendQuickGift) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = "إرسال هدية",
                    tint = Color(0xFFEC4899)
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages, key = { it.id }) { msg ->
                Box(
                    contentAlignment = if (msg.isFromMe) Alignment.CenterStart else Alignment.CenterEnd,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = if (msg.isFromMe) Alignment.Start else Alignment.End,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (msg.isFromMe) Color(0xFFB358F7) else Color.White
                            )
                            .border(
                                width = 1.dp,
                                color = if (msg.isFromMe) Color.Transparent else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = msg.textAr,
                            color = if (msg.isFromMe) Color.White else Color(0xFF1F1F2E),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.timeAr,
                            color = if (msg.isFromMe) Color.White.copy(alpha = 0.75f) else Color(0xFF9CA3AF),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB358F7))
                    .testTag("send_direct_message_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "إرسال",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = messageInputText,
                onValueChange = onMessageInputChange,
                placeholder = { Text("اكتب رسالة...") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

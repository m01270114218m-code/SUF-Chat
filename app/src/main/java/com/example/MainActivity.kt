package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.config.DynamicThemeManager
import com.example.models.CasualGameItem
import com.example.models.MainNavTab
import com.example.models.StandaloneSubScreen
import com.example.models.WinTickerNotice
import com.example.ui.components.FullScreenSvga7DOverlay
import com.example.ui.components.ServerDrivenAssetLayer
import com.example.ui.screens.AudioRoomScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.GamesCenterScreen
import com.example.ui.screens.HomeLobbyScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StandaloneAccountSettingsScreen
import com.example.ui.screens.StandaloneAgenciesScreen
import com.example.ui.screens.StandaloneAppPolicyScreen
import com.example.ui.screens.StandaloneBadgesScreen
import com.example.ui.screens.StandaloneBagWardrobeScreen
import com.example.ui.screens.StandaloneChargeAgencyScreen
import com.example.ui.screens.StandaloneCpRelationshipsScreen
import com.example.ui.screens.StandaloneCustomerServiceScreen
import com.example.ui.screens.StandaloneDailyTasksScreen
import com.example.ui.screens.StandaloneDiamondsOnlyScreen
import com.example.ui.screens.StandaloneEventCenterScreen
import com.example.ui.screens.StandaloneFamilyClanScreen
import com.example.ui.screens.StandaloneGiftAtlasScreen
import com.example.ui.screens.StandaloneInnerAccountDetailScreen
import com.example.ui.screens.StandaloneInviteFriendsScreen
import com.example.ui.screens.StandaloneLanguageSettingsScreen
import com.example.ui.screens.StandaloneLeaderboardScreen
import com.example.ui.screens.StandaloneLevelsScreen
import com.example.ui.screens.StandaloneStoreScreen
import com.example.ui.screens.StandaloneVipNobilityScreen
import com.example.ui.screens.StandaloneWalletScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AgencyAuthorizationViewModel
import com.example.viewmodel.MainVoiceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ZadiraRoyalVoiceApp()
            }
        }
    }
}

@Composable
fun ZadiraRoyalVoiceApp(
    viewModel: MainVoiceViewModel = viewModel(),
    agencyAuthViewModel: AgencyAuthorizationViewModel = viewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
    val activeSubScreen by viewModel.activeSubScreen.collectAsStateWithLifecycle()
    val inspectedAccountProfile by viewModel.inspectedAccountProfile.collectAsStateWithLifecycle()
    val selectedHomeCategory by viewModel.selectedHomeCategory.collectAsStateWithLifecycle()
    val selectedRoomChatFilter by viewModel.selectedRoomChatFilter.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val agencyAuthState by agencyAuthViewModel.authorizationState.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val activeRoom by viewModel.activeRoom.collectAsStateWithLifecycle()
    val roomMessages by viewModel.roomMessages.collectAsStateWithLifecycle()
    val isMyMicMuted by viewModel.isMyMicMuted.collectAsStateWithLifecycle()
    val activeSvgaOverlay by viewModel.activeSvgaOverlay.collectAsStateWithLifecycle()
    val bannerToastText by viewModel.bannerToastText.collectAsStateWithLifecycle()
    val discoverPosts by viewModel.discoverPosts.collectAsStateWithLifecycle()
    val messageThreads by viewModel.messageThreads.collectAsStateWithLifecycle()
    val activeChatThread by viewModel.activeChatThread.collectAsStateWithLifecycle()
    val storeItems by viewModel.storeItems.collectAsStateWithLifecycle()
    val walletTransactions by viewModel.walletTransactions.collectAsStateWithLifecycle()
    val dailyTasks by viewModel.dailyTasks.collectAsStateWithLifecycle()
    val leaderboardEntries by viewModel.leaderboardEntries.collectAsStateWithLifecycle()
    val agencyHostsMembers by viewModel.agencyHostsMembers.collectAsStateWithLifecycle()
    val chargeAgentShipmentLogs by viewModel.chargeAgentShipmentLogs.collectAsStateWithLifecycle()
    val homeBanners by viewModel.homeBanners.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Attach Room Database (ZadiraDatabase) for offline user data, room settings, chat history, and database banners
    LaunchedEffect(Unit) {
        viewModel.attachRoomDatabase(context)
    }

    // Synchronize the dedicated AgencyAuthorizationViewModel with server-side flags & user profile
    LaunchedEffect(userProfile) {
        agencyAuthViewModel.bindUserProfile(userProfile)
    }

    var activePlayableGame by remember { mutableStateOf<CasualGameItem?>(null) }

    if (!isLoggedIn) {
        AuthScreen(
            authErrorMessage = authErrorMessage,
            onLoginSuccess = { email, password, nickname, avatarType ->
                viewModel.loginOrRegister(
                    email = email,
                    password = password,
                    nickname = nickname,
                    avatarType = avatarType
                )
            }
        )
        return
    }

    val currentTab = MainNavTab.entries.getOrElse(selectedTabIndex) { MainNavTab.HOME }

    if (activeSubScreen != StandaloneSubScreen.NONE) {
        BackHandler {
            viewModel.closeSubScreen()
        }
    } else if (activeRoom != null) {
        BackHandler {
            viewModel.leaveVoiceRoom()
        }
    } else if (selectedTabIndex != 0) {
        BackHandler {
            viewModel.selectMainTab(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (activeRoom != null && activeSubScreen == StandaloneSubScreen.NONE) {
            AudioRoomScreen(
                room = activeRoom!!,
                currentUser = userProfile,
                messages = roomMessages,
                selectedChatFilter = selectedRoomChatFilter,
                isMyMicMuted = isMyMicMuted,
                giftsCatalog = viewModel.giftsCatalog,
                gamesCatalog = viewModel.gamesCatalog,
                storeItems = storeItems,
                onSelectChatFilter = { filter -> viewModel.setRoomChatFilter(filter) },
                onToggleMyMic = { viewModel.toggleMyMicMute() },
                onTakeOrLeaveSeat = { seatIdx -> viewModel.takeOrLeaveSeat(seatIdx) },
                onSendChatMessage = { text -> viewModel.sendRoomChatMessage(text) },
                onSendGift = { gift, receiver, combo ->
                    viewModel.sendGiftInRoom(gift, receiver, combo)
                },
                onPlayMiniGame = { game -> viewModel.playMiniGameInRoom(game) },
                onTogglePkBattle = { viewModel.toggleRoomPkBattle() },
                onSupportPkTeam = { isRed -> viewModel.supportPkTeam(isRed) },
                onClaimLuckyBag = { viewModel.grabRoomLuckyBag() },
                onDropLuckyBag = { coins -> viewModel.dropRoomLuckyBag(coins) },
                onResetMicCounters = { viewModel.resetRoomMicGiftCounters() },
                onToggleLockSeat = { seatIdx -> viewModel.toggleLockRoomSeat(seatIdx) },
                onChangeRoomBackground = { bgId -> viewModel.changeRoomBackgroundStyle(bgId) },
                onChangeRoomMicShape = { micShapeId -> viewModel.changeRoomMicShapeStyle(micShapeId) },
                onBuyOrEquipRoomTheme = { item -> viewModel.buyOrEquipStoreItem(item) },
                onChangeMyRoomRole = { newRole -> viewModel.changeMyRoomRole(newRole) },
                onEditRoomNameAndPhoto = { newTitle, newCover, newCustomCoverUri ->
                    viewModel.editRoomNameAndPhoto(newTitle, newCover, newCustomCoverUri)
                },
                onUpdateRoomCustomCoverPhoto = { uriString ->
                    viewModel.updateCurrentRoomCustomCover(uriString)
                },
                onInspectSeatInnerAccount = { seat ->
                    if (seat.occupantId == userProfile.uuid || seat.occupantId == userProfile.displayId) {
                        seat.occupantCustomAvatarUri?.let { viewModel.updateUserCustomAvatar(it) }
                    }
                    viewModel.openInnerAccountDetailsFromSeat(seat)
                },
                onOpenStoreFromRoom = {
                    viewModel.leaveVoiceRoom()
                    viewModel.openSubScreen(StandaloneSubScreen.STORE)
                },
                onFollowRoomUser = { targetId, targetName ->
                    viewModel.followRoomUser(targetId, targetName)
                },
                onKickUserFromSeat = { seatIdx, targetName ->
                    viewModel.kickUserFromSeat(seatIdx, targetName)
                },
                onMuteUserOnSeat = { seatIdx, targetName ->
                    viewModel.muteUserOnSeat(seatIdx, targetName)
                },
                onBanUserFromRoom = { seatIdx, targetId, targetName ->
                    viewModel.banUserFromRoom(seatIdx, targetId, targetName)
                },
                onAssignRoomAdmin = { targetId, targetName ->
                    viewModel.assignRoomAdmin(targetId, targetName)
                },
                onSendSeatEmojiReaction = { emoji, titleAr ->
                    viewModel.sendSeatEmojiReaction(emoji, titleAr)
                },
                onRollDiceInRoom = { viewModel.rollDiceInRoom() },
                onLeaveRoom = { viewModel.leaveVoiceRoom() }
            )
        } else if (activeSubScreen != StandaloneSubScreen.NONE) {
            AnimatedContent(
                targetState = activeSubScreen,
                transitionSpec = {
                    (fadeIn(tween(240)) + scaleIn(initialScale = 0.96f, animationSpec = tween(240)))
                        .togetherWith(fadeOut(tween(180)) + scaleOut(targetScale = 0.96f, animationSpec = tween(180)))
                },
                label = "standalone_subscreen_transition",
                modifier = Modifier.fillMaxSize()
            ) { subScreen ->
                when (subScreen) {
                    StandaloneSubScreen.STORE -> StandaloneStoreScreen(
                        userProfile = userProfile,
                        storeItems = storeItems,
                        onBuyOrEquipItem = { item -> viewModel.buyOrEquipStoreItem(item) },
                        onGiftStoreItemToFriend = { item, targetId ->
                            viewModel.giftStoreItemToFriend(item, targetId)
                        },
                        onPreviewEntryMount7D = { mountName ->
                            viewModel.triggerEntryWelcomePreview(mountName)
                        },
                        onOpenMyAccessories = {
                            viewModel.openSubScreen(StandaloneSubScreen.BAG_WARDROBE)
                        },
                        onOpenWallet = {
                            viewModel.openSubScreen(StandaloneSubScreen.WALLET)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.BAG_WARDROBE -> StandaloneBagWardrobeScreen(
                        userProfile = userProfile,
                        storeItems = storeItems,
                        onEquipOwnedItem = { item -> viewModel.buyOrEquipStoreItem(item) },
                        onPreviewEntryMount7D = { mountName ->
                            viewModel.triggerEntryWelcomePreview(mountName)
                        },
                        onOpenStore = {
                            viewModel.openSubScreen(StandaloneSubScreen.STORE)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.VIP_NOBILITY -> StandaloneVipNobilityScreen(
                        userProfile = userProfile,
                        onUpgradeVip = { tier, cost -> viewModel.upgradeVipTier(tier, cost) },
                        onPreviewVipMount = { mountName ->
                            viewModel.triggerEntryWelcomePreview(mountName)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.WALLET, StandaloneSubScreen.COINS_ONLY -> StandaloneWalletScreen(
                        userProfile = userProfile,
                        transactions = walletTransactions,
                        onRechargeCoins = { coins -> viewModel.rechargeGoldCoins(coins) },
                        onExchangeDiamonds = { diamonds ->
                            viewModel.exchangeDiamondsToCoins(diamonds)
                        },
                        onOpenAgencies = {
                            viewModel.openSubScreen(StandaloneSubScreen.AGENCIES)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.DIAMONDS_ONLY -> StandaloneDiamondsOnlyScreen(
                        userProfile = userProfile,
                        onExchangeDiamonds = { diamonds ->
                            viewModel.exchangeDiamondsToCoins(diamonds)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.LANGUAGE_SETTINGS -> StandaloneLanguageSettingsScreen(
                        currentLanguageAr = userProfile.appLanguageNameAr,
                        onSelectLanguage = { langNameAr ->
                            viewModel.updateAppLanguage(langNameAr)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.CUSTOMER_SERVICE -> StandaloneCustomerServiceScreen(
                        userProfile = userProfile,
                        onSubmitTicket = { category, msg ->
                            viewModel.submitCustomerServiceTicket(category, msg)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.INVITE_FRIENDS -> StandaloneInviteFriendsScreen(
                        userProfile = userProfile,
                        onInviteNewFriend = {
                            viewModel.inviteFriendAndIncrementCounter()
                        },
                        onClaimMilestoneReward = { milestone, coins, frame ->
                            viewModel.claimInviteFriendReward(milestone, coins, frame)
                        },
                        onCopyInviteCode = { code ->
                            viewModel.showToast("📋 تم نسخ كود الدعوة الملكي الخاص بك: $code")
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.AGENCIES -> StandaloneAgenciesScreen(
                        userProfile = userProfile,
                        hostMembers = agencyHostsMembers,
                        onInviteUserToAgencyById = { targetId, hostName ->
                            viewModel.inviteUserToAgencyById(targetId, hostName)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.CHARGE_AGENCY -> StandaloneChargeAgencyScreen(
                        userProfile = userProfile,
                        shipmentLogs = chargeAgentShipmentLogs,
                        onShipCoinsToUserById = { targetId, amount ->
                            viewModel.shipCoinsAsChargeAgent(targetId, amount)
                        },
                        onSendAgentRewardById = { targetId, reward ->
                            viewModel.sendChargeAgentRewardById(targetId, reward)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.DAILY_TASKS -> StandaloneDailyTasksScreen(
                        userProfile = userProfile,
                        dailyTasks = dailyTasks,
                        onClaimCheckIn = { viewModel.claimDailyCheckIn() },
                        onClaimTask = { task -> viewModel.claimDailyTask(task) },
                        onSpinWheel = { prizeCoins -> viewModel.spinLuckyWheel(prizeCoins) },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.LEADERBOARD -> StandaloneLeaderboardScreen(
                        entries = leaderboardEntries,
                        onBack = { viewModel.closeSubScreen() },
                        onOpenUserProfile = { entry ->
                            viewModel.openAccountDetailsByDisplayId(
                                displayId = entry.displayId,
                                fallbackName = entry.nameAr,
                                fallbackAvatarType = entry.avatarType,
                                fallbackCustomAvatarUri = entry.customAvatarUri,
                                fallbackFrame = entry.frameStyle,
                                fallbackCountryFlag = entry.countryFlag,
                                fallbackWealthLv = entry.level,
                                fallbackVip = entry.vipTier
                            )
                        }
                    )

                    StandaloneSubScreen.BADGES -> StandaloneBadgesScreen(
                        userProfile = userProfile,
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.LEVELS -> StandaloneLevelsScreen(
                        userProfile = userProfile,
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.CP_RELATIONSHIPS -> StandaloneCpRelationshipsScreen(
                        userProfile = userProfile,
                        onBoostCpIntimacy = { viewModel.boostCpIntimacy() },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.FAMILY_CLAN -> StandaloneFamilyClanScreen(
                        userProfile = userProfile,
                        onEnterFamilyRoom = {
                            viewModel.closeSubScreen()
                            rooms.firstOrNull()?.let { viewModel.enterVoiceRoom(it) }
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.GIFT_ATLAS -> StandaloneGiftAtlasScreen(
                        giftsCatalog = viewModel.giftsCatalog,
                        onPreviewGiftSvga = { titleAr, _ ->
                            viewModel.triggerEntryWelcomePreview(titleAr)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.EVENT_CENTER -> StandaloneEventCenterScreen(
                        onClaimEventBonus = { bonusCoins ->
                            viewModel.rechargeGoldCoins(bonusCoins)
                        },
                        onPreviewDragonSvga = { eventTitle ->
                            viewModel.triggerEntryWelcomePreview(eventTitle)
                        },
                        onBack = { viewModel.closeSubScreen() }
                    )

                    StandaloneSubScreen.ACCOUNT_SETTINGS -> StandaloneAccountSettingsScreen(
                        userProfile = userProfile,
                        onSaveProfileInfo = { name, bio, flag, country ->
                            viewModel.updateProfileDetails(name, bio, flag, country)
                        },
                        onToggleLinkAccount = { provider ->
                            viewModel.toggleLinkAccount(provider)
                        },
                        onOpenAppPolicy = {
                            viewModel.openSubScreen(StandaloneSubScreen.APP_POLICY)
                        },
                        onLogout = { viewModel.logoutAccount() },
                        onDeleteAccount = { viewModel.deleteAccountPermanently() },
                        onBack = { viewModel.closeSubScreen() },
                        onPickAvatarFromDeviceStorage = { uriString ->
                            viewModel.updateUserCustomAvatar(uriString)
                        }
                    )

                    StandaloneSubScreen.APP_POLICY -> StandaloneAppPolicyScreen(
                        onBack = {
                            viewModel.openSubScreen(StandaloneSubScreen.ACCOUNT_SETTINGS)
                        }
                    )

                    StandaloneSubScreen.INNER_ACCOUNT_DETAIL -> {
                        val targetAccount = inspectedAccountProfile ?: userProfile
                        StandaloneInnerAccountDetailScreen(
                            accountProfile = targetAccount,
                            isMyOwnAccount = targetAccount.displayId == userProfile.displayId,
                            onPickAvatarFromDeviceStorage = { uriString ->
                                viewModel.updateUserCustomAvatar(uriString)
                            },
                            onPickCoverFromDeviceStorage = { uriString ->
                                viewModel.updateUserCustomCover(uriString)
                            },
                            onCopyIdNotice = { idText ->
                                viewModel.showToast("📋 تم نسخ المعرف الملكي ID: $idText")
                            },
                            onOpenSubScreen = { nextScreen ->
                                viewModel.openSubScreen(nextScreen)
                            },
                            onPreviewEntryMount = { mountName ->
                                viewModel.triggerEntryWelcomePreview(mountName)
                            },
                            onBack = { viewModel.closeInnerAccountDetails() }
                        )
                    }

                    StandaloneSubScreen.NONE -> {}
                }
            }
        } else {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = Color.White,
                bottomBar = {
                    Royal3DBottomNavigationBar(
                        currentTab = currentTab,
                        unreadMessagesCount = messageThreads.sumOf { it.unreadCount },
                        onSelectTab = { tab ->
                            viewModel.selectMainTab(MainNavTab.entries.indexOf(tab))
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            (fadeIn(tween(240)) + scaleIn(initialScale = 0.96f, animationSpec = tween(240)))
                                .togetherWith(fadeOut(tween(180)) + scaleOut(targetScale = 0.96f, animationSpec = tween(180)))
                        },
                        label = "main_tab_3d_transition"
                    ) { tab ->
                        when (tab) {
                            MainNavTab.HOME -> HomeLobbyScreen(
                                rooms = rooms,
                                selectedCountryFilter = selectedHomeCategory,
                                onSelectCountryFilter = { cat -> viewModel.setHomeCategory(cat) },
                                onEnterRoom = { room -> viewModel.enterVoiceRoom(room) },
                                onCreateRoom = { title, category, announcement, customCoverUri ->
                                    viewModel.createAndEnterMyRoom(title, category, announcement, customCoverUri)
                                },
                                onOpenSubScreen = { screen ->
                                    viewModel.openSubScreen(screen)
                                },
                                onClaimFirstRecharge = {
                                    viewModel.rechargeGoldCoins(10000L)
                                },
                                databaseBanners = homeBanners
                            )

                            MainNavTab.DISCOVER -> DiscoverScreen(
                                posts = discoverPosts,
                                giftsCatalog = viewModel.giftsCatalog,
                                onToggleLike = { postId -> viewModel.toggleLikePost(postId) },
                                onAddComment = { _, comment ->
                                    viewModel.showToast("💬 تم إضافة تعليقك: $comment")
                                },
                                onPublishPost = { content, _ ->
                                    viewModel.createFriendMomentPost(content, "غرفتي الملكية 7D")
                                },
                                onSendGiftToAuthor = { gift, authorName ->
                                    viewModel.sendGiftInRoom(gift, authorName, 1)
                                },
                                onPublishPostWithImage = { content, roomTag, imageUri ->
                                    viewModel.createFriendMomentPost(content, roomTag, imageUri)
                                },
                                onInspectAuthorProfile = { post ->
                                    viewModel.openAccountDetailsByDisplayId(
                                        displayId = post.authorDisplayId,
                                        fallbackName = post.authorName,
                                        fallbackAvatarType = post.authorAvatarType,
                                        fallbackCustomAvatarUri = post.authorCustomAvatarUri,
                                        fallbackFrame = post.authorFrame,
                                        fallbackCountryFlag = post.authorCountryFlag,
                                        fallbackWealthLv = post.authorLevel
                                    )
                                }
                            )

                            MainNavTab.GAMES -> GamesCenterScreen(
                                userProfile = userProfile,
                                gamesCatalog = viewModel.gamesCatalog,
                                winTickers = listOf(
                                    WinTickerNotice(
                                        id = "win_1",
                                        winnerName = "${userProfile.nickname} 👑",
                                        winAmount = 150000L,
                                        gameTitleAr = "إمبراطورية الذهب 7D"
                                    ),
                                    WinTickerNotice(
                                        id = "win_2",
                                        winnerName = "الأميرة شهد 🌸",
                                        winAmount = 88000L,
                                        gameTitleAr = "التنين ضد النمر"
                                    )
                                ),
                                activePlayableGame = activePlayableGame,
                                lastGameWinResult = bannerToastText,
                                onOpenGame = { game -> activePlayableGame = game },
                                onPlayRound = { _ ->
                                    activePlayableGame?.let { viewModel.playMiniGameInRoom(it) }
                                },
                                onCloseGame = { activePlayableGame = null }
                            )

                            MainNavTab.MESSAGES -> MessagesScreen(
                                conversations = messageThreads,
                                activeConversationId = activeChatThread?.id,
                                giftsCatalog = viewModel.giftsCatalog,
                                onOpenConversation = { threadId ->
                                    messageThreads.firstOrNull { it.id == threadId }
                                        ?.let { viewModel.openMessageThread(it) }
                                },
                                onCloseConversation = { viewModel.closeMessageThread() },
                                onSendDirectMessage = { _, text -> viewModel.sendDirectMessage(text) },
                                onSendGiftInChat = { gift, _ ->
                                    viewModel.sendGiftInRoom(
                                        gift,
                                        activeChatThread?.friendName ?: "صديق",
                                        1
                                    )
                                },
                                onInspectFriendProfile = { thread ->
                                    viewModel.openAccountDetailsByDisplayId(
                                        displayId = thread.friendDisplayId,
                                        fallbackName = thread.friendName,
                                        fallbackAvatarType = thread.friendAvatarType,
                                        fallbackCustomAvatarUri = thread.friendCustomAvatarUri,
                                        fallbackFrame = thread.friendFrame,
                                        fallbackCountryFlag = thread.countryFlag,
                                        fallbackWealthLv = thread.level
                                    )
                                }
                            )

                            MainNavTab.PROFILE -> ProfileScreen(
                                userProfile = userProfile,
                                agencyAuthState = agencyAuthState,
                                onOpenSubScreen = { screen ->
                                    viewModel.openSubScreen(screen)
                                },
                                onOpenInnerAccountDetails = {
                                    viewModel.openInnerAccountDetails()
                                },
                                onPickAccountPhotoFromStorage = { uriString ->
                                    viewModel.updateUserCustomAvatar(uriString)
                                },
                                onCopyIdNotice = {
                                    viewModel.showToast("📋 تم نسخ معرف حسابك ID: ${userProfile.displayId}")
                                }
                            )
                        }
                    }
                }
            }
        }

        // Remote visual studio layer: transparent PNG/WebP/GIF/SVGA assets positioned from Supabase.
        val remoteArea = if (activeRoom != null) "room" else currentTab.name.lowercase()
        ServerDrivenAssetLayer(
            area = remoteArea,
            modifier = Modifier.fillMaxSize()
        )

        // Full-Screen 7D SVGA Gift / Entry Mount Overlay
        activeSvgaOverlay?.let { svga ->
            FullScreenSvga7DOverlay(
                effectType = svga.effectType,
                titleAr = svga.titleAr,
                senderName = svga.senderName,
                receiverName = svga.receiverName,
                comboCount = svga.comboCount,
                isEntranceMount = svga.isEntranceMount,
                assetFormat = svga.assetFormat,
                customAssetUri = svga.customAssetUri,
                onDismiss = { viewModel.dismissSvgaOverlay() }
            )
        }

        // Small, transparent, colorful system notification at the bottom of the screen
        // Never displayed on screen inside the voice room (room events appear in the bottom chat stream)
        LaunchedEffect(activeRoom, bannerToastText) {
            if (activeRoom != null && bannerToastText != null) {
                viewModel.clearToast()
            } else if (bannerToastText != null) {
                kotlinx.coroutines.delay(2600L)
                viewModel.clearToast()
            }
        }

        if (activeRoom == null && bannerToastText != null) {
            val message = bannerToastText!!
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 68.dp, start = 24.dp, end = 24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xBF2E1065),
                                Color(0xBF7E22CE),
                                Color(0xBFBE185D)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xAAFFD700),
                                Color(0xAA38BDF8),
                                Color(0xAAEC4899)
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { viewModel.clearToast() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✨",
                        fontSize = 12.sp
                    )
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun Royal3DBottomNavigationBar(
    currentTab: MainNavTab,
    unreadMessagesCount: Int,
    onSelectTab: (MainNavTab) -> Unit
) {
    val orderedTabs = listOf(
        MainNavTab.PROFILE,
        MainNavTab.MESSAGES,
        MainNavTab.DISCOVER,
        MainNavTab.HOME
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, spotColor = Color(0x1A000000))
                .background(Color.White)
                .border(width = 0.6.dp, color = Color(0xFFECEEF4))
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            orderedTabs.forEach { tab ->
                val isSelected = currentTab == tab

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clickable { onSelectTab(tab) }
                        .testTag("nav_tab_${tab.name.lowercase()}")
                ) {
                    Canvas(modifier = Modifier.size(28.dp)) {
                        val w = size.width
                        val h = size.height
                        val activeBrush = Brush.linearGradient(
                            colors = listOf(Color(0xFF9333EA), Color(0xFFD946EF), Color(0xFFEC4899))
                        )
                        val inactiveColor = Color(0xFFA0A5B5)

                        when (tab) {
                            MainNavTab.HOME -> {
                                val housePath = Path().apply {
                                    moveTo(w * 0.50f, h * 0.12f)
                                    lineTo(w * 0.88f, h * 0.45f)
                                    lineTo(w * 0.80f, h * 0.45f)
                                    lineTo(w * 0.80f, h * 0.86f)
                                    lineTo(w * 0.20f, h * 0.86f)
                                    lineTo(w * 0.20f, h * 0.45f)
                                    lineTo(w * 0.12f, h * 0.45f)
                                    close()
                                }
                                if (isSelected) {
                                    drawPath(path = housePath, brush = activeBrush)
                                    drawArc(
                                        color = Color.White,
                                        startAngle = 20f,
                                        sweepAngle = 140f,
                                        useCenter = false,
                                        topLeft = Offset(w * 0.35f, h * 0.46f),
                                        size = Size(w * 0.30f, h * 0.24f),
                                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                } else {
                                    drawPath(
                                        path = housePath,
                                        color = inactiveColor,
                                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                    drawArc(
                                        color = inactiveColor,
                                        startAngle = 20f,
                                        sweepAngle = 140f,
                                        useCenter = false,
                                        topLeft = Offset(w * 0.35f, h * 0.46f),
                                        size = Size(w * 0.30f, h * 0.24f),
                                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            }

                            MainNavTab.DISCOVER -> {
                                if (isSelected) {
                                    drawCircle(brush = activeBrush, radius = w * 0.28f, center = Offset(w * 0.50f, h * 0.50f))
                                    rotate(degrees = -28f, pivot = Offset(w * 0.50f, h * 0.50f)) {
                                        drawOval(
                                            brush = activeBrush,
                                            topLeft = Offset(w * 0.08f, h * 0.38f),
                                            size = Size(w * 0.84f, h * 0.24f),
                                            style = Stroke(width = 2.2.dp.toPx())
                                        )
                                    }
                                } else {
                                    drawCircle(
                                        color = inactiveColor,
                                        radius = w * 0.27f,
                                        center = Offset(w * 0.50f, h * 0.50f),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    rotate(degrees = -28f, pivot = Offset(w * 0.50f, h * 0.50f)) {
                                        drawOval(
                                            color = inactiveColor,
                                            topLeft = Offset(w * 0.08f, h * 0.38f),
                                            size = Size(w * 0.84f, h * 0.24f),
                                            style = Stroke(width = 1.8.dp.toPx())
                                        )
                                    }
                                }
                            }

                            MainNavTab.MESSAGES -> {
                                if (isSelected) {
                                    drawOval(brush = activeBrush, topLeft = Offset(w * 0.14f, h * 0.16f), size = Size(w * 0.72f, h * 0.64f))
                                    drawCircle(color = Color.White, radius = w * 0.055f, center = Offset(w * 0.40f, h * 0.48f))
                                    drawCircle(color = Color.White, radius = w * 0.055f, center = Offset(w * 0.60f, h * 0.48f))
                                } else {
                                    drawOval(
                                        color = inactiveColor,
                                        topLeft = Offset(w * 0.14f, h * 0.16f),
                                        size = Size(w * 0.72f, h * 0.64f),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    drawCircle(color = inactiveColor, radius = w * 0.05f, center = Offset(w * 0.40f, h * 0.48f))
                                    drawCircle(color = inactiveColor, radius = w * 0.05f, center = Offset(w * 0.60f, h * 0.48f))
                                }
                            }

                            MainNavTab.PROFILE, MainNavTab.GAMES -> {
                                if (isSelected) {
                                    drawCircle(brush = activeBrush, radius = w * 0.20f, center = Offset(w * 0.50f, h * 0.30f))
                                    drawRoundRect(
                                        brush = activeBrush,
                                        topLeft = Offset(w * 0.20f, h * 0.55f),
                                        size = Size(w * 0.60f, h * 0.32f),
                                        cornerRadius = CornerRadius(w * 0.25f, w * 0.25f)
                                    )
                                    drawArc(
                                        color = Color.White,
                                        startAngle = 20f,
                                        sweepAngle = 140f,
                                        useCenter = false,
                                        topLeft = Offset(w * 0.38f, h * 0.60f),
                                        size = Size(w * 0.24f, h * 0.16f),
                                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                } else {
                                    drawCircle(
                                        color = inactiveColor,
                                        radius = w * 0.19f,
                                        center = Offset(w * 0.50f, h * 0.30f),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    drawRoundRect(
                                        color = inactiveColor,
                                        topLeft = Offset(w * 0.20f, h * 0.55f),
                                        size = Size(w * 0.60f, h * 0.30f),
                                        cornerRadius = CornerRadius(w * 0.25f, w * 0.25f),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.config.MasterAppDatabaseTable
import com.example.data.ChatHistoryEntity
import com.example.data.HomeBannerEntity
import com.example.data.RoomSettingsEntity
import com.example.data.UserAccountEntity
import com.example.data.ZadiraAppDao
import com.example.data.ZadiraDatabase
import com.example.services.PharaohSupabaseAuth
import com.example.services.SupabaseRpcClient
import com.example.services.SupabaseRestClient
import com.example.services.RemoteVisualCatalogStore
import com.example.services.RemoteAccessContext
import com.example.services.RemoteAccessContextStore
import com.example.services.RemoteAgencyAccess
import com.example.services.RemoteFeatureAccess
import com.example.models.AppUserRole
import com.example.models.CasualGameItem
import com.example.models.ChargeAgentRewardItem
import com.example.models.ChargeAgentShipmentLog
import com.example.models.ChatBubbleMessage
import com.example.models.DailyTaskItem
import com.example.models.DirectMessageThread
import com.example.models.FrameStyle3D
import com.example.models.FriendPostMoment
import com.example.models.GiftItem3D
import com.example.models.HomeBannerSlideItem
import com.example.models.HostAgencyMemberRecord
import com.example.models.LeaderboardEntry
import com.example.models.MicSeatState
import com.example.models.RoomChatMessage
import com.example.models.RoomMicShapeStyle
import com.example.models.RoomPermissionRole
import com.example.models.StandaloneSubScreen
import com.example.models.StoreCatalogItem
import com.example.models.StoreItemCategory
import com.example.models.SvgaEffectType
import com.example.models.UserProfile
import com.example.models.VoiceRoomModel
import com.example.models.WalletTransactionItem
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveSvgaAnimationState(
    val effectType: SvgaEffectType,
    val titleAr: String,
    val senderName: String,
    val receiverName: String,
    val comboCount: Int = 1,
    val isEntranceMount: Boolean = false,
    val assetFormat: String = "SVGA • GIF • WEBP • MP4 • PNG",
    val customAssetUri: String? = null
)

class MainVoiceViewModel : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _activeSubScreen = MutableStateFlow(StandaloneSubScreen.NONE)
    val activeSubScreen: StateFlow<StandaloneSubScreen> = _activeSubScreen.asStateFlow()

    private val _inspectedAccountProfile = MutableStateFlow<UserProfile?>(null)
    val inspectedAccountProfile: StateFlow<UserProfile?> = _inspectedAccountProfile.asStateFlow()

    private val _selectedHomeCategory = MutableStateFlow("الكل")
    val selectedHomeCategory: StateFlow<String> = _selectedHomeCategory.asStateFlow()

    private val _selectedRoomChatFilter = MutableStateFlow("الكل")
    val selectedRoomChatFilter: StateFlow<String> = _selectedRoomChatFilter.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            uuid = "user_1201637",
            displayId = "1201637",
            isSpecialId = true,
            email = "mohamed@zadiralive.com",
            nickname = "محمد",
            avatarType = "PRINCE",
            countryFlag = "🇪🇬",
            countryNameAr = "مصر",
            role = AppUserRole.SUPPORTER,
            wealthLevel = 45,
            charismaLevel = 38,
            vipTier = 5,
            goldCoins = 285000,
            crystalDiamonds = 142000,
            frameStyle = FrameStyle3D.IMPERIAL_GOLD_WINGS,
            entryWelcomeName = "تنين الإمبراطور الذهبي 7D",
            isHostAgent = false,
            isHostMember = false,
            isChargeAgent = false,
            chargeAgentCoinsBalance = 0L,
            badges = MasterAppDatabaseTable.defaultBadgesSeed()
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _rooms = MutableStateFlow(buildInitialRooms())
    val rooms: StateFlow<List<VoiceRoomModel>> = _rooms.asStateFlow()

    private val _activeRoom = MutableStateFlow<VoiceRoomModel?>(null)
    val activeRoom: StateFlow<VoiceRoomModel?> = _activeRoom.asStateFlow()

    private val _roomMessages = MutableStateFlow(buildInitialRoomMessages())
    val roomMessages: StateFlow<List<RoomChatMessage>> = _roomMessages.asStateFlow()

    private val _isMyMicMuted = MutableStateFlow(false)
    val isMyMicMuted: StateFlow<Boolean> = _isMyMicMuted.asStateFlow()

    private val _activeSvgaOverlay = MutableStateFlow<ActiveSvgaAnimationState?>(null)
    val activeSvgaOverlay: StateFlow<ActiveSvgaAnimationState?> = _activeSvgaOverlay.asStateFlow()

    private val _bannerToastText = MutableStateFlow<String?>(null)
    val bannerToastText: StateFlow<String?> = _bannerToastText.asStateFlow()

    private val _discoverPosts = MutableStateFlow(buildInitialDiscoverPosts())
    val discoverPosts: StateFlow<List<FriendPostMoment>> = _discoverPosts.asStateFlow()

    private val _messageThreads = MutableStateFlow(buildInitialMessageThreads())
    val messageThreads: StateFlow<List<DirectMessageThread>> = _messageThreads.asStateFlow()

    private val _activeChatThread = MutableStateFlow<DirectMessageThread?>(null)
    val activeChatThread: StateFlow<DirectMessageThread?> = _activeChatThread.asStateFlow()

    private var appDao: ZadiraAppDao? = null
    private var isDatabaseAttached = false

    private val _storeItems = MutableStateFlow(MasterAppDatabaseTable.getStoreAndAccessoriesTable())
    val storeItems: StateFlow<List<StoreCatalogItem>> = _storeItems.asStateFlow()

    private val _homeBanners = MutableStateFlow(MasterAppDatabaseTable.homeBannersTable.value)
    val homeBanners: StateFlow<List<HomeBannerSlideItem>> = _homeBanners.asStateFlow()

    val roomAndAppVisualConfig: StateFlow<MasterAppDatabaseTable.RoomVisualCustomizationRecord> =
        MasterAppDatabaseTable.roomAndAppVisualConfigFlow

    private val _walletTransactions = MutableStateFlow(buildInitialWalletTransactions())
    val walletTransactions: StateFlow<List<WalletTransactionItem>> = _walletTransactions.asStateFlow()

    private val _dailyTasks = MutableStateFlow(buildInitialDailyTasks())
    val dailyTasks: StateFlow<List<DailyTaskItem>> = _dailyTasks.asStateFlow()

    private val _leaderboardEntries = MutableStateFlow(buildInitialLeaderboard())
    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> = _leaderboardEntries.asStateFlow()

    private val _agencyHostsMembers = MutableStateFlow(MasterAppDatabaseTable.getAgencyHostsTable())
    val agencyHostsMembers: StateFlow<List<HostAgencyMemberRecord>> = _agencyHostsMembers.asStateFlow()

    private val _chargeAgentShipmentLogs = MutableStateFlow(
        listOf(
            ChargeAgentShipmentLog(
                id = "ship_1",
                targetUserId = "888888",
                descriptionAr = "شحن عملات ذهبية عبر وكالة الشحن",
                coinsShipped = 100000L,
                timestampText = "منذ 15 دقيقة"
            ),
            ChargeAgentShipmentLog(
                id = "ship_2",
                targetUserId = "777777",
                descriptionAr = "إرسال مكافأة إطار أجنحة الإمبراطور الدائري",
                coinsShipped = 0L,
                rewardNameAr = "إطار أجنحة الإمبراطور 👑",
                timestampText = "منذ ساعة"
            )
        )
    )
    val chargeAgentShipmentLogs: StateFlow<List<ChargeAgentShipmentLog>> = _chargeAgentShipmentLogs.asStateFlow()

    val giftsCatalog: List<GiftItem3D> = buildInitialGiftsCatalog()
    val gamesCatalog: List<CasualGameItem> = buildInitialGamesCatalog()

    /**
     * Attaches the local Room Database (`ZadiraDatabase`) to persist and synchronize:
     * 1. Local User Account data (real name, real photo, levels, balances)
     * 2. Local Room Settings (each room's real name and real photo set by the room owner)
     * 3. Local Chat History (in-room messages and private direct messages for offline access)
     * 4. Database-Assigned Home Banners (`تعيين البنارات يكون من قاعدة البيانات وليس من التطبيق`)
     */
    fun attachRoomDatabase(context: Context) {
        if (isDatabaseAttached) return
        isDatabaseAttached = true
        val dao = ZadiraDatabase.getInstance(context).appDao()
        appDao = dao

        viewModelScope.launch {
            // Server is the source of truth for remotely managed visual assets.
            runCatching {
                RemoteVisualCatalogStore.replaceFromJson(SupabaseRestClient.loadUiAssets())
            }
            // 1. Seed & Observe Database-Assigned Home Banners (`home_banners_table`)
            val existingBanners = dao.getAllHomeBanners()
            if (existingBanners.isEmpty()) {
                val seedEntities = MasterAppDatabaseTable.homeBannersTable.value.mapIndexed { idx, b ->
                    b.toEntity(sortOrder = idx + 1)
                }
                dao.upsertHomeBanners(seedEntities)
            }
            launch {
                dao.observeActiveHomeBanners().collectLatest { entities ->
                    if (entities.isNotEmpty()) {
                        val mapped = entities.map { it.toBannerSlideItem() }
                        _homeBanners.value = mapped
                    }
                }
            }

            // 2. Restore or Seed Local User Account (`user_account_table`)
            val savedUser = dao.getFirstSavedUser()
            if (savedUser != null) {
                val restoredProfile = savedUser.toUserProfile()
                _userProfile.value = restoredProfile
                val masterRec = MasterAppDatabaseTable.findAccountByDisplayId(restoredProfile.displayId)
                if (masterRec != null) {
                    masterRec.profile = restoredProfile
                }
            } else {
                dao.saveUserAccount(_userProfile.value.toEntity())
            }

            // 3. Restore or Seed Room Settings (`room_settings_table`)
            val savedRooms = dao.getAllRoomSettings()
            if (savedRooms.isEmpty()) {
                dao.upsertAllRoomSettings(_rooms.value.map { it.toRoomSettingsEntity() })
            } else {
                val byId = savedRooms.associateBy { it.roomId }
                val currentList = _rooms.value
                val mergedRooms = currentList.map { room ->
                    val saved = byId[room.id]
                    if (saved != null) {
                        room.mergeWithSettingsEntity(saved)
                    } else room
                }
                val additionalSaved = savedRooms
                    .filter { s -> currentList.none { it.id == s.roomId } }
                    .map { s -> s.toVoiceRoomModel() }
                _rooms.value = additionalSaved + mergedRooms
            }

            // 4. Restore or Seed Offline Chat History (`chat_history_table`)
            val savedRoomChat = dao.getAllRoomChatHistory()
            if (savedRoomChat.isEmpty()) {
                dao.insertChatHistoryBatch(_roomMessages.value.map { it.toChatHistoryEntity(roomId = "room_1") })
            } else {
                _roomMessages.value = savedRoomChat.map { it.toRoomChatMessage() }
            }

            // Restore Direct Message Threads offline history
            val updatedThreads = _messageThreads.value.map { thread ->
                val savedDm = dao.getDirectChatHistoryForThread(thread.id)
                if (savedDm.isEmpty()) {
                    dao.insertChatHistoryBatch(
                        thread.messages.map { m ->
                            m.toDirectChatEntity(
                                threadId = thread.id,
                                friendName = thread.friendName,
                                friendDisplayId = thread.friendDisplayId,
                                friendCustomAvatarUri = thread.friendCustomAvatarUri,
                                friendAvatarType = thread.friendAvatarType,
                                myProfile = _userProfile.value
                            )
                        }
                    )
                    thread
                } else {
                    thread.copy(messages = savedDm.map { it.toChatBubbleMessage() })
                }
            }
            _messageThreads.value = updatedThreads

            // Synchronize everything across all state flows after loading from Room DB
            syncCurrentProfileToMasterTable()
        }
    }

    /**
     * Production authentication: username/password are the only normal login fields.
     * Supabase owns the account identity and profile; quick login reuses one device-bound account.
     */
    fun loginOrRegister(
        email: String,
        password: String,
        nickname: String?,
        avatarType: String = "PRINCE",
        createAccount: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val remoteUserId = if (email == "__QUICK_LOGIN__") {
                    PharaohSupabaseAuth.quickLogin()
                } else {
                    PharaohSupabaseAuth.loginOrCreate(
                        username = email.trim(),
                        credential = password,
                        nickname = nickname,
                        allowCreate = createAccount
                    )
                }

                val remoteProfile = PharaohSupabaseAuth.loadProfile(remoteUserId)
                refreshRemoteAccessContext()
                refreshStoreCatalogFromSupabase()

                if (remoteProfile.isBanned) {
                    _isLoggedIn.value = false
                    _authErrorMessage.value = "هذا الحساب محظور: لا يمكن الدخول إلى التطبيق."
                    return@launch
                }

                val displayName = remoteProfile.displayName.ifBlank {
                    nickname?.ifBlank { "مستخدم فرعون بارتي" } ?: "مستخدم فرعون بارتي"
                }

                _userProfile.value = _userProfile.value.copy(
                    uuid = remoteProfile.id,
                    displayId = remoteProfile.displayId ?: remoteUserId.takeLast(7),
                    email = "",
                    nickname = displayName,
                    customAvatarUri = remoteProfile.avatarUrl,
                    countryFlag = if (remoteProfile.country == "EG") "🇪🇬" else _userProfile.value.countryFlag,
                    goldCoins = remoteProfile.coins,
                    crystalDiamonds = remoteProfile.diamonds,
                    vipTier = remoteProfile.vipLevel,
                    isHostAgent = remoteProfile.isHostAgent,
                    isHostMember = remoteProfile.isHostMember,
                    isChargeAgent = remoteProfile.isChargeAgent,
                    isBanned = false
                )

                _authErrorMessage.value = null
                _isLoggedIn.value = true
                showToast("👑 مرحباً $displayName • الحساب متصل بـ Supabase • ID: ${_userProfile.value.displayId}")
            } catch (e: Exception) {
                _isLoggedIn.value = false
                _authErrorMessage.value = e.message?.ifBlank { "تعذر تسجيل الدخول إلى قاعدة البيانات" }
                    ?: "تعذر تسجيل الدخول إلى قاعدة البيانات"
            }
        }
    }

    private suspend fun refreshStoreCatalogFromSupabase() {
        runCatching {
            val raw = SupabaseRestClient.loadStoreItemsRaw()
            val arr = org.json.JSONArray(raw)
            val mapped = buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val category = runCatching {
                        StoreItemCategory.valueOf(o.optString("category").uppercase())
                    }.getOrDefault(StoreItemCategory.FRAMES)
                    val frameStyle = o.optString("frame_id").takeIf { it.isNotBlank() }?.let {
                        runCatching { FrameStyle3D.valueOf(it) }.getOrNull()
                    }
                    add(
                        StoreCatalogItem(
                            id = o.optString("id"),
                            nameAr = o.optString("name"),
                            subtitleAr = o.optString("name"),
                            category = category,
                            priceCoins = o.optLong("price_coins"),
                            iconEmoji = o.optString("icon", category.iconEmoji),
                            primaryColor = Color(0xFFFFD700),
                            secondaryColor = Color(0xFF8B5CF6),
                            frameStyle = frameStyle,
                            specialIdValue = if (category == StoreItemCategory.SPECIAL_IDS) o.optString("id") else null,
                            assetFormat = o.optString("mime_type", o.optString("asset_type", "image")),
                            customAssetUri = o.optString("asset_url").takeIf { it.isNotBlank() },
                            isVisible = o.optBoolean("enabled", true)
                        )
                    )
                }
            }
            if (mapped.isNotEmpty()) _storeItems.value = mapped
        }
    }

    private suspend fun refreshRemoteAccessContext() {
        runCatching {
            val raw = SupabaseRpcClient.getMyAccessContext()
            val json = JSONObject(raw)
            val agencies = mutableListOf<RemoteAgencyAccess>()
            val agencyArray = json.optJSONArray("agencies")
            if (agencyArray != null) for (i in 0 until agencyArray.length()) {
                val a = agencyArray.getJSONObject(i)
                agencies += RemoteAgencyAccess(
                    agencyId = a.optString("agency_id"),
                    agencyName = a.optString("agency_name"),
                    agencyType = a.optString("agency_type"),
                    role = a.optString("role"),
                    coinsBalance = a.optLong("coins_balance")
                )
            }
            val features = mutableListOf<RemoteFeatureAccess>()
            val featureArray = json.optJSONArray("feature_controls")
            if (featureArray != null) for (i in 0 until featureArray.length()) {
                val f = featureArray.getJSONObject(i)
                features += RemoteFeatureAccess(
                    featureKey = f.optString("feature_key"),
                    visible = f.optBoolean("visible", true),
                    enabled = f.optBoolean("enabled", true),
                    requiredRole = f.optString("required_role", "ALL")
                )
            }
            RemoteAccessContextStore.set(
                RemoteAccessContext(
                    isChargeAgent = json.optBoolean("is_charge_agent", false),
                    isHostAgent = json.optBoolean("is_host_agent", false),
                    isHost = json.optBoolean("is_host", false),
                    agencies = agencies,
                    features = features
                )
            )
        }.onFailure {
            RemoteAccessContextStore.clear()
        }
    }

    private fun syncRemoteProfileToSupabase() {
        val profile = _userProfile.value
        viewModelScope.launch {
            runCatching {
                PharaohSupabaseAuth.syncProfile(
                    userId = profile.uuid,
                    displayName = profile.nickname,
                    coins = profile.goldCoins,
                    diamonds = profile.crystalDiamonds,
                    vipLevel = profile.vipTier,
                    frameId = profile.frameStyle.name,
                    entryId = profile.entryWelcomeName,
                    avatarUrl = profile.customAvatarUri,
                    coverUrl = profile.customCoverUri
                )
            }.onFailure {
                showToast("⚠️ تعذر مزامنة الحساب مع قاعدة البيانات")
            }
        }
    }

    private fun syncCurrentProfileToMasterTable() {
        // 1. Auto-level up Wealth & Charisma if XP reached target
        _userProfile.update { current ->
            var nextWealthLv = current.wealthLevel
            var nextWealthExp = current.wealthExpCurrent
            var nextWealthTarget = current.wealthExpTarget
            while (nextWealthExp >= nextWealthTarget && nextWealthTarget > 0L) {
                nextWealthExp -= nextWealthTarget
                nextWealthLv += 1
                nextWealthTarget = (nextWealthTarget * 12L) / 10L
            }

            var nextCharismaLv = current.charismaLevel
            var nextCharismaExp = current.charismaExpCurrent
            var nextCharismaTarget = current.charismaExpTarget
            while (nextCharismaExp >= nextCharismaTarget && nextCharismaTarget > 0L) {
                nextCharismaExp -= nextCharismaTarget
                nextCharismaLv += 1
                nextCharismaTarget = (nextCharismaTarget * 12L) / 10L
            }

            if (nextWealthLv != current.wealthLevel || nextCharismaLv != current.charismaLevel) {
                current.copy(
                    wealthLevel = nextWealthLv,
                    wealthExpCurrent = nextWealthExp,
                    wealthExpTarget = nextWealthTarget,
                    charismaLevel = nextCharismaLv,
                    charismaExpCurrent = nextCharismaExp,
                    charismaExpTarget = nextCharismaTarget
                )
            } else {
                current
            }
        }

        val current = _userProfile.value

        // 2. Sync to MasterAppDatabaseTable
        val record = MasterAppDatabaseTable.findAccountByDisplayId(current.displayId)
        if (record != null) {
            record.profile = current
        }

        // 3. Sync inspected account profile if viewing self
        if (_inspectedAccountProfile.value?.uuid == current.uuid ||
            _inspectedAccountProfile.value?.displayId == current.displayId
        ) {
            _inspectedAccountProfile.value = current
        }

        // 4. Sync active room & all rooms (host name, host photo, and seated occupant name/photo/levels/frame)
        _activeRoom.update { room ->
            room ?: return@update null
            val isMyOwnedRoom = room.hostUserId == current.uuid || room.roomDisplayId == current.displayId
            room.copy(
                hostName = if (isMyOwnedRoom) current.nickname else room.hostName,
                hostCustomAvatarUri = if (isMyOwnedRoom) (current.customAvatarUri ?: room.hostCustomAvatarUri) else room.hostCustomAvatarUri,
                hostFrame = if (isMyOwnedRoom) current.frameStyle else room.hostFrame,
                countryFlag = if (isMyOwnedRoom) current.countryFlag else room.countryFlag,
                seats = room.seats.map { seat ->
                    if (seat.occupantUserId == current.uuid || seat.occupantDisplayId == current.displayId) {
                        seat.copy(
                            occupantName = current.nickname,
                            occupantDisplayId = current.displayId,
                            occupantAvatarType = current.avatarType,
                            occupantCustomAvatarUri = current.customAvatarUri,
                            occupantFrame = current.frameStyle,
                            occupantVip = current.vipTier,
                            occupantWealthLevel = current.wealthLevel,
                            occupantCharismaLevel = current.charismaLevel,
                            soundWaveColor = current.frameStyle.soundWaveColor,
                            nameColor = current.frameStyle.nameGradientColor
                        )
                    } else seat
                }
            )
        }

        _rooms.update { list ->
            list.map { room ->
                val isMyOwnedRoom = room.hostUserId == current.uuid || room.roomDisplayId == current.displayId
                room.copy(
                    hostName = if (isMyOwnedRoom) current.nickname else room.hostName,
                    hostCustomAvatarUri = if (isMyOwnedRoom) (current.customAvatarUri ?: room.hostCustomAvatarUri) else room.hostCustomAvatarUri,
                    hostFrame = if (isMyOwnedRoom) current.frameStyle else room.hostFrame,
                    countryFlag = if (isMyOwnedRoom) current.countryFlag else room.countryFlag,
                    seats = room.seats.map { seat ->
                        if (seat.occupantUserId == current.uuid || seat.occupantDisplayId == current.displayId) {
                            seat.copy(
                                occupantName = current.nickname,
                                occupantDisplayId = current.displayId,
                                occupantAvatarType = current.avatarType,
                                occupantCustomAvatarUri = current.customAvatarUri,
                                occupantFrame = current.frameStyle,
                                occupantVip = current.vipTier,
                                occupantWealthLevel = current.wealthLevel,
                                occupantCharismaLevel = current.charismaLevel,
                                soundWaveColor = current.frameStyle.soundWaveColor,
                                nameColor = current.frameStyle.nameGradientColor
                            )
                        } else seat
                    }
                )
            }
        }

        // 5. Sync Room Chat Messages sent by current user
        _roomMessages.update { msgs ->
            msgs.map { msg ->
                if (msg.senderUserId == current.uuid || msg.senderDisplayId == current.displayId) {
                    msg.copy(
                        senderName = current.nickname,
                        senderDisplayId = current.displayId,
                        senderAvatarType = current.avatarType,
                        senderCustomAvatarUri = current.customAvatarUri,
                        senderVip = current.vipTier,
                        senderWealthLevel = current.wealthLevel,
                        senderChatBubbleFrame = current.equippedChatBubbleName
                    )
                } else msg
            }
        }

        // 6. Sync Discover Moments authored by current user
        _discoverPosts.update { posts ->
            posts.map { post ->
                if (post.authorDisplayId == current.displayId) {
                    post.copy(
                        authorName = current.nickname,
                        authorCustomAvatarUri = current.customAvatarUri,
                        authorAvatarType = current.avatarType,
                        authorFrame = current.frameStyle,
                        authorCountryFlag = current.countryFlag
                    )
                } else post
            }
        }

        // 7. Sync Leaderboard entry for current user
        _leaderboardEntries.update { entries ->
            entries.map { entry ->
                if (entry.displayId == current.displayId || entry.rank == 1) {
                    entry.copy(
                        nameAr = current.nickname,
                        displayId = current.displayId,
                        countryFlag = current.countryFlag,
                        avatarType = current.avatarType,
                        customAvatarUri = current.customAvatarUri,
                        frameStyle = current.frameStyle,
                        level = current.wealthLevel,
                        vipTier = current.vipTier
                    )
                } else entry
            }
        }

        // 8. Sync Host Agency Members record if user is present
        _agencyHostsMembers.update { members ->
            members.map { member ->
                if (member.userId == current.displayId) {
                    member.copy(
                        nickname = current.nickname,
                        customAvatarUri = current.customAvatarUri,
                        avatarType = current.avatarType,
                        frameStyle = current.frameStyle
                    )
                } else member
            }
        }

        // 9. Persist updated User Account, Rooms, and Chat Identity to local Room Database
        val dao = appDao
        if (dao != null) {
            viewModelScope.launch {
                dao.saveUserAccount(current.toEntity())
                dao.upsertAllRoomSettings(_rooms.value.map { it.toRoomSettingsEntity() })
                dao.syncUserIdentityInChatHistory(
                    uuid = current.uuid,
                    displayId = current.displayId,
                    newName = current.nickname,
                    newAvatarUri = current.customAvatarUri,
                    wealthLv = current.wealthLevel,
                    charismaLv = current.charismaLevel,
                    vipTier = current.vipTier,
                    chatBubbleFrame = current.equippedChatBubbleName
                )
            }
        }
    }

    private fun persistRoomsToDatabase() {
        val dao = appDao ?: return
        val currentRooms = _rooms.value
        viewModelScope.launch {
            dao.upsertAllRoomSettings(currentRooms.map { it.toRoomSettingsEntity() })
        }
    }

    private fun persistRoomChatMessageToDatabase(msg: RoomChatMessage, roomId: String) {
        val dao = appDao ?: return
        viewModelScope.launch {
            dao.insertChatHistoryMessage(msg.toChatHistoryEntity(roomId = roomId))
        }
    }

    fun selectMainTab(index: Int) {
        _selectedTabIndex.value = index
        _activeSubScreen.value = StandaloneSubScreen.NONE
    }

    fun openSubScreen(subScreen: StandaloneSubScreen) {
        val current = _userProfile.value
        if (subScreen == StandaloneSubScreen.AGENCIES && !current.isHostAgent && !current.isApprovedHost) {
            return
        }
        if (subScreen == StandaloneSubScreen.CHARGE_AGENCY && !current.isChargeAgent) {
            return
        }
        if (subScreen == StandaloneSubScreen.INNER_ACCOUNT_DETAIL && _inspectedAccountProfile.value == null) {
            _inspectedAccountProfile.value = current
        }
        _activeSubScreen.value = subScreen
    }

    /**
     * Opens the full Inner Account Detail view (`عرض التفاصيل من الداخل عند لمس مرتين على الصورة`).
     */
    fun openInnerAccountDetails(targetProfile: UserProfile? = null) {
        _inspectedAccountProfile.value = targetProfile ?: _userProfile.value
        _activeSubScreen.value = StandaloneSubScreen.INNER_ACCOUNT_DETAIL
    }

    /**
     * Builds or resolves the full `UserProfile` for any occupied mic seat and opens its Inner Account Details.
     */
    fun openInnerAccountDetailsFromSeat(seat: MicSeatState) {
        val me = _userProfile.value
        val occupantDisplayId = seat.occupantDisplayId ?: me.displayId
        if (occupantDisplayId == me.displayId || seat.occupantUserId == me.uuid) {
            _inspectedAccountProfile.value = me
        } else {
            val dbAccount = MasterAppDatabaseTable.findAccountByDisplayId(occupantDisplayId)
            _inspectedAccountProfile.value = dbAccount?.profile ?: UserProfile(
                uuid = seat.occupantUserId ?: "user_$occupantDisplayId",
                displayId = occupantDisplayId,
                isSpecialId = occupantDisplayId.length <= 6 || occupantDisplayId.toSet().size == 1,
                email = "royal_$occupantDisplayId@zadiralive.com",
                nickname = seat.occupantName ?: "عضو ملكي",
                bio = "🎙️ متواجد الآن على ${seat.seatLabelAr} • زاديرا لايف 7D ✨",
                avatarType = seat.occupantAvatarType,
                customAvatarUri = seat.occupantCustomAvatarUri,
                role = seat.occupantRole,
                wealthLevel = seat.occupantWealthLevel,
                charismaLevel = seat.occupantCharismaLevel,
                vipTier = seat.occupantVip,
                frameStyle = seat.occupantFrame,
                badges = MasterAppDatabaseTable.defaultBadgesSeed()
            )
        }
    }

    /**
     * Resolves and opens any user's account profile by their `displayId` across Discover, Messages, Leaderboard, or Room Chat.
     */
    fun openAccountDetailsByDisplayId(
        displayId: String,
        fallbackName: String? = null,
        fallbackAvatarType: String = "PRINCE",
        fallbackCustomAvatarUri: String? = null,
        fallbackFrame: FrameStyle3D = FrameStyle3D.IMPERIAL_GOLD_WINGS,
        fallbackCountryFlag: String = "🇪🇬",
        fallbackWealthLv: Int = 38,
        fallbackVip: Int = 4
    ) {
        val me = _userProfile.value
        val cleanId = displayId.removePrefix("user_").trim()
        if (cleanId == me.displayId || displayId == me.uuid) {
            openInnerAccountDetails(me)
            return
        }
        val dbAccount = MasterAppDatabaseTable.findAccountByDisplayId(cleanId)
        val resolvedProfile = if (dbAccount != null) {
            dbAccount.profile.copy(
                customAvatarUri = fallbackCustomAvatarUri ?: dbAccount.profile.customAvatarUri
            )
        } else {
            // Also check if this user is host of any room or seated in any room
            val roomMatch = _rooms.value.firstOrNull { it.roomDisplayId == cleanId }
            UserProfile(
                uuid = "user_$cleanId",
                displayId = cleanId,
                isSpecialId = cleanId.length <= 6 || cleanId.toSet().size == 1,
                email = "user_$cleanId@zadiralive.com",
                nickname = fallbackName ?: roomMatch?.hostName ?: "عضو ملكي",
                bio = roomMatch?.announcementAr ?: "👑 مرحباً بكم في ملفي الشخصي • زاديرا لايف 7D ✨",
                avatarType = fallbackAvatarType,
                customAvatarUri = fallbackCustomAvatarUri ?: roomMatch?.hostCustomAvatarUri,
                countryFlag = fallbackCountryFlag,
                role = AppUserRole.HOST,
                wealthLevel = fallbackWealthLv,
                charismaLevel = (fallbackWealthLv - 3).coerceAtLeast(10),
                vipTier = fallbackVip,
                frameStyle = fallbackFrame,
                badges = MasterAppDatabaseTable.defaultBadgesSeed()
            )
        }
        openInnerAccountDetails(resolvedProfile)
    }

    fun closeInnerAccountDetails() {
        _inspectedAccountProfile.value = null
        if (_activeSubScreen.value == StandaloneSubScreen.INNER_ACCOUNT_DETAIL) {
            _activeSubScreen.value = StandaloneSubScreen.NONE
        }
    }

    /**
     * Updates the user's personal profile photo picked from device storage (`الصور تأخذ من تخزين الجهاز لصورة الحساب الشخصي`).
     */
    fun updateUserCustomAvatar(uriString: String) {
        if (uriString.isBlank()) return
        _userProfile.update { it.copy(customAvatarUri = uriString) }
        val updatedMe = _userProfile.value
        if (_inspectedAccountProfile.value?.displayId == updatedMe.displayId) {
            _inspectedAccountProfile.value = updatedMe
        }
        // Sync to active room seats & rooms list
        _activeRoom.update { room ->
            room ?: return@update null
            room.copy(
                hostCustomAvatarUri = if (room.roomDisplayId == updatedMe.displayId) uriString else room.hostCustomAvatarUri,
                seats = room.seats.map { seat ->
                    if (seat.occupantDisplayId == updatedMe.displayId || seat.occupantUserId == updatedMe.uuid) {
                        seat.copy(occupantCustomAvatarUri = uriString)
                    } else seat
                }
            )
        }
        _rooms.update { list ->
            list.map { room ->
                room.copy(
                    hostCustomAvatarUri = if (room.roomDisplayId == updatedMe.displayId) uriString else room.hostCustomAvatarUri,
                    seats = room.seats.map { seat ->
                        if (seat.occupantDisplayId == updatedMe.displayId || seat.occupantUserId == updatedMe.uuid) {
                            seat.copy(occupantCustomAvatarUri = uriString)
                        } else seat
                    }
                )
            }
        }
        syncCurrentProfileToMasterTable()
        syncRemoteProfileToSupabase()
        showToast("📷 تم تحديث صورة حسابك الشخصي من تخزين الجهاز بنجاح!")
    }

    /**
     * Updates the user's personal account cover banner picked from device storage.
     */
    fun updateUserCustomCover(uriString: String) {
        if (uriString.isBlank()) return
        _userProfile.update { it.copy(customCoverUri = uriString) }
        val updatedMe = _userProfile.value
        if (_inspectedAccountProfile.value?.displayId == updatedMe.displayId) {
            _inspectedAccountProfile.value = updatedMe
        }
        syncCurrentProfileToMasterTable()
        syncRemoteProfileToSupabase()
        showToast("🖼️ تم تحديث غلاف الحساب الشخصي من تخزين الجهاز بنجاح!")
    }

    /**
     * Synchronizes the current user's profile state from `MasterAppDatabaseTable` whenever
     * an external server or database table grants/modifies permissions, frames, badges, or agency roles.
     */
    fun refreshUserPermissionsFromDatabase() {
        val currentId = _userProfile.value.displayId
        val record = MasterAppDatabaseTable.findAccountByDisplayId(currentId)
        if (record != null) {
            _userProfile.value = record.profile
        }
    }

    fun closeSubScreen() {
        _inspectedAccountProfile.value = null
        _activeSubScreen.value = StandaloneSubScreen.NONE
    }

    fun setHomeCategory(category: String) {
        _selectedHomeCategory.value = category
    }

    fun setRoomChatFilter(filter: String) {
        _selectedRoomChatFilter.value = filter
    }

    fun enterVoiceRoom(room: VoiceRoomModel) {
        val me = _userProfile.value
        // Automatically detect user's role in the room strictly via their Unique ID (`displayId` / `uuid`)
        val autoDetectedRole = MasterAppDatabaseTable.resolveRoomRoleAutomaticallyById(
            room = room,
            userDisplayId = me.displayId,
            userUuid = me.uuid
        )
        val roomWithAutoRole = room.copy(myRoleInRoom = autoDetectedRole)

        // Automatically trigger entry welcome announcement in chat
        val welcomeMsg = RoomChatMessage(
            id = "welcome_${System.currentTimeMillis()}",
            senderUserId = me.uuid,
            senderName = me.nickname,
            senderDisplayId = me.displayId,
            senderRole = me.role,
            senderRoomRole = autoDetectedRole,
            senderVip = me.vipTier,
            senderWealthLevel = me.wealthLevel,
            senderCustomAvatarUri = me.customAvatarUri,
            messageText = "✨ دخل الغرفة على متن دخولية [${me.entryWelcomeName}] (صلاحية المعرف التلقائية: ${autoDetectedRole.titleAr})!",
            isSystemWelcome = true,
            highlightColor = Color(0xFFFFD700)
        )
        _roomMessages.update { (it + welcomeMsg).takeLast(50) }
        persistRoomChatMessageToDatabase(welcomeMsg, roomWithAutoRole.id)
        _activeRoom.value = roomWithAutoRole
        if (me.entryWelcomeName.isNotBlank()) {
            triggerEntryWelcomePreview(me.entryWelcomeName)
        }
    }

    fun createAndEnterMyRoom(
        roomTitleAr: String,
        categoryAr: String,
        announcementAr: String,
        customCoverUri: String? = null
    ) {
        val me = _userProfile.value
        viewModelScope.launch {
            try {
                val raw = SupabaseRpcClient.createRoom(
                    name = roomTitleAr.ifBlank { "غرفة " + me.nickname },
                    title = roomTitleAr.ifBlank { "غرفة " + me.nickname + " الملكية" },
                    type = "VOICE",
                    privacy = "public",
                    country = "EG",
                    coverUrl = customCoverUri?.takeIf { it.startsWith("http") },
                    maxSeats = 10
                )
                val remoteRoomId = runCatching { JSONObject(raw).optString("id") }
                    .getOrDefault("")
                    .ifBlank { error("قاعدة البيانات لم تُرجع معرف الغرفة") }

                val defaultVisuals = MasterAppDatabaseTable.roomAndAppVisualConfigFlow.value
                MasterAppDatabaseTable.setRoomOwnerByIdInDatabase(remoteRoomId, me.displayId)
                val newRoom = VoiceRoomModel(
                    id = remoteRoomId,
                    roomDisplayId = me.displayId,
                    titleAr = roomTitleAr.ifBlank { "غرفة " + me.nickname + " الملكية 7D" },
                    announcementAr = announcementAr.ifBlank { "أهلاً وسهلاً بالجميع في غرفتنا الملكية 👑" },
                    categoryAr = categoryAr,
                    countryFlag = me.countryFlag,
                    hostName = me.nickname,
                    hostUserId = me.uuid,
                    myRoleInRoom = RoomPermissionRole.OWNER,
                    onlineCount = 1,
                    heatScore = 9999L,
                    coverBadgeText = "👑 7D",
                    customCoverImageUri = customCoverUri,
                    hostCustomAvatarUri = me.customAvatarUri,
                    backgroundStyleId = defaultVisuals.defaultRoomBackgroundStyleId,
                    micShapeStyleId = defaultVisuals.defaultMicShapeStyleId,
                    seats = buildSeatsForRoom(me.nickname, me.displayId, me.frameStyle, me.customAvatarUri)
                )
                _rooms.update { listOf(newRoom) + it }
                persistRoomsToDatabase()
                enterVoiceRoom(newRoom)
                showToast("🏠 تم إنشاء الغرفة وحفظها فعلياً في قاعدة البيانات")
            } catch (e: Exception) {
                showToast("⚠️ لم يتم إنشاء الغرفة في قاعدة البيانات: " + (e.message ?: "خطأ غير معروف"))
            }
        }
    }

    fun leaveVoiceRoom() {
        _inspectedAccountProfile.value = null
        _activeRoom.value = null
    }

    fun toggleMyMicMute() {
        val nextMute = !_isMyMicMuted.value
        val roomId = _activeRoom.value?.id
        if (roomId != null) viewModelScope.launch { runCatching { SupabaseRpcClient.setMyMute(roomId, nextMute) } }
        _isMyMicMuted.value = nextMute
        val me = _userProfile.value
        _activeRoom.update { room ->
            room ?: return@update null
            room.copy(
                seats = room.seats.map { seat ->
                    if (seat.occupantUserId == me.uuid || seat.occupantDisplayId == me.displayId) {
                        seat.copy(isMuted = nextMute, isSpeaking = !nextMute)
                    } else seat
                }
            )
        }
    }

    /**
     * Sit on an empty mic seat OR leave an occupied mic seat (`صعود المايك والنزول من المايك`).
     */
    fun takeOrLeaveSeat(seatIndex: Int) {
        val currentRoom = _activeRoom.value ?: return
        val me = _userProfile.value
        val targetSeat = currentRoom.seats.firstOrNull { it.seatIndex == seatIndex } ?: return
        viewModelScope.launch {
            runCatching {
                if (targetSeat.occupantUserId == me.uuid || targetSeat.occupantDisplayId == me.displayId) {
                    SupabaseRpcClient.leaveRoomSeat(currentRoom.id)
                } else {
                    SupabaseRpcClient.joinRoomSeat(currentRoom.id, seatIndex + 1)
                }
            }.onFailure {
                showToast("⚠️ تعذر تحديث المايك في قاعدة البيانات")
            }
        }

        if (targetSeat.isLocked && currentRoom.myRoleInRoom == RoomPermissionRole.MEMBER) {
            showToast("🔒 هذا المايك مغلق حالياً")
            return
        }

        val isMyCurrentSeat = targetSeat.occupantUserId == me.uuid || targetSeat.occupantDisplayId == me.displayId
        val updatedSeats = currentRoom.seats.map { seat ->
            when {
                seat.seatIndex == seatIndex && isMyCurrentSeat -> {
                    // Leave Mic (`نزول المايك`)
                    seat.copy(
                        occupantUserId = null,
                        occupantName = null,
                        occupantDisplayId = null,
                        occupantCustomAvatarUri = null,
                        isSpeaking = false,
                        isMuted = false
                    )
                }
                seat.seatIndex == seatIndex && seat.occupantName == null -> {
                    // Sit on Mic (`صعود المايك`)
                    seat.copy(
                        isLocked = false,
                        occupantUserId = me.uuid,
                        occupantName = me.nickname,
                        occupantDisplayId = me.displayId,
                        occupantAvatarType = me.avatarType,
                        occupantCustomAvatarUri = me.customAvatarUri,
                        occupantFrame = me.frameStyle,
                        occupantRole = me.role,
                        occupantRoomRole = currentRoom.myRoleInRoom,
                        occupantVip = me.vipTier,
                        occupantWealthLevel = me.wealthLevel,
                        occupantCharismaLevel = me.charismaLevel,
                        isSpeaking = !_isMyMicMuted.value,
                        isMuted = _isMyMicMuted.value,
                        soundWaveColor = me.frameStyle.soundWaveColor,
                        nameColor = me.frameStyle.nameGradientColor
                    )
                }
                seat.occupantUserId == me.uuid || seat.occupantDisplayId == me.displayId -> {
                    // Vacate previous seat when moving to a new mic seat
                    seat.copy(
                        occupantUserId = null,
                        occupantName = null,
                        occupantDisplayId = null,
                        occupantCustomAvatarUri = null,
                        isSpeaking = false,
                        isMuted = false
                    )
                }
                else -> seat
            }
        }
        val updatedRoom = currentRoom.copy(seats = updatedSeats)
        _activeRoom.value = updatedRoom
        _rooms.update { list -> list.map { if (it.id == updatedRoom.id) updatedRoom else it } }
        showToast(
            if (isMyCurrentSeat) "⬇️ نزلت من ${targetSeat.seatLabelAr}"
            else "🎙️ صعدت على ${targetSeat.seatLabelAr}"
        )
    }

    /**
     * Switch user's permission role in the active room (`صاحب الغرفة` | `أدمن الغرفة` | `مستخدم في الغرفة`).
     */
    fun changeMyRoomRole(newRole: RoomPermissionRole) {
        val currentRoom = _activeRoom.value ?: return
        val updatedRoom = currentRoom.copy(myRoleInRoom = newRole)
        _activeRoom.value = updatedRoom
        _rooms.update { list -> list.map { if (it.id == updatedRoom.id) updatedRoom else it } }
        showToast("${newRole.badgeEmoji} دورك الآن في الغرفة: ${newRole.titleAr}")
    }

    /**
     * Room Owner exclusive action: Change Room Name & Room Cover Photo from Device Storage (`تغيير اسم الغرفة وتغيير صورة الغرفة من تخزين الجهاز`).
     */
    fun editRoomNameAndPhoto(
        newTitleAr: String,
        newCoverEmoji: String,
        newCustomCoverUri: String? = null
    ) {
        val currentRoom = _activeRoom.value ?: return
        val me = _userProfile.value
        val myAutoRole = MasterAppDatabaseTable.resolveRoomRoleAutomaticallyById(currentRoom, me.displayId, me.uuid)
        if (myAutoRole != RoomPermissionRole.OWNER && currentRoom.myRoleInRoom != RoomPermissionRole.OWNER) {
            showToast("⚠️ هذه الصلاحية خاصة بصاحب الغرفة فقط")
            return
        }
        val updatedRoom = currentRoom.copy(
            titleAr = newTitleAr.ifBlank { currentRoom.titleAr },
            coverBadgeText = newCoverEmoji.ifBlank { currentRoom.coverBadgeText },
            customCoverImageUri = newCustomCoverUri ?: currentRoom.customCoverImageUri
        )
        _activeRoom.value = updatedRoom
        _rooms.update { list -> list.map { if (it.id == updatedRoom.id) updatedRoom else it } }
        persistRoomsToDatabase()
        showToast("✅ تم تحديث اسم وصورة الغرفة وحفظها في قاعدة البيانات")
    }

    fun updateCurrentRoomCustomCover(uriString: String) {
        if (uriString.isBlank()) return
        val currentRoom = _activeRoom.value ?: return
        val updatedRoom = currentRoom.copy(customCoverImageUri = uriString)
        _activeRoom.value = updatedRoom
        _rooms.update { list -> list.map { if (it.id == updatedRoom.id) updatedRoom else it } }
        persistRoomsToDatabase()
        showToast("📷 تم تحديث صورة الغرفة الحقيقية وحفظها بنجاح!")
    }

    /**
     * Follow a user inside the voice room (`متابعة`).
     */
    fun followRoomUser(targetUserId: String, targetUserName: String) {
        val currentRoom = _activeRoom.value ?: return
        val alreadyFollowing = currentRoom.followingUserIds.contains(targetUserId)
        val updatedFollowing = if (alreadyFollowing) {
            currentRoom.followingUserIds - targetUserId
        } else {
            currentRoom.followingUserIds + targetUserId
        }
        _activeRoom.value = currentRoom.copy(followingUserIds = updatedFollowing)
        if (!alreadyFollowing) {
            _userProfile.update { it.copy(followingCount = it.followingCount + 1) }
            showToast("➕ تمت متابعة $targetUserName بنجاح")
        } else {
            showToast("تم إلغاء متابعة $targetUserName")
        }
    }

    /**
     * Room Owner & Room Admin action: Kick user from mic/room (`طرد مستخدم`).
     */
    fun kickUserFromSeat(seatIndex: Int, targetUserName: String) {
        val currentRoom = _activeRoom.value ?: return
        if (currentRoom.myRoleInRoom == RoomPermissionRole.MEMBER) return
        val updatedSeats = currentRoom.seats.map { seat ->
            if (seat.seatIndex == seatIndex) {
                seat.copy(
                    occupantUserId = null,
                    occupantName = null,
                    occupantDisplayId = null,
                    isSpeaking = false,
                    isMuted = false
                )
            } else seat
        }
        _activeRoom.value = currentRoom.copy(seats = updatedSeats)
        showToast("🚫 تم طرد $targetUserName من المايك والغرفة")
    }

    /**
     * Room Owner & Room Admin action: Mute/Unmute user on mic (`كتم مستخدم`).
     */
    fun muteUserOnSeat(seatIndex: Int, targetUserName: String) {
        val currentRoom = _activeRoom.value ?: return
        if (currentRoom.myRoleInRoom == RoomPermissionRole.MEMBER) return
        var mutedNow = false
        val updatedSeats = currentRoom.seats.map { seat ->
            if (seat.seatIndex == seatIndex) {
                mutedNow = !seat.isMuted
                seat.copy(isMuted = mutedNow, isSpeaking = !mutedNow)
            } else seat
        }
        _activeRoom.value = currentRoom.copy(seats = updatedSeats)
        showToast(if (mutedNow) "🔇 تم كتم مايك $targetUserName" else "🔊 تم فك الكتم عن $targetUserName")
    }

    /**
     * Room Owner exclusive action: Ban user from the room (`حظر من الغرفة`).
     */
    fun banUserFromRoom(seatIndex: Int, targetUserId: String, targetUserName: String) {
        val currentRoom = _activeRoom.value ?: return
        if (currentRoom.myRoleInRoom != RoomPermissionRole.OWNER) return
        val updatedSeats = currentRoom.seats.map { seat ->
            if (seat.seatIndex == seatIndex) {
                seat.copy(
                    occupantUserId = null,
                    occupantName = null,
                    occupantDisplayId = null,
                    isSpeaking = false,
                    isMuted = false
                )
            } else seat
        }
        val updatedRoom = currentRoom.copy(
            seats = updatedSeats,
            bannedUserIds = (currentRoom.bannedUserIds + targetUserId).distinct()
        )
        _activeRoom.value = updatedRoom
        showToast("⛔ تم حظر $targetUserName نهائياً من الغرفة")
    }

    /**
     * Room Owner exclusive action: Assign or Revoke Room Admin (`تعيين أدمن في الغرفة`).
     */
    fun assignRoomAdmin(targetUserId: String, targetUserName: String) {
        val currentRoom = _activeRoom.value ?: return
        if (currentRoom.myRoleInRoom != RoomPermissionRole.OWNER) return
        val cleanId = targetUserId.removePrefix("user_").trim()
        val isCurrentlyAdmin = currentRoom.adminUserIds.contains(targetUserId) || currentRoom.adminUserIds.contains(cleanId)
        MasterAppDatabaseTable.setRoomAdminByIdInDatabase(currentRoom.id, cleanId, !isCurrentlyAdmin)
        val nextAdmins = if (isCurrentlyAdmin) {
            currentRoom.adminUserIds - targetUserId - cleanId
        } else {
            (currentRoom.adminUserIds + targetUserId + cleanId).distinct()
        }
        val updatedRoom = currentRoom.copy(adminUserIds = nextAdmins)
        _activeRoom.value = updatedRoom
        _rooms.update { list -> list.map { if (it.id == updatedRoom.id) updatedRoom else it } }
        showToast(
            if (isCurrentlyAdmin) "تم إلغاء صلاحية الأدمن عن $targetUserName (ID: $cleanId)"
            else "🛡️ تم تعيين $targetUserName (ID: $cleanId) أدمن في الغرفة"
        )
    }

    /**
     * Room Owner & Room Admin action: Lock or Unlock a Mic Seat (`قفل مايك / فتح مايك`).
     */
    fun toggleLockRoomSeat(seatIndex: Int) {
        val currentRoom = _activeRoom.value ?: return
        if (currentRoom.myRoleInRoom == RoomPermissionRole.MEMBER) return
        var lockedNow = false
        val updatedSeats = currentRoom.seats.map { seat ->
            if (seat.seatIndex == seatIndex) {
                lockedNow = !seat.isLocked
                seat.copy(
                    isLocked = lockedNow,
                    occupantUserId = if (lockedNow) null else seat.occupantUserId,
                    occupantName = if (lockedNow) null else seat.occupantName,
                    occupantDisplayId = if (lockedNow) null else seat.occupantDisplayId,
                    isSpeaking = if (lockedNow) false else seat.isSpeaking
                )
            } else seat
        }
        _activeRoom.value = currentRoom.copy(seats = updatedSeats)
        showToast(if (lockedNow) "🔒 تم إغلاق المايك #$seatIndex" else "🔓 تم فتح المايك #$seatIndex")
    }

    fun sendRoomChatMessage(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val roomId = _activeRoom.value?.id ?: return
        viewModelScope.launch {
            try {
                val row = JSONObject(SupabaseRpcClient.sendRoomMessage(roomId, clean))
                val me = _userProfile.value
                val msg = RoomChatMessage(id = row.optString("id", "msg_" + System.currentTimeMillis()), senderUserId = me.uuid, senderName = me.nickname, senderDisplayId = me.displayId, senderRole = me.role, senderRoomRole = _activeRoom.value?.myRoleInRoom ?: RoomPermissionRole.MEMBER, senderVip = me.vipTier, senderWealthLevel = me.wealthLevel, senderAvatarType = me.avatarType, senderCustomAvatarUri = me.customAvatarUri, senderChatBubbleFrame = me.equippedChatBubbleName, messageText = clean, highlightColor = me.frameStyle.primaryColor)
                _roomMessages.update { (it + msg).takeLast(50) }
            } catch (e: Exception) { showToast("⚠️ تعذر إرسال الرسالة إلى Supabase: " + (e.message ?: "خطأ غير معروف")) }
        }
    }
    fun sendSeatEmojiReaction(emoji: String, titleAr: String = "") {
        val currentRoom = _activeRoom.value ?: return
        val me = _userProfile.value
        val now = System.currentTimeMillis()
        val isUserOnSeat = currentRoom.seats.any {
            it.occupantUserId == me.uuid || it.occupantDisplayId == me.displayId
        }
        val updatedSeats = if (isUserOnSeat) {
            currentRoom.seats.map { seat ->
                if (seat.occupantUserId == me.uuid || seat.occupantDisplayId == me.displayId) {
                    seat.copy(activeReactionEmoji = emoji, reactionTimestamp = now)
                } else seat
            }
        } else {
            val targetIdx = currentRoom.seats.firstOrNull { !it.isEmpty }?.seatIndex ?: 1
            currentRoom.seats.map { seat ->
                if (seat.seatIndex == targetIdx) {
                    seat.copy(activeReactionEmoji = emoji, reactionTimestamp = now)
                } else seat
            }
        }
        val updatedRoom = currentRoom.copy(seats = updatedSeats)
        _activeRoom.value = updatedRoom
        _rooms.update { list -> list.map { if (it.id == updatedRoom.id) updatedRoom else it } }

        val emojiMsg = RoomChatMessage(
            id = "emoji_$now",
            senderUserId = me.uuid,
            senderName = me.nickname,
            senderDisplayId = me.displayId,
            senderRole = me.role,
            senderRoomRole = currentRoom.myRoleInRoom,
            senderVip = me.vipTier,
            senderWealthLevel = me.wealthLevel,
            senderAvatarType = me.avatarType,
            senderCustomAvatarUri = me.customAvatarUri,
            senderChatBubbleFrame = me.equippedChatBubbleName,
            messageText = "تفاعل بإيموجي متحرك على المايك $emoji ✨",
            highlightColor = Color(0xFFFFD700)
        )
        _roomMessages.update { (it + emojiMsg).takeLast(50) }
        persistRoomChatMessageToDatabase(emojiMsg, updatedRoom.id)
        showToast("$emoji تم إظهار الإيموجي المتحرك فوق المايك!")
    }

    /**
     * Allows the Room Owner or Database Admin to change the microphone shape in the room (`تغيير شكل المايكات في الغرفة ومن قاعدة البيانات`).
     */
    fun changeRoomMicShapeStyle(micShapeId: String) {
        val shapeStyle = RoomMicShapeStyle.fromId(micShapeId)
        _activeRoom.update { room ->
            room ?: return@update null
            room.copy(micShapeStyleId = shapeStyle.id)
        }
        val activeId = _activeRoom.value?.id
        if (activeId != null) {
            _rooms.update { list ->
                list.map { r -> if (r.id == activeId) r.copy(micShapeStyleId = shapeStyle.id) else r }
            }
        }
        val currentConfig = MasterAppDatabaseTable.roomAndAppVisualConfigFlow.value
        MasterAppDatabaseTable.updateRoomAndAppVisualCustomizationFromDatabase(
            currentConfig.copy(defaultMicShapeStyleId = shapeStyle.id)
        )
        persistRoomsToDatabase()
        showToast("${shapeStyle.iconEmoji} تم تغيير شكل المايكات إلى: ${shapeStyle.titleAr}")
    }

    fun saveHomeBannerToDatabase(banner: HomeBannerSlideItem) {
        MasterAppDatabaseTable.addOrEditHomeBannerFromDatabase(banner)
        val dao = appDao
        if (dao != null) {
            viewModelScope.launch {
                val all = MasterAppDatabaseTable.homeBannersTable.value.mapIndexed { idx, b ->
                    b.toEntity(sortOrder = idx + 1)
                }
                dao.upsertHomeBanners(all)
            }
        }
    }

    fun deleteHomeBannerFromDatabase(bannerId: String) {
        MasterAppDatabaseTable.deleteHomeBannerFromDatabase(bannerId)
        showToast("🗑️ تم حذف البنر من الشاشة الرئيسية")
    }

    fun reorderHomeBannerInDatabase(bannerId: String, moveEarlier: Boolean) {
        MasterAppDatabaseTable.reorderHomeBannerInDatabase(bannerId, moveEarlier)
    }

    fun rollDiceInRoom() {
        val diceNumber = (1..6).random()
        val diceEmoji = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")[diceNumber - 1]
        sendRoomChatMessage("🎲 رمى نرد الحظ الملكي وحصل على: $diceEmoji ($diceNumber)")
    }

    fun sendGiftInRoom(gift: GiftItem3D, receiverName: String, comboCount: Int = 1) {
        val me = _userProfile.value
        val totalCost = gift.priceCoins * comboCount
        if (me.goldCoins < totalCost) {
            showToast("⚠️ رصيد العملات الذهبية غير كافٍ، يرجى شحن المحفظة")
            return
        }
        val currentRoomId = _activeRoom.value?.id
        val receiverId = _activeRoom.value?.seats?.firstOrNull { it.occupantName == receiverName && it.occupantUserId != null }?.occupantUserId
        val giftUuid = gift.id
        if (currentRoomId != null && receiverId != null && runCatching { java.util.UUID.fromString(currentRoomId) }.isSuccess && runCatching { java.util.UUID.fromString(receiverId) }.isSuccess && runCatching { java.util.UUID.fromString(giftUuid) }.isSuccess) {
            viewModelScope.launch {
                runCatching { SupabaseRpcClient.sendGift(me.uuid, receiverId, currentRoomId, giftUuid, comboCount) }
                    .onFailure { showToast("⚠️ لم تُسجل الهدية في قاعدة البيانات") }
            }
        }
        _userProfile.update {
            it.copy(
                goldCoins = it.goldCoins - totalCost,
                wealthExpCurrent = it.wealthExpCurrent + totalCost / 2,
                charismaExpCurrent = it.charismaExpCurrent + totalCost / 4
            )
        }
        syncCurrentProfileToMasterTable()
        syncRemoteProfileToSupabase()

        val updatedMe = _userProfile.value
        val giftAnnouncement = RoomChatMessage(
            id = "gift_${System.currentTimeMillis()}",
            senderUserId = updatedMe.uuid,
            senderName = updatedMe.nickname,
            senderDisplayId = updatedMe.displayId,
            senderRole = updatedMe.role,
            senderVip = updatedMe.vipTier,
            senderWealthLevel = updatedMe.wealthLevel,
            senderAvatarType = updatedMe.avatarType,
            senderCustomAvatarUri = updatedMe.customAvatarUri,
            senderChatBubbleFrame = updatedMe.equippedChatBubbleName,
            messageText = "أرسل ${gift.iconEmoji} ${gift.nameAr} ×$comboCount إلى $receiverName!",
            isGiftAnnouncement = true,
            giftIconEmoji = gift.iconEmoji,
            highlightColor = gift.primaryGlow
        )
        _roomMessages.update { (it + giftAnnouncement).takeLast(50) }
        persistRoomChatMessageToDatabase(giftAnnouncement, _activeRoom.value?.id ?: "room_1")

        _activeSvgaOverlay.value = ActiveSvgaAnimationState(
            effectType = gift.effectType,
            titleAr = gift.nameAr,
            senderName = me.nickname,
            receiverName = receiverName,
            comboCount = comboCount
        )
    }

    fun dismissSvgaOverlay() {
        _activeSvgaOverlay.value = null
    }

    fun triggerEntryWelcomePreview(mountTitleAr: String) {
        val me = _userProfile.value
        val effect = when {
            mountTitleAr.contains("تنين") -> SvgaEffectType.GOLDEN_DRAGON_7D
            mountTitleAr.contains("بوغاتي") || mountTitleAr.contains("سيارة") -> SvgaEffectType.SPORTS_CAR_BUGATTI_5D
            mountTitleAr.contains("عنقاء") -> SvgaEffectType.CRYSTAL_PHOENIX_7D
            mountTitleAr.contains("يخت") -> SvgaEffectType.LUXURY_YACHT_6D
            else -> SvgaEffectType.ROYAL_PALACE_CASTLE_7D
        }
        viewModelScope.launch {
            val remoteAsset = runCatching {
                val raw = SupabaseRestClient.findStoreAsset(mountTitleAr)
                val arr = org.json.JSONArray(raw)
                arr.optJSONObject(0)?.optString("asset_url")?.takeIf { it.isNotBlank() }
            }.getOrNull()
            _activeSvgaOverlay.value = ActiveSvgaAnimationState(
                effectType = effect,
                titleAr = mountTitleAr,
                senderName = me.nickname,
                receiverName = "دخولية غرفة ملكية 7D",
                comboCount = 1,
                isEntranceMount = true,
                assetFormat = "SVGA • GIF • WEBP • MP4 • PNG",
                customAssetUri = remoteAsset
            )
        }
    }

    fun playMiniGameInRoom(game: CasualGameItem) {
        val me = _userProfile.value
        if (me.goldCoins < game.minBetCoins) {
            showToast("⚠️ رصيدك لا يكفي للمشاركة في ${game.titleAr}")
            return
        }
        val reward = game.minBetCoins * 2
        _userProfile.update { it.copy(goldCoins = it.goldCoins - game.minBetCoins + reward) }
        syncCurrentProfileToMasterTable()
        syncRemoteProfileToSupabase()
        showToast("🎰 مبروك! ربحت +$reward 🪙 في ${game.titleAr}")
    }

    fun toggleRoomPkBattle() {
        _activeRoom.update { room ->
            room ?: return@update null
            val nextActive = !room.pkBattle.isActive
            room.copy(pkBattle = room.pkBattle.copy(isActive = nextActive))
        }
    }

    fun supportPkTeam(isRedTeam: Boolean) {
        _activeRoom.update { room ->
            room ?: return@update null
            val pk = room.pkBattle
            room.copy(
                pkBattle = if (isRedTeam) pk.copy(redScore = pk.redScore + 5000)
                else pk.copy(blueScore = pk.blueScore + 5000)
            )
        }
    }

    fun grabRoomLuckyBag() {
        val currentRoom = _activeRoom.value ?: return
        if (currentRoom.activeLuckyBagCoins <= 0L) return
        val wonCoins = 888L
        _userProfile.update { it.copy(goldCoins = it.goldCoins + wonCoins) }
        _activeRoom.value = currentRoom.copy(
            activeLuckyBagCoins = (currentRoom.activeLuckyBagCoins - wonCoins).coerceAtLeast(0L)
        )
        syncCurrentProfileToMasterTable()
        showToast("🧧 مبروك! حصلت على +$wonCoins 🪙 من حقيبة الحظ")
    }

    fun dropRoomLuckyBag(amount: Long) {
        val currentRoom = _activeRoom.value ?: return
        val me = _userProfile.value
        if (me.goldCoins < amount) {
            showToast("⚠️ رصيدك غير كافٍ لإطلاق حقيبة الحظ")
            return
        }
        _userProfile.update { it.copy(goldCoins = it.goldCoins - amount) }
        _activeRoom.value = currentRoom.copy(activeLuckyBagCoins = currentRoom.activeLuckyBagCoins + amount)
        syncCurrentProfileToMasterTable()
        showToast("🧧 أطلقت حقيبة حظ بقيمة $amount 🪙 في الغرفة!")
    }

    fun resetRoomMicGiftCounters() {
        _activeRoom.update { room ->
            room ?: return@update null
            room.copy(seats = room.seats.map { it.copy(seatGiftPoints = 0L) })
        }
    }

    fun changeRoomBackgroundStyle(styleId: String) {
        val themeItem = _storeItems.value.firstOrNull {
            it.category == StoreItemCategory.ROOM_THEMES && it.roomThemeId == styleId
        }
        val me = _userProfile.value
        if (themeItem != null && !themeItem.isOwned) {
            if (me.goldCoins < themeItem.priceCoins) {
                showToast("⚠️ رصيد العملات غير كافٍ لشراء ${themeItem.nameAr}")
                return
            }
            _userProfile.update { it.copy(goldCoins = it.goldCoins - themeItem.priceCoins) }
        }

        _storeItems.update { list ->
            list.map { existing ->
                if (existing.category == StoreItemCategory.ROOM_THEMES) {
                    if (existing.roomThemeId == styleId) {
                        existing.copy(isOwned = true, isEquipped = true)
                    } else {
                        existing.copy(isEquipped = false)
                    }
                } else existing
            }
        }

        _activeRoom.update { room ->
            room ?: return@update null
            room.copy(backgroundStyleId = styleId)
        }
        val activeId = _activeRoom.value?.id
        if (activeId != null) {
            _rooms.update { list ->
                list.map { r ->
                    if (r.id == activeId) r.copy(backgroundStyleId = styleId) else r
                }
            }
        }
        persistRoomsToDatabase()
        syncCurrentProfileToMasterTable()
    }

    /**
     * Buy or Equip item from the Store (`المتجر`) or My Accessories (`إكسسواراتي`).
     */
    fun buyOrEquipStoreItem(item: StoreCatalogItem) {
        viewModelScope.launch {
            try {
                if (PharaohSupabaseAuth.accessToken().isNullOrBlank()) { showToast("⚠️ يجب تسجيل الدخول أولاً"); return@launch }
                val alreadyOwned = item.isOwned
                if (!alreadyOwned) {
                    val result = JSONObject(SupabaseRpcClient.purchaseStoreItem(item.id))
                    if (!result.optBoolean("success", false)) error(result.optString("error", "تعذر إتمام الشراء"))
                }
                if (item.category == StoreItemCategory.FRAMES || item.category == StoreItemCategory.ENTRY_MOUNTS) {
                    val result = JSONObject(SupabaseRpcClient.equipStoreItem(item.id))
                    if (!result.optBoolean("success", false)) error(result.optString("error", "تعذر تفعيل العنصر"))
                }
                val remoteProfile = PharaohSupabaseAuth.loadProfile(_userProfile.value.uuid)
                _userProfile.update { it.copy(goldCoins = remoteProfile.coins, crystalDiamonds = remoteProfile.diamonds, vipTier = remoteProfile.vipLevel, customAvatarUri = remoteProfile.avatarUrl) }
                refreshStoreCatalogFromSupabase()
                showToast(if (alreadyOwned) "✨ تم تفعيل " + item.nameAr else "🛍️ تم شراء " + item.nameAr + " من Supabase")
            } catch (e: Exception) { showToast("⚠️ فشل العملية: " + (e.message ?: "خطأ غير معروف")) }
        }
    }
    fun giftStoreItemToFriend(item: StoreCatalogItem, targetUserId: String) {
        val me = _userProfile.value
        if (me.goldCoins < item.priceCoins) {
            showToast("⚠️ رصيد العملات الذهبية غير كافٍ لإهداء ${item.nameAr}")
            return
        }
        _userProfile.update { it.copy(goldCoins = it.goldCoins - item.priceCoins) }
        if (item.category == StoreItemCategory.FRAMES && item.frameStyle != null) {
            MasterAppDatabaseTable.grantFrameToUserById(targetUserId, item.frameStyle, item.id)
        } else if (item.category == StoreItemCategory.ENTRY_MOUNTS) {
            MasterAppDatabaseTable.grantEntryMountToUserById(targetUserId, item.nameAr, item.id)
        }
        syncCurrentProfileToMasterTable()
        syncRemoteProfileToSupabase()
        showToast("🎁 تم إهداء ${item.nameAr} إلى المستخدم ID: $targetUserId بنجاح!")
    }

    /**
     * Upgrade or Activate one of the 5 VIP Sections (`VIP 1` .. `VIP 5`).
     */
    fun upgradeVipTier(targetTier: Int, costCoins: Long) {
        val me = _userProfile.value
        val section = MasterAppDatabaseTable.getVipFiveSectionsTable().firstOrNull { it.vipTier == targetTier }
        val isFreeReactivation = me.vipTier >= targetTier
        if (!isFreeReactivation && me.goldCoins < costCoins) {
            showToast("⚠️ رصيدك لا يكفي لترقية VIP $targetTier")
            return
        }

        _userProfile.update { profile ->
            profile.copy(
                goldCoins = if (isFreeReactivation) profile.goldCoins else profile.goldCoins - costCoins,
                vipTier = targetTier.coerceIn(1, 5),
                frameStyle = section?.grantedFrameStyle ?: profile.frameStyle,
                entryWelcomeName = section?.grantedEntryMountNameAr ?: profile.entryWelcomeName
            )
        }
        syncCurrentProfileToMasterTable()
        syncRemoteProfileToSupabase()
        showToast("👑 تم تفعيل القسم الملكي VIP $targetTier مع الإطار الدائري والدخولية!")
    }

    /**
     * Host Agent Action: Invite any user by ID to become a Host in the agency (`إرسال دعوة لمستخدم ليصبح مضيفاً في وكالته`).
     */
    fun inviteUserToAgencyById(targetUserId: String, hostNameAr: String) {
        val addedMember = MasterAppDatabaseTable.inviteAndAddHostToAgency(targetUserId, hostNameAr)
        _agencyHostsMembers.value = MasterAppDatabaseTable.getAgencyHostsTable()
        showToast("📨 تم إرسال دعوة الوكالة وربط المضيف ${addedMember.nickname} (ID: ${addedMember.userId})")
    }

    /**
     * Charge Agent Action: Ship coins to any user by their ID (`شحن عملات لأي مستخدم عن طريق الآي دي الخاص به`).
     */
    fun shipCoinsAsChargeAgent(targetUserId: String, coinsAmount: Long) {
        val me = _userProfile.value
        if (!me.isChargeAgent || coinsAmount <= 0L) {
            showToast("⚠️ حسابك غير مفعل كوكيل شحن أو المبلغ غير صحيح")
            return
        }
        viewModelScope.launch {
            try {
                val agency = RemoteAccessContextStore.state.value.agencies.firstOrNull {
                    it.role == "charge_agent" || it.role == "owner"
                } ?: error("لا توجد وكالة شحن مرتبطة بالحساب")
                val targetUuid = SupabaseRestClient.findProfileIdByDisplayId(targetUserId)
                    ?: error("لم يتم العثور على المستخدم بهذا ID")
                val raw = SupabaseRpcClient.transferAgencyCoins(
                    agencyId = agency.agencyId,
                    targetUserId = targetUuid,
                    coins = coinsAmount,
                    note = "تحويل وكيل شحن إلى ID " + targetUserId
                )
                val result = JSONObject(raw)
                _userProfile.update { it.copy(chargeAgentCoinsBalance = result.optLong("remaining", agency.coinsBalance - coinsAmount)) }
                _chargeAgentShipmentLogs.update {
                    listOf(ChargeAgentShipmentLog(
                        id = "ship_" + System.currentTimeMillis(),
                        targetUserId = targetUserId.trim(),
                        descriptionAr = "تحويل حقيقي عبر Supabase إلى ID: " + targetUserId,
                        coinsShipped = coinsAmount,
                        timestampText = "الآن"
                    )) + it
                }
                showToast("⚡ تم تحويل %,d 🪙 إلى ID %s".format(coinsAmount, targetUserId))
            } catch (e: Exception) {
                showToast("⚠️ فشل التحويل: " + (e.message ?: "خطأ غير معروف"))
            }
        }
    }

    fun sendChargeAgentRewardById(targetUserId: String, rewardItem: ChargeAgentRewardItem) {
        val me = _userProfile.value
        if (!me.isChargeAgent) {
            showToast("⚠️ حسابك غير مفعل كوكيل شحن")
            return
        }
        viewModelScope.launch {
            try {
                val agency = RemoteAccessContextStore.state.value.agencies.firstOrNull {
                    it.role == "charge_agent" || it.role == "owner"
                } ?: error("لا توجد وكالة مرتبطة بالحساب")
                val targetUuid = SupabaseRestClient.findProfileIdByDisplayId(targetUserId)
                    ?: error("لم يتم العثور على المستخدم بهذا ID")
                val grantType = if (rewardItem.category == StoreItemCategory.FRAMES) "frame" else "entry"
                SupabaseRpcClient.grantAgencyItem(
                    agencyId = agency.agencyId,
                    targetUserId = targetUuid,
                    itemId = rewardItem.id,
                    grantType = grantType
                )
                _chargeAgentShipmentLogs.update {
                    listOf(ChargeAgentShipmentLog(
                        id = "rew_" + System.currentTimeMillis(),
                        targetUserId = targetUserId.trim(),
                        descriptionAr = "منح حقيقي من Supabase: " + rewardItem.nameAr,
                        coinsShipped = 0L,
                        rewardNameAr = rewardItem.nameAr,
                        timestampText = "الآن"
                    )) + it
                }
                showToast("🎁 تم إرسال " + rewardItem.nameAr + " إلى ID " + targetUserId)
            } catch (e: Exception) {
                showToast("⚠️ فشل إرسال المكافأة: " + (e.message ?: "خطأ غير معروف"))
            }
        }
    }

    fun rechargeGoldCoins(amount: Long) {
        // Direct client-side coin creation is intentionally disabled.
        // Recharge is performed by a charge agent/admin and recorded in Supabase.
        showToast("💳 الشحن يتم عبر وكيل الشحن من خلال قاعدة البيانات")
    }

    fun exchangeDiamondsToCoins(diamondsAmount: Long) {
        val me = _userProfile.value
        if (me.crystalDiamonds < diamondsAmount) {
            showToast("⚠️ رصيد الألماس غير كافٍ للتحويل")
            return
        }
        val coinsGained = (diamondsAmount * 12) / 10
        _userProfile.update {
            it.copy(
                crystalDiamonds = it.crystalDiamonds - diamondsAmount,
                goldCoins = it.goldCoins + coinsGained
            )
        }
        syncCurrentProfileToMasterTable()
        showToast("💎 تم تحويل %,d ألماسة إلى +%,d 🪙".format(diamondsAmount, coinsGained))
    }

    fun claimDailyCheckIn() {
        val me = _userProfile.value
        if (me.isCheckedInToday) return
        _userProfile.update {
            it.copy(
                goldCoins = it.goldCoins + 1500L,
                checkInStreakDays = (it.checkInStreakDays % 7) + 1,
                isCheckedInToday = true
            )
        }
        syncCurrentProfileToMasterTable()
        showToast("📅 تم تسجيل حضورك اليومي واستلام +1,500 🪙")
    }

    fun claimDailyTask(task: DailyTaskItem) {
        if (task.isClaimed) return
        _dailyTasks.update { list ->
            list.map { if (it.id == task.id) it.copy(isClaimed = true) else it }
        }
        _userProfile.update {
            it.copy(
                goldCoins = it.goldCoins + task.rewardCoins,
                wealthExpCurrent = it.wealthExpCurrent + task.rewardExp.toLong(),
                charismaExpCurrent = it.charismaExpCurrent + task.rewardExp.toLong()
            )
        }
        syncCurrentProfileToMasterTable()
        showToast("🎁 استلمت مكافأة المهمة +${task.rewardCoins} 🪙 و +${task.rewardExp} XP")
    }

    fun spinLuckyWheel(prizeCoins: Long) {
        _userProfile.update { it.copy(goldCoins = it.goldCoins + prizeCoins) }
        syncCurrentProfileToMasterTable()
        showToast("🎡 مبروك! ربحت +%,d 🪙 من عجلة الحظ!".format(prizeCoins))
    }

    fun boostCpIntimacy() {
        val me = _userProfile.value
        if (me.goldCoins < 1000L) {
            showToast("⚠️ تحتاج إلى 1,000 عملة ذهبية لإرسال خاتم العشاق")
            return
        }
        _userProfile.update {
            it.copy(
                goldCoins = it.goldCoins - 1000L,
                cpPoints = it.cpPoints + 2500L
            )
        }
        syncCurrentProfileToMasterTable()
        showToast("💍 زادت نقاط الألفة مع شريك الـ CP بمقدار +2,500 نقطة!")
    }

    fun toggleLikePost(postId: String) {
        _discoverPosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val nextLiked = !post.isLikedByMe
                    post.copy(
                        isLikedByMe = nextLiked,
                        likesCount = if (nextLiked) post.likesCount + 1 else post.likesCount - 1
                    )
                } else post
            }
        }
    }

    fun createFriendMomentPost(contentAr: String, roomTagAr: String, postImageUri: String? = null) {
        val me = _userProfile.value
        val myRoomTitle = _rooms.value.firstOrNull { it.roomDisplayId == me.displayId || it.hostUserId == me.uuid }?.titleAr
        val newPost = FriendPostMoment(
            id = "post_${System.currentTimeMillis()}",
            authorName = me.nickname,
            authorDisplayId = me.displayId,
            authorCountryFlag = me.countryFlag,
            authorRole = me.role,
            authorFrame = me.frameStyle,
            authorAvatarType = me.avatarType,
            authorCustomAvatarUri = me.customAvatarUri,
            postImageUri = postImageUri,
            timeAgoAr = "الآن",
            contentAr = contentAr,
            roomTagAr = roomTagAr.ifBlank { myRoomTitle ?: "غرفة ${me.nickname} 7D" },
            likesCount = 1,
            commentsCount = 0,
            giftCount = 0
        )
        _discoverPosts.update { listOf(newPost) + it }
        showToast("✨ تم نشر لحظتك الملكية في صفحة اكتشف")
    }

    fun openMessageThread(thread: DirectMessageThread) {
        _activeChatThread.value = thread.copy(unreadCount = 0)
        _messageThreads.update { list ->
            list.map { if (it.id == thread.id) it.copy(unreadCount = 0) else it }
        }
    }

    fun closeMessageThread() {
        _activeChatThread.value = null
    }

    fun sendDirectMessage(text: String) {
        val active = _activeChatThread.value ?: return
        val me = _userProfile.value
        val newMsg = ChatBubbleMessage(
            id = "dm_${System.currentTimeMillis()}",
            senderIsMe = true,
            text = text,
            timestampAr = "الآن"
        )
        val updated = active.copy(messages = active.messages + newMsg)
        _activeChatThread.value = updated
        _messageThreads.update { list -> list.map { if (it.id == updated.id) updated else it } }
        val dao = appDao
        if (dao != null) {
            viewModelScope.launch {
                dao.insertChatHistoryMessage(
                    newMsg.toDirectChatEntity(
                        threadId = updated.id,
                        friendName = updated.friendName,
                        friendDisplayId = updated.friendDisplayId,
                        friendCustomAvatarUri = updated.friendCustomAvatarUri,
                        friendAvatarType = updated.friendAvatarType,
                        myProfile = me
                    )
                )
            }
        }
    }

    fun updateProfileDetails(
        newNickname: String,
        newBio: String,
        newFlag: String,
        newCountryNameAr: String
    ) {
        _userProfile.update {
            it.copy(
                nickname = newNickname.ifBlank { it.nickname },
                bio = newBio.ifBlank { it.bio },
                countryFlag = newFlag,
                countryNameAr = newCountryNameAr
            )
        }
        syncCurrentProfileToMasterTable()
        showToast("✅ تم حفظ بيانات الملف الشخصي")
    }

    fun toggleLinkAccount(provider: String) {
        _userProfile.update {
            if (provider == "GOOGLE") it.copy(isAccountLinkedGoogle = !it.isAccountLinkedGoogle)
            else it.copy(isAccountLinkedPhone = !it.isAccountLinkedPhone)
        }
        syncCurrentProfileToMasterTable()
    }

    fun updateAppLanguage(languageNameAr: String) {
        _userProfile.update { it.copy(appLanguageNameAr = languageNameAr) }
        syncCurrentProfileToMasterTable()
        showToast("🌐 تم تغيير لغة التطبيق إلى: $languageNameAr")
    }

    fun inviteFriendAndIncrementCounter() {
        _userProfile.update { it.copy(invitedFriendsCount = it.invitedFriendsCount + 1) }
        syncCurrentProfileToMasterTable()
        showToast("🎉 تم إرسال دعوة لصديق جديد! عدد المدعوين الآن: ${_userProfile.value.invitedFriendsCount}")
    }

    fun claimInviteFriendReward(milestone: Int, rewardCoins: Long, rewardFrame: FrameStyle3D?) {
        val current = _userProfile.value
        if (current.claimedInviteMilestones.contains(milestone)) {
            showToast("✅ تم استلام هذه المكافأة مسبقاً")
            return
        }
        if (current.invitedFriendsCount < milestone) {
            showToast("⚠️ تحتاج إلى دعوة $milestone أصدقاء لاستلام هذه المكافأة (الحالي: ${current.invitedFriendsCount})")
            return
        }
        _userProfile.update {
            it.copy(
                goldCoins = it.goldCoins + rewardCoins,
                frameStyle = rewardFrame ?: it.frameStyle,
                claimedInviteMilestones = it.claimedInviteMilestones + milestone
            )
        }
        if (rewardFrame != null) {
            _storeItems.update { list ->
                list.map { item ->
                    if (item.frameStyle == rewardFrame) {
                        item.copy(isOwned = true, isEquipped = true)
                    } else if (item.category == StoreItemCategory.FRAMES) {
                        item.copy(isEquipped = false)
                    } else item
                }
            }
            // Update active room seat frame if user is seated
            val me = _userProfile.value
            _activeRoom.update { room ->
                room ?: return@update null
                room.copy(
                    seats = room.seats.map { seat ->
                        if (seat.occupantUserId == me.uuid || seat.occupantDisplayId == me.displayId) {
                            seat.copy(
                                occupantFrame = rewardFrame,
                                soundWaveColor = rewardFrame.soundWaveColor,
                                nameColor = rewardFrame.nameGradientColor
                            )
                        } else seat
                    }
                )
            }
        }
        syncCurrentProfileToMasterTable()
        val frameNotice = if (rewardFrame != null) " + ${rewardFrame.nameAr}" else ""
        showToast("🎁 مبروك! استلمت مكافأة دعوة الأصدقاء: +%,d عملة ذهبية%s".format(rewardCoins, frameNotice))
    }

    fun submitCustomerServiceTicket(categoryAr: String, messageAr: String) {
        if (messageAr.isBlank()) {
            showToast("⚠️ يرجى كتابة تفاصيل استفسارك لخدمة العملاء")
            return
        }
        showToast("🎧 تم إرسال طلبك لقسم [$categoryAr] بنجاح! سيرد عليك فريق خدمة العملاء فوراً.")
    }

    fun equipAvatarFrameDirectly(frameStyle: FrameStyle3D) {
        _userProfile.update { it.copy(frameStyle = frameStyle) }
        val me = _userProfile.value
        _storeItems.update { list ->
            list.map { item ->
                if (item.category == StoreItemCategory.FRAMES) {
                    item.copy(
                        isOwned = if (item.frameStyle == frameStyle) true else item.isOwned,
                        isEquipped = item.frameStyle == frameStyle
                    )
                } else item
            }
        }
        _activeRoom.update { room ->
            room ?: return@update null
            room.copy(
                seats = room.seats.map { seat ->
                    if (seat.occupantUserId == me.uuid || seat.occupantDisplayId == me.displayId) {
                        seat.copy(
                            occupantFrame = frameStyle,
                            soundWaveColor = frameStyle.soundWaveColor,
                            nameColor = frameStyle.nameGradientColor
                        )
                    } else seat
                }
            )
        }
        _rooms.update { list ->
            list.map { room ->
                room.copy(
                    seats = room.seats.map { seat ->
                        if (seat.occupantUserId == me.uuid || seat.occupantDisplayId == me.displayId) {
                            seat.copy(
                                occupantFrame = frameStyle,
                                soundWaveColor = frameStyle.soundWaveColor,
                                nameColor = frameStyle.nameGradientColor
                            )
                        } else seat
                    }
                )
            }
        }
        syncCurrentProfileToMasterTable()
        showToast("⭕ تم تركيب ${frameStyle.nameAr} على المايك والصورة الشخصية!")
    }

    fun logoutAccount() {
        _activeSubScreen.value = StandaloneSubScreen.NONE
        _activeRoom.value = null
        _isLoggedIn.value = false
    }

    fun deleteAccountPermanently() {
        _activeSubScreen.value = StandaloneSubScreen.NONE
        _activeRoom.value = null
        _isLoggedIn.value = false
        showToast("🗑️ تم حذف الحساب وتسجيل الخروج")
    }

    fun showToast(message: String) {
        _bannerToastText.value = message
    }

    fun clearToast() {
        _bannerToastText.value = null
    }

    private fun buildInitialRooms(): List<VoiceRoomModel> = listOf(
        VoiceRoomModel(
            id = "room_1",
            roomDisplayId = "1201637",
            titleAr = "قصر السلاطين والملوك 7D 👑",
            announcementAr = "أهلاً بكم في القصر الملكي • احترام الجميع واجب ✨",
            categoryAr = "دردشة",
            countryFlag = "🇪🇬",
            hostName = "محمد",
            hostUserId = "user_1201637",
            myRoleInRoom = RoomPermissionRole.OWNER,
            onlineCount = 428,
            heatScore = 98500L,
            coverBadgeText = "👑 7D",
            seats = buildSeatsForRoom("محمد", "1201637", FrameStyle3D.IMPERIAL_GOLD_WINGS)
        ),
        VoiceRoomModel(
            id = "room_2",
            roomDisplayId = "888888",
            titleAr = "سهرة الأميرة شهد للمواهب 🌸",
            announcementAr = "غناء وطرب ومسابقات صوتية مباشرة 🎙️",
            categoryAr = "دردشة",
            countryFlag = "🇪🇬",
            hostName = "الأميرة شهد",
            hostUserId = "user_888888",
            myRoleInRoom = RoomPermissionRole.ADMIN,
            onlineCount = 315,
            heatScore = 76400L,
            coverBadgeText = "🌸 VIP",
            seats = buildSeatsForRoom("الأميرة شهد", "888888", FrameStyle3D.ROSE_GOLD_PHOENIX)
        ),
        VoiceRoomModel(
            id = "room_3",
            roomDisplayId = "777777",
            titleAr = "ديوانية السلطان فهد للـ PK ⚔️",
            announcementAr = "تحديات PK نارية وهدايا التنين الذهبي 🐉",
            categoryAr = "دردشة",
            countryFlag = "🇦🇪",
            hostName = "السلطان فهد",
            hostUserId = "user_777777",
            myRoleInRoom = RoomPermissionRole.MEMBER,
            onlineCount = 512,
            heatScore = 124000L,
            coverBadgeText = "🐉 PK",
            seats = buildSeatsForRoom("السلطان فهد", "777777", FrameStyle3D.CRYSTAL_DRAGON_ICE)
        ),
        VoiceRoomModel(
            id = "room_4",
            roomDisplayId = "999999",
            titleAr = "مقر قبيلة صقور العرب 🦁",
            announcementAr = "اجتماع أعضاء القبيلة والداعمين الملكيين 🛡️",
            categoryAr = "دردشة",
            countryFlag = "🇶🇦",
            hostName = "الأمير نواف",
            hostUserId = "user_999999",
            myRoleInRoom = RoomPermissionRole.MEMBER,
            onlineCount = 264,
            heatScore = 64200L,
            coverBadgeText = "🦁 CLAN",
            seats = buildSeatsForRoom("الأمير نواف", "999999", FrameStyle3D.EMERALD_SULTAN_CROWN)
        ),
        VoiceRoomModel(
            id = "room_5",
            roomDisplayId = "555666",
            titleAr = "نجوم الطرب الأصيل 🎶",
            announcementAr = "أحلى الأصوات الطربية والمسابقات الغنائية 🎤",
            categoryAr = "دردشة",
            countryFlag = "🇪🇬",
            hostName = "نور القمر",
            hostUserId = "user_555666",
            myRoleInRoom = RoomPermissionRole.ADMIN,
            onlineCount = 195,
            heatScore = 54300L,
            coverBadgeText = "🎶 LIVE",
            seats = buildSeatsForRoom("نور القمر", "555666", FrameStyle3D.ROYAL_VIOLET_AURA)
        ),
        VoiceRoomModel(
            id = "room_6",
            roomDisplayId = "444333",
            titleAr = "مجلس النشامى وأهل الكرم ☕",
            announcementAr = "سوالف ووناسة وتعارف شباب وبنات الخليج والوطن العربي ✨",
            categoryAr = "دردشة",
            countryFlag = "🇾🇪",
            hostName = "الشيخ ذياب",
            hostUserId = "user_444333",
            myRoleInRoom = RoomPermissionRole.MEMBER,
            onlineCount = 178,
            heatScore = 49800L,
            coverBadgeText = "☕ VIP",
            seats = buildSeatsForRoom("الشيخ ذياب", "444333", FrameStyle3D.IMPERIAL_GOLD_WINGS)
        ),
        VoiceRoomModel(
            id = "room_7",
            roomDisplayId = "333222",
            titleAr = "كافيه القلوب الراقية 💖",
            announcementAr = "هدوء وموسيقى كلاسيكية ودردشة راقية للجميع 🌸",
            categoryAr = "دردشة",
            countryFlag = "🇦🇪",
            hostName = "الملكة ريم",
            hostUserId = "user_333222",
            myRoleInRoom = RoomPermissionRole.MEMBER,
            onlineCount = 234,
            heatScore = 61900L,
            coverBadgeText = "💖 7D",
            seats = buildSeatsForRoom("الملكة ريم", "333222", FrameStyle3D.ROSE_GOLD_PHOENIX)
        ),
        VoiceRoomModel(
            id = "room_8",
            roomDisplayId = "222111",
            titleAr = "ملوك التحديات والجوائز 🏆",
            announcementAr = "صناديق حظ وهدايا ذهبية كل ربع ساعة 🎁",
            categoryAr = "دردشة",
            countryFlag = "🇶🇦",
            hostName = "الإمبراطور راشد",
            hostUserId = "user_222111",
            myRoleInRoom = RoomPermissionRole.MEMBER,
            onlineCount = 389,
            heatScore = 88200L,
            coverBadgeText = "🏆 TOP",
            seats = buildSeatsForRoom("الإمبراطور راشد", "222111", FrameStyle3D.CRYSTAL_DRAGON_ICE)
        )
    )

    private fun buildSeatsForRoom(
        hostName: String,
        hostDisplayId: String,
        hostFrame: FrameStyle3D,
        hostCustomAvatarUri: String? = null
    ): List<MicSeatState> = listOf(
        MicSeatState(
            seatIndex = 0,
            seatLabelAr = "مايك المضيف",
            isVipSeat = true,
            occupantUserId = "user_$hostDisplayId",
            occupantName = hostName,
            occupantDisplayId = hostDisplayId,
            occupantAvatarType = if (hostName.contains("شهد")) "PRINCESS" else "PRINCE",
            occupantCustomAvatarUri = hostCustomAvatarUri,
            occupantFrame = hostFrame,
            occupantRole = AppUserRole.HOST,
            occupantRoomRole = RoomPermissionRole.OWNER,
            occupantVip = 5,
            isSpeaking = true,
            seatGiftPoints = 48500L,
            soundWaveColor = hostFrame.soundWaveColor,
            nameColor = hostFrame.nameGradientColor
        ),
        MicSeatState(
            seatIndex = 1,
            seatLabelAr = "مايك 1",
            occupantUserId = "user_888888",
            occupantName = "الأميرة شهد 🌸",
            occupantDisplayId = "888888",
            occupantAvatarType = "PRINCESS",
            occupantFrame = FrameStyle3D.ROSE_GOLD_PHOENIX,
            occupantRole = AppUserRole.HOST,
            occupantRoomRole = RoomPermissionRole.ADMIN,
            occupantVip = 4,
            isSpeaking = true,
            seatGiftPoints = 22400L,
            soundWaveColor = Color(0xFFFF4081),
            nameColor = Color(0xFFFF80AB)
        ),
        MicSeatState(
            seatIndex = 2,
            seatLabelAr = "مايك 2",
            occupantUserId = "user_777777",
            occupantName = "السلطان فهد 💎",
            occupantDisplayId = "777777",
            occupantAvatarType = "PRINCE",
            occupantFrame = FrameStyle3D.CRYSTAL_DRAGON_ICE,
            occupantRole = AppUserRole.SUPPORTER,
            occupantRoomRole = RoomPermissionRole.MEMBER,
            occupantVip = 5,
            isSpeaking = true,
            seatGiftPoints = 35900L,
            soundWaveColor = Color(0xFF00E5FF),
            nameColor = Color(0xFF80D8FF)
        ),
        MicSeatState(seatIndex = 3, seatLabelAr = "مايك 3"),
        MicSeatState(seatIndex = 4, seatLabelAr = "مايك 4"),
        MicSeatState(
            seatIndex = 5,
            seatLabelAr = "مايك 5",
            occupantUserId = "user_555666",
            occupantName = "نور القمر 🇲🇦",
            occupantDisplayId = "555666",
            occupantAvatarType = "PRINCESS",
            occupantFrame = FrameStyle3D.EMERALD_SULTAN_CROWN,
            occupantRole = AppUserRole.HOST,
            occupantRoomRole = RoomPermissionRole.MEMBER,
            occupantVip = 3,
            isSpeaking = true,
            seatGiftPoints = 12800L,
            soundWaveColor = Color(0xFF00E676),
            nameColor = Color(0xFF69F0AE)
        ),
        MicSeatState(seatIndex = 6, seatLabelAr = "مايك 6"),
        MicSeatState(seatIndex = 7, seatLabelAr = "مايك 7"),
        MicSeatState(seatIndex = 8, seatLabelAr = "مايك 8", isLocked = true)
    )

    private fun buildInitialRoomMessages(): List<RoomChatMessage> = listOf(
        RoomChatMessage(
            id = "m1",
            senderUserId = "user_888888",
            senderName = "الأميرة شهد 🌸",
            senderDisplayId = "888888",
            senderRole = AppUserRole.HOST,
            senderRoomRole = RoomPermissionRole.ADMIN,
            senderVip = 4,
            senderWealthLevel = 36,
            messageText = "أهلاً وسهلاً بكم جميعاً في السهرة الملكية 👑✨",
            highlightColor = Color(0xFFFF4081)
        ),
        RoomChatMessage(
            id = "m2",
            senderUserId = "user_777777",
            senderName = "السلطان فهد 💎",
            senderDisplayId = "777777",
            senderRole = AppUserRole.SUPPORTER,
            senderRoomRole = RoomPermissionRole.MEMBER,
            senderVip = 5,
            senderWealthLevel = 50,
            messageText = "منورين المايكات! الإطارات الدائرية والموجات الصوتية تحفة 🔥",
            highlightColor = Color(0xFF00E5FF)
        )
    )

    private fun buildInitialGiftsCatalog(): List<GiftItem3D> = listOf(
        GiftItem3D(
            id = "gift_dragon_7d",
            nameAr = "تنين الإمبراطور 7D",
            priceCoins = 25000,
            categoryAr = "ملكي 7D",
            dimensionBadge = "7D SVGA",
            iconEmoji = "🐉",
            effectType = SvgaEffectType.GOLDEN_DRAGON_7D,
            primaryGlow = Color(0xFFFFD700),
            secondaryGlow = Color(0xFFFF1744),
            receivedCount = 18
        ),
        GiftItem3D(
            id = "gift_castle_7d",
            nameAr = "قصر السلاطين 7D",
            priceCoins = 50000,
            categoryAr = "ملكي 7D",
            dimensionBadge = "7D SVGA",
            iconEmoji = "🏰",
            effectType = SvgaEffectType.ROYAL_PALACE_CASTLE_7D,
            primaryGlow = Color(0xFFFFD700),
            secondaryGlow = Color(0xFFAA00FF),
            receivedCount = 9
        ),
        GiftItem3D(
            id = "gift_bugatti_5d",
            nameAr = "بوغاتي النيون 5D",
            priceCoins = 12000,
            categoryAr = "سيارات",
            dimensionBadge = "5D SVGA",
            iconEmoji = "🏎️",
            effectType = SvgaEffectType.SPORTS_CAR_BUGATTI_5D,
            primaryGlow = Color(0xFF00E5FF),
            secondaryGlow = Color(0xFF2979FF),
            receivedCount = 27
        ),
        GiftItem3D(
            id = "gift_phoenix_7d",
            nameAr = "عنقاء الياقوت 7D",
            priceCoins = 35000,
            categoryAr = "ملكي 7D",
            dimensionBadge = "7D SVGA",
            iconEmoji = "🦅",
            effectType = SvgaEffectType.CRYSTAL_PHOENIX_7D,
            primaryGlow = Color(0xFFFF4081),
            secondaryGlow = Color(0xFFFFD700),
            receivedCount = 14
        ),
        GiftItem3D(
            id = "gift_yacht_6d",
            nameAr = "اليخت الملكي 6D",
            priceCoins = 18000,
            categoryAr = "فاخر",
            dimensionBadge = "6D SVGA",
            iconEmoji = "🛥️",
            effectType = SvgaEffectType.LUXURY_YACHT_6D,
            primaryGlow = Color(0xFF00E5FF),
            secondaryGlow = Color(0xFFFFD700),
            receivedCount = 21
        ),
        GiftItem3D(
            id = "gift_crown_4d",
            nameAr = "تاج الألماس 4D",
            priceCoins = 5000,
            categoryAr = "كلاسيك",
            dimensionBadge = "4D SVGA",
            iconEmoji = "💎",
            effectType = SvgaEffectType.DIAMOND_CROWN_4D,
            primaryGlow = Color(0xFFE040FB),
            secondaryGlow = Color(0xFF00E5FF),
            receivedCount = 45
        )
    )

    private fun buildInitialGamesCatalog(): List<CasualGameItem> = listOf(
        CasualGameItem(
            id = "game_wheel",
            titleAr = "عجلة الإمبراطور 7D",
            subtitleAr = "اربح حتى X50 من العملات الذهبية",
            badgeText = "HOT 🔥",
            iconEmoji = "🎡",
            minBetCoins = 1000,
            maxMultiplier = "X50",
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFF8C5808)
        ),
        CasualGameItem(
            id = "game_dragon_treasure",
            titleAr = "كنز التنين الذهبي",
            subtitleAr = "صناديق الحظ الفورية داخل الغرفة",
            badgeText = "7D",
            iconEmoji = "🐉",
            minBetCoins = 2000,
            maxMultiplier = "X100",
            primaryColor = Color(0xFFFF4081),
            secondaryColor = Color(0xFF880E4F)
        )
    )

    private fun buildInitialDiscoverPosts(): List<FriendPostMoment> = listOf(
        FriendPostMoment(
            id = "post_1",
            authorName = "الأميرة شهد 🌸",
            authorDisplayId = "888888",
            authorCountryFlag = "🇸🇦",
            authorRole = AppUserRole.HOST,
            authorFrame = FrameStyle3D.ROSE_GOLD_PHOENIX,
            authorAvatarType = "PRINCESS",
            timeAgoAr = "منذ 10 دقائق",
            contentAr = "حياكم الله جميعاً في سهرتنا الصوتية الليلة! مسابقات وهدايا وإطارات دائرية جديدة بانتظاركم 🎙️👑✨",
            roomTagAr = "سهرة الأميرة شهد للمواهب 🌸",
            likesCount = 342,
            commentsCount = 58,
            giftCount = 24
        ),
        FriendPostMoment(
            id = "post_2",
            authorName = "السلطان فهد 💎",
            authorDisplayId = "777777",
            authorCountryFlag = "🇦🇪",
            authorRole = AppUserRole.SUPPORTER,
            authorFrame = FrameStyle3D.CRYSTAL_DRAGON_ICE,
            authorAvatarType = "PRINCE",
            timeAgoAr = "منذ 35 دقيقة",
            contentAr = "تم تفعيل دخولية تنين الإمبراطور الذهبي 7D والـ VIP 5! ننتظركم في تحدي الـ PK الليلة 🐉🔥",
            roomTagAr = "ديوانية السلطان فهد للـ PK ⚔️",
            likesCount = 518,
            commentsCount = 94,
            giftCount = 67
        )
    )

    private fun buildInitialMessageThreads(): List<DirectMessageThread> = listOf(
        DirectMessageThread(
            id = "th_1",
            friendName = "الأميرة شهد 🌸",
            friendDisplayId = "888888",
            friendRole = AppUserRole.HOST,
            friendFrame = FrameStyle3D.ROSE_GOLD_PHOENIX,
            friendAvatarType = "PRINCESS",
            isOnline = true,
            unreadCount = 2,
            messages = listOf(
                ChatBubbleMessage("m_1", false, "مساء النور! شكراً على استضافتي في الوكالة الملكية 🌸", "10:15 م"),
                ChatBubbleMessage("m_2", true, "أهلاً بكِ يا شهد، إحصائيات ساعات المايك والهدايا ممتازة هذا الشهر 👑", "10:17 م")
            )
        ),
        DirectMessageThread(
            id = "th_2",
            friendName = "السلطان فهد 💎",
            friendDisplayId = "777777",
            friendRole = AppUserRole.SUPPORTER,
            friendFrame = FrameStyle3D.CRYSTAL_DRAGON_ICE,
            friendAvatarType = "PRINCE",
            isOnline = true,
            unreadCount = 0,
            messages = listOf(
                ChatBubbleMessage("m_3", false, "تم استلام شحن العملات ومكافأة الإطار الدائري من وكالة الشحن، تسلم! ⚡", "09:40 م")
            )
        )
    )

    private fun buildInitialWalletTransactions(): List<WalletTransactionItem> = listOf(
        WalletTransactionItem("tx_1", "شحن رصيد عبر وكالة الشحن المعتمدة", "+150,000 🪙", "اليوم • 09:30 م", true),
        WalletTransactionItem("tx_2", "تحويل ألماس المضيف إلى عملات ذهبية", "+45,000 🪙", "أمس • 11:15 م", true),
        WalletTransactionItem("tx_3", "إرسال هدية تنين الإمبراطور 7D", "-25,000 🪙", "أمس • 08:20 م", false)
    )

    private fun buildInitialDailyTasks(): List<DailyTaskItem> = listOf(
        DailyTaskItem("dt_1", "التحدث على المايك لمدة 15 دقيقة", "اصعد على المايك في أي غرفة صوتية", 2000L, 300, 15, 15, false, "🎙️"),
        DailyTaskItem("dt_2", "إرسال هدية 7D لصديق في الغرفة", "ادعم مضيفاً أو صديقاً على المايك", 3500L, 500, 1, 1, false, "🎁"),
        DailyTaskItem("dt_3", "متابعة 3 أصدقاء جدد داخل الغرف", "اضغط على صورة عضو واختر متابعة", 1500L, 200, 3, 3, false, "➕")
    )

    private fun buildInitialLeaderboard(): List<LeaderboardEntry> = listOf(
        LeaderboardEntry(1, "محمد 👑", "1201637", "🇪🇬", "PRINCE", FrameStyle3D.IMPERIAL_GOLD_WINGS, 45, 5, 980000L, "980K 🪙"),
        LeaderboardEntry(2, "السلطان فهد 💎", "777777", "🇦🇪", "PRINCE", FrameStyle3D.CRYSTAL_DRAGON_ICE, 42, 5, 845000L, "845K 🪙"),
        LeaderboardEntry(3, "الأميرة شهد 🌸", "888888", "🇸🇦", "PRINCESS", FrameStyle3D.ROSE_GOLD_PHOENIX, 39, 4, 720000L, "720K 🪙"),
        LeaderboardEntry(4, "الأمير نواف 🇰🇼", "999999", "🇰🇼", "PRINCE", FrameStyle3D.EMERALD_SULTAN_CROWN, 34, 4, 540000L, "540K 🪙"),
        LeaderboardEntry(5, "نور القمر 🇲🇦", "555666", "🇲🇦", "PRINCESS", FrameStyle3D.ROYAL_VIOLET_AURA, 31, 3, 410000L, "410K 🪙")
    )

    private fun UserProfile.toEntity(): UserAccountEntity = UserAccountEntity(
        uuid = uuid,
        displayId = displayId,
        isSpecialId = isSpecialId,
        email = email,
        nickname = nickname,
        bio = bio,
        avatarType = avatarType,
        customAvatarUri = customAvatarUri,
        customCoverUri = customCoverUri,
        countryFlag = countryFlag,
        countryNameAr = countryNameAr,
        roleCode = role.name,
        wealthLevel = wealthLevel,
        wealthExpCurrent = wealthExpCurrent,
        wealthExpTarget = wealthExpTarget,
        charismaLevel = charismaLevel,
        charismaExpCurrent = charismaExpCurrent,
        charismaExpTarget = charismaExpTarget,
        vipTier = vipTier,
        goldCoins = goldCoins,
        crystalDiamonds = crystalDiamonds,
        equippedFrameId = frameStyle.name,
        equippedWelcomeName = entryWelcomeName,
        equippedChatBubbleName = equippedChatBubbleName,
        cpPartnerName = cpPartnerName,
        cpPartnerId = cpPartnerId,
        cpPoints = cpPoints,
        familyNameAr = familyNameAr,
        familyRankAr = familyRankAr,
        visitorsCount = visitorsCount,
        followersCount = followersCount,
        followingCount = followingCount,
        friendsCount = friendsCount,
        appLanguageNameAr = appLanguageNameAr,
        checkInStreakDays = checkInStreakDays,
        isCheckedInToday = isCheckedInToday,
        invitedFriendsCount = invitedFriendsCount,
        isHostAgent = isHostAgent,
        isApprovedHost = isHostMember,
        isChargeAgent = isChargeAgent,
        chargeAgentCoinsBalance = chargeAgentCoinsBalance
    )

    private fun UserAccountEntity.toUserProfile(): UserProfile {
        val parsedRole = runCatching { AppUserRole.valueOf(roleCode) }.getOrDefault(AppUserRole.SUPPORTER)
        val parsedFrame = runCatching { FrameStyle3D.valueOf(equippedFrameId) }.getOrDefault(FrameStyle3D.IMPERIAL_GOLD_WINGS)
        return UserProfile(
            uuid = uuid,
            displayId = displayId,
            isSpecialId = isSpecialId,
            email = email,
            nickname = nickname,
            bio = bio,
            avatarType = avatarType,
            customAvatarUri = customAvatarUri,
            customCoverUri = customCoverUri,
            countryFlag = countryFlag,
            countryNameAr = countryNameAr,
            role = parsedRole,
            wealthLevel = wealthLevel,
            wealthExpCurrent = wealthExpCurrent,
            wealthExpTarget = wealthExpTarget,
            charismaLevel = charismaLevel,
            charismaExpCurrent = charismaExpCurrent,
            charismaExpTarget = charismaExpTarget,
            vipTier = vipTier,
            goldCoins = goldCoins,
            crystalDiamonds = crystalDiamonds,
            frameStyle = parsedFrame,
            entryWelcomeName = equippedWelcomeName,
            equippedChatBubbleName = equippedChatBubbleName,
            cpPartnerName = cpPartnerName,
            cpPartnerId = cpPartnerId,
            cpPoints = cpPoints,
            familyNameAr = familyNameAr,
            familyRankAr = familyRankAr,
            visitorsCount = visitorsCount,
            followersCount = followersCount,
            followingCount = followingCount,
            friendsCount = friendsCount,
            appLanguageNameAr = appLanguageNameAr,
            checkInStreakDays = checkInStreakDays,
            isCheckedInToday = isCheckedInToday,
            invitedFriendsCount = invitedFriendsCount,
            isHostAgent = isHostAgent,
            isHostMember = isApprovedHost,
            isChargeAgent = isChargeAgent,
            chargeAgentCoinsBalance = chargeAgentCoinsBalance,
            badges = MasterAppDatabaseTable.defaultBadgesSeed()
        )
    }

    private fun VoiceRoomModel.toRoomSettingsEntity(): RoomSettingsEntity = RoomSettingsEntity(
        roomId = id,
        roomDisplayId = roomDisplayId,
        titleAr = titleAr,
        announcementAr = announcementAr,
        categoryAr = categoryAr,
        countryFlag = countryFlag,
        hostUserId = hostUserId,
        hostName = hostName,
        customCoverImageUri = customCoverImageUri,
        hostCustomAvatarUri = hostCustomAvatarUri,
        backgroundStyleId = backgroundStyleId,
        micShapeStyleId = micShapeStyleId,
        onlineCount = onlineCount,
        heatScore = heatScore,
        coverBadgeText = coverBadgeText,
        isPkBattleActive = pkBattle.isActive,
        pkRedScore = pkBattle.redScore,
        pkBlueScore = pkBattle.blueScore
    )

    private fun VoiceRoomModel.mergeWithSettingsEntity(saved: RoomSettingsEntity): VoiceRoomModel = copy(
        roomDisplayId = saved.roomDisplayId,
        titleAr = saved.titleAr,
        announcementAr = saved.announcementAr,
        categoryAr = saved.categoryAr,
        countryFlag = saved.countryFlag,
        hostUserId = saved.hostUserId,
        hostName = saved.hostName,
        customCoverImageUri = saved.customCoverImageUri ?: customCoverImageUri,
        hostCustomAvatarUri = saved.hostCustomAvatarUri ?: hostCustomAvatarUri,
        backgroundStyleId = saved.backgroundStyleId,
        micShapeStyleId = saved.micShapeStyleId,
        coverBadgeText = saved.coverBadgeText,
        seats = seats.map { seat ->
            if (seat.seatIndex == 0) {
                seat.copy(
                    occupantName = saved.hostName,
                    occupantDisplayId = saved.roomDisplayId,
                    occupantCustomAvatarUri = saved.hostCustomAvatarUri ?: seat.occupantCustomAvatarUri
                )
            } else seat
        }
    )

    private fun RoomSettingsEntity.toVoiceRoomModel(): VoiceRoomModel = VoiceRoomModel(
        id = roomId,
        roomDisplayId = roomDisplayId,
        titleAr = titleAr,
        announcementAr = announcementAr,
        categoryAr = categoryAr,
        countryFlag = countryFlag,
        hostName = hostName,
        hostUserId = hostUserId,
        myRoleInRoom = if (roomDisplayId == _userProfile.value.displayId) RoomPermissionRole.OWNER else RoomPermissionRole.MEMBER,
        onlineCount = onlineCount,
        heatScore = heatScore,
        coverBadgeText = coverBadgeText,
        customCoverImageUri = customCoverImageUri,
        hostCustomAvatarUri = hostCustomAvatarUri,
        backgroundStyleId = backgroundStyleId,
        micShapeStyleId = micShapeStyleId,
        seats = buildSeatsForRoom(hostName, roomDisplayId, FrameStyle3D.IMPERIAL_GOLD_WINGS, hostCustomAvatarUri)
    )

    private fun RoomChatMessage.toChatHistoryEntity(roomId: String): ChatHistoryEntity = ChatHistoryEntity(
        messageId = id,
        chatScope = "ROOM",
        roomId = roomId,
        senderUserId = senderUserId,
        senderDisplayId = senderDisplayId,
        senderName = senderName,
        senderCustomAvatarUri = senderCustomAvatarUri,
        senderAvatarType = senderAvatarType,
        senderRoleCode = senderRole.name,
        senderRoomRoleCode = senderRoomRole.name,
        senderVip = senderVip,
        senderWealthLevel = senderWealthLevel,
        senderChatBubbleFrame = senderChatBubbleFrame,
        messageText = messageText,
        isGiftAnnouncement = isGiftAnnouncement,
        isSystemWelcome = isSystemWelcome,
        giftIconEmoji = giftIconEmoji
    )

    private fun ChatHistoryEntity.toRoomChatMessage(): RoomChatMessage {
        val parsedRole = runCatching { AppUserRole.valueOf(senderRoleCode) }.getOrDefault(AppUserRole.USER)
        val parsedRoomRole = runCatching { RoomPermissionRole.valueOf(senderRoomRoleCode) }.getOrDefault(RoomPermissionRole.MEMBER)
        return RoomChatMessage(
            id = messageId,
            senderUserId = senderUserId,
            senderName = senderName,
            senderDisplayId = senderDisplayId,
            senderRole = parsedRole,
            senderRoomRole = parsedRoomRole,
            senderVip = senderVip,
            senderWealthLevel = senderWealthLevel,
            senderAvatarType = senderAvatarType,
            senderCustomAvatarUri = senderCustomAvatarUri,
            senderChatBubbleFrame = senderChatBubbleFrame,
            messageText = messageText,
            isGiftAnnouncement = isGiftAnnouncement,
            isSystemWelcome = isSystemWelcome,
            giftIconEmoji = giftIconEmoji,
            highlightColor = if (isGiftAnnouncement || isSystemWelcome) Color(0xFFFFD700) else Color(0xFF00E5FF)
        )
    }

    private fun ChatBubbleMessage.toDirectChatEntity(
        threadId: String,
        friendName: String,
        friendDisplayId: String,
        friendCustomAvatarUri: String?,
        friendAvatarType: String,
        myProfile: UserProfile
    ): ChatHistoryEntity = ChatHistoryEntity(
        messageId = "${threadId}_$id",
        chatScope = "DIRECT",
        threadId = threadId,
        senderUserId = if (senderIsMe) myProfile.uuid else "user_$friendDisplayId",
        senderDisplayId = if (senderIsMe) myProfile.displayId else friendDisplayId,
        senderName = if (senderIsMe) myProfile.nickname else friendName,
        senderCustomAvatarUri = if (senderIsMe) myProfile.customAvatarUri else friendCustomAvatarUri,
        senderAvatarType = if (senderIsMe) myProfile.avatarType else friendAvatarType,
        senderVip = if (senderIsMe) myProfile.vipTier else 4,
        senderWealthLevel = if (senderIsMe) myProfile.wealthLevel else 36,
        senderCharismaLevel = if (senderIsMe) myProfile.charismaLevel else 32,
        senderIsMe = senderIsMe,
        messageText = text,
        coinsGiftAmount = coinsGiftAmount,
        timestampText = timestampAr
    )

    private fun ChatHistoryEntity.toChatBubbleMessage(): ChatBubbleMessage = ChatBubbleMessage(
        id = messageId.substringAfter("${threadId}_", messageId),
        senderIsMe = senderIsMe,
        text = messageText,
        timestampAr = timestampText,
        coinsGiftAmount = coinsGiftAmount,
        senderName = senderName
    )

    private fun HomeBannerSlideItem.toEntity(sortOrder: Int): HomeBannerEntity = HomeBannerEntity(
        bannerId = id,
        titleAr = titleAr,
        subtitleAr = subtitleAr,
        badgeTextAr = badgeTextAr,
        iconEmoji = iconEmoji,
        customImageUri = customImageUri,
        actionTargetScreen = targetSubScreen.name,
        primaryColorHex = gradientStartHex,
        secondaryColorHex = gradientMidHex,
        accentGoldHex = accentColorHex,
        sortOrder = sortOrder,
        isActive = isVisible
    )

    private fun HomeBannerEntity.toBannerSlideItem(): HomeBannerSlideItem = HomeBannerSlideItem(
        id = bannerId,
        titleAr = titleAr,
        subtitleAr = subtitleAr,
        badgeTextAr = badgeTextAr,
        iconEmoji = iconEmoji,
        gradientStartHex = primaryColorHex,
        gradientMidHex = secondaryColorHex,
        gradientEndHex = "#1E1B4B",
        accentColorHex = accentGoldHex,
        targetSubScreen = runCatching { StandaloneSubScreen.valueOf(actionTargetScreen) }
            .getOrDefault(StandaloneSubScreen.COINS_ONLY),
        customImageUri = customImageUri,
        sortOrder = sortOrder,
        isVisible = isActive
    )
}

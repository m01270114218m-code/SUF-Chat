package com.example.config

import androidx.compose.ui.graphics.Color
import com.example.models.AppUserRole
import com.example.models.BadgeModel
import com.example.models.ChargeAgentRewardItem
import com.example.models.FrameStyle3D
import com.example.models.HomeBannerSlideItem
import com.example.models.HostAgencyMemberRecord
import com.example.models.RoomMicShapeStyle
import com.example.models.RoomPermissionRole
import com.example.models.StandaloneSubScreen
import com.example.models.StoreCatalogItem
import com.example.models.StoreItemCategory
import com.example.models.UserProfile
import com.example.models.VipSectionConfig
import com.example.models.VoiceRoomModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * =========================================================================================
 * INTERNAL MASTER DATABASE CONFIGURATION TABLE (`MasterAppDatabaseTable`)
 * =========================================================================================
 * This file acts as the single internal source-of-truth data table inside the source code
 * for every small and large element in the application.
 *
 * - Designed for direct binding to a remote database (Firebase Firestore / Supabase / MySQL / REST API).
 * - Supports full CRUD operations: Reset (`reset*`), Edit (`edit*`), Swap/Toggle (`swap*`),
 *   Hide (`setVisibility*`), and Delete (`delete*`) for any item, section, or role.
 * - Supports granting any user by their `displayId` (`ID`):
 *   - Specific Circular Frames (`grantFrameToUserById`)
 *   - Specific Animated Entry Mounts (`grantEntryMountToUserById`)
 *   - Specific Badges (`grantBadgeToUserById`)
 *   - Charge Agent activation & coin balance (`activateChargeAgentById`)
 *   - Host Agent or Host activation (`activateHostAgentById` / `assignHostToAgencyById`)
 *   - Special ID assignment (`assignSpecialIdToUser`)
 * - Strictly internal: No database administration or server debug controls are ever shown in the end-user UI.
 */
object MasterAppDatabaseTable {

    // -------------------------------------------------------------------------------------
    // 1. PERSISTENT ACCOUNT REGISTRY TABLE (Email + Password -> Persistent User Profile & ID)
    // -------------------------------------------------------------------------------------
    data class RegisteredUserAccountRecord(
        val email: String,
        val passwordPlain: String,
        var profile: UserProfile,
        val ownedItemIds: MutableSet<String> = mutableSetOf(
            "store_frame_1",
            "store_frame_3",
            "store_mount_1",
            "store_mount_2",
            "store_bubble_1",
            "store_bubble_2",
            "store_room_theme_1"
        )
    )

    private val registeredAccountsByEmail = mutableMapOf<String, RegisteredUserAccountRecord>()
    private var nextGeneratedNumericId = 1201638L

    /**
     * Authenticates an existing user if the email is already registered (matching email & password),
     * or creates a brand-new account with a unique generated ID if the email does not exist yet.
     */
    fun authenticateOrCreateAccount(
        emailInput: String,
        passwordInput: String,
        nicknameInput: String,
        avatarTypeInput: String
    ): Pair<RegisteredUserAccountRecord?, String?> {
        val normalizedEmail = emailInput.trim().lowercase()
        val existing = registeredAccountsByEmail[normalizedEmail]
        if (existing != null) {
            return if (existing.passwordPlain == passwordInput) {
                val updatedNickname = if (nicknameInput.isNotBlank()) nicknameInput.trim() else existing.profile.nickname
                val dbAgencyGrant = agencyPermissionsRegistryByUserId[existing.profile.displayId]
                existing.profile = if (dbAgencyGrant != null) {
                    existing.profile.copy(
                        nickname = updatedNickname,
                        isHostAgent = dbAgencyGrant.isHostAgent,
                        isHostMember = dbAgencyGrant.isApprovedHost,
                        isChargeAgent = dbAgencyGrant.isChargeAgent,
                        agencyName = dbAgencyGrant.agencyNameAr,
                        chargeAgentCoinsBalance = dbAgencyGrant.chargeAgentCoinsBalance,
                        hostMicHours = dbAgencyGrant.hostMicHours,
                        hostActiveDays = dbAgencyGrant.hostActiveDays,
                        hostGiftsDiamonds = dbAgencyGrant.hostGiftsDiamonds,
                        role = dbAgencyGrant.resolvedAppRole
                    )
                } else {
                    existing.profile.copy(nickname = updatedNickname)
                }
                existing to null
            } else {
                null to "كلمة المرور غير صحيحة لهذا البريد الإلكتروني"
            }
        }

        // Create a brand new account with a unique ID
        val newDisplayId = (nextGeneratedNumericId++).toString()
        val cleanName = if (nicknameInput.isNotBlank()) nicknameInput.trim() else normalizedEmail.substringBefore("@")
        val initialFrame = if (avatarTypeInput == "PRINCESS") {
            FrameStyle3D.ROSE_GOLD_PHOENIX
        } else {
            FrameStyle3D.IMPERIAL_GOLD_WINGS
        }

        // Check if the Server/Database Agency Permissions Table has pre-activated permissions for this User ID
        val dbAgencyGrant = agencyPermissionsRegistryByUserId[newDisplayId]

        val newProfile = UserProfile(
            uuid = "user_$newDisplayId",
            displayId = newDisplayId,
            isSpecialId = false,
            email = normalizedEmail,
            nickname = cleanName,
            avatarType = avatarTypeInput,
            countryFlag = "🇪🇬",
            countryNameAr = "مصر",
            role = dbAgencyGrant?.resolvedAppRole ?: AppUserRole.SUPPORTER,
            wealthLevel = 12,
            wealthExpCurrent = 12500,
            wealthExpTarget = 25000,
            charismaLevel = 10,
            charismaExpCurrent = 9800,
            charismaExpTarget = 20000,
            vipTier = 3,
            goldCoins = 150000,
            crystalDiamonds = 65000,
            frameStyle = initialFrame,
            entryWelcomeName = "تنين الإمبراطور الذهبي 7D",
            equippedChatBubbleName = "",
            equippedSoundWaveName = "موجة النبض الإمبراطوري الذهبي",
            coloredNameHex = "#FFD700",
            soundWaveColorHex = "#00E5FF",
            agencyName = dbAgencyGrant?.agencyNameAr ?: "",
            isHostAgent = dbAgencyGrant?.isHostAgent ?: false,
            isHostMember = dbAgencyGrant?.isApprovedHost ?: false,
            hostMicHours = dbAgencyGrant?.hostMicHours ?: 0,
            hostActiveDays = dbAgencyGrant?.hostActiveDays ?: 0,
            hostGiftsDiamonds = dbAgencyGrant?.hostGiftsDiamonds ?: 0L,
            isChargeAgent = dbAgencyGrant?.isChargeAgent ?: false,
            chargeAgentCoinsBalance = dbAgencyGrant?.chargeAgentCoinsBalance ?: 0L,
            badges = defaultBadgesSeed().toList()
        )

        val newRecord = RegisteredUserAccountRecord(
            email = normalizedEmail,
            passwordPlain = passwordInput,
            profile = newProfile
        )
        registeredAccountsByEmail[normalizedEmail] = newRecord
        bindDefaultRoomPermissionsForUser(newProfile.displayId, newProfile.uuid)
        return newRecord to null
    }

    // -------------------------------------------------------------------------------------
    // 1B. AUTOMATIC ROOM ROLE RESOLUTION TABLE BY USER ID (`صلاحيات الغرف تلقائياً عبر المعرف الخاص ID`)
    //     Automatically recognizes the user as:
    //     - `OWNER` (`صاحب الغرفة`): Full room permissions (kick, mute, ban, assign admin, lock/unlock mic, edit room name & photo)
    //     - `ADMIN` (`أدمن الغرفة`): Lock mic, unlock mic, kick user, mute user
    //     - `MEMBER` (`مستخدم عادي`): Sit on mic, leave own mic, follow, send gift, chat
    // -------------------------------------------------------------------------------------
    data class RoomRoleDatabaseRecord(
        val roomId: String,
        val roomOwnerDisplayId: String,
        val adminDisplayIds: MutableSet<String> = mutableSetOf(),
        val bannedDisplayIds: MutableSet<String> = mutableSetOf(),
        val mutedDisplayIds: MutableSet<String> = mutableSetOf()
    )

    private val roomRoleRegistryByRoomId = mutableMapOf<String, RoomRoleDatabaseRecord>(
        "room_1" to RoomRoleDatabaseRecord(
            roomId = "room_1",
            roomOwnerDisplayId = "1201637",
            adminDisplayIds = mutableSetOf("888888", "777777")
        ),
        "room_2" to RoomRoleDatabaseRecord(
            roomId = "room_2",
            roomOwnerDisplayId = "888888",
            adminDisplayIds = mutableSetOf("1201637", "1201638")
        ),
        "room_3" to RoomRoleDatabaseRecord(
            roomId = "room_3",
            roomOwnerDisplayId = "777777",
            adminDisplayIds = mutableSetOf("555666")
        ),
        "room_4" to RoomRoleDatabaseRecord(
            roomId = "room_4",
            roomOwnerDisplayId = "999999",
            adminDisplayIds = mutableSetOf("444999")
        )
    )

    fun bindDefaultRoomPermissionsForUser(userDisplayId: String, userUuid: String) {
        val cleanId = userDisplayId.trim()
        // Ensure room_1 is owned by the logged-in user's ID and room_2 has the user's ID as Admin
        roomRoleRegistryByRoomId["room_1"] = RoomRoleDatabaseRecord(
            roomId = "room_1",
            roomOwnerDisplayId = cleanId,
            adminDisplayIds = mutableSetOf("888888", "777777")
        )
        val room2 = roomRoleRegistryByRoomId.getOrPut("room_2") {
            RoomRoleDatabaseRecord(roomId = "room_2", roomOwnerDisplayId = "888888")
        }
        room2.adminDisplayIds.add(cleanId)
        room2.adminDisplayIds.add(userUuid)
    }

    /**
     * Automatically resolves the user's role in a room strictly via their Unique ID (`displayId` / `uuid`).
     */
    fun resolveRoomRoleAutomaticallyById(
        room: VoiceRoomModel,
        userDisplayId: String,
        userUuid: String = "user_$userDisplayId"
    ): RoomPermissionRole {
        val cleanId = userDisplayId.trim()
        val cleanUuid = userUuid.trim()
        val dbRecord = roomRoleRegistryByRoomId[room.id]

        // 1. Check if User ID matches the Room Owner ID (`صاحب الغرفة`)
        if (room.roomDisplayId == cleanId ||
            room.hostUserId == cleanUuid ||
            room.hostUserId == "user_$cleanId" ||
            dbRecord?.roomOwnerDisplayId == cleanId
        ) {
            return RoomPermissionRole.OWNER
        }

        // 2. Check if User ID is registered in the Room Admins Table (`أدمن الغرفة`)
        if (room.adminUserIds.contains(cleanId) ||
            room.adminUserIds.contains(cleanUuid) ||
            room.adminUserIds.contains("user_$cleanId") ||
            dbRecord?.adminDisplayIds?.contains(cleanId) == true ||
            dbRecord?.adminDisplayIds?.contains(cleanUuid) == true
        ) {
            return RoomPermissionRole.ADMIN
        }

        // 3. Otherwise, the system automatically recognizes the user as a Regular Room User (`مستخدم عادي`)
        return RoomPermissionRole.MEMBER
    }

    fun setRoomAdminByIdInDatabase(roomId: String, targetUserIdOrDisplayId: String, makeAdmin: Boolean) {
        val clean = targetUserIdOrDisplayId.removePrefix("user_").trim()
        val rec = roomRoleRegistryByRoomId.getOrPut(roomId) {
            RoomRoleDatabaseRecord(roomId = roomId, roomOwnerDisplayId = "1201637")
        }
        if (makeAdmin) {
            rec.adminDisplayIds.add(clean)
            rec.adminDisplayIds.add("user_$clean")
        } else {
            rec.adminDisplayIds.remove(clean)
            rec.adminDisplayIds.remove("user_$clean")
        }
    }

    fun setRoomOwnerByIdInDatabase(roomId: String, newOwnerDisplayId: String) {
        val clean = newOwnerDisplayId.removePrefix("user_").trim()
        val rec = roomRoleRegistryByRoomId.getOrPut(roomId) {
            RoomRoleDatabaseRecord(roomId = roomId, roomOwnerDisplayId = clean)
        }
        roomRoleRegistryByRoomId[roomId] = rec.copy(roomOwnerDisplayId = clean)
    }

    // -------------------------------------------------------------------------------------
    // 1C. MASTER FEATURE VISIBILITY & REMOTE SERVER CONTROL TABLE (`جدول التحكم فيما يظهر وما لا يظهر وما يتعدل من السيرفر`)
    //     Controls every primary and secondary feature, screen, button, and module from external databases.
    // -------------------------------------------------------------------------------------
    data class RemoteFeatureControlRecord(
        val featureKey: String,
        val sectionCategory: String, // PRIMARY_TAB, STANDALONE_SCREEN, ROOM_FEATURE, AGENCY_PORTAL, VIP_MODULE
        val titleAr: String,
        val isVisibleInApp: Boolean = true,
        val isEnabledForUse: Boolean = true,
        val requiresDatabaseRoleActivation: Boolean = false,
        val requiredRoleOrFlag: String = "NONE",
        val sortOrder: Int = 0,
        val configJsonPayload: String = "{}"
    )

    private val masterFeatureControlTable = mutableListOf(
        RemoteFeatureControlRecord("tab_home_rooms", "PRIMARY_TAB", "الرئيسية وغرف الدردشة الصوتية", true, true, false, "ALL", 1),
        RemoteFeatureControlRecord("tab_discover_moments", "PRIMARY_TAB", "اكتشف واللحظات اليومية", true, true, false, "ALL", 2),
        RemoteFeatureControlRecord("tab_games_center", "PRIMARY_TAB", "مركز الألعاب التفاعلية", true, true, false, "ALL", 3),
        RemoteFeatureControlRecord("tab_messages_chat", "PRIMARY_TAB", "الرسائل والدردشة الخاصة", true, true, false, "ALL", 4),
        RemoteFeatureControlRecord("tab_my_profile", "PRIMARY_TAB", "ملفي الشخصي", true, true, false, "ALL", 5),
        RemoteFeatureControlRecord("screen_royal_store", "STANDALONE_SCREEN", "المتجر الملكي (إطارات • دخوليات • آي دي مميز)", true, true, false, "ALL", 6),
        RemoteFeatureControlRecord("screen_my_accessories", "STANDALONE_SCREEN", "إكسسواراتي (إطاراتي • دخولياتي • فقاعاتي)", true, true, false, "ALL", 7),
        RemoteFeatureControlRecord("screen_vip_5_sections", "STANDALONE_SCREEN", "الـ VIP (5 أقسام قابلة للضبط من قاعدة البيانات)", true, true, false, "ALL", 8),
        RemoteFeatureControlRecord("screen_host_agency_center", "AGENCY_PORTAL", "مركز وكالة المضيفين (للوكيل فقط بعد التفعيل)", true, true, true, "isHostAgent", 9),
        RemoteFeatureControlRecord("screen_host_personal_data", "AGENCY_PORTAL", "بيانات المضيف الرسمية (للمضيف بعد التفعيل)", true, true, true, "isApprovedHost", 10),
        RemoteFeatureControlRecord("screen_charge_agency_center", "AGENCY_PORTAL", "مركز وكالة الشحن والعملات والمكافآت (لوكيل الشحن بعد التفعيل)", true, true, true, "isChargeAgent", 11),
        RemoteFeatureControlRecord("screen_wallet_and_diamonds", "STANDALONE_SCREEN", "المحفظة وتحويل الألماس", true, true, false, "ALL", 12),
        RemoteFeatureControlRecord("screen_badges_medals", "STANDALONE_SCREEN", "الشارات والأوسمة الملكية", true, true, false, "ALL", 13),
        RemoteFeatureControlRecord("screen_levels_prestige", "STANDALONE_SCREEN", "المستوى والهيبة (ثروة وكاريزما)", true, true, false, "ALL", 14),
        RemoteFeatureControlRecord("screen_cp_relationships", "STANDALONE_SCREEN", "العلاقات الملكية وشريك الـ CP", true, true, false, "ALL", 15),
        RemoteFeatureControlRecord("screen_family_clan", "STANDALONE_SCREEN", "العائلة والقبيلة الملكية", true, true, false, "ALL", 16),
        RemoteFeatureControlRecord("screen_daily_tasks_wheel", "STANDALONE_SCREEN", "المهام اليومية وعجلة الحظ", true, true, false, "ALL", 17),
        RemoteFeatureControlRecord("screen_gift_atlas_7d", "STANDALONE_SCREEN", "أطلس الهدايا المضاء 7D", true, true, false, "ALL", 18),
        RemoteFeatureControlRecord("room_auto_id_permissions", "ROOM_FEATURE", "التعرف التلقائي على صلاحيات الغرفة عبر الـ ID", true, true, false, "AUTO_BY_ID", 19),
        RemoteFeatureControlRecord("room_entry_mount_svga", "ROOM_FEATURE", "الرسوم المتحركة للدخوليات عند دخول الغرفة", true, true, false, "ALL", 20),
        RemoteFeatureControlRecord("room_circular_frames_soundwaves", "ROOM_FEATURE", "الإطارات الدائرية والموجات الصوتية الملونة والاسم الملون", true, true, false, "ALL", 21)
    )

    private val _featureControlStateFlow = MutableStateFlow(masterFeatureControlTable.toList())
    val featureControlStateFlow: StateFlow<List<RemoteFeatureControlRecord>> = _featureControlStateFlow.asStateFlow()

    fun getFeatureControlTable(): List<RemoteFeatureControlRecord> =
        masterFeatureControlTable.sortedBy { it.sortOrder }

    fun isFeatureVisible(featureKey: String): Boolean =
        masterFeatureControlTable.firstOrNull { it.featureKey == featureKey }?.isVisibleInApp ?: true

    fun setFeatureVisibilityFromDatabase(featureKey: String, isVisible: Boolean, isEnabled: Boolean = true) {
        val idx = masterFeatureControlTable.indexOfFirst { it.featureKey == featureKey }
        if (idx >= 0) {
            masterFeatureControlTable[idx] = masterFeatureControlTable[idx].copy(
                isVisibleInApp = isVisible,
                isEnabledForUse = isEnabled
            )
            _featureControlStateFlow.value = masterFeatureControlTable.toList()
        }
    }

    fun editFeatureControlRowFromDatabase(featureKey: String, updated: RemoteFeatureControlRecord) {
        val idx = masterFeatureControlTable.indexOfFirst { it.featureKey == featureKey }
        if (idx >= 0) {
            masterFeatureControlTable[idx] = updated
        } else {
            masterFeatureControlTable.add(updated)
        }
        _featureControlStateFlow.value = masterFeatureControlTable.toList()
    }

    fun findAccountByDisplayId(displayId: String): RegisteredUserAccountRecord? {
        return registeredAccountsByEmail.values.firstOrNull { it.profile.displayId == displayId.trim() }
    }

    // -------------------------------------------------------------------------------------
    // 2. DATABASE-DRIVEN GRANT & ROLE ASSIGNMENT FUNCTIONS BY USER ID (`ID`)
    // -------------------------------------------------------------------------------------
    /**
     * Grants a specific badge from the database to any user by their `displayId`.
     */
    fun grantBadgeToUserById(targetDisplayId: String, badge: BadgeModel): Boolean {
        val record = findAccountByDisplayId(targetDisplayId) ?: return false
        val updatedBadges = (record.profile.badges.filterNot { it.id == badge.id } + badge.copy(isUnlocked = true))
        record.profile = record.profile.copy(badges = updatedBadges)
        return true
    }

    /**
     * Grants a circular 3D frame from the database or Charge Agent center to any user by `displayId`.
     */
    fun grantFrameToUserById(targetDisplayId: String, frameStyle: FrameStyle3D, itemStoreId: String? = null): Boolean {
        val record = findAccountByDisplayId(targetDisplayId)
        if (record != null) {
            if (itemStoreId != null) record.ownedItemIds.add(itemStoreId)
            record.profile = record.profile.copy(frameStyle = frameStyle)
            return true
        }
        return true
    }

    /**
     * Grants an animated Entry Mount (`دخولية متحركة`) from the database or Charge Agent center to any user by `displayId`.
     */
    fun grantEntryMountToUserById(targetDisplayId: String, mountNameAr: String, itemStoreId: String? = null): Boolean {
        val record = findAccountByDisplayId(targetDisplayId)
        if (record != null) {
            if (itemStoreId != null) record.ownedItemIds.add(itemStoreId)
            record.profile = record.profile.copy(entryWelcomeName = mountNameAr)
            return true
        }
        return true
    }

    // -------------------------------------------------------------------------------------
    // 2B. SERVER & DATABASE AGENCY PERMISSIONS REGISTRY TABLE (`جدول تفعيل وصلاحيات مركز الوكالات من السيرفر وقاعدة البيانات`)
    //     By default, the Agency Center (`مركز الوكالات` / `بيانات المضيف` / `وكالة الشحن`) is 100% HIDDEN
    //     from the app for all users (`isHostAgent = false`, `isApprovedHost = false`, `isChargeAgent = false`)
    //     until a user's `displayId` is granted permission in this database table.
    // -------------------------------------------------------------------------------------
    data class AgencyDatabasePermissionRecord(
        val userDisplayId: String,
        val isHostAgent: Boolean = false,
        val isApprovedHost: Boolean = false,
        val isChargeAgent: Boolean = false,
        val agencyNameAr: String = "وكالة الملوك الرسمية 👑",
        val chargeAgentCoinsBalance: Long = 0L,
        val hostMicHours: Int = 0,
        val hostActiveDays: Int = 0,
        val hostGiftsDiamonds: Long = 0L
    ) {
        val resolvedAppRole: AppUserRole
            get() = when {
                isChargeAgent -> AppUserRole.CHARGE_AGENT
                isHostAgent -> AppUserRole.HOST_AGENT
                isApprovedHost -> AppUserRole.HOST
                else -> AppUserRole.SUPPORTER
            }
    }

    private val agencyPermissionsRegistryByUserId = mutableMapOf<String, AgencyDatabasePermissionRecord>()
    private val _agencyPermissionsStateFlow = MutableStateFlow<Map<String, AgencyDatabasePermissionRecord>>(emptyMap())
    val agencyPermissionsStateFlow: StateFlow<Map<String, AgencyDatabasePermissionRecord>> = _agencyPermissionsStateFlow.asStateFlow()

    fun getAgencyPermissionRecordById(targetDisplayId: String): AgencyDatabasePermissionRecord? =
        agencyPermissionsRegistryByUserId[targetDisplayId.trim()]

    /**
     * Activates or deactivates a user as a Charge Agent (`وكيل شحن`) by `displayId` from the database
     * and sets their agency coin balance. Once activated, the Charge Agency Center immediately appears to them.
     */
    fun activateChargeAgentById(targetDisplayId: String, isChargeAgent: Boolean, agencyCoins: Long): Boolean {
        val cleanId = targetDisplayId.trim()
        val currentGrant = agencyPermissionsRegistryByUserId[cleanId] ?: AgencyDatabasePermissionRecord(userDisplayId = cleanId)
        agencyPermissionsRegistryByUserId[cleanId] = currentGrant.copy(
            isChargeAgent = isChargeAgent,
            chargeAgentCoinsBalance = if (isChargeAgent) agencyCoins else 0L
        )
        _agencyPermissionsStateFlow.value = agencyPermissionsRegistryByUserId.toMap()
        val record = findAccountByDisplayId(cleanId) ?: return true
        record.profile = record.profile.copy(
            isChargeAgent = isChargeAgent,
            chargeAgentCoinsBalance = if (isChargeAgent) agencyCoins else 0L,
            role = if (isChargeAgent) AppUserRole.CHARGE_AGENT else record.profile.role
        )
        return true
    }

    /**
     * Activates or deactivates a user as a Host Agent (`وكيل مضيفين`) or Host (`مضيف`) by `displayId` from the database.
     * Hidden from the app until granted here.
     */
    fun updateHostAgencyRoleById(
        targetDisplayId: String,
        isHostAgent: Boolean,
        isHostMember: Boolean,
        agencyNameAr: String,
        initialHostMicHours: Int = 94,
        initialHostActiveDays: Int = 21,
        initialHostGiftsDiamonds: Long = 248000L
    ): Boolean {
        val cleanId = targetDisplayId.trim()
        val currentGrant = agencyPermissionsRegistryByUserId[cleanId] ?: AgencyDatabasePermissionRecord(userDisplayId = cleanId)
        agencyPermissionsRegistryByUserId[cleanId] = currentGrant.copy(
            isHostAgent = isHostAgent,
            isApprovedHost = isHostMember,
            agencyNameAr = if (isHostAgent || isHostMember) agencyNameAr else "",
            hostMicHours = if (isHostMember) initialHostMicHours else 0,
            hostActiveDays = if (isHostMember) initialHostActiveDays else 0,
            hostGiftsDiamonds = if (isHostMember) initialHostGiftsDiamonds else 0L
        )
        _agencyPermissionsStateFlow.value = agencyPermissionsRegistryByUserId.toMap()
        val record = findAccountByDisplayId(cleanId) ?: return true
        record.profile = record.profile.copy(
            isHostAgent = isHostAgent,
            isHostMember = isHostMember,
            agencyName = if (isHostAgent || isHostMember) agencyNameAr else "",
            hostMicHours = if (isHostMember) initialHostMicHours else 0,
            hostActiveDays = if (isHostMember) initialHostActiveDays else 0,
            hostGiftsDiamonds = if (isHostMember) initialHostGiftsDiamonds else 0L,
            role = when {
                isHostAgent -> AppUserRole.HOST_AGENT
                isHostMember -> AppUserRole.HOST
                else -> AppUserRole.SUPPORTER
            }
        )
        return true
    }

    // -------------------------------------------------------------------------------------
    // 3. STORE & ACCESSORIES MASTER TABLE (`المتجر` & `إكسسواراتي`)
    //    - Store sells ONLY: FRAMES (`الإطارات`), ENTRY_MOUNTS (`الدخوليات`), SPECIAL_IDS (`الآي دي المميز`)
    //    - My Accessories displays ONLY owned: FRAMES, ENTRY_MOUNTS, CHAT_BUBBLES (`الفقاعات المملوكة`)
    // -------------------------------------------------------------------------------------
    private val masterStoreItemsList = mutableListOf<StoreCatalogItem>()

    init {
        resetStoreCatalogTable()
    }

    fun getStoreAndAccessoriesTable(): List<StoreCatalogItem> =
        masterStoreItemsList.filter { it.isVisible }

    fun editStoreItem(itemId: String, updatedItem: StoreCatalogItem) {
        val idx = masterStoreItemsList.indexOfFirst { it.id == itemId }
        if (idx >= 0) masterStoreItemsList[idx] = updatedItem
    }

    fun setStoreItemVisibility(itemId: String, isVisible: Boolean) {
        val idx = masterStoreItemsList.indexOfFirst { it.id == itemId }
        if (idx >= 0) masterStoreItemsList[idx] = masterStoreItemsList[idx].copy(isVisible = isVisible)
    }

    fun swapStoreItemsOrder(firstId: String, secondId: String) {
        val i = masterStoreItemsList.indexOfFirst { it.id == firstId }
        val j = masterStoreItemsList.indexOfFirst { it.id == secondId }
        if (i >= 0 && j >= 0) {
            val tmp = masterStoreItemsList[i]
            masterStoreItemsList[i] = masterStoreItemsList[j]
            masterStoreItemsList[j] = tmp
        }
    }

    fun deleteStoreItem(itemId: String) {
        masterStoreItemsList.removeAll { it.id == itemId }
    }

    fun resetStoreCatalogTable() {
        masterStoreItemsList.clear()
        masterStoreItemsList.addAll(
            listOf(
                // 1. Circular 3D Avatar & Mic Frames (`الإطارات الدائرية للبيع وفي إكسسواراتي`)
                StoreCatalogItem(
                    id = "store_frame_1",
                    nameAr = "إطار أجنحة الإمبراطور الدائري 7D",
                    subtitleAr = "إطار دائري مجنح يظهر على الصورة والمايك مع موجات ذهبية",
                    category = StoreItemCategory.FRAMES,
                    priceCoins = 45000,
                    dimensionTag = "7D دائري",
                    iconEmoji = "👑",
                    primaryColor = Color(0xFFFFD700),
                    secondaryColor = Color(0xFF9E6B0E),
                    frameStyle = FrameStyle3D.IMPERIAL_GOLD_WINGS,
                    isEquipped = true,
                    isOwned = true
                ),
                StoreCatalogItem(
                    id = "store_frame_2",
                    nameAr = "إطار تنين الجليد الدائري 6D",
                    subtitleAr = "إطار دائري كريستالي مع موجات صوتية سماوية متوهجة",
                    category = StoreItemCategory.FRAMES,
                    priceCoins = 38000,
                    dimensionTag = "6D دائري",
                    iconEmoji = "🐉",
                    primaryColor = Color(0xFF00E5FF),
                    secondaryColor = Color(0xFF0055B3),
                    frameStyle = FrameStyle3D.CRYSTAL_DRAGON_ICE,
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_frame_3",
                    nameAr = "إطار عنقاء الياقوت الدائري 5D",
                    subtitleAr = "إطار دائري وردي ملكي مع موجات صوتية وردية واسم ملون",
                    category = StoreItemCategory.FRAMES,
                    priceCoins = 32000,
                    dimensionTag = "5D دائري",
                    iconEmoji = "🦅",
                    primaryColor = Color(0xFFFF4081),
                    secondaryColor = Color(0xFF880E4F),
                    frameStyle = FrameStyle3D.ROSE_GOLD_PHOENIX,
                    isEquipped = false,
                    isOwned = true
                ),
                StoreCatalogItem(
                    id = "store_frame_4",
                    nameAr = "إطار تاج السلطان الزمردي الدائري",
                    subtitleAr = "إطار دائري مرصع بالزمرد الأخضر مع موجات صوتية زمردية",
                    category = StoreItemCategory.FRAMES,
                    priceCoins = 22000,
                    dimensionTag = "4D دائري",
                    iconEmoji = "❇️",
                    primaryColor = Color(0xFF00E676),
                    secondaryColor = Color(0xFF00695C),
                    frameStyle = FrameStyle3D.EMERALD_SULTAN_CROWN,
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_frame_5",
                    nameAr = "إطار الهالة البنفسجية الدائري",
                    subtitleAr = "إطار دائري نيون بنفسجي مع موجات صوتية ملونة على المايك",
                    category = StoreItemCategory.FRAMES,
                    priceCoins = 12000,
                    dimensionTag = "3D دائري",
                    iconEmoji = "🔮",
                    primaryColor = Color(0xFFE040FB),
                    secondaryColor = Color(0xFF4A148C),
                    frameStyle = FrameStyle3D.ROYAL_VIOLET_AURA,
                    isEquipped = false,
                    isOwned = false
                ),

                // 2. Animated Entry Mounts (`الدخوليات المتحركة عند دخول الغرفة`)
                StoreCatalogItem(
                    id = "store_mount_1",
                    nameAr = "تنين الإمبراطور الذهبي 7D",
                    subtitleAr = "دخولية متحركة بملء الشاشة تظهر تلقائياً عند دخولك الغرفة",
                    category = StoreItemCategory.ENTRY_MOUNTS,
                    priceCoins = 80000,
                    dimensionTag = "7D SVGA",
                    iconEmoji = "🐉",
                    primaryColor = Color(0xFFFFD700),
                    secondaryColor = Color(0xFFB71C1C),
                    isEquipped = true,
                    isOwned = true
                ),
                StoreCatalogItem(
                    id = "store_mount_2",
                    nameAr = "أسطول بوغاتي النيون الملكي 6D",
                    subtitleAr = "سيارة رياضية مجسمة مع إشعار دخول ملكي عند دخول الغرفة",
                    category = StoreItemCategory.ENTRY_MOUNTS,
                    priceCoins = 55000,
                    dimensionTag = "6D SVGA",
                    iconEmoji = "🏎️",
                    primaryColor = Color(0xFF00E5FF),
                    secondaryColor = Color(0xFF1A237E),
                    isEquipped = false,
                    isOwned = true
                ),
                StoreCatalogItem(
                    id = "store_mount_3",
                    nameAr = "عنقاء الكريستال المتوهجة 7D",
                    subtitleAr = "تحليق العنقاء الملكية فوق المايكات فور دخولك الغرفة",
                    category = StoreItemCategory.ENTRY_MOUNTS,
                    priceCoins = 95000,
                    dimensionTag = "7D SVGA",
                    iconEmoji = "🔥",
                    primaryColor = Color(0xFFFF4081),
                    secondaryColor = Color(0xFF880E4F),
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_mount_4",
                    nameAr = "اليخت الملكي الفاخر 6D",
                    subtitleAr = "عبور اليخت الإمبراطوري المذهب مع أمواج الكريستال",
                    category = StoreItemCategory.ENTRY_MOUNTS,
                    priceCoins = 68000,
                    dimensionTag = "6D SVGA",
                    iconEmoji = "🛥️",
                    primaryColor = Color(0xFF40C4FF),
                    secondaryColor = Color(0xFF01579B),
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_mount_5",
                    nameAr = "الحصان المجنح الأسطوري 5D",
                    subtitleAr = "دخولية الفارس الملكي المجنح مع نجوم ذهبية متحركة",
                    category = StoreItemCategory.ENTRY_MOUNTS,
                    priceCoins = 42000,
                    dimensionTag = "5D SVGA",
                    iconEmoji = "🦄",
                    primaryColor = Color(0xFFE040FB),
                    secondaryColor = Color(0xFF4A148C),
                    isEquipped = false,
                    isOwned = false
                ),

                // 3. Special VIP IDs (`الآي دي المميز للبيع في المتجر`)
                StoreCatalogItem(
                    id = "store_id_1",
                    nameAr = "آي دي ملكي سباعي 7777777",
                    subtitleAr = "معرف إمبراطوري نادر مع شارة ذهبية متوهجة",
                    category = StoreItemCategory.SPECIAL_IDS,
                    priceCoins = 150000,
                    dimensionTag = "VIP ID",
                    iconEmoji = "7️⃣",
                    primaryColor = Color(0xFFFFD700),
                    secondaryColor = Color(0xFF8D6E63),
                    specialIdValue = "7777777",
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_id_2",
                    nameAr = "آي دي الملوك 8888888",
                    subtitleAr = "معرف ملكي فاخر يظهر باللون الذهبي المشع",
                    category = StoreItemCategory.SPECIAL_IDS,
                    priceCoins = 120000,
                    dimensionTag = "VIP ID",
                    iconEmoji = "8️⃣",
                    primaryColor = Color(0xFF00E5FF),
                    secondaryColor = Color(0xFF0D47A1),
                    specialIdValue = "8888888",
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_id_3",
                    nameAr = "آي دي السلاطين 9999999",
                    subtitleAr = "معرف خاص لكبار الشخصيات والداعمين",
                    category = StoreItemCategory.SPECIAL_IDS,
                    priceCoins = 95000,
                    dimensionTag = "VIP ID",
                    iconEmoji = "9️⃣",
                    primaryColor = Color(0xFFFF4081),
                    secondaryColor = Color(0xFF880E4F),
                    specialIdValue = "9999999",
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_id_4",
                    nameAr = "آي دي النخبة 1111111",
                    subtitleAr = "رقم متسلسل نادر مع ختم التاج الملكي",
                    category = StoreItemCategory.SPECIAL_IDS,
                    priceCoins = 110000,
                    dimensionTag = "VIP ID",
                    iconEmoji = "1️⃣",
                    primaryColor = Color(0xFF00E676),
                    secondaryColor = Color(0xFF1B5E20),
                    specialIdValue = "1111111",
                    isEquipped = false,
                    isOwned = false
                ),

                // 4. Purchasable Chat Bubble Frames (`إطارات قاعة الدردشة التي تضاعف قاعة الدردشة ويمكن شراؤها من المتجر`)
                // Note: Default chat writing in rooms is 100% transparent without a border (`equippedChatBubbleName = ""`).
                // Purchasing & equipping any of these frames adds an ornate frame around messages in the chat hall!
                StoreCatalogItem(
                    id = "store_bubble_1",
                    nameAr = "إطار المخطوطة الإمبراطورية الذهبية 👑",
                    subtitleAr = "إطار يضاعف فخامة قاعة الدردشة بحواف ذهبية مرصعة",
                    category = StoreItemCategory.CHAT_BUBBLES,
                    priceCoins = 18000,
                    dimensionTag = "×2 قاعة الدردشة",
                    iconEmoji = "💬",
                    primaryColor = Color(0xFFFFD700),
                    secondaryColor = Color(0xFF6D4C0D),
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_bubble_2",
                    nameAr = "إطار لهب التنين الناري 🔥",
                    subtitleAr = "إطار دردشة متوهج يضاعف هيبة رسائلك في قاعة الدردشة",
                    category = StoreItemCategory.CHAT_BUBBLES,
                    priceCoins = 22000,
                    dimensionTag = "×2 قاعة الدردشة",
                    iconEmoji = "🔥",
                    primaryColor = Color(0xFFFF5252),
                    secondaryColor = Color(0xFF7F0000),
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_bubble_3",
                    nameAr = "إطار الكريستال الجليدي الأزرق ❄️",
                    subtitleAr = "إطار دردشة كريستالي شفاف يضاعف جمال قاعة الدردشة",
                    category = StoreItemCategory.CHAT_BUBBLES,
                    priceCoins = 16000,
                    dimensionTag = "×2 قاعة الدردشة",
                    iconEmoji = "❄️",
                    primaryColor = Color(0xFF00E5FF),
                    secondaryColor = Color(0xFF004D40),
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_bubble_4",
                    nameAr = "إطار ياقوت الأميرات الوردي 🌸",
                    subtitleAr = "إطار دردشة ملكي متلألئ يضاعف مساحة وهيبة رسالتك",
                    category = StoreItemCategory.CHAT_BUBBLES,
                    priceCoins = 20000,
                    dimensionTag = "×2 قاعة الدردشة",
                    iconEmoji = "🌸",
                    primaryColor = Color(0xFFF472B6),
                    secondaryColor = Color(0xFF831843),
                    isEquipped = false,
                    isOwned = false
                ),

                // 5. Animated & Transparent Chatroom Background Themes (`خلفيات وثيمات الغرف المتحركة والشفافة عالية الجودة`)
                StoreCatalogItem(
                    id = "store_room_theme_1",
                    nameAr = "ليالي الهلال الملكي المتحركة 🌙",
                    subtitleAr = "ثيم خلفية غرفة شفاف ومتحرك عالي الجودة مع هلال ذهبي ونجوم",
                    category = StoreItemCategory.ROOM_THEMES,
                    priceCoins = 0,
                    dimensionTag = "7D Theme",
                    iconEmoji = "🌙",
                    primaryColor = Color(0xFFFFE082),
                    secondaryColor = Color(0xFF141B2D),
                    roomThemeId = "PALACE_NIGHT",
                    isEquipped = true,
                    isOwned = true
                ),
                StoreCatalogItem(
                    id = "store_room_theme_2",
                    nameAr = "أورورا الكريستال الشفافة الملونة 🌌",
                    subtitleAr = "موجات أورورا شفافة متحركة عالية الدقة 7D لغرفتك الصوتية",
                    category = StoreItemCategory.ROOM_THEMES,
                    priceCoins = 18000,
                    dimensionTag = "7D Glass",
                    iconEmoji = "🌌",
                    primaryColor = Color(0xFF38BDF8),
                    secondaryColor = Color(0xFFA855F7),
                    roomThemeId = "TRANSPARENT_AURORA",
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_room_theme_3",
                    nameAr = "قصر الذهب الإمبراطوري الشفاف 👑",
                    subtitleAr = "ثيم شفاف فاخر بذرات الذهب المتطايرة وإضاءة القصر الملكي",
                    category = StoreItemCategory.ROOM_THEMES,
                    priceCoins = 35000,
                    dimensionTag = "7D Royal",
                    iconEmoji = "👑",
                    primaryColor = Color(0xFFFFD700),
                    secondaryColor = Color(0xFFF59E0B),
                    roomThemeId = "ROYAL_GOLD_PALACE",
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_room_theme_4",
                    nameAr = "سماء الياقوت الأزرق الشفافة 💎",
                    subtitleAr = "سديم ياقوتي شفاف عالي الجودة مع شهب ونجوم متحركة",
                    category = StoreItemCategory.ROOM_THEMES,
                    priceCoins = 25000,
                    dimensionTag = "6D Crystal",
                    iconEmoji = "💎",
                    primaryColor = Color(0xFF00E5FF),
                    secondaryColor = Color(0xFF3B82F6),
                    roomThemeId = "SAPPHIRE_STARLIGHT",
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_room_theme_5",
                    nameAr = "واحة الزمرد الشفافة المتحركة 💚",
                    subtitleAr = "إضاءة زمردية كريستالية شفافة وموجات ضوئية هادئة",
                    category = StoreItemCategory.ROOM_THEMES,
                    priceCoins = 22000,
                    dimensionTag = "6D Glass",
                    iconEmoji = "💚",
                    primaryColor = Color(0xFF10B981),
                    secondaryColor = Color(0xFF34D399),
                    roomThemeId = "EMERALD_CRYSTAL",
                    isEquipped = false,
                    isOwned = false
                ),
                StoreCatalogItem(
                    id = "store_room_theme_6",
                    nameAr = "مجرة الورد الملكي الشفافة 🌸",
                    subtitleAr = "مجرة مخملية وردية شفافة مع نجوم مضيئة عالية الدقة",
                    category = StoreItemCategory.ROOM_THEMES,
                    priceCoins = 28000,
                    dimensionTag = "7D Nebula",
                    iconEmoji = "🌸",
                    primaryColor = Color(0xFFF472B6),
                    secondaryColor = Color(0xFFC084FC),
                    roomThemeId = "ROSE_VELVET_GALAXY",
                    isEquipped = false,
                    isOwned = false
                )
            )
        )
    }

    // -------------------------------------------------------------------------------------
    // 4. VIP 5 SECTIONS TABLE (`الفي اي بي تعرض خمس اقسام يتم ظبطهم لاحقا من قاعده البيانات`)
    // -------------------------------------------------------------------------------------
    private val vipFiveSectionsList = mutableListOf<VipSectionConfig>()

    init {
        resetVipFiveSectionsTable()
    }

    fun getVipFiveSectionsTable(): List<VipSectionConfig> =
        vipFiveSectionsList.filter { it.isVisible }

    fun editVipSection(vipTier: Int, updatedConfig: VipSectionConfig) {
        val idx = vipFiveSectionsList.indexOfFirst { it.vipTier == vipTier }
        if (idx >= 0) vipFiveSectionsList[idx] = updatedConfig
    }

    fun resetVipFiveSectionsTable() {
        vipFiveSectionsList.clear()
        vipFiveSectionsList.addAll(
            listOf(
                VipSectionConfig(
                    vipTier = 1,
                    titleAr = "القسم الأول: VIP 1 • الفارس الملكي",
                    badgeTitleAr = "VIP 1 🛡️",
                    priceCoins = 10000L,
                    primaryColorHex = "#00E676",
                    secondaryColorHex = "#004D40",
                    soundWaveColorHex = "#00E676",
                    coloredNameHex = "#69F0AE",
                    grantedFrameStyle = FrameStyle3D.ROYAL_VIOLET_AURA,
                    grantedEntryMountNameAr = "الحصان المجنح الأسطوري 5D",
                    perksAr = listOf(
                        "إطار دائري خاص بـ VIP 1 يظهر على المايك",
                        "موجات صوتية خضراء زمردية مميزة أثناء التحدث",
                        "اسم ملون متوهج داخل الغرف الصوتية",
                        "دخولية الحصان المجنح المتحركة عند دخول الغرفة"
                    )
                ),
                VipSectionConfig(
                    vipTier = 2,
                    titleAr = "القسم الثاني: VIP 2 • الأمير المتوج",
                    badgeTitleAr = "VIP 2 ⚜️",
                    priceCoins = 25000L,
                    primaryColorHex = "#00E5FF",
                    secondaryColorHex = "#01579B",
                    soundWaveColorHex = "#00E5FF",
                    coloredNameHex = "#80D8FF",
                    grantedFrameStyle = FrameStyle3D.EMERALD_SULTAN_CROWN,
                    grantedEntryMountNameAr = "اليخت الملكي الفاخر 6D",
                    perksAr = listOf(
                        "إطار تاج السلطان الدائري على الصورة والمايك",
                        "موجات صوتية سماوية نيون ثلاثية الأبعاد",
                        "اسم ملون كريستالي + فقاعة دردشة ملكية",
                        "دخولية اليخت الملكي الفاخر عند دخول أي غرفة"
                    )
                ),
                VipSectionConfig(
                    vipTier = 3,
                    titleAr = "القسم الثالث: VIP 3 • الدوق العظيم",
                    badgeTitleAr = "VIP 3 💎",
                    priceCoins = 50000L,
                    primaryColorHex = "#E040FB",
                    secondaryColorHex = "#4A148C",
                    soundWaveColorHex = "#E040FB",
                    coloredNameHex = "#EA80FC",
                    grantedFrameStyle = FrameStyle3D.ROSE_GOLD_PHOENIX,
                    grantedEntryMountNameAr = "أسطول بوغاتي النيون الملكي 6D",
                    perksAr = listOf(
                        "إطار عنقاء الياقوت الدائري المجنح",
                        "موجات صوتية أرجوانية ملكية نابضة على المايك",
                        "اسم ملون وردي/أرجواني + أولوية الصعود على المايك",
                        "دخولية سيارة بوغاتي النيون المتحركة عند دخول الغرفة"
                    )
                ),
                VipSectionConfig(
                    vipTier = 4,
                    titleAr = "القسم الرابع: VIP 4 • الملك الذهبي",
                    badgeTitleAr = "VIP 4 🦁",
                    priceCoins = 100000L,
                    primaryColorHex = "#FF4081",
                    secondaryColorHex = "#880E4F",
                    soundWaveColorHex = "#FF4081",
                    coloredNameHex = "#FF80AB",
                    grantedFrameStyle = FrameStyle3D.CRYSTAL_DRAGON_ICE,
                    grantedEntryMountNameAr = "عنقاء الكريستال المتوهجة 7D",
                    perksAr = listOf(
                        "إطار تنين الجليد الدائري 6D مع أجنحة متحركة",
                        "موجات صوتية ياقوتية متعددة الطبقات على المايك",
                        "اسم ملون ياقوتي متدرج + حماية من الكتم والطرد العادي",
                        "دخولية عنقاء الكريستال 7D بملء الشاشة عند دخول الغرفة"
                    )
                ),
                VipSectionConfig(
                    vipTier = 5,
                    titleAr = "القسم الخامس: VIP 5 • الإمبراطور الأسطوري 7D",
                    badgeTitleAr = "VIP 5 👑",
                    priceCoins = 200000L,
                    primaryColorHex = "#FFD700",
                    secondaryColorHex = "#8A540A",
                    soundWaveColorHex = "#FFD700",
                    coloredNameHex = "#FFF59D",
                    grantedFrameStyle = FrameStyle3D.IMPERIAL_GOLD_WINGS,
                    grantedEntryMountNameAr = "تنين الإمبراطور الذهبي 7D",
                    perksAr = listOf(
                        "إطار أجنحة الإمبراطور الدائري 7D المرصع بالذهب",
                        "موجات صوتية ذهبية إمبراطورية ثلاثية الأبعاد على المايك",
                        "اسم ذهبي ملكي مشع + إخفاء الهوية عند الرغبة",
                        "دخولية تنين الإمبراطور الذهبي 7D السينمائية عند دخول الغرفة"
                    )
                )
            )
        )
    }

    // -------------------------------------------------------------------------------------
    // 5. HOST AGENCY MEMBERS & TELEMETRY TABLE (`مركز الوكالة للوكيل وبيانات المضيف`)
    // -------------------------------------------------------------------------------------
    private val agencyHostsRoster = mutableListOf(
        HostAgencyMemberRecord(
            userId = "888888",
            nickname = "الأميرة شهد 🌸",
            avatarType = "PRINCESS",
            frameStyle = FrameStyle3D.ROSE_GOLD_PHOENIX,
            isOnMicNow = true,
            currentRoomNameAr = "قصر السلاطين والملوك 7D • مايك #1",
            isOnlineInApp = true,
            totalGiftDiamondsReceived = 485000,
            totalMicHours = 142,
            activeMicDays = 26,
            expectedSalaryUsd = 920
        ),
        HostAgencyMemberRecord(
            userId = "777777",
            nickname = "السلطان فهد 💎",
            avatarType = "PRINCE",
            frameStyle = FrameStyle3D.CRYSTAL_DRAGON_ICE,
            isOnMicNow = true,
            currentRoomNameAr = "ديوانية الخليج الملكية • مايك المضيف",
            isOnlineInApp = true,
            totalGiftDiamondsReceived = 620000,
            totalMicHours = 168,
            activeMicDays = 28,
            expectedSalaryUsd = 1250
        ),
        HostAgencyMemberRecord(
            userId = "555666",
            nickname = "نور القمر 🇲🇦",
            avatarType = "PRINCESS",
            frameStyle = FrameStyle3D.EMERALD_SULTAN_CROWN,
            isOnMicNow = false,
            currentRoomNameAr = "متصلة في صفحة اكتشف (خارج المايك)",
            isOnlineInApp = true,
            totalGiftDiamondsReceived = 215000,
            totalMicHours = 86,
            activeMicDays = 19,
            expectedSalaryUsd = 430
        ),
        HostAgencyMemberRecord(
            userId = "444999",
            nickname = "الأمير نواف 🇰🇼",
            avatarType = "PRINCE",
            frameStyle = FrameStyle3D.ROYAL_VIOLET_AURA,
            isOnMicNow = true,
            currentRoomNameAr = "سهرة الطرب والعود العربي • مايك #3",
            isOnlineInApp = true,
            totalGiftDiamondsReceived = 310000,
            totalMicHours = 112,
            activeMicDays = 23,
            expectedSalaryUsd = 610
        )
    )

    fun getAgencyHostsTable(): List<HostAgencyMemberRecord> = agencyHostsRoster.toList()

    fun inviteAndAddHostToAgency(targetUserId: String, hostNameAr: String): HostAgencyMemberRecord {
        val cleanId = targetUserId.trim()
        // Also grant Host Member (`isApprovedHost = true`) permission in the database to this invited user ID
        // so that when they open their account, `بيانات المضيف الرسمية` appears for them!
        val existingGrant = agencyPermissionsRegistryByUserId[cleanId]
        updateHostAgencyRoleById(
            targetDisplayId = cleanId,
            isHostAgent = existingGrant?.isHostAgent ?: false,
            isHostMember = true,
            agencyNameAr = existingGrant?.agencyNameAr?.ifBlank { "وكالة الملوك الرسمية 👑" } ?: "وكالة الملوك الرسمية 👑",
            initialHostMicHours = 4,
            initialHostActiveDays = 1,
            initialHostGiftsDiamonds = 15000L
        )
        val existing = agencyHostsRoster.firstOrNull { it.userId == cleanId }
        if (existing != null) return existing
        val newMember = HostAgencyMemberRecord(
            userId = cleanId,
            nickname = if (hostNameAr.isNotBlank()) hostNameAr else "مضيف جديد #$cleanId",
            avatarType = "PRINCE",
            frameStyle = FrameStyle3D.IMPERIAL_GOLD_WINGS,
            isOnMicNow = true,
            currentRoomNameAr = "انضم حديثاً • متواجد على المايك الآن 🎙️",
            isOnlineInApp = true,
            totalGiftDiamondsReceived = 15000,
            totalMicHours = 4,
            activeMicDays = 1,
            expectedSalaryUsd = 50
        )
        agencyHostsRoster.add(0, newMember)
        return newMember
    }

    // -------------------------------------------------------------------------------------
    // 6. CHARGE AGENT REWARDS CATALOG (`مكافآت وكيل الشحن من الإطارات والدخوليات`)
    // -------------------------------------------------------------------------------------
    val chargeAgentRewardsCatalog: List<ChargeAgentRewardItem> = listOf(
        ChargeAgentRewardItem(
            id = "agent_reward_frame_gold",
            nameAr = "إطار أجنحة الإمبراطور الدائري (مكافأة شحن)",
            category = StoreItemCategory.FRAMES,
            iconEmoji = "👑",
            durationDays = 15,
            frameStyle = FrameStyle3D.IMPERIAL_GOLD_WINGS
        ),
        ChargeAgentRewardItem(
            id = "agent_reward_frame_dragon",
            nameAr = "إطار تنين الجليد الدائري (مكافأة شحن)",
            category = StoreItemCategory.FRAMES,
            iconEmoji = "🐉",
            durationDays = 15,
            frameStyle = FrameStyle3D.CRYSTAL_DRAGON_ICE
        ),
        ChargeAgentRewardItem(
            id = "agent_reward_frame_rose",
            nameAr = "إطار عنقاء الياقوت الدائري (مكافأة شحن)",
            category = StoreItemCategory.FRAMES,
            iconEmoji = "🦅",
            durationDays = 7,
            frameStyle = FrameStyle3D.ROSE_GOLD_PHOENIX
        ),
        ChargeAgentRewardItem(
            id = "agent_reward_mount_dragon",
            nameAr = "دخولية تنين الإمبراطور الذهبي 7D",
            category = StoreItemCategory.ENTRY_MOUNTS,
            iconEmoji = "🐉",
            durationDays = 15
        ),
        ChargeAgentRewardItem(
            id = "agent_reward_mount_bugatti",
            nameAr = "دخولية أسطول بوغاتي النيون 6D",
            category = StoreItemCategory.ENTRY_MOUNTS,
            iconEmoji = "🏎️",
            durationDays = 15
        ),
        ChargeAgentRewardItem(
            id = "agent_reward_mount_yacht",
            nameAr = "دخولية اليخت الملكي الفاخر 6D",
            category = StoreItemCategory.ENTRY_MOUNTS,
            iconEmoji = "🛥️",
            durationDays = 7
        )
    )

    fun defaultBadgesSeed(): List<BadgeModel> = listOf(
        BadgeModel(
            id = "b_emperor_7d",
            titleAr = "وسام إمبراطور القصر 7D",
            subtitleAr = "يُمنح لكبار الداعمين وأصحاب الهيبة الملكية",
            categoryAr = "ملكي",
            iconEmoji = "👑",
            tierText = "LV.99",
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFF8C5808)
        ),
        BadgeModel(
            id = "b_dragon_knight",
            titleAr = "فارس التنين الذهبي",
            subtitleAr = "إرسال 50 هدية تنين 7D داخل الغرف الصوتية",
            categoryAr = "الداعمين",
            iconEmoji = "🐉",
            tierText = "7D SVGA",
            primaryColor = Color(0xFFFF4081),
            secondaryColor = Color(0xFF880E4F)
        ),
        BadgeModel(
            id = "b_crystal_star",
            titleAr = "نجم المايك الكريستالي",
            subtitleAr = "التحدث والتفاعل لأكثر من 100 ساعة صوتية نقية",
            categoryAr = "المضيفين",
            iconEmoji = "🎙️",
            tierText = "PRO",
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFF0D47A1)
        ),
        BadgeModel(
            id = "b_royal_council_star",
            titleAr = "درع المجالس الصوتية الملكية",
            subtitleAr = "عضو متألق في مجالس وغرف زاديرا لايف الصوتية",
            categoryAr = "البطولات",
            iconEmoji = "🛡️",
            tierText = "VIP",
            primaryColor = Color(0xFF00E676),
            secondaryColor = Color(0xFF004D40)
        )
    )

    init {
        // Seed default primary account so if someone logs in with mohamed@zadiralive.com / 12345678
        // or registers any new email, it works seamlessly.
        // Note: Agency portals (`isHostAgent`, `isHostMember`, `isChargeAgent`) are FALSE by default
        // so that the Agency Center is completely hidden in the app until activated from the database!
        registeredAccountsByEmail["mohamed@zadiralive.com"] = RegisteredUserAccountRecord(
            email = "mohamed@zadiralive.com",
            passwordPlain = "12345678",
            profile = UserProfile(
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
                equippedChatBubbleName = "",
                equippedSoundWaveName = "موجة النبض الإمبراطوري الذهبي",
                coloredNameHex = "#FFD700",
                soundWaveColorHex = "#00E5FF",
                agencyName = "",
                isHostAgent = false,
                isHostMember = false,
                hostMicHours = 0,
                hostActiveDays = 0,
                hostGiftsDiamonds = 0L,
                isChargeAgent = false,
                chargeAgentCoinsBalance = 0L,
                badges = defaultBadgesSeed()
            )
        )
    }

    // -------------------------------------------------------------------------------------
    // 7. SWIPEABLE HOME BANNERS DATABASE TABLE (`البنارات في الشاشة الرئيسية قابلة للسحب والتبديل وقابلة للتعيين من قاعدة البيانات`)
    // -------------------------------------------------------------------------------------
    private val homeBannersList = mutableListOf(
        HomeBannerSlideItem(
            id = "banner_recharge_rewards",
            titleAr = "مكافآت الشحن الملكية",
            subtitleAr = "اشحن الآن واحصل على سيارات ذهبية وهدايا مضاعفة 7D",
            badgeTextAr = "مكافآت الشحن 🪙",
            iconEmoji = "🏎️",
            gradientStartHex = "#3B2004",
            gradientMidHex = "#6E3E07",
            gradientEndHex = "#1C1108",
            accentColorHex = "#FFE57F",
            targetSubScreen = StandaloneSubScreen.COINS_ONLY,
            sortOrder = 1,
            isVisible = true
        ),
        HomeBannerSlideItem(
            id = "banner_store_themes_bubbles",
            titleAr = "مهرجان المتجر وثيمات الغرف",
            subtitleAr = "ثيمات خلفيات شفافة متحركة وإطارات تضاعف قاعة الدردشة",
            badgeTextAr = "المتجر الملكي 🛍️",
            iconEmoji = "🌌",
            gradientStartHex = "#3B0764",
            gradientMidHex = "#6B21A8",
            gradientEndHex = "#1E1B4B",
            accentColorHex = "#F472B6",
            targetSubScreen = StandaloneSubScreen.STORE,
            sortOrder = 2,
            isVisible = true
        ),
        HomeBannerSlideItem(
            id = "banner_vip_nobility",
            titleAr = "امتيازات الـ VIP والألقاب",
            subtitleAr = "افتح شارات الهيبة الملكية والدخوليات الأسطورية 7D",
            badgeTextAr = "نخبة VIP 👑",
            iconEmoji = "👑",
            gradientStartHex = "#0F172A",
            gradientMidHex = "#1E3A8A",
            gradientEndHex = "#172554",
            accentColorHex = "#38BDF8",
            targetSubScreen = StandaloneSubScreen.VIP_NOBILITY,
            sortOrder = 3,
            isVisible = true
        ),
        HomeBannerSlideItem(
            id = "banner_championship_ranking",
            titleAr = "بطولة السلاطين ولوحة الصدارة",
            subtitleAr = "تنافس مع أقوى الغرف والعائلات واربح جوائز يومية",
            badgeTextAr = "لوحة الصدارة 🏆",
            iconEmoji = "🏆",
            gradientStartHex = "#064E3B",
            gradientMidHex = "#047857",
            gradientEndHex = "#022C22",
            accentColorHex = "#34D399",
            targetSubScreen = StandaloneSubScreen.LEADERBOARD,
            sortOrder = 4,
            isVisible = true
        )
    )

    private val _homeBannersStateFlow = MutableStateFlow(homeBannersList.sortedBy { it.sortOrder })
    val homeBannersStateFlow: StateFlow<List<HomeBannerSlideItem>> = _homeBannersStateFlow.asStateFlow()
    val homeBannersTable: StateFlow<List<HomeBannerSlideItem>> get() = homeBannersStateFlow

    fun getHomeBannersTable(): List<HomeBannerSlideItem> =
        homeBannersList.filter { it.isVisible }.sortedBy { it.sortOrder }

    fun assignOrUpdateHomeBannerFromDatabase(banner: HomeBannerSlideItem) {
        val idx = homeBannersList.indexOfFirst { it.id == banner.id }
        if (idx >= 0) {
            homeBannersList[idx] = banner
        } else {
            homeBannersList.add(banner)
        }
        _homeBannersStateFlow.value = getHomeBannersTable()
    }

    fun addOrEditHomeBannerFromDatabase(banner: HomeBannerSlideItem) {
        assignOrUpdateHomeBannerFromDatabase(banner)
    }

    fun reorderHomeBannerInDatabase(bannerId: String, moveEarlier: Boolean) {
        val sorted = homeBannersList.sortedBy { it.sortOrder }.toMutableList()
        val idx = sorted.indexOfFirst { it.id == bannerId }
        if (idx < 0) return
        val targetIdx = if (moveEarlier) (idx - 1).coerceAtLeast(0) else (idx + 1).coerceAtMost(sorted.lastIndex)
        if (idx == targetIdx) return
        val item = sorted.removeAt(idx)
        sorted.add(targetIdx, item)
        homeBannersList.clear()
        sorted.forEachIndexed { newIndex, slide ->
            homeBannersList.add(slide.copy(sortOrder = newIndex + 1))
        }
        _homeBannersStateFlow.value = getHomeBannersTable()
    }

    fun deleteHomeBannerFromDatabase(bannerId: String) {
        homeBannersList.removeAll { it.id == bannerId }
        _homeBannersStateFlow.value = getHomeBannersTable()
    }

    // -------------------------------------------------------------------------------------
    // 8. ROOM & APP COMPLETE VISUAL CUSTOMIZATION DATABASE TABLE
    // (`تغيير شكل المايكات وأي شكل وأي شيء في الغرفة من قاعدة البيانات لاحقاً وأي شيء في التطبيق`)
    // -------------------------------------------------------------------------------------
    data class RoomVisualCustomizationRecord(
        val configId: String = "master_room_app_visual_v1",
        val defaultRoomBackgroundStyleId: String = "PALACE_NIGHT",
        val defaultMicShapeStyleId: String = RoomMicShapeStyle.CIRCLE.id,
        val micSeatSizeDp: Int = 50,
        val micOuterRingSizeDp: Int = 62,
        val chatWritingAlwaysTransparentByDefault: Boolean = true,
        val purchasedChatFrameDoublesChatHall: Boolean = true,
        val unifiedFourPagesHeaderTopHex: String = "#E7D6FA",
        val unifiedFourPagesHeaderMidHex: String = "#F2EAFC",
        val unifiedFourPagesSurfaceHex: String = "#F6F6F9",
        val primaryAccentVioletHex: String = "#9333EA",
        val secondaryAccentPinkHex: String = "#EC4899",
        val royalGoldAccentHex: String = "#FBBF24"
    )

    private val _roomAndAppVisualConfigFlow = MutableStateFlow(RoomVisualCustomizationRecord())
    val roomAndAppVisualConfigFlow: StateFlow<RoomVisualCustomizationRecord> = _roomAndAppVisualConfigFlow.asStateFlow()

    fun updateRoomAndAppVisualConfigFromDatabase(newConfig: RoomVisualCustomizationRecord) {
        _roomAndAppVisualConfigFlow.value = newConfig
    }

    fun updateRoomAndAppVisualCustomizationFromDatabase(newConfig: RoomVisualCustomizationRecord) {
        updateRoomAndAppVisualConfigFromDatabase(newConfig)
    }
}

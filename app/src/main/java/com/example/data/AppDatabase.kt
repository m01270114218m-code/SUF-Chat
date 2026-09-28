package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * 1. User Accounts & Persistent Local Identity Table (`user_account_table`)
 * Stores local user data, real account name, real avatar & cover URI, levels, XP, and balances for offline access.
 */
@Entity(tableName = "user_account_table")
data class UserAccountEntity(
    @PrimaryKey val uuid: String,
    val displayId: String,
    val isSpecialId: Boolean,
    val email: String,
    val passwordHash: String = "",
    val nickname: String,
    val bio: String = "👑 مرحباً بكم في عالمي الملكي • زاديرا لايف 7D ✨",
    val avatarType: String = "PRINCE",
    val customAvatarUri: String? = null,
    val customCoverUri: String? = null,
    val countryFlag: String,
    val countryNameAr: String,
    val roleCode: String,
    val wealthLevel: Int,
    val wealthExpCurrent: Long = 34500L,
    val wealthExpTarget: Long = 50000L,
    val charismaLevel: Int,
    val charismaExpCurrent: Long = 28200L,
    val charismaExpTarget: Long = 40000L,
    val vipTier: Int,
    val goldCoins: Long,
    val crystalDiamonds: Long,
    val equippedFrameId: String,
    val equippedWelcomeName: String,
    val equippedChatBubbleName: String = "",
    val coloredNameHex: String = "#FFD700",
    val soundWaveColorHex: String = "#00E5FF",
    val cpPartnerName: String = "الأميرة شهد 🌸",
    val cpPartnerId: String = "999888",
    val cpPoints: Long = 54000L,
    val familyNameAr: String = "قبيلة الأسود الملكية 🦁",
    val familyRankAr: String = "قائد القبيلة",
    val visitorsCount: Int = 18,
    val followersCount: Int = 124,
    val followingCount: Int = 14,
    val friendsCount: Int = 9,
    val appLanguageNameAr: String = "العربية",
    val checkInStreakDays: Int = 3,
    val isCheckedInToday: Boolean = false,
    val invitedFriendsCount: Int = 2,
    val isHostAgent: Boolean = false,
    val isApprovedHost: Boolean = false,
    val isChargeAgent: Boolean = false,
    val chargeAgentCoinsBalance: Long = 0L,
    val isBanned: Boolean = false
)

/**
 * 2. Local & Offline Room Settings Table (`room_settings_table`)
 * Stores each voice room's real name and real photo set by the room owner, plus room theme, mic shape, and counters.
 */
@Entity(tableName = "room_settings_table")
data class RoomSettingsEntity(
    @PrimaryKey val roomId: String,
    val roomDisplayId: String,
    val titleAr: String,
    val announcementAr: String,
    val categoryAr: String,
    val countryFlag: String,
    val hostUserId: String,
    val hostName: String,
    val customCoverImageUri: String? = null,
    val hostCustomAvatarUri: String? = null,
    val backgroundStyleId: String = "ROYAL_VELVET_GALAXY",
    val micShapeStyleId: String = "ROYAL_CIRCLE",
    val onlineCount: Int = 12,
    val heatScore: Long = 15000L,
    val coverBadgeText: String = "👑 7D",
    val isPkBattleActive: Boolean = false,
    val pkRedScore: Long = 0L,
    val pkBlueScore: Long = 0L,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * 3. Local & Offline Chat History Table (`chat_history_table`)
 * Stores both in-room chat messages (`chatScope = "ROOM"`) and private direct messages (`chatScope = "DIRECT"`)
 * locally for offline access and synchronization.
 */
@Entity(tableName = "chat_history_table")
data class ChatHistoryEntity(
    @PrimaryKey val messageId: String,
    val chatScope: String, // "ROOM" or "DIRECT"
    val roomId: String = "",
    val threadId: String = "",
    val senderUserId: String = "",
    val senderDisplayId: String = "",
    val senderName: String,
    val senderCustomAvatarUri: String? = null,
    val senderAvatarType: String = "PRINCE",
    val senderRoleCode: String = "USER",
    val senderRoomRoleCode: String = "MEMBER",
    val senderVip: Int = 1,
    val senderWealthLevel: Int = 1,
    val senderCharismaLevel: Int = 1,
    val senderChatBubbleFrame: String = "",
    val senderIsMe: Boolean = false,
    val messageText: String,
    val isGiftAnnouncement: Boolean = false,
    val isSystemWelcome: Boolean = false,
    val giftIconEmoji: String? = null,
    val coinsGiftAmount: Long? = null,
    val timestampText: String = "الآن",
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * 4. Database-Assigned Home Banners Table (`home_banners_table`)
 * Banners on the Home screen are strictly configured and assigned from the database (`تعيين البنارات يكون من قاعدة البيانات وليس من التطبيق`).
 */
@Entity(tableName = "home_banners_table")
data class HomeBannerEntity(
    @PrimaryKey val bannerId: String,
    val titleAr: String,
    val subtitleAr: String,
    val badgeTextAr: String,
    val iconEmoji: String,
    val customImageUri: String? = null,
    val actionTargetScreen: String = "WALLET",
    val primaryColorHex: String = "#9333EA",
    val secondaryColorHex: String = "#EC4899",
    val accentGoldHex: String = "#F59E0B",
    val sortOrder: Int = 1,
    val isActive: Boolean = true
)

/**
 * 5. Automatic Room Role & Permissions by User ID Table (`room_role_permissions_table`)
 * Controls automatic recognition of a user entering a voice room via their unique `displayId`:
 * - `OWNER` (`صاحب الغرفة`): Kick, Mute, Ban, Assign Admin, Lock/Unlock Mic, Edit Room Name & Photo
 * - `ADMIN` (`أدمن الغرفة`): Lock/Unlock Mic, Kick User, Mute User
 * - `MEMBER` (`مستخدم عادي`): Sit on Mic, Leave Own Mic, Follow, Send Gift, Chat
 */
@Entity(tableName = "room_role_permissions_table", primaryKeys = ["roomId", "userDisplayId"])
data class RoomRolePermissionEntity(
    val roomId: String,
    val userDisplayId: String,
    val roomOwnerDisplayId: String,
    val assignedRoomRole: String, // OWNER, ADMIN, MEMBER
    val canKickUser: Boolean = false,
    val canMuteUser: Boolean = false,
    val canBanUser: Boolean = false,
    val canAssignAdmin: Boolean = false,
    val canLockUnlockMic: Boolean = false,
    val canEditRoomNameAndPhoto: Boolean = false,
    val isBannedFromRoom: Boolean = false,
    val isMutedInRoom: Boolean = false,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * 6. Master Feature Visibility & Remote Server Control Table (`feature_visibility_control_table`)
 */
@Entity(tableName = "feature_visibility_control_table")
data class FeatureVisibilityControlEntity(
    @PrimaryKey val featureKey: String,
    val sectionCategory: String,
    val titleAr: String,
    val subtitleAr: String = "",
    val iconEmoji: String = "✨",
    val isVisibleInApp: Boolean = true,
    val isEnabledForUse: Boolean = true,
    val requiresDatabaseActivation: Boolean = false,
    val requiredDatabaseFlag: String = "NONE",
    val sortOrder: Int = 0,
    val remoteConfigJson: String = "{}"
)

/**
 * 7. Store & Owned Accessories Table (`store_catalog_table`)
 */
@Entity(tableName = "store_catalog_table")
data class StoreCatalogEntity(
    @PrimaryKey val itemId: String,
    val nameAr: String,
    val subtitleAr: String,
    val categoryCode: String,
    val priceCoins: Long,
    val durationDays: Int = 30,
    val dimensionTag: String = "7D SVGA",
    val iconEmoji: String = "👑",
    val primaryColorHex: String = "#FFD700",
    val secondaryColorHex: String = "#8C5808",
    val frameStyleCode: String? = null,
    val specialIdValue: String? = null,
    val isVisibleInStore: Boolean = true,
    val sortOrder: Int = 0
)

/**
 * 8. Five Configurable VIP Sections Table (`vip_five_sections_table`)
 */
@Entity(tableName = "vip_five_sections_table")
data class VipTierSectionEntity(
    @PrimaryKey val vipTier: Int,
    val titleAr: String,
    val badgeTitleAr: String,
    val priceCoins: Long,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val soundWaveColorHex: String,
    val coloredNameHex: String,
    val grantedFrameStyleCode: String,
    val grantedEntryMountNameAr: String,
    val perksSummaryAr: String,
    val isVisible: Boolean = true
)

/**
 * 9. Host Agencies & Host Members Telemetry Table (`host_agency_members_table`)
 */
@Entity(tableName = "host_agency_members_table")
data class HostMemberTelemetryEntity(
    @PrimaryKey val hostUserDisplayId: String,
    val agencyId: String,
    val agencyOwnerDisplayId: String,
    val hostNickname: String,
    val isOnMicNow: Boolean,
    val currentRoomNameAr: String,
    val isOnlineInApp: Boolean,
    val totalGiftDiamondsReceived: Long,
    val totalMicHours: Int,
    val activeMicDays: Int,
    val expectedSalaryUsd: Int
)

/**
 * 10. Charge Agencies & Coin/Reward Shipments Table (`charge_shipments_table`)
 */
@Entity(tableName = "charge_shipments_table")
data class ChargeShipmentEntity(
    @PrimaryKey val shipmentId: String,
    val chargeAgentDisplayId: String,
    val targetUserDisplayId: String,
    val coinsShipped: Long,
    val complimentaryRewardId: String? = null,
    val complimentaryRewardNameAr: String? = null,
    val timestampText: String
)

/**
 * 11. Direct Database Item & Badge Grants by User ID Table (`database_direct_grants_table`)
 */
@Entity(tableName = "database_direct_grants_table")
data class DirectDatabaseGrantEntity(
    @PrimaryKey val grantId: String,
    val targetUserDisplayId: String,
    val grantType: String,
    val itemCodeOrId: String,
    val itemTitleAr: String,
    val durationDays: Int = 30,
    val isActive: Boolean = true,
    val grantedAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * 12. Global UI Style & Theme Control Table (`ui_style_control_table`)
 */
@Entity(tableName = "ui_style_control_table")
data class UiStyleControlEntity(
    @PrimaryKey val configKey: String = "master_style",
    val themeName: String = "Oriental Gold & Dark Neon Glassmorphism 7D",
    val loginThemeStyle: String = "ORIENTAL_ISLAMIC_GOLD_4K",
    val voiceRoomThemeStyle: String = "DARK_NEON_GLASSMORPHISM_4K",
    val primaryBgHex: String = "#120629",
    val goldPrimaryHex: String = "#FFD700",
    val crystalBlueHex: String = "#00E5FF",
    val neonPurpleHex: String = "#E040FB",
    val neonPinkHex: String = "#FF4081",
    val buttonRadiusDp: Int = 24,
    val cardRadiusDp: Int = 18,
    val borderThicknessDp: Float = 2.2f,
    val enable7DAnimations: Boolean = true,
    val enable4KVectorAssets: Boolean = true
)

@Dao
interface ZadiraAppDao {
    // --- 1. Local User Account Queries ---
    @Query("SELECT * FROM user_account_table LIMIT 1")
    fun observeCurrentUser(): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_account_table LIMIT 1")
    suspend fun getFirstSavedUser(): UserAccountEntity?

    @Query("SELECT * FROM user_account_table WHERE displayId = :displayId LIMIT 1")
    suspend fun findUserByDisplayId(displayId: String): UserAccountEntity?

    @Query("SELECT * FROM user_account_table WHERE email = :email LIMIT 1")
    suspend fun findUserByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserAccount(user: UserAccountEntity)

    @Query("UPDATE user_account_table SET goldCoins = :coins, crystalDiamonds = :diamonds WHERE uuid = :uuid")
    suspend fun updateWalletBalance(uuid: String, coins: Long, diamonds: Long)

    @Query("UPDATE user_account_table SET displayId = :specialId, isSpecialId = 1 WHERE uuid = :uuid")
    suspend fun updateSpecialId(uuid: String, specialId: String)

    @Query("UPDATE user_account_table SET roleCode = :roleCode WHERE uuid = :uuid")
    suspend fun updateUserRole(uuid: String, roleCode: String)

    @Query("UPDATE user_account_table SET isChargeAgent = :isAgent, chargeAgentCoinsBalance = :coinsPool WHERE displayId = :displayId")
    suspend fun updateChargeAgentStatusById(displayId: String, isAgent: Boolean, coinsPool: Long)

    @Query("UPDATE user_account_table SET isHostAgent = :isHostAgent, isApprovedHost = :isApprovedHost WHERE displayId = :displayId")
    suspend fun updateHostAgencyStatusById(displayId: String, isHostAgent: Boolean, isApprovedHost: Boolean)

    @Query("UPDATE user_account_table SET wealthLevel = :wealthLv, charismaLevel = :charismaLv, vipTier = :vip WHERE uuid = :uuid")
    suspend fun updateUserLevels(uuid: String, wealthLv: Int, charismaLv: Int, vip: Int)

    @Query("UPDATE user_account_table SET isBanned = :banned WHERE uuid = :uuid")
    suspend fun updateBanStatus(uuid: String, banned: Boolean)

    @Query("DELETE FROM user_account_table")
    suspend fun clearAllUsers()

    // --- 2. Room Settings Queries (Offline & Local Room Persistence) ---
    @Query("SELECT * FROM room_settings_table ORDER BY heatScore DESC, updatedAtEpochMs DESC")
    fun observeAllRoomSettings(): Flow<List<RoomSettingsEntity>>

    @Query("SELECT * FROM room_settings_table ORDER BY heatScore DESC, updatedAtEpochMs DESC")
    suspend fun getAllRoomSettings(): List<RoomSettingsEntity>

    @Query("SELECT * FROM room_settings_table WHERE roomId = :roomId LIMIT 1")
    suspend fun getRoomSettingsById(roomId: String): RoomSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoomSettings(room: RoomSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllRoomSettings(rooms: List<RoomSettingsEntity>)

    // --- 3. Chat History Queries (Offline & Local Room + Direct Chat Persistence) ---
    @Query("SELECT * FROM chat_history_table WHERE chatScope = 'ROOM' AND (roomId = :roomId OR roomId = '') ORDER BY createdAtEpochMs ASC LIMIT 100")
    fun observeRoomChatHistory(roomId: String): Flow<List<ChatHistoryEntity>>

    @Query("SELECT * FROM chat_history_table WHERE chatScope = 'ROOM' ORDER BY createdAtEpochMs ASC LIMIT 100")
    suspend fun getAllRoomChatHistory(): List<ChatHistoryEntity>

    @Query("SELECT * FROM chat_history_table WHERE chatScope = 'DIRECT' AND threadId = :threadId ORDER BY createdAtEpochMs ASC")
    suspend fun getDirectChatHistoryForThread(threadId: String): List<ChatHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatHistoryMessage(message: ChatHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatHistoryBatch(messages: List<ChatHistoryEntity>)

    @Query("UPDATE chat_history_table SET senderName = :newName, senderCustomAvatarUri = :newAvatarUri, senderWealthLevel = :wealthLv, senderCharismaLevel = :charismaLv, senderVip = :vipTier, senderChatBubbleFrame = :chatBubbleFrame WHERE senderDisplayId = :displayId OR senderUserId = :uuid")
    suspend fun syncUserIdentityInChatHistory(
        uuid: String,
        displayId: String,
        newName: String,
        newAvatarUri: String?,
        wealthLv: Int,
        charismaLv: Int,
        vipTier: Int,
        chatBubbleFrame: String
    )

    // --- 4. Database-Assigned Home Banners Queries ---
    @Query("SELECT * FROM home_banners_table WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun observeActiveHomeBanners(): Flow<List<HomeBannerEntity>>

    @Query("SELECT * FROM home_banners_table ORDER BY sortOrder ASC")
    suspend fun getAllHomeBanners(): List<HomeBannerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHomeBanners(banners: List<HomeBannerEntity>)

    // --- 5. Automatic Room Role by User ID Queries ---
    @Query("SELECT * FROM room_role_permissions_table WHERE roomId = :roomId AND userDisplayId = :userDisplayId LIMIT 1")
    suspend fun getRoomRoleForUserById(roomId: String, userDisplayId: String): RoomRolePermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoomRolePermission(entity: RoomRolePermissionEntity)

    @Query("DELETE FROM room_role_permissions_table WHERE roomId = :roomId AND userDisplayId = :userDisplayId")
    suspend fun deleteRoomRolePermission(roomId: String, userDisplayId: String)

    // --- 6. Feature Visibility & Remote Server Control Queries ---
    @Query("SELECT * FROM feature_visibility_control_table ORDER BY sortOrder ASC")
    fun observeAllFeatureControls(): Flow<List<FeatureVisibilityControlEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeatureControls(items: List<FeatureVisibilityControlEntity>)

    @Query("UPDATE feature_visibility_control_table SET isVisibleInApp = :isVisible, isEnabledForUse = :isEnabled WHERE featureKey = :featureKey")
    suspend fun updateFeatureVisibility(featureKey: String, isVisible: Boolean, isEnabled: Boolean)

    // --- 7. Store Catalog & VIP 5 Sections Queries ---
    @Query("SELECT * FROM store_catalog_table WHERE isVisibleInStore = 1 ORDER BY sortOrder ASC")
    fun observeVisibleStoreCatalog(): Flow<List<StoreCatalogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStoreCatalogItems(items: List<StoreCatalogEntity>)

    @Query("SELECT * FROM vip_five_sections_table WHERE isVisible = 1 ORDER BY vipTier ASC")
    fun observeVipFiveSections(): Flow<List<VipTierSectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVipFiveSections(sections: List<VipTierSectionEntity>)

    // --- 8. Host Agencies, Charge Agencies & Direct Database Grants Queries ---
    @Query("SELECT * FROM host_agency_members_table")
    fun observeHostAgencyMembers(): Flow<List<HostMemberTelemetryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHostAgencyMember(member: HostMemberTelemetryEntity)

    @Query("SELECT * FROM charge_shipments_table ORDER BY shipmentId DESC")
    fun observeChargeShipments(): Flow<List<ChargeShipmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargeShipment(shipment: ChargeShipmentEntity)

    @Query("SELECT * FROM database_direct_grants_table WHERE targetUserDisplayId = :displayId AND isActive = 1")
    suspend fun getDirectGrantsForUserById(displayId: String): List<DirectDatabaseGrantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDirectDatabaseGrant(grant: DirectDatabaseGrantEntity)

    // --- 9. Global Style Config Queries ---
    @Query("SELECT * FROM ui_style_control_table WHERE configKey = 'master_style' LIMIT 1")
    fun observeStyleConfig(): Flow<UiStyleControlEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStyleConfig(config: UiStyleControlEntity)
}

@Database(
    entities = [
        UserAccountEntity::class,
        RoomSettingsEntity::class,
        ChatHistoryEntity::class,
        HomeBannerEntity::class,
        RoomRolePermissionEntity::class,
        FeatureVisibilityControlEntity::class,
        StoreCatalogEntity::class,
        VipTierSectionEntity::class,
        HostMemberTelemetryEntity::class,
        ChargeShipmentEntity::class,
        DirectDatabaseGrantEntity::class,
        UiStyleControlEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ZadiraDatabase : RoomDatabase() {
    abstract fun appDao(): ZadiraAppDao

    companion object {
        @Volatile
        private var INSTANCE: ZadiraDatabase? = null

        fun getInstance(context: Context): ZadiraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZadiraDatabase::class.java,
                    "zadira_live_royal.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.voicerooms.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** التصنيف — public.categories */
@Serializable
data class Category(
    val id: Int,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
)

/** الهدية — public.gifts */
@Serializable
data class Gift(
    val id: Int,
    val name: String,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("animation_url") val animationUrl: String? = null,
    @SerialName("anim_type") val animType: String = "svga",
    val price: Int = 0,
    @SerialName("diamond_value") val diamondValue: Int = 0,
    val category: String = "normal",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

/** معاملة هدية — public.gift_transactions */
@Serializable
data class GiftTransaction(
    val id: String,
    @SerialName("room_id") val roomId: String? = null,
    @SerialName("sender_id") val senderId: String? = null,
    @SerialName("receiver_id") val receiverId: String? = null,
    @SerialName("gift_id") val giftId: Int? = null,
    val amount: Int = 1,
    @SerialName("total_coins") val totalCoins: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    val gift: Gift? = null,
    val sender: Profile? = null,
    val receiver: Profile? = null,
)

/** البانر / الوجهة المتحركة — public.banners */
@Serializable
data class Banner(
    val id: Int,
    val title: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("animation_url") val animationUrl: String? = null,
    @SerialName("link_url") val linkUrl: String? = null,
    val placement: String = "home",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

/** الوكالة — public.agencies */
@Serializable
data class Agency(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    @SerialName("logo_url") val logoUrl: String? = null,
    val description: String? = null,
    val commission: Double = 10.0,
    @SerialName("total_earnings") val totalEarnings: Long = 0,
    @SerialName("members_count") val membersCount: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    val owner: Profile? = null,
)

/** عضو الوكالة — public.agency_members */
@Serializable
data class AgencyMember(
    val id: String,
    @SerialName("agency_id") val agencyId: String,
    @SerialName("user_id") val userId: String,
    val role: String = "member",
    @SerialName("joined_at") val joinedAt: String? = null,
    val profile: Profile? = null,
)

/** المستوى — public.levels */
@Serializable
data class Level(
    val level: Int,
    val title: String? = null,
    @SerialName("min_xp") val minXp: Int = 0,
    @SerialName("badge_icon") val badgeIcon: String? = null,
    @SerialName("badge_color") val badgeColor: String? = null,
    @SerialName("reward_coins") val rewardCoins: Int = 0,
)

/** إعداد — public.app_settings */
@Serializable
data class AppSetting(
    val key: String,
    val value: String? = null,
    @SerialName("value_type") val valueType: String = "string",
    val description: String? = null,
)

/** معاملة محفظة — public.wallet_transactions */
@Serializable
data class WalletTransaction(
    val id: String,
    @SerialName("user_id") val userId: String,
    val amount: Long,
    val currency: String = "coins",
    @SerialName("tx_type") val txType: String? = null,
    val reference: String? = null,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/** إشعار — public.notifications */
@Serializable
data class AppNotification(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String? = null,
    val body: String? = null,
    val type: String = "general",
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
)

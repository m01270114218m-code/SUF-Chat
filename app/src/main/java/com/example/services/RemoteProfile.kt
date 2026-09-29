package com.example.services

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteProfile(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val country: String = "EG",
    val coins: Long = 0,
    val diamonds: Long = 0,
    @SerialName("vip_level") val vipLevel: Int = 0,
    @SerialName("display_id") val displayId: String? = null,
    @SerialName("equipped_frame_id") val equippedFrameId: String? = null,
    @SerialName("equipped_entry_welcome_id") val equippedEntryWelcomeId: String? = null,
    @SerialName("is_host_agent") val isHostAgent: Boolean = false,
    @SerialName("is_host_member") val isHostMember: Boolean = false,
    @SerialName("is_charge_agent") val isChargeAgent: Boolean = false,
    @SerialName("is_banned") val isBanned: Boolean = false
)

@kotlinx.serialization.Serializable
data class InventoryUpsert(
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("item_id") val itemId: String,
    @kotlinx.serialization.SerialName("is_equipped") val isEquipped: Boolean
)

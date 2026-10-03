package com.voicerooms.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * الملف الشخصي — يطابق جدول public.profiles
 */
@Serializable
data class Profile(
    val id: String,
    val username: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    val gender: String = "other",
    val country: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Long = 0,
    val diamonds: Long = 0,
    @SerialName("vip_level") val vipLevel: Int = 0,
    val role: String = "user",
    @SerialName("agency_id") val agencyId: String? = null,
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("is_banned") val isBanned: Boolean = false,
    @SerialName("ban_reason") val banReason: String? = null,
    @SerialName("last_seen") val lastSeen: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    val name: String get() = displayName ?: username ?: "مستخدم"
}

/** بيانات تحديث الملف الشخصي */
@Serializable
data class ProfileUpdate(
    @SerialName("display_name") val displayName: String? = null,
    val bio: String? = null,
    val gender: String? = null,
    val country: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val username: String? = null,
)

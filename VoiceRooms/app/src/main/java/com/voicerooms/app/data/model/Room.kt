package com.voicerooms.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** الغرفة — يطابق جدول public.rooms */
@Serializable
data class Room(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("agency_id") val agencyId: String? = null,
    val name: String,
    val description: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("background_url") val backgroundUrl: String? = null,
    @SerialName("category_id") val categoryId: Int? = null,
    @SerialName("room_type") val roomType: String = "public",
    @SerialName("max_seats") val maxSeats: Int = 8,
    @SerialName("welcome_msg") val welcomeMsg: String? = null,
    @SerialName("is_locked") val isLocked: Boolean = false,
    @SerialName("mic_locked") val micLocked: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    val listeners: Int = 0,
    @SerialName("total_visits") val totalVisits: Long = 0,
    @SerialName("created_at") val createdAt: String? = null,
    // علاقات مضمّنة (select مع join)
    val owner: Profile? = null,
    val category: Category? = null,
)

/** عضو الغرفة / المقعد — يطابق public.room_members */
@Serializable
data class RoomMember(
    val id: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("user_id") val userId: String,
    val role: String = "member",
    @SerialName("seat_index") val seatIndex: Int? = null,
    @SerialName("is_muted") val isMuted: Boolean = true,
    @SerialName("is_speaking") val isSpeaking: Boolean = false,
    @SerialName("is_hand_up") val isHandUp: Boolean = false,
    @SerialName("joined_at") val joinedAt: String? = null,
    @SerialName("left_at") val leftAt: String? = null,
    val profile: Profile? = null,
) {
    val isOnSeat: Boolean get() = seatIndex != null
    val isAdmin: Boolean get() = role in listOf("owner", "admin", "moderator")
}

/** رسالة الغرفة — يطابق public.room_messages */
@Serializable
data class RoomMessage(
    val id: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("user_id") val userId: String? = null,
    val content: String? = null,
    @SerialName("msg_type") val msgType: String = "text",
    @SerialName("created_at") val createdAt: String? = null,
    val profile: Profile? = null,
)

/** حظر — يطابق public.room_bans */
@Serializable
data class RoomBan(
    val id: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("banned_by") val bannedBy: String? = null,
    val reason: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/** طلب إنشاء غرفة */
@Serializable
data class CreateRoomRequest(
    val name: String,
    val description: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("category_id") val categoryId: Int? = null,
    @SerialName("room_type") val roomType: String = "public",
    @SerialName("max_seats") val maxSeats: Int = 8,
    @SerialName("welcome_msg") val welcomeMsg: String? = null,
)

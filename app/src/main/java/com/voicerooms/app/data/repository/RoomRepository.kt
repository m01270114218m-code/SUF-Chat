package com.voicerooms.app.data.repository

import com.voicerooms.app.data.model.CreateRoomRequest
import com.voicerooms.app.data.model.Room
import com.voicerooms.app.data.model.RoomMember
import com.voicerooms.app.data.model.RoomMessage
import com.voicerooms.app.data.remote.SupabaseProvider
import com.voicerooms.app.util.ApiResult
import com.voicerooms.app.util.runCatchingApi
import com.voicerooms.app.util.translateError
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * مستودع الغرف — القائمة، الإنشاء، الدخول/الخروج، المقاعد، وكل أدوات المشرف.
 */
class RoomRepository {

    private val db get() = SupabaseProvider.db

    private val roomColumns = Columns.raw("*, owner:profiles(*), category:categories(*)")
    private val memberColumns = Columns.raw("*, profile:profiles(*)")

    // ------------------------------------------------------------------ القوائم
    suspend fun getRooms(categoryId: Int? = null, query: String? = null): ApiResult<List<Room>> =
        runCatchingApi {
            db.from("rooms").select(roomColumns) {
                filter {
                    eq("is_active", true)
                    if (categoryId != null && categoryId != 0) eq("category_id", categoryId)
                    if (!query.isNullOrBlank()) ilike("name", "%$query%")
                }
                order("listeners", Order.DESCENDING)
                order("created_at", Order.DESCENDING)
                limit(100)
            }.decodeList<Room>()
        }.mapError()

    suspend fun getFeaturedRooms(): ApiResult<List<Room>> = runCatchingApi {
        db.from("rooms").select(roomColumns) {
            filter {
                eq("is_active", true)
                eq("is_featured", true)
            }
            order("listeners", Order.DESCENDING)
            limit(20)
        }.decodeList<Room>()
    }.mapError()

    suspend fun getRoom(roomId: String): ApiResult<Room> = runCatchingApi {
        db.from("rooms").select(roomColumns) {
            filter { eq("id", roomId) }
        }.decodeSingle<Room>()
    }.mapError()

    suspend fun getMyRooms(userId: String): ApiResult<List<Room>> = runCatchingApi {
        db.from("rooms").select(roomColumns) {
            filter { eq("owner_id", userId) }
            order("created_at", Order.DESCENDING)
        }.decodeList<Room>()
    }.mapError()

    // ------------------------------------------------------------------ إنشاء
    suspend fun createRoom(userId: String, req: CreateRoomRequest): ApiResult<Room> = runCatchingApi {
        db.from("rooms").insert(
            mapOf(
                "owner_id" to userId,
                "name" to req.name,
                "description" to req.description,
                "cover_url" to req.coverUrl,
                "category_id" to req.categoryId,
                "room_type" to req.roomType,
                "max_seats" to req.maxSeats,
                "welcome_msg" to req.welcomeMsg,
            )
        ) {
            select(roomColumns)
        }.decodeSingle<Room>()
    }.mapError()

    suspend fun updateRoom(roomId: String, fields: Map<String, Any?>): ApiResult<Unit> = runCatchingApi {
        db.from("rooms").update(fields) { filter { eq("id", roomId) } }
        Unit
    }.mapError()

    suspend fun deleteRoom(roomId: String): ApiResult<Unit> = runCatchingApi {
        db.from("rooms").delete { filter { eq("id", roomId) } }
        Unit
    }.mapError()

    // ------------------------------------------------------------------ دخول/خروج
    suspend fun joinRoom(roomId: String, password: String? = null): ApiResult<Unit> = runCatchingApi {
        db.rpc(
            "join_room",
            buildJsonObject {
                put("p_room_id", roomId)
                put("p_password", password)
            }
        )
        Unit
    }.mapError()

    suspend fun leaveRoom(roomId: String): ApiResult<Unit> = runCatchingApi {
        db.rpc("leave_room", buildJsonObject { put("p_room_id", roomId) })
        Unit
    }.mapError()

    // ------------------------------------------------------------------ المقاعد
    suspend fun takeSeat(roomId: String, seat: Int): ApiResult<Unit> = runCatchingApi {
        db.rpc("take_seat", buildJsonObject {
            put("p_room_id", roomId)
            put("p_seat", seat)
        })
        Unit
    }.mapError()

    suspend fun leaveSeat(roomId: String): ApiResult<Unit> = runCatchingApi {
        db.rpc("leave_seat", buildJsonObject { put("p_room_id", roomId) })
        Unit
    }.mapError()

    suspend fun setMuted(roomId: String, userId: String, muted: Boolean): ApiResult<Unit> = runCatchingApi {
        db.from("room_members").update(mapOf("is_muted" to muted)) {
            filter {
                eq("room_id", roomId)
                eq("user_id", userId)
            }
        }
        Unit
    }.mapError()

    suspend fun setHandUp(roomId: String, userId: String, up: Boolean): ApiResult<Unit> = runCatchingApi {
        db.from("room_members").update(mapOf("is_hand_up" to up)) {
            filter {
                eq("room_id", roomId)
                eq("user_id", userId)
            }
        }
        Unit
    }.mapError()

    suspend fun setSpeaking(roomId: String, userId: String, speaking: Boolean): ApiResult<Unit> = runCatchingApi {
        db.from("room_members").update(mapOf("is_speaking" to speaking)) {
            filter {
                eq("room_id", roomId)
                eq("user_id", userId)
            }
        }
        Unit
    }.mapError()

    // ------------------------------------------------------------------ الأعضاء
    suspend fun getMembers(roomId: String): ApiResult<List<RoomMember>> = runCatchingApi {
        db.from("room_members").select(memberColumns) {
            filter { eq("room_id", roomId) }
            order("seat_index", Order.ASCENDING)
        }.decodeList<RoomMember>().filter { it.leftAt == null }
    }.mapError()

    // ------------------------------------------------------------------ أدوات المشرف
    suspend fun adminMute(roomId: String, userId: String, mute: Boolean): ApiResult<Unit> = runCatchingApi {
        db.rpc("admin_mute", buildJsonObject {
            put("p_room_id", roomId)
            put("p_user_id", userId)
            put("p_mute", mute)
        })
        Unit
    }.mapError()

    suspend fun kickUser(roomId: String, userId: String): ApiResult<Unit> = runCatchingApi {
        db.rpc("kick_user", buildJsonObject {
            put("p_room_id", roomId)
            put("p_user_id", userId)
        })
        Unit
    }.mapError()

    suspend fun banUser(roomId: String, userId: String, reason: String? = null): ApiResult<Unit> = runCatchingApi {
        db.rpc("ban_user", buildJsonObject {
            put("p_room_id", roomId)
            put("p_user_id", userId)
            put("p_reason", reason)
        })
        Unit
    }.mapError()

    suspend fun setMemberRole(roomId: String, userId: String, role: String): ApiResult<Unit> = runCatchingApi {
        db.rpc("set_member_role", buildJsonObject {
            put("p_room_id", roomId)
            put("p_user_id", userId)
            put("p_role", role)
        })
        Unit
    }.mapError()

    suspend fun transferOwnership(roomId: String, newOwnerId: String): ApiResult<Unit> = runCatchingApi {
        db.from("rooms").update(mapOf("owner_id" to newOwnerId)) { filter { eq("id", roomId) } }
        db.from("room_members").update(mapOf("role" to "admin")) {
            filter { eq("room_id", roomId) }
        }
        db.from("room_members").update(mapOf("role" to "owner")) {
            filter {
                eq("room_id", roomId)
                eq("user_id", newOwnerId)
            }
        }
        Unit
    }.mapError()

    // ------------------------------------------------------------------ الشات
    suspend fun getMessages(roomId: String, limit: Int = 60): ApiResult<List<RoomMessage>> = runCatchingApi {
        db.from("room_messages").select(Columns.raw("*, profile:profiles(*)")) {
            filter { eq("room_id", roomId) }
            order("created_at", Order.DESCENDING)
            limit(limit.toLong())
        }.decodeList<RoomMessage>().reversed()
    }.mapError()

    suspend fun sendMessage(roomId: String, userId: String, content: String): ApiResult<Unit> = runCatchingApi {
        db.from("room_messages").insert(
            mapOf(
                "room_id" to roomId,
                "user_id" to userId,
                "content" to content,
                "msg_type" to "text",
            )
        )
        Unit
    }.mapError()

    private fun <T> ApiResult<T>.mapError(): ApiResult<T> =
        if (this is ApiResult.Error) ApiResult.Error(translateError(message), code) else this
}

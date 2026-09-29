package com.example.services

import com.example.config.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SupabaseRpcClient {
    private suspend fun call(name: String, body: JSONObject): String = withContext(Dispatchers.IO) {
        val connection = URL("${SupabaseConfig.URL}/rest/v1/rpc/$name").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${PharaohSupabaseAuth.accessToken() ?: SupabaseConfig.PUBLISHABLE_KEY}")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val result = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("Supabase RPC $name failed: $code $result")
            result
        } finally { connection.disconnect() }
    }


    suspend fun updateMyProfile(displayName: String, avatarUrl: String?, coverUrl: String?, frameId: String?, entryId: String?): String =
        call("update_my_profile", JSONObject().apply {
            put("p_display_name", displayName)
            put("p_avatar_url", avatarUrl ?: JSONObject.NULL)
            put("p_cover_url", coverUrl ?: JSONObject.NULL)
            put("p_equipped_frame_id", frameId ?: JSONObject.NULL)
            put("p_equipped_entry_welcome_id", entryId ?: JSONObject.NULL)
        })

    suspend fun purchaseStoreItem(itemId: String): String =
        call("purchase_store_item", JSONObject().apply { put("p_item_id", itemId) })

    suspend fun equipStoreItem(itemId: String): String =
        call("equip_store_item", JSONObject().apply { put("p_item_id", itemId) })

    suspend fun sendRoomMessage(roomId: String, message: String): String =
        call("send_room_message", JSONObject().apply {
            put("p_room_id", roomId); put("p_message", message)
        })

    suspend fun getMyAccessContext(): String =
        call("get_my_access_context", JSONObject())

    suspend fun transferAgencyCoins(agencyId: String, targetUserId: String, coins: Long, note: String? = null): String =
        call("transfer_agency_coins", JSONObject().apply {
            put("p_agency_id", agencyId)
            put("p_target_user_id", targetUserId)
            put("p_coins", coins)
            put("p_note", note ?: JSONObject.NULL)
        })

    suspend fun grantAgencyItem(agencyId: String, targetUserId: String, itemId: String, grantType: String): String =
        call("grant_agency_item", JSONObject().apply {
            put("p_agency_id", agencyId)
            put("p_target_user_id", targetUserId)
            put("p_item_id", itemId)
            put("p_grant_type", grantType)
        })

    suspend fun createRoom(name: String, title: String, type: String, privacy: String, country: String, coverUrl: String?, maxSeats: Int) =
        call("create_room", JSONObject().apply {
            put("p_name", name); put("p_title", title); put("p_type", type); put("p_privacy", privacy)
            put("p_country", country); put("p_cover_url", coverUrl ?: JSONObject.NULL); put("p_max_seats", maxSeats)
        })

    suspend fun joinRoomSeat(roomId: String, seat: Int) =
        call("join_room_seat", JSONObject().apply { put("p_room", roomId); put("p_seat", seat) })

    suspend fun leaveRoomSeat(roomId: String) =
        call("leave_room_seat", JSONObject().apply { put("p_room", roomId) })

    suspend fun setMyMute(roomId: String, muted: Boolean) =
        call("set_my_mute", JSONObject().apply { put("p_room_id", roomId); put("p_muted", muted) })

    suspend fun setSeatState(roomId: String, seat: Int, locked: Boolean, muted: Boolean) =
        call("set_seat_state", JSONObject().apply {
            put("p_room", roomId); put("p_seat", seat); put("p_locked", locked); put("p_muted", muted)
        })

    suspend fun sendGift(senderId: String, receiverId: String, roomId: String, giftId: String, quantity: Int) =
        call("send_gift", JSONObject().apply {
            put("p_sender", senderId); put("p_receiver", receiverId); put("p_room", roomId)
            put("p_gift", giftId); put("p_quantity", quantity)
        })
}

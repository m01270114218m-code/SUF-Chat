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
            connection.setRequestProperty("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
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

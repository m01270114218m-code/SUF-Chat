package com.pharaohparty.app

import android.content.Context
import android.provider.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.http.contentType
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class AuthRequest(
    val action: String,
    val username: String? = null,
    val password: String? = null,
    val nickname: String? = null,
    val device_key: String? = null
)

@Serializable
data class AuthResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val user_id: String? = null,
    val error: String? = null
)

@Serializable
data class LiveKitTokenResponse(
    val server_url: String? = null,
    val participant_token: String? = null,
    val error: String? = null
)

@Serializable
data class Profile(
    val id: String,
    val username: String? = null,
    val display_name: String? = null,
    val avatar_url: String? = null,
    val country: String? = null,
    val coins: Long = 0,
    val diamonds: Long = 0,
    val vip_level: Int = 0,
    val display_id: String? = null,
    val role_code: String = "USER",
    val is_host_agent: Boolean = false,
    val is_host_member: Boolean = false,
    val is_charge_agent: Boolean = false,
    val is_banned: Boolean = false
)

@Serializable
data class Room(
    val id: String,
    val name: String,
    val title: String? = null,
    val host_id: String,
    val type: String = "voice",
    val privacy: String = "public",
    val country: String = "EG",
    val cover_url: String? = null,
    val max_seats: Int = 8,
    val is_active: Boolean = true
)

@Serializable
data class RoomSeat(
    val room_id: String,
    val seat_no: Int,
    val user_id: String? = null,
    val is_muted: Boolean = false,
    val locked: Boolean = false,
    val joined_at: String? = null
)

@Serializable
data class RoomMessage(val id: String, val room_id: String, val user_id: String, val message: String, val created_at: String)

@Serializable
data class Gift(val id: String, val name: String, val icon: String = "🎁", val price_coins: Long = 0, val enabled: Boolean = true)

@Serializable
data class StoreItem(
    val id: String,
    val name: String,
    val category: String,
    val price_coins: Long = 0,
    val icon: String? = null,
    val asset_url: String? = null,
    val frame_id: String? = null,
    val entry_welcome_id: String? = null,
    val enabled: Boolean = true,
    val is_animated: Boolean = false,
    val animation_type: String? = null
)

class PartyApi(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val client = HttpClient(io.ktor.client.engine.android.Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    var accessToken: String? = null
        private set

    var userId: String? = null
        private set

    suspend fun auth(request: AuthRequest): AuthResponse {
        val response = client.post(
            BuildConfig.SUPABASE_URL + "/functions/v1/" + BuildConfig.AUTH_FUNCTION
        ) {
            contentType(ContentType.Application.Json)
            header("apikey", BuildConfig.SUPABASE_KEY)
            setBody(request)
        }

        val raw = response.bodyAsText()
        val body = runCatching { json.decodeFromString<AuthResponse>(raw) }.getOrElse {
            AuthResponse(error = raw.ifBlank { null })
        }
        if (!response.status.isSuccess()) {
            error(body.error ?: "تعذر تسجيل الدخول (${response.status.value})")
        }
        val token = body.access_token ?: error(body.error ?: "لم يتم إنشاء جلسة صالحة")
        val uid = body.user_id ?: error("لم يتم إنشاء معرف مستخدم")
        accessToken = token
        userId = uid
        SessionStore(context).save(token, body.refresh_token.orEmpty(), uid)
        return body
    }

    suspend fun restore(): Boolean {
        val session = SessionStore(context).read()
        if (session.access.isBlank() || session.user.isBlank()) return false
        accessToken = session.access
        userId = session.user
        return true
    }

    suspend fun profile(): Profile {
        val response = get("/rest/v1/profiles?id=eq.$userId&select=*")
        return response.body<List<Profile>>().firstOrNull()
            ?: error("الحساب غير موجود في قاعدة البيانات")
    }

    suspend fun rooms(): List<Room> {
        return get("/rest/v1/rooms?select=*&is_active=eq.true&order=created_at.desc").body()
    }

    suspend fun store(): List<StoreItem> {
        return get("/rest/v1/store_items?select=*&enabled=eq.true&order=sort_order.asc").body()
    }

    suspend fun seats(roomId: String): List<RoomSeat> = get("/rest/v1/room_seats?room_id=eq.$roomId&order=seat_no.asc").body()

    suspend fun messages(roomId: String): List<RoomMessage> = get("/rest/v1/room_messages?room_id=eq.$roomId&order=created_at.asc&limit=100").body()

    suspend fun gifts(): List<Gift> = get("/rest/v1/gifts?enabled=eq.true&order=sort_order.asc").body()

    suspend fun claimSeat(roomId: String, seatNo: Int): RoomSeat {
        val response = rpc("claim_room_seat", buildJsonObject { put("p_room_id", roomId); put("p_seat_no", seatNo) })
        if (!response.status.isSuccess()) error(response.bodyAsText())
        return response.body()
    }

    suspend fun setSeatState(roomId: String, seatNo: Int, locked: Boolean, muted: Boolean) {
        val response = rpc("set_seat_state", buildJsonObject { put("p_room", roomId); put("p_seat", seatNo); put("p_locked", locked); put("p_muted", muted) })
        if (!response.status.isSuccess()) error(response.bodyAsText())
    }

    suspend fun joinRoom(roomId: String, seatNo: Int): RoomSeat {
        val response = rpc("join_room", buildJsonObject { put("p_room_id", roomId); seatNo?.let { put("p_seat_no", it) } })
        if (!response.status.isSuccess()) error(response.bodyAsText())
        return response.body()
    }

    suspend fun leaveRoom(roomId: String) {
        val response = rpc("leave_room", buildJsonObject { put("p_room_id", roomId) })
        if (!response.status.isSuccess()) error(response.bodyAsText())
    }

    suspend fun liveKitToken(roomId: String): LiveKitTokenResponse {
        val response = client.post(
            BuildConfig.SUPABASE_URL + "/functions/v1/" + BuildConfig.LIVEKIT_TOKEN_FUNCTION
        ) {
            contentType(ContentType.Application.Json)
            header("apikey", BuildConfig.SUPABASE_KEY)
            header("Authorization", "Bearer " + (accessToken ?: error("جلسة منتهية")))
            setBody(buildJsonObject { put("room_id", roomId) })
        }
        val body = response.body<LiveKitTokenResponse>()
        if (!response.status.isSuccess()) {
            error(body.error ?: "تعذر تجهيز اتصال الصوت")
        }
        return body
    }

    private suspend fun rpc(name: String, payload: kotlinx.serialization.json.JsonObject): HttpResponse {
        return client.post(BuildConfig.SUPABASE_URL + "/rest/v1/rpc/" + name) {
            contentType(ContentType.Application.Json)
            header("apikey", BuildConfig.SUPABASE_KEY)
            header("Authorization", "Bearer " + (accessToken ?: error("جلسة منتهية")))
            setBody(payload)
        }
    }

    private suspend fun get(path: String): HttpResponse {
        return client.get(BuildConfig.SUPABASE_URL + path) {
            header("apikey", BuildConfig.SUPABASE_KEY)
            accessToken?.let { header("Authorization", "Bearer $it") }
            accept(ContentType.Application.Json)
        }
    }

    suspend fun logout() { SessionStore(context).clear(); accessToken = null; userId = null }

    fun deviceKey(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown-device"
    }
}

data class StoredSession(
    val access: String,
    val refresh: String,
    val user: String
)

class SessionStore(private val context: Context) {
    private val prefs
        get() = context.getSharedPreferences("pharaoh_session", Context.MODE_PRIVATE)

    fun save(access: String, refresh: String, user: String) {
        prefs.edit()
            .putString("access", access)
            .putString("refresh", refresh)
            .putString("user", user)
            .apply()
    }

    fun read(): StoredSession {
        return StoredSession(
            prefs.getString("access", "").orEmpty(),
            prefs.getString("refresh", "").orEmpty(),
            prefs.getString("user", "").orEmpty()
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

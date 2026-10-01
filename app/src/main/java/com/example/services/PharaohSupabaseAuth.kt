package com.example.services

import android.content.Context
import com.example.config.SupabaseConfig
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object PharaohSupabaseAuth {
    private const val PREFS = "pharaoh_supabase_session"
    private const val ACCESS_TOKEN = "access_token"
    private const val REFRESH_TOKEN = "refresh_token"
    private const val USER_ID = "user_id"

    private lateinit var appContext: Context
    private var token: String? = null
    private var userId: String? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        token = prefs.getString(ACCESS_TOKEN, null)
        userId = prefs.getString(USER_ID, null)
    }

    suspend fun loginOrCreate(username: String, credential: String, nickname: String?, allowCreate: Boolean = true): String {
        return callAuthFunction(
            mode = if (allowCreate) "register" else "login",
            username = username.trim(),
            password = credential,
            displayName = nickname?.trim().orEmpty()
        )
    }

    suspend fun quickLogin(): String {
        val androidId = android.provider.Settings.Secure.getString(
            appContext.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ).orEmpty()
        if (androidId.isBlank()) error("تعذر قراءة معرف الجهاز للدخول السريع")
        return callAuthFunction(mode = "quick", deviceKey = androidId)
    }

    private suspend fun callAuthFunction(
        mode: String,
        username: String = "",
        password: String = "",
        displayName: String = "",
        deviceKey: String = ""
    ): String {
        val body = JSONObject().apply {
            put("mode", mode)
            if (username.isNotBlank()) put("username", username)
            if (password.isNotBlank()) put("password", password)
            if (displayName.isNotBlank()) put("display_name", displayName)
            if (deviceKey.isNotBlank()) put("device_key", deviceKey)
        }
        val response = functionRequest(body)
        if (!response.optBoolean("ok", false)) {
            error(response.optString("error").ifBlank { "تعذر تسجيل الدخول إلى قاعدة البيانات" })
        }
        val access = response.optString("access_token").takeIf { it.isNotBlank() }
            ?: error("Supabase لم يرجع جلسة دخول")
        val id = response.optString("user_id").takeIf { it.isNotBlank() }
            ?: error("Supabase لم يرجع معرف الحساب")
        val refresh = response.optString("refresh_token").takeIf { it.isNotBlank() }
        token = access
        userId = id
        saveSession(access, refresh, id)
        return id
    }

    suspend fun loadProfile(userId: String): RemoteProfile {
        val encoded = java.net.URLEncoder.encode(userId, "UTF-8")
        val raw = restRequest(
            "GET",
            "/rest/v1/profiles?select=id,display_name,avatar_url,country,coins,diamonds,vip_level,display_id,equipped_frame_id,equipped_entry_welcome_id,is_host_agent,is_host_member,is_charge_agent,is_banned,username&id=eq.$encoded"
        )
        val item = JSONArray(raw).optJSONObject(0) ?: error("ملف المستخدم غير موجود في Supabase")
        return Json.decodeFromString<RemoteProfile>(item.toString())
    }

    suspend fun syncProfile(
        userId: String,
        displayName: String,
        coins: Long,
        diamonds: Long,
        vipLevel: Int,
        frameId: String?,
        entryId: String?,
        avatarUrl: String?,
        coverUrl: String?
    ) {
        SupabaseRpcClient.updateMyProfile(displayName, avatarUrl, coverUrl, frameId, entryId)
    }

    suspend fun upsertInventory(userId: String, itemId: String, equipped: Boolean) {
        restRequest(
            "POST",
            "/rest/v1/user_inventory?on_conflict=user_id,item_id",
            JSONObject().apply {
                put("user_id", userId)
                put("item_id", itemId)
                put("is_equipped", equipped)
            },
            extraHeaders = mapOf("Prefer" to "resolution=merge-duplicates,return=minimal")
        )
    }

    fun accessToken(): String? = token

    fun signOut() {
        val current = token
        if (!current.isNullOrBlank()) {
            runCatching {
                kotlinx.coroutines.runBlocking {
                    restRequest("POST", "/auth/v1/logout", bearer = current)
                }
            }
        }
        token = null
        userId = null
        if (::appContext.isInitialized) {
            appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        }
    }

    private fun saveSession(access: String, refresh: String?, id: String) {
        val editor = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(ACCESS_TOKEN, access)
            .putString(USER_ID, id)
        if (refresh != null) editor.putString(REFRESH_TOKEN, refresh)
        editor.apply()
    }

    private fun functionRequest(body: JSONObject): JSONObject {
        val connection = (URL(SupabaseConfig.URL + "/functions/v1/pharaoh-auth").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 20_000
            connection.readTimeout = 25_000
            connection.doOutput = true
            connection.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val result = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("Supabase request failed: $code $result")
            return JSONObject(result)
        } finally {
            connection.disconnect()
        }
    }

    private fun restRequest(
        method: String,
        path: String,
        body: JSONObject? = null,
        bearer: String? = token,
        extraHeaders: Map<String, String> = emptyMap()
    ): String {
        val connection = (URL(SupabaseConfig.URL + path).openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            connection.setRequestProperty("Authorization", "Bearer " + (bearer ?: token ?: SupabaseConfig.PUBLISHABLE_KEY))
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            extraHeaders.forEach { (key, value) -> connection.setRequestProperty(key, value) }
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val result = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("Supabase request failed: $code $result")
            return result
        } finally {
            connection.disconnect()
        }
    }
}

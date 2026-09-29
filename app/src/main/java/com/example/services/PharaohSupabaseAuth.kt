package com.example.services

import android.content.Context
import com.example.config.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.security.MessageDigest
import java.util.UUID
import java.net.URL

object PharaohSupabaseAuth {
    private const val PREFS = "pharaoh_supabase_session"
    private const val ACCESS_TOKEN = "access_token"
    private const val USER_ID = "user_id"
    private const val QUICK_EMAIL = "quick_email"
    private const val QUICK_PASSWORD = "quick_password"

    private lateinit var appContext: Context
    private var token: String? = null
    private var userId: String? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        token = prefs.getString(ACCESS_TOKEN, null)
        userId = prefs.getString(USER_ID, null)
    }

    private fun accountEmail(username: String): String {
        val raw = username.trim().lowercase()
        if (raw.contains("@")) {
            require(raw.length >= 6) { "البريد الإلكتروني غير صالح" }
            return raw
        }
        val normalized = raw.replace(Regex("[^a-z0-9_.-]"), "_")
        require(normalized.length >= 3) { "اسم الحساب يجب أن يكون 3 أحرف أو أكثر" }
        return "$normalized@accounts.pharaohparty.com"
    }

    suspend fun loginOrCreate(username: String, credential: String, nickname: String?, allowCreate: Boolean = true): String =
        withContext(Dispatchers.IO) {
            val email = accountEmail(username)
            val login = runCatching {
                authRequest(
                    "POST",
                    "/auth/v1/token?grant_type=password",
                    JSONObject().apply {
                        put("email", email)
                        put("password", credential)
                    }
                )
            }.getOrNull()

            val response = if (login != null) login else if (allowCreate) authRequest(
                "POST",
                "/auth/v1/signup",
                JSONObject().apply {
                    put("email", email)
                    put("password", credential)
                    put("data", JSONObject().apply {
                        put("display_name", nickname?.trim().takeUnless { it.isNullOrBlank() } ?: username.trim())
                        put("username", username.trim())
                    })
                }
            ) else error("بيانات الدخول غير صحيحة أو الحساب غير موجود.")

            val access = response.optString("access_token").takeIf { it.isNotBlank() }
            val id = response.optJSONObject("user")?.optString("id")?.takeIf { it.isNotBlank() }
                ?: response.optString("id").takeIf { it.isNotBlank() }

            if (access.isNullOrBlank() || id.isNullOrBlank()) {
                error("تم إنشاء الحساب، لكن تأكيد البريد الإلكتروني مفعّل في Supabase. عطّل Confirm email حتى يدخل الحساب فوراً باسم الحساب وكلمة السر.")
            }

            token = access
            userId = id
            saveSession(access, id)
            id
        }

    suspend fun quickLogin(): String = withContext(Dispatchers.IO) {
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var email = prefs.getString(QUICK_EMAIL, null)
        var password = prefs.getString(QUICK_PASSWORD, null)
        if (!email.isNullOrBlank() && email.endsWith(".local", ignoreCase = true)) {
            email = null
        }
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            val deviceId = android.provider.Settings.Secure.getString(appContext.contentResolver, android.provider.Settings.Secure.ANDROID_ID).orEmpty()
            val hash = MessageDigest.getInstance("SHA-256").digest(deviceId.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
            email = "device_" + hash.take(24) + "@quick.pharaohparty.com"
            password = UUID.randomUUID().toString().replace("-", "") + "Q9!"
            prefs.edit().putString(QUICK_EMAIL, email).putString(QUICK_PASSWORD, password).apply()
        }
        val login = runCatching { authRequest("POST", "/auth/v1/token?grant_type=password", JSONObject().apply { put("email", email); put("password", password) }) }.getOrNull()
        val response = login ?: authRequest("POST", "/auth/v1/signup", JSONObject().apply {
            put("email", email); put("password", password);
            put("data", JSONObject().apply { put("display_name", "مستخدم فرعون بارتي"); put("username", email) })
        })
        val access = response.optString("access_token").takeIf { it.isNotBlank() }
        val id = response.optJSONObject("user")?.optString("id")?.takeIf { it.isNotBlank() } ?: response.optString("id").takeIf { it.isNotBlank() }
        if (access.isNullOrBlank() || id.isNullOrBlank()) error("تعذر إنشاء جلسة الدخول السريع. تأكد من تفعيل Email Signups في Supabase.")
        token = access; userId = id; saveSession(access, id); id
    }

    suspend fun loadProfile(userId: String): RemoteProfile = withContext(Dispatchers.IO) {
        val encoded = java.net.URLEncoder.encode(userId, "UTF-8")
        val raw = restRequest(
            "GET",
            "/rest/v1/profiles?select=id,display_name,avatar_url,country,coins,diamonds,vip_level,display_id,equipped_frame_id,equipped_entry_welcome_id,is_host_agent,is_host_member,is_charge_agent,is_banned&id=eq.$encoded"
        )
        val item = JSONArray(raw).optJSONObject(0) ?: error("ملف المستخدم غير موجود")
        Json.decodeFromString<RemoteProfile>(item.toString())
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

    private fun saveSession(access: String, id: String) {
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(ACCESS_TOKEN, access)
            .putString(USER_ID, id)
            .apply()
    }

    private fun authRequest(method: String, path: String, body: JSONObject): JSONObject =
        JSONObject(restRequest(method, path, body = body))

    private fun restRequest(
        method: String,
        path: String,
        body: JSONObject? = null,
        bearer: String? = token,
        extraHeaders: Map<String, String> = emptyMap()
    ): String {
        val connection = (URL("${SupabaseConfig.URL}$path").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${bearer ?: token ?: SupabaseConfig.PUBLISHABLE_KEY}")
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

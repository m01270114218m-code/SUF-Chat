package com.example.data

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class SupabaseAuthResult(
    val userId: String,
    val email: String,
    val accessToken: String
)

class SupabaseClient(context: Context) {
    private val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)

    private val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val publishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY

    suspend fun signIn(email: String, password: String): SupabaseAuthResult =
        withContext(Dispatchers.IO) {
            authRequest("/auth/v1/token?grant_type=password", email, password)
        }

    suspend fun signUp(email: String, password: String): SupabaseAuthResult =
        withContext(Dispatchers.IO) {
            authRequest("/auth/v1/signup", email, password)
        }

    suspend fun ensureProfile(
        userId: String,
        email: String,
        nickname: String,
        avatarType: String
    ): JSONObject = withContext(Dispatchers.IO) {
        val current = request(
            method = "GET",
            path = "/rest/v1/profiles?id=eq.$userId&select=*",
            token = sessionToken()
        )
        if (current.isNotEmpty()) {
            return@withContext current.first()
        }

        val displayId = buildDisplayId(userId)
        val body = JSONObject()
            .put("id", userId)
            .put("display_name", nickname.ifBlank { email.substringBefore("@").ifBlank { "مستخدم جديد" } })
            .put("country", "EG")
            .put("display_id", displayId)
            .put("role_code", "USER")
            .put("wealth_level", 0)
            .put("charisma_level", 0)
            .put("vip_level", 0)
            .put("coins", 0)
            .put("diamonds", 0)

        val created = request(
            method = "POST",
            path = "/rest/v1/profiles",
            token = sessionToken(),
            body = body.toString(),
            extraHeaders = mapOf("Prefer" to "return=representation")
        )
        if (created.isEmpty()) throw IOException("تعذر إنشاء ملف المستخدم في Supabase")
        created.first()
    }

    fun saveSession(result: SupabaseAuthResult) {
        prefs.edit()
            .putString("access_token", result.accessToken)
            .putString("user_id", result.userId)
            .putString("email", result.email)
            .apply()
    }

    fun sessionToken(): String? = prefs.getString("access_token", null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    private fun authRequest(path: String, email: String, password: String): SupabaseAuthResult {
        val response = request(
            method = "POST",
            path = path,
            token = null,
            body = JSONObject()
                .put("email", email.trim())
                .put("password", password)
                .toString()
        ).firstOrNull() ?: throw IOException("استجابة Supabase فارغة")

        val accessToken = response.optString("access_token")
        val user = response.optJSONObject("user")
            ?: throw IOException(
                if (response.optBoolean("email_confirmed", false).not() && accessToken.isBlank())
                    "تم إنشاء الحساب. يجب تأكيد البريد الإلكتروني قبل تسجيل الدخول."
                else "تعذر إنشاء جلسة Supabase"
            )

        if (accessToken.isBlank()) {
            throw IOException("الحساب يحتاج إلى تأكيد البريد الإلكتروني قبل تسجيل الدخول.")
        }

        return SupabaseAuthResult(
            userId = user.getString("id"),
            email = user.optString("email", email.trim()),
            accessToken = accessToken
        )
    }

    private fun request(
        method: String,
        path: String,
        token: String?,
        body: String? = null,
        extraHeaders: Map<String, String> = emptyMap()
    ): List<JSONObject> {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 20000
            doInput = true
            if (body != null) doOutput = true
            setRequestProperty("apikey", publishableKey)
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            if (!token.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
            extraHeaders.forEach { (key, value) -> setRequestProperty(key, value) }
        }

        try {
            if (body != null) {
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (status !in 200..299) {
                val message = runCatching {
                    JSONObject(text).optString("msg")
                        .ifBlank { JSONObject(text).optString("message") }
                        .ifBlank { JSONObject(text).optString("error_description") }
                }.getOrNull().orEmpty()
                throw IOException(message.ifBlank { "Supabase HTTP $status" })
            }

            if (text.isBlank()) return emptyList()
            return when {
                text.trimStart().startsWith("[") -> {
                    val array = org.json.JSONArray(text)
                    List(array.length()) { array.getJSONObject(it) }
                }
                else -> listOf(JSONObject(text))
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun buildDisplayId(userId: String): String {
        val digits = userId.filter { it.isDigit() }
        val suffix = digits.takeLast(7).padStart(7, '0')
        return suffix
    }
}

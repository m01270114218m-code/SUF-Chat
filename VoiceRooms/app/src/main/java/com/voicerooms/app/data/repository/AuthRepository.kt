package com.voicerooms.app.data.repository

import com.voicerooms.app.data.model.Profile
import com.voicerooms.app.data.remote.SupabaseProvider
import com.voicerooms.app.util.ApiResult
import com.voicerooms.app.util.runCatchingApi
import com.voicerooms.app.util.translateError
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * مستودع المصادقة — تسجيل الدخول/الإنشاء/الخروج + جلب الملف الشخصي.
 */
class AuthRepository {

    private val auth get() = SupabaseProvider.auth
    private val db get() = SupabaseProvider.db

    val currentUserId: String? get() = auth.currentUserOrNull()?.id
    val isLoggedIn: Boolean get() = auth.currentUserOrNull() != null

    suspend fun signUp(
        email: String,
        password: String,
        username: String,
        displayName: String,
    ): ApiResult<Unit> = runCatchingApi {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("username", username)
                put("display_name", displayName)
            }
        }
        Unit
    }.mapError()

    suspend fun signIn(email: String, password: String): ApiResult<Unit> = runCatchingApi {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        Unit
    }.mapError()

    suspend fun signOut(): ApiResult<Unit> = runCatchingApi {
        auth.signOut()
        Unit
    }.mapError()

    suspend fun sendPasswordReset(email: String): ApiResult<Unit> = runCatchingApi {
        auth.resetPasswordForEmail(email)
        Unit
    }.mapError()

    suspend fun getMyProfile(): ApiResult<Profile> = runCatchingApi {
        val uid = currentUserId ?: throw IllegalStateException("غير مسجّل الدخول")
        db.from("profiles").select {
            filter { eq("id", uid) }
        }.decodeSingle<Profile>()
    }.mapError()

    suspend fun getProfile(userId: String): ApiResult<Profile> = runCatchingApi {
        db.from("profiles").select {
            filter { eq("id", userId) }
        }.decodeSingle<Profile>()
    }.mapError()

    private fun <T> ApiResult<T>.mapError(): ApiResult<T> =
        if (this is ApiResult.Error) ApiResult.Error(translateError(message), code) else this
}

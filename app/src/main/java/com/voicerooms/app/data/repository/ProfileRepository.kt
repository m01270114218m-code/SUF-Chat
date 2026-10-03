package com.voicerooms.app.data.repository

import com.voicerooms.app.data.model.Profile
import com.voicerooms.app.data.model.ProfileUpdate
import com.voicerooms.app.data.remote.SupabaseProvider
import com.voicerooms.app.util.ApiResult
import com.voicerooms.app.util.runCatchingApi
import com.voicerooms.app.util.translateError
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * مستودع الملف الشخصي — التحديث، رفع الصورة، المتابعة، المحفظة.
 */
class ProfileRepository {

    private val db get() = SupabaseProvider.db

    suspend fun updateProfile(userId: String, update: ProfileUpdate): ApiResult<Profile> = runCatchingApi {
        db.from("profiles").update(update) {
            filter { eq("id", userId) }
            select()
        }.decodeSingle<Profile>()
    }.mapError()

    suspend fun setOnline(userId: String, online: Boolean): ApiResult<Unit> = runCatchingApi {
        db.from("profiles").update(
            mapOf(
                "is_online" to online,
                "last_seen" to "now()"
            )
        ) { filter { eq("id", userId) } }
        Unit
    }.mapError()

    /** رفع صورة شخصية إلى Storage وإرجاع الرابط العام */
    suspend fun uploadAvatar(userId: String, bytes: ByteArray, ext: String = "jpg"): ApiResult<String> =
        runCatchingApi {
            val path = "$userId/avatar.$ext"
            val bucket = SupabaseProvider.storage.from("avatars")
            withContext(Dispatchers.IO) {
                bucket.upload(path, bytes, upsert = true)
            }
            bucket.publicUrl(path)
        }.mapError()

    suspend fun getProfile(userId: String): ApiResult<Profile> = runCatchingApi { db.from("profiles").select { filter { eq("id", userId) } }.decodeSingle<Profile>() }.mapError()

    suspend fun getFollowersCount(userId: String): Int = runCatchingApi {
        db.from("follows").select { filter { eq("following_id", userId) } }.decodeList<Map<String, String>>().size
    }.let { if (it is ApiResult.Success) it.data else 0 }

    suspend fun getFollowingCount(userId: String): Int = runCatchingApi {
        db.from("follows").select { filter { eq("follower_id", userId) } }.decodeList<Map<String, String>>().size
    }.let { if (it is ApiResult.Success) it.data else 0 }

    suspend fun isFollowing(followerId: String, followingId: String): Boolean = runCatchingApi {
        db.from("follows").select {
            filter {
                eq("follower_id", followerId)
                eq("following_id", followingId)
            }
        }.decodeList<Map<String, String>>().isNotEmpty()
    }.let { if (it is ApiResult.Success) it.data else false }

    suspend fun follow(followerId: String, followingId: String): ApiResult<Unit> = runCatchingApi {
        db.from("follows").insert(mapOf("follower_id" to followerId, "following_id" to followingId))
        Unit
    }.mapError()

    suspend fun unfollow(followerId: String, followingId: String): ApiResult<Unit> = runCatchingApi {
        db.from("follows").delete {
            filter {
                eq("follower_id", followerId)
                eq("following_id", followingId)
            }
        }
        Unit
    }.mapError()

    suspend fun recharge(userId: String, amount: Long): ApiResult<Unit> = runCatchingApi {
        db.rpc("recharge_coins", mapOf("p_user_id" to userId, "p_amount" to amount))
        Unit
    }.mapError()

    suspend fun getWalletTransactions(userId: String): ApiResult<List<com.voicerooms.app.data.model.WalletTransaction>> = runCatchingApi {
        db.from("wallet_transactions").select {
            filter { eq("user_id", userId) }
            order("created_at", Order.DESCENDING)
            limit(50)
        }.decodeList<com.voicerooms.app.data.model.WalletTransaction>()
    }.mapError()

    suspend fun getNotifications(userId: String): ApiResult<List<com.voicerooms.app.data.model.AppNotification>> = runCatchingApi {
        db.from("notifications").select {
            filter { eq("user_id", userId) }
            order("created_at", Order.DESCENDING)
            limit(50)
        }.decodeList<com.voicerooms.app.data.model.AppNotification>()
    }.mapError()

    suspend fun searchUsers(query: String): ApiResult<List<Profile>> = runCatchingApi {
        db.from("profiles").select(Columns.list("id", "username", "display_name", "avatar_url", "level", "is_online")) {
            filter { ilike("display_name", "%$query%") }
            limit(30)
            order("level", Order.DESCENDING)
        }.decodeList<Profile>()
    }.mapError()

    private fun <T> ApiResult<T>.mapError(): ApiResult<T> =
        if (this is ApiResult.Error) ApiResult.Error(translateError(message), code) else this
}

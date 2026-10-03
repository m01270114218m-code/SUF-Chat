package com.voicerooms.app.data.repository

import com.voicerooms.app.data.model.AppSetting
import com.voicerooms.app.data.model.Banner
import com.voicerooms.app.data.model.Category
import com.voicerooms.app.data.model.Level
import com.voicerooms.app.data.remote.SupabaseProvider
import com.voicerooms.app.util.ApiResult
import com.voicerooms.app.util.runCatchingApi
import com.voicerooms.app.util.translateError
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

/**
 * مستودع الإعدادات والبيانات المرجعية — كل شيء متحكم فيه من الداتابيز.
 */
class SettingsRepository {

    private val db get() = SupabaseProvider.db

    suspend fun getSettings(): ApiResult<Map<String, String>> = runCatchingApi {
        db.from("app_settings").select().decodeList<AppSetting>()
            .associate { it.key to (it.value ?: "") }
    }.mapError()

    suspend fun getCategories(): ApiResult<List<Category>> = runCatchingApi {
        db.from("categories").select {
            filter { eq("is_active", true) }
            order("sort_order", Order.ASCENDING)
        }.decodeList<Category>()
    }.mapError()

    suspend fun getLevels(): ApiResult<List<Level>> = runCatchingApi {
        db.from("levels").select { order("level", Order.ASCENDING) }.decodeList<Level>()
    }.mapError()

    suspend fun getBanners(placement: String = "home"): ApiResult<List<Banner>> = runCatchingApi {
        db.from("banners").select {
            filter {
                eq("is_active", true)
                eq("placement", placement)
            }
            order("sort_order", Order.ASCENDING)
        }.decodeList<Banner>()
    }.mapError()

    private fun <T> ApiResult<T>.mapError(): ApiResult<T> =
        if (this is ApiResult.Error) ApiResult.Error(translateError(message), code) else this
}

package com.voicerooms.app.data.remote

import com.voicerooms.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * مزوّد Supabase — نقطة واحدة لإنشاء العميل وتهيئة الخدمات الأربعة:
 * Auth (المصادقة) + Postgrest (قاعدة البيانات) + Realtime (البث) + Storage (الملفات).
 */
object SupabaseProvider {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    // ---- وصول سريع للخدمات ----
    val auth get() = client.auth
    val db get() = client.postgrest
    val realtime get() = client.realtime
    val storage get() = client.storage
}

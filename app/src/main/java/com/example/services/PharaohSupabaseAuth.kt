package com.example.services

import android.content.Context
import com.example.config.SupabaseConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Email
import io.github.jan.supabase.createSupabaseClient

object PharaohSupabaseAuth {
    private lateinit var client: io.github.jan.supabase.SupabaseClient

    fun init(context: Context) {
        client = createSupabaseClient(SupabaseConfig.URL, SupabaseConfig.PUBLISHABLE_KEY) {
            install(Auth) {
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }
        }
    }

    private fun accountEmail(username: String): String {
        val normalized = username.trim().lowercase().replace(Regex("[^a-z0-9_.-]"), "_")
        require(normalized.length >= 3) { "اسم الحساب يجب أن يكون 3 أحرف أو أكثر" }
        return "$normalized@accounts.pharaohparty.local"
    }

    suspend fun loginOrCreate(username: String, credential: String, nickname: String?): String {
        val email = accountEmail(username)
        return runCatching {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = credential
            }
            client.auth.currentUserOrNull()?.id ?: error("لم يتم إنشاء جلسة للحساب")
        }.getOrElse {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = credential
                data = kotlinx.serialization.json.buildJsonObject {
                    put("display_name", nickname?.trim().takeUnless { it.isNullOrBlank() } ?: username.trim())
                    put("username", username.trim())
                }
            }
            client.auth.currentUserOrNull()?.id
                ?: error("تم إنشاء الحساب، لكن تأكيد البريد الإلكتروني مفعّل في Supabase. يجب تعطيل Confirm email حتى يدخل الحساب فوراً باسم الحساب وكلمة السر.")
        }
    }

    fun signOut() {
        if (::client.isInitialized) {
            kotlinx.coroutines.runBlocking { client.auth.signOut() }
        }
    }
}

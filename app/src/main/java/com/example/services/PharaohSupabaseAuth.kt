package com.example.services

import android.content.Context
import com.example.config.SupabaseConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.providers.Email
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.createSupabaseClient

object PharaohSupabaseAuth {
    private lateinit var client: io.github.jan.supabase.SupabaseClient

    fun init(context: Context) {
        client = createSupabaseClient(SupabaseConfig.URL, SupabaseConfig.PUBLISHABLE_KEY) {
            install(Auth) {
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }
            install(Postgrest)
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

    suspend fun loadProfile(userId: String): RemoteProfile {
        return client.from("profiles").select(
            columns = Columns.list(
                "id,display_name,avatar_url,country,coins,diamonds,vip_level,display_id,equipped_frame_id,equipped_entry_welcome_id,is_host_agent,is_host_member,is_charge_agent,is_banned"
            )
        ) {
            filter { eq("id", userId) }
        }.decodeSingle<RemoteProfile>()
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
        client.from("profiles").update({
            set("display_name", displayName)
            set("coins", coins)
            set("diamonds", diamonds)
            set("vip_level", vipLevel)
            set("equipped_frame_id", frameId)
            set("equipped_entry_welcome_id", entryId)
            set("avatar_url", avatarUrl)
            set("custom_cover_url", coverUrl)
        }) {
            filter { eq("id", userId) }
        }
    }

    suspend fun upsertInventory(userId: String, itemId: String, equipped: Boolean) {
        client.from("user_inventory").upsert(
            mapOf(
                "user_id" to userId,
                "item_id" to itemId,
                "is_equipped" to equipped
            )
        )
    }

    fun signOut() {
        if (::client.isInitialized) {
            kotlinx.coroutines.runBlocking { client.auth.signOut() }
        }
    }
}

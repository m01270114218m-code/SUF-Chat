package com.example.services

import com.example.config.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object SupabaseRestClient {
    private fun request(path: String): String =
        (URL("${SupabaseConfig.URL}/rest/v1/$path").openConnection() as HttpURLConnection).let { connection ->
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            connection.setRequestProperty("Accept", "application/json")
            try {
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) error("Supabase REST $code: $body")
                body
            } finally {
                connection.disconnect()
            }
        }

    suspend fun loadUiTheme(): String = withContext(Dispatchers.IO) { request("ui_theme?select=*&id=eq.1") }
    suspend fun loadUiAssets(): String = withContext(Dispatchers.IO) { request("ui_assets?select=*&enabled=eq.true&order=z_index.asc,sort_order.asc") }
    suspend fun loadStoreItemsRaw(): String = withContext(Dispatchers.IO) { request("store_items?select=*&enabled=eq.true&order=sort_order.asc") }
    suspend fun findStoreAsset(name: String): String = withContext(Dispatchers.IO) { request("store_items?select=id,name,asset_url,asset_type,mime_type,is_animated,animation_type,duration_ms,loop&name=eq.${java.net.URLEncoder.encode(name, "UTF-8")}&limit=1") }
    suspend fun loadAppConfig(): String = withContext(Dispatchers.IO) { request("app_config?select=*&id=eq.global") }
    suspend fun loadAppSettings(): String = withContext(Dispatchers.IO) { request("app_settings?select=*") }
    suspend fun loadPublicRooms(): String = withContext(Dispatchers.IO) { request("rooms?select=*&is_active=eq.true&order=created_at.desc") }
    suspend fun loadPublicProfiles(): String = withContext(Dispatchers.IO) { request("profiles?select=id,display_name,avatar_url,country,coins,diamonds,vip_level,display_id,equipped_frame_id,equipped_entry_welcome_id,is_banned") }
    suspend fun verifyConnection(): Boolean = withContext(Dispatchers.IO) {
        runCatching { request("app_settings?select=key&limit=1"); true }.getOrDefault(false)
    }
}

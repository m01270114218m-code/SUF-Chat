package com.example

import android.app.Application
import android.util.Log
import com.example.services.SupabaseRestClient
import com.example.services.PharaohSupabaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PharaohPartyApplication : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        PharaohSupabaseAuth.init(this)
        scope.launch {
            val connected = SupabaseRestClient.verifyConnection()
            if (connected) {
                runCatching {
                    SupabaseRestClient.loadAppConfig()
                    SupabaseRestClient.loadAppSettings()
                    SupabaseRestClient.loadUiTheme()
                    SupabaseRestClient.loadUiAssets()
                    SupabaseRestClient.loadPublicRooms()
                }.onSuccess {
                    Log.i("PharaohParty", "Supabase connected: remote configuration and public tables loaded")
                }.onFailure {
                    Log.e("PharaohParty", "Supabase connected but data load failed", it)
                }
            } else {
                Log.e("PharaohParty", "Supabase connection failed")
            }
        }
    }
}

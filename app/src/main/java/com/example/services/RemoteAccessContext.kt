package com.example.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RemoteAgencyAccess(
    val agencyId: String,
    val agencyName: String,
    val agencyType: String,
    val role: String,
    val coinsBalance: Long
)

data class RemoteFeatureAccess(
    val featureKey: String,
    val visible: Boolean,
    val enabled: Boolean,
    val requiredRole: String
)

data class RemoteAccessContext(
    val isChargeAgent: Boolean = false,
    val isHostAgent: Boolean = false,
    val isHost: Boolean = false,
    val agencies: List<RemoteAgencyAccess> = emptyList(),
    val features: List<RemoteFeatureAccess> = emptyList()
) {
    fun featureVisible(key: String, fallback: Boolean = true): Boolean =
        features.firstOrNull { it.featureKey == key }?.visible ?: fallback
}

object RemoteAccessContextStore {
    private val _state = MutableStateFlow(RemoteAccessContext())
    val state: StateFlow<RemoteAccessContext> = _state.asStateFlow()
    fun set(value: RemoteAccessContext) { _state.value = value }
    fun clear() { _state.value = RemoteAccessContext() }
}

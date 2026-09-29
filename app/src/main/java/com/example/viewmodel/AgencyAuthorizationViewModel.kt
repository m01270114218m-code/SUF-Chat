package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.config.MasterAppDatabaseTable
import com.example.models.UserProfile
import com.example.services.RemoteAccessContextStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Immutable UI state representing server-side Agency Authorization & Feature Visibility flags.
 *
 * By default, all agency portals (`مركز الوكالات` / `بيانات المضيف` / `وكالة الشحن`) are hidden (`false`)
 * until the user's unique ID (`displayId`) is explicitly granted authorization from the server/database.
 */
data class AgencyAuthorizationUiState(
    val currentUserDisplayId: String = "",
    val isHostAgentAuthorized: Boolean = false,
    val isApprovedHostAuthorized: Boolean = false,
    val isChargeAgentAuthorized: Boolean = false,
    val agencyNameAr: String = "",
    val chargeAgentCoinsBalance: Long = 0L,
    val hostMicHours: Int = 0,
    val hostActiveDays: Int = 0,
    val hostGiftsDiamonds: Long = 0L,
    val serverFeatureVisibilityMap: Map<String, Boolean> = emptyMap(),
    val serverFeatureEnabledMap: Map<String, Boolean> = emptyMap()
) {
    /**
     * True ONLY if the server/database granted `isHostAgent` to this user AND the feature flag is visible.
     */
    val shouldShowHostAgencyCenter: Boolean
        get() = isHostAgentAuthorized && (serverFeatureVisibilityMap["screen_host_agency_center"] ?: true)

    /**
     * True ONLY if the server/database granted `isApprovedHost` to this user AND the feature flag is visible.
     */
    val shouldShowHostPersonalData: Boolean
        get() = isApprovedHostAuthorized && (serverFeatureVisibilityMap["screen_host_personal_data"] ?: true)

    /**
     * True ONLY if the server/database granted `isChargeAgent` to this user AND the feature flag is visible.
     */
    val shouldShowChargeAgencyCenter: Boolean
        get() = isChargeAgentAuthorized && (serverFeatureVisibilityMap["screen_charge_agency_center"] ?: true)

    /**
     * True if at least one server-activated agency/host portal is authorized for this user.
     */
    val hasAnyAgencyPortalVisible: Boolean
        get() = shouldShowHostAgencyCenter || shouldShowHostPersonalData || shouldShowChargeAgencyCenter

    /**
     * Checks whether any primary/secondary app feature is visible according to server-side flags.
     */
    fun isFeatureVisible(featureKey: String): Boolean {
        return when (featureKey) {
            "screen_host_agency_center" -> shouldShowHostAgencyCenter
            "screen_host_personal_data" -> shouldShowHostPersonalData
            "screen_charge_agency_center" -> shouldShowChargeAgencyCenter
            else -> serverFeatureVisibilityMap[featureKey] ?: true
        }
    }
}

/**
 * ViewModel that handles observing agency authorization state and server-side feature flags,
 * allowing specific app features (Host Agency Center, Host Personal Telemetry, Charge Agency Center,
 * Store, VIP 5 Sections, etc.) to be hidden or shown dynamically based on server/database state.
 */
class AgencyAuthorizationViewModel : ViewModel() {

    private val _observedUserProfile = MutableStateFlow<UserProfile?>(null)
    val observedUserProfile: StateFlow<UserProfile?> = _observedUserProfile.asStateFlow()

    /**
     * Reactive stream combining:
     * 1. The active user's profile (`displayId` and profile flags)
     * 2. The server/database Agency Permissions Registry (`agencyPermissionsStateFlow`)
     * 3. The server/database Feature Visibility Control Table (`featureControlStateFlow`)
     */
    val authorizationState: StateFlow<AgencyAuthorizationUiState> = combine(
        _observedUserProfile,
        MasterAppDatabaseTable.agencyPermissionsStateFlow,
        MasterAppDatabaseTable.featureControlStateFlow,
        RemoteAccessContextStore.state
    ) { user, agencyRegistryMap, featureControls, remoteAccess ->
        val displayId = user?.displayId?.trim().orEmpty()
        val serverGrant = agencyRegistryMap[displayId]

        val isHostAgent = serverGrant?.isHostAgent ?: (user?.isHostAgent == true)
        val isApprovedHost = serverGrant?.isApprovedHost ?: (user?.isApprovedHost == true)
        val isChargeAgent = serverGrant?.isChargeAgent ?: (user?.isChargeAgent == true)
        val agencyName = serverGrant?.agencyNameAr?.ifBlank { user?.agencyName.orEmpty() }
            ?: user?.agencyName.orEmpty()
        val chargeCoins = serverGrant?.chargeAgentCoinsBalance ?: (user?.chargeAgentCoinsBalance ?: 0L)
        val micHours = serverGrant?.hostMicHours ?: (user?.hostMicHours ?: 0)
        val activeDays = serverGrant?.hostActiveDays ?: (user?.hostActiveDays ?: 0)
        val giftDiamonds = serverGrant?.hostGiftsDiamonds ?: (user?.hostGiftsDiamonds ?: 0L)

        val visibilityMap = featureControls.associate { it.featureKey to it.isVisibleInApp }.toMutableMap().apply {
            remoteAccess.features.forEach { put(it.featureKey, it.visible) }
        }
        val enabledMap = featureControls.associate { it.featureKey to it.isEnabledForUse }.toMutableMap().apply {
            remoteAccess.features.forEach { put(it.featureKey, it.enabled) }
        }

        AgencyAuthorizationUiState(
            currentUserDisplayId = displayId,
            isHostAgentAuthorized = isHostAgent,
            isApprovedHostAuthorized = isApprovedHost,
            isChargeAgentAuthorized = isChargeAgent,
            agencyNameAr = agencyName,
            chargeAgentCoinsBalance = chargeCoins,
            hostMicHours = micHours,
            hostActiveDays = activeDays,
            hostGiftsDiamonds = giftDiamonds,
            serverFeatureVisibilityMap = visibilityMap,
            serverFeatureEnabledMap = enabledMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AgencyAuthorizationUiState(
            serverFeatureVisibilityMap = MasterAppDatabaseTable.getFeatureControlTable()
                .associate { it.featureKey to it.isVisibleInApp },
            serverFeatureEnabledMap = MasterAppDatabaseTable.getFeatureControlTable()
                .associate { it.featureKey to it.isEnabledForUse }
        )
    )

    /**
     * Binds the currently authenticated user profile so its `displayId` is continuously observed
     * against the server/database agency authorization table.
     */
    fun bindUserProfile(profile: UserProfile) {
        _observedUserProfile.value = profile
    }

    /**
     * Updates Host Agent / Host authorization from the server/database for a specific `displayId`.
     */
    fun setServerHostAgencyAuthorization(
        targetDisplayId: String,
        isHostAgent: Boolean,
        isApprovedHost: Boolean,
        agencyNameAr: String = "وكالة الملوك الرسمية 👑"
    ) {
        MasterAppDatabaseTable.updateHostAgencyRoleById(
            targetDisplayId = targetDisplayId,
            isHostAgent = isHostAgent,
            isHostMember = isApprovedHost,
            agencyNameAr = agencyNameAr
        )
    }

    /**
     * Updates Charge Agent authorization and coin pool from the server/database for a specific `displayId`.
     */
    fun setServerChargeAgencyAuthorization(
        targetDisplayId: String,
        isChargeAgent: Boolean,
        agencyCoinPool: Long
    ) {
        MasterAppDatabaseTable.activateChargeAgentById(
            targetDisplayId = targetDisplayId,
            isChargeAgent = isChargeAgent,
            agencyCoins = agencyCoinPool
        )
    }

    /**
     * Updates server-side visibility flag for any screen, tab, or module key.
     */
    fun setServerFeatureVisibilityFlag(
        featureKey: String,
        isVisible: Boolean,
        isEnabled: Boolean = true
    ) {
        MasterAppDatabaseTable.setFeatureVisibilityFromDatabase(
            featureKey = featureKey,
            isVisible = isVisible,
            isEnabled = isEnabled
        )
    }
}

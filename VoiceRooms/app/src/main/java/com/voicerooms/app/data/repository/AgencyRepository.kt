package com.voicerooms.app.data.repository

import com.voicerooms.app.data.model.Agency
import com.voicerooms.app.data.model.AgencyMember
import com.voicerooms.app.data.remote.SupabaseProvider
import com.voicerooms.app.util.ApiResult
import com.voicerooms.app.util.runCatchingApi
import com.voicerooms.app.util.translateError
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

/**
 * مستودع الوكالات — القائمة، الإنشاء، الأعضاء.
 */
class AgencyRepository {

    private val db get() = SupabaseProvider.db

    suspend fun getAgencies(): ApiResult<List<Agency>> = runCatchingApi {
        db.from("agencies").select(Columns.raw("*, owner:profiles(*)")) {
            filter { eq("is_active", true) }
            order("total_earnings", Order.DESCENDING)
            limit(100)
        }.decodeList<Agency>()
    }.mapError()

    suspend fun getAgency(agencyId: String): ApiResult<Agency> = runCatchingApi {
        db.from("agencies").select(Columns.raw("*, owner:profiles(*)")) {
            filter { eq("id", agencyId) }
        }.decodeSingle<Agency>()
    }.mapError()

    suspend fun getMyAgency(userId: String): ApiResult<Agency?> = runCatchingApi {
        db.from("agencies").select(Columns.raw("*, owner:profiles(*)")) {
            filter { eq("owner_id", userId) }
            limit(1)
        }.decodeList<Agency>().firstOrNull()
    }.mapError()

    suspend fun createAgency(userId: String, name: String, description: String?, logoUrl: String?): ApiResult<Agency> =
        runCatchingApi {
            db.from("agencies").insert(
                mapOf(
                    "owner_id" to userId,
                    "name" to name,
                    "description" to description,
                    "logo_url" to logoUrl,
                )
            ) { select() }.decodeSingle<Agency>()
        }.mapError()

    suspend fun getAgencyMembers(agencyId: String): ApiResult<List<AgencyMember>> = runCatchingApi {
        db.from("agency_members").select(Columns.raw("*, profile:profiles(*)")) {
            filter { eq("agency_id", agencyId) }
            order("joined_at", Order.DESCENDING)
        }.decodeList<AgencyMember>()
    }.mapError()

    suspend fun addMember(agencyId: String, userId: String): ApiResult<Unit> = runCatchingApi {
        db.from("agency_members").insert(
            mapOf("agency_id" to agencyId, "user_id" to userId, "role" to "member")
        )
        db.from("profiles").update(mapOf("agency_id" to agencyId)) { filter { eq("id", userId) } }
        Unit
    }.mapError()

    private fun <T> ApiResult<T>.mapError(): ApiResult<T> =
        if (this is ApiResult.Error) ApiResult.Error(translateError(message), code) else this
}

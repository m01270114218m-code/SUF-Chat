package com.voicerooms.app.data.repository

import com.voicerooms.app.data.model.Gift
import com.voicerooms.app.data.model.GiftTransaction
import com.voicerooms.app.data.remote.SupabaseProvider
import com.voicerooms.app.util.ApiResult
import com.voicerooms.app.util.runCatchingApi
import com.voicerooms.app.util.translateError
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * مستودع الهدايا — القائمة، الإرسال (SVGA/عادي)، وتغذية الهدايا في الغرفة.
 */
class GiftRepository {

    private val db get() = SupabaseProvider.db

    suspend fun getGifts(): ApiResult<List<Gift>> = runCatchingApi {
        db.from("gifts").select {
            filter { eq("is_active", true) }
            order("sort_order", Order.ASCENDING)
        }.decodeList<Gift>()
    }.mapError()

    suspend fun sendGift(
        roomId: String,
        receiverId: String,
        giftId: Int,
        amount: Long = 1L,
    ): ApiResult<Unit> = runCatchingApi {
        db.rpc(
            "send_gift",
            buildJsonObject {
                put("p_room_id", roomId)
                put("p_receiver", receiverId)
                put("p_gift_id", giftId)
                put("p_amount", amount)
            }
        )
        Unit
    }.mapError()

    suspend fun getRoomGiftFeed(roomId: String, limit: Int = 30): ApiResult<List<GiftTransaction>> =
        runCatchingApi {
            db.from("gift_transactions").select(
                Columns.raw("*, gift:gifts(*), sender:profiles!gift_transactions_sender_id_fkey(*), receiver:profiles!gift_transactions_receiver_id_fkey(*)")
            ) {
                filter { eq("room_id", roomId) }
                order("created_at", Order.DESCENDING)
                limit(limit.toLong())
            }.decodeList<GiftTransaction>()
        }.mapError()

    private fun <T> ApiResult<T>.mapError(): ApiResult<T> =
        if (this is ApiResult.Error) ApiResult.Error(translateError(message), code) else this
}

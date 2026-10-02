package com.pharaohparty.app
import android.content.Context
import android.provider.Settings
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
@Serializable data class AuthRequest(val action:String,val username:String?=null,val password:String?=null,val nickname:String?=null,val device_key:String?=null)
@Serializable data class AuthResponse(val access_token:String?=null,val refresh_token:String?=null,val user_id:String?=null,val error:String?=null)
@Serializable data class LiveKitTokenResponse(val server_url:String?=null,val participant_token:String?=null,val error:String?=null)
@Serializable data class Profile(val id:String,val username:String?=null,val display_name:String?=null,val avatar_url:String?=null,val country:String?=null,val coins:Long=0,val diamonds:Long=0,val vip_level:Int=0,val display_id:String?=null,val role_code:String="USER",val is_host_agent:Boolean=false,val is_host_member:Boolean=false,val is_charge_agent:Boolean=false,val is_banned:Boolean=false)
@Serializable data class Room(val id:String,val name:String,val title:String?=null,val host_id:String,val type:String="voice",val privacy:String="public",val country:String="EG",val cover_url:String?=null,val max_seats:Int=8,val is_active:Boolean=true)
@Serializable data class RoomSeat(val room_id:String,val seat_no:Int,val user_id:String?=null,val is_muted:Boolean=false,val locked:Boolean=false,val joined_at:String?=null)
@Serializable data class StoreItem(val id:String,val name:String,val category:String,val price_coins:Long=0,val icon:String?=null,val asset_url:String?=null,val frame_id:String?=null,val entry_welcome_id:String?=null,val enabled:Boolean=true,val is_animated:Boolean=false,val animation_type:String?=null)
class PartyApi(private val context:Context){
 private val json=Json{ignoreUnknownKeys=true;explicitNulls=false}
 private val client=HttpClient(io.ktor.client.engine.android.Android){install(ContentNegotiation){json(json)}}
 var accessToken:String?=null; private set
 var userId:String?=null; private set
 suspend fun auth(request:AuthRequest):AuthResponse{val response=client.post(BuildConfig.SUPABASE_URL+"/functions/v1/"+BuildConfig.AUTH_FUNCTION){contentType(ContentType.Application.Json);header("apikey",BuildConfig.SUPABASE_KEY);setBody(request)};val body=response.body<AuthResponse>();if(!response.status.isSuccess())error(body.error?:"تعذر تسجيل الدخول");if(body.access_token.isNullOrBlank()||body.user_id.isNullOrBlank())error("لم يتم إنشاء جلسة صالحة");accessToken=body.access_token;userId=body.user_id;SessionStore(context).save(body.access_token,body.refresh_token?:"",body.user_id);return body}
 suspend fun restore():Boolean{val s=SessionStore(context).read();if(s.access.isBlank()||s.user.isBlank())return false;accessToken=s.access;userId=s.user;return true}
 suspend fun profile():Profile{val response=get("/rest/v1/profiles?id=eq."+userId+"&select=*");return response.body<List<Profile>>().firstOrNull()?:error("الحساب غير موجود في قاعدة البيانات")}
 suspend fun rooms():List<Room>=get("/rest/v1/rooms?select=*&is_active=eq.true&order=created_at.desc").body()
 suspend fun store():List<StoreItem>=get("/rest/v1/store_items?select=*&enabled=eq.true&order=sort_order.asc").body()
 suspend fun joinRoom(roomId:String, seatNo:Int?=null):RoomSeat{val response=client.post(BuildConfig.SUPABASE_URL+"/rest/v1/rpc/join_room"){header("apikey",BuildConfig.SUPABASE_KEY);header("Authorization","Bearer "+(accessToken?:error("جلسة منتهية")));contentType(ContentType.Application.Json);setBody(mapOf("p_room_id" to roomId,"p_seat_no" to seatNo))};if(!response.status.isSuccess())error(response.bodyAsText());return response.body()}
 suspend fun leaveRoom(roomId:String){val response=client.post(BuildConfig.SUPABASE_URL+"/rest/v1/rpc/leave_room"){header("apikey",BuildConfig.SUPABASE_KEY);header("Authorization","Bearer "+(accessToken?:error("جلسة منتهية")));contentType(ContentType.Application.Json);setBody(mapOf("p_room_id" to roomId))};if(!response.status.isSuccess())error(response.bodyAsText())}
 suspend fun liveKitToken(roomId:String):LiveKitTokenResponse{val response=client.post(BuildConfig.SUPABASE_URL+"/functions/v1/"+BuildConfig.LIVEKIT_TOKEN_FUNCTION){contentType(ContentType.Application.Json);header("apikey",BuildConfig.SUPABASE_KEY);header("Authorization","Bearer "+(accessToken?:error("جلسة منتهية")));setBody(mapOf("room_id" to roomId))};val body=response.body<LiveKitTokenResponse>();if(!response.status.isSuccess())error(body.error?:"تعذر تجهيز اتصال الصوت");return body}
 private suspend fun get(path:String):HttpResponse=client.get(BuildConfig.SUPABASE_URL+path){header("apikey",BuildConfig.SUPABASE_KEY);accessToken?.let{header("Authorization","Bearer "+it)};accept(ContentType.Application.Json)}
 fun deviceKey():String=Settings.Secure.getString(context.contentResolver,Settings.Secure.ANDROID_ID)?:"unknown-device"
}
data class StoredSession(val access:String,val refresh:String,val user:String)
class SessionStore(private val context:Context){private val prefs get()=context.getSharedPreferences("pharaoh_session",Context.MODE_PRIVATE);fun save(access:String,refresh:String,user:String){prefs.edit().putString("access",access).putString("refresh",refresh).putString("user",user).apply()};fun read()=StoredSession(prefs.getString("access","")?:"",prefs.getString("refresh","")?:"",prefs.getString("user","")?:"");fun clear(){prefs.edit().clear().apply()}}
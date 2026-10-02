package com.pharaohparty.app
import android.content.Context
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
class LiveKitManager(context:Context){
 private val appContext=context.applicationContext
 var room:Room?=null
  private set
 suspend fun connect(api:PartyApi,roomId:String):Room{
  val token=api.liveKitToken(roomId)
  val url=token.server_url?:error("LIVEKIT_URL_MISSING")
  val jwt=token.participant_token?:error("LIVEKIT_TOKEN_MISSING")
  val r=LiveKit.create(appContext)
  r.connect(url,jwt)
  room=r
  return r
 }
 fun disconnect(){room?.disconnect();room=null}
}
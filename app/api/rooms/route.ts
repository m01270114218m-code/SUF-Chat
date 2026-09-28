import { NextResponse } from "next/server"
import { supabaseRest } from "@/lib/supabase-rest"
export const dynamic = "force-dynamic"

export async function GET(request: Request) {
  const id = new URL(request.url).searchParams.get("id")
  if (!id) return NextResponse.json({error:"id is required"},{status:400})
  try {
    const rooms=await supabaseRest<any[]>("rooms", `?id=eq.${encodeURIComponent(id)}&select=*`)
    const seats=await supabaseRest<any[]>("room_seats", `?room_id=eq.${encodeURIComponent(id)}&select=*&order=seat_no.asc`)
    const messages=await supabaseRest<any[]>("room_messages", `?room_id=eq.${encodeURIComponent(id)}&select=*&order=created_at.asc&limit=100`)
    return NextResponse.json({room:rooms[0]||null,seats:seats||[],messages:messages||[]})
  } catch(error) { return NextResponse.json({error:error instanceof Error?error.message:"Supabase error"},{status:502}) }
}

export async function POST(request: Request) {
  const body=await request.json()
  const action=body?.action
  try {
    if(action==="message"){ await supabaseRest("room_messages","",{method:"POST",headers:{Prefer:"return=minimal"},body:JSON.stringify({room_id:body.roomId,user_id:body.userId,message:String(body.message||"").slice(0,2000)})}); return NextResponse.json({ok:true}) }
    if(action==="join"){ const result=await supabaseRest("rpc/join_room","",{method:"POST",body:JSON.stringify({p_room_id:body.roomId,p_seat_no:body.seatNo})}); return NextResponse.json(result) }
    if(action==="leave"){ const result=await supabaseRest("rpc/leave_room","",{method:"POST",body:JSON.stringify({p_room_id:body.roomId})}); return NextResponse.json(result) }
    if(action==="mute"){ const result=await supabaseRest("rpc/set_my_mute","",{method:"POST",body:JSON.stringify({p_room_id:body.roomId,p_muted:Boolean(body.muted)})}); return NextResponse.json(result) }
    return NextResponse.json({error:"unsupported action"},{status:400})
  } catch(error) { return NextResponse.json({error:error instanceof Error?error.message:"Supabase error"},{status:502}) }
}
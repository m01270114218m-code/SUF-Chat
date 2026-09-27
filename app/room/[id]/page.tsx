"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { supabase } from "@/lib/supabase";
import VoiceClient from "@/components/VoiceClient";

type Message={id:string;message:string;created_at:string;profiles?:{display_name:string}|null};
type Seat={seat_no:number;user_id:string|null;is_muted:boolean;locked:boolean;profiles?:{display_name:string;avatar_url:string|null}|null};

export default function RoomPage(){
 const {id}=useParams<{id:string}>(); const router=useRouter();
 const [room,setRoom]=useState<any>(null); const [seats,setSeats]=useState<Seat[]>([]); const [messages,setMessages]=useState<Message[]>([]); const [text,setText]=useState(""); const [userId,setUserId]=useState<string|null>(null); const [joined,setJoined]=useState(false);
 useEffect(()=>{load(); const ch=supabase.channel("room-"+id).on("postgres_changes",{event:"*",schema:"public",table:"room_messages",filter:"room_id=eq."+id},load).on("postgres_changes",{event:"*",schema:"public",table:"room_seats",filter:"room_id=eq."+id},load).subscribe(); return()=>{supabase.removeChannel(ch)}},[id]);
 async function load(){const {data:{user}}=await supabase.auth.getUser();setUserId(user?.id??null);const [r,s,m]=await Promise.all([supabase.from("rooms").select("*").eq("id",id).single(),supabase.from("room_seats").select("*,profiles(display_name,avatar_url)").eq("room_id",id).order("seat_no"),supabase.from("room_messages").select("*,profiles(display_name)").eq("room_id",id).order("created_at",{ascending:true}).limit(100)]);setRoom(r.data);setSeats((s.data??[]) as Seat[]);setMessages((m.data??[]) as Message[]);setJoined(!!user&&((s.data??[]) as any[]).some(x=>x.user_id===user.id));}
 async function send(){if(!userId||!text.trim())return; await supabase.from("room_messages").insert({room_id:id,user_id:userId,message:text.trim()});setText("");}\n async function join(){const {error}=await supabase.rpc("join_room",{p_room_id:id,p_seat_no:null});if(error){alert(error.message);return}setJoined(true);load()}\n async function leave(){await supabase.rpc("leave_room",{p_room_id:id});setJoined(false);load()}
 if(!room)return <main className="room"><button onClick={()=>router.back()}>← رجوع</button><div className="empty">جاري تحميل الغرفة...</div></main>;
 return <main className="room"><header><button className="icon" onClick={()=>router.back()}>←</button><div><b>{room.name}</b><span>{room.title||"غرفة صوتية"}</span></div><span className="live">● مباشر</span></header><VoiceClient roomId={id} userId={userId} enabled={joined}/><section className="room-body"><div className="seats">{seats.map(s=><div className={"seat "+(s.user_id?"filled":"")} key={s.seat_no}><div className="seat-avatar">{s.profiles?.avatar_url?<img src={s.profiles.avatar_url} alt=""/>:"🎙️"}</div><small>{s.user_id?s.profiles?.display_name||"عضو":"مقعد "+s.seat_no}</small>{s.is_muted&&<i>🔇</i>}</div>)}</div><div className="chat-box">{messages.map(m=><div className="msg" key={m.id}><b>{m.profiles?.display_name||"مستخدم"}</b><span>{m.message}</span></div>)}</div></section><footer className="room-actions">{joined?<button onClick={leave}>خروج</button>:<button onClick={join}>🎙️ دخول المقعد</button>}<input value={text} onChange={e=>setText(e.target.value)} onKeyDown={e=>e.key==="Enter"&&send()} placeholder="اكتب رسالة..." /><button onClick={send}>إرسال</button><button>🎁</button><button>🎤</button></footer></main>;
}

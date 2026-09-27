"use client";
import { useEffect,useState } from "react";
import { useParams,useRouter } from "next/navigation";
import { supabase } from "@/lib/supabase";
type Seat={seat_no:number;user_id:string|null;is_muted:boolean;locked:boolean;profiles?:{display_name:string;avatar_url:string|null}|null};
type Message={id:string;message:string;created_at:string;profiles?:{display_name:string}|null};
export default function RoomPage(){
 const {id}=useParams<{id:string}>(),router=useRouter();
 const [room,setRoom]=useState<any>(null),[seats,setSeats]=useState<Seat[]>([]),[messages,setMessages]=useState<Message[]>([]),[text,setText]=useState(""),[uid,setUid]=useState<string|null>(null),[joined,setJoined]=useState(false),[notice,setNotice]=useState("");
 async function load(){
  const {data:{user}}=await supabase.auth.getUser();setUid(user?.id??null);
  const [r,s,m]=await Promise.all([supabase.from("rooms").select("*").eq("id",id).maybeSingle(),supabase.from("room_seats").select("seat_no,user_id,is_muted,locked").eq("room_id",id).order("seat_no"),supabase.from("room_messages").select("id,message,created_at,profiles(display_name)").eq("room_id",id).order("created_at",{ascending:true}).limit(100)]);
  const raw=(s.data??[]) as Seat[];const ids=raw.map(x=>x.user_id).filter(Boolean) as string[];const map:Record<string,{display_name:string;avatar_url:string|null}>={};
  if(ids.length){const p=await supabase.from("public_profiles").select("id,display_name,avatar_url").in("id",ids);for(const x of p.data??[])map[x.id]=x}
  setRoom(r.data);setSeats(raw.map(x=>({...x,profiles:x.user_id?map[x.user_id]:null})));setMessages((m.data??[]) as Message[]);setJoined(!!user&&raw.some(x=>x.user_id===user.id));
 }
 useEffect(()=>{load();const ch=supabase.channel("room-"+id).on("postgres_changes",{event:"*",schema:"public",table:"room_messages",filter:"room_id=eq."+id},load).on("postgres_changes",{event:"*",schema:"public",table:"room_seats",filter:"room_id=eq."+id},load).subscribe();return()=>{supabase.removeChannel(ch)}},[id]);
 async function join(){const {error}=await supabase.rpc("join_room",{p_room_id:id,p_seat_no:null});if(error)setNotice(error.message);else{setJoined(true);setNotice("تم دخول المقعد");load()}}
 async function leave(){await supabase.rpc("leave_room",{p_room_id:id});setJoined(false);load()}
 async function mute(){const next=!Boolean(seats.find(s=>s.user_id===uid)?.is_muted);await supabase.rpc("set_my_mute",{p_room_id:id,p_muted:next});load()}
 async function send(){if(!uid||!text.trim())return;const {error}=await supabase.from("room_messages").insert({room_id:id,user_id:uid,message:text.trim()});if(error)setNotice(error.message);else setText("")}
 if(!room)return <main className="room"><button onClick={()=>router.back()}>← رجوع</button><div className="empty">جاري تحميل الغرفة...</div></main>;
 const seated=seats.filter(s=>s.user_id);
 return <main className="room"><header><button className="icon" onClick={()=>router.back()}>←</button><div><b>{room.name}</b><span>{room.title||"غرفة صوتية"} · {seated.length}/{room.max_seats}</span></div><span className="live">● مباشر</span></header><section className="room-body"><div className="seats">{seats.map(s=><div className={"seat "+(s.user_id?"filled":"")} key={s.seat_no}><div className="seat-avatar">{s.profiles?.avatar_url?<img src={s.profiles.avatar_url} alt=""/>:"🎙️"}</div><small>{s.user_id?s.profiles?.display_name||"عضو":"مقعد "+s.seat_no}</small>{s.is_muted&&<i>🔇</i>}</div>)}</div><div className="voice-status">{joined?"أنت داخل الغرفة. فعّل الميكروفون عند إضافة WebRTC/مزود الصوت.":"اضغط دخول المقعد."}</div><div className="chat-box">{messages.map(m=><div className="msg" key={m.id}><b>{m.profiles?.display_name||"مستخدم"}</b><span>{m.message}</span></div>)}</div></section>{notice&&<div className="toast">{notice}</div>}<footer className="room-actions">{joined?<><button onClick={mute}>🎙️ كتم/تشغيل</button><button onClick={leave}>خروج</button></>:<button onClick={join}>🎙️ دخول المقعد</button>}<input value={text} onChange={e=>setText(e.target.value)} onKeyDown={e=>e.key==="Enter"&&send()} placeholder="اكتب رسالة..."/><button onClick={send}>إرسال</button></footer></main>;
}

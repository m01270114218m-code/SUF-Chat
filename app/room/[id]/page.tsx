"use client";

import { useEffect, useRef, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { supabase } from "@/lib/supabase";

type Seat={seat_no:number;user_id:string|null;is_muted:boolean;locked:boolean;profiles?:{display_name:string;avatar_url:string|null}|null};
type Message={id:string;message:string;created_at:string;profiles?:{display_name:string}|null};
type Gift={id:string;name:string;icon:string;price_coins:number};

export default function RoomPage(){
 const {id}=useParams<{id:string}>(),router=useRouter();
 const [room,setRoom]=useState<any>(null),[seats,setSeats]=useState<Seat[]>([]),[messages,setMessages]=useState<Message[]>([]);
 const [text,setText]=useState(""),[uid,setUid]=useState<string|null>(null),[joined,setJoined]=useState(false),[muted,setMuted]=useState(false),[notice,setNotice]=useState("");
 const [gifts,setGifts]=useState<Gift[]>([]),[showGifts,setShowGifts]=useState(false);
 const channelRef=useRef<any>(null), peers=useRef<Record<string,RTCPeerConnection>>({}), streamRef=useRef<MediaStream|null>(null);
 const [remoteStreams,setRemoteStreams]=useState<Record<string,MediaStream>>({});

 async function refresh(){
  const [r,s,m,g,u]=await Promise.all([
   supabase.from("rooms").select("*").eq("id",id).maybeSingle(),
   supabase.from("room_seats").select("seat_no,user_id,is_muted,locked").eq("room_id",id).order("seat_no"),
   supabase.from("room_messages").select("id,message,created_at,profiles(display_name)").eq("room_id",id).order("created_at",{ascending:true}).limit(100),
   supabase.from("gifts").select("id,name,icon,price_coins").eq("enabled",true).order("sort_order"),
   supabase.auth.getUser()
  ]);
  setRoom(r.data);\n  const rawSeats=(s.data??[]) as Seat[];\n  const ids=rawSeats.map(x=>x.user_id).filter(Boolean) as string[];\n  let profileMap:Record<string,{display_name:string;avatar_url:string|null}>={};\n  if(ids.length){const pr=await supabase.from("public_profiles").select("id,display_name,avatar_url").in("id",ids);for(const p of pr.data??[])profileMap[p.id]=p;}\n  setSeats(rawSeats.map(x=>({...x,profiles:x.user_id?profileMap[x.user_id]:null})));setMessages((m.data??[]) as Message[]);setGifts((g.data??[]) as Gift[]);setUid(u.data.user?.id??null);
  if(u.data.user) setJoined((s.data??[]).some((x:any)=>x.user_id===u.data.user.id));
 }
 async function join(){
  const {data,error}=await supabase.rpc("join_room",{p_room_id:id,p_seat_no:null});
  if(error){setNotice(error.message);return}
  setJoined(true);setNotice("تم دخول المقعد");
  if(data) await startVoice();
  refresh();
 }
 async function leave(){
  if(uid) await supabase.rpc("leave_room",{p_room_id:id});
  stopVoice();setJoined(false);refresh();
 }
 async function toggleMute(){
  const next=!muted;setMuted(next);streamRef.current?.getAudioTracks().forEach(t=>t.enabled=!next);
  await supabase.rpc("set_my_mute",{p_room_id:id,p_muted:next});refresh();
 }
 async function send(){
  if(!text.trim()||!uid)return;
  const {error}=await supabase.from("room_messages").insert({room_id:id,user_id:uid,message:text.trim()});
  if(error)setNotice(error.message);else setText("");
 }
 async function sendGift(g:Gift){
  const receiver=seats.find(s=>s.user_id&&s.user_id!==uid)?.user_id;
  if(!receiver){setNotice("لا يوجد متحدث آخر لإرسال الهدية");return}
  const {error}=await supabase.rpc("send_room_gift",{p_room_id:id,p_receiver_id:receiver,p_gift_id:g.id,p_quantity:1});
  setNotice(error?error.message:"تم إرسال "+g.icon+" "+g.name);setShowGifts(false);refresh();
 }

 function sendSignal(payload:any){channelRef.current?.send({type:"broadcast",event:"voice-signal",payload}).catch(()=>{});}
 function makePeer(remoteId:string,initiator:boolean){
  if(peers.current[remoteId])return peers.current[remoteId];
  const pc=new RTCPeerConnection({iceServers:[{urls:"stun:stun.l.google.com:19302"},{urls:"stun:stun1.l.google.com:19302"}]});
  peers.current[remoteId]=pc;
  streamRef.current?.getTracks().forEach(t=>pc.addTrack(t,streamRef.current!));
  pc.onicecandidate=e=>{if(e.candidate)sendSignal({kind:"candidate",from:uid,to:remoteId,candidate:e.candidate})};
  pc.ontrack=e=>{const st=e.streams[0];if(st)setRemoteStreams(x=>({...x,[remoteId]:st}))};
  pc.onconnectionstatechange=()=>{if(["failed","closed","disconnected"].includes(pc.connectionState)){pc.close();delete peers.current[remoteId];setRemoteStreams(x=>{const y={...x};delete y[remoteId];return y})}};
  if(initiator)pc.createOffer().then(o=>pc.setLocalDescription(o)).then(()=>sendSignal({kind:"offer",from:uid,to:remoteId,sdp:pc.localDescription}));
  return pc;
 }
 async function startVoice(){
  if(!uid||streamRef.current)return;
  try{
   streamRef.current=await navigator.mediaDevices.getUserMedia({audio:{echoCancellation:true,noiseSuppression:true,autoGainControl:true},video:false});
   streamRef.current.getAudioTracks().forEach(t=>t.enabled=!muted);
  }catch(e){setNotice("تم دخول المقعد، لكن المتصفح رفض الميكروفون.");return}
 }
 function stopVoice(){
  Object.values(peers.current).forEach(p=>p.close());peers.current={};
  streamRef.current?.getTracks().forEach(t=>t.stop());streamRef.current=null;setRemoteStreams({});
 }

 useEffect(()=>{
  refresh();
  const db=supabase.channel("room-db-"+id).on("postgres_changes",{event:"*",schema:"public",table:"room_messages",filter:"room_id=eq."+id},refresh).on("postgres_changes",{event:"*",schema:"public",table:"room_seats",filter:"room_id=eq."+id},refresh).subscribe();
  const voice=supabase.channel("voice-"+id);
  channelRef.current=voice;
  voice.on("broadcast",{event:"voice-signal"},async({payload}:any)=>{
   if(!payload||payload.to!==uid||payload.from===uid)return;
   if(payload.kind==="hello"){if(uid&&uid<payload.from)makePeer(payload.from,true);return;}\n   const pc=makePeer(payload.from,false);
   try{
    if(payload.kind==="offer"){await pc.setRemoteDescription(payload.sdp);const answer=await pc.createAnswer();await pc.setLocalDescription(answer);sendSignal({kind:"answer",from:uid,to:payload.from,sdp:pc.localDescription})}
    else if(payload.kind==="answer")await pc.setRemoteDescription(payload.sdp);
    else if(payload.kind==="candidate"&&payload.candidate)await pc.addIceCandidate(payload.candidate);
   }catch{}
  }).subscribe(status=>{if(status==="SUBSCRIBED"&&uid)sendSignal({kind:"hello",from:uid,to:"*"});});
  return()=>{supabase.removeChannel(db);supabase.removeChannel(voice);if(uid)void supabase.rpc("leave_room",{p_room_id:id});stopVoice()};
 },[id,uid]);

 useEffect(()=>{if(!uid||!channelRef.current)return;const t=setTimeout(()=>sendSignal({kind:"hello",from:uid,to:"*"}),300);return()=>clearTimeout(t)},[uid]);

 useEffect(()=>{
  const fn=(e:BeforeUnloadEvent)=>{if(uid)navigator.sendBeacon?.("/api/leave-room",JSON.stringify({roomId:id}))};
  window.addEventListener("beforeunload",fn);return()=>window.removeEventListener("beforeunload",fn)
 },[id,uid]);

 if(!room)return <main className="room"><button onClick={()=>router.back()}>← رجوع</button><div className="empty">جاري تحميل الغرفة...</div></main>;

 const seated=seats.filter(s=>s.user_id);
 return <main className="room">
  <header><button className="icon" onClick={()=>router.back()}>←</button><div><b>{room.name}</b><span>{room.title||"غرفة صوتية"} · {seated.length}/{room.max_seats}</span></div><span className="live">● مباشر</span></header>
  <section className="room-body">
   <div className="seats">{seats.map(s=><div className={"seat "+(s.user_id?"filled":"")} key={s.seat_no}><div className="seat-avatar">{s.profiles?.avatar_url?<img src={s.profiles.avatar_url} alt=""/>:"🎙️"}</div><small>{s.user_id?s.profiles?.display_name||"عضو":"مقعد "+s.seat_no}</small>{s.is_muted&&<i>🔇</i>}</div>)}</div>
   <div className="voice-status">{joined?"🎙️ أنت داخل الغرفة — الصوت المباشر متصل عند السماح بالميكروفون.":"اضغط «دخول المقعد» للمشاركة بالصوت."}</div>
   {Object.entries(remoteStreams).map(([peer,st])=><audio key={peer} autoPlay ref={el=>{if(el)el.srcObject=st}} />)}
   <div className="chat-box">{messages.map(m=><div className="msg" key={m.id}><b>{m.profiles?.display_name||"مستخدم"}</b><span>{m.message}</span></div>)}</div>
  </section>
  {notice&&<div className="toast">{notice}</div>}
  {showGifts&&<div className="modal"><div className="modal-card"><h3>الهدايا</h3><div className="gift-grid">{gifts.map(g=><button key={g.id} onClick={()=>sendGift(g)}>{g.icon}<b>{g.name}</b><small>{g.price_coins} 🪙</small></button>)}</div><button onClick={()=>setShowGifts(false)}>إغلاق</button></div></div>}
  <footer className="room-actions">{joined?<button onClick={toggleMute}>{muted?"🔇 تشغيل الميك":"🎙️ كتم"}</button>:<button onClick={join}>🎙️ دخول المقعد</button>}<input value={text} onChange={e=>setText(e.target.value)} onKeyDown={e=>e.key==="Enter"&&send()} placeholder="اكتب رسالة..."/><button onClick={send}>إرسال</button><button onClick={()=>setShowGifts(true)}>🎁</button>{joined&&<button onClick={leave}>خروج</button>}</footer>
 </main>;
}

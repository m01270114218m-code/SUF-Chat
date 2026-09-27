"use client";
import { useEffect, useState } from "react";
import type { CSSProperties } from "react";
import Link from "next/link";
import { supabase } from "@/lib/supabase";

type Profile={id:string;display_name:string;avatar_url:string|null;country:string;coins:number;diamonds:number;vip_level:number};
type Room={id:string;name:string;title:string|null;country:string;cover_url:string|null;max_seats:number;host_id:string};
type Person={id:string;display_name:string;avatar_url:string|null};
type Note={id:string;title:string;body:string|null;read_at:string|null;created_at:string};
type Theme={app_name:string;primary_color:string;secondary_color:string;background_color:string;surface_color:string;text_color:string;muted_text_color:string;border_color:string;accent_color:string;radius:string;logo_url:string|null;app_icon_url:string|null;background_image_url:string|null};
type Asset={area:string;asset_key:string;image_url:string|null;label:string|null;enabled:boolean};

export default function Home(){
 const [tab,setTab]=useState<"rooms"|"chat"|"notifications"|"me">("rooms");
 const [rooms,setRooms]=useState<Room[]>([]),[profile,setProfile]=useState<Profile|null>(null),[userId,setUserId]=useState<string|null>(null);
 const [email,setEmail]=useState(""),[password,setPassword]=useState(""),[phone,setPhone]=useState(""),[busy,setBusy]=useState(false),[notice,setNotice]=useState("");
 const [people,setPeople]=useState<Person[]>([]),[to,setTo]=useState(""),[messages,setMessages]=useState<any[]>([]),[message,setMessage]=useState("");
 const [notes,setNotes]=useState<Note[]>([]),[showCreate,setShowCreate]=useState(false),[roomName,setRoomName]=useState(""),[roomTitle,setRoomTitle]=useState(""),[roomSeats,setRoomSeats]=useState(8);
 const [theme,setTheme]=useState<Theme|null>(null),[assets,setAssets]=useState<Record<string,Asset>>({});
 const icon=(area:string,key:string,emoji:string)=>{const a=assets[area+":"+key];return a?.enabled&&a.image_url?<img src={a.image_url} alt={a.label||""}/>:emoji};

 useEffect(()=>{load();const {data}=supabase.auth.onAuthStateChange(()=>load());return()=>data.subscription.unsubscribe()},[]);
 async function load(){
  const {data:{user}}=await supabase.auth.getUser(); setUserId(user?.id??null);
  if(!user){setProfile(null);return}
  const [p,r,ps,n,dm]=await Promise.all([
   supabase.from("profiles").select("id,display_name,avatar_url,country,coins,diamonds,vip_level").eq("id",user.id).maybeSingle(),
   supabase.from("rooms").select("id,name,title,country,cover_url,max_seats,host_id").eq("is_active",true).order("created_at",{ascending:false}).limit(50),
   supabase.from("profiles").select("id,display_name,avatar_url").neq("id",user.id).order("display_name").limit(100),
   supabase.from("notifications").select("id,title,body,read_at,created_at").eq("user_id",user.id).order("created_at",{ascending:false}).limit(50),
   supabase.from("direct_messages").select("id,sender_id,receiver_id,message,created_at").or("sender_id.eq."+user.id+",receiver_id.eq."+user.id).order("created_at",{ascending:false}).limit(100)
  ]);
  setProfile(p.data as Profile|null);setRooms((r.data??[]) as Room[]);setPeople((ps.data??[]) as Person[]);setNotes((n.data??[]) as Note[]);setMessages((dm.data??[]).reverse());
 }
 async function auth(){
  if(!email||password.length<6){setNotice("اكتب البريد وكلمة المرور بشكل صحيح");return}
  setBusy(true);setNotice("");
  const {error}=await supabase.auth.signInWithPassword({email,password});
  if(error){
   const created=await supabase.auth.signUp({email,password,options:{data:{display_name:email.split("@")[0]}}});
   setNotice(created.error?created.error.message:"تم إنشاء الحساب. أكّد البريد إذا كان التأكيد مطلوبًا.");
  }else {localStorage.setItem("suf_quick_email",email);setNotice("تم تسجيل الدخول");}
  setBusy(false);await load();
 }
 async function phoneAuth(){
  if(!/^\+?[1-9]\d{7,14}$/.test(phone)){setNotice("اكتب رقم الهاتف بصيغة دولية مثل +201xxxxxxxxx");return}
  if(password.length<6){setNotice("كلمة المرور 6 أحرف على الأقل");return}
  setBusy(true);setNotice("");
  const {error}=await supabase.auth.signInWithPassword({phone,password});
  if(error){
   const created=await supabase.auth.signUp({phone,password,options:{data:{display_name:phone}}});
   setNotice(created.error?"تعذر تسجيل الهاتف: "+created.error.message:"تم إنشاء الحساب. أكّد رمز الهاتف إذا طُلب.");
  }else setNotice("تم تسجيل الدخول");
  setBusy(false);await load();
 }
 async function social(provider:"google"|"facebook"){
  setBusy(true);setNotice("");
  const {error}=await supabase.auth.signInWithOAuth({provider,options:{redirectTo:window.location.origin}});
  if(error){setNotice(error.message);setBusy(false)}
 }
 async function quickLogin(){
  setBusy(true);setNotice("");
  const {data,error}=await supabase.auth.signInAnonymously({options:{data:{display_name:"مستخدم سريع"}}});
  if(error){
   setNotice(error.message.includes("Anonymous")||error.message.includes("anonymous")
    ?"الدخول السريع يحتاج تفعيل Anonymous Sign-Ins من إعدادات Supabase: Authentication → Sign In / Providers."
    :"تعذر إنشاء الحساب السريع: "+error.message);
  }else if(data.user){
   setUserId(data.user.id);
   setNotice("تم إنشاء حسابك السريع وتسجيل الدخول");
   await load();
  }
  setBusy(false);
 }
 async function createRoom(){
  const {data:{user}}=await supabase.auth.getUser(); if(!user||!roomName.trim())return;
  const {error}=await supabase.from("rooms").insert({name:roomName.trim(),title:roomTitle.trim()||null,host_id:user.id,max_seats:Math.max(2,Math.min(16,roomSeats)),country:profile?.country||"EG"});
  setNotice(error?.message||"تم إنشاء الغرفة");if(!error){setShowCreate(false);setRoomName("");setRoomTitle("");load()}
 }
 async function sendDM(){
  const {data:{user}}=await supabase.auth.getUser();if(!user||!to||!message.trim())return;
  const {error}=await supabase.from("direct_messages").insert({sender_id:user.id,receiver_id:to,message:message.trim()});
  if(error)setNotice(error.message);else{setMessage("");load()}
 }
 async function markRead(id:string){await supabase.from("notifications").update({read_at:new Date().toISOString()}).eq("id",id);load()}
 async function signOut(){await supabase.auth.signOut();setProfile(null);setUserId(null)}
 const themeStyle=theme?({["--primary" as any]:theme.primary_color,["--secondary" as any]:theme.secondary_color,["--bg" as any]:theme.background_color,["--surface" as any]:theme.surface_color,["--text" as any]:theme.text_color,["--muted" as any]:theme.muted_text_color,["--border" as any]:theme.border_color,["--accent" as any]:theme.accent_color,["--radius" as any]:theme.radius,backgroundImage:theme.background_image_url?`url(${theme.background_image_url})`:undefined} as CSSProperties):undefined;
 if(!userId)return <main className="auth" style={themeStyle}><div className="auth-card"><div className="logo">{theme?.logo_url?<img src={theme.logo_url} alt={theme.app_name}/>:theme?.app_name||"SUF"}</div><h1>غرف صوتية حقيقية</h1><p>تسجيل، غرف، شات، إشعارات وصوت مباشر.</p><input value={email} onChange={e=>setEmail(e.target.value)} placeholder="البريد الإلكتروني" type="email" autoComplete="email"/><input value={phone} onChange={e=>setPhone(e.target.value)} placeholder="رقم الهاتف +20..." type="tel" autoComplete="tel"/><input value={password} onChange={e=>setPassword(e.target.value)} placeholder="كلمة المرور" type="password" autoComplete="current-password"/><div className="row"><button onClick={auth} disabled={busy}>دخول بالبريد</button><button onClick={phoneAuth} disabled={busy}>دخول بالهاتف</button></div><div className="row"><button onClick={()=>social("google")} disabled={busy}>Google</button><button onClick={()=>social("facebook")} disabled={busy}>Facebook</button></div><button onClick={quickLogin} disabled={busy}>⚡ إنشاء حساب ودخول سريع</button>{notice&&<small>{notice}</small>}</div></main>;

 return <main className="app">
  <header><div><b>SUF</b><span>{tab==="rooms"?"الغرف الصوتية":tab==="chat"?"الشات":tab==="notifications"?"الإشعارات":"حسابي"}</span></div><div className="header-actions">{tab==="rooms"&&<button onClick={()=>setShowCreate(true)}>{icon("home","create_room","＋")} غرفة</button>}<button className="icon" onClick={load}>{icon("home","refresh","↻")}</button></div></header>
  <section className="content">
   {tab==="rooms"&&<><div className="hero"><div><h1>الغرف الآن</h1><p>انضم وابدأ الكلام بالصوت فورًا.</p></div><span className="live">● مباشر</span></div><div className="room-grid">{rooms.map(r=><Link className="room-card" href={"/room/"+r.id} key={r.id}><div className="cover" style={r.cover_url?{backgroundImage:"url("+r.cover_url+")"}:{}}><span>{icon("home","room_default","🎙️")}</span></div><div className="room-info"><b>{r.name}</b><small>{r.title||"غرفة صوتية"}</small><small>🇪🇬 {r.country} · {r.max_seats} مقعد</small></div></Link>)}{!rooms.length&&<div className="empty">لا توجد غرف نشطة. أنشئ أول غرفة.</div>}</div></>}
   {tab==="chat"&&<div className="panel"><h2>الرسائل الخاصة</h2><select value={to} onChange={e=>setTo(e.target.value)}><option value="">اختر مستخدمًا</option>{people.map(p=><option key={p.id} value={p.id}>{p.display_name}</option>)}</select><div className="chat-list">{messages.map(m=><div className={m.sender_id===userId?"msg mine":"msg"} key={m.id}><span>{m.message}</span></div>)}{!messages.length&&<div className="empty">لا توجد رسائل بعد.</div>}</div><div className="composer"><input value={message} onChange={e=>setMessage(e.target.value)} onKeyDown={e=>e.key==="Enter"&&sendDM()} placeholder="اكتب رسالة..."/><button onClick={sendDM}>إرسال</button></div></div>}
   {tab==="notifications"&&<div className="panel"><h2>الإشعارات</h2>{notes.map(n=><button className={"notification "+(!n.read_at?"unread":"")} key={n.id} onClick={()=>markRead(n.id)}><b>{n.title}</b><span>{n.body}</span><small>{new Date(n.created_at).toLocaleString("ar-EG")}</small></button>)}{!notes.length&&<div className="empty">لا توجد إشعارات.</div>}</div>}
   {tab==="me"&&<div className="panel profile"><div className="avatar">{profile?.avatar_url?<img src={profile.avatar_url} alt=""/>:"👤"}</div><h2>{profile?.display_name||"مستخدم جديد"}</h2><p>VIP {profile?.vip_level??0} · {profile?.country||"EG"}</p><div className="stats"><span>🪙 {profile?.coins??0}<small>عملات</small></span><span>💎 {profile?.diamonds??0}<small>ألماس</small></span></div><button onClick={signOut}>تسجيل الخروج</button></div>}
  </section>
  <nav><button className={tab==="rooms"?"active":""} onClick={()=>setTab("rooms")}>{icon("home","home","🏠")}<small>الرئيسية</small></button><button className={tab==="chat"?"active":""} onClick={()=>setTab("chat")}>{icon("home","chat","💬")}<small>الشات</small></button><button className={tab==="notifications"?"active":""} onClick={()=>setTab("notifications")}>{icon("home","notifications","🔔")}<small>الإشعارات</small></button><button className={tab==="me"?"active":""} onClick={()=>setTab("me")}>{icon("home","profile","👤")}<small>حسابي</small></button></nav>
  {showCreate&&<div className="modal"><div className="modal-card"><h2>إنشاء غرفة</h2><input value={roomName} onChange={e=>setRoomName(e.target.value)} placeholder="اسم الغرفة"/><input value={roomTitle} onChange={e=>setRoomTitle(e.target.value)} placeholder="الوصف"/><input value={roomSeats} onChange={e=>setRoomSeats(Number(e.target.value))} type="number" min="2" max="16" placeholder="المقاعد"/><div className="row"><button onClick={createRoom}>إنشاء</button><button onClick={()=>setShowCreate(false)}>إلغاء</button></div></div></div>}
 </main>;
}

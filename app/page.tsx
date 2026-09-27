"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { supabase } from "@/lib/supabase";

type Room = { id:string; name:string; title:string|null; country:string; cover_url:string|null; max_seats:number; host_id:string; };
type Profile = { id:string; display_name:string; avatar_url:string|null; coins:number; diamonds:number; vip_level:number; };

export default function Home() {
  const [tab,setTab]=useState<"rooms"|"chat"|"notifications"|"me">("rooms");
  const [rooms,setRooms]=useState<Room[]>([]);
  const [profile,setProfile]=useState<Profile|null>(null);
  const [userId,setUserId]=useState<string|null>(null);
  const [email,setEmail]=useState("");
  const [password,setPassword]=useState("");
  const [busy,setBusy]=useState(false);
  const [notice,setNotice]=useState("");

  useEffect(()=>{ load(); const {data}=supabase.auth.onAuthStateChange(()=>load()); return ()=>data.subscription.unsubscribe(); },[]);
  async function load(){
    const {data:{user}}=await supabase.auth.getUser();
    setUserId(user?.id ?? null);
    if(user){
      const p=await supabase.from("profiles").select("*").eq("id",user.id).maybeSingle();
      setProfile(p.data as Profile|null);
    }
    const r=await supabase.from("rooms").select("id,name,title,country,cover_url,max_seats,host_id").eq("is_active",true).order("created_at",{ascending:false}).limit(30);
    setRooms((r.data ?? []) as Room[]);
  }
  async function auth(){
    setBusy(true); setNotice("");
    const res=await supabase.auth.signInWithPassword({email,password});
    if(res.error){
      const sign=await supabase.auth.signUp({email,password});
      setNotice(sign.error ? sign.error.message : "تم إنشاء الحساب. افحص بريدك إذا كان تأكيد البريد مفعّلًا.");
    } else setNotice("تم تسجيل الدخول");
    setBusy(false); load();
  }
  async function signOut(){ await supabase.auth.signOut(); setProfile(null); setUserId(null); }
  const title=useMemo(()=>({rooms:"الغرف الصوتية",chat:"الشات",notifications:"الإشعارات",me:"حسابي"}[tab]),[tab]);

  if(!userId) return <main className="auth"><div className="auth-card"><div className="logo">SUF</div><h1>غرف صوتية وشات</h1><p>ادخل وابدأ التواصل مع الناس في غرفتك المفضلة.</p><input value={email} onChange={e=>setEmail(e.target.value)} placeholder="البريد الإلكتروني" type="email"/><input value={password} onChange={e=>setPassword(e.target.value)} placeholder="كلمة المرور" type="password"/><button onClick={auth} disabled={busy}>{busy?"جاري الدخول...":"دخول / إنشاء حساب"}</button>{notice&&<small>{notice}</small>}</div></main>;

  return <main className="app">
    <header><div><b>SUF</b><span>{title}</span></div><button className="icon" onClick={load}>↻</button></header>
    <section className="content">
      {tab==="rooms" && <><div className="hero"><div><h1>الغرف الآن</h1><p>اختر غرفة وانضم للمحادثة الصوتية.</p></div><span className="live">● مباشر</span></div><div className="room-grid">{rooms.map(r=><Link className="room-card" href={"/room/"+r.id} key={r.id}><div className="cover" style={r.cover_url?{backgroundImage:"url("+r.cover_url+")"}:{}}><span>🎙️</span></div><div className="room-info"><b>{r.name}</b><small>{r.title||"غرفة صوتية"}</small><small>🇪🇬 {r.country} · حتى {r.max_seats} مقاعد</small></div></Link>)}{rooms.length===0&&<div className="empty">لا توجد غرف نشطة حاليًا.</div>}</div></>}
      {tab==="chat" && <div className="panel"><h2>الرسائل</h2><p>المحادثات الخاصة تظهر هنا.</p><div className="empty">ابدأ بمتابعة مستخدمين من الغرف لإنشاء محادثات.</div></div>}
      {tab==="notifications" && <div className="panel"><h2>الإشعارات</h2><div className="empty">لا توجد إشعارات جديدة.</div></div>}
      {tab==="me" && <div className="panel profile"><div className="avatar">{profile?.avatar_url?<img src={profile.avatar_url} alt=""/>:"👤"}</div><h2>{profile?.display_name||"مستخدم جديد"}</h2><p>VIP {profile?.vip_level??0}</p><div className="stats"><span>🪙 {profile?.coins??0}<small>عملات</small></span><span>💎 {profile?.diamonds??0}<small>ألماس</small></span></div><button onClick={signOut}>تسجيل الخروج</button></div>}
    </section>
    <nav><button className={tab==="rooms"?"active":""} onClick={()=>setTab("rooms")}>🏠<small>الرئيسية</small></button><button className={tab==="chat"?"active":""} onClick={()=>setTab("chat")}>💬<small>الشات</small></button><button className={tab==="notifications"?"active":""} onClick={()=>setTab("notifications")}>🔔<small>الإشعارات</small></button><button className={tab==="me"?"active":""} onClick={()=>setTab("me")}>👤<small>حسابي</small></button></nav>
  </main>;
}

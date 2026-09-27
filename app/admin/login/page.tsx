"use client";

import { FormEvent, useState } from "react";
import { createBrowserClient } from "@supabase/ssr";

const supabase = createBrowserClient(process.env.NEXT_PUBLIC_SUPABASE_URL!, process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!);

export default function AdminLoginPage() {
  const [email,setEmail]=useState(""); const [password,setPassword]=useState(""); const [error,setError]=useState(""); const [busy,setBusy]=useState(false);
  async function submit(e:FormEvent){
    e.preventDefault(); setBusy(true); setError("");
    const {error}=await supabase.auth.signInWithPassword({email,password});
    if(error){setError(error.message);setBusy(false);return;}
    window.location.href="/admin";
  }
  return <main className="admin-shell" style={{maxWidth:520,margin:"40px auto"}}><section className="admin-card"><small>PHARAOH PARTY CONTROL</small><h1>دخول لوحة التحكم</h1><p>الدخول يتم بحساب Supabase حقيقي، ولا توجد كلمة مرور ثابتة داخل المشروع.</p><form onSubmit={submit} style={{display:"grid",gap:14}}><label>البريد الإلكتروني<input type="email" required value={email} onChange={e=>setEmail(e.target.value)}/></label><label>كلمة المرور<input type="password" required value={password} onChange={e=>setPassword(e.target.value)}/></label><button disabled={busy}>{busy?"جارٍ الدخول…":"دخول آمن"}</button>{error&&<div className="admin-message">{error}</div>}</form></section></main>;
}

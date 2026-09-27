"use client";

import { useEffect, useState } from "react";

type Item={id?:string;area:string;item_key:string;item_type:string;label:string;visible:boolean;enabled:boolean;sort_order:number;asset_url?:string|null;config:Record<string,unknown>};
export default function AdminPage(){
 const [settings,setSettings]=useState<any>(null); const [items,setItems]=useState<Item[]>([]); const [assets,setAssets]=useState<any[]>([]); const [msg,setMsg]=useState("");
 const load=()=>Promise.all([fetch("/api/party/settings").then(r=>r.json()),fetch("/api/party/design").then(r=>r.json()),fetch("/api/party/assets").then(r=>r.json())]).then(([s,d,a])=>{setSettings(s);setItems(d.items??[]);setAssets(a.assets??[])});
 useEffect(()=>{load()},[]);
 const saveSettings=async()=>{setMsg("الحفظ يحتاج جلسة مدير مصادق عليها وسيتم تفعيلها مع نظام Auth");};
 if(!settings)return <main className="admin-shell"><h1>Pharaoh Party Admin</h1><p>جاري تحميل إعدادات قاعدة البيانات…</p></main>;
 const setFeature=(k:string,v:boolean)=>setSettings((s:any)=>({...s,feature_visibility:{...s.feature_visibility,[k]:v}}));
 return <main className="admin-shell"><header className="admin-header"><div><small>PHARAOH PARTY CONTROL</small><h1>لوحة التحكم والتصميم</h1></div><button onClick={saveSettings}>حفظ</button></header>
 <section className="admin-card"><h2>هوية التطبيق</h2><div className="form-grid"><label>اسم التطبيق<input value={settings.design?.app_name??""} onChange={e=>setSettings((s:any)=>({...s,design:{...s.design,app_name:e.target.value}}))}/></label><label>اللون الرئيسي<input value={settings.design?.primary_color??""} onChange={e=>setSettings((s:any)=>({...s,design:{...s.design,primary_color:e.target.value}}))}/></label><label>الخلفية<input value={settings.design?.background??""} onChange={e=>setSettings((s:any)=>({...s,design:{...s.design,background:e.target.value}}))}/></label></div></section>
 <section className="admin-card"><h2>الأقسام</h2><div className="toggle-grid">{Object.entries(settings.feature_visibility??{}).map(([k,v])=><button key={k} className={v?"on":"off"} onClick={()=>setFeature(k,!v as boolean)}>{k}: {v?"ظاهر":"مخفي"}</button>)}</div></section>
 <section className="admin-card"><h2>عناصر الواجهات</h2><p>كل عنصر يمكن التحكم في ظهوره وتفعيله وترتيبه ورابط الأصل الخاص به.</p>{items.map((x,i)=><div className="design-row" key={x.area+"/"+x.item_key}><div><b>{x.label}</b><small>{x.area} · {x.item_type} · {x.item_key}</small></div><button onClick={()=>setItems(a=>a.map((z,j)=>j===i?{...z,visible:!z.visible}:z))}>{x.visible?"إخفاء":"إظهار"}</button><button onClick={()=>setItems(a=>a.map((z,j)=>j===i?{...z,enabled:!z.enabled}:z))}>{x.enabled?"تعطيل":"تفعيل"}</button></div>)}</section>
 <section className="admin-card"><h2>الأصول</h2>{assets.map((a:any)=><div className="asset-row" key={a.id}><b>{a.name}</b><span>{a.category}</span><input placeholder="رابط الأصل" defaultValue={a.asset_url??""}/></div>)}</section>
 {msg&&<div className="admin-message">{msg}</div>}</main>;
}

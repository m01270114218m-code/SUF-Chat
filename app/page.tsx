"use client";

import { useEffect, useState } from "react";

type Settings = {
  feature_visibility: Record<string, boolean>;
  design: { app_name?: string; theme?: string; primary_color?: string; background?: string };
  content: { home_title?: string; welcome_message?: string };
  maintenance_mode: boolean;
  global_announcement: string;
};

type DesignItem = { area: string; item_key: string; item_type: string; label: string; visible: boolean; enabled: boolean; sort_order: number; config: Record<string, unknown>; asset_url?: string | null };

const fallback: Settings = {
  feature_visibility: { store: true, agency_center: false, recharge: true, rooms: true, gifts: true, vip: true, profile_frames: true, mic_skins: true },
  design: { app_name: "Pharaoh Party", theme: "dark", primary_color: "#7c3aed", background: "#09090b" },
  content: { home_title: "Pharaoh Party", welcome_message: "مرحباً بك في فرعون بارتي" },
  maintenance_mode: false,
  global_announcement: "",
};

export default function Home() {
  const [settings, setSettings] = useState<Settings>(fallback);
  const [items, setItems] = useState<DesignItem[]>([]);

  useEffect(() => {
    Promise.all([fetch("/api/party/settings").then(r => r.ok ? r.json() : null), fetch("/api/party/design").then(r => r.ok ? r.json() : null)])
      .then(([s, d]) => { if (s) setSettings(s); if (d?.items) setItems(d.items); })
      .catch(() => undefined);
  }, []);

  const v = settings.feature_visibility;
  const d = settings.design;
  const item = (area: string, key: string) => items.find(x => x.area === area && x.item_key === key && x.visible);

  if (settings.maintenance_mode) return <main className="party-shell" style={{ "--accent": d.primary_color } as React.CSSProperties}><section className="maintenance"><div className="crown">👑</div><h1>{d.app_name}</h1><h2>التطبيق تحت الصيانة</h2><p>سنعود قريباً.</p></section></main>;

  return <main className="party-shell" style={{ "--accent": d.primary_color, "--party-bg": d.background } as React.CSSProperties}>
    <header className="party-header"><div><small>VOICE PARTY</small><h1>{d.app_name}</h1></div><button className="icon-button">☰</button></header>
    {settings.global_announcement && <div className="announcement">📢 {settings.global_announcement}</div>}

    {item("home", "hero") && <section className="party-hero"><div><span className="badge">✦ LIVE PARTY</span><h2>{settings.content.home_title}</h2><p>{settings.content.welcome_message}</p><div className="hero-actions">{v.rooms && <button>🎙️ الغرف</button>}{v.store && <button>🛍️ المتجر</button>}{v.recharge && <button>💎 شحن</button>}</div></div><div className="hero-crown">👑</div></section>}

    {v.rooms && item("home", "rooms") && <section className="party-section"><div className="section-title"><div><small>LIVE NOW</small><h2>الغرف المباشرة</h2></div><button>عرض الكل</button></div><div className="room-grid"><article className="room-card"><div className="room-cover">👑</div><div><b>الغرفة الملكية</b><p>🎙️ 12 متصل · ✦ VIP</p></div></article><article className="room-card"><div className="room-cover">🎉</div><div><b>ليلة فرعون</b><p>🎙️ 8 متصل · هدايا مباشرة</p></div></article><article className="room-card"><div className="room-cover">🔥</div><div><b>Party Lounge</b><p>🎙️ 24 متصل</p></div></article></div></section>}

    <section className="feature-strip">
      {v.gifts && item("room", "gift_panel") && <div>🎁<b>الهدايا</b><span>مؤثرات وهدايا داخل الغرفة</span></div>}
      {v.vip && <div>👑<b>VIP</b><span>مزايا ومستويات مميزة</span></div>}
      {v.profile_frames && item("profile", "frame") && <div>🖼️<b>الإطارات</b><span>إطارات شفافة ثلاثية الأبعاد</span></div>}
      {v.mic_skins && item("room", "mic_style") && <div>🎙️<b>الميكروفونات</b><span>أشكال قابلة للتبديل</span></div>}
    </section>

    <nav className="bottom-nav"><button>⌂<span>الرئيسية</span></button>{v.rooms && <button>🎙<span>الغرف</span></button>}{v.store && <button>🛍<span>المتجر</span></button>}<button>👤<span>حسابي</span></button></nav>
  </main>;
}

"use client";

import { useEffect, useState } from "react";

export default function RoomPage({ params }: { params: { id: string } }) {
  const [room, setRoom] = useState<any>(null);
  const [assets, setAssets] = useState<any[]>([]);
  const [design, setDesign] = useState<any[]>([]);

  useEffect(() => {
    Promise.all([
      fetch("/api/party/rooms").then(r => r.ok ? r.json() : null),
      fetch("/api/party/assets").then(r => r.ok ? r.json() : null),
      fetch("/api/party/design").then(r => r.ok ? r.json() : null)
    ]).then(([r, a, d]) => {
      setRoom(r?.rooms?.find((x: any) => x.room_key === params.id) ?? r?.rooms?.[0] ?? null);
      setAssets(a?.assets ?? []);
      setDesign(d?.items ?? []);
    });
  }, [params.id]);

  const cfg = room?.config ?? {};
  const mic = assets.find(x => x.category === "mic" && x.name.includes(cfg.mic_style === "luxury_3d" ? "فاخر" : "ملكي"));
  const bg = assets.find(x => x.category === "background");
  const has = (area: string, key: string) => design.some(x => x.area === area && x.item_key === key && x.visible && x.enabled);

  return <main className="party-room" style={{ backgroundImage: bg?.asset_url ? `url(${bg.asset_url})` : undefined }}>
    <header className="room-header"><button>‹</button><div><small>LIVE ROOM</small><h1>{room?.name ?? "الغرفة الملكية"}</h1></div><button>⋮</button></header>
    <div className="room-info"><span>👑 VIP</span><span>🎙️ {cfg.seats ?? 9} مقاعد</span><span>🟢 مباشر</span></div>
    <section className="mic-grid">{Array.from({ length: Number(cfg.seats ?? 9) }).map((_, i) => <div className="mic-seat" key={i}><div className="avatar-ring"><div className="avatar">{i === 0 ? "👑" : "🙂"}</div>{has("profile", "frame") && <div className="frame-overlay">✦</div>}</div>{has("room", "mic_style") && <div className="mic-skin">{mic?.asset_url ? <img src={mic.asset_url} alt="mic" /> : "🎙️"}</div>}<b>{i === 0 ? "مضيف الغرفة" : `عضو ${i}`}</b></div>)}</section>
    {has("room", "entrance_animation") && <div className="entrance-effect">✦ دخول ملكي ✦</div>}
    {has("room", "gift_panel") && <section className="gift-bar"><button>🎁</button><button>👑</button><button>💎</button><button>🔥</button><button className="send">إرسال</button></section>}
    <footer className="room-footer"><button>🎤</button><button>💬</button><button>🎁</button><button>👥</button><button>⚙️</button></footer>
  </main>;
}

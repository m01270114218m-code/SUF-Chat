"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";
const items=[
  ["الرئيسية","/party","⌂"],
  ["اكتشف","/party/discover","◈"],
  ["الرسائل","/party/messages","✉"],
  ["أنا","/party/me","◉"],
] as const;
export default function BottomNav(){const path=usePathname();return <nav className="bottom-nav">{items.map(([label,href,icon])=><Link key={href} href={href} className={path===href?"active":""}><span>{icon}</span><small>{label}</small></Link>)}</nav>}

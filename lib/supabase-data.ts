import { supabaseRest } from "@/lib/supabase-rest"
import { defaultConfig } from "@/lib/default-config"
import type { AppConfig, LiveRoom, RechargePackage } from "@/lib/types"

type DbTheme = { app_name:string; primary_color:string; secondary_color:string; accent_color:string; background_color:string; logo_url:string|null; app_icon_url:string|null; background_image_url:string|null }
type DbAsset = { area:string; asset_key:string; image_url:string|null; label:string|null; enabled:boolean; sort_order:number }
type DbRoom = { id:string; name:string; title:string|null; country:string; cover_url:string|null; max_seats:number; is_active:boolean; host_id:string; type:string }
type DbPackage = { id:string; coins:number|string; bonus:number|string; price_usd:number|string; popular:boolean; enabled:boolean }

export async function loadRemoteAppConfig(): Promise<AppConfig> {
  const [themeRows, assetRows, roomRows, packageRows] = await Promise.all([
    supabaseRest<DbTheme[]>("ui_theme", "?id=eq.1&select=*&limit=1"),
    supabaseRest<DbAsset[]>("ui_assets", "?select=area,asset_key,image_url,label,enabled,sort_order&order=area.asc,sort_order.asc"),
    supabaseRest<DbRoom[]>("rooms", "?is_active=eq.true&select=*&order=created_at.desc"),
    supabaseRest<DbPackage[]>("recharge_packages", "?enabled=eq.true&select=*&order=coins.asc"),
  ])
  const theme = themeRows[0]
  const assets = assetRows || []
  const roomDefault = assets.find(a => a.area === "home" && a.asset_key === "room_default")?.image_url
  const rooms: LiveRoom[] = (roomRows || []).map((r, i) => ({
    id: r.id, title: r.title || r.name, host: r.name,
    cover: r.cover_url || roomDefault || defaultConfig.liveRooms[i % defaultConfig.liveRooms.length]?.cover || "/room-1.png",
    country: r.country === "EG" ? "مصر" : r.country, countryFlag: r.country === "EG" ? "🇪🇬" : "🌍",
    viewers: 0, tag: r.type === "voice" ? "دردشة" : r.type, hot: i === 0,
  }))
  return {
    ...defaultConfig,
    theme: theme ? { ...defaultConfig.theme, appName: theme.app_name, primary: theme.primary_color, secondary: theme.secondary_color, accent: theme.accent_color, roomBackground: theme.background_color } : defaultConfig.theme,
    liveRooms: rooms.length ? rooms : defaultConfig.liveRooms,
    rechargePackages: (packageRows || []).map((p): RechargePackage => ({ id:p.id, coins:Number(p.coins), bonus:Number(p.bonus), priceUSD:Number(p.price_usd), popular:p.popular })),
  }
}

export async function loadAssets() { return supabaseRest<DbAsset[]>("ui_assets", "?select=*&order=area.asc,sort_order.asc") }
export async function loadGifts() { return supabaseRest("gifts", "?enabled=eq.true&select=*&order=sort_order.asc") }
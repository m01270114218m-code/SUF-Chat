import { NextResponse } from "next/server"
import { getConfig, setConfig, resetConfig } from "@/lib/config-store"
import type { AppConfig } from "@/lib/types"
import { supabaseRest } from "@/lib/supabase-rest"
import { loadRemoteAppConfig } from "@/lib/supabase-data"

export const dynamic = "force-dynamic"

async function readRemoteConfig(): Promise<AppConfig | null> {
  try {
    const remote = await loadRemoteAppConfig()
    const stored = await supabaseRest<Array<{ config: AppConfig }>>("app_config", "?id=eq.global&select=config&limit=1")
    const base = stored[0]?.config
    return { ...remote, ...(base || {}), theme: { ...remote.theme, ...(base?.theme || {}) }, liveRooms: remote.liveRooms, rechargePackages: remote.rechargePackages }
  } catch { return null }
}

export async function GET() { return NextResponse.json((await readRemoteConfig()) || getConfig()) }

export async function POST(request: Request) {
  const body = (await request.json()) as { action?: string; config?: AppConfig }
  if (body.action === "reset") return NextResponse.json(resetConfig())
  if (body.config) {
    setConfig(body.config)
    try { await supabaseRest("app_config", "?on_conflict=id", { method:"POST", headers:{ Prefer:"resolution=merge-duplicates,return=minimal" }, body:JSON.stringify({ id:"global", config:body.config }) }) } catch {}
    return NextResponse.json(body.config)
  }
  return NextResponse.json((await readRemoteConfig()) || getConfig())
}
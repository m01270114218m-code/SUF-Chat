import { NextResponse } from "next/server"
import { getConfig, setConfig, resetConfig } from "@/lib/config-store"
import type { AppConfig } from "@/lib/types"
import { supabaseRest } from "@/lib/supabase-rest"

export const dynamic = "force-dynamic"

async function readRemoteConfig(): Promise<AppConfig | null> {
  try {
    const rows = await supabaseRest<Array<{ config: AppConfig }>>(
      "app_config",
      "?id=eq.global&select=config",
    )
    return rows[0]?.config ?? null
  } catch {
    return null
  }
}

export async function GET() {
  return NextResponse.json((await readRemoteConfig()) ?? getConfig())
}

export async function POST(request: Request) {
  const body = (await request.json()) as { action?: string; config?: AppConfig }

  if (body.action === "reset") {
    const config = resetConfig()
    return NextResponse.json(config)
  }

  if (body.config) {
    setConfig(body.config)
    try {
      await supabaseRest("app_config", "?on_conflict=id", {
        method: "POST",
        headers: { Prefer: "resolution=merge-duplicates,return=minimal" },
        body: JSON.stringify({ id: "global", config: body.config }),
      })
    } catch {
      // The local store remains the fallback when the database policy rejects the write.
    }
    return NextResponse.json(body.config)
  }

  return NextResponse.json((await readRemoteConfig()) ?? getConfig())
}

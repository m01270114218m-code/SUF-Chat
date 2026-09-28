import { NextResponse } from "next/server"
import { getConfig, setConfig, resetConfig } from "@/lib/config-store"
import type { AppConfig } from "@/lib/types"

export async function GET() {
  return NextResponse.json(getConfig())
}

export async function POST(request: Request) {
  const body = (await request.json()) as { action?: string; config?: AppConfig }
  if (body.action === "reset") {
    return NextResponse.json(resetConfig())
  }
  if (body.config) {
    setConfig(body.config)
    return NextResponse.json(body.config)
  }
  return NextResponse.json(getConfig())
}

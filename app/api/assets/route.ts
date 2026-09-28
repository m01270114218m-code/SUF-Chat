import { NextResponse } from "next/server"
import { loadAssets, loadGifts } from "@/lib/supabase-data"
export const dynamic = "force-dynamic"
export async function GET() {
  try { const [assets,gifts] = await Promise.all([loadAssets(),loadGifts()]); return NextResponse.json({assets,gifts}) }
  catch (error) { return NextResponse.json({ error:error instanceof Error ? error.message : "Supabase error" }, {status:502}) }
}
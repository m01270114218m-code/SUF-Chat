import { NextResponse } from "next/server"
import { supabaseAdmin } from "@/lib/supabase-admin"

const TABLES = new Set(["profiles","rooms","gifts","recharge_packages","vip_subscriptions","recharge_orders","ui_assets","ui_theme","app_settings","app_config"])

export async function GET(req: Request) {
  try {
    const table = new URL(req.url).searchParams.get("table") || ""
    if (!TABLES.has(table)) return NextResponse.json({ error: "جدول غير مسموح" }, { status: 400 })
    const query = new URL(req.url).searchParams.get("query") || "select=*"
    return NextResponse.json(await supabaseAdmin(`${table}?${query}`))
  } catch (e) { return NextResponse.json({ error: e instanceof Error ? e.message : "فشل الطلب" }, { status: 500 }) }
}

export async function POST(req: Request) {
  try {
    const body = await req.json()
    if (!TABLES.has(String(body.table))) return NextResponse.json({ error: "جدول غير مسموح" }, { status: 400 })
    return NextResponse.json(await supabaseAdmin(String(body.table), { method: "POST", headers: { Prefer: "return=representation" }, body: JSON.stringify(body.data) }))
  } catch (e) { return NextResponse.json({ error: e instanceof Error ? e.message : "فشل الحفظ" }, { status: 500 }) }
}

export async function PATCH(req: Request) {
  try {
    const body = await req.json()
    if (!TABLES.has(String(body.table)) || !body.filter) return NextResponse.json({ error: "بيانات التعديل غير صحيحة" }, { status: 400 })
    return NextResponse.json(await supabaseAdmin(`${body.table}?${body.filter}`, { method: "PATCH", headers: { Prefer: "return=representation" }, body: JSON.stringify(body.data) }))
  } catch (e) { return NextResponse.json({ error: e instanceof Error ? e.message : "فشل التعديل" }, { status: 500 }) }
}

export async function DELETE(req: Request) {
  try {
    const body = await req.json()
    if (!TABLES.has(String(body.table)) || !body.filter) return NextResponse.json({ error: "بيانات الحذف غير صحيحة" }, { status: 400 })
    return NextResponse.json(await supabaseAdmin(`${body.table}?${body.filter}`, { method: "DELETE", headers: { Prefer: "return=representation" } }))
  } catch (e) { return NextResponse.json({ error: e instanceof Error ? e.message : "فشل الحذف" }, { status: 500 }) }
}

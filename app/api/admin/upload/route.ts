import { NextResponse } from "next/server"
export const runtime = "nodejs"

export async function POST(req: Request) {
  const url = process.env.NEXT_PUBLIC_SUPABASE_URL
  const key = process.env.SUPABASE_SERVICE_ROLE_KEY
  if (!url || !key) return NextResponse.json({ error: "SUPABASE_SERVICE_ROLE_KEY غير مضبوط في بيئة الخادم" }, { status: 503 })
  const form = await req.formData()
  const file = form.get("file")
  const folder = String(form.get("folder") || "ui")
  if (!(file instanceof File)) return NextResponse.json({ error: "لم يتم إرسال ملف" }, { status: 400 })
  if (file.size > 25 * 1024 * 1024) return NextResponse.json({ error: "الحد الأقصى 25MB" }, { status: 400 })
  const safeName = file.name.replace(/[^a-zA-Z0-9._-]/g, "-")
  const path = `${folder}/${Date.now()}-${safeName}`
  const r = await fetch(`${url}/storage/v1/object/app-assets/${path}`, { method:"POST", headers:{apikey:key,Authorization:`Bearer ${key}`,"Content-Type":file.type||"application/octet-stream","x-upsert":"true"}, body:await file.arrayBuffer() })
  if (!r.ok) return NextResponse.json({ error: await r.text() }, { status: 500 })
  return NextResponse.json({ path, publicUrl:`${url}/storage/v1/object/public/app-assets/${path}` })
}

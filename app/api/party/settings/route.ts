import { NextResponse } from "next/server";
import { createClient } from "@supabase/supabase-js";

export async function GET() {
  const url = process.env.NEXT_PUBLIC_SUPABASE_URL;
  const key = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY;
  if (!url || !key) return NextResponse.json({ error: "Supabase environment is not configured" }, { status: 500 });
  const supabase = createClient(url, key);
  const { data, error } = await supabase.from("admin_control_settings").select("feature_visibility,design,content,maintenance_mode,global_announcement").eq("id", true).maybeSingle();
  if (error) return NextResponse.json({ error: error.message }, { status: 500 });
  return NextResponse.json(data ?? { feature_visibility: {}, design: {}, content: {}, maintenance_mode: false, global_announcement: "" }, { headers: { "Cache-Control": "no-store" } });
}

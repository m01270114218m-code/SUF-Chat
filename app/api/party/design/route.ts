import { NextResponse } from "next/server";
import { createClient } from "@supabase/supabase-js";

export async function GET() {
  const url = process.env.NEXT_PUBLIC_SUPABASE_URL;
  const key = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY;
  if (!url || !key) return NextResponse.json({ error: "Supabase environment is not configured" }, { status: 500 });
  const supabase = createClient(url, key);
  const { data, error } = await supabase.from("pharaoh_design_items").select("area,item_key,item_type,label,visible,enabled,sort_order,config,asset_url").eq("visible", true).order("area").order("sort_order");
  if (error) return NextResponse.json({ error: error.message }, { status: 500 });
  return NextResponse.json({ items: data ?? [] }, { headers: { "Cache-Control": "no-store" } });
}

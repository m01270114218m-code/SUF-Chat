import { NextResponse } from "next/server";
import { createClient } from "@supabase/supabase-js";

export async function GET() {
  const url = process.env.NEXT_PUBLIC_SUPABASE_URL;
  const key = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY;

  if (!url || !key) {
    return NextResponse.json(
      { posts: [], error: "Supabase environment is not configured" },
      { status: 500 },
    );
  }

  const supabase = createClient(url, key);
  const { data, error } = await supabase
    .from("pharaoh_posts")
    .select("*")
    .order("created_at", { ascending: false })
    .limit(50);

  if (error) {
    return NextResponse.json(
      { posts: [], error: error.message },
      { status: 500 },
    );
  }

  return NextResponse.json(
    { posts: data ?? [] },
    { headers: { "Cache-Control": "no-store" } },
  );
}

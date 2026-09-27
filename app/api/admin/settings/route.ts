import { NextResponse } from "next/server";
import { createClient } from "../../../../lib/supabase/server";

async function adminClient() {
  const supabase = await createClient();
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) return { supabase, user: null, admin: null };
  const { data: admin } = await supabase.from("pharaoh_admins").select("user_id,role,active").eq("user_id", user.id).eq("active", true).maybeSingle();
  return { supabase, user, admin };
}

export async function GET() {
  const { supabase, user, admin } = await adminClient();
  if (!user || !admin) return NextResponse.json({ error: "غير مصرح" }, { status: 403 });
  const { data, error } = await supabase.from("admin_control_settings").select("id,feature_visibility,design,content,maintenance_mode,global_announcement").eq("id", true).single();
  if (error) return NextResponse.json({ error: error.message }, { status: 500 });
  return NextResponse.json(data);
}

export async function PATCH(req: Request) {
  const { supabase, user, admin } = await adminClient();
  if (!user || !admin || !["owner", "admin", "editor"].includes(admin.role)) return NextResponse.json({ error: "غير مصرح" }, { status: 403 });
  const body = await req.json();
  const allowed = { feature_visibility: body.feature_visibility, design: body.design, content: body.content, maintenance_mode: body.maintenance_mode, global_announcement: body.global_announcement };
  const { data, error } = await supabase.from("admin_control_settings").update(allowed).eq("id", true).select("*").single();
  if (error) return NextResponse.json({ error: error.message }, { status: 500 });
  return NextResponse.json(data);
}

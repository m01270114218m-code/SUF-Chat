const URL = process.env.NEXT_PUBLIC_SUPABASE_URL
const KEY = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY

export async function supabaseAdmin<T = unknown>(path: string, init: RequestInit = {}): Promise<T> {
  if (!URL || !KEY) throw new Error("Supabase environment variables are not configured")
  const headers = new Headers(init.headers)
  headers.set("apikey", KEY)
  headers.set("Authorization", `Bearer ${KEY}`)
  headers.set("Content-Type", headers.get("Content-Type") || "application/json")
  const res = await fetch(`${URL}/rest/v1/${path}`, { ...init, headers, cache: "no-store" })
  const text = await res.text()
  if (!res.ok) throw new Error(text || `Supabase request failed: ${res.status}`)
  return (text ? JSON.parse(text) : null) as T
}

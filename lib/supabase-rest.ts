const SUPABASE_URL = process.env.NEXT_PUBLIC_SUPABASE_URL!
const SUPABASE_KEY = process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY!

function headers(extra: Record<string, string> = {}) {
  return {
    apikey: SUPABASE_KEY,
    Authorization: `Bearer ${SUPABASE_KEY}`,
    "Content-Type": "application/json",
    ...extra,
  }
}

export async function supabaseRest<T = unknown>(
  table: string,
  params = "",
  init: RequestInit = {},
): Promise<T> {
  if (!SUPABASE_URL || !SUPABASE_KEY) {
    throw new Error("Supabase environment variables are missing")
  }

  const response = await fetch(`${SUPABASE_URL}/rest/v1/${table}${params}`, {
    ...init,
    headers: headers((init.headers ?? {}) as Record<string, string>),
    cache: "no-store",
  })

  if (!response.ok) {
    throw new Error(`Supabase REST ${response.status}: ${await response.text()}`)
  }

  return (await response.json()) as T
}
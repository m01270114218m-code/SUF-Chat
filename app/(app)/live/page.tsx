"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Camera, Mic, Radio, Lock, Globe, ChevronRight } from "lucide-react"
import { useConfig } from "@/components/config-provider"

const TYPES = [
  { id: "voice", label: "غرفة صوتية", desc: "دردشة صوتية مع 8 مقاعد مايك", icon: Mic },
  { id: "live", label: "بث فيديو", desc: "بث مباشر بالكاميرا", icon: Camera },
  { id: "party", label: "حفلة", desc: "غرفة ترفيهية جماعية", icon: Radio },
]

export default function LivePage() {
  const router = useRouter()
  const { config } = useConfig()
  const [title, setTitle] = useState("")
  const [type, setType] = useState("voice")
  const [privacy, setPrivacy] = useState<"public" | "private">("public")
  const [country, setCountry] = useState(config.countries[0]?.name ?? "مصر")

  function start() {
    const room = config.liveRooms[0]
    router.push(`/room/${room?.id ?? "r1"}`)
  }

  return (
    <div className="min-h-screen pb-28">
      <header className="px-4 pt-6 pb-4">
        <h1 className="text-2xl font-bold">بدء البث المباشر</h1>
        <p className="text-sm text-muted-foreground mt-1">أنشئ غرفتك وابدأ استقبال الجمهور والهدايا</p>
      </header>

      <div className="px-4 space-y-5">
        <div className="space-y-2">
          <label className="text-sm font-medium">عنوان الغرفة</label>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="اكتب عنوانًا جذابًا لغرفتك"
            className="w-full rounded-2xl border border-border bg-card px-4 py-3 text-sm outline-none focus:border-[var(--app-primary)]"
          />
        </div>

        <div className="space-y-2">
          <label className="text-sm font-medium">نوع البث</label>
          <div className="grid gap-2">
            {TYPES.map((t) => {
              const Icon = t.icon
              const active = type === t.id
              return (
                <button
                  key={t.id}
                  onClick={() => setType(t.id)}
                  className={`flex items-center gap-3 rounded-2xl border p-3 text-right transition ${
                    active ? "border-[var(--app-primary)] bg-[var(--app-primary)]/10" : "border-border bg-card"
                  }`}
                >
                  <span
                    className="grid size-11 place-items-center rounded-xl text-white"
                    style={{ background: `linear-gradient(135deg, var(--app-primary), var(--app-secondary))` }}
                  >
                    <Icon className="size-5" />
                  </span>
                  <div className="flex-1">
                    <p className="text-sm font-semibold">{t.label}</p>
                    <p className="text-xs text-muted-foreground">{t.desc}</p>
                  </div>
                  <span
                    className={`size-4 rounded-full border-2 ${active ? "border-[var(--app-primary)] bg-[var(--app-primary)]" : "border-muted-foreground"}`}
                  />
                </button>
              )
            })}
          </div>
        </div>

        <div className="space-y-2">
          <label className="text-sm font-medium">الدولة</label>
          <div className="flex flex-wrap gap-2">
            {config.countries.map((c) => (
              <button
                key={c.code}
                onClick={() => setCountry(c.name)}
                className={`rounded-full border px-3 py-1.5 text-sm ${
                  country === c.name ? "border-[var(--app-primary)] bg-[var(--app-primary)]/10" : "border-border bg-card"
                }`}
              >
                {c.flag} {c.name}
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2">
          <button
            onClick={() => setPrivacy("public")}
            className={`flex items-center justify-center gap-2 rounded-2xl border p-3 text-sm font-medium ${
              privacy === "public" ? "border-[var(--app-primary)] bg-[var(--app-primary)]/10" : "border-border bg-card"
            }`}
          >
            <Globe className="size-4" /> عامة
          </button>
          <button
            onClick={() => setPrivacy("private")}
            className={`flex items-center justify-center gap-2 rounded-2xl border p-3 text-sm font-medium ${
              privacy === "private" ? "border-[var(--app-primary)] bg-[var(--app-primary)]/10" : "border-border bg-card"
            }`}
          >
            <Lock className="size-4" /> خاصة
          </button>
        </div>

        <button
          onClick={start}
          className="flex w-full items-center justify-center gap-2 rounded-2xl py-4 text-base font-bold text-white shadow-lg"
          style={{ background: `linear-gradient(135deg, var(--app-primary), var(--app-secondary))` }}
        >
          بدء البث الآن <ChevronRight className="size-5" />
        </button>
      </div>
    </div>
  )
}

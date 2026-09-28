"use client"

import { Bell, UserPlus, Heart, MessageCircle, Share2, Gift, Gem, Diamond } from "lucide-react"
import { useConfig } from "@/components/config-provider"

const tags = ["شحن وتفعيل", "اقتباس", "متاح شحن 24ساعه"]

export default function MomentsPage() {
  const { config } = useConfig()

  return (
    <main className="px-4 pt-6">
      <div className="flex items-center justify-between">
        <Bell className="size-6" />
        <div className="flex items-center gap-3">
          <h1 className="text-2xl font-extrabold">
            الحالة <span className="text-[var(--app-primary)]">💫</span>
          </h1>
          <span className="text-sm text-muted-foreground">يتبع</span>
        </div>
      </div>

      {/* tags card */}
      <div className="mt-4 space-y-2 rounded-2xl bg-gradient-to-br from-sky-100 to-fuchsia-100 p-3">
        {tags.map((t) => (
          <div key={t} className="flex items-center justify-end gap-2 text-sm">
            <span className="font-semibold text-fuchsia-700">#{t}</span>
            <span className="grid size-8 place-items-center rounded-md bg-white/70">💎</span>
          </div>
        ))}
      </div>

      {/* post */}
      <article className="mt-5">
        <div className="flex items-start justify-between">
          <button className="grid size-9 place-items-center rounded-full bg-muted" aria-label="متابعة">
            <UserPlus className="size-4" />
          </button>
          <div className="flex items-center gap-2">
            <div className="text-right">
              <p className="text-sm font-bold">نور 🦋</p>
              <div className="mt-1 flex items-center justify-end gap-1">
                <span className="flex items-center gap-0.5 rounded-full bg-emerald-500 px-1.5 text-[10px] font-bold text-white">
                  <Gem className="size-2.5" />2
                </span>
                <span className="flex items-center gap-0.5 rounded-full bg-sky-500 px-1.5 text-[10px] font-bold text-white">
                  <Diamond className="size-2.5" />0
                </span>
                <span className="rounded-full bg-purple-500 px-1.5 text-[10px] font-bold text-white">VIP2</span>
              </div>
            </div>
            <img src="/avatar-user.png" alt="" className="size-11 rounded-full object-cover" />
          </div>
        </div>

        <p className="mt-3 text-right text-sm leading-relaxed">
          حبيبي نور عيني انت كل حاجه ليا حلوه في الدنيا دي انت سندي وانت حبي انت احلى شيء حصل لي في حياتي بحبك وبموت 💕
        </p>

        <div className="mt-3 aspect-[3/4] overflow-hidden rounded-2xl">
          <img src="/room-1.png" alt="لحظة" className="size-full object-cover" />
        </div>

        <p className="mt-2 text-right text-xs text-muted-foreground">2026-09-21 · مصر</p>

        <div className="mt-3 flex items-center justify-between">
          <button className="flex items-center gap-1 text-sm font-semibold text-[var(--app-primary)]">
            <Gift className="size-5" /> تقديم الهدايا
          </button>
          <div className="flex items-center gap-4 text-muted-foreground">
            <span className="flex items-center gap-1 text-sm">
              <Heart className="size-5" /> 4
            </span>
            <span className="flex items-center gap-1 text-sm">
              <MessageCircle className="size-5" /> 0
            </span>
            <Share2 className="size-5" />
          </div>
        </div>
      </article>
    </main>
  )
}

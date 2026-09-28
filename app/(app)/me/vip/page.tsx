"use client"

import Link from "next/link"
import { ChevronRight, Crown, Check } from "lucide-react"
import { useConfig } from "@/components/config-provider"

export default function VipPage() {
  const { config } = useConfig()

  return (
    <div className="min-h-screen bg-gradient-to-b from-indigo-950 to-slate-950 pb-28 text-white">
      <header className="flex items-center justify-between px-4 pt-6 pb-4">
        <Link href="/me" aria-label="رجوع" className="text-white/70">
          <ChevronRight className="size-6" />
        </Link>
        <h1 className="flex items-center gap-2 text-lg font-bold">
          امتيازات VIP <Crown className="size-5 text-amber-400" />
        </h1>
        <span className="w-6" />
      </header>

      <div className="space-y-4 px-4">
        {config.vipLevels.map((v) => (
          <div
            key={v.level}
            className="rounded-3xl border p-5"
            style={{ borderColor: v.color, background: `linear-gradient(135deg, ${v.color}22, transparent)` }}
          >
            <div className="flex items-center justify-between">
              <span className="rounded-full px-3 py-1 text-sm font-bold" style={{ background: v.color }}>
                ${v.priceUSD}
              </span>
              <div className="text-right">
                <p className="flex items-center justify-end gap-2 text-lg font-extrabold" style={{ color: v.color }}>
                  {v.name} <Crown className="size-5" />
                </p>
                <p className="text-xs text-white/60">المستوى {v.level}</p>
              </div>
            </div>
            <ul className="mt-4 space-y-2">
              {v.perks.map((perk) => (
                <li key={perk} className="flex items-center justify-end gap-2 text-sm">
                  {perk}
                  <Check className="size-4" style={{ color: v.color }} />
                </li>
              ))}
            </ul>
            <button
              className="mt-4 w-full rounded-2xl py-3 text-sm font-bold text-white"
              style={{ background: v.color }}
            >
              اشترك الآن
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}

"use client"

import { useState } from "react"
import Link from "next/link"
import { ChevronRight, Coins, Check } from "lucide-react"
import { useConfig } from "@/components/config-provider"

export default function RechargePage() {
  const { config } = useConfig()
  const [selected, setSelected] = useState(config.rechargePackages[1]?.id ?? config.rechargePackages[0]?.id)

  const pkg = config.rechargePackages.find((p) => p.id === selected)

  return (
    <div className="min-h-screen pb-28">
      <header className="flex items-center justify-between px-4 pt-6 pb-4">
        <Link href="/me" aria-label="رجوع" className="text-muted-foreground">
          <ChevronRight className="size-6" />
        </Link>
        <h1 className="text-lg font-bold">شحن العملات</h1>
        <span className="w-6" />
      </header>

      <div className="mx-4 rounded-3xl bg-gradient-to-br from-amber-300 to-orange-500 p-5 text-white">
        <p className="text-sm opacity-90">رصيدك الحالي</p>
        <p className="mt-1 flex items-center gap-2 text-3xl font-extrabold">
          <Coins className="size-7" /> {config.wallet.coins.toLocaleString()}
        </p>
      </div>

      <div className="mt-5 grid grid-cols-2 gap-3 px-4">
        {config.rechargePackages.map((p) => {
          const active = selected === p.id
          return (
            <button
              key={p.id}
              onClick={() => setSelected(p.id)}
              className={`relative rounded-2xl border-2 bg-card p-4 text-center transition ${
                active ? "border-[var(--app-primary)]" : "border-transparent"
              }`}
            >
              {p.popular && (
                <span className="absolute -top-2 right-3 rounded-full bg-[var(--app-primary)] px-2 py-0.5 text-[10px] font-bold text-white">
                  الأكثر رواجًا
                </span>
              )}
              {active && (
                <span className="absolute left-2 top-2 grid size-5 place-items-center rounded-full bg-[var(--app-primary)] text-white">
                  <Check className="size-3" />
                </span>
              )}
              <Coins className="mx-auto size-7 text-amber-500" />
              <p className="mt-2 text-lg font-extrabold">{p.coins.toLocaleString()}</p>
              {p.bonus > 0 && <p className="text-xs text-emerald-500">+{p.bonus} هدية</p>}
              <p className="mt-1 text-sm font-bold text-[var(--app-primary)]">${p.priceUSD}</p>
            </button>
          )
        })}
      </div>

      <div className="fixed bottom-0 inset-x-0 mx-auto max-w-md border-t border-border bg-background/95 p-4 backdrop-blur">
        <button
          className="flex w-full items-center justify-center gap-2 rounded-2xl py-4 font-bold text-white shadow-lg"
          style={{ background: `linear-gradient(135deg, var(--app-primary), var(--app-secondary))` }}
        >
          {pkg ? `شحن الآن • $${pkg.priceUSD}` : "اختر باقة"}
        </button>
      </div>
    </div>
  )
}

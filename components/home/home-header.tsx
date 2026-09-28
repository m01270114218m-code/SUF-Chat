"use client"

import { useState } from "react"
import { Search } from "lucide-react"
import { useConfig } from "@/components/config-provider"
import { cn } from "@/lib/utils"

const tabs = ["متعلق", "بث مباشر", "حزب"]

export function HomeHeader() {
  const { config } = useConfig()
  const [tab, setTab] = useState("بث مباشر")
  const [country, setCountry] = useState(config.countries[0]?.code)

  return (
    <header className="sticky top-0 z-30 bg-gradient-to-b from-pink-50 to-background px-4 pt-4 pb-2">
      <div className="flex items-center justify-between">
        <button aria-label="بحث" className="grid size-9 place-items-center rounded-full text-foreground/70">
          <Search className="size-5" />
        </button>
        <nav className="flex items-center gap-5">
          {tabs.map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={cn(
                "relative py-1 text-base font-bold transition-colors",
                tab === t ? "text-foreground" : "text-foreground/40",
              )}
            >
              {t}
              {tab === t && (
                <span
                  className="absolute -bottom-0.5 right-1/2 h-1 w-6 translate-x-1/2 rounded-full"
                  style={{ background: "var(--app-primary)" }}
                />
              )}
            </button>
          ))}
        </nav>
      </div>

      <div className="mt-3 flex gap-2 overflow-x-auto pb-1 [scrollbar-width:none]">
        {config.countries.map((c) => (
          <button
            key={c.code}
            onClick={() => setCountry(c.code)}
            className={cn(
              "flex shrink-0 items-center gap-1.5 rounded-full px-3 py-1.5 text-sm font-semibold transition",
              country === c.code
                ? "bg-[var(--app-primary)]/10 text-[var(--app-primary)]"
                : "text-foreground/60",
            )}
          >
            <span className="text-base leading-none">{c.flag}</span>
            {c.name}
          </button>
        ))}
      </div>
    </header>
  )
}

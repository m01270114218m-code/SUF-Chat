"use client"

import { ChevronLeft } from "lucide-react"
import {
  Gem,
  Store,
  BadgeCheck,
  Gift,
  Castle,
  Backpack,
  Heart,
  Shield,
  Medal,
  Crown,
  UserPlus,
  type LucideIcon,
} from "lucide-react"
import { useConfig } from "@/components/config-provider"

const iconMap: Record<string, LucideIcon> = {
  gem: Gem,
  store: Store,
  badge: BadgeCheck,
  gift: Gift,
  castle: Castle,
  backpack: Backpack,
  heart: Heart,
  shield: Shield,
  medal: Medal,
  crown: Crown,
  userPlus: UserPlus,
}

export function ProfileMenu() {
  const { config } = useConfig()
  const items = config.profileMenu.filter((m) => m.enabled)

  return (
    <div className="mx-4 mt-4 divide-y divide-border rounded-3xl bg-card px-4 shadow-sm">
      {items.map((item) => {
        const Icon = iconMap[item.icon] ?? Gem
        return (
          <button key={item.id} className="flex w-full items-center justify-between py-4">
            <ChevronLeft className="size-5 text-muted-foreground" />
            <span className="flex items-center gap-3">
              <span className="font-medium text-foreground">{item.label}</span>
              <span
                className="grid size-9 place-items-center rounded-full"
                style={{ background: "color-mix(in srgb, var(--app-primary) 12%, transparent)" }}
              >
                <Icon className="size-5 text-[var(--app-primary)]" />
              </span>
            </span>
          </button>
        )
      })}
    </div>
  )
}

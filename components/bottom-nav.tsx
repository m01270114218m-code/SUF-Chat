"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { Home, MessageCircle, Camera, Globe2, Smile } from "lucide-react"
import { cn } from "@/lib/utils"

const items = [
  { href: "/", label: "الرئيسية", icon: Home },
  { href: "/chats", label: "الدردشات", icon: MessageCircle, badge: 5 },
  { href: "/live", label: "بث", icon: Camera, center: true },
  { href: "/moments", label: "اللحظات", icon: Globe2 },
  { href: "/me", label: "حسابي", icon: Smile },
]

export function BottomNav() {
  const pathname = usePathname()

  if (pathname.startsWith("/room/")) return null

  return (
    <nav className="fixed bottom-0 inset-x-0 z-40 mx-auto max-w-md">
      <div className="m-3 flex items-center justify-around rounded-full bg-background/90 backdrop-blur-md border border-border shadow-lg px-2 py-2">
        {items.map((item) => {
          const active = pathname === item.href
          const Icon = item.icon
          if (item.center) {
            return (
              <Link
                key={item.href}
                href={item.href}
                aria-label={item.label}
                className="-mt-8 grid size-16 place-items-center rounded-full text-white shadow-xl"
                style={{
                  background: `linear-gradient(135deg, var(--app-primary), var(--app-secondary))`,
                }}
              >
                <Icon className="size-7" />
              </Link>
            )
          }
          return (
            <Link
              key={item.href}
              href={item.href}
              aria-label={item.label}
              className={cn(
                "relative grid place-items-center rounded-full p-3 transition-colors",
                active ? "text-[var(--app-primary)]" : "text-muted-foreground",
              )}
            >
              <Icon className="size-6" />
              {item.badge ? (
                <span className="absolute top-1 left-1 grid min-w-5 place-items-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
                  {item.badge}
                </span>
              ) : null}
            </Link>
          )
        })}
      </div>
    </nav>
  )
}

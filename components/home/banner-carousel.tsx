"use client"

import { useConfig } from "@/components/config-provider"

export function BannerCarousel() {
  const { config } = useConfig()

  return (
    <div className="flex gap-3 overflow-x-auto px-4 pt-2 pb-1 [scrollbar-width:none]">
      {config.banners.map((b) => (
        <div
          key={b.id}
          className={`relative h-28 w-[85%] shrink-0 overflow-hidden rounded-2xl bg-gradient-to-br ${b.gradient}`}
        >
          <img
            src={b.image || "/placeholder.svg"}
            alt={b.title}
            className="absolute inset-0 size-full object-cover mix-blend-overlay opacity-90"
          />
          <div className="absolute inset-0 flex items-center justify-end p-4">
            <h3 className="text-lg font-extrabold text-white drop-shadow-lg">{b.title}</h3>
          </div>
        </div>
      ))}
    </div>
  )
}

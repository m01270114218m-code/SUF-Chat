"use client"

import Link from "next/link"
import { Signal } from "lucide-react"
import { useConfig } from "@/components/config-provider"

export function RoomGrid() {
  const { config } = useConfig()

  return (
    <div className="grid grid-cols-2 gap-3 px-4 pt-3">
      {config.liveRooms.map((room) => (
        <Link
          key={room.id}
          href={`/room/${room.id}`}
          className="group relative aspect-[3/4] overflow-hidden rounded-2xl bg-muted shadow-sm"
        >
          <img
            src={room.cover || "/placeholder.svg"}
            alt={room.title}
            className="size-full object-cover transition-transform group-active:scale-105"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-black/20" />

          {room.tag && (
            <span className="absolute right-2 top-2 rounded-full bg-white/85 px-2 py-0.5 text-[11px] font-bold text-[var(--app-primary)]">
              {room.tag}
            </span>
          )}

          <div className="absolute inset-x-2 bottom-2 space-y-1">
            <div className="flex items-center gap-1 text-sm font-bold text-white drop-shadow">
              <span>{room.countryFlag}</span>
              <span className="truncate">{room.title}</span>
            </div>
            <div className="flex items-center justify-between">
              <div className="flex -space-x-2">
                {Array.from({ length: 4 }).map((_, i) => (
                  <span
                    key={i}
                    className="size-5 rounded-full border border-white/60 bg-gradient-to-br from-pink-300 to-purple-400"
                  />
                ))}
              </div>
              <span className="flex items-center gap-0.5 text-xs font-semibold text-white">
                <Signal className="size-3" />
                {room.viewers}
              </span>
            </div>
          </div>
        </Link>
      ))}
    </div>
  )
}

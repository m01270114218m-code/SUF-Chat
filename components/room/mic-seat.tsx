"use client"

import { Mic, MicOff, Lock, Heart } from "lucide-react"
import { cn } from "@/lib/utils"

export type Seat = {
  index: number
  userName: string | null
  avatar: string | null
  muted: boolean
  locked: boolean
  charm: number
  speaking?: boolean
}

export function MicSeat({ seat, onTap }: { seat: Seat; onTap: (seat: Seat) => void }) {
  const occupied = Boolean(seat.userName)

  return (
    <button
      onClick={() => onTap(seat)}
      className="flex flex-col items-center gap-1.5"
      aria-label={`مقعد الميكروفون ${seat.index}`}
    >
      <div className="relative">
        <div
          className={cn(
            "grid size-16 place-items-center rounded-full transition",
            seat.speaking && "ring-2 ring-[var(--app-accent)] ring-offset-2 ring-offset-[var(--app-room-bg)]",
          )}
          style={{ background: "rgba(255,255,255,0.06)" }}
        >
          {occupied ? (
            <img
              src={seat.avatar || "/placeholder.svg"}
              alt={seat.userName || ""}
              className="size-16 rounded-full object-cover"
            />
          ) : seat.locked ? (
            <Lock className="size-6 text-white/40" />
          ) : (
            <Mic className="size-6 text-white/50" />
          )}
        </div>
        {occupied && seat.muted && (
          <span className="absolute -bottom-0.5 -left-0.5 grid size-6 place-items-center rounded-full bg-red-500 text-white">
            <MicOff className="size-3.5" />
          </span>
        )}
        {occupied && seat.charm > 0 && (
          <span className="absolute -top-1 right-1/2 flex translate-x-1/2 items-center gap-0.5 rounded-full bg-black/50 px-1.5 py-0.5 text-[10px] font-bold text-pink-300">
            <Heart className="size-2.5 fill-pink-400 text-pink-400" />
            {seat.charm}
          </span>
        )}
      </div>
      <span className="max-w-16 truncate text-xs text-white/70">
        {occupied ? seat.userName : seat.index}
      </span>
    </button>
  )
}

"use client"

import { useMemo, useState } from "react"
import { useParams, useRouter } from "next/navigation"
import {
  Power,
  Wifi,
  Users,
  Trophy,
  Megaphone,
  Grid3x3,
  MessageSquare,
  Gift,
  Smile,
  Mic,
  MicOff,
  ChevronLeft,
} from "lucide-react"
import { useConfig } from "@/components/config-provider"
import { MicSeat, type Seat } from "@/components/room/mic-seat"

type ChatMsg = { id: string; user: string; text: string; system?: boolean }

export default function RoomPage() {
  const params = useParams<{ id: string }>()
  const router = useRouter()
  const { config } = useConfig()

  const room = config.liveRooms.find((r) => r.id === params.id) ?? config.liveRooms[0]

  const [seats, setSeats] = useState<Seat[]>(() =>
    Array.from({ length: 8 }, (_, i) => ({
      index: i + 1,
      userName: i === 0 ? room?.host ?? "المضيف" : null,
      avatar: i === 0 ? "/avatar-user.png" : null,
      muted: false,
      locked: false,
      charm: i === 0 ? 128 : 0,
      speaking: i === 0,
    })),
  )
  const [onMic, setOnMic] = useState(false)
  const [myMuted, setMyMuted] = useState(false)
  const [messages, setMessages] = useState<ChatMsg[]>([
    { id: "m0", user: "النظام", system: true, text: config.moderation.message },
    { id: "m1", user: room?.host ?? "المضيف", text: "أهلاً وسهلاً بالجميع في الغرفة 🌸" },
  ])
  const [input, setInput] = useState("")

  const listeners = useMemo(() => Math.max(1, Math.floor((room?.viewers ?? 0) / 30)), [room])

  function takeSeat(seat: Seat) {
    if (seat.userName || seat.locked) return
    setSeats((prev) =>
      prev.map((s) =>
        s.index === seat.index
          ? { ...s, userName: "أنت", avatar: "/avatar-user.png", speaking: true }
          : s,
      ),
    )
    setOnMic(true)
    pushSystem(`احتل مقعد الميكروفون ${seat.index}`)
  }

  function leaveMic() {
    setSeats((prev) =>
      prev.map((s) => (s.userName === "أنت" ? { ...s, userName: null, avatar: null, speaking: false } : s)),
    )
    setOnMic(false)
  }

  function pushSystem(text: string) {
    setMessages((m) => [...m, { id: crypto.randomUUID(), user: "أنت", system: true, text }])
  }

  function send() {
    const value = input.trim()
    if (!value) return
    setMessages((m) => [...m, { id: crypto.randomUUID(), user: "أنت", text: value }])
    setInput("")
  }

  return (
    <div
      className="relative min-h-dvh text-white"
      style={{
        background: `linear-gradient(180deg, ${config.theme.roomBackground}, #0a0d24 70%)`,
      }}
    >
      {/* top bar */}
      <div className="flex items-center justify-between px-4 pt-4">
        <div className="flex items-center gap-3">
          <button onClick={() => router.back()} aria-label="خروج" className="text-white/80">
            <Power className="size-6" />
          </button>
          <Wifi className="size-5 text-green-400" />
        </div>
        <div className="flex items-center gap-2 rounded-full bg-white/10 py-1 pl-3 pr-1">
          <div className="text-right">
            <p className="text-sm font-bold leading-tight">{room?.host}</p>
            <p className="text-[11px] leading-tight text-white/60">ID: 6858895</p>
          </div>
          <img src="/avatar-user.png" alt="" className="size-9 rounded-full object-cover" />
        </div>
      </div>

      <div className="flex items-center justify-between px-4 pt-3">
        <span className="flex items-center gap-1 rounded-full bg-white/10 px-3 py-1 text-sm">
          <Users className="size-4" /> {listeners}
        </span>
        <div className="flex items-center gap-2">
          <span className="flex items-center gap-1 rounded-full bg-white/10 px-3 py-1 text-sm">
            <Trophy className="size-4 text-amber-400" /> 0
          </span>
          <button className="flex items-center gap-1 rounded-full border border-white/20 px-3 py-1 text-sm">
            <Megaphone className="size-4" /> إعلان
          </button>
        </div>
      </div>

      {/* mic seats */}
      <div className="grid grid-cols-4 gap-4 px-4 py-6">
        {seats.map((seat) => (
          <MicSeat key={seat.index} seat={seat} onTap={takeSeat} />
        ))}
      </div>

      {/* chat feed */}
      <div className="space-y-3 px-4 pb-40">
        {messages.map((m) =>
          m.system && m.user === "النظام" ? (
            <div key={m.id} className="rounded-2xl bg-black/40 p-3 text-sm leading-relaxed text-white/80">
              {m.text}
            </div>
          ) : m.system ? (
            <div key={m.id} className="flex items-center gap-2 text-sm text-amber-300">
              <Megaphone className="size-4 shrink-0" />
              <span>
                {m.user} {m.text}
              </span>
            </div>
          ) : (
            <div key={m.id} className="flex items-start gap-2">
              <img src="/avatar-user.png" alt="" className="size-8 shrink-0 rounded-full object-cover" />
              <div className="rounded-2xl bg-white/10 px-3 py-2">
                <p className="text-xs font-semibold text-[var(--app-accent)]">{m.user}</p>
                <p className="text-sm">{m.text}</p>
              </div>
            </div>
          ),
        )}
      </div>

      {/* bottom action bar */}
      <div className="fixed inset-x-0 bottom-0 mx-auto max-w-md bg-gradient-to-t from-[#0a0d24] to-transparent p-3">
        <div className="flex items-center gap-2">
          <button className="grid size-10 place-items-center rounded-full bg-white/10" aria-label="التطبيقات">
            <Grid3x3 className="size-5" />
          </button>
          {onMic && (
            <button
              onClick={() => setMyMuted((v) => !v)}
              className="grid size-10 place-items-center rounded-full bg-white/10"
              aria-label="كتم"
            >
              {myMuted ? <MicOff className="size-5 text-red-400" /> : <Mic className="size-5" />}
            </button>
          )}
          <button className="grid size-10 place-items-center rounded-full bg-white/10" aria-label="رسالة">
            <MessageSquare className="size-5" />
          </button>
          <button
            className="grid size-10 place-items-center rounded-full"
            style={{ background: "linear-gradient(135deg,var(--app-primary),var(--app-secondary))" }}
            aria-label="هدية"
          >
            <Gift className="size-5" />
          </button>
          <div className="flex flex-1 items-center gap-2 rounded-full bg-white/10 px-3">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.nativeEvent.isComposing && e.keyCode !== 229) send()
              }}
              placeholder="Hi"
              className="h-10 flex-1 bg-transparent text-sm outline-none placeholder:text-white/50"
            />
            <Smile className="size-5 text-white/60" />
          </div>
        </div>
        {onMic && (
          <button onClick={leaveMic} className="mt-2 flex items-center gap-1 text-xs text-white/50">
            <ChevronLeft className="size-3" /> النزول من المايك
          </button>
        )}
      </div>
    </div>
  )
}

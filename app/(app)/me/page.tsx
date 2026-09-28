"use client"

import Link from "next/link"
import { Pencil, Gem, Diamond, Trophy, ChevronRight, Crown, Wallet } from "lucide-react"
import { useConfig } from "@/components/config-provider"
import { ProfileMenu } from "@/components/me/profile-menu"

export default function MePage() {
  const { config } = useConfig()

  return (
    <main className="pb-4">
      {/* header */}
      <div className="relative px-4 pt-6">
        <button className="absolute left-4 top-6 text-muted-foreground" aria-label="تعديل">
          <Pencil className="size-6" />
        </button>
        <div className="flex items-center justify-end gap-3">
          <div className="text-right">
            <p className="flex items-center justify-end gap-1 text-xl font-extrabold">
              محمد فرعون
              <span className="grid size-5 place-items-center rounded-full bg-sky-400 text-[10px] text-white">♂</span>
            </p>
            <p className="text-sm text-muted-foreground">🇪🇬 مصر · ID:6858895</p>
          </div>
          <img src="/avatar-user.png" alt="الصورة الشخصية" className="size-16 rounded-full object-cover" />
        </div>

        {/* badges */}
        <div className="mt-3 flex justify-end gap-2">
          {[
            { icon: Diamond, val: config.wallet.diamonds, cls: "from-sky-400 to-cyan-500" },
            { icon: Gem, val: config.wallet.coins, cls: "from-emerald-400 to-green-500" },
            { icon: Trophy, val: 0, cls: "from-amber-500 to-orange-600" },
          ].map((b, i) => {
            const Icon = b.icon
            return (
              <span
                key={i}
                className={`flex items-center gap-1 rounded-full bg-gradient-to-r ${b.cls} px-3 py-1 text-sm font-bold text-white`}
              >
                <Icon className="size-4" /> {b.val}
              </span>
            )
          })}
        </div>
      </div>

      {/* stats */}
      <div className="mx-4 mt-4 grid grid-cols-4 rounded-3xl bg-card py-4 shadow-sm">
        {[
          ["الإيرادات", 0],
          ["زائر", 0],
          ["تابع", 0],
          ["المتابع", 0],
        ].map(([label, val]) => (
          <div key={label} className="text-center">
            <p className="text-lg font-extrabold">{val}</p>
            <p className="text-xs text-muted-foreground">{label}</p>
          </div>
        ))}
      </div>

      {/* VIP */}
      <Link
        href="/me/vip"
        className="mx-4 mt-4 flex items-center justify-between rounded-2xl bg-gradient-to-r from-indigo-900 to-purple-900 px-5 py-4 text-white"
      >
        <ChevronRight className="size-5" />
        <span className="font-bold">امتيازات حصرية</span>
        <span className="flex items-center gap-1 font-extrabold">
          VIP <Crown className="size-5 text-amber-400" />
        </span>
      </Link>

      {/* wallet */}
      <div className="mx-4 mt-4 rounded-3xl bg-card p-4 shadow-sm">
        <p className="flex items-center justify-end gap-2 font-bold">
          حقيبتي <Wallet className="size-5 text-[var(--app-primary)]" />
        </p>
        <div className="mt-3 grid grid-cols-2 gap-3">
          <div className="rounded-2xl bg-gradient-to-br from-fuchsia-400 to-pink-500 p-4 text-white">
            <p className="text-sm">ألماس</p>
            <p className="text-2xl font-extrabold">{config.wallet.diamonds}</p>
          </div>
          <Link
            href="/me/recharge"
            className="rounded-2xl bg-gradient-to-br from-amber-300 to-orange-400 p-4 text-white"
          >
            <p className="text-sm">عملة</p>
            <p className="text-2xl font-extrabold">{config.wallet.coins.toFixed(2)}</p>
          </Link>
        </div>
      </div>

      {/* my room */}
      <Link
        href={`/room/${config.liveRooms[0]?.id ?? "r1"}`}
        className="mx-4 mt-4 flex items-center justify-between rounded-2xl bg-gradient-to-r from-pink-200 to-fuchsia-200 p-4"
      >
        <span className="grid size-11 place-items-center rounded-full bg-[var(--app-primary)] text-white">
          <ChevronRight className="size-6" />
        </span>
        <div className="text-right">
          <p className="font-bold text-[var(--app-primary)]">غرفتي</p>
          <p className="text-sm text-fuchsia-700/70">يتحدثون الآن</p>
        </div>
      </Link>

      <ProfileMenu />

      {/* admin entry */}
      <Link
        href="/admin"
        className="mx-4 mt-4 flex items-center justify-center gap-2 rounded-2xl border border-dashed border-[var(--app-primary)] py-3 text-sm font-semibold text-[var(--app-primary)]"
      >
        لوحة التحكم والمطور الذكي
      </Link>
    </main>
  )
}

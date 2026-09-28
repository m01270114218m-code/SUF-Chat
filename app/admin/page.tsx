"use client"

import { useState } from "react"
import useSWR from "swr"
import Link from "next/link"
import {
  LayoutDashboard,
  Radio,
  ImageIcon,
  ShieldCheck,
  Coins,
  Building2,
  Crown,
  ShieldAlert,
  Palette,
  RotateCcw,
  ExternalLink,
  Trash2,
  Users,
  Eye,
} from "lucide-react"
import { AiAssistant } from "@/components/admin/ai-assistant"
import type { AppConfig } from "@/lib/types"

const fetcher = (url: string) => fetch(url).then((r) => r.json())

type Tab = "overview" | "rooms" | "banners" | "roles" | "recharge" | "agencies" | "vip" | "moderation" | "theme"

const TABS: { id: Tab; label: string; icon: typeof LayoutDashboard }[] = [
  { id: "overview", label: "نظرة عامة", icon: LayoutDashboard },
  { id: "rooms", label: "الغرف", icon: Radio },
  { id: "banners", label: "البانرات", icon: ImageIcon },
  { id: "roles", label: "الأدوار", icon: ShieldCheck },
  { id: "recharge", label: "الشحن", icon: Coins },
  { id: "agencies", label: "الوكالات", icon: Building2 },
  { id: "vip", label: "VIP", icon: Crown },
  { id: "moderation", label: "الرقابة", icon: ShieldAlert },
  { id: "theme", label: "المظهر", icon: Palette },
]

export default function AdminPage() {
  const { data: config, mutate } = useSWR<AppConfig>("/api/config", fetcher)
  const [tab, setTab] = useState<Tab>("overview")

  function updateConfig(next: AppConfig) {
    mutate(next, { revalidate: false })
  }

  async function persist(next: AppConfig) {
    updateConfig(next)
    await fetch("/api/config", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ config: next }),
    })
  }

  async function reset() {
    const res = await fetch("/api/config", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ action: "reset" }),
    })
    updateConfig(await res.json())
  }

  return (
    <div dir="rtl" className="min-h-screen bg-slate-950 text-white">
      <header className="sticky top-0 z-10 flex items-center justify-between gap-3 border-b border-white/10 bg-slate-950/90 px-4 py-3 backdrop-blur">
        <div className="flex items-center gap-2">
          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-fuchsia-500 to-purple-600 font-bold">
            {config?.theme.appName?.[0] ?? "F"}
          </span>
          <div>
            <h1 className="text-sm font-bold leading-tight">لوحة تحكم {config?.theme.appName ?? "التطبيق"}</h1>
            <p className="text-[11px] text-slate-400">إدارة كاملة عبر الذكاء الاصطناعي</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={reset}
            className="flex items-center gap-1.5 rounded-lg border border-white/10 bg-white/5 px-3 py-1.5 text-xs hover:bg-white/10"
          >
            <RotateCcw className="h-3.5 w-3.5" /> إعادة تعيين
          </button>
          <Link
            href="/"
            target="_blank"
            className="flex items-center gap-1.5 rounded-lg bg-gradient-to-br from-fuchsia-500 to-purple-600 px-3 py-1.5 text-xs font-medium"
          >
            <ExternalLink className="h-3.5 w-3.5" /> عرض التطبيق
          </Link>
        </div>
      </header>

      <div className="grid gap-4 p-4 lg:grid-cols-[1fr_400px]">
        <div className="order-2 lg:order-1 space-y-4">
          <nav className="flex flex-wrap gap-1.5">
            {TABS.map((t) => {
              const Icon = t.icon
              return (
                <button
                  key={t.id}
                  onClick={() => setTab(t.id)}
                  className={`flex items-center gap-1.5 rounded-lg px-3 py-2 text-xs font-medium transition ${
                    tab === t.id ? "bg-fuchsia-600 text-white" : "bg-white/5 text-slate-300 hover:bg-white/10"
                  }`}
                >
                  <Icon className="h-4 w-4" /> {t.label}
                </button>
              )
            })}
          </nav>

          {!config ? (
            <div className="rounded-2xl border border-white/10 bg-slate-900/60 p-8 text-center text-slate-400">
              جارٍ التحميل...
            </div>
          ) : (
            <div className="rounded-2xl border border-white/10 bg-slate-900/60 p-4">
              {tab === "overview" && <Overview config={config} />}
              {tab === "rooms" && (
                <List
                  title="غرف البث المباشر"
                  items={config.liveRooms.map((r) => ({
                    id: r.id,
                    primary: r.title,
                    secondary: `${r.host} • ${r.countryFlag} ${r.country} • ${r.viewers} مشاهد`,
                    tag: r.tag,
                  }))}
                  onRemove={(id) => persist({ ...config, liveRooms: config.liveRooms.filter((r) => r.id !== id) })}
                />
              )}
              {tab === "banners" && (
                <List
                  title="البانرات الإعلانية"
                  items={config.banners.map((b) => ({ id: b.id, primary: b.title, secondary: b.image }))}
                  onRemove={(id) => persist({ ...config, banners: config.banners.filter((b) => b.id !== id) })}
                />
              )}
              {tab === "roles" && (
                <List
                  title="أدوار الغرفة"
                  items={config.roles.map((r) => ({
                    id: r.id,
                    primary: r.name,
                    secondary: r.permissions.join("، "),
                    color: r.color,
                  }))}
                  onRemove={(id) => persist({ ...config, roles: config.roles.filter((r) => r.id !== id) })}
                />
              )}
              {tab === "recharge" && (
                <List
                  title="باقات الشحن"
                  items={config.rechargePackages.map((p) => ({
                    id: p.id,
                    primary: `${p.coins.toLocaleString()} عملة ${p.bonus ? `+ ${p.bonus} هدية` : ""}`,
                    secondary: `$${p.priceUSD}`,
                    tag: p.popular ? "الأكثر رواجًا" : undefined,
                  }))}
                  onRemove={(id) =>
                    persist({ ...config, rechargePackages: config.rechargePackages.filter((p) => p.id !== id) })
                  }
                />
              )}
              {tab === "agencies" && (
                <List
                  title="الوكالات"
                  items={config.agencies.map((a) => ({
                    id: a.id,
                    primary: `${a.logo} ${a.name}`,
                    secondary: `${a.members} عضو • ${a.hosts} مضيف • أرباح $${a.earnings.toLocaleString()}`,
                    tag: a.status === "active" ? "نشطة" : a.status === "pending" ? "قيد المراجعة" : "موقوفة",
                  }))}
                  onRemove={(id) => persist({ ...config, agencies: config.agencies.filter((a) => a.id !== id) })}
                />
              )}
              {tab === "vip" && (
                <List
                  title="مستويات VIP"
                  items={config.vipLevels.map((v) => ({
                    id: String(v.level),
                    primary: v.name,
                    secondary: v.perks.join("، "),
                    tag: `$${v.priceUSD}`,
                    color: v.color,
                  }))}
                />
              )}
              {tab === "moderation" && (
                <div className="space-y-3">
                  <h2 className="font-semibold">نظام الرقابة بالذكاء الاصطناعي</h2>
                  <label className="flex items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={config.moderation.enabled}
                      onChange={(e) =>
                        persist({ ...config, moderation: { ...config.moderation, enabled: e.target.checked } })
                      }
                      className="h-4 w-4 accent-fuchsia-500"
                    />
                    تفعيل الرقابة التلقائية على مدار 24 ساعة
                  </label>
                  <textarea
                    value={config.moderation.message}
                    onChange={(e) => updateConfig({ ...config, moderation: { ...config.moderation, message: e.target.value } })}
                    onBlur={(e) => persist({ ...config, moderation: { ...config.moderation, message: e.target.value } })}
                    rows={5}
                    className="w-full rounded-xl border border-white/10 bg-slate-800 p-3 text-sm outline-none focus:border-fuchsia-500"
                  />
                </div>
              )}
              {tab === "theme" && <ThemeEditor config={config} onChange={updateConfig} onPersist={persist} />}
            </div>
          )}
        </div>

        <div className="order-1 lg:order-2 lg:sticky lg:top-[73px] lg:h-[calc(100vh-89px)]">
          <AiAssistant onConfigChange={updateConfig} />
        </div>
      </div>
    </div>
  )
}

function Overview({ config }: { config: AppConfig }) {
  const stats = [
    { label: "غرف نشطة", value: config.liveRooms.length, icon: Radio },
    { label: "إجمالي المشاهدين", value: config.liveRooms.reduce((s, r) => s + r.viewers, 0).toLocaleString(), icon: Eye },
    { label: "الوكالات", value: config.agencies.length, icon: Building2 },
    { label: "الأدوار", value: config.roles.length, icon: ShieldCheck },
    { label: "باقات الشحن", value: config.rechargePackages.length, icon: Coins },
    { label: "مستويات VIP", value: config.vipLevels.length, icon: Crown },
  ]
  return (
    <div className="space-y-4">
      <h2 className="font-semibold">نظرة عامة على التطبيق</h2>
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
        {stats.map((s) => {
          const Icon = s.icon
          return (
            <div key={s.label} className="rounded-xl border border-white/10 bg-white/5 p-4">
              <Icon className="h-5 w-5 text-fuchsia-400" />
              <p className="mt-2 text-2xl font-bold">{s.value}</p>
              <p className="text-xs text-slate-400">{s.label}</p>
            </div>
          )
        })}
      </div>
      <div className="rounded-xl border border-white/10 bg-white/5 p-4">
        <div className="flex items-center gap-2 text-sm">
          <Users className="h-4 w-4 text-fuchsia-400" />
          <span className="text-slate-300">
            محفظة المستخدم الافتراضية: {config.wallet.coins.toLocaleString()} عملة • {config.wallet.diamonds} ألماس
          </span>
        </div>
      </div>
    </div>
  )
}

function List({
  title,
  items,
  onRemove,
}: {
  title: string
  items: { id: string; primary: string; secondary?: string; tag?: string; color?: string }[]
  onRemove?: (id: string) => void
}) {
  return (
    <div className="space-y-3">
      <h2 className="font-semibold">{title}</h2>
      <div className="space-y-2">
        {items.map((it) => (
          <div key={it.id} className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 p-3">
            {it.color && <span className="h-3 w-3 shrink-0 rounded-full" style={{ background: it.color }} />}
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium">{it.primary}</p>
              {it.secondary && <p className="truncate text-xs text-slate-400">{it.secondary}</p>}
            </div>
            {it.tag && (
              <span className="shrink-0 rounded-full bg-fuchsia-500/20 px-2 py-0.5 text-[11px] text-fuchsia-300">
                {it.tag}
              </span>
            )}
            {onRemove && (
              <button
                onClick={() => onRemove(it.id)}
                className="shrink-0 rounded-lg p-1.5 text-slate-400 hover:bg-red-500/15 hover:text-red-400"
                aria-label="حذف"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            )}
          </div>
        ))}
        {items.length === 0 && <p className="text-sm text-slate-500">لا توجد عناصر</p>}
      </div>
    </div>
  )
}

function ThemeEditor({
  config,
  onChange,
  onPersist,
}: {
  config: AppConfig
  onChange: (c: AppConfig) => void
  onPersist: (c: AppConfig) => void
}) {
  const fields: { key: keyof AppConfig["theme"]; label: string; color?: boolean }[] = [
    { key: "appName", label: "اسم التطبيق" },
    { key: "primary", label: "اللون الأساسي", color: true },
    { key: "secondary", label: "اللون الثانوي", color: true },
    { key: "accent", label: "لون التمييز", color: true },
    { key: "roomBackground", label: "خلفية الغرفة", color: true },
  ]
  return (
    <div className="space-y-3">
      <h2 className="font-semibold">مظهر التطبيق</h2>
      <div className="grid gap-3 sm:grid-cols-2">
        {fields.map((f) => (
          <label key={f.key} className="space-y-1.5">
            <span className="text-xs text-slate-400">{f.label}</span>
            <div className="flex items-center gap-2">
              {f.color && (
                <input
                  type="color"
                  value={config.theme[f.key]}
                  onChange={(e) => onChange({ ...config, theme: { ...config.theme, [f.key]: e.target.value } })}
                  onBlur={(e) => onPersist({ ...config, theme: { ...config.theme, [f.key]: e.target.value } })}
                  className="h-9 w-12 shrink-0 rounded border border-white/10 bg-transparent"
                />
              )}
              <input
                type="text"
                value={config.theme[f.key]}
                onChange={(e) => onChange({ ...config, theme: { ...config.theme, [f.key]: e.target.value } })}
                onBlur={(e) => onPersist({ ...config, theme: { ...config.theme, [f.key]: e.target.value } })}
                className="w-full rounded-lg border border-white/10 bg-slate-800 px-3 py-2 text-sm outline-none focus:border-fuchsia-500"
              />
            </div>
          </label>
        ))}
      </div>
    </div>
  )
}

"use client"

import { Search, Eraser, Bird, Bell, Heart, Users } from "lucide-react"
import { useConfig } from "@/components/config-provider"

const conversations = [
  {
    id: "system",
    name: "رسالة النظام",
    official: true,
    time: "09-16",
    color: "from-fuchsia-500 to-purple-500",
    icon: Bird,
    preview: "شكرًا لاختيارك، يسعدنا انضمامك إلينا!",
  },
  { id: "friends", name: "أصدقاء", color: "from-sky-400 to-cyan-500", icon: Bell, preview: "" },
  { id: "follow", name: "تابع", color: "from-pink-400 to-rose-500", icon: Heart, preview: "" },
  { id: "family", name: "عائلة", color: "from-purple-400 to-indigo-500", icon: Users, preview: "" },
  { id: "events", name: "مركز الأحداث", color: "from-amber-400 to-orange-500", icon: Bell, badge: 5, preview: "" },
]

export default function ChatsPage() {
  const { config } = useConfig()

  return (
    <main className="px-4 pt-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Search className="size-6" />
          <Eraser className="size-6" />
        </div>
        <h1 className="text-2xl font-extrabold">الدردشات</h1>
      </div>

      <div className="mt-4 divide-y divide-border">
        {conversations.map((c) => {
          const Icon = c.icon
          return (
            <button key={c.id} className="flex w-full items-center gap-3 py-4">
              <div className="relative">
                <span className={`grid size-14 place-items-center rounded-full bg-gradient-to-br ${c.color}`}>
                  <Icon className="size-7 text-white" />
                </span>
                {c.badge ? (
                  <span className="absolute -top-1 -right-1 grid min-w-5 place-items-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
                    {c.badge}
                  </span>
                ) : null}
              </div>
              <div className="flex-1 text-right">
                <div className="flex items-center justify-end gap-2">
                  {c.official && (
                    <span className="rounded-md bg-fuchsia-100 px-1.5 text-[10px] font-bold text-fuchsia-600">
                      رسمي
                    </span>
                  )}
                  <span className="font-bold">{c.name}</span>
                </div>
                {c.preview && (
                  <p className="truncate text-sm text-muted-foreground">
                    {c.preview.replace("اختيارك", `اختيارك ${config.theme.appName}`)}
                  </p>
                )}
              </div>
              {c.time && <span className="text-xs text-muted-foreground">{c.time}</span>}
            </button>
          )
        })}
      </div>
    </main>
  )
}

"use client"

import { useRef, useState } from "react"
import { Sparkles, Send, Loader2, CheckCircle2, AlertCircle } from "lucide-react"
import type { AppConfig } from "@/lib/types"

type Msg = {
  role: "user" | "assistant"
  text: string
  applied?: string[]
  error?: boolean
}

const SUGGESTIONS = [
  "غيّر اسم التطبيق إلى Nova ولون التطبيق إلى أزرق",
  "أضف غرفة بث جديدة باسم سهرة الخليج من السعودية",
  "أضف باقة شحن 3000 عملة بسعر 20 دولار",
  "أنشئ دور جديد اسمه سفير له صلاحية دعوة الأعضاء",
  "أضف وكالة جديدة باسم وكالة النخبة",
  "عطّل رسالة الرقابة",
]

export function AiAssistant({ onConfigChange }: { onConfigChange: (c: AppConfig) => void }) {
  const [messages, setMessages] = useState<Msg[]>([
    {
      role: "assistant",
      text: "مرحبًا! أنا مطوّر الذكاء الاصطناعي الخاص بك. اكتب أي طلب لتعديل التطبيق (الغرف، البانرات، الأدوار، الشحن، الوكالات، المظهر، الرقابة...) وسأنفّذه فورًا.",
    },
  ])
  const [input, setInput] = useState("")
  const [loading, setLoading] = useState(false)
  const scrollRef = useRef<HTMLDivElement>(null)

  async function submit(text: string) {
    const prompt = text.trim()
    if (!prompt || loading) return
    setMessages((m) => [...m, { role: "user", text: prompt }])
    setInput("")
    setLoading(true)
    try {
      const res = await fetch("/api/admin/ai", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt }),
      })
      const data = await res.json()
      if (!res.ok) {
        setMessages((m) => [...m, { role: "assistant", text: data.error || "حدث خطأ", error: true }])
      } else {
        setMessages((m) => [
          ...m,
          { role: "assistant", text: data.reply, applied: data.applied as string[] },
        ])
        if (data.config) onConfigChange(data.config as AppConfig)
      }
    } catch {
      setMessages((m) => [...m, { role: "assistant", text: "تعذّر الاتصال بالخادم", error: true }])
    } finally {
      setLoading(false)
      requestAnimationFrame(() => {
        scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" })
      })
    }
  }

  return (
    <div className="flex flex-col h-full rounded-2xl border border-white/10 bg-slate-900/60 overflow-hidden">
      <div className="flex items-center gap-2 px-4 py-3 border-b border-white/10 bg-gradient-to-l from-fuchsia-600/20 to-transparent">
        <span className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-fuchsia-500 to-purple-600">
          <Sparkles className="h-5 w-5 text-white" />
        </span>
        <div>
          <p className="font-semibold text-white leading-tight">مطوّر الذكاء الاصطناعي</p>
          <p className="text-xs text-slate-400">حوّل طلباتك إلى تعديلات مباشرة على التطبيق</p>
        </div>
      </div>

      <div ref={scrollRef} className="flex-1 overflow-y-auto p-4 space-y-4 min-h-0">
        {messages.map((m, i) => (
          <div key={i} className={`flex ${m.role === "user" ? "justify-start" : "justify-end"}`}>
            <div
              className={`max-w-[85%] rounded-2xl px-4 py-2.5 text-sm leading-relaxed ${
                m.role === "user"
                  ? "bg-fuchsia-600 text-white"
                  : m.error
                    ? "bg-red-500/15 text-red-200 border border-red-500/30"
                    : "bg-slate-800 text-slate-100 border border-white/5"
              }`}
            >
              <div className="flex items-start gap-2">
                {m.role === "assistant" && !m.error && <Sparkles className="h-4 w-4 mt-0.5 shrink-0 text-fuchsia-400" />}
                {m.error && <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" />}
                <span>{m.text}</span>
              </div>
              {m.applied && m.applied.length > 0 && (
                <ul className="mt-2 space-y-1 border-t border-white/10 pt-2">
                  {m.applied.map((a, j) => (
                    <li key={j} className="flex items-center gap-1.5 text-xs text-emerald-300">
                      <CheckCircle2 className="h-3.5 w-3.5 shrink-0" />
                      {a}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        ))}
        {loading && (
          <div className="flex justify-end">
            <div className="rounded-2xl bg-slate-800 border border-white/5 px-4 py-2.5">
              <Loader2 className="h-4 w-4 animate-spin text-fuchsia-400" />
            </div>
          </div>
        )}
      </div>

      <div className="px-4 pb-2 flex flex-wrap gap-1.5">
        {SUGGESTIONS.map((s) => (
          <button
            key={s}
            onClick={() => submit(s)}
            disabled={loading}
            className="text-[11px] rounded-full border border-white/10 bg-white/5 px-2.5 py-1 text-slate-300 hover:bg-white/10 disabled:opacity-50"
          >
            {s}
          </button>
        ))}
      </div>

      <form
        onSubmit={(e) => {
          e.preventDefault()
          submit(input)
        }}
        className="flex items-center gap-2 p-3 border-t border-white/10"
      >
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="اكتب طلبك هنا... مثال: أضف غرفة جديدة"
          className="flex-1 rounded-xl bg-slate-800 border border-white/10 px-4 py-2.5 text-sm text-white placeholder:text-slate-500 outline-none focus:border-fuchsia-500"
        />
        <button
          type="submit"
          disabled={loading || !input.trim()}
          className="flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-fuchsia-500 to-purple-600 text-white disabled:opacity-40"
          aria-label="إرسال"
        >
          {loading ? <Loader2 className="h-5 w-5 animate-spin" /> : <Send className="h-5 w-5" />}
        </button>
      </form>
    </div>
  )
}

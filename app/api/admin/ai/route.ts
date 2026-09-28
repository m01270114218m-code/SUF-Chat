import { generateObject } from "ai"
import { z } from "zod"
import { getConfig, applyActions } from "@/lib/config-store"
import type { AdminAction } from "@/lib/types"

export const maxDuration = 30

const themeSchema = z
  .object({
    appName: z.string().optional(),
    primary: z.string().optional(),
    secondary: z.string().optional(),
    accent: z.string().optional(),
    roomBackground: z.string().optional(),
  })
  .partial()

const actionSchema = z.discriminatedUnion("type", [
  z.object({ type: z.literal("set_theme"), payload: themeSchema }),
  z.object({
    type: z.literal("add_banner"),
    payload: z.object({
      title: z.string(),
      image: z.string(),
      gradient: z.string(),
    }),
  }),
  z.object({ type: z.literal("remove_banner"), payload: z.object({ id: z.string() }) }),
  z.object({
    type: z.literal("add_room"),
    payload: z.object({
      title: z.string(),
      host: z.string(),
      cover: z.string(),
      country: z.string(),
      countryFlag: z.string(),
      viewers: z.number(),
      tag: z.string(),
      hot: z.boolean().optional(),
    }),
  }),
  z.object({ type: z.literal("remove_room"), payload: z.object({ id: z.string() }) }),
  z.object({
    type: z.literal("add_role"),
    payload: z.object({
      name: z.string(),
      color: z.string(),
      permissions: z.array(z.string()),
    }),
  }),
  z.object({ type: z.literal("remove_role"), payload: z.object({ id: z.string() }) }),
  z.object({
    type: z.literal("add_recharge"),
    payload: z.object({
      coins: z.number(),
      bonus: z.number(),
      priceUSD: z.number(),
      popular: z.boolean().optional(),
    }),
  }),
  z.object({ type: z.literal("remove_recharge"), payload: z.object({ id: z.string() }) }),
  z.object({
    type: z.literal("add_agency"),
    payload: z.object({
      name: z.string(),
      logo: z.string(),
      members: z.number(),
      hosts: z.number(),
      monthlyTarget: z.number(),
      earnings: z.number(),
      status: z.enum(["active", "pending", "suspended"]),
    }),
  }),
  z.object({
    type: z.literal("update_agency"),
    payload: z.object({
      id: z.string(),
      name: z.string().optional(),
      members: z.number().optional(),
      hosts: z.number().optional(),
      monthlyTarget: z.number().optional(),
      earnings: z.number().optional(),
      status: z.enum(["active", "pending", "suspended"]).optional(),
    }),
  }),
  z.object({
    type: z.literal("add_vip"),
    payload: z.object({
      level: z.number(),
      name: z.string(),
      color: z.string(),
      priceUSD: z.number(),
      perks: z.array(z.string()),
    }),
  }),
  z.object({
    type: z.literal("set_wallet"),
    payload: z.object({ coins: z.number().optional(), diamonds: z.number().optional() }),
  }),
  z.object({
    type: z.literal("toggle_menu_item"),
    payload: z.object({ id: z.string(), enabled: z.boolean() }),
  }),
  z.object({
    type: z.literal("set_moderation"),
    payload: z.object({ enabled: z.boolean().optional(), message: z.string().optional() }),
  }),
])

export async function POST(req: Request) {
  try {
    const { prompt } = (await req.json()) as { prompt?: string }
    if (!prompt || !prompt.trim()) {
      return Response.json({ error: "الرجاء إدخال طلب" }, { status: 400 })
    }

    const config = getConfig()

    const { object } = await generateObject({
      model: "openai/gpt-4.1-mini",
      schema: z.object({
        reply: z.string().describe("رد قصير ودود باللغة العربية يشرح ما تم تنفيذه"),
        actions: z.array(actionSchema).describe("قائمة الإجراءات التي يجب تطبيقها على التطبيق"),
      }),
      system: `أنت مطوّر ذكاء اصطناعي متخصص في إدارة تطبيق غرف دردشة صوتية اسمه "${config.theme.appName}".
مهمتك تحويل طلبات المدير المكتوبة بالعربية إلى إجراءات (actions) منظمة تُطبَّق مباشرة على التطبيق.

القواعد:
- حلّل الطلب بدقّة وأنشئ الإجراءات المناسبة فقط. إن لم يتطلب الطلب أي تغيير، أعد مصفوفة actions فارغة واشرح ذلك في reply.
- الألوان يجب أن تكون بصيغة hex مثل #ff2e88.
- للصور استخدم مسارات موجودة مثل /room-1.png أو /room-2.png أو /banner-party.png أو رابط placeholder مثل /placeholder.svg?height=200&width=300.
- عند إضافة غرفة ضع viewers رقمًا واقعيًا بين 100 و5000.
- عند إضافة باقة شحن احسب bonus كنسبة معقولة من coins.
- استخدم رموز الدول والأعلام الصحيحة (EG 🇪🇬، SA 🇸🇦، AE 🇦🇪، إلخ).
- reply يجب أن يكون بالعربية، قصير وواضح.

الإعدادات الحالية للتطبيق:
${JSON.stringify(
        {
          theme: config.theme,
          banners: config.banners.map((b) => ({ id: b.id, title: b.title })),
          liveRooms: config.liveRooms.map((r) => ({ id: r.id, title: r.title })),
          roles: config.roles.map((r) => ({ id: r.id, name: r.name })),
          rechargePackages: config.rechargePackages.map((p) => ({ id: p.id, coins: p.coins })),
          agencies: config.agencies.map((a) => ({ id: a.id, name: a.name })),
          profileMenu: config.profileMenu.map((m) => ({ id: m.id, label: m.label, enabled: m.enabled })),
          moderation: config.moderation,
          wallet: config.wallet,
        },
        null,
        2,
      )}`,
      prompt,
    })

    const { config: newConfig, applied } = applyActions(object.actions as AdminAction[])

    return Response.json({
      reply: object.reply,
      applied,
      actions: object.actions,
      config: newConfig,
    })
  } catch (err) {
    console.log("[v0] AI admin error:", err instanceof Error ? err.message : String(err))
    return Response.json(
      { error: "حدث خطأ أثناء معالجة الطلب. حاول مرة أخرى." },
      { status: 500 },
    )
  }
}

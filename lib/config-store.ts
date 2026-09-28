import type { AppConfig, AdminAction } from "./types"
import { defaultConfig } from "./default-config"

// In-memory config store. Persists for the lifetime of the server process.
// Swap for a database (Neon/Supabase) to persist across restarts.
const globalForConfig = globalThis as unknown as { __appConfig?: AppConfig }

export function getConfig(): AppConfig {
  if (!globalForConfig.__appConfig) {
    globalForConfig.__appConfig = structuredClone(defaultConfig)
  }
  return globalForConfig.__appConfig
}

export function setConfig(config: AppConfig) {
  globalForConfig.__appConfig = config
}

export function resetConfig() {
  globalForConfig.__appConfig = structuredClone(defaultConfig)
  return globalForConfig.__appConfig
}

function id(prefix: string) {
  return `${prefix}_${Math.random().toString(36).slice(2, 8)}`
}

export function applyActions(actions: AdminAction[]): {
  config: AppConfig
  applied: string[]
} {
  const config = structuredClone(getConfig())
  const applied: string[] = []

  for (const action of actions) {
    switch (action.type) {
      case "set_theme":
        config.theme = { ...config.theme, ...action.payload }
        applied.push(`تحديث المظهر: ${Object.keys(action.payload).join("، ")}`)
        break
      case "add_banner":
        config.banners.push({ ...action.payload, id: id("b") })
        applied.push(`إضافة بانر: ${action.payload.title}`)
        break
      case "remove_banner":
        config.banners = config.banners.filter((b) => b.id !== action.payload.id)
        applied.push(`حذف بانر`)
        break
      case "add_room":
        config.liveRooms.push({ ...action.payload, id: id("r") })
        applied.push(`إضافة غرفة: ${action.payload.title}`)
        break
      case "remove_room":
        config.liveRooms = config.liveRooms.filter((r) => r.id !== action.payload.id)
        applied.push(`حذف غرفة`)
        break
      case "add_role":
        config.roles.push({ ...action.payload, id: id("role") })
        applied.push(`إضافة دور: ${action.payload.name}`)
        break
      case "remove_role":
        config.roles = config.roles.filter((r) => r.id !== action.payload.id)
        applied.push(`حذف دور`)
        break
      case "add_recharge":
        config.rechargePackages.push({ ...action.payload, id: id("p") })
        applied.push(`إضافة باقة شحن: ${action.payload.coins} عملة`)
        break
      case "remove_recharge":
        config.rechargePackages = config.rechargePackages.filter((p) => p.id !== action.payload.id)
        applied.push(`حذف باقة شحن`)
        break
      case "add_agency":
        config.agencies.push({ ...action.payload, id: id("a") })
        applied.push(`إضافة وكالة: ${action.payload.name}`)
        break
      case "update_agency": {
        const { id: agencyId, ...rest } = action.payload
        config.agencies = config.agencies.map((a) => (a.id === agencyId ? { ...a, ...rest } : a))
        applied.push(`تحديث وكالة`)
        break
      }
      case "add_vip":
        config.vipLevels.push(action.payload)
        applied.push(`إضافة مستوى VIP: ${action.payload.name}`)
        break
      case "set_wallet":
        config.wallet = { ...config.wallet, ...action.payload }
        applied.push(`تعديل المحفظة`)
        break
      case "toggle_menu_item":
        config.profileMenu = config.profileMenu.map((m) =>
          m.id === action.payload.id ? { ...m, enabled: action.payload.enabled } : m,
        )
        applied.push(`تبديل عنصر القائمة`)
        break
      case "set_moderation":
        config.moderation = { ...config.moderation, ...action.payload }
        applied.push(`تحديث نظام الرقابة`)
        break
      default:
        break
    }
  }

  setConfig(config)
  return { config, applied }
}

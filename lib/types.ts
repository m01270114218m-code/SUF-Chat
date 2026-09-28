export type Theme = {
  appName: string
  primary: string
  secondary: string
  accent: string
  roomBackground: string
}

export type Banner = {
  id: string
  title: string
  image: string
  gradient: string
}

export type LiveRoom = {
  id: string
  title: string
  host: string
  cover: string
  country: string
  countryFlag: string
  viewers: number
  tag: string
  hot?: boolean
}

export type MicSeat = {
  index: number
  userName: string | null
  avatar: string | null
  muted: boolean
  locked: boolean
  charm: number
}

export type Role = {
  id: string
  name: string
  color: string
  permissions: string[]
}

export type RechargePackage = {
  id: string
  coins: number
  bonus: number
  priceUSD: number
  popular?: boolean
}

export type Agency = {
  id: string
  name: string
  logo: string
  members: number
  hosts: number
  monthlyTarget: number
  earnings: number
  status: "active" | "pending" | "suspended"
}

export type VipLevel = {
  level: number
  name: string
  color: string
  priceUSD: number
  perks: string[]
}

export type MenuItem = {
  id: string
  label: string
  icon: string
  enabled: boolean
}

export type AppConfig = {
  theme: Theme
  banners: Banner[]
  countries: { code: string; name: string; flag: string }[]
  liveRooms: LiveRoom[]
  roles: Role[]
  rechargePackages: RechargePackage[]
  agencies: Agency[]
  vipLevels: VipLevel[]
  profileMenu: MenuItem[]
  wallet: { coins: number; diamonds: number }
  moderation: { enabled: boolean; message: string }
}

export type AdminAction =
  | { type: "set_theme"; payload: Partial<Theme> }
  | { type: "add_banner"; payload: Omit<Banner, "id"> }
  | { type: "remove_banner"; payload: { id: string } }
  | { type: "add_room"; payload: Omit<LiveRoom, "id"> }
  | { type: "remove_room"; payload: { id: string } }
  | { type: "add_role"; payload: Omit<Role, "id"> }
  | { type: "remove_role"; payload: { id: string } }
  | { type: "add_recharge"; payload: Omit<RechargePackage, "id"> }
  | { type: "remove_recharge"; payload: { id: string } }
  | { type: "add_agency"; payload: Omit<Agency, "id"> }
  | { type: "update_agency"; payload: { id: string } & Partial<Agency> }
  | { type: "add_vip"; payload: VipLevel }
  | { type: "set_wallet"; payload: Partial<{ coins: number; diamonds: number }> }
  | { type: "toggle_menu_item"; payload: { id: string; enabled: boolean } }
  | { type: "set_moderation"; payload: Partial<{ enabled: boolean; message: string }> }

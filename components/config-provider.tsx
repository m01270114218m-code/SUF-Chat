"use client"

import { createContext, useContext } from "react"
import useSWR from "swr"
import type { AppConfig } from "@/lib/types"
import { defaultConfig } from "@/lib/default-config"

const fetcher = (url: string) => fetch(url).then((r) => r.json())

type ConfigContextValue = {
  config: AppConfig
  isLoading: boolean
  mutate: () => void
}

const ConfigContext = createContext<ConfigContextValue>({
  config: defaultConfig,
  isLoading: true,
  mutate: () => {},
})

export function ConfigProvider({ children }: { children: React.ReactNode }) {
  const { data, isLoading, mutate } = useSWR<AppConfig>("/api/config", fetcher, {
    fallbackData: defaultConfig,
    refreshInterval: 4000,
  })

  const config = data ?? defaultConfig

  return (
    <ConfigContext.Provider value={{ config, isLoading, mutate: () => mutate() }}>
      <style>{`:root{
        --app-primary:${config.theme.primary};
        --app-secondary:${config.theme.secondary};
        --app-accent:${config.theme.accent};
        --app-room-bg:${config.theme.roomBackground};
      }`}</style>
      {children}
    </ConfigContext.Provider>
  )
}

export function useConfig() {
  return useContext(ConfigContext)
}

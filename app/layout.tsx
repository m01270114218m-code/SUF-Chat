import { Analytics } from '@vercel/analytics/next'
import type { Metadata, Viewport } from 'next'
import { Tajawal } from 'next/font/google'
import { ConfigProvider } from '@/components/config-provider'
import './globals.css'

const tajawal = Tajawal({
  subsets: ['arabic', 'latin'],
  weight: ['400', '500', '700', '800'],
  variable: '--font-tajawal',
})

export const metadata: Metadata = {
  title: 'FanC — غرف الدردشة الصوتية',
  description:
    'تطبيق غرف دردشة صوتية احترافي مع نظام أدوار، شحن، وكالات، ولوحة تحكم مدعومة بالذكاء الاصطناعي.',
  generator: 'v0.app',
}

export const viewport: Viewport = {
  themeColor: '#141a3a',
  colorScheme: 'dark light',
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="ar" dir="rtl" className={tajawal.variable}>
      <body className="antialiased font-sans">
        <ConfigProvider>{children}</ConfigProvider>
        {process.env.NODE_ENV === 'production' && <Analytics />}
      </body>
    </html>
  )
}

import "./globals.css";
import type { ReactNode } from "react";

export const metadata = {
  title: "SUF Voice Rooms",
  description: "غرف صوتية وشات"
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return <html lang="ar" dir="rtl"><body>{children}</body></html>;
}
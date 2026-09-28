import { HomeHeader } from "@/components/home/home-header"
import { BannerCarousel } from "@/components/home/banner-carousel"
import { RoomGrid } from "@/components/home/room-grid"

export default function HomePage() {
  return (
    <main>
      <HomeHeader />
      <BannerCarousel />
      <RoomGrid />
    </main>
  )
}

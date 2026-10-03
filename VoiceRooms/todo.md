# خطة تنفيذ تطبيق "غرف صوتية" — Android احترافي كامل + Supabase

## الهدف
تطبيق أندرويد احترافي (Kotlin + Jetpack Compose) لغرف الدردشة الصوتية شبيه بـ «أهلا شات / يلا»،
جاهز للعمل فعليًا، وكل شيء متحكّم فيه من قاعدة بيانات Supabase (الأدوار، الوظائف، الهدايا،
الوجهات/البانرات المتحركة SVGA، الوكالات، المستويات، الإعدادات).

## المراحل
- [x] المرحلة 1: مخطط قاعدة البيانات الكامل (schema.sql + seed.sql)
- [x] المرحلة 2: بنية مشروع Android (Gradle, Manifest, Resources, Theme, Navigation)
- [x] المرحلة 3: طبقة البيانات (Models, SupabaseProvider, Repositories, ApiResult)
- [x] المرحلة 4: طبقة العرض — كل ViewModels (Auth, Home, Room, Profile, CreateRoom, Agency, Settings, Social, Gifts)
- [x] المرحلة 5: المكوّنات القابلة لإعادة الاستخدام (Common, RoomCard, GiftSheet, SVGA, BottomBar)
- [x] المرحلة 6: شاشات المصادقة (Splash, Login, Register)
- [x] المرحلة 7: الشاشات الرئيسية (Home, Explore, Messages, Profile, UserProfile, EditProfile)
- [x] المرحلة 8: شاشة الغرفة الصوتية (Room) + الأدوار + أدوات المشرف
- [x] المرحلة 9: شاشات الاقتصاد والوكالات (Wallet, Gifts, Agency, CreateRoom, Settings)
- [x] المرحلة 10: AppNavGraph + ربط كل الشاشات
- [x] المرحلة 11: WebRTC (الصوت) + Service + SVGA integration
- [x] المرحلة 12: README + دليل الربط بقاعدة البيانات
- [x] المرحلة 13: توليد صور التصميم + تجهيز ملف ZIP النهائي

## تفاصيل المراحل المتبقية (مكتملة)
- [x] إنشاء SettingsScreen.kt (المستويات + إعدادات التطبيق من قاعدة البيانات)
- [x] إنشاء RoomScreen.kt (المقاعد، الأعضاء، الشات، زر الهدايا، أدوات المشرف، كتم/رفع اليد، مؤشرات المتحدث، طبقة أنيميشن الهدايا)
- [x] إنشاء AppNavGraph.kt (ربط كل الشاشات عبر Routes)
- [x] إنشاء voice/VoiceEngine.kt (WebRTC: PeerConnectionFactory, AudioTrack، إدارة الاتصالات والأصوات)
- [x] إنشاء voice/SignalingClient.kt (توقيع عبر Supabase Realtime Broadcast)
- [x] إنشاء voice/VoiceRoomController.kt (تنسيق الجلسة Mesh)
- [x] إنشاء voice/VoiceService.kt (خدمة أمامية لإبقاء الصوت أثناء المحادثة) + إعلانها في Manifest
- [x] ربط VoiceEngine بشاشة الغرفة
- [x] إنشاء README.md (خطوات إعداد Supabase + البناء)
- [x] تجهيز ملف ZIP النهائي + الصور

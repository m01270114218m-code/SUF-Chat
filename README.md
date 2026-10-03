# 🎙️ غرف صوتية — تطبيق أندرويد احترافي جاهز للعمل (Kotlin + Jetpack Compose + Supabase)

تطبيق دردشة صوتية كامل شبيه بـ «أهلا شات / يلا» — **جاهز للعمل فعليًا وليس تجريبيًا**، وكل شيء فيه
(الأدوار، الوظائف، الهدايا، الوجهات/البانرات المتحركة SVGA، الوكالات، المستويات، الإعدادات)
**متحكَّم فيه بالكامل من قاعدة بيانات Supabase**.

---

## ✨ الميزات

- **مصادقة كاملة**: تسجيل / دخول / خروج / استرجاع الجلسة (Supabase Auth — Email).
- **غرف صوتية حقيقية**: صوت فعلي عبر **WebRTC** (Mesh) مع إشارات (Signaling) عبر **Supabase Realtime Broadcast**.
- **مقاعد وأدوار**: مالك / مشرف / مشرف عام / عضو — مع كتم، رفع اليد، مؤشر تحدّث، ونزول/صعود المقعد.
- **أدوات مشرف**: كتم إجباري، طرد، حظر نهائي، ترقية/تنزيل الدور، قفل الغرفة، قفل الميكروفونات.
- **هدايا متحركة SVGA**: لوحة هدايا تُقرأ من قاعدة البيانات + تشغيل أنيميشن SVGA فوق الشاشة.
- **وجهات/بانرات متحركة**: جدول `banners` يدعم صورًا وملفات SVGA.
- **اقتصاد**: محفظة، عملات (coins)، ألماس (diamonds)، معاملات، مستويات وXP ومكافآت.
- **وكالات**: إنشاء وكالة، عمولة، أعضاء، أرباح.
- **الشات النصي** داخل الغرفة (Realtime/polling).
- **خدمة أمامية (Foreground Service)**: تُبقي الصوت نشطًا أثناء الخلفية/إطفاء الشاشة.
- **كل الإعدادات من قاعدة البيانات**: جدول `app_settings` (اسم التطبيق، الوصف، الإصدار، تفعيل/تعطيل الميزات…).

---

## 🧱 التقنيات

| الطبقة | التقنية |
|---|---|
| اللغة | Kotlin 1.9.24 |
| الواجهة | Jetpack Compose (Material 3) + Navigation Compose |
| المعمارية | MVVM + Repository + StateFlow + Manual DI (AppContainer) |
| الباك-إند | **Supabase** (Auth + Postgrest + Realtime + Storage) عبر `supabase-kt 2.6.0` |
| الصوت | WebRTC (`io.github.webrtc-sdk:android:125.6422.04`) |
| الأنيميشن | SVGA (`com.github.yyued:SVGAPlayer-Android:2.6.1`) |
| الصور | Coil |
| البناء | Gradle 8.7 · AGP 8.5.0 · JDK 17 · compileSdk 34 · minSdk 24 |

---

## 📂 هيكل المشروع

```
app/src/main/java/com/voicerooms/app/
├── MainActivity.kt                 # نقطة الدخول + الثيم
├── VoiceRoomsApp.kt                # Application + AppContainer
├── di/AppContainer.kt              # الحاوية اليدوية للتبعيات
├── data/
│   ├── model/                      # Profile, Room, Gift, Banner, Agency, Level, AppSetting...
│   ├── remote/SupabaseProvider.kt  # عميل Supabase (Auth/Postgrest/Realtime/Storage)
│   └── repository/                 # Auth, Profile, Room, Gift, Settings, Agency
├── ui/
│   ├── navigation/                 # Routes + AppNavGraph
│   ├── screens/                    # كل الشاشات
│   ├── components/                 # Common, RoomCard, GiftSheet, SvgaPlayer, BottomBar
│   ├── theme/                      # Color, Type, Theme
│   ├── viewmodel/                  # كل الـ ViewModels
│   └── AppViewModelProvider.kt     # مصنع الـ ViewModels
├── voice/                          # VoiceEngine, SignalingClient, VoiceRoomController, VoiceService
└── util/ApiResult.kt               # غلاف موحّد + ترجمة الأخطاء للعربية

supabase/
├── schema.sql                      # كل الجداول + الدوال (RPC) + سياسات RLS + Realtime
└── seed.sql                        # بيانات أولية (إعدادات، مستويات، تصنيفات، هدايا، بانرات)
```

---

## 🚀 خطوات التشغيل (Setup)

### 1) إنشاء مشروع Supabase
1. اذهب إلى [https://supabase.com](https://supabase.com) وأنشئ مشروعًا جديدًا.
2. من **Project Settings → API** انسخ:
   - `Project URL` (مثال: `https://abcd1234.supabase.co`)
   - `anon public key`

### 2) تنفيذ مخطط قاعدة البيانات
من **SQL Editor** في لوحة Supabase:
1. الصق محتوى `supabase/schema.sql` ثم اضغط **Run**.
2. الصق محتوى `supabase/seed.sql` ثم اضغط **Run**.

> سيُنشئ ذلك كل الجداول، الدوال (RPC)، سياسات الأمان RLS، وقنوات Realtime، مع بيانات أولية.

### 3) ضبط بيانات الاتصال في التطبيق
افتح `app/build.gradle.kts` وعدّل القيمتين:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://YOUR-PROJECT.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"YOUR-ANON-KEY\"")
```

> بديل: ضعهما في `local.properties` واقرأهما في `build.gradle.kts`.

### 4) تفعيل المصادقة
- **Authentication → Providers → Email**: مُفعّل افتراضيًا.
- للتطوير السريع يمكنك تعطيل تأكيد البريد من **Authentication → Settings → Disable email confirmations**.

### 5) تخزين الملفات (SVGA / الصور)
- أنشئ Bucket عام (Public) باسم `media` من **Storage**.
- ارفع ملفات `.svga` للهدايا/البانرات، وضع رابط كل ملف في عمود `animation_url` بالجدول المناسب (`gifts` / `banners`).

### 6) البناء والتشغيل
1. افتح المجلد في **Android Studio (Hedgehog أو أحدث)**.
2. تأكد أن **JDK 17** مُحدَّد (File → Settings → Build → Gradle → Gradle JDK).
3. اضغط **Sync Project with Gradle Files**.
4. شغّل على جهاز/محاكي: **Run ▶**.

> ملاحظة: من سطر الأوامر: `./gradlew assembleDebug` (يتطلب Android SDK + JDK 17).

---

## 🔐 التحكم الكامل من قاعدة البيانات

كل ما يلي يُقرأ/يُدار من Supabase بدون تعديل الكود:

| الجدول | الوظيفة |
|---|---|
| `app_settings` | اسم التطبيق، الوصف، الإصدار، تشغيل/إيقاف الميزات |
| `levels` | المستويات، XP المطلوب، الألوان، المكافآت |
| `categories` | تصنيفات الغرف |
| `gifts` | الهدايا + `animation_url` (SVGA) + السعر |
| `banners` | الوجهات/البانرات المتحركة + أماكن العرض |
| `agencies` / `agency_members` | الوكالات والعمولة والأعضاء |
| `rooms` / `room_members` / `room_bans` / `room_messages` | الغرف والأدوار والمقاعد والحظر والشات |
| `profiles` | المستخدمون، الأدوار، العملات، الألماس، المستوى |
| `wallet_transactions` | عمليات المحفظة |

الدوال الحسّاسة (join/leave/take_seat/kick/ban/set_role/send_gift…) تُنفَّذ عبر **RPC** داخل قاعدة البيانات،
فتكون القواعد والتحقق في مكان واحد آمن.

---

## 🔊 الصوت (WebRTC) — ملاحظات مهمة للإنتاج

- التطبيق يستخدم خوادم **STUN** عامة (Google) افتراضيًا في `VoiceEngine.ICE_SERVERS`.
- للإنتاج (شبكات مقيّدة/NAT صعب) **يُنصح بشدة** بإضافة خادم **TURN** خاص بك:
  ```kotlin
  // voice/VoiceEngine.kt
  private val ICE_SERVERS = listOf(
      "stun:stun.l.google.com:19302",
      "turn:YOUR-TURN-SERVER:3478?transport=udp",  // + username/credential
  )
  ```
- المعمارية الحالية **Mesh** (مناسبة للغرف الصغيرة ≤ 8). للغرف الكبيرة استخدم SFU (مثل LiveKit / mediasoup).
- الإشارات (Signaling) تمر عبر قناة Realtime باسم `room-signal-<roomId>` على الحدث `signal`.

---

## 🧩 الإذن المطلوب
- `RECORD_AUDIO` (الميكروفون) — يُطلب عند دخول الغرفة.
- `POST_NOTIFICATIONS` (أندرويد 13+) — لإظهار إشعار الخدمة الأمامية.

---

## 🛠️ استكشاف الأخطاء

| المشكلة | الحل |
|---|---|
| فشل الاتصال بـ Supabase | تأكد من صحة `SUPABASE_URL` و `ANON_KEY` |
| لا يظهر الصوت | امنح إذن الميكروفون، وتأكد من خوادم ICE/TURN |
| لا تُحمَّل الهدايا | تأكد أن `animation_url` رابط مباشر لملف `.svga` عام |
| خطأ صلاحيات (RLS) | نفّذ `schema.sql` كاملًا؛ السياسات تُنشأ معه |
| تعذّر البناء | تأكد من JDK 17 و Android SDK 34 |

---

## 📄 الترخيص
هذا المشروع مُقدَّم للاستخدام الحر — عدّله وانشره كما تشاء.

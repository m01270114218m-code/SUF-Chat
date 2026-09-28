package com.example.models

import androidx.compose.ui.graphics.Color

/**
 * Bottom Navigation Tabs (`الصفحة الرئيسية` | `اكتشف` | `الألعاب` | `الرسائل` | `أنا`).
 */
enum class MainNavTab(val titleAr: String) {
    HOME("الرئيسية"),
    DISCOVER("اكتشف"),
    GAMES("الألعاب"),
    MESSAGES("الرسائل"),
    PROFILE("أنا")
}

/**
 * All standalone full-screen pages so that every feature works independently
 * in its own dedicated screen.
 */
enum class StandaloneSubScreen {
    NONE,
    STORE,              // المتجر الملكي (قسم الإطارات، قسم الدخوليات، قسم الآي دي)
    BAG_WARDROBE,       // الحقيبة (تعرض ما أمتلكه من إطارات ودخوليات)
    WALLET,             // العملة (تعرض العملة الذهبية فقط)
    COINS_ONLY,         // العملة فقط
    DIAMONDS_ONLY,      // الألماس فقط
    VIP_NOBILITY,       // مركز الـ VIP الملكي (يعرض 5 أقسام يتم ضبطها من قاعدة البيانات)
    LEVELS,             // المستوى (تعرض مستويات حسابي)
    BADGES,             // الشارة (تعرض الشارات المملوكة)
    GIFT_ATLAS,         // أطلس ومعرض الهدايا 7D
    CP_RELATIONSHIPS,   // العلاقات الملكية وشريك الـ CP
    FAMILY_CLAN,        // العائلة (تعرض العائلة المنضم إليها)
    LANGUAGE_SETTINGS,  // اللغة (تعرض لغات عديدة للتطبيق)
    CUSTOMER_SERVICE,   // خدمة العملاء
    INVITE_FRIENDS,     // دعوة الأصدقاء للحصول على مكافآت إطارات وعملات ذهبية
    DAILY_TASKS,        // المهام اليومية وعجلة الحظ 7D
    AGENCIES,           // وكالتي (مركز وكيل المضيفين أو بيانات المضيف الشخصية)
    CHARGE_AGENCY,      // مركز وكالة الشحن (خاص بوكيل الشحن لشحن العملات وإرسال المكافآت بالـ ID)
    LEADERBOARD,        // لوحة الصدارة والترتيب العالمي
    EVENT_CENTER,       // مركز الأحداث والمهرجانات الملكية
    ACCOUNT_SETTINGS,   // إعدادات الحساب والخصوصية
    APP_POLICY,         // سياسة التطبيق وشروط الاستخدام
    INNER_ACCOUNT_DETAIL // صفحة تفاصيل الحساب من الداخل (تفتح عند اللمس مرتين على الصورة)
}

/**
 * User's permission role inside a specific Voice Room:
 * - OWNER: صاحب الغرفة (طرد، كتم، حظر، تعيين أدمن، تغيير اسم الغرفة، تغيير صورة الغرفة، قفل/فتح مايك)
 * - ADMIN: أدمن الغرفة (قفل مايك، فتح مايك، طرد مستخدم، كتم مستخدم)
 * - MEMBER: مستخدم في الغرفة (صعود مايك، نزول من المايك، متابعة، إرسال هدية، والتفاعل داخل الغرفة فقط)
 */
enum class RoomPermissionRole(val titleAr: String, val badgeEmoji: String, val badgeColor: Color) {
    OWNER("صاحب الغرفة", "👑", Color(0xFFFFD700)),
    ADMIN("أدمن الغرفة", "🛡️", Color(0xFF00E5FF)),
    MEMBER("مستخدم في الغرفة", "🎙️", Color(0xFF00E676))
}

typealias RoomUserRole = RoomPermissionRole

/**
 * Global User Roles controlled via the database (`user_roles_assignment` table).
 */
enum class AppUserRole(
    val code: String,
    val titleAr: String,
    val badgeColor: Color,
    val iconEmoji: String
) {
    SUPER_ADMIN("SUPER_ADMIN", "سوبر أدمن", Color(0xFFFF1744), "👑"),
    ADMIN("ADMIN", "أدمن", Color(0xFFFF9100), "🛡️"),
    MANAGER("MANAGER", "مدير", Color(0xFFE040FB), "⚜️"),
    BD("BD", "بدي BD", Color(0xFF00E5FF), "💎"),
    CHARGE_AGENT("CHARGE_AGENT", "وكيل شحن", Color(0xFFFFD700), "🪙"),
    HOST_AGENT("HOST_AGENT", "وكيل مضيفين", Color(0xFF00E676), "🏢"),
    HOST("HOST", "مضيف", Color(0xFFFF4081), "🎙️"),
    SUPPORTER("SUPPORTER", "داعم ملكي", Color(0xFFFFD700), "🦁"),
    ROOM_OWNER("ROOM_OWNER", "صاحب الغرفة", Color(0xFFFFD700), "👑"),
    ROOM_ADMIN("ROOM_ADMIN", "أدمن الغرفة", Color(0xFF00E5FF), "🛡️"),
    USER("USER", "عضو ملكي", Color(0xFF90CAF9), "✨");

    val secondaryColor: Color get() = badgeColor.copy(alpha = 0.7f)
}

typealias UserRole = AppUserRole

/**
 * Circular 3D Avatar & Microphone Frame Styles (`الإطارات الدائرية على الصورة والمايك`).
 */
enum class FrameStyle3D(
    val id: String,
    val nameAr: String,
    val primaryColor: Color,
    val accentColor: Color,
    val soundWaveColor: Color,
    val nameGradientColor: Color,
    val hasWings: Boolean,
    val hasCrown: Boolean,
    val priceCoins: Long
) {
    NONE(
        id = "frame_none",
        nameAr = "بدون إطار خارجي",
        primaryColor = Color(0xFFFFD700),
        accentColor = Color(0xFF00E5FF),
        soundWaveColor = Color(0xFF00E5FF),
        nameGradientColor = Color(0xFFFFFFFF),
        hasWings = false,
        hasCrown = false,
        priceCoins = 0
    ),
    IMPERIAL_GOLD_WINGS(
        id = "frame_imperial_wings",
        nameAr = "إطار أجنحة الإمبراطور الذهبي 7D",
        primaryColor = Color(0xFFFFD700),
        accentColor = Color(0xFFFF8F00),
        soundWaveColor = Color(0xFFFFD700),
        nameGradientColor = Color(0xFFFFF59D),
        hasWings = true,
        hasCrown = true,
        priceCoins = 45000
    ),
    CRYSTAL_DRAGON_ICE(
        id = "frame_crystal_dragon",
        nameAr = "إطار تنين الجليد الكريستالي 6D",
        primaryColor = Color(0xFF00E5FF),
        accentColor = Color(0xFF2979FF),
        soundWaveColor = Color(0xFF00E5FF),
        nameGradientColor = Color(0xFF80D8FF),
        hasWings = true,
        hasCrown = true,
        priceCoins = 38000
    ),
    ROSE_GOLD_PHOENIX(
        id = "frame_rose_phoenix",
        nameAr = "إطار عنقاء الياقوت الوردي 5D",
        primaryColor = Color(0xFFFF4081),
        accentColor = Color(0xFFFFD700),
        soundWaveColor = Color(0xFFFF4081),
        nameGradientColor = Color(0xFFFF80AB),
        hasWings = true,
        hasCrown = true,
        priceCoins = 32000
    ),
    EMERALD_SULTAN_CROWN(
        id = "frame_emerald_sultan",
        nameAr = "إطار تاج السلطان الزمردي 4D",
        primaryColor = Color(0xFF00E676),
        accentColor = Color(0xFFFFD700),
        soundWaveColor = Color(0xFF00E676),
        nameGradientColor = Color(0xFF69F0AE),
        hasWings = false,
        hasCrown = true,
        priceCoins = 22000
    ),
    ROYAL_VIOLET_AURA(
        id = "frame_violet_aura",
        nameAr = "إطار الهالة البنفسجية الملكية 3D",
        primaryColor = Color(0xFFE040FB),
        accentColor = Color(0xFF7C4DFF),
        soundWaveColor = Color(0xFFE040FB),
        nameGradientColor = Color(0xFFEA80FC),
        hasWings = false,
        hasCrown = false,
        priceCoins = 12000
    );

    val secondaryColor: Color get() = accentColor

    companion object {
        val GOLD_ROYAL: FrameStyle3D get() = IMPERIAL_GOLD_WINGS
    }
}

typealias AvatarFrameStyle = FrameStyle3D

/**
 * Animated, High-Quality, and Transparent Chatroom Background Themes purchasable from the Store
 * or selectable by Room Owners (`خلفيات وثيمات الغرف المتحركة والشفافة عالية الجودة`).
 */
enum class RoomBackgroundTheme(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val iconEmoji: String,
    val priceCoins: Long,
    val topColor: Color,
    val midColor: Color,
    val bottomColor: Color,
    val accentGlowColor: Color,
    val secondaryGlowColor: Color,
    val isTransparentGlass: Boolean = true
) {
    PALACE_NIGHT(
        id = "PALACE_NIGHT",
        titleAr = "ليالي الهلال الملكي المتحركة 🌙",
        subtitleAr = "سماء ليلية متحركة مع هلال ذهبي ونجوم متلألئة",
        iconEmoji = "🌙",
        priceCoins = 0L,
        topColor = Color(0xFF141B2D),
        midColor = Color(0xFF0D1220),
        bottomColor = Color(0xFF06080E),
        accentGlowColor = Color(0xFFFFE082),
        secondaryGlowColor = Color(0xFF64B5F6),
        isTransparentGlass = true
    ),
    TRANSPARENT_AURORA(
        id = "TRANSPARENT_AURORA",
        titleAr = "أورورا الكريستال الشفافة الملونة 🌌",
        subtitleAr = "موجات أورورا شفافة متحركة عالية الدقة 7D",
        iconEmoji = "🌌",
        priceCoins = 18000L,
        topColor = Color(0xFF1A103C),
        midColor = Color(0xFF0F2942),
        bottomColor = Color(0xFF090D1A),
        accentGlowColor = Color(0xFF38BDF8),
        secondaryGlowColor = Color(0xFFA855F7),
        isTransparentGlass = true
    ),
    ROYAL_GOLD_PALACE(
        id = "ROYAL_GOLD_PALACE",
        titleAr = "قصر الذهب الإمبراطوري الشفاف 👑",
        subtitleAr = "ذرات ذهب ملكي متطايرة وهالة إمبراطورية شفافة",
        iconEmoji = "👑",
        priceCoins = 35000L,
        topColor = Color(0xFF2B1D06),
        midColor = Color(0xFF1C1308),
        bottomColor = Color(0xFF0C0904),
        accentGlowColor = Color(0xFFFFD700),
        secondaryGlowColor = Color(0xFFF59E0B),
        isTransparentGlass = true
    ),
    SAPPHIRE_STARLIGHT(
        id = "SAPPHIRE_STARLIGHT",
        titleAr = "سماء الياقوت الأزرق الشفافة 💎",
        subtitleAr = "سديم ياقوتي شفاف مع شهب ونجوم متحركة",
        iconEmoji = "💎",
        priceCoins = 25000L,
        topColor = Color(0xFF0C2340),
        midColor = Color(0xFF08172E),
        bottomColor = Color(0xFF040B17),
        accentGlowColor = Color(0xFF00E5FF),
        secondaryGlowColor = Color(0xFF3B82F6),
        isTransparentGlass = true
    ),
    EMERALD_CRYSTAL(
        id = "EMERALD_CRYSTAL",
        titleAr = "واحة الزمرد الشفافة المتحركة 💚",
        subtitleAr = "إضاءة زمردية كريستالية شفافة وموجات هادئة",
        iconEmoji = "💚",
        priceCoins = 22000L,
        topColor = Color(0xFF062E22),
        midColor = Color(0xFF071F1A),
        bottomColor = Color(0xFF04100E),
        accentGlowColor = Color(0xFF10B981),
        secondaryGlowColor = Color(0xFF34D399),
        isTransparentGlass = true
    ),
    ROSE_VELVET_GALAXY(
        id = "ROSE_VELVET_GALAXY",
        titleAr = "مجرة الورد الملكي الشفافة 🌸",
        subtitleAr = "مجرة مخملية وردية شفافة مع نجوم مضيئة",
        iconEmoji = "🌸",
        priceCoins = 28000L,
        topColor = Color(0xFF33102A),
        midColor = Color(0xFF1F0B24),
        bottomColor = Color(0xFF0D0612),
        accentGlowColor = Color(0xFFF472B6),
        secondaryGlowColor = Color(0xFFC084FC),
        isTransparentGlass = true
    );

    companion object {
        fun fromId(id: String?): RoomBackgroundTheme =
            entries.firstOrNull { it.id == id } ?: PALACE_NIGHT
    }
}

/**
 * Store & Accessories Item Categories:
 */
enum class StoreItemCategory(val titleAr: String, val iconEmoji: String) {
    FRAMES("قسم الإطارات", "⭕"),
    ENTRY_MOUNTS("قسم الدخوليات", "🏎️"),
    SPECIAL_IDS("قسم الآي دي", "🆔"),
    ROOM_THEMES("ثيمات الغرف", "🌌"),
    CHAT_BUBBLES("إطارات الدردشة", "💬")
}

/**
 * Purchasable Chat Bubble Frame Styles (`إطارات قاعة الدردشة من المتجر`).
 * By default (`TRANSPARENT_NONE`), writing in rooms is 100% transparent with NO border (`دائما الكتابه في الغرف تكون شفافه ولا يظهر حولها اطار`).
 * Purchasing and equipping a chat frame from the Store adds a glowing frame around the user's messages!
 */
enum class ChatBubbleFrameStyle(
    val id: String,
    val titleAr: String,
    val badgeEmoji: String,
    val hasBorderFrame: Boolean,
    val borderPrimaryColor: Color,
    val borderSecondaryColor: Color,
    val backgroundTint: Color
) {
    TRANSPARENT_NONE(
        id = "NONE",
        titleAr = "كتابة شفافة بدون إطار (افتراضي)",
        badgeEmoji = "",
        hasBorderFrame = false,
        borderPrimaryColor = Color.Transparent,
        borderSecondaryColor = Color.Transparent,
        backgroundTint = Color.Transparent
    ),
    ROYAL_GOLD_SCROLL(
        id = "ROYAL_GOLD_SCROLL",
        titleAr = "إطار المخطوطة الإمبراطورية الذهبية 👑",
        badgeEmoji = "👑",
        hasBorderFrame = true,
        borderPrimaryColor = Color(0xFFFFD700),
        borderSecondaryColor = Color(0xFFF59E0B),
        backgroundTint = Color(0xFF422006).copy(alpha = 0.55f)
    ),
    DRAGON_FIRE_FRAME(
        id = "DRAGON_FIRE_FRAME",
        titleAr = "إطار لهب التنين الناري 🔥",
        badgeEmoji = "🔥",
        hasBorderFrame = true,
        borderPrimaryColor = Color(0xFFFF5252),
        borderSecondaryColor = Color(0xFFFF9100),
        backgroundTint = Color(0xFF450A0A).copy(alpha = 0.55f)
    ),
    CRYSTAL_ICE_FRAME(
        id = "CRYSTAL_ICE_FRAME",
        titleAr = "إطار الكريستال الجليدي الأزرق ❄️",
        badgeEmoji = "❄️",
        hasBorderFrame = true,
        borderPrimaryColor = Color(0xFF00E5FF),
        borderSecondaryColor = Color(0xFF38BDF8),
        backgroundTint = Color(0xFF082F49).copy(alpha = 0.55f)
    ),
    ROSE_DIAMOND_FRAME(
        id = "ROSE_DIAMOND_FRAME",
        titleAr = "إطار ياقوت الأميرات الوردي 🌸",
        badgeEmoji = "🌸",
        hasBorderFrame = true,
        borderPrimaryColor = Color(0xFFF472B6),
        borderSecondaryColor = Color(0xFFC084FC),
        backgroundTint = Color(0xFF500724).copy(alpha = 0.55f)
    );

    companion object {
        fun fromName(nameOrId: String?): ChatBubbleFrameStyle {
            if (nameOrId.isNullOrBlank()) return TRANSPARENT_NONE
            return when {
                nameOrId.contains("الذهبية") || nameOrId.contains("المخطوطة") || nameOrId == "ROYAL_GOLD_SCROLL" -> ROYAL_GOLD_SCROLL
                nameOrId.contains("التنين") || nameOrId.contains("لهب") || nameOrId == "DRAGON_FIRE_FRAME" -> DRAGON_FIRE_FRAME
                nameOrId.contains("الكريستال") || nameOrId.contains("الجليدي") || nameOrId == "CRYSTAL_ICE_FRAME" -> CRYSTAL_ICE_FRAME
                nameOrId.contains("ياقوت") || nameOrId.contains("الأميرات") || nameOrId == "ROSE_DIAMOND_FRAME" -> ROSE_DIAMOND_FRAME
                else -> entries.firstOrNull { it.id.equals(nameOrId, ignoreCase = true) || it.titleAr == nameOrId } ?: ROYAL_GOLD_SCROLL
            }
        }
    }
}

/**
 * Database-Configurable Microphone Seat Shape inside Voice Rooms (`تغيير شكل المايكات وأي شكل في الغرفة من قاعدة البيانات`).
 */
enum class RoomMicShapeStyle(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val iconEmoji: String,
    val cornerPercent: Int,
    val borderStrokeDp: Float,
    val primaryGlowColor: Color,
    val secondaryGlowColor: Color
) {
    CIRCLE(
        id = "CIRCLE",
        titleAr = "مايك دائري كلاسيكي 🎙️",
        subtitleAr = "الشكل الدائري الشفاف المطابق للواجهات الرئيسية",
        iconEmoji = "⭕",
        cornerPercent = 50,
        borderStrokeDp = 1.4f,
        primaryGlowColor = Color(0xFF38BDF8),
        secondaryGlowColor = Color(0xFFA855F7)
    ),
    ROYAL_CIRCLE(
        id = "ROYAL_CIRCLE",
        titleAr = "مايك دائري ملكي ⭕",
        subtitleAr = "مايك دائري بموجات صوتية نيون",
        iconEmoji = "⭕",
        cornerPercent = 50,
        borderStrokeDp = 1.6f,
        primaryGlowColor = Color(0xFF38BDF8),
        secondaryGlowColor = Color(0xFFA855F7)
    ),
    ROYAL_HEXAGON(
        id = "ROYAL_HEXAGON",
        titleAr = "مايك سداسي ملكي ⬡",
        subtitleAr = "إطار مايك هندسي سداسي بلمعة كريستالية",
        iconEmoji = "💠",
        cornerPercent = 24,
        borderStrokeDp = 2.0f,
        primaryGlowColor = Color(0xFFFFD700),
        secondaryGlowColor = Color(0xFF9333EA)
    ),
    IMPERIAL_HEXAGON(
        id = "IMPERIAL_HEXAGON",
        titleAr = "مايك سداسي إمبراطوري ⬡",
        subtitleAr = "شكل سداسي هندسي مرصع بالذهب",
        iconEmoji = "⬡",
        cornerPercent = 24,
        borderStrokeDp = 2.0f,
        primaryGlowColor = Color(0xFFFFD700),
        secondaryGlowColor = Color(0xFF9333EA)
    ),
    ROUNDED_SHIELD(
        id = "ROUNDED_SHIELD",
        titleAr = "مايك درع القصر 🛡️",
        subtitleAr = "مايك مربع بحواف ناعمة ودرع ملكي",
        iconEmoji = "🛡️",
        cornerPercent = 28,
        borderStrokeDp = 1.8f,
        primaryGlowColor = Color(0xFFEC4899),
        secondaryGlowColor = Color(0xFF8B5CF6)
    ),
    DIAMOND_CREST(
        id = "DIAMOND_CREST",
        titleAr = "مايك الجوهرة الماسية 💎",
        subtitleAr = "مايك بتاج ماسي متوهج ثلاثي الأبعاد",
        iconEmoji = "💎",
        cornerPercent = 36,
        borderStrokeDp = 2.2f,
        primaryGlowColor = Color(0xFF00E5FF),
        secondaryGlowColor = Color(0xFF3B82F6)
    ),
    DIAMOND_CRYSTAL(
        id = "DIAMOND_CRYSTAL",
        titleAr = "مايك الكريستال الماسي 💎",
        subtitleAr = "مايك بهندسة الجوهرة الكريستالية",
        iconEmoji = "💎",
        cornerPercent = 34,
        borderStrokeDp = 2.2f,
        primaryGlowColor = Color(0xFF00E5FF),
        secondaryGlowColor = Color(0xFF3B82F6)
    ),
    CROWN_RING(
        id = "CROWN_RING",
        titleAr = "مايك التاج الإمبراطوري 👑",
        subtitleAr = "مايك دائري محاط بحلقة تاج ذهبي",
        iconEmoji = "👑",
        cornerPercent = 50,
        borderStrokeDp = 2.4f,
        primaryGlowColor = Color(0xFFFBBF24),
        secondaryGlowColor = Color(0xFFF43F5E)
    ),
    GOLDEN_CROWN_THRONE(
        id = "GOLDEN_CROWN_THRONE",
        titleAr = "مايك عرش السلطان الذهبي 👑",
        subtitleAr = "مايك ملكي بتاج ذهبي متوهج",
        iconEmoji = "👑",
        cornerPercent = 28,
        borderStrokeDp = 2.4f,
        primaryGlowColor = Color(0xFFFBBF24),
        secondaryGlowColor = Color(0xFFF43F5E)
    ),
    LOTUS_BLOSSOM(
        id = "LOTUS_BLOSSOM",
        titleAr = "مايك زهرة اللوتس 🌸",
        subtitleAr = "مايك بتصميم بتلات اللوتس الملكية",
        iconEmoji = "🌸",
        cornerPercent = 38,
        borderStrokeDp = 1.8f,
        primaryGlowColor = Color(0xFFF472B6),
        secondaryGlowColor = Color(0xFFC084FC)
    ),
    CYBER_NEON_RING(
        id = "CYBER_NEON_RING",
        titleAr = "مايك النيون السماوي ⚡",
        subtitleAr = "مايك دائري بحلقة ليزر نيون 7D",
        iconEmoji = "⚡",
        cornerPercent = 50,
        borderStrokeDp = 2.0f,
        primaryGlowColor = Color(0xFF00E5FF),
        secondaryGlowColor = Color(0xFF10B981)
    );

    val ringPrimaryColor: Color get() = primaryGlowColor
    val ringSecondaryColor: Color get() = secondaryGlowColor

    companion object {
        fun fromId(id: String?): RoomMicShapeStyle =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CIRCLE
    }
}

/**
 * Swipeable & Database-Configurable Home Screen Banner Model (`البنارات في الشاشة الرئيسية قابلة للسحب والتبديل وقابلة للتعيين من قاعدة البيانات`).
 */
data class HomeBannerSlideItem(
    val id: String,
    val titleAr: String = "مكافآت الشحن الملكية",
    val subtitleAr: String = "اشحن الآن واحصل على سيارات ذهبية وهدايا مضاعفة 7D",
    val badgeTextAr: String = "مكافآت الشحن 🪙",
    val iconEmoji: String = "🏎️",
    val gradientStartHex: String = "#3B2004",
    val gradientMidHex: String = "#6E3E07",
    val gradientEndHex: String = "#1C1108",
    val accentColorHex: String = "#FFE57F",
    val targetSubScreen: StandaloneSubScreen = StandaloneSubScreen.COINS_ONLY,
    val customImageUri: String? = null,
    val sortOrder: Int = 1,
    val isVisible: Boolean = true
) {
    val titleEn: String get() = titleAr
    val badgeTagAr: String get() = badgeTextAr
    val orderIndex: Int get() = sortOrder
    val isActive: Boolean get() = isVisible
    val gradientStartColor: Color get() = parseHexColorSafe(gradientStartHex, Color(0xFF4C1D95))
    val gradientCenterColor: Color get() = parseHexColorSafe(gradientMidHex, Color(0xFF7C3AED))
    val gradientEndColor: Color get() = parseHexColorSafe(gradientEndHex, Color(0xFF1E1B4B))
}

private fun parseHexColorSafe(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        val value = when (clean.length) {
            6 -> ("FF$clean").toLong(16)
            8 -> clean.toLong(16)
            else -> return fallback
        }
        Color(value)
    } catch (_: Exception) {
        fallback
    }
}

data class StoreCatalogItem(
    val id: String,
    val nameAr: String,
    val subtitleAr: String,
    val category: StoreItemCategory,
    val priceCoins: Long,
    val durationDays: Int = 30,
    val dimensionTag: String = "7D SVGA",
    val iconEmoji: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val frameStyle: FrameStyle3D? = null,
    val specialIdValue: String? = null,
    val roomThemeId: String? = null,
    val assetFormat: String = "ALL (SVGA • GIF • WEBP • MP4 • PNG)",
    val customAssetUri: String? = null,
    val isEquipped: Boolean = false,
    val isOwned: Boolean = false,
    val isVisible: Boolean = true
)

/**
 * 5 Configurable VIP Sections (`الفي اي بي تعرض خمس اقسام يتم ظبطهم لاحقا من قاعده البيانات`).
 */
data class VipSectionConfig(
    val vipTier: Int, // 1..5
    val titleAr: String,
    val badgeTitleAr: String,
    val priceCoins: Long,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val soundWaveColorHex: String,
    val coloredNameHex: String,
    val grantedFrameStyle: FrameStyle3D,
    val grantedEntryMountNameAr: String,
    val perksAr: List<String>,
    val isVisible: Boolean = true
)

/**
 * Host Member Record in the Host Agency Center (`تظهر في مركز الوكالة للوكيل بيانات المستخدم على المايك وداخل التطبيق وقيمة الهدايا والساعات`).
 */
data class HostAgencyMemberRecord(
    val userId: String,
    val nickname: String,
    val avatarType: String = "PRINCESS",
    val customAvatarUri: String? = null,
    val frameStyle: FrameStyle3D = FrameStyle3D.ROSE_GOLD_PHOENIX,
    val isOnMicNow: Boolean,
    val currentRoomNameAr: String,
    val isOnlineInApp: Boolean,
    val totalGiftDiamondsReceived: Long,
    val totalMicHours: Int,
    val activeMicDays: Int,
    val expectedSalaryUsd: Int
)

/**
 * Charge Agent Reward Item (`مكافآت يرسلها وكيل الشحن لأي مستخدم مثل الإطارات والدخوليات`).
 */
data class ChargeAgentRewardItem(
    val id: String,
    val nameAr: String,
    val category: StoreItemCategory, // FRAMES or ENTRY_MOUNTS
    val iconEmoji: String,
    val durationDays: Int,
    val frameStyle: FrameStyle3D? = null
)

/**
 * Charge Agent Shipment / Reward Log Entry (`سجل شحن العملات وإرسال المكافآت في مركز وكالة الشحن`).
 */
data class ChargeAgentShipmentLog(
    val id: String,
    val targetUserId: String,
    val descriptionAr: String,
    val coinsShipped: Long,
    val rewardNameAr: String? = null,
    val timestampText: String
)

/**
 * Current User Profile State.
 */
data class UserProfile(
    val uuid: String,
    val displayId: String,
    val isSpecialId: Boolean,
    val email: String,
    val nickname: String,
    val bio: String = "👑 مرحباً بكم في غرفتي الصوتية الملكية • زاديرا لايف 7D ✨",
    val avatarType: String = "PRINCE",
    val customAvatarUri: String? = null,
    val customCoverUri: String? = null,
    val countryFlag: String = "🇪🇬",
    val countryNameAr: String = "مصر",
    val role: AppUserRole = AppUserRole.SUPPORTER,
    val wealthLevel: Int = 45,
    val wealthExpCurrent: Long = 78500,
    val wealthExpTarget: Long = 100000,
    val charismaLevel: Int = 38,
    val charismaExpCurrent: Long = 54000,
    val charismaExpTarget: Long = 80000,
    val vipTier: Int = 5, // 1..5
    val goldCoins: Long = 285000,
    val crystalDiamonds: Long = 142000,
    val frameStyle: FrameStyle3D = FrameStyle3D.IMPERIAL_GOLD_WINGS,
    val entryWelcomeName: String = "تنين الإمبراطور الذهبي 7D",
    val equippedChatBubbleName: String = "",
    val equippedSoundWaveName: String = "موجة النبض الإمبراطوري الذهبي",
    val coloredNameHex: String = "#FFD700",
    val soundWaveColorHex: String = "#00E5FF",
    val agencyName: String = "",
    val isHostAgent: Boolean = false,
    val isHostMember: Boolean = false,
    val hostMicHours: Int = 0,
    val hostActiveDays: Int = 0,
    val hostGiftsDiamonds: Long = 0L,
    val isChargeAgent: Boolean = false,
    val chargeAgentCoinsBalance: Long = 0L,
    val familyNameAr: String = "قبيلة صقور العرب 🦅",
    val familyRankAr: String = "قائد القبيلة 👑",
    val cpPartnerName: String = "الأميرة شهد 🌸",
    val cpPartnerId: String = "888888",
    val cpPoints: Long = 52400,
    val friendsCount: Int = 1280,
    val followersCount: Int = 45900,
    val followingCount: Int = 310,
    val visitorsCount: Int = 8420,
    val sentGiftsCount: Long = 128500L,
    val receivedGiftsCount: Long = 340000L,
    val checkInStreakDays: Int = 4,
    val isCheckedInToday: Boolean = false,
    val badges: List<BadgeModel> = emptyList(),
    val appLanguageNameAr: String = "العربية",
    val invitedFriendsCount: Int = 3,
    val claimedInviteMilestones: List<Int> = emptyList(),
    val isAccountLinkedGoogle: Boolean = true,
    val isAccountLinkedPhone: Boolean = true,
    val isBanned: Boolean = false
) {
    val wealthXpCurrent: Long get() = wealthExpCurrent
    val wealthXpTarget: Long get() = wealthExpTarget
    val charismaXpCurrent: Long get() = charismaExpCurrent
    val charismaXpTarget: Long get() = charismaExpTarget
    val cpIntimacyLevel: Int get() = ((cpPoints / 5000L) + 1L).toInt()
    val cpIntimacyScore: Long get() = cpPoints
    val isApprovedHost: Boolean get() = isHostMember
    val bioTextAr: String get() = bio
    val fansCount: Int get() = followersCount
    val clanNameAr: String get() = familyNameAr
    val clanRoleAr: String get() = familyRankAr
    val equippedChatBubbleStyle: ChatBubbleFrameStyle get() = ChatBubbleFrameStyle.fromName(equippedChatBubbleName)
}

data class BadgeModel(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val categoryAr: String = "ملكي",
    val iconEmoji: String,
    val tierText: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val isUnlocked: Boolean = true,
    val levelRequired: Int = 10
) {
    val titleEn: String get() = tierText
    val levelText: String get() = tierText
    val descriptionAr: String get() = subtitleAr
    val category: String get() = categoryAr
    val glowColor: Color get() = primaryColor
}

/**
 * Seat on the 10-Seat Audio Stage inside a Voice Room.
 */
data class MicSeatState(
    val seatIndex: Int, // 0 = Host Seat, 1..7 = Speaker Seats, 8..9 = VIP Seats
    val seatLabelAr: String = "${seatIndex + 1}",
    val isVipSeat: Boolean = false,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val occupantUserId: String? = null,
    val occupantName: String? = null,
    val occupantDisplayId: String? = null,
    val occupantAvatarType: String = "PRINCE",
    val occupantCustomAvatarUri: String? = null,
    val occupantFrame: FrameStyle3D = FrameStyle3D.IMPERIAL_GOLD_WINGS,
    val occupantRole: AppUserRole = AppUserRole.USER,
    val occupantRoomRole: RoomPermissionRole = RoomPermissionRole.MEMBER,
    val occupantVip: Int = 1,
    val occupantWealthLevel: Int = 15,
    val occupantCharismaLevel: Int = 12,
    val occupantMultiplierBadge: String? = null,
    val isSpeaking: Boolean = false,
    val seatGiftPoints: Long = 0L,
    val soundWaveColor: Color = Color(0xFF00E5FF),
    val nameColor: Color = Color(0xFFFFF59D),
    val activeReactionEmoji: String? = null,
    val reactionTimestamp: Long = 0L
) {
    val occupantId: String? get() = occupantDisplayId ?: occupantUserId
    val occupantVipTier: Int get() = occupantVip
    val giftCoinsCounter: Long get() = seatGiftPoints
    val isEmpty: Boolean get() = occupantName == null
    val activeEmojiReaction: String? get() = activeReactionEmoji
}

/**
 * Live PK Battle state inside the Voice Room.
 */
data class PkBattleState(
    val isActive: Boolean = true,
    val redTeamNameAr: String = "فريق محمد 👑",
    val blueTeamNameAr: String = "فريق الأميرة شهد 🌸",
    val redScore: Long = 158400,
    val blueScore: Long = 142900,
    val remainingSeconds: Int = 165
)

/**
 * Voice Chat Room model shown on the Home Lobby and inside the Live Audio Room.
 */
data class VoiceRoomModel(
    val id: String,
    val roomDisplayId: String,
    val titleAr: String,
    val announcementAr: String,
    val categoryAr: String,
    val countryFlag: String,
    val hostName: String,
    val hostUserId: String,
    val myRoleInRoom: RoomPermissionRole = RoomPermissionRole.OWNER,
    val onlineCount: Int,
    val heatScore: Long,
    val coverBadgeText: String,
    val customCoverImageUri: String? = null,
    val hostCustomAvatarUri: String? = null,
    val isSvga7DRoom: Boolean = true,
    val backgroundStyleId: String = "PALACE_NIGHT",
    val micShapeStyleId: String = "CIRCLE",
    val seats: List<MicSeatState> = emptyList(),
    val pkBattle: PkBattleState = PkBattleState(),
    val activeLuckyBagCoins: Long = 8888L,
    val adminUserIds: List<String> = emptyList(),
    val bannedUserIds: List<String> = emptyList(),
    val followingUserIds: List<String> = emptyList(),
    val countryNameAr: String = "العالم العربي",
    val hostRole: AppUserRole = AppUserRole.ROOM_OWNER,
    val hostAvatarType: String = "PRINCE",
    val hostFrame: FrameStyle3D = FrameStyle3D.IMPERIAL_GOLD_WINGS,
    val hasActiveEvent: Boolean = true,
    val seatAvatars: List<String> = listOf("PRINCE", "PRINCESS", "SULTAN", "QUEEN"),
    val multiplierText: String? = coverBadgeText
) {
    val categoryTag: String get() = categoryAr
    val onlineUsersCount: Int get() = onlineCount
    val is3DPalaceTheme: Boolean get() = isSvga7DRoom
    val roomId: String get() = id
    val title: String get() = titleAr
    val displayCode: String get() = roomDisplayId
    val cardFrameStyle: FrameStyle3D get() = hostFrame
    val tagBadgeText: String get() = categoryAr
    val topGifters: List<String> get() = listOf("محمد 👑", "الأميرة شهد 🌸", "السلطان فهد 🦁")
    val micSeats: List<MicSeatState> get() = seats
    val isPkBattleActive: Boolean get() = pkBattle.isActive
    val pkRedScore: Long get() = pkBattle.redScore
    val pkBlueScore: Long get() = pkBattle.blueScore
    val pkRemainingSeconds: Int get() = pkBattle.remainingSeconds
    val coverFrameType: String get() = coverBadgeText
}

typealias VoiceRoom = VoiceRoomModel
typealias VoiceRoomItem = VoiceRoomModel

data class RoomChatMessage(
    val id: String,
    val senderUserId: String = "user_other",
    val senderName: String,
    val senderDisplayId: String,
    val senderRole: AppUserRole,
    val senderRoomRole: RoomPermissionRole = RoomPermissionRole.MEMBER,
    val senderVip: Int,
    val senderWealthLevel: Int,
    val senderCharismaLevel: Int = 12,
    val senderAvatarType: String = "PRINCE",
    val senderCustomAvatarUri: String? = null,
    val senderChatBubbleFrame: String = "",
    val messageText: String,
    val isGiftAnnouncement: Boolean = false,
    val isSystemWelcome: Boolean = false,
    val giftIconEmoji: String? = null,
    val highlightColor: Color = Color(0xFFFFD700)
) {
    val contentAr: String get() = messageText
    val chatBubbleStyle: ChatBubbleFrameStyle get() = ChatBubbleFrameStyle.fromName(senderChatBubbleFrame)
}

enum class SvgaEffectType {
    GOLDEN_DRAGON_7D,
    ROYAL_PALACE_CASTLE_7D,
    LUXURY_YACHT_6D,
    SPORTS_CAR_BUGATTI_5D,
    CRYSTAL_PHOENIX_7D,
    DIAMOND_CROWN_4D,
    ROMANTIC_ROSES_3D
}

data class GiftItem3D(
    val id: String,
    val nameAr: String,
    val priceCoins: Long,
    val categoryAr: String,
    val dimensionBadge: String,
    val iconEmoji: String,
    val effectType: SvgaEffectType,
    val primaryGlow: Color,
    val secondaryGlow: Color,
    val receivedCount: Int = 12
) {
    val dimensionLevel: String get() = dimensionBadge
    val badgeTag: String get() = dimensionBadge
    val svgaEffectType: SvgaEffectType get() = effectType
    val category: String get() = categoryAr
    val primaryColor: Color get() = primaryGlow
    val secondaryColor: Color get() = secondaryGlow
    val iconSymbol: String get() = iconEmoji
    val diamondValue: Long get() = priceCoins / 2
    val titleAr: String get() = nameAr
    val coinPrice: Long get() = priceCoins
}

typealias GiftItem = GiftItem3D
typealias GiftCatalogItem = GiftItem3D

enum class GameArtworkTheme {
    SHARK_HUNTER,
    DRAGON_VS_TIGER,
    GREEDY_FRUIT,
    TEEN_PATTI,
    SOCCER_STRIKER,
    SLOTS_777,
    OLYMPUS_ZEUS,
    TRENZY_CARS,
    FOOTBALL_CLUB,
    SPEED_CAR,
    ROYAL_KING,
    GOLDEN_EMPIRE
}

data class WinTickerNotice(
    val id: String,
    val winnerName: String,
    val winAmount: Long,
    val gameTitleAr: String
)

data class CasualGameItem(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val badgeText: String,
    val iconEmoji: String,
    val minBetCoins: Long,
    val maxMultiplier: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val playersOnline: Int = 1480,
    val jackpotPool: Long = 2500000L,
    val is3DAnimated: Boolean = true,
    val artworkTheme: GameArtworkTheme = GameArtworkTheme.GOLDEN_EMPIRE
) {
    val subtitleEn: String get() = subtitleAr
    val category: String get() = badgeText
    val minBet: Long get() = minBetCoins
    val bannerResType: String get() = when {
        id.contains("wheel", ignoreCase = true) -> "WHEEL"
        id.contains("slot", ignoreCase = true) -> "SLOTS"
        else -> "DRAGON"
    }
}

data class FriendPostMoment(
    val id: String,
    val authorName: String,
    val authorDisplayId: String = "8829410",
    val authorCountryFlag: String = "🇪🇬",
    val authorRole: AppUserRole = AppUserRole.USER,
    val authorFrame: FrameStyle3D = FrameStyle3D.NONE,
    val authorAvatarType: String = "PRINCESS",
    val authorCustomAvatarUri: String? = null,
    val postImageUri: String? = null,
    val timeAgoAr: String,
    val contentAr: String,
    val roomTagAr: String = "",
    val likesCount: Int,
    val commentsCount: Int,
    val giftCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isFollowingAuthor: Boolean = false,
    val attachedGift: GiftItem3D? = null,
    val authorLevel: Int = 38,
    val voiceDurationSec: Int = 18,
    val comments: List<String> = listOf(
        "الأميرة شهد 🌸: سهرة ملكية رائعة ما شاء الله! ✨",
        "السلطان فهد 🦁: بالتوفيق يا ملوك الغرفة 🔥"
    )
) {
    constructor(
        id: String,
        authorName: String,
        authorId: String,
        authorAvatarType: String,
        authorVipTier: Int,
        authorWealthLevel: Int,
        timeAgoAr: String,
        contentAr: String,
        attachedRoomTitle: String,
        likesCount: Int,
        commentsCount: Int,
        isLikedByMe: Boolean
    ) : this(
        id = id,
        authorName = authorName,
        authorDisplayId = authorId,
        authorAvatarType = authorAvatarType,
        timeAgoAr = timeAgoAr,
        contentAr = contentAr,
        roomTagAr = attachedRoomTitle,
        likesCount = likesCount,
        commentsCount = commentsCount,
        isLikedByMe = isLikedByMe,
        authorLevel = authorWealthLevel
    )

    val userName: String get() = authorName
    val userRole: AppUserRole get() = authorRole
    val avatarType: String get() = authorAvatarType
    val frameStyle: FrameStyle3D get() = authorFrame
    val timeAgo: String get() = timeAgoAr
    val AttachedRoomTitle: String get() = roomTagAr
    val isLiked: Boolean get() = isLikedByMe
    val authorCountry: String get() = authorCountryFlag
    val authorId: String get() = authorDisplayId
    val giftsCount: Int get() = giftCount
}

typealias FriendMomentPost = FriendPostMoment
typealias DiscoverMomentPost = FriendPostMoment

data class DirectMessageThread(
    val id: String,
    val friendName: String,
    val friendDisplayId: String,
    val friendRole: AppUserRole,
    val friendFrame: FrameStyle3D,
    val friendAvatarType: String,
    val friendCustomAvatarUri: String? = null,
    val isOnline: Boolean,
    val unreadCount: Int,
    val messages: List<ChatBubbleMessage>,
    val level: Int = 34,
    val countryFlag: String = "🇸🇦"
) {
    val userId: String get() = friendDisplayId
    val friendId: String get() = friendDisplayId
    val userName: String get() = friendName
    val userRole: AppUserRole get() = friendRole
    val role: AppUserRole get() = friendRole
    val avatarType: String get() = friendAvatarType
    val frameStyle: FrameStyle3D get() = friendFrame
    val friendSpecialId: String get() = friendDisplayId
    val lastMessage: String get() = messages.lastOrNull()?.text ?: ""
    val lastMessageAr: String get() = lastMessage
    val lastTime: String get() = messages.lastOrNull()?.timestampAr ?: "الآن"
    val lastTimeAr: String get() = lastTime
}

typealias FriendConversation = DirectMessageThread
typealias MessageThread = DirectMessageThread

data class ChatBubbleMessage(
    val id: String,
    val senderIsMe: Boolean,
    val text: String,
    val timestampAr: String,
    val coinsGiftAmount: Long? = null,
    val senderName: String = if (senderIsMe) "أنا" else "صديق"
) {
    constructor(
        id: String,
        senderName: String,
        textAr: String,
        timeAr: String,
        isFromMe: Boolean
    ) : this(
        id = id,
        senderIsMe = isFromMe,
        text = textAr,
        timestampAr = timeAr,
        senderName = senderName
    )

    val timeText: String get() = timestampAr
    val timeAr: String get() = timestampAr
    val textAr: String get() = text
    val isFromMe: Boolean get() = senderIsMe
}

typealias DirectChatMessage = ChatBubbleMessage

data class WalletTransactionItem(
    val id: String,
    val titleAr: String,
    val amountText: String,
    val dateText: String,
    val isPositive: Boolean
)

data class AgencyInfoModel(
    val id: String,
    val nameAr: String,
    val code: String,
    val agentTypeAr: String,
    val membersCount: Int,
    val monthlyVolumeDiamonds: String,
    val commissionRateText: String,
    val contactId: String
)

data class DailyTaskItem(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val rewardCoins: Long,
    val rewardExp: Int,
    val progressCurrent: Int,
    val progressTarget: Int,
    val isClaimed: Boolean = false,
    val iconEmoji: String = "🎁"
)

data class LeaderboardEntry(
    val rank: Int,
    val nameAr: String,
    val displayId: String,
    val countryFlag: String,
    val avatarType: String,
    val frameStyle: FrameStyle3D,
    val level: Int,
    val vipTier: Int,
    val scoreValue: Long,
    val scoreLabel: String,
    val customAvatarUri: String? = null
)

enum class RoomChatFilterTab(val titleAr: String) {
    ALL("الكل"),
    CHAT("الدردشة"),
    ROOM("الغرفة")
}

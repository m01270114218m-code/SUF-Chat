-- ============================================================================
-- ZADIRA LIVE - COMPLETE SERVER-DRIVEN VOICE CHAT DATABASE SCHEMA (SQL)
-- Supports: PostgreSQL / MySQL / Supabase / Cloud SQL
-- Controls 100% of App UI Styles, Themes, Buttons, Roles, Agencies, Special IDs,
-- Entrance Welcomes (SVGA), Level Rules, Bans, Voice Rooms, Store, and Gifts.
-- ============================================================================

-- 1. DYNAMIC UI THEME & COMPONENT STYLE CONFIGURATION TABLE
-- Every color, gradient, button shape, 3D border bevel, and asset URL in the app
-- can be modified dynamically from this table without updating the APK.
CREATE TABLE IF NOT EXISTS app_theme_style_config (
    config_key VARCHAR(100) PRIMARY KEY,
    theme_name VARCHAR(120) NOT NULL DEFAULT 'Royal Arabian Night 7D',
    primary_bg_hex VARCHAR(16) NOT NULL DEFAULT '#14072B',
    secondary_bg_hex VARCHAR(16) NOT NULL DEFAULT '#240E4B',
    card_surface_hex VARCHAR(16) NOT NULL DEFAULT '#2D1459',
    gold_primary_hex VARCHAR(16) NOT NULL DEFAULT '#FFD700',
    gold_secondary_hex VARCHAR(16) NOT NULL DEFAULT '#D4AF37',
    gold_deep_hex VARCHAR(16) NOT NULL DEFAULT '#996515',
    crystal_blue_hex VARCHAR(16) NOT NULL DEFAULT '#00E5FF',
    neon_magenta_hex VARCHAR(16) NOT NULL DEFAULT '#FF2A85',
    button_corner_radius_dp INT NOT NULL DEFAULT 22,
    card_corner_radius_dp INT NOT NULL DEFAULT 18,
    border_bevel_thickness_dp FLOAT NOT NULL DEFAULT 2.5,
    glow_intensity FLOAT NOT NULL DEFAULT 0.85,
    button_3d_elevation_dp INT NOT NULL DEFAULT 10,
    enable_7d_particles BOOLEAN NOT NULL DEFAULT TRUE,
    enable_svga_animations BOOLEAN NOT NULL DEFAULT TRUE,
    default_room_bg_url TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. ADMINISTRATIVE ROLES & PERMISSIONS TABLE
-- Roles supported:
--   SUPER_ADMIN   (سوبر أدمن - تحكم كامل بالمنصة)
--   ADMIN         (أدمن - إدارة الغرف والمستخدمين والحظر)
--   MANAGER       (مدير - إدارة المشرفين والفعاليات)
--   BD            (بدي Business Development - إدارة الوكالات وتعيينها)
--   CHARGE_AGENT  (وكيل شحن - شحن العملات الذهبية للمستخدمين)
--   HOST_AGENT    (وكيل مضيفين - إدارة المضيفين والرواتب)
--   HOST          (مضيف - صاحب أو مذيع الغرفة الصوتية)
--   SUPPORTER     (داعم VIP - كبار الداعمين والنبلاء)
--   USER          (مستخدم عادي)
CREATE TABLE IF NOT EXISTS role_definitions (
    role_code VARCHAR(40) PRIMARY KEY,
    role_name_ar VARCHAR(100) NOT NULL,
    role_name_en VARCHAR(100) NOT NULL,
    badge_color_hex VARCHAR(16) NOT NULL,
    badge_icon_url TEXT,
    can_ban_users BOOLEAN DEFAULT FALSE,
    can_assign_admins BOOLEAN DEFAULT FALSE,
    can_assign_special_id BOOLEAN DEFAULT FALSE,
    can_grant_entry_welcome BOOLEAN DEFAULT FALSE,
    can_modify_user_level BOOLEAN DEFAULT FALSE,
    can_recharge_coins BOOLEAN DEFAULT FALSE,
    can_manage_hosts BOOLEAN DEFAULT FALSE,
    can_lock_any_room BOOLEAN DEFAULT FALSE,
    priority_rank INT NOT NULL DEFAULT 1
);

-- 3. USERS MASTER TABLE
CREATE TABLE IF NOT EXISTS users (
    user_uuid VARCHAR(64) PRIMARY KEY,
    display_id VARCHAR(32) UNIQUE NOT NULL,        -- Standard or Special ID (آي دي مميز)
    is_special_id BOOLEAN DEFAULT FALSE,
    special_id_tier VARCHAR(32) DEFAULT 'NONE',    -- ROYAL_GOLD, DIAMOND_7D, IMPERIAL
    email VARCHAR(180) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    avatar_url TEXT,
    country_code VARCHAR(8) DEFAULT 'EG',
    country_name_ar VARCHAR(64) DEFAULT 'مصر',
    gender VARCHAR(16) DEFAULT 'MALE',
    bio TEXT DEFAULT 'مرحباً بكم في عالمي الملكي ✨',
    role_code VARCHAR(40) DEFAULT 'USER' REFERENCES role_definitions(role_code),
    wealth_level INT DEFAULT 1,
    wealth_exp BIGINT DEFAULT 0,
    charisma_level INT DEFAULT 1,
    charisma_exp BIGINT DEFAULT 0,
    vip_tier INT DEFAULT 0,                        -- VIP 0 to VIP 10
    gold_coins BIGINT DEFAULT 5000,
    crystal_diamonds BIGINT DEFAULT 0,
    equipped_frame_id VARCHAR(64) DEFAULT 'frame_royal_wings',
    equipped_entry_welcome_id VARCHAR(64) DEFAULT 'welcome_golden_pegasus',
    equipped_bubble_id VARCHAR(64) DEFAULT 'bubble_imperial_gold',
    agency_id VARCHAR(64) DEFAULT NULL,
    is_banned BOOLEAN DEFAULT FALSE,
    ban_reason TEXT DEFAULT NULL,
    ban_expires_at TIMESTAMP DEFAULT NULL,
    friends_count INT DEFAULT 0,
    following_count INT DEFAULT 0,
    fans_count INT DEFAULT 0,
    visitors_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. SPECIAL IDS REGISTRY (جدول الآي دي المميز)
CREATE TABLE IF NOT EXISTS special_ids_registry (
    special_id VARCHAR(32) PRIMARY KEY,
    tier_name VARCHAR(50) NOT NULL,                -- 3D_GOLD, 5D_CRYSTAL, 7D_EMPEROR
    badge_frame_asset TEXT NOT NULL,
    glow_color_hex VARCHAR(16) NOT NULL DEFAULT '#FFD700',
    price_coins BIGINT NOT NULL DEFAULT 50000,
    assigned_to_user_uuid VARCHAR(64) REFERENCES users(user_uuid),
    assigned_by_admin_uuid VARCHAR(64),
    assigned_at TIMESTAMP,
    expires_at TIMESTAMP
);

-- 5. ENTRY WELCOMES / ENTRANCE EFFECTS (جدول الترحيبيات والمؤثرات المتحركة SVGA/3D)
CREATE TABLE IF NOT EXISTS entry_welcomes_catalog (
    welcome_id VARCHAR(64) PRIMARY KEY,
    name_ar VARCHAR(120) NOT NULL,
    name_en VARCHAR(120) NOT NULL,
    animation_type VARCHAR(32) NOT NULL DEFAULT 'SVGA_7D', -- SVGA_3D, SVGA_5D, SVGA_7D
    svga_asset_url TEXT NOT NULL,
    sound_effect_url TEXT,
    banner_color_start_hex VARCHAR(16) DEFAULT '#FFD700',
    banner_color_end_hex VARCHAR(16) DEFAULT '#FF007F',
    price_coins BIGINT DEFAULT 25000,
    min_vip_required INT DEFAULT 1,
    duration_ms INT DEFAULT 6000
);

-- 6. LEVEL PROGRESSION RULES (جدول رفع المستوى وقواعد الخبرة)
CREATE TABLE IF NOT EXISTS level_progression_rules (
    level_number INT PRIMARY KEY,
    level_type VARCHAR(24) NOT NULL DEFAULT 'WEALTH', -- WEALTH or CHARISMA
    required_exp BIGINT NOT NULL,
    badge_title_ar VARCHAR(80) NOT NULL,
    badge_gradient_start_hex VARCHAR(16) NOT NULL,
    badge_gradient_end_hex VARCHAR(16) NOT NULL,
    badge_3d_icon_url TEXT,
    reward_frame_id VARCHAR(64),
    reward_welcome_id VARCHAR(64)
);

-- 7. ACCOUNT BANS & MODERATION LOGS (جدول حظر الحسابات والقرارات الإدارية)
CREATE TABLE IF NOT EXISTS account_moderation_logs (
    log_id BIGSERIAL PRIMARY KEY,
    target_user_uuid VARCHAR(64) NOT NULL REFERENCES users(user_uuid),
    admin_user_uuid VARCHAR(64) NOT NULL REFERENCES users(user_uuid),
    action_type VARCHAR(40) NOT NULL, -- BAN_ACCOUNT, UNBAN_ACCOUNT, ASSIGN_ROLE, GRANT_SPECIAL_ID, LEVEL_BOOST, GRANT_WELCOME
    action_payload_json TEXT,
    reason TEXT,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. AGENCIES (جدول الوكالات: وكالات الشحن، وكالات المضيفين، وإدارة البدي BD)
CREATE TABLE IF NOT EXISTS agencies (
    agency_id VARCHAR(64) PRIMARY KEY,
    agency_code VARCHAR(32) UNIQUE NOT NULL,
    agency_name VARCHAR(120) NOT NULL,
    agency_type VARCHAR(32) NOT NULL, -- CHARGE_AGENCY (وكالة شحن), HOST_AGENCY (وكالة مضيفين), BD_AGENCY (وكالة بدي)
    owner_user_uuid VARCHAR(64) NOT NULL REFERENCES users(user_uuid),
    bd_supervisor_uuid VARCHAR(64) REFERENCES users(user_uuid),
    agency_level INT DEFAULT 1,
    monthly_volume_coins BIGINT DEFAULT 0,
    agency_balance_coins BIGINT DEFAULT 0,
    commission_rate_percent FLOAT DEFAULT 12.5,
    members_count INT DEFAULT 1,
    status VARCHAR(24) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. VOICE CHAT ROOMS & SEATS CONFIG (جدول الغرف الصوتية والمايكات)
CREATE TABLE IF NOT EXISTS voice_rooms (
    room_id VARCHAR(64) PRIMARY KEY,
    room_display_code VARCHAR(32) UNIQUE NOT NULL,
    room_title VARCHAR(150) NOT NULL,
    announcement_ar TEXT NOT NULL,
    host_user_uuid VARCHAR(64) NOT NULL REFERENCES users(user_uuid),
    category_code VARCHAR(40) NOT NULL DEFAULT 'POPULAR',
    country_code VARCHAR(8) DEFAULT 'EG',
    seat_count INT NOT NULL DEFAULT 9,             -- Supports 6, 8, 9, 12, 15 seats dynamically
    background_theme_id VARCHAR(64) DEFAULT 'bg_palace_moonlight_7d',
    frame_style_id VARCHAR(64) DEFAULT 'card_frame_imperial_gold',
    online_count INT DEFAULT 0,
    total_gifts_coins BIGINT DEFAULT 0,
    is_live_party_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. GIFTS CATALOG (جدول الهدايا المتحركة 3D / 5D / 7D / SVGA)
CREATE TABLE IF NOT EXISTS gifts_catalog (
    gift_id VARCHAR(64) PRIMARY KEY,
    name_ar VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    category VARCHAR(40) NOT NULL,                 -- POPULAR, VIP, LUXURY_7D, CP_LOVE
    price_coins BIGINT NOT NULL,
    diamond_value BIGINT NOT NULL,
    svga_animation_url TEXT NOT NULL,
    preview_icon_name VARCHAR(64) NOT NULL,
    is_fullscreen_7d BOOLEAN DEFAULT TRUE,
    triggers_global_banner BOOLEAN DEFAULT FALSE
);

-- 11. CASUAL GAMES CATALOG (جدول ألعاب الغرف واللوبي 3x4)
CREATE TABLE IF NOT EXISTS games_catalog (
    game_id VARCHAR(64) PRIMARY KEY,
    title_ar VARCHAR(100) NOT NULL,
    title_en VARCHAR(100) NOT NULL,
    game_url_or_module TEXT NOT NULL,
    min_bet_coins BIGINT DEFAULT 100,
    max_multiplier INT DEFAULT 100,
    jackpot_pool_coins BIGINT DEFAULT 500000,
    sort_order INT DEFAULT 1,
    is_enabled BOOLEAN DEFAULT TRUE
);

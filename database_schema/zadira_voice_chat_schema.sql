-- =======================================================================================
-- ZADIRA LIVE 7D - COMPLETE PRODUCTION DATABASE SCHEMA & CONTROL TABLES (SQL)
-- =======================================================================================
-- This file is included inside the project archive (`database_schema/`) for GitHub &
-- production backend deployment (PostgreSQL / MySQL / Supabase / Firebase Data Connect).
-- None of these database tables are exposed inside the user-facing UI.
-- =======================================================================================

-- 1. MASTER APP MODULES REGISTRY TABLE (جدول التحكم في جميع الأقسام الأساسية والثانوية: إضافة / تعديل / إخفاء / حذف)
CREATE TABLE IF NOT EXISTS app_modules_registry_table (
    module_id VARCHAR(64) PRIMARY KEY,
    module_key VARCHAR(64) UNIQUE NOT NULL,
    title_ar VARCHAR(120) NOT NULL,
    subtitle_ar VARCHAR(255) NOT NULL,
    icon_emoji VARCHAR(16) NOT NULL DEFAULT '👑',
    is_primary_section BOOLEAN NOT NULL DEFAULT TRUE,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 1,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. PERSISTENT USER ACCOUNTS & AUTHENTICATION TABLE (جدول الحسابات وتسجيل الدخول بالبريد وكلمة السر والآي دي)
-- Rule: Logging in with the same email + password opens the same account and ID.
-- Registering with a new email creates a brand-new account with a new unique ID.
CREATE TABLE IF NOT EXISTS users_accounts_table (
    user_uuid VARCHAR(64) PRIMARY KEY,
    display_id VARCHAR(32) UNIQUE NOT NULL,
    is_special_id BOOLEAN NOT NULL DEFAULT FALSE,
    email VARCHAR(190) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    bio VARCHAR(255) DEFAULT 'أهلاً بكم في عالمي الملكي 👑',
    avatar_type VARCHAR(32) DEFAULT 'PRINCE',
    country_flag VARCHAR(16) DEFAULT '🇪🇬',
    country_name_ar VARCHAR(64) DEFAULT 'مصر',
    system_role VARCHAR(32) NOT NULL DEFAULT 'USER', -- SUPER_ADMIN, ADMIN, MANAGER, BD, CHARGE_AGENT, HOST_AGENT, HOST, SUPPORTER, ROOM_OWNER, ROOM_ADMIN, USER
    wealth_level INT NOT NULL DEFAULT 1,
    wealth_exp_current BIGINT NOT NULL DEFAULT 0,
    wealth_exp_target BIGINT NOT NULL DEFAULT 5000,
    charisma_level INT NOT NULL DEFAULT 1,
    charisma_exp_current BIGINT NOT NULL DEFAULT 0,
    charisma_exp_target BIGINT NOT NULL DEFAULT 5000,
    vip_tier INT NOT NULL DEFAULT 0, -- 0..5 (5 VIP Sections)
    gold_coins BIGINT NOT NULL DEFAULT 25000,
    crystal_diamonds BIGINT NOT NULL DEFAULT 5000,
    equipped_circular_frame_id VARCHAR(64) DEFAULT 'IMPERIAL_GOLD_WINGS',
    sound_wave_color_hex VARCHAR(16) DEFAULT '#00E5FF',
    colored_name_primary_hex VARCHAR(16) DEFAULT '#FFD700',
    colored_name_secondary_hex VARCHAR(16) DEFAULT '#00E5FF',
    equipped_entry_mount_name VARCHAR(120) DEFAULT 'تنين الإمبراطور الذهبي 7D',
    equipped_chat_bubble_name VARCHAR(120) DEFAULT 'فقاعة القصر الملكي الذهبية',
    is_host_agent BOOLEAN NOT NULL DEFAULT FALSE,
    is_host_member BOOLEAN NOT NULL DEFAULT FALSE,
    is_charge_agent BOOLEAN NOT NULL DEFAULT FALSE,
    charge_agent_coins_balance BIGINT NOT NULL DEFAULT 0,
    is_banned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. STORE CATALOG TABLE (جدول المتجر المستقل: الإطارات الدائرية، الدخوليات المتحركة، والآي دي المميز للبيع)
CREATE TABLE IF NOT EXISTS store_catalog_table (
    item_id VARCHAR(64) PRIMARY KEY,
    category VARCHAR(32) NOT NULL, -- FRAMES, ENTRY_MOUNTS, SPECIAL_IDS, CHAT_BUBBLES
    name_ar VARCHAR(120) NOT NULL,
    subtitle_ar VARCHAR(255) NOT NULL,
    price_coins BIGINT NOT NULL,
    duration_days INT NOT NULL DEFAULT 30,
    dimension_tag VARCHAR(16) NOT NULL DEFAULT '7D SVGA',
    icon_emoji VARCHAR(16) NOT NULL,
    circular_frame_style VARCHAR(64),
    sound_wave_color_hex VARCHAR(16) DEFAULT '#00E5FF',
    name_color_hex VARCHAR(16) DEFAULT '#FFD700',
    special_id_value VARCHAR(32),
    svga_asset_path VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 4. USER OWNED ACCESSORIES TABLE (جدول إكسسواراتي المملوكة: الإطارات المملوكة، الدخوليات المملوكة، الفقاعات المملوكة)
CREATE TABLE IF NOT EXISTS user_owned_accessories_table (
    ownership_id VARCHAR(64) PRIMARY KEY,
    user_display_id VARCHAR(32) NOT NULL,
    item_id VARCHAR(64) NOT NULL,
    category VARCHAR(32) NOT NULL, -- FRAMES, ENTRY_MOUNTS, CHAT_BUBBLES, SPECIAL_IDS
    is_equipped BOOLEAN NOT NULL DEFAULT FALSE,
    granted_by_source VARCHAR(64) DEFAULT 'STORE_PURCHASE', -- STORE_PURCHASE, CHARGE_AGENT_REWARD, DATABASE_GRANT, VIP_REWARD
    acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. DIRECT DATABASE GRANTS TABLE (جدول إرسال شارة معينة أو إطار دائري من قاعدة البيانات إلى أي مستخدم)
CREATE TABLE IF NOT EXISTS database_direct_grants_table (
    grant_id VARCHAR(64) PRIMARY KEY,
    target_user_display_id VARCHAR(32) NOT NULL,
    grant_type VARCHAR(32) NOT NULL, -- BADGE, CIRCULAR_FRAME, ENTRY_MOUNT, SPECIAL_ID, VIP_TIER
    granted_item_id VARCHAR(64) NOT NULL,
    granted_title_ar VARCHAR(120) NOT NULL,
    sound_wave_color_hex VARCHAR(16) DEFAULT '#00E5FF',
    colored_name_hex VARCHAR(16) DEFAULT '#FFD700',
    auto_equip BOOLEAN NOT NULL DEFAULT TRUE,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. VIP 5 SECTIONS CONFIGURATION TABLE (جدول أقسام الـ VIP الخمسة القابلة للضبط من قاعدة البيانات)
CREATE TABLE IF NOT EXISTS vip_five_sections_table (
    vip_tier INT PRIMARY KEY CHECK (vip_tier BETWEEN 1 AND 5),
    title_ar VARCHAR(100) NOT NULL,
    badge_title_ar VARCHAR(100) NOT NULL,
    price_coins_monthly BIGINT NOT NULL,
    circular_frame_style VARCHAR(64) NOT NULL,
    entry_mount_name_ar VARCHAR(120) NOT NULL,
    sound_wave_color_hex VARCHAR(16) NOT NULL,
    colored_name_primary_hex VARCHAR(16) NOT NULL,
    colored_name_secondary_hex VARCHAR(16) NOT NULL,
    perks_json TEXT NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- 7. HOST AGENCIES & HOST TELEMETRY TABLE (جدول وكالات المضيفين، دعوات الوكالة، وبيانات المضيف على المايك والساعات والهدايا)
CREATE TABLE IF NOT EXISTS agency_hosts_telemetry_table (
    host_user_display_id VARCHAR(32) PRIMARY KEY,
    agency_code VARCHAR(32) NOT NULL,
    host_nickname VARCHAR(100) NOT NULL,
    is_on_mic_now BOOLEAN NOT NULL DEFAULT FALSE,
    is_online_in_app BOOLEAN NOT NULL DEFAULT TRUE,
    current_room_name_ar VARCHAR(120) NOT NULL,
    total_gifts_diamonds BIGINT NOT NULL DEFAULT 0,
    total_mic_hours INT NOT NULL DEFAULT 0,
    active_mic_days INT NOT NULL DEFAULT 0,
    expected_salary_usd INT NOT NULL DEFAULT 0,
    invited_by_agent_id VARCHAR(32),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. CHARGE AGENCY & AGENT REWARDS TABLE (جدول وكالة الشحن، رصيد العملات، شحن الـ ID، ومكافآت الوكيل من إطارات ودخوليات)
CREATE TABLE IF NOT EXISTS charge_agency_shipments_table (
    shipment_id VARCHAR(64) PRIMARY KEY,
    agent_display_id VARCHAR(32) NOT NULL,
    target_user_display_id VARCHAR(32) NOT NULL,
    coins_shipped BIGINT NOT NULL DEFAULT 0,
    reward_item_id VARCHAR(64),
    reward_name_ar VARCHAR(120),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. VOICE ROOMS & ROOM ROLES PERMISSIONS TABLE (جدول الغرف الصوتية وصلاحيات صاحب الغرفة وأدمن الغرفة والمستخدم)
-- Owner permissions: Kick, Mute, Ban, Assign Admin, Edit Room Name, Edit Room Cover Photo, Lock/Unlock Mic.
-- Admin permissions: Lock/Unlock Mic, Kick User, Mute User.
-- Member permissions: Sit on Mic, Leave Mic, Send Chat & Gifts, Follow Users.
CREATE TABLE IF NOT EXISTS voice_rooms_table (
    room_id VARCHAR(64) PRIMARY KEY,
    room_display_id VARCHAR(32) UNIQUE NOT NULL,
    owner_user_display_id VARCHAR(32) NOT NULL,
    title_ar VARCHAR(140) NOT NULL,
    announcement_ar VARCHAR(255) NOT NULL,
    cover_photo_badge VARCHAR(64) NOT NULL DEFAULT '👑 7D',
    background_style_id VARCHAR(64) NOT NULL DEFAULT 'PALACE_NIGHT',
    online_count INT NOT NULL DEFAULT 1,
    heat_score BIGINT NOT NULL DEFAULT 1000
);

CREATE TABLE IF NOT EXISTS voice_room_roles_table (
    room_id VARCHAR(64) NOT NULL,
    user_display_id VARCHAR(32) NOT NULL,
    room_role VARCHAR(32) NOT NULL, -- OWNER, ADMIN, MEMBER, BANNED
    assigned_by_display_id VARCHAR(32),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id, user_display_id)
);

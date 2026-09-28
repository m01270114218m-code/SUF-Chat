-- =========================================================================================
-- COMPLETE MASTER DATABASE SCHEMA & SEED TABLES (MySQL / PostgreSQL / Supabase / SQLite)
-- =========================================================================================
-- Controls 100% of the application:
-- 1. Global UI Design System, Colors, Appearances, Shapes, Borders, and Notification Styles
-- 2. Buttons, Icons, Images, and Navigation Routes Registry
-- 3. Animated & Transparent Chatroom Background Themes Catalog (Store & In-Room Selector)
-- 4. Users, Accounts, Balances, Equipped Frames, Entry Mounts, Chat Bubbles & Room Themes
-- 5. Voice Rooms, 10-Seat Layouts, Room Owners, Room Admins, Muted & Banned Users
-- 6. In-Room Role Permissions Matrix (MEMBER, ADMIN, OWNER)
-- 7. Store Catalog (Circular 3D Frames, Entry Mounts, Room Themes, Special IDs, Chat Bubbles)
-- 8. VIP 5 Tiers Configuration Table
-- 9. Gifts Catalog & SVGA Effects Table
-- 10. Host Agencies, Charge Agencies & User ID Role Grants
-- =========================================================================================

-- -----------------------------------------------------------------------------------------
-- 1. GLOBAL UI DESIGN & APPEARANCE TABLE (`app_ui_design_system`)
-- -----------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_ui_design_system (
    config_key VARCHAR(64) PRIMARY KEY,
    theme_name VARCHAR(128) NOT NULL,
    main_screen_bg_hex VARCHAR(16) NOT NULL DEFAULT '#F6F6F9',
    header_gradient_start_hex VARCHAR(16) NOT NULL DEFAULT '#EDE4FF',
    header_gradient_mid_hex VARCHAR(16) NOT NULL DEFAULT '#F5EEFF',
    card_surface_hex VARCHAR(16) NOT NULL DEFAULT '#FFFFFF',
    primary_accent_hex VARCHAR(16) NOT NULL DEFAULT '#9333EA',
    secondary_accent_hex VARCHAR(16) NOT NULL DEFAULT '#EC4899',
    gold_accent_hex VARCHAR(16) NOT NULL DEFAULT '#FFD700',
    text_primary_hex VARCHAR(16) NOT NULL DEFAULT '#1C1C28',
    text_secondary_hex VARCHAR(16) NOT NULL DEFAULT '#6B7280',
    button_corner_radius_dp INT NOT NULL DEFAULT 24,
    card_corner_radius_dp INT NOT NULL DEFAULT 18,
    notification_position VARCHAR(32) NOT NULL DEFAULT 'BOTTOM_CENTER',
    notification_is_transparent BOOLEAN NOT NULL DEFAULT TRUE,
    notification_show_in_room BOOLEAN NOT NULL DEFAULT FALSE,
    room_show_store_button BOOLEAN NOT NULL DEFAULT FALSE,
    room_show_games_button BOOLEAN NOT NULL DEFAULT FALSE,
    room_enable_pk_mode BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO app_ui_design_system (
    config_key, theme_name, main_screen_bg_hex, header_gradient_start_hex, header_gradient_mid_hex,
    card_surface_hex, primary_accent_hex, secondary_accent_hex, gold_accent_hex,
    text_primary_hex, text_secondary_hex, button_corner_radius_dp, card_corner_radius_dp,
    notification_position, notification_is_transparent, notification_show_in_room,
    room_show_store_button, room_show_games_button, room_enable_pk_mode
) VALUES (
    'MASTER_MAIN_INTERFACE_STYLE',
    'Unified Soft Lavender-Pearl Main Interface & Transparent 7D Voice Rooms',
    '#F6F6F9', '#EDE4FF', '#F5EEFF', '#FFFFFF', '#9333EA', '#EC4899', '#FFD700',
    '#1C1C28', '#6B7280', 24, 18,
    'BOTTOM_CENTER', TRUE, FALSE, FALSE, FALSE, FALSE
);

-- -----------------------------------------------------------------------------------------
-- 2. BUTTONS, ICONS, IMAGES & UI ELEMENTS TABLE (`app_buttons_icons_registry`)
-- -----------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_buttons_icons_registry (
    element_id VARCHAR(64) PRIMARY KEY,
    screen_code VARCHAR(64) NOT NULL,
    label_ar VARCHAR(128) NOT NULL,
    icon_name VARCHAR(64) NOT NULL,
    custom_image_url TEXT,
    bg_color_hex VARCHAR(16) NOT NULL,
    border_color_hex VARCHAR(16) NOT NULL,
    shape_type VARCHAR(32) NOT NULL DEFAULT 'ROUNDED_PILL',
    action_route VARCHAR(64) NOT NULL,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0
);

INSERT INTO app_buttons_icons_registry (element_id, screen_code, label_ar, icon_name, custom_image_url, bg_color_hex, border_color_hex, shape_type, action_route, is_visible, display_order) VALUES
('nav_tab_home', 'MAIN_BOTTOM_BAR', 'الرئيسية', 'ic_home_3d', NULL, '#9333EA', '#EC4899', 'CIRCLE', 'HOME', TRUE, 1),
('nav_tab_discover', 'MAIN_BOTTOM_BAR', 'اكتشف', 'ic_discover_3d', NULL, '#9333EA', '#EC4899', 'CIRCLE', 'DISCOVER', TRUE, 2),
('nav_tab_messages', 'MAIN_BOTTOM_BAR', 'الرسائل', 'ic_messages_3d', NULL, '#9333EA', '#EC4899', 'CIRCLE', 'MESSAGES', TRUE, 3),
('nav_tab_profile', 'MAIN_BOTTOM_BAR', 'حسابي', 'ic_profile_3d', NULL, '#9333EA', '#EC4899', 'CIRCLE', 'PROFILE', TRUE, 4),
('room_theme_selector_pill', 'AUDIO_ROOM', 'ثيم الخلفية', 'ic_palette_theme', NULL, '#38BDF8', '#A855F7', 'ROUNDED_PILL', 'OPEN_THEME_SELECTOR', TRUE, 5),
('room_gift_button', 'AUDIO_ROOM', 'إرسال هدية', 'ic_card_giftcard', NULL, '#EC4899', '#F43F5E', 'CIRCLE', 'OPEN_GIFT_SHEET', TRUE, 6),
('room_mic_button', 'AUDIO_ROOM', 'كتم/فتح المايك', 'ic_mic', NULL, '#33FFFFFF', '#4DFFFFFF', 'CIRCLE', 'TOGGLE_MIC', TRUE, 7),
('room_tools_button', 'AUDIO_ROOM', 'أدوات الغرفة', 'ic_grid_view', NULL, '#33FFFFFF', '#4DFFFFFF', 'CIRCLE', 'OPEN_TOOLS_SHEET', TRUE, 8),
('room_chat_input_pill', 'AUDIO_ROOM', 'تحدث في الشات', 'ic_chat_bubble', NULL, '#33FFFFFF', '#4DFFFFFF', 'ROUNDED_PILL', 'OPEN_CHAT_DIALOG', TRUE, 9),
('room_store_button', 'AUDIO_ROOM', 'زر المتجر داخل الغرفة (ملغي)', 'ic_store', NULL, '#00000000', '#00000000', 'NONE', 'DISABLED', FALSE, 99),
('room_games_button', 'AUDIO_ROOM', 'زر الألعاب داخل الغرفة (ملغي)', 'ic_games', NULL, '#00000000', '#00000000', 'NONE', 'DISABLED', FALSE, 100),
('room_pk_button', 'AUDIO_ROOM', 'وضع PK داخل الغرفة (ملغي)', 'ic_pk', NULL, '#00000000', '#00000000', 'NONE', 'DISABLED', FALSE, 101);

-- -----------------------------------------------------------------------------------------
-- 3. ANIMATED & TRANSPARENT CHATROOM BACKGROUND THEMES TABLE (`room_background_themes`)
-- -----------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS room_background_themes (
    theme_id VARCHAR(64) PRIMARY KEY,
    store_item_id VARCHAR(64) NOT NULL,
    title_ar VARCHAR(128) NOT NULL,
    subtitle_ar TEXT NOT NULL,
    icon_emoji VARCHAR(16) NOT NULL,
    price_coins BIGINT NOT NULL DEFAULT 0,
    is_free_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_animated BOOLEAN NOT NULL DEFAULT TRUE,
    is_transparent_glass BOOLEAN NOT NULL DEFAULT TRUE,
    top_color_hex VARCHAR(16) NOT NULL,
    mid_color_hex VARCHAR(16) NOT NULL,
    bottom_color_hex VARCHAR(16) NOT NULL,
    accent_glow_hex VARCHAR(16) NOT NULL,
    secondary_glow_hex VARCHAR(16) NOT NULL,
    custom_bg_image_url TEXT,
    custom_svga_animation_url TEXT,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO room_background_themes (
    theme_id, store_item_id, title_ar, subtitle_ar, icon_emoji, price_coins,
    is_free_default, is_animated, is_transparent_glass,
    top_color_hex, mid_color_hex, bottom_color_hex, accent_glow_hex, secondary_glow_hex
) VALUES
('PALACE_NIGHT', 'store_room_theme_1', 'ليالي الهلال الملكي المتحركة', 'خلفية شفافة عالية الجودة مع هلال ذهبي ونجوم متلألئة متحركة', '🌙', 0, TRUE, TRUE, TRUE, '#0A0E1A', '#0D1324', '#11182E', '#FFE082', '#38BDF8'),
('TRANSPARENT_AURORA', 'store_room_theme_2', 'أورورا الكريستال الشفافة', 'موجات أورورا شفافة متحركة عالية الدقة مع جزيئات ضوئية عائمة', '🌌', 18000, FALSE, TRUE, TRUE, '#07162C', '#0F2942', '#1B1438', '#38BDF8', '#A855F7'),
('ROYAL_GOLD_PALACE', 'store_room_theme_3', 'قصر الذهب الإمبراطوري الشفاف', 'ثيم شفاف فاخر بذرات الذهب المتطايرة وإضاءة القصر الملكي', '👑', 35000, FALSE, TRUE, TRUE, '#1B1105', '#2A1B08', '#140D04', '#FFD700', '#F59E0B'),
('SAPPHIRE_STARLIGHT', 'store_room_theme_4', 'سماء الياقوت الأزرق الشفافة', 'سديم ياقوتي شفاف عالي الجودة مع شهب ونجوم متحركة', '💎', 25000, FALSE, TRUE, TRUE, '#041028', '#0A224E', '#061534', '#00E5FF', '#3B82F6'),
('EMERALD_CRYSTAL', 'store_room_theme_5', 'واحة الزمرد الشفافة المتحركة', 'إضاءة زمردية كريستالية شفافة وموجات ضوئية هادئة', '💚', 22000, FALSE, TRUE, TRUE, '#041E19', '#08362C', '#05221C', '#10B981', '#34D399'),
('ROSE_VELVET_GALAXY', 'store_room_theme_6', 'مجرة الورد الملكي الشفافة', 'مجرة مخملية وردية شفافة مع نجوم مضيئة عالية الدقة', '🌸', 28000, FALSE, TRUE, TRUE, '#21071C', '#380D30', '#1B0518', '#F472B6', '#C084FC');

-- -----------------------------------------------------------------------------------------
-- 4. IN-ROOM ROLES & PERMISSIONS MATRIX TABLE (`room_roles_permissions_matrix`)
-- -----------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS room_roles_permissions_matrix (
    role_code VARCHAR(32) PRIMARY KEY,
    title_ar VARCHAR(64) NOT NULL,
    badge_emoji VARCHAR(16) NOT NULL,
    badge_color_hex VARCHAR(16) NOT NULL,
    can_take_and_leave_mic BOOLEAN NOT NULL DEFAULT TRUE,
    can_send_gifts BOOLEAN NOT NULL DEFAULT TRUE,
    can_write_chat BOOLEAN NOT NULL DEFAULT TRUE,
    can_view_account_card_on_tap BOOLEAN NOT NULL DEFAULT TRUE,
    can_follow_and_open_profile BOOLEAN NOT NULL DEFAULT TRUE,
    can_lock_unlock_mics BOOLEAN NOT NULL DEFAULT FALSE,
    can_kick_user_from_mic BOOLEAN NOT NULL DEFAULT FALSE,
    can_mute_user_on_mic BOOLEAN NOT NULL DEFAULT FALSE,
    can_assign_room_admin BOOLEAN NOT NULL DEFAULT FALSE,
    can_ban_user_from_room BOOLEAN NOT NULL DEFAULT FALSE,
    can_edit_room_name_photo_and_theme BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO room_roles_permissions_matrix VALUES
('MEMBER', 'مستخدم في الغرفة', '🎙️', '#38BDF8', TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
('ADMIN', 'أدمن الغرفة', '🛡️', '#10B981', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE),
('OWNER', 'صاحب الغرفة', '👑', '#FFD700', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE);

-- -----------------------------------------------------------------------------------------
-- 5. USERS & ACCOUNTS MANAGEMENT TABLE (`users_accounts`)
-- -----------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users_accounts (
    uuid VARCHAR(64) PRIMARY KEY,
    display_id VARCHAR(32) UNIQUE NOT NULL,
    is_special_id BOOLEAN NOT NULL DEFAULT FALSE,
    email VARCHAR(128) UNIQUE NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    nickname VARCHAR(128) NOT NULL,
    avatar_type VARCHAR(32) NOT NULL DEFAULT 'PRINCE',
    custom_avatar_url TEXT,
    custom_cover_url TEXT,
    country_flag VARCHAR(16) NOT NULL DEFAULT '🇪🇬',
    country_name_ar VARCHAR(64) NOT NULL DEFAULT 'مصر',
    app_role VARCHAR(32) NOT NULL DEFAULT 'SUPPORTER',
    wealth_level INT NOT NULL DEFAULT 1,
    charisma_level INT NOT NULL DEFAULT 1,
    vip_tier INT NOT NULL DEFAULT 0,
    gold_coins BIGINT NOT NULL DEFAULT 150000,
    crystal_diamonds BIGINT NOT NULL DEFAULT 65000,
    equipped_frame_style VARCHAR(64) NOT NULL DEFAULT 'IMPERIAL_GOLD_WINGS',
    equipped_entry_mount VARCHAR(128) NOT NULL DEFAULT 'تنين الإمبراطور الذهبي 7D',
    equipped_chat_bubble VARCHAR(128) NOT NULL DEFAULT 'فقاعة المخطوطة الإمبراطورية الذهبية',
    equipped_room_theme_id VARCHAR(64) NOT NULL DEFAULT 'PALACE_NIGHT',
    is_host_agent BOOLEAN NOT NULL DEFAULT FALSE,
    is_approved_host BOOLEAN NOT NULL DEFAULT FALSE,
    is_charge_agent BOOLEAN NOT NULL DEFAULT FALSE,
    charge_agent_coins_balance BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users_accounts (
    uuid, display_id, is_special_id, email, password_hash, nickname, avatar_type,
    country_flag, country_name_ar, app_role, wealth_level, charisma_level, vip_tier,
    gold_coins, crystal_diamonds, equipped_frame_style, equipped_entry_mount, equipped_room_theme_id
) VALUES (
    'user_1201637', '1201637', TRUE, 'mohamed@zadiralive.com', '12345678', 'محمد', 'PRINCE',
    '🇪🇬', 'مصر', 'SUPPORTER', 45, 38, 5, 285000, 142000,
    'IMPERIAL_GOLD_WINGS', 'تنين الإمبراطور الذهبي 7D', 'PALACE_NIGHT'
);

-- -----------------------------------------------------------------------------------------
-- 6. VOICE ROOMS & IN-ROOM ROLES ASSIGNMENTS TABLE (`voice_rooms` & `voice_room_member_roles`)
-- -----------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS voice_rooms (
    room_id VARCHAR(64) PRIMARY KEY,
    room_display_id VARCHAR(32) UNIQUE NOT NULL,
    title_ar VARCHAR(128) NOT NULL,
    announcement_ar TEXT NOT NULL,
    owner_display_id VARCHAR(32) NOT NULL,
    owner_nickname VARCHAR(128) NOT NULL,
    country_flag VARCHAR(16) NOT NULL DEFAULT '🇪🇬',
    category_ar VARCHAR(64) NOT NULL DEFAULT 'حزب',
    custom_cover_url TEXT,
    background_theme_id VARCHAR(64) NOT NULL DEFAULT 'PALACE_NIGHT',
    total_ seats_count INT NOT NULL DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS voice_room_member_roles (
    room_id VARCHAR(64) NOT NULL,
    user_display_id VARCHAR(32) NOT NULL,
    room_role VARCHAR(32) NOT NULL DEFAULT 'MEMBER', -- 'OWNER', 'ADMIN', 'MEMBER'
    is_muted BOOLEAN NOT NULL DEFAULT FALSE,
    is_banned BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id, user_display_id)
);

-- -----------------------------------------------------------------------------------------
-- 7. STORE & ACCESSORIES CATALOG TABLE (`store_catalog_items`)
-- -----------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS store_catalog_items (
    item_id VARCHAR(64) PRIMARY KEY,
    name_ar VARCHAR(128) NOT NULL,
    subtitle_ar TEXT NOT NULL,
    category VARCHAR(32) NOT NULL, -- 'FRAMES', 'ENTRY_MOUNTS', 'ROOM_THEMES', 'SPECIAL_IDS', 'CHAT_BUBBLES'
    price_coins BIGINT NOT NULL,
    dimension_tag VARCHAR(32) NOT NULL,
    icon_emoji VARCHAR(16) NOT NULL,
    primary_color_hex VARCHAR(16) NOT NULL,
    secondary_color_hex VARCHAR(16) NOT NULL,
    frame_style_code VARCHAR(64),
    room_theme_id VARCHAR(64),
    special_id_value VARCHAR(32),
    is_visible BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO store_catalog_items VALUES
('store_frame_1', 'إطار أجنحة الإمبراطور الدائري 7D', 'إطار دائري مجنح يظهر على الصورة والمايك مع موجات ذهبية', 'FRAMES', 45000, '7D دائري', '👑', '#FFD700', '#9E6B0E', 'IMPERIAL_GOLD_WINGS', NULL, NULL, TRUE),
('store_frame_2', 'إطار تنين الجليد الدائري 6D', 'إطار دائري كريستالي مع موجات صوتية سماوية متوهجة', 'FRAMES', 38000, '6D دائري', '🐉', '#00E5FF', '#0055B3', 'CRYSTAL_DRAGON_ICE', NULL, NULL, TRUE),
('store_frame_3', 'إطار عنقاء الياقوت الدائري 5D', 'إطار دائري وردي ملكي مع موجات صوتية وردية واسم ملون', 'FRAMES', 32000, '5D دائري', '🦅', '#FF4081', '#880E4F', 'ROSE_GOLD_PHOENIX', NULL, NULL, TRUE),
('store_mount_1', 'تنين الإمبراطور الذهبي 7D', 'دخولية متحركة بملء الشاشة تظهر تلقائياً عند دخولك الغرفة', 'ENTRY_MOUNTS', 80000, '7D SVGA', '🐉', '#FFD700', '#B71C1C', NULL, NULL, NULL, TRUE),
('store_mount_2', 'أسطول بوغاتي النيون الملكي 6D', 'سيارة رياضية مجسمة مع إشعار دخول ملكي عند دخول الغرفة', 'ENTRY_MOUNTS', 55000, '6D SVGA', '🏎️', '#00E5FF', '#1A237E', NULL, NULL, NULL, TRUE),
('store_room_theme_1', 'ليالي الهلال الملكي المتحركة 🌙', 'ثيم خلفية غرفة شفاف ومتحرك عالي الجودة مع هلال ذهبي ونجوم', 'ROOM_THEMES', 0, '7D Theme', '🌙', '#FFE082', '#141B2D', NULL, 'PALACE_NIGHT', NULL, TRUE),
('store_room_theme_2', 'أورورا الكريستال الشفافة الملونة 🌌', 'موجات أورورا شفافة متحركة عالية الدقة 7D لغرفتك الصوتية', 'ROOM_THEMES', 18000, '7D Glass', '🌌', '#38BDF8', '#A855F7', NULL, 'TRANSPARENT_AURORA', NULL, TRUE),
('store_room_theme_3', 'قصر الذهب الإمبراطوري الشفاف 👑', 'ثيم شفاف فاخر بذرات الذهب المتطايرة وإضاءة القصر الملكي', 'ROOM_THEMES', 35000, '7D Royal', '👑', '#FFD700', '#F59E0B', NULL, 'ROYAL_GOLD_PALACE', NULL, TRUE),
('store_room_theme_4', 'سماء الياقوت الأزرق الشفافة 💎', 'سديم ياقوتي شفاف عالي الجودة مع شهب ونجوم متحركة', 'ROOM_THEMES', 25000, '6D Crystal', '💎', '#00E5FF', '#3B82F6', NULL, 'SAPPHIRE_STARLIGHT', NULL, TRUE),
('store_room_theme_5', 'واحة الزمرد الشفافة المتحركة 💚', 'إضاءة زمردية كريستالية شفافة وموجات ضوئية هادئة', 'ROOM_THEMES', 22000, '6D Glass', '💚', '#10B981', '#34D399', NULL, 'EMERALD_CRYSTAL', NULL, TRUE),
('store_room_theme_6', 'مجرة الورد الملكي الشفافة 🌸', 'مجرة مخملية وردية شفافة مع نجوم مضيئة عالية الدقة', 'ROOM_THEMES', 28000, '7D Nebula', '🌸', '#F472B6', '#C084FC', NULL, 'ROSE_VELVET_GALAXY', NULL, TRUE),
('store_id_1', 'آي دي ملكي سباعي 7777777', 'معرف إمبراطوري نادر مع شارة ذهبية متوهجة', 'SPECIAL_IDS', 150000, 'VIP ID', '7️⃣', '#FFD700', '#8D6E63', NULL, NULL, '7777777', TRUE);

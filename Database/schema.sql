-- =======================================================================================
-- AHLAN CHAT & ZADIRA LIVE 7D - COMPLETE POSTGRESQL PRODUCTION SCHEMA (schema.sql)
-- =======================================================================================
-- Architecture:
-- 1. PostgreSQL with SERIALIZABLE / FOR UPDATE transactional locks for zero double-spending.
-- 2. Mirrored with Redis (`room:status:{room_id}` & `blacklist:device_ids`) for millisecond latency.
-- =======================================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ENUM TYPES
DO $$ BEGIN
    CREATE TYPE agency_type_enum AS ENUM ('RECHARGE', 'HOST');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE ban_type_enum AS ENUM ('USER_ACCOUNT', 'IP_ADDRESS', 'DEVICE_ID', 'ROOM_BAN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE store_category_enum AS ENUM ('FRAMES', 'ENTRY_MOUNTS', 'SPECIAL_IDS', 'CHAT_BUBBLES');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- =======================================================================================
-- 1. USERS & IDENTITY TABLE (`users`)
-- =======================================================================================
CREATE TABLE IF NOT EXISTS users (
    uuid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    custom_id VARCHAR(16) UNIQUE NOT NULL, -- 7 or 8-digit readable ID or Special VIP ID
    is_special_id BOOLEAN NOT NULL DEFAULT FALSE,
    email VARCHAR(190) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    bio VARCHAR(255) DEFAULT 'أهلاً بكم في عالمي الملكي 👑',
    avatar_url VARCHAR(500) DEFAULT 'PRINCE',
    country_flag VARCHAR(16) DEFAULT '🇪🇬',
    country_name_ar VARCHAR(64) DEFAULT 'مصر',
    active_avatar_frame_id VARCHAR(64) DEFAULT 'IMPERIAL_GOLD_WINGS',
    active_entrance_effect_id VARCHAR(120) DEFAULT 'تنين الإمبراطور الذهبي 7D',
    active_chat_bubble_id VARCHAR(120) DEFAULT 'فقاعة القصر الملكي الذهبية',
    sound_wave_color_hex VARCHAR(16) DEFAULT '#FFD700',
    colored_name_primary_hex VARCHAR(16) DEFAULT '#FFD700',
    colored_name_secondary_hex VARCHAR(16) DEFAULT '#00E5FF',
    user_level INT NOT NULL DEFAULT 1,
    user_xp BIGINT NOT NULL DEFAULT 0,
    wealth_level INT NOT NULL DEFAULT 1,
    wealth_xp BIGINT NOT NULL DEFAULT 0,
    charisma_level INT NOT NULL DEFAULT 1,
    charisma_xp BIGINT NOT NULL DEFAULT 0,
    vip_level INT NOT NULL DEFAULT 0 CHECK (vip_level BETWEEN 0 AND 5),
    -- Database Role Flags (Control Dynamic UI Buttons in the Mobile App)
    role_code VARCHAR(32) NOT NULL DEFAULT 'USER',
    is_super_admin BOOLEAN NOT NULL DEFAULT FALSE,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    is_recharge_agent BOOLEAN NOT NULL DEFAULT FALSE,
    is_host_agent BOOLEAN NOT NULL DEFAULT FALSE,
    is_host BOOLEAN NOT NULL DEFAULT FALSE,
    is_banned BOOLEAN NOT NULL DEFAULT FALSE,
    device_unique_id VARCHAR(128),
    last_known_ip VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_custom_id ON users(custom_id);
CREATE INDEX IF NOT EXISTS idx_users_device_id ON users(device_unique_id);

-- =======================================================================================
-- 2. WALLETS & FINANCIAL TRANSACTIONS TABLES (`wallets`, `coin_transactions`, `cashout_requests`)
-- =======================================================================================
CREATE TABLE IF NOT EXISTS wallets (
    user_uuid UUID PRIMARY KEY REFERENCES users(uuid) ON DELETE CASCADE,
    custom_id VARCHAR(16) UNIQUE NOT NULL,
    coin_balance BIGINT NOT NULL DEFAULT 0 CHECK (coin_balance >= 0),
    diamond_balance BIGINT NOT NULL DEFAULT 0 CHECK (diamond_balance >= 0),
    frozen_coins BIGINT NOT NULL DEFAULT 0 CHECK (frozen_coins >= 0),
    frozen_diamonds BIGINT NOT NULL DEFAULT 0 CHECK (frozen_diamonds >= 0),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coin_transactions (
    tx_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    serial_number BIGSERIAL UNIQUE NOT NULL,
    sender_custom_id VARCHAR(16) NOT NULL,
    receiver_custom_id VARCHAR(16) NOT NULL,
    tx_type VARCHAR(32) NOT NULL, -- GIFT_SEND, AGENCY_RECHARGE, STORE_PURCHASE, DIAMOND_EXCHANGE, CASHOUT
    coins_amount BIGINT NOT NULL DEFAULT 0,
    diamonds_amount BIGINT NOT NULL DEFAULT 0,
    reference_item_id VARCHAR(64),
    reference_note VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cashout_requests (
    request_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_custom_id VARCHAR(16) NOT NULL,
    diamonds_deducted BIGINT NOT NULL CHECK (diamonds_deducted > 0),
    payout_usd NUMERIC(12, 2) NOT NULL,
    payout_method VARCHAR(64) NOT NULL, -- BANK_IBAN, VODAFONE_CASH, ZAIN_CASH, USDT_TRC20
    account_details VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, PAID, REJECTED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =======================================================================================
-- 3. COMPREHENSIVE DUAL AGENCY SYSTEM (`agencies`, `agency_members`, `agency_invitations`)
-- =======================================================================================
CREATE TABLE IF NOT EXISTS agencies (
    agency_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agency_code VARCHAR(32) UNIQUE NOT NULL,
    owner_custom_id VARCHAR(16) NOT NULL REFERENCES users(custom_id),
    agency_name VARCHAR(120) NOT NULL,
    agency_type agency_type_enum NOT NULL, -- 'RECHARGE' or 'HOST'
    agency_coin_balance BIGINT NOT NULL DEFAULT 0 CHECK (agency_coin_balance >= 0),
    commission_percentage NUMERIC(5, 2) NOT NULL DEFAULT 12.00,
    whatsapp_contact VARCHAR(64),
    country_name_ar VARCHAR(64) DEFAULT 'مصر',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS agency_members (
    membership_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agency_id UUID NOT NULL REFERENCES agencies(agency_id) ON DELETE CASCADE,
    host_custom_id VARCHAR(16) UNIQUE NOT NULL REFERENCES users(custom_id),
    host_nickname VARCHAR(100) NOT NULL,
    is_on_mic_now BOOLEAN NOT NULL DEFAULT FALSE,
    is_online_in_app BOOLEAN NOT NULL DEFAULT TRUE,
    current_room_title VARCHAR(140) DEFAULT 'متصل في الردهة',
    monthly_hours_on_mic INT NOT NULL DEFAULT 0,
    monthly_active_days INT NOT NULL DEFAULT 0,
    monthly_diamonds_received BIGINT NOT NULL DEFAULT 0,
    target_completed BOOLEAN NOT NULL DEFAULT FALSE,
    calculated_salary_usd NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =======================================================================================
-- 4. ADVANCED MULTI-TIERED BANNING SYSTEM (`banned_entities`)
-- =======================================================================================
CREATE TABLE IF NOT EXISTS banned_entities (
    ban_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ban_type ban_type_enum NOT NULL, -- USER_ACCOUNT, IP_ADDRESS, DEVICE_ID, ROOM_BAN
    target_value VARCHAR(190) NOT NULL, -- custom_id, IP address, or hardware device_unique_id
    room_id VARCHAR(64), -- Populated if ban_type = 'ROOM_BAN'
    reason_ar VARCHAR(255) NOT NULL DEFAULT 'مخالفة قوانين المنصة الملكية',
    banned_by_custom_id VARCHAR(16) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE, -- NULL = Permanent Ban
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_banned_target ON banned_entities(ban_type, target_value);

-- =======================================================================================
-- 5. VOICE ROOMS, 9 MIC SLOTS, MODERATORS & MIC QUEUE (`rooms`, `mic_slots`, `room_moderators`)
-- =======================================================================================
CREATE TABLE IF NOT EXISTS rooms (
    room_id VARCHAR(64) PRIMARY KEY,
    room_display_id VARCHAR(32) UNIQUE NOT NULL,
    owner_custom_id VARCHAR(16) NOT NULL REFERENCES users(custom_id),
    title VARCHAR(140) NOT NULL,
    announcement VARCHAR(255) NOT NULL,
    category_ar VARCHAR(64) NOT NULL DEFAULT 'ملكي 7D',
    country_flag VARCHAR(16) NOT NULL DEFAULT '🇪🇬',
    background_style_id VARCHAR(64) NOT NULL DEFAULT 'ORIENTAL_ISLAMIC_GOLD', -- ORIENTAL_ISLAMIC_GOLD, DARK_NEON_GLASS, PALACE_NIGHT
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    password_hash VARCHAR(128),
    request_to_speak_mode BOOLEAN NOT NULL DEFAULT FALSE,
    is_frozen_by_super_admin BOOLEAN NOT NULL DEFAULT FALSE,
    visitor_count INT NOT NULL DEFAULT 1,
    heat_score BIGINT NOT NULL DEFAULT 1000,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mic_slots (
    room_id VARCHAR(64) NOT NULL REFERENCES rooms(room_id) ON DELETE CASCADE,
    slot_index INT NOT NULL CHECK (slot_index BETWEEN 0 AND 9), -- 0 = Host Seat, 1..8 = 8 Speaker Seats, 9 = VIP Guardian Seat
    current_user_custom_id VARCHAR(16) REFERENCES users(custom_id),
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    is_muted BOOLEAN NOT NULL DEFAULT FALSE,
    seat_gift_points BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id, slot_index)
);

CREATE TABLE IF NOT EXISTS room_moderators (
    room_id VARCHAR(64) NOT NULL REFERENCES rooms(room_id) ON DELETE CASCADE,
    moderator_custom_id VARCHAR(16) NOT NULL REFERENCES users(custom_id) ON DELETE CASCADE,
    assigned_by_owner_id VARCHAR(16) NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id, moderator_custom_id)
);

CREATE TABLE IF NOT EXISTS mic_request_queue (
    room_id VARCHAR(64) NOT NULL REFERENCES rooms(room_id) ON DELETE CASCADE,
    requester_custom_id VARCHAR(16) NOT NULL REFERENCES users(custom_id) ON DELETE CASCADE,
    requester_nickname VARCHAR(100) NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id, requester_custom_id)
);

-- =======================================================================================
-- 6. VIP 5 SECTIONS & STORE / ACCESSORIES TABLES (`vip_five_sections`, `store_items`, `user_inventory`)
-- =======================================================================================
CREATE TABLE IF NOT EXISTS vip_five_sections (
    vip_tier INT PRIMARY KEY CHECK (vip_tier BETWEEN 1 AND 5),
    title_ar VARCHAR(100) NOT NULL,
    badge_title_ar VARCHAR(100) NOT NULL,
    price_coins_monthly BIGINT NOT NULL,
    circular_frame_style VARCHAR(64) NOT NULL,
    entry_mount_name_ar VARCHAR(120) NOT NULL,
    sound_wave_color_hex VARCHAR(16) NOT NULL,
    colored_name_primary_hex VARCHAR(16) NOT NULL,
    colored_name_secondary_hex VARCHAR(16) NOT NULL,
    anti_kick_immunity BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS store_items (
    item_id VARCHAR(64) PRIMARY KEY,
    category store_category_enum NOT NULL, -- FRAMES, ENTRY_MOUNTS, SPECIAL_IDS, CHAT_BUBBLES
    name_ar VARCHAR(120) NOT NULL,
    subtitle_ar VARCHAR(255) NOT NULL,
    price_coins BIGINT NOT NULL,
    circular_frame_style VARCHAR(64),
    special_id_value VARCHAR(32),
    svga_asset_url VARCHAR(255),
    is_for_sale_in_store BOOLEAN NOT NULL DEFAULT TRUE
);

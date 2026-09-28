-- ============================================================================
-- ZADIRA LIVE - ADMIN ROLES SEED DATA & MANAGEMENT STORED PROCEDURES
-- Ready for backend integration: Super Admin, Admin, Manager, BD,
-- Charge Agent, Host Agent, Host, Supporter, Special IDs, Welcomes, Level Up, Ban.
-- ============================================================================

-- Seed Default Role Hierarchy
INSERT INTO role_definitions (
    role_code, role_name_ar, role_name_en, badge_color_hex,
    can_ban_users, can_assign_admins, can_assign_special_id,
    can_grant_entry_welcome, can_modify_user_level, can_recharge_coins,
    can_manage_hosts, can_lock_any_room, priority_rank
) VALUES
('SUPER_ADMIN', 'سوبر أدمن', 'Super Admin', '#FF1744', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, 100),
('ADMIN', 'أدمن', 'Admin', '#FF9100', TRUE, FALSE, TRUE, TRUE, TRUE, FALSE, TRUE, TRUE, 80),
('MANAGER', 'مدير', 'Manager', '#E040FB', TRUE, FALSE, TRUE, TRUE, FALSE, FALSE, TRUE, TRUE, 70),
('BD', 'بدي (تطوير أعمال)', 'Business Development (BD)', '#00E5FF', FALSE, FALSE, TRUE, TRUE, FALSE, FALSE, TRUE, FALSE, 60),
('CHARGE_AGENT', 'وكيل شحن معتمد', 'Charge Agent', '#FFD700', FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, 50),
('HOST_AGENT', 'وكيل مضيفين', 'Host Agent', '#00E676', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, 45),
('HOST', 'مضيف معتمد', 'Official Host', '#FF4081', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 30),
('SUPPORTER', 'داعم ملكي VIP', 'Royal Supporter', '#FFD700', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 20),
('USER', 'عضو', 'Member', '#90CAF9', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 1)
ON CONFLICT (role_code) DO NOTHING;

-- ============================================================================
-- PROCEDURE 1: Assign Administrative Role (تعيين سوبر أدمن / أدمن / مدير / بدي / وكيل شحن / وكيل مضيفين / مضيف / داعم)
-- ============================================================================
-- Example Usage:
-- UPDATE users SET role_code = 'SUPER_ADMIN' WHERE display_id = '1201637';

-- ============================================================================
-- PROCEDURE 2: Assign Special VIP ID (منح آي دي مميز للمستخدم)
-- ============================================================================
-- Example Usage:
-- UPDATE users SET display_id = '777777', is_special_id = TRUE, special_id_tier = '7D_EMPEROR'
-- WHERE user_uuid = :target_uuid;

-- ============================================================================
-- PROCEDURE 3: Grant Entry Welcome SVGA Animation (منح ترحيبية دخول متحركة)
-- ============================================================================
-- Example Usage:
-- UPDATE users SET equipped_entry_welcome_id = 'welcome_royal_dragon_7d'
-- WHERE display_id = :target_display_id;

-- ============================================================================
-- PROCEDURE 4: Boost User Wealth / Charisma Level (رفع المستوى الفوري)
-- ============================================================================
-- Example Usage:
-- UPDATE users SET wealth_level = :new_wealth_level, charisma_level = :new_charisma_level, vip_tier = :new_vip
-- WHERE display_id = :target_display_id;

-- ============================================================================
-- PROCEDURE 5: Ban / Unban User Account (حظر الحساب أو فك الحظر)
-- ============================================================================
-- Example Usage:
-- UPDATE users SET is_banned = TRUE, ban_reason = 'مخالفة سياسة الغرف الصوتية', ban_expires_at = NOW() + INTERVAL '30 days'
-- WHERE display_id = :target_display_id;

-- ============================================================================
-- PROCEDURE 6: Activate Host Agency Center / Host Data / Charge Agency by User ID
-- (تفعيل مركز الوكالات أو بيانات المضيف أو وكالة الشحن من واجهة قاعدة البيانات - مخفي من التطبيق افتراضياً حتى يُعطى المستخدم صلاحيته)
-- ============================================================================
-- A) Grant Host Agent Permission (`مركز وكالتي لوكيل المضيفين`):
-- UPDATE users_accounts_table
-- SET is_host_agent = TRUE, system_role = 'HOST_AGENT'
-- WHERE display_id = :target_display_id;

-- B) Grant Official Host Permission (`بيانات المضيف الرسمية فقط دون وكالة`):
-- UPDATE users_accounts_table
-- SET is_host_member = TRUE, system_role = 'HOST'
-- WHERE display_id = :target_display_id;

-- C) Grant Charge Agent Permission & Coin Pool (`مركز وكالة الشحن بعدد العملات ومكافآت الوكيل`):
-- UPDATE users_accounts_table
-- SET is_charge_agent = TRUE, charge_agent_coins_balance = 5000000, system_role = 'CHARGE_AGENT'
-- WHERE display_id = :target_display_id;

-- D) Hide / Revoke Agency Permissions from a User (`إخفاء وسحب صلاحية الوكالة من المستخدم`):
-- UPDATE users_accounts_table
-- SET is_host_agent = FALSE, is_host_member = FALSE, is_charge_agent = FALSE, charge_agent_coins_balance = 0
-- WHERE display_id = :target_display_id;

-- ============================================================================
-- PROCEDURE 7: Automatic Room Permission Binding by User ID (`صلاحيات الغرف تلقائياً عبر المعرف الخاص ID`)
-- ============================================================================
-- Set Room Owner by ID (`صاحب الغرفة تلقائياً عبر الـ ID`):
-- UPDATE voice_rooms_table SET owner_user_display_id = :owner_display_id WHERE room_id = :room_id;
-- INSERT INTO voice_room_roles_table (room_id, user_display_id, room_role)
-- VALUES (:room_id, :owner_display_id, 'OWNER')
-- ON CONFLICT (room_id, user_display_id) DO UPDATE SET room_role = 'OWNER';

-- Assign Room Admin by ID (`تعيين أدمن الغرفة عبر الـ ID`):
-- INSERT INTO voice_room_roles_table (room_id, user_display_id, room_role)
-- VALUES (:room_id, :admin_display_id, 'ADMIN')
-- ON CONFLICT (room_id, user_display_id) DO UPDATE SET room_role = 'ADMIN';

-- ============================================================================
-- PROCEDURE 8: Show / Hide / Modify Any App Screen or Module from Database (`التحكم فيما يظهر وما لا يظهر من قاعدة البيانات`)
-- ============================================================================
-- Example Usage:
-- UPDATE app_modules_registry_table
-- SET is_visible = :is_visible, is_enabled = :is_enabled, title_ar = :new_title_ar, sort_order = :new_sort_order
-- WHERE module_key = :module_key;


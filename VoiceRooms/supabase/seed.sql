-- ============================================================================
--  VoiceRooms — بيانات أولية (Seed Data)
--  شغّل هذا الملف بعد schema.sql
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1) الإعدادات العامة (التحكم الكامل من الداتابيز)
-- ---------------------------------------------------------------------------
insert into public.app_settings (key, value, value_type, description) values
    ('app_name',              'غرف صوتية',      'string', 'اسم التطبيق'),
    ('app_tagline',           'أصوات تجمعنا',   'string', 'الشعار النصي'),
    ('min_app_version',       '1',              'int',    'أقل إصدار مسموح'),
    ('force_update',          'false',          'bool',   'إجبار التحديث'),
    ('maintenance_mode',      'false',          'bool',   'وضع الصيانة'),
    ('maintenance_msg',       'التطبيق تحت الصيانة، حاول لاحقًا', 'string', 'رسالة الصيانة'),
    ('allow_registration',    'true',           'bool',   'السماح بالتسجيل'),
    ('allow_guest',           'false',          'bool',   'السماح بالدخول كزائر'),
    ('gifts_enabled',         'true',           'bool',   'تفعيل الهدايا'),
    ('animated_gifts_enabled','true',           'bool',   'تفعيل الهدايا المتحركة SVGA'),
    ('banners_enabled',       'true',           'bool',   'تفعيل البانرات المتحركة'),
    ('agencies_enabled',      'true',           'bool',   'تفعيل نظام الوكالات'),
    ('wallet_enabled',        'true',           'bool',   'تفعيل المحفظة'),
    ('max_rooms_per_user',    '3',              'int',    'أقصى عدد غرف للمستخدم'),
    ('default_max_seats',     '8',              'int',    'عدد المقاعد الافتراضي'),
    ('default_room_cover',    '',               'string', 'صورة الغرفة الافتراضية'),
    ('theme_primary',         '#7C4DFF',        'string', 'اللون الأساسي'),
    ('theme_secondary',       '#00E5FF',        'string', 'اللون الثانوي'),
    ('recharge_packages',     '[{"coins":1000,"price":0.99},{"coins":5500,"price":4.99},{"coins":12000,"price":9.99},{"coins":65000,"price":49.99}]', 'json', 'باقات الشحن')
on conflict (key) do nothing;

-- ---------------------------------------------------------------------------
-- 2) المستويات
-- ---------------------------------------------------------------------------
insert into public.levels (level, title, min_xp, badge_icon, badge_color, reward_coins) values
    (1,  'مبتدئ',        0,    '🥉', '#8D6E63', 0),
    (2,  'نشيط',         100,  '🥈', '#B0BEC5', 50),
    (3,  'متألق',        300,  '🥇', '#FFD54F', 100),
    (4,  'نجم',          600,  '⭐', '#FFB300', 200),
    (5,  'محبوب',        1000, '🌟', '#FF8F00', 300),
    (6,  'مميز',         1600, '💫', '#FF6F00', 500),
    (7,  'أسطورة',       2500, '👑', '#FFD700', 800),
    (8,  'ملك',          4000, '🏆', '#FFC107', 1200),
    (9,  'إمبراطور',     6000, '💎', '#00E5FF', 2000),
    (10, 'خارق',         10000,'🔥', '#FF1744', 3500)
on conflict (level) do nothing;

-- ---------------------------------------------------------------------------
-- 3) التصنيفات
-- ---------------------------------------------------------------------------
insert into public.categories (name, icon, color, sort_order) values
    ('الكل',    '🌐', '#7C4DFF', 0),
    ('موسيقى',  '🎵', '#00E5FF', 1),
    ('ألعاب',   '🎮', '#FF4081', 2),
    ('دردشة',   '💬', '#76FF03', 3),
    ('رياضة',   '⚽', '#FFAB00', 4),
    ('تعليم',   '📚', '#00B0FF', 5),
    ('ترفيه',   '🎭', '#E040FB', 6),
    ('شعر',     '📜', '#FF6E40', 7),
    ('عائلي',   '👨‍👩‍👧', '#69F0AE', 8)
on conflict do nothing;

-- ---------------------------------------------------------------------------
-- 4) الهدايا (عادية + متحركة SVGA)
--    animation_url: ضع روابط ملفات SVGA الخاصة بك على Supabase Storage
-- ---------------------------------------------------------------------------
insert into public.gifts (name, icon_url, animation_url, anim_type, price, diamond_value, category, sort_order) values
    ('وردة',   'https://cdn-icons-png.flaticon.com/512/765/765590.png', null, 'none', 10,   5,   'normal', 1),
    ('قلب',    'https://cdn-icons-png.flaticon.com/512/833/833472.png', null, 'none', 20,   10,  'normal', 2),
    ('نجمة',   'https://cdn-icons-png.flaticon.com/512/1828/1828884.png', null, 'none', 50,  25,  'normal', 3),
    ('تاج',    'https://cdn-icons-png.flaticon.com/512/3163/3163496.png', null, 'none', 100, 50,  'luxury', 4),
    ('صاروخ',  'https://cdn-icons-png.flaticon.com/512/3212/3212608.png', null, 'none', 200, 100, 'luxury', 5),
    ('سيارة',  'https://cdn-icons-png.flaticon.com/512/3097/3097180.png', null, 'none', 500, 250, 'luxury', 6),
    ('قلعة',   'https://cdn-icons-png.flaticon.com/512/2602/2602428.png', null, 'none', 1000,500, 'special',7),
    ('يخت',    'https://cdn-icons-png.flaticon.com/512/2942/2942792.png', null, 'none', 2000,1000,'special',8)
on conflict do nothing;

-- ---------------------------------------------------------------------------
-- 5) البانرات / الوجهات المتحركة
-- ---------------------------------------------------------------------------
insert into public.banners (title, image_url, animation_url, placement, sort_order) values
    ('مرحبًا بك في غرف صوتية', 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1200', null, 'home', 1),
    ('فعّل الهدايا المتحركة',  'https://images.unsplash.com/photo-1513151233558-d860c5398176?w=1200', null, 'home', 2),
    ('انضم لوكالة النجوم',     'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=1200', null, 'home', 3)
on conflict do nothing;

-- ============================================================================
--  انتهى ✅
-- ============================================================================

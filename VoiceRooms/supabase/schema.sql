-- ============================================================================
--  VoiceRooms — مخطط قاعدة البيانات الكامل (Supabase / PostgreSQL)
--  تطبيق "غرف صوتية" — كل شيء متحكم فيه من قاعدة البيانات
--  شغّل هذا الملف كامل داخل: Supabase Dashboard > SQL Editor
-- ============================================================================

create extension if not exists "uuid-ossp";
create extension if not exists "pgcrypto";

-- ============================================================================
--  1) الإعدادات العامة (التحكم الكامل من الداتابيز)
-- ============================================================================
create table if not exists public.app_settings (
    key         text primary key,
    value       text,
    value_type  text default 'string',   -- string | int | bool | json
    description text,
    updated_at  timestamptz default now()
);

-- ============================================================================
--  2) المستويات (Levels) — متحكم فيها من الداتابيز
-- ============================================================================
create table if not exists public.levels (
    level        int primary key,
    title        text,
    min_xp       int not null default 0,
    badge_icon   text,
    badge_color  text,
    reward_coins int default 0
);

-- ============================================================================
--  3) الملفات الشخصية
-- ============================================================================
create table if not exists public.profiles (
    id            uuid primary key references auth.users(id) on delete cascade,
    username      text unique,
    display_name  text,
    avatar_url    text,
    bio           text,
    gender        text check (gender in ('male','female','other')) default 'other',
    country       text,
    level         int default 1,
    xp            int default 0,
    coins         bigint default 0,
    diamonds      bigint default 0,
    vip_level     int default 0,
    role          text default 'user',    -- user | agency_owner | admin | super_admin
    agency_id     uuid,
    is_online     boolean default false,
    is_banned     boolean default false,
    ban_reason    text,
    last_seen     timestamptz default now(),
    created_at    timestamptz default now(),
    updated_at    timestamptz default now()
);

-- ============================================================================
--  4) الوكالات (Agencies)
-- ============================================================================
create table if not exists public.agencies (
    id            uuid primary key default uuid_generate_v4(),
    owner_id      uuid not null references public.profiles(id) on delete cascade,
    name          text not null,
    logo_url      text,
    description   text,
    commission    numeric(5,2) default 10.00,   -- نسبة العمولة %
    total_earnings bigint default 0,
    members_count int default 0,
    is_active     boolean default true,
    created_at    timestamptz default now()
);

alter table public.profiles
    drop constraint if exists profiles_agency_fk;
alter table public.profiles
    add constraint profiles_agency_fk
    foreign key (agency_id) references public.agencies(id) on delete set null;

create table if not exists public.agency_members (
    id         uuid primary key default uuid_generate_v4(),
    agency_id  uuid not null references public.agencies(id) on delete cascade,
    user_id    uuid not null references public.profiles(id) on delete cascade,
    role       text default 'member',  -- owner | manager | member
    joined_at  timestamptz default now(),
    unique (agency_id, user_id)
);

-- ============================================================================
--  5) التصنيفات
-- ============================================================================
create table if not exists public.categories (
    id         serial primary key,
    name       text not null,
    icon       text,
    color      text,
    sort_order int default 0,
    is_active  boolean default true
);

-- ============================================================================
--  6) الغرف
-- ============================================================================
create table if not exists public.rooms (
    id             uuid primary key default uuid_generate_v4(),
    owner_id       uuid not null references public.profiles(id) on delete cascade,
    agency_id      uuid references public.agencies(id) on delete set null,
    name           text not null,
    description    text,
    cover_url      text,
    background_url text,
    category_id    int references public.categories(id) on delete set null,
    room_type      text check (room_type in ('public','private','password')) default 'public',
    password_hash  text,
    max_seats      int default 8 check (max_seats between 2 and 20),
    welcome_msg    text,
    is_locked      boolean default false,   -- قفل الغرفة (لا دخول جديد)
    mic_locked     boolean default false,   -- قفل المايك العام
    is_active      boolean default true,
    is_featured    boolean default false,
    listeners      int default 0,
    total_visits   bigint default 0,
    created_at     timestamptz default now(),
    updated_at     timestamptz default now()
);

-- ============================================================================
--  7) أعضاء الغرفة / المقاعد / الأدوار
-- ============================================================================
create table if not exists public.room_members (
    id           uuid primary key default uuid_generate_v4(),
    room_id      uuid not null references public.rooms(id) on delete cascade,
    user_id      uuid not null references public.profiles(id) on delete cascade,
    role         text check (role in ('owner','admin','moderator','member')) default 'member',
    seat_index   int,
    is_muted     boolean default true,
    is_speaking  boolean default false,
    is_hand_up   boolean default false,
    joined_at    timestamptz default now(),
    left_at      timestamptz,
    unique (room_id, user_id)
);

create table if not exists public.room_bans (
    id         uuid primary key default uuid_generate_v4(),
    room_id    uuid not null references public.rooms(id) on delete cascade,
    user_id    uuid not null references public.profiles(id) on delete cascade,
    banned_by  uuid references public.profiles(id) on delete set null,
    reason     text,
    created_at timestamptz default now(),
    unique (room_id, user_id)
);

-- ============================================================================
--  8) رسائل الغرفة
-- ============================================================================
create table if not exists public.room_messages (
    id         uuid primary key default uuid_generate_v4(),
    room_id    uuid not null references public.rooms(id) on delete cascade,
    user_id    uuid references public.profiles(id) on delete set null,
    content    text not null,
    msg_type   text check (msg_type in ('text','system','gift','join','leave')) default 'text',
    created_at timestamptz default now()
);

-- ============================================================================
--  9) الهدايا (SVGA / صور)
-- ============================================================================
create table if not exists public.gifts (
    id            serial primary key,
    name          text not null,
    icon_url      text,
    animation_url text,                 -- رابط ملف SVGA للهدية المتحركة
    anim_type     text default 'svga',  -- svga | lottie | none
    price         int default 0,        -- بالعملات
    diamond_value int default 0,        -- ما يحصل عليه المستقبل بالألماس
    category      text default 'normal',-- normal | luxury | special
    is_active     boolean default true,
    sort_order    int default 0
);

create table if not exists public.gift_transactions (
    id          uuid primary key default uuid_generate_v4(),
    room_id     uuid references public.rooms(id) on delete set null,
    sender_id   uuid references public.profiles(id) on delete set null,
    receiver_id uuid references public.profiles(id) on delete set null,
    gift_id     int references public.gifts(id) on delete set null,
    amount      int default 1,
    total_coins int default 0,
    created_at  timestamptz default now()
);

-- ============================================================================
--  10) الوجهات/البانرات المتحركة (Banners / SVGA)
-- ============================================================================
create table if not exists public.banners (
    id            serial primary key,
    title         text,
    image_url     text,
    animation_url text,                 -- SVGA للبانر المتحرك
    link_url      text,
    placement     text default 'home',  -- home | room | profile
    is_active     boolean default true,
    sort_order    int default 0,
    start_at      timestamptz,
    end_at        timestamptz
);

-- ============================================================================
--  11) المحفظة والمعاملات
-- ============================================================================
create table if not exists public.wallet_transactions (
    id          uuid primary key default uuid_generate_v4(),
    user_id     uuid not null references public.profiles(id) on delete cascade,
    amount      bigint not null,          -- موجب = إيداع، سالب = خصم
    currency    text default 'coins',     -- coins | diamonds
    tx_type     text,                     -- recharge | gift_send | gift_receive | reward | withdraw
    reference   text,
    note        text,
    created_at  timestamptz default now()
);

-- ============================================================================
--  12) المتابعات والأصدقاء
-- ============================================================================
create table if not exists public.follows (
    follower_id  uuid references public.profiles(id) on delete cascade,
    following_id uuid references public.profiles(id) on delete cascade,
    created_at   timestamptz default now(),
    primary key (follower_id, following_id)
);

-- ============================================================================
--  13) الإشعارات
-- ============================================================================
create table if not exists public.notifications (
    id         uuid primary key default uuid_generate_v4(),
    user_id    uuid not null references public.profiles(id) on delete cascade,
    title      text,
    body       text,
    type       text default 'general',
    data       jsonb,
    is_read    boolean default false,
    created_at timestamptz default now()
);

-- ============================================================================
--  14) الفهارس
-- ============================================================================
create index if not exists idx_rooms_active     on public.rooms(is_active) where is_active;
create index if not exists idx_rooms_category   on public.rooms(category_id);
create index if not exists idx_rooms_owner      on public.rooms(owner_id);
create index if not exists idx_rooms_featured   on public.rooms(is_featured) where is_featured;
create index if not exists idx_members_room     on public.room_members(room_id);
create index if not exists idx_members_user     on public.room_members(user_id);
create index if not exists idx_messages_room    on public.room_messages(room_id, created_at desc);
create index if not exists idx_gifttx_room      on public.gift_transactions(room_id, created_at desc);
create index if not exists idx_notif_user       on public.notifications(user_id, created_at desc);
create index if not exists idx_wallet_user      on public.wallet_transactions(user_id, created_at desc);

-- ============================================================================
--  15) الدوال والمشغلات
-- ============================================================================

-- 15.1 إنشاء ملف شخصي تلقائيًا
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
    insert into public.profiles (id, username, display_name, avatar_url)
    values (
        new.id,
        coalesce(new.raw_user_meta_data->>'username', 'user_' || substr(new.id::text, 1, 8)),
        coalesce(new.raw_user_meta_data->>'display_name', new.raw_user_meta_data->>'username', 'مستخدم'),
        new.raw_user_meta_data->>'avatar_url'
    )
    on conflict (id) do nothing;
    return new;
end; $$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- 15.2 تحديث updated_at
create or replace function public.touch_updated_at()
returns trigger language plpgsql as $$
begin new.updated_at = now(); return new; end; $$;

drop trigger if exists trg_profiles_touch on public.profiles;
create trigger trg_profiles_touch before update on public.profiles
    for each row execute function public.touch_updated_at();
drop trigger if exists trg_rooms_touch on public.rooms;
create trigger trg_rooms_touch before update on public.rooms
    for each row execute function public.touch_updated_at();

-- 15.3 الانضمام لغرفة
create or replace function public.join_room(p_room_id uuid, p_password text default null)
returns public.room_members language plpgsql security definer set search_path = public as $$
declare v_room public.rooms; v_member public.room_members; v_count int;
begin
    select * into v_room from public.rooms where id = p_room_id and is_active;
    if not found then raise exception 'ROOM_NOT_FOUND'; end if;
    if v_room.is_locked then raise exception 'ROOM_LOCKED'; end if;
    if exists (select 1 from public.room_bans where room_id = p_room_id and user_id = auth.uid()) then
        raise exception 'USER_BANNED';
    end if;
    if v_room.room_type = 'password' and v_room.password_hash is not null then
        if p_password is null or crypt(p_password, v_room.password_hash) <> v_room.password_hash then
            raise exception 'WRONG_PASSWORD';
        end if;
    end if;

    select * into v_member from public.room_members where room_id = p_room_id and user_id = auth.uid();
    if found then
        update public.room_members set left_at = null, joined_at = now() where id = v_member.id
            returning * into v_member;
        update public.rooms set listeners = (select count(*) from public.room_members
            where room_id = p_room_id and left_at is null), total_visits = total_visits + 1
            where id = p_room_id;
        return v_member;
    end if;

    select count(*) into v_count from public.room_members where room_id = p_room_id and left_at is null;
    if v_count >= v_room.max_seats + 30 then raise exception 'ROOM_FULL'; end if;

    insert into public.room_members (room_id, user_id, role, seat_index, is_muted)
    values (p_room_id, auth.uid(),
            case when v_room.owner_id = auth.uid() then 'owner' else 'member' end, null, true)
    returning * into v_member;

    update public.rooms set listeners = (select count(*) from public.room_members
        where room_id = p_room_id and left_at is null), total_visits = total_visits + 1
        where id = p_room_id;

    insert into public.room_messages (room_id, user_id, content, msg_type)
    values (p_room_id, auth.uid(), null, 'join');

    return v_member;
end; $$;

-- 15.4 مغادرة الغرفة
create or replace function public.leave_room(p_room_id uuid)
returns void language plpgsql security definer set search_path = public as $$
begin
    update public.room_members set left_at = now(), seat_index = null,
        is_speaking = false, is_muted = true, is_hand_up = false
        where room_id = p_room_id and user_id = auth.uid();
    update public.rooms set listeners = (select count(*) from public.room_members
        where room_id = p_room_id and left_at is null) where id = p_room_id;
end; $$;

-- 15.5 حجز / تحرير مقعد
create or replace function public.take_seat(p_room_id uuid, p_seat int)
returns void language plpgsql security definer set search_path = public as $$
declare v_max int; v_locked boolean;
begin
    select max_seats, mic_locked into v_max, v_locked from public.rooms where id = p_room_id;
    if p_seat < 0 or p_seat >= v_max then raise exception 'INVALID_SEAT'; end if;
    if v_locked and not public.is_room_admin(p_room_id) then raise exception 'MIC_LOCKED'; end if;
    if exists (select 1 from public.room_members where room_id = p_room_id
               and seat_index = p_seat and left_at is null and user_id <> auth.uid()) then
        raise exception 'SEAT_TAKEN';
    end if;
    update public.room_members set seat_index = p_seat, is_muted = false
        where room_id = p_room_id and user_id = auth.uid();
end; $$;

create or replace function public.leave_seat(p_room_id uuid)
returns void language plpgsql security definer set search_path = public as $$
begin
    update public.room_members set seat_index = null, is_speaking = false, is_muted = true
        where room_id = p_room_id and user_id = auth.uid();
end; $$;

-- 15.6 هل المستخدم مشرف؟
create or replace function public.is_room_admin(p_room_id uuid)
returns boolean language sql security definer set search_path = public as $$
    select exists (select 1 from public.room_members
        where room_id = p_room_id and user_id = auth.uid()
          and role in ('owner','admin','moderator') and left_at is null);
$$;

-- 15.7 إرسال هدية (خصم + إضافة + تسجيل)
create or replace function public.send_gift(p_room_id uuid, p_receiver uuid, p_gift_id int, p_amount int default 1)
returns void language plpgsql security definer set search_path = public as $$
declare v_price int; v_diamond int; v_total int; v_balance bigint;
begin
    select price, diamond_value into v_price, v_diamond from public.gifts where id = p_gift_id and is_active;
    if not found then raise exception 'GIFT_NOT_FOUND'; end if;
    v_total := v_price * p_amount;

    select coins into v_balance from public.profiles where id = auth.uid() for update;
    if v_balance < v_total then raise exception 'INSUFFICIENT_BALANCE'; end if;

    update public.profiles set coins = coins - v_total where id = auth.uid();
    update public.profiles set diamonds = diamonds + (v_diamond * p_amount) where id = p_receiver;

    insert into public.gift_transactions (room_id, sender_id, receiver_id, gift_id, amount, total_coins)
    values (p_room_id, auth.uid(), p_receiver, p_gift_id, p_amount, v_total);

    insert into public.wallet_transactions (user_id, amount, currency, tx_type, reference)
    values (auth.uid(), -v_total, 'coins', 'gift_send', p_gift_id::text);

    insert into public.wallet_transactions (user_id, amount, currency, tx_type, reference)
    values (p_receiver, v_diamond * p_amount, 'diamonds', 'gift_receive', p_gift_id::text);

    insert into public.room_messages (room_id, user_id, content, msg_type)
    values (p_room_id, auth.uid(), p_gift_id::text, 'gift');
end; $$;

-- 15.8 ترقية/تنزيل دور
create or replace function public.set_member_role(p_room_id uuid, p_user_id uuid, p_role text)
returns void language plpgsql security definer set search_path = public as $$
begin
    if not public.is_room_admin(p_room_id) then raise exception 'NOT_AUTHORIZED'; end if;
    if p_role not in ('admin','moderator','member') then raise exception 'INVALID_ROLE'; end if;
    update public.room_members set role = p_role
        where room_id = p_room_id and user_id = p_user_id;
end; $$;

-- 15.9 كتم/طرد/حظر
create or replace function public.admin_mute(p_room_id uuid, p_user_id uuid, p_mute boolean)
returns void language plpgsql security definer set search_path = public as $$
begin
    if not public.is_room_admin(p_room_id) then raise exception 'NOT_AUTHORIZED'; end if;
    update public.room_members set is_muted = p_mute
        where room_id = p_room_id and user_id = p_user_id;
end; $$;

create or replace function public.kick_user(p_room_id uuid, p_user_id uuid)
returns void language plpgsql security definer set search_path = public as $$
begin
    if not public.is_room_admin(p_room_id) then raise exception 'NOT_AUTHORIZED'; end if;
    update public.room_members set left_at = now(), seat_index = null, is_speaking = false
        where room_id = p_room_id and user_id = p_user_id;
    update public.rooms set listeners = (select count(*) from public.room_members
        where room_id = p_room_id and left_at is null) where id = p_room_id;
end; $$;

create or replace function public.ban_user(p_room_id uuid, p_user_id uuid, p_reason text default null)
returns void language plpgsql security definer set search_path = public as $$
begin
    if not public.is_room_admin(p_room_id) then raise exception 'NOT_AUTHORIZED'; end if;
    insert into public.room_bans (room_id, user_id, banned_by, reason)
    values (p_room_id, p_user_id, auth.uid(), p_reason)
    on conflict (room_id, user_id) do nothing;
    perform public.kick_user(p_room_id, p_user_id);
end; $$;

-- 15.10 إضافة XP وترقية المستوى
create or replace function public.add_xp(p_user_id uuid, p_xp int)
returns void language plpgsql security definer set search_path = public as $$
declare v_new_xp int; v_level int;
begin
    update public.profiles set xp = xp + p_xp where id = p_user_id returning xp into v_new_xp;
    select coalesce(max(level),1) into v_level from public.levels where min_xp <= v_new_xp;
    update public.profiles set level = v_level where id = p_user_id;
end; $$;

-- 15.11 شحن الرصيد
create or replace function public.recharge_coins(p_user_id uuid, p_amount bigint)
returns void language plpgsql security definer set search_path = public as $$
begin
    update public.profiles set coins = coins + p_amount where id = p_user_id;
    insert into public.wallet_transactions (user_id, amount, currency, tx_type)
    values (p_user_id, p_amount, 'coins', 'recharge');
end; $$;

-- ============================================================================
--  16) أمان مستوى الصفوف (RLS)
-- ============================================================================
alter table public.profiles          enable row level security;
alter table public.rooms             enable row level security;
alter table public.room_members      enable row level security;
alter table public.room_messages     enable row level security;
alter table public.room_bans         enable row level security;
alter table public.categories        enable row level security;
alter table public.gifts             enable row level security;
alter table public.gift_transactions enable row level security;
alter table public.follows           enable row level security;
alter table public.app_settings      enable row level security;
alter table public.levels            enable row level security;
alter table public.agencies          enable row level security;
alter table public.agency_members    enable row level security;
alter table public.banners           enable row level security;
alter table public.wallet_transactions enable row level security;
alter table public.notifications     enable row level security;

-- profiles
drop policy if exists p_profiles_select on public.profiles;
create policy p_profiles_select on public.profiles for select using (true);
drop policy if exists p_profiles_update on public.profiles;
create policy p_profiles_update on public.profiles for update using (auth.uid() = id) with check (auth.uid() = id);
drop policy if exists p_profiles_insert on public.profiles;
create policy p_profiles_insert on public.profiles for insert with check (auth.uid() = id);

-- categories / gifts / levels / banners / app_settings (قراءة للجميع، كتابة للسوبر أدمن)
do $$
declare t text;
begin
  foreach t in array array['categories','gifts','levels','banners','app_settings'] loop
    execute format('drop policy if exists p_%s_select on public.%I;', t, t);
    execute format('create policy p_%s_select on public.%I for select using (true);', t, t);
  end loop;
end $$;

-- rooms
drop policy if exists p_rooms_select on public.rooms;
create policy p_rooms_select on public.rooms for select using (true);
drop policy if exists p_rooms_insert on public.rooms;
create policy p_rooms_insert on public.rooms for insert with check (auth.uid() = owner_id);
drop policy if exists p_rooms_update on public.rooms;
create policy p_rooms_update on public.rooms for update using (auth.uid() = owner_id) with check (auth.uid() = owner_id);
drop policy if exists p_rooms_delete on public.rooms;
create policy p_rooms_delete on public.rooms for delete using (auth.uid() = owner_id);

-- room_members
drop policy if exists p_members_select on public.room_members;
create policy p_members_select on public.room_members for select using (true);
drop policy if exists p_members_insert on public.room_members;
create policy p_members_insert on public.room_members for insert with check (auth.uid() = user_id);
drop policy if exists p_members_update on public.room_members;
create policy p_members_update on public.room_members for update using (auth.uid() = user_id or public.is_room_admin(room_id));
drop policy if exists p_members_delete on public.room_members;
create policy p_members_delete on public.room_members for delete using (auth.uid() = user_id or public.is_room_admin(room_id));

-- room_messages
drop policy if exists p_messages_select on public.room_messages;
create policy p_messages_select on public.room_messages for select using (true);
drop policy if exists p_messages_insert on public.room_messages;
create policy p_messages_insert on public.room_messages for insert with check (auth.uid() = user_id);
drop policy if exists p_messages_delete on public.room_messages;
create policy p_messages_delete on public.room_messages for delete using (auth.uid() = user_id or public.is_room_admin(room_id));

-- room_bans
drop policy if exists p_bans_select on public.room_bans;
create policy p_bans_select on public.room_bans for select using (true);
drop policy if exists p_bans_insert on public.room_bans;
create policy p_bans_insert on public.room_bans for insert with check (public.is_room_admin(room_id));

-- gift_transactions
drop policy if exists p_gifttx_select on public.gift_transactions;
create policy p_gifttx_select on public.gift_transactions for select using (true);
drop policy if exists p_gifttx_insert on public.gift_transactions;
create policy p_gifttx_insert on public.gift_transactions for insert with check (auth.uid() = sender_id);

-- follows
drop policy if exists p_follows_select on public.follows;
create policy p_follows_select on public.follows for select using (true);
drop policy if exists p_follows_insert on public.follows;
create policy p_follows_insert on public.follows for insert with check (auth.uid() = follower_id);
drop policy if exists p_follows_delete on public.follows;
create policy p_follows_delete on public.follows for delete using (auth.uid() = follower_id);

-- agencies
drop policy if exists p_agencies_select on public.agencies;
create policy p_agencies_select on public.agencies for select using (true);
drop policy if exists p_agencies_insert on public.agencies;
create policy p_agencies_insert on public.agencies for insert with check (auth.uid() = owner_id);
drop policy if exists p_agencies_update on public.agencies;
create policy p_agencies_update on public.agencies for update using (auth.uid() = owner_id);

-- agency_members
drop policy if exists p_agmembers_select on public.agency_members;
create policy p_agmembers_select on public.agency_members for select using (true);
drop policy if exists p_agmembers_insert on public.agency_members;
create policy p_agmembers_insert on public.agency_members for insert with check (
    exists (select 1 from public.agencies a where a.id = agency_id and a.owner_id = auth.uid())
);

-- wallet_transactions
drop policy if exists p_wallet_select on public.wallet_transactions;
create policy p_wallet_select on public.wallet_transactions for select using (auth.uid() = user_id);
drop policy if exists p_wallet_insert on public.wallet_transactions;
create policy p_wallet_insert on public.wallet_transactions for insert with check (auth.uid() = user_id);

-- notifications
drop policy if exists p_notif_select on public.notifications;
create policy p_notif_select on public.notifications for select using (auth.uid() = user_id);
drop policy if exists p_notif_update on public.notifications;
create policy p_notif_update on public.notifications for update using (auth.uid() = user_id);

-- ============================================================================
--  17) Realtime
-- ============================================================================
alter publication supabase_realtime add table public.rooms;
alter publication supabase_realtime add table public.room_members;
alter publication supabase_realtime add table public.room_messages;
alter publication supabase_realtime add table public.profiles;
alter publication supabase_realtime add table public.gift_transactions;
alter publication supabase_realtime add table public.notifications;

-- ============================================================================
--  18) صلاحيات تنفيذ الدوال
-- ============================================================================
grant execute on function public.join_room(uuid, text)          to authenticated;
grant execute on function public.leave_room(uuid)               to authenticated;
grant execute on function public.take_seat(uuid, int)           to authenticated;
grant execute on function public.leave_seat(uuid)               to authenticated;
grant execute on function public.is_room_admin(uuid)            to authenticated;
grant execute on function public.send_gift(uuid, uuid, int, int) to authenticated;
grant execute on function public.set_member_role(uuid, uuid, text) to authenticated;
grant execute on function public.admin_mute(uuid, uuid, boolean) to authenticated;
grant execute on function public.kick_user(uuid, uuid)          to authenticated;
grant execute on function public.ban_user(uuid, uuid, text)     to authenticated;
grant execute on function public.add_xp(uuid, int)              to authenticated;
grant execute on function public.recharge_coins(uuid, bigint)   to authenticated;

-- ============================================================================
--  انتهى المخطط ✅
-- ============================================================================

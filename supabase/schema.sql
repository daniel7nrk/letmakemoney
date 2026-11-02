-- ================================================================
-- PromptPay — Supabase Database Schema
-- ================================================================
-- HOW TO APPLY:
--   1. Go to https://supabase.com → your project
--   2. Click "SQL Editor" in the left sidebar
--   3. Paste this entire file and click "Run"
-- ================================================================

-- ─── 1. TRANSACTIONS ─────────────────────────────────────────────
create table if not exists public.transactions (
    id                      bigint primary key,
    user_id                 uuid references auth.users(id) on delete cascade,
    amount                  double precision    not null,
    currency                text                not null default 'RWF',
    merchant                text                not null default '',
    description             text                not null default '',
    category                text                not null default 'Uncategorized',
    fee                     double precision    not null default 0.0,
    fee_category            text                not null default 'Transaction Fee',
    source                  text                not null default 'SMS',
    status                  text                not null default 'CONFIRMED',
    is_recurring            boolean             not null default false,
    notes                   text                not null default '',
    payment_method          text                not null default 'Mobile Money',
    date_epoch_millis       bigint              not null,
    updated_at_epoch_millis bigint              not null,
    is_synced               boolean             not null default true,
    created_at              timestamptz         not null default now()
);

-- Only the owner can read/write their own transactions
alter table public.transactions enable row level security;

drop policy if exists "Users can manage own transactions" on public.transactions;
create policy "Users can manage own transactions"
    on public.transactions for all
    using  (auth.uid() = user_id)
    with check (auth.uid() = user_id);

-- Speed up per-user date-sorted queries
create index if not exists idx_tx_user_date
    on public.transactions(user_id, date_epoch_millis desc);


-- ─── 2. SCAM ALERTS ──────────────────────────────────────────────
create table if not exists public.scam_alerts (
    id                bigint      primary key,
    user_id           uuid        references auth.users(id) on delete cascade,
    sender            text        not null,
    body              text        not null,
    primary_reason    text        not null,
    detected_signals  text        not null default '',
    is_overridden     boolean     not null default false,
    date_epoch_millis bigint      not null,
    created_at        timestamptz not null default now()
);

alter table public.scam_alerts enable row level security;

drop policy if exists "Users can manage own scam alerts" on public.scam_alerts;
create policy "Users can manage own scam alerts"
    on public.scam_alerts for all
    using  (auth.uid() = user_id)
    with check (auth.uid() = user_id);


-- ─── 3. USER PROFILES ────────────────────────────────────────────
create table if not exists public.user_profiles (
    id                          bigint      primary key,
    user_id                     uuid        references auth.users(id) on delete cascade unique,
    name                        text        not null default '',
    email                       text        not null default '',
    country_code                text        not null default 'RWANDA',
    currency_code               text        not null default 'RWF',
    language_code               text        not null default 'en',
    lock_mode                   text        not null default 'PIN',
    push_notifications_enabled  boolean     not null default true,
    biometric_enabled           boolean     not null default false,
    updated_at                  timestamptz not null default now()
);

alter table public.user_profiles enable row level security;

drop policy if exists "Users can manage own profile" on public.user_profiles;
create policy "Users can manage own profile"
    on public.user_profiles for all
    using  (auth.uid() = user_id)
    with check (auth.uid() = user_id);


-- ================================================================
-- Done! All 3 tables created with Row Level Security enabled.
-- Each user can only see and edit their own data.
-- ================================================================

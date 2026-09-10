create table if not exists public.push_device_tokens (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users(id) on delete cascade,
    token text not null unique,
    platform text not null default 'android' check (platform = 'android'),
    is_active boolean not null default true,
    last_seen_at timestamptz not null default now(),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists push_device_tokens_user_id_idx
    on public.push_device_tokens (user_id);

alter table public.push_device_tokens enable row level security;

create policy "Users can view their push tokens"
on public.push_device_tokens for select
to authenticated
using ((select auth.uid()) = user_id);

create policy "Users can register their push tokens"
on public.push_device_tokens for insert
to authenticated
with check ((select auth.uid()) = user_id);

create policy "Users can update their push tokens"
on public.push_device_tokens for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

create policy "Users can delete their push tokens"
on public.push_device_tokens for delete
to authenticated
using ((select auth.uid()) = user_id);

grant select, insert, update, delete on public.push_device_tokens to authenticated;

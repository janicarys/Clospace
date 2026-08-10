-- Clospace backend integration migration
-- Run this once in Supabase SQL Editor before testing the merged Android app.

-- 1) Fields used by the updated frontend but missing from the original schema.
alter table public.clothing
    add column if not exists material text,
    add column if not exists tags text,
    add column if not exists times_worn integer not null default 0;

alter table public.outfits
    add column if not exists tags text;

alter table public.outfit_items
    add column if not exists x double precision not null default 0.5,
    add column if not exists y double precision not null default 0.5,
    add column if not exists scale double precision not null default 1.0,
    add column if not exists layer integer not null default 0;

-- 2) RLS policies for the user's own application data.
-- The app always sends the Supabase Auth UUID as user_id.

drop policy if exists "Users can view their clothing" on public.clothing;
drop policy if exists "Users can insert their clothing" on public.clothing;
drop policy if exists "Users can update their clothing" on public.clothing;
drop policy if exists "Users can delete their clothing" on public.clothing;

create policy "Users can view their clothing"
on public.clothing for select to authenticated
using (auth.uid() = user_id);

create policy "Users can insert their clothing"
on public.clothing for insert to authenticated
with check (auth.uid() = user_id);

create policy "Users can update their clothing"
on public.clothing for update to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

create policy "Users can delete their clothing"
on public.clothing for delete to authenticated
using (auth.uid() = user_id);


drop policy if exists "Users can view their outfits" on public.outfits;
drop policy if exists "Users can insert their outfits" on public.outfits;
drop policy if exists "Users can update their outfits" on public.outfits;
drop policy if exists "Users can delete their outfits" on public.outfits;

create policy "Users can view their outfits"
on public.outfits for select to authenticated
using (auth.uid() = user_id);

create policy "Users can insert their outfits"
on public.outfits for insert to authenticated
with check (auth.uid() = user_id);

create policy "Users can update their outfits"
on public.outfits for update to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

create policy "Users can delete their outfits"
on public.outfits for delete to authenticated
using (auth.uid() = user_id);


drop policy if exists "Users can view their outfit items" on public.outfit_items;
drop policy if exists "Users can insert their outfit items" on public.outfit_items;
drop policy if exists "Users can update their outfit items" on public.outfit_items;
drop policy if exists "Users can delete their outfit items" on public.outfit_items;

create policy "Users can view their outfit items"
on public.outfit_items for select to authenticated
using (
    exists (
        select 1
        from public.outfits o
        where o.id = outfit_items.outfit_id
          and o.user_id = auth.uid()
    )
);

create policy "Users can insert their outfit items"
on public.outfit_items for insert to authenticated
with check (
    exists (
        select 1
        from public.outfits o
        where o.id = outfit_items.outfit_id
          and o.user_id = auth.uid()
    )
);

create policy "Users can update their outfit items"
on public.outfit_items for update to authenticated
using (
    exists (
        select 1
        from public.outfits o
        where o.id = outfit_items.outfit_id
          and o.user_id = auth.uid()
    )
)
with check (
    exists (
        select 1
        from public.outfits o
        where o.id = outfit_items.outfit_id
          and o.user_id = auth.uid()
    )
);

create policy "Users can delete their outfit items"
on public.outfit_items for delete to authenticated
using (
    exists (
        select 1
        from public.outfits o
        where o.id = outfit_items.outfit_id
          and o.user_id = auth.uid()
    )
);


drop policy if exists "Users can view their calendar entries" on public.calendar_entries;
drop policy if exists "Users can insert their calendar entries" on public.calendar_entries;
drop policy if exists "Users can update their calendar entries" on public.calendar_entries;
drop policy if exists "Users can delete their calendar entries" on public.calendar_entries;

create policy "Users can view their calendar entries"
on public.calendar_entries for select to authenticated
using (auth.uid() = user_id);

create policy "Users can insert their calendar entries"
on public.calendar_entries for insert to authenticated
with check (
    auth.uid() = user_id
    and exists (
        select 1
        from public.outfits o
        where o.id = calendar_entries.outfit_id
          and o.user_id = auth.uid()
    )
);

create policy "Users can update their calendar entries"
on public.calendar_entries for update to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

create policy "Users can delete their calendar entries"
on public.calendar_entries for delete to authenticated
using (auth.uid() = user_id);

-- 3) Storage upload policy.
-- Files are stored under: <auth-user-uuid>/<random-file-name>.
drop policy if exists "Users can upload their clothing images" on storage.objects;
create policy "Users can upload their clothing images"
on storage.objects for insert to authenticated
with check (
    bucket_id = 'clothing-images'
    and (storage.foldername(name))[1] = auth.uid()::text
);

-- Optional cleanup policy for files owned by the authenticated user.
drop policy if exists "Users can delete their clothing images" on storage.objects;
create policy "Users can delete their clothing images"
on storage.objects for delete to authenticated
using (
    bucket_id = 'clothing-images'
    and (storage.foldername(name))[1] = auth.uid()::text
);

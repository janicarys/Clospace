# Clospace – Frontend + Supabase integration

The active fragment-based frontend now uses `BackendRepository` for Closet, Outfit, and Calendar data. Room remains in the project only for compatibility with older screens/classes.

## Supabase schema used

- `users`
- `clothing`
- `outfits`
- `outfit_items`
- `calendar_entries`
- Storage bucket: `clothing-images` (public)

## Before running the app

1. Open Supabase SQL Editor.
2. Run `supabase/clospace_backend.sql`.
3. Confirm the RLS policies exist for `clothing`, `outfits`, `outfit_items`, and `calendar_entries`.
4. Confirm `clothing-images` is public and has the upload policy from the SQL migration.
5. In Supabase Authentication, create/verify a test account or use the app Sign Up flow.

## Data mapping

The updated UI model is mapped to the existing backend:

- `clothing.id` -> `ClothingItem.id`
- `clothing.user_id` -> authenticated Supabase user UUID
- `clothing.image_url` -> public Storage URL, cached locally for rendering
- `outfits.id` -> `Outfit.id`
- `outfits.name` -> `Outfit.caption`
- `outfits.occasion` -> `Outfit.occasion`
- `calendar_entries.wear_date` -> `Outfit.plannedDate`
- `outfit_items.outfit_id` + `clothing_id` -> outfit membership
- `outfit_items.x/y/scale/layer` -> outfit canvas placement

The migration adds the frontend fields that were not present in the original schema: `clothing.material`, `clothing.tags`, `clothing.times_worn`, `outfits.tags`, and `outfit_items.x/y/scale/layer`.

## Authentication

Supabase Auth is now the source of truth for login. The existing username field is treated as the user's email address because Supabase Email Auth requires an email/password pair. The `users.id` profile UUID matches `auth.uid()`.

If email confirmation is enabled in Supabase Auth, the user must confirm the email before logging in.

## Build verification

A full Gradle build could not be executed in the packaging environment because the Gradle wrapper distribution is not cached and external network access is unavailable. Open the project in Android Studio and let Gradle sync normally before running the app.

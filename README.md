# Clospace

A personal wardrobe and outfit-planning app for Android. Build a digital closet by photographing (or importing) your clothes, letting ML remove the background and guess each item's category, then compose outfits on a freeform canvas and schedule them on a calendar.

Built with **Kotlin** and a **Supabase** backend (Postgres + Auth + Storage).

---

## Features

### Closet (Add & organize your clothes)
- Add garments from the **camera** or **gallery**.
- **ML Kit Subject Segmentation** automatically removes the photo background (fallback: keeps the original image).
- **ML Kit Image Labeling** auto-detects the category (Top / Bottom / Footwear / Accessory); you can override it.
- Each garment stores name, category, color, material, and reusable tags.
- Category-grouped grid (Tops, Bottoms, Footwear, Accessories) with live search across name/category/color/material/tags.
- **Photo editor**: rotate 90°, fine rotation slider (±45°), flip, with undo/redo.

### Outfits (Compose & style)
- Browse your closet and multi-select garments to compose an outfit.
- **Freeform canvas**: drag items to position them, pinch to scale (0.4x–4x), tap to select, reorder layers (Bring Forward / Send to Back).
- **Drag-to-delete trash zone**: a subtle trash target appears only while dragging and deletes the item on drop.
- Add more clothes to an existing canvas at any time.
- Save outfits with a caption, reusable tags, multiple occasions, and an optional scheduled date.
- Rich previews recreated from the saved placement data (not a screenshot), so the layout is preserved faithfully across screens.

### Calendar (Plan your week)
- Month view with outfit thumbnails on scheduled days and a multi-outfit badge.
- Tap a day to see, open, or remove that day's outfits.
- Create an outfit for a specific date and return right back to that day.

### Accounts
- Email/password sign-up and sign-in via Supabase Auth.
- Profile display name and gender, avatar, and sign-out.
- **Row Level Security**: every user can only read/write their own data.

---

## Tech Stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | XML layouts, Material Components, ViewPager2 + Fragments, RecyclerView |
| Backend / DB | Supabase (PostgREST, Auth, Storage) |
| Image segmentation | Google ML Kit Subject Segmentation |
| Image labeling | Google ML Kit Image Labeling |
| Asynchronous | Kotlin Coroutines |
| Serialization | kotlinx.serialization |
| Local (image/cache) | SharedPreferences, disk cache, in-memory preview LRU |
| Legacy | Room (retained for backward-compat screens) |

---

## Project structure

```
app/src/main/java/com/mobdeve/s15/reyes/janicamegan/clospace/
├── BackendRepository.kt        # Main Supabase data layer (CRUD, tags, outfits, calendar, image cache)
├── AuthRepository.kt           # Email/password auth, session helpers
├── SupabaseManager.kt          # Shared Supabase client singleton
├── SessionManager.kt           # Session persistence (SharedPreferences)
├── MainActivity.kt             # 4-tab host (Closet / Outfit / Calendar / Settings)
├── SplashActivity.kt           # Splash + data pre-warm
├── LoginActivity.kt            # Email/password login
├── SignUpActivity.kt           # Account sign-up
├── ClosetFragment.kt           # Closet tab: add garments, grid + search
├── OutfitFragment.kt           # Outfit tab: grid of saved outfits + search
├── CalendarFragment.kt         # Calendar tab: month grid + day sheet
├── SettingsFragment.kt         # Settings tab menu
├── SelectGarmentsActivity.kt   # Multi-select garments to build an outfit
├── OutfitCanvasActivity.kt     # Freeform drag/scale/trash canvas
├── OutfitDetailsActivity.kt    # New-outfit details & save
├── OutfitDetailActivity.kt     # Saved-outfit view & edit
├── GarmentDetailActivity.kt    # Garment detail & edit (tags, fields, used-in)
├── ItemImageEditorActivity.kt  # Rotate/flip/undo/redo photo editor
├── ManageTagsActivity.kt       # Manage reusable tags (create/rename/delete)
├── AccountActivity.kt          # Profile / sign-out
├── ClospaceBottomSheets.kt     # Styled reusable bottom sheets + day-outfit sheet
├── TagPickerDialog.kt          # Chip-based tag picker sheet
├── adapter/                    # RecyclerView adapters
├── util/                       # ImageDecoder, OutfitRenderer, preview cache, insets, transitions,
│                               # GarmentCutout (ML Kit), GarmentClassifier (ML Kit)
└── view/DraggableImageView.kt  # Custom view: drag + pinch + drop callbacks
```

`supabase/clospace_backend.sql` — the one-time SQL migration (columns + RLS policies).

---

## Setting up a local build

1. **Clone / open in Android Studio** and let Gradle sync.
2. **Supabase backend** (one time):
   - Create a Supabase project, enable **Email** auth.
   - Open the **SQL Editor**, paste the contents of `supabase/clospace_backend.sql`, and run it. This adds the missing columns and the RLS policies.
   - Create the `clothing-images` Storage bucket (public).
3. **Credentials**: the project URL and publishable key live in `SupabaseManager.kt`. For development builds, update them to match your Supabase project (keep them out of version control for production).
4. Open the project in Android Studio, select a device/emulator (API 24+, minSdk 24), and **Run**.

---

## How a garment gets added (quick tour)

1. `ClosetFragment` launches the camera/gallery → the photo is copied into app storage.
2. `GarmentCutout` runs **ML Kit Subject Segmentation** and writes a transparent-background PNG (falls back to the original on failure).
3. `GarmentClassifier` runs **ML Kit Image Labeling** to suggest a category.
4. `BackendRepository.insertClothing` uploads the PNG to Supabase Storage and inserts a `clothing` row scoped to the signed-in user.
5. The grid reloads from `getClothing()`, which downloads/caches the image locally.

## How an outfit is composed (quick tour)

1. `SelectGarmentsActivity` → pick items → **NEXT**.
2. `OutfitCanvasActivity` renders each garment as a draggable/scalable view; positions are normalized (fractions of canvas width/height) and carry the canvas aspect ratio.
3. On save, placements are stored in `outfit_items`.
4. `OutfitRenderer` rebuilds the preview from those placements (reproducing the portrait layout, padding, and each photo's letterbox) — so previews match the canvas wherever they appear.
5. The calendar & outfit lists show these rendered previews (cached per outfit).

---

## Notes

- The legacy Room screens (`OutfitActivity`, `CalendarActivity`, `SettingsActivity`) are retained from the project's earlier version for navigation compatibility; the current fragment-based UI is the supported path.
- `fallbackToDestructiveMigration()` is set on the Room database; data loss only affects the unused legacy tables.
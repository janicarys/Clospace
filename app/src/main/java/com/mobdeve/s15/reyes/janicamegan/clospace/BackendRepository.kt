package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.UUID

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

/** Active backend data layer for the updated fragment-based frontend. */
class BackendRepository(private val context: Context) {

    private val client = SupabaseManager.client
    private val imageCacheDir by lazy {
        File(context.cacheDir, "supabase_clothing").apply { mkdirs() }
    }

    private fun requireUserId(): String =
        client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No authenticated Supabase user")

    suspend fun getClothing(): List<ClothingItem> {
        val userId = requireUserId()
        val rows = client.from("clothing").select {
            filter { eq("user_id", userId) }
        }.decodeList<ClothingRow>()
        val tagMap = clothingTagMap(userId)
        return rows.map { it.toUi(tagMap[it.id]) }
    }

    /** Pre-fetches the main data sets (and caches their images) so the first
     *  screen after login loads instantly. */
    suspend fun warmUp() {
        getClothing()
        getOutfits()
        getTags()
    }

    suspend fun getClothingById(id: Int): ClothingItem? {
        val userId = requireUserId()
        val row = client.from("clothing").select {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }.decodeList<ClothingRow>().firstOrNull() ?: return null
        val tagMap = clothingTagMap(userId)
        return row.toUi(tagMap[row.id])
    }

    suspend fun insertClothing(
        category: String,
        localImagePath: String,
        name: String = "",
        color: String = "",
        material: String = "",
        tags: List<String> = emptyList()
    ): ClothingItem {
        val userId = requireUserId()
        val extension = if (localImagePath.lowercase().endsWith(".png")) "png" else "jpg"
        val storagePath = "$userId/${UUID.randomUUID()}.$extension"
        val file = File(localImagePath)

        client.storage.from("clothing-images").upload(storagePath, file.readBytes())

        val publicUrl = client.storage.from("clothing-images").publicUrl(storagePath)

        val inserted = client.from("clothing").insert(
            ClothingInsert(
                userId = userId,
                name = name,
                category = category,
                color = color,
                material = material,
                tags = tags.joinToString(", "),
                season = "",
                imageUrl = publicUrl,
                favorite = false,
                timesWorn = 0
            )
        ) { select() }.decodeSingle<ClothingRow>()

        replaceClothingTags(inserted.id.toInt(), tags)

        return inserted.toUi()
    }

    suspend fun updateClothing(
        id: Int,
        name: String? = null,
        category: String? = null,
        color: String? = null,
        material: String? = null,
        tags: String? = null
    ) {
        val userId = requireUserId()
        val current = client.from("clothing").select {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }.decodeList<ClothingRow>().firstOrNull()
            ?: throw IllegalArgumentException("Clothing item not found")

        client.from("clothing").update(
            ClothingUpdate(
                name = name ?: current.name.orEmpty(),
                category = category ?: current.category.orEmpty(),
                color = color ?: current.color.orEmpty(),
                material = material ?: current.material.orEmpty(),
                tags = tags ?: current.tags.orEmpty(),
                timesWorn = current.timesWorn
            )
        ) {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }
    }

    suspend fun deleteClothing(id: Int) {
        val userId = requireUserId()
        client.from("outfit_items").delete { filter { eq("clothing_id", id) } }
        client.from("clothing").delete {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }
    }

    /** Replaces an item's photo with an edited local image (uploaded to storage). */
    suspend fun updateClothingImage(id: Int, localImagePath: String) {
        val userId = requireUserId()
        val extension = if (localImagePath.lowercase().endsWith(".png")) "png" else "jpg"
        val storagePath = "$userId/${UUID.randomUUID()}.$extension"
        client.storage.from("clothing-images").upload(storagePath, File(localImagePath).readBytes())
        val publicUrl = client.storage.from("clothing-images").publicUrl(storagePath)
        client.from("clothing").update(ClothingImageUpdate(imageUrl = publicUrl)) {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }
    }

    // ---- Reusable tags ----

    suspend fun getTags(): List<Tag> {
        val userId = requireUserId()
        return client.from("tags").select {
            filter { eq("user_id", userId) }
        }.decodeList<TagRow>()
            .map { it.toUi() }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun getClothingTags(clothingId: Int): List<String> {
        val rows = client.from("clothing_tags").select {
            filter { eq("clothing_id", clothingId.toLong()) }
        }.decodeList<ClothingTagRow>()
        if (rows.isEmpty()) return emptyList()
        return tagNamesByIds(rows.map { it.tagId })
    }

    suspend fun getOutfitTags(outfitId: Int): List<String> {
        val rows = client.from("outfit_tags").select {
            filter { eq("outfit_id", outfitId.toLong()) }
        }.decodeList<OutfitTagRow>()
        if (rows.isEmpty()) return emptyList()
        return tagNamesByIds(rows.map { it.tagId })
    }

    /** Ensures each name exists as a reusable tag, then syncs the join rows for this garment. */
    suspend fun replaceClothingTags(clothingId: Int, names: List<String>) {
        val userId = requireUserId()
        val cleaned = names.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val tagIds = ensureTags(cleaned)
        client.from("clothing_tags").delete { filter { eq("clothing_id", clothingId.toLong()) } }
        if (tagIds.isNotEmpty()) {
            client.from("clothing_tags").insert(tagIds.map { ClothingTagInsert(clothingId = clothingId.toLong(), tagId = it) })
        }
        updateClothing(clothingId, tags = cleaned.joinToString(", "))
    }

    /** Ensures each name exists as a reusable tag, then syncs the join rows for this outfit. */
    suspend fun replaceOutfitTags(outfitId: Int, names: List<String>) {
        val userId = requireUserId()
        val cleaned = names.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val tagIds = ensureTags(cleaned)
        client.from("outfit_tags").delete { filter { eq("outfit_id", outfitId.toLong()) } }
        if (tagIds.isNotEmpty()) {
            client.from("outfit_tags").insert(tagIds.map { OutfitTagInsert(outfitId = outfitId.toLong(), tagId = it) })
        }
    }

    /** Creates a new reusable tag, returning it (id + normalized name). */
    suspend fun createTag(name: String): Tag {
        val userId = requireUserId()
        val cleaned = name.trim()
        val existing = client.from("tags").select {
            filter {
                eq("user_id", userId)
                eq("name", cleaned)
            }
        }.decodeList<TagRow>().firstOrNull()
        if (existing != null) return existing.toUi()
        val inserted = client.from("tags").insert(
            TagInsert(userId = userId, name = cleaned)
        ) { select() }.decodeSingle<TagRow>()
        return inserted.toUi()
    }

    /** Renames a reusable tag everywhere it is used. */
    suspend fun renameTag(tagId: Long, newName: String) {
        val userId = requireUserId()
        client.from("tags").update(
            TagUpdate(name = newName.trim())
        ) {
            filter {
                eq("id", tagId)
                eq("user_id", userId)
            }
        }
    }

    /** Deletes a reusable tag and its links from all garments/outfits. */
    suspend fun deleteTag(tagId: Long) {
        val userId = requireUserId()
        client.from("tags").delete {
            filter {
                eq("id", tagId)
                eq("user_id", userId)
            }
        }
    }

    private suspend fun ensureTags(names: List<String>): List<Long> {
        val userId = requireUserId()
        if (names.isEmpty()) return emptyList()
        val existing = client.from("tags").select {
            filter { eq("user_id", userId) }
        }.decodeList<TagRow>()
        val byName = existing.associate { it.name to it.id }
        val result = mutableListOf<Long>()
        for (name in names) {
            val found = byName[name]
            if (found != null) {
                result += found
            } else {
                val inserted = client.from("tags").insert(
                    TagInsert(userId = userId, name = name)
                ) { select() }.decodeSingle<TagRow>()
                result += inserted.id
            }
        }
        return result
    }

    private suspend fun tagNamesByIds(ids: List<Long>): List<String> {
        if (ids.isEmpty()) return emptyList()
        val rows = client.from("tags").select().decodeList<TagRow>()
        val byId = rows.associate { it.id to it.name }
        return ids.mapNotNull { byId[it] }.sortedBy { it.lowercase() }
    }

    private suspend fun clothingTagMap(userId: String): Map<Long, List<String>> {
        val links = client.from("clothing_tags").select().decodeList<ClothingTagRow>()
        if (links.isEmpty()) return emptyMap()
        val nameById = client.from("tags").select {
            filter { eq("user_id", userId) }
        }.decodeList<TagRow>().associate { it.id to it.name }
        return links.groupBy { it.clothingId }.mapValues { (_, rows) ->
            rows.mapNotNull { nameById[it.tagId] }.sortedBy { it.lowercase() }
        }
    }

    private suspend fun outfitTagMap(userId: String): Map<Long, List<String>> {
        val links = client.from("outfit_tags").select().decodeList<OutfitTagRow>()
        if (links.isEmpty()) return emptyMap()
        val nameById = client.from("tags").select {
            filter { eq("user_id", userId) }
        }.decodeList<TagRow>().associate { it.id to it.name }
        return links.groupBy { it.outfitId }.mapValues { (_, rows) ->
            rows.mapNotNull { nameById[it.tagId] }.sortedBy { it.lowercase() }
        }
    }

    private suspend fun ClothingRow.toUi(tagNames: List<String>?): ClothingItem {
        return ClothingItem(
            id = id.toInt(),
            ownerId = userId.hashCode(),
            name = name.orEmpty(),
            category = category.orEmpty(),
            color = color.orEmpty(),
            material = material.orEmpty(),
            tags = tagNames?.joinToString(", ") ?: tags.orEmpty(),
            imagePath = cacheImage(imageUrl).orEmpty(),
            timesWorn = timesWorn
        )
    }

    suspend fun getOutfits(): List<OutfitWithItems> {
        val userId = requireUserId()
        val rows = client.from("outfits").select {
            filter { eq("user_id", userId) }
        }.decodeList<OutfitRow>()
        return rows.sortedByDescending { it.id }.map { buildOutfitWithItems(it, userId) }
    }

    suspend fun getOutfitById(id: Int): OutfitWithItems? {
        val userId = requireUserId()
        val row = client.from("outfits").select {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }.decodeList<OutfitRow>().firstOrNull() ?: return null
        return buildOutfitWithItems(row, userId)
    }

    suspend fun getOutfitsForClothing(clothingId: Int): List<Outfit> {
        val userId = requireUserId()
        val itemRows = client.from("outfit_items").select {
            filter { eq("clothing_id", clothingId) }
        }.decodeList<OutfitItemRow>()
        if (itemRows.isEmpty()) return emptyList()
        val ids = itemRows.map { it.outfitId }.toSet()
        val rows = client.from("outfits").select {
            filter { eq("user_id", userId) }
        }.decodeList<OutfitRow>()
        val tagMap = outfitTagMap(userId)
        return rows.filter { it.id in ids }.map { it.toUi(getPlannedDate(it.id), tagMap[it.id]) }
    }

    suspend fun createOutfit(
        caption: String?,
        occasion: String?,
        selectedDate: String?,
        tags: String?,
        placements: List<OutfitItem>
    ): Int {
        val userId = requireUserId()
        val row = client.from("outfits").insert(
            OutfitInsert(userId = userId, name = caption, occasion = occasion, tags = tags)
        ) { select() }.decodeSingle<OutfitRow>()
        val id = row.id.toInt()
        replaceOutfitItems(id, placements)
        setCalendarDate(row.id, selectedDate)
        syncOutfitTags(id, tags)
        return id
    }

    suspend fun updateOutfit(
        id: Int,
        caption: String?,
        occasion: String?,
        selectedDate: String?,
        tags: String?,
        placements: List<OutfitItem>? = null
    ) {
        val userId = requireUserId()
        client.from("outfits").update(
            OutfitUpdate(name = caption, occasion = occasion, tags = tags)
        ) {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }
        if (placements != null) replaceOutfitItems(id, placements)
        setCalendarDate(id.toLong(), selectedDate)
        syncOutfitTags(id, tags)
    }

    private suspend fun syncOutfitTags(outfitId: Int, tags: String?) {
        val names = tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        replaceOutfitTags(outfitId, names)
    }

    suspend fun deleteOutfit(id: Int) {
        val userId = requireUserId()
        client.from("calendar_entries").delete { filter { eq("outfit_id", id) } }
        client.from("outfit_items").delete { filter { eq("outfit_id", id) } }
        client.from("outfits").delete {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }
    }

    suspend fun getCalendarOutfits(month: String): Map<LocalDate, List<OutfitWithItems>> {
        val userId = requireUserId()
        val start = LocalDate.parse("$month-01")
        val end = start.plusMonths(1)
        val entries = client.from("calendar_entries").select {
            filter {
                eq("user_id", userId)
                gte("wear_date", start.toString())
                lt("wear_date", end.toString())
            }
        }.decodeList<CalendarRow>()

        val result = linkedMapOf<LocalDate, MutableList<OutfitWithItems>>()
        for (entry in entries) {
            val outfit = getOutfitById(entry.outfitId.toInt()) ?: continue
            result.getOrPut(LocalDate.parse(entry.wearDate)) { mutableListOf() }.add(outfit)
        }
        return result
    }

    suspend fun setCalendarDate(outfitId: Long, date: String?) {
        val userId = requireUserId()
        client.from("calendar_entries").delete {
            filter {
                eq("outfit_id", outfitId)
                eq("user_id", userId)
            }
        }
        if (!date.isNullOrBlank()) {
            client.from("calendar_entries").insert(
                CalendarInsert(userId = userId, outfitId = outfitId, wearDate = date)
            )
        }
    }

    suspend fun removeFromCalendar(outfitId: Long, date: String) {
        val userId = requireUserId()
        client.from("calendar_entries").delete {
            filter {
                eq("outfit_id", outfitId)
                eq("wear_date", date)
                eq("user_id", userId)
            }
        }
    }

    private suspend fun replaceOutfitItems(outfitId: Int, placements: List<OutfitItem>) {
        client.from("outfit_items").delete { filter { eq("outfit_id", outfitId) } }
        if (placements.isEmpty()) return
        client.from("outfit_items").insert(
            placements.map {
                OutfitItemInsert(
                    outfitId = outfitId.toLong(),
                    clothingId = it.clothingId.toLong(),
                    x = it.x.toDouble(),
                    y = it.y.toDouble(),
                    scale = it.scale.toDouble(),
                    layer = it.layer
                )
            }
        )
    }

    private suspend fun buildOutfitWithItems(row: OutfitRow, userId: String): OutfitWithItems {
        val itemRows = client.from("outfit_items").select {
            filter { eq("outfit_id", row.id) }
        }.decodeList<OutfitItemRow>()

        val clothingRows = if (itemRows.isEmpty()) emptyList() else client.from("clothing").select {
            filter { eq("user_id", userId) }
        }.decodeList<ClothingRow>()
        val clothingById = clothingRows.associateBy { it.id }
        val clothingTags = clothingTagMap(userId)
        val outfitTags = outfitTagMap(userId)

        val placements = itemRows.mapNotNull { item ->
            clothingById[item.clothingId]?.let { clothing ->
                OutfitPlacement(
                    item = clothing.toUi(clothingTags[clothing.id]),
                    x = item.x.toFloat(),
                    y = item.y.toFloat(),
                    scale = item.scale.toFloat(),
                    layer = item.layer
                )
            }
        }.sortedBy { it.layer }

        return OutfitWithItems(
            outfit = row.toUi(getPlannedDate(row.id), outfitTags[row.id]),
            placements = placements
        )
    }

    private suspend fun getPlannedDate(outfitId: Long): String? {
        val userId = requireUserId()
        return client.from("calendar_entries").select {
            filter {
                eq("outfit_id", outfitId)
                eq("user_id", userId)
            }
        }.decodeList<CalendarRow>().firstOrNull()?.wearDate
    }

    private fun TagRow.toUi(): Tag = Tag(id = id, name = name)

    private suspend fun ClothingRow.toUi(): ClothingItem {
        return ClothingItem(
            id = id.toInt(),
            ownerId = userId.hashCode(),
            name = name.orEmpty(),
            category = category.orEmpty(),
            color = color.orEmpty(),
            material = material.orEmpty(),
            tags = tags.orEmpty(),
            imagePath = cacheImage(imageUrl).orEmpty(),
            timesWorn = timesWorn
        )
    }

    private fun OutfitRow.toUi(plannedDate: String?, tagNames: List<String>?): Outfit = Outfit(
        id = id.toInt(),
        ownerId = userId.hashCode(),
        caption = name,
        occasion = occasion,
        tags = tagNames?.joinToString(", ") ?: tags,
        plannedDate = plannedDate
    )

    private suspend fun cacheImage(url: String?): String? {
        if (url.isNullOrBlank()) return null
        if (!url.startsWith("http")) return url
        val file = File(imageCacheDir, UUID.nameUUIDFromBytes(url.toByteArray()).toString())
        if (file.exists() && file.length() > 0) return file.absolutePath
        return withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 20_000
                connection.connect()
                if (connection.responseCode !in 200..299) return@runCatching null
                connection.inputStream.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                file.absolutePath
            }.getOrNull()
        }
    }
}

@Serializable
data class ClothingRow(
    val id: Long,
    @SerialName("user_id") val userId: String,
    val name: String? = null,
    val category: String? = null,
    val color: String? = null,
    val season: String? = null,
    val material: String? = null,
    val tags: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val favorite: Boolean = false,
    @SerialName("times_worn") val timesWorn: Int = 0
)

@Serializable
data class ClothingInsert(
    @SerialName("user_id") val userId: String,
    val name: String,
    val category: String,
    val color: String,
    val season: String,
    val material: String,
    val tags: String,
    @SerialName("image_url") val imageUrl: String,
    val favorite: Boolean,
    @SerialName("times_worn") val timesWorn: Int
)

@Serializable
data class ClothingUpdate(
    val name: String,
    val category: String,
    val color: String,
    val material: String,
    val tags: String,
    @SerialName("times_worn") val timesWorn: Int
)

@Serializable
data class ClothingImageUpdate(
    @SerialName("image_url") val imageUrl: String
)

@Serializable
data class OutfitRow(
    val id: Long,
    @SerialName("user_id") val userId: String,
    val name: String? = null,
    val occasion: String? = null,
    val tags: String? = null
)

@Serializable
data class OutfitInsert(
    @SerialName("user_id") val userId: String,
    val name: String? = null,
    val occasion: String? = null,
    val tags: String? = null
)

@Serializable
data class OutfitUpdate(
    val name: String? = null,
    val occasion: String? = null,
    val tags: String? = null
)

@Serializable
data class OutfitItemRow(
    @SerialName("outfit_id") val outfitId: Long,
    @SerialName("clothing_id") val clothingId: Long,
    val x: Double = 0.5,
    val y: Double = 0.5,
    val scale: Double = 1.0,
    val layer: Int = 0
)

@Serializable
data class OutfitItemInsert(
    @SerialName("outfit_id") val outfitId: Long,
    @SerialName("clothing_id") val clothingId: Long,
    val x: Double,
    val y: Double,
    val scale: Double,
    val layer: Int
)

@Serializable
data class CalendarRow(
    val id: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("outfit_id") val outfitId: Long,
    @SerialName("wear_date") val wearDate: String
)

@Serializable
data class CalendarInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("outfit_id") val outfitId: Long,
    @SerialName("wear_date") val wearDate: String
)

// ---- Reusable tags (normalized) ----

data class Tag(val id: Long, var name: String)

@Serializable
data class TagRow(
    val id: Long,
    @SerialName("user_id") val userId: String,
    val name: String
)

@Serializable
data class TagInsert(
    @SerialName("user_id") val userId: String,
    val name: String
)

@Serializable
data class TagUpdate(
    val name: String
)

@Serializable
data class ClothingTagRow(
    @SerialName("clothing_id") val clothingId: Long,
    @SerialName("tag_id") val tagId: Long
)

@Serializable
data class ClothingTagInsert(
    @SerialName("clothing_id") val clothingId: Long,
    @SerialName("tag_id") val tagId: Long
)

@Serializable
data class OutfitTagRow(
    @SerialName("outfit_id") val outfitId: Long,
    @SerialName("tag_id") val tagId: Long
)

@Serializable
data class OutfitTagInsert(
    @SerialName("outfit_id") val outfitId: Long,
    @SerialName("tag_id") val tagId: Long
)

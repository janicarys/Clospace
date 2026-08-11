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
        return client.from("clothing").select {
            filter { eq("user_id", userId) }
        }.decodeList<ClothingRow>().map { it.toUi() }
    }

    suspend fun getClothingById(id: Int): ClothingItem? {
        val userId = requireUserId()
        return client.from("clothing").select {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }.decodeList<ClothingRow>().firstOrNull()?.toUi()
    }

    suspend fun insertClothing(
        category: String,
        localImagePath: String,
        name: String = "",
        color: String = "",
        material: String = "",
        tags: String = ""
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
                tags = tags,
                season = "",
                imageUrl = publicUrl,
                favorite = false,
                timesWorn = 0
            )
        ) { select() }.decodeSingle<ClothingRow>()

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

    suspend fun getOutfits(): List<OutfitWithItems> {
        val userId = requireUserId()
        val rows = client.from("outfits").select {
            filter { eq("user_id", userId) }
        }.decodeList<OutfitRow>()
        return rows.sortedByDescending { it.id }.map { buildOutfitWithItems(it) }
    }

    suspend fun getOutfitById(id: Int): OutfitWithItems? {
        val userId = requireUserId()
        val row = client.from("outfits").select {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }.decodeList<OutfitRow>().firstOrNull() ?: return null
        return buildOutfitWithItems(row)
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
        return rows.filter { it.id in ids }.map { it.toUi(getPlannedDate(it.id)) }
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
        replaceOutfitItems(row.id.toInt(), placements)
        setCalendarDate(row.id, selectedDate)
        return row.id.toInt()
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

    private suspend fun buildOutfitWithItems(row: OutfitRow): OutfitWithItems {
        val itemRows = client.from("outfit_items").select {
            filter { eq("outfit_id", row.id) }
        }.decodeList<OutfitItemRow>()

        val clothingRows = if (itemRows.isEmpty()) emptyList() else client.from("clothing").select {
            filter { eq("user_id", requireUserId()) }
        }.decodeList<ClothingRow>()
        val clothingById = clothingRows.associateBy { it.id }

        val placements = itemRows.mapNotNull { item ->
            clothingById[item.clothingId]?.let { clothing ->
                OutfitPlacement(
                    item = clothing.toUi(),
                    x = item.x.toFloat(),
                    y = item.y.toFloat(),
                    scale = item.scale.toFloat(),
                    layer = item.layer
                )
            }
        }.sortedBy { it.layer }

        return OutfitWithItems(
            outfit = row.toUi(getPlannedDate(row.id)),
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

    private fun OutfitRow.toUi(plannedDate: String?): Outfit = Outfit(
        id = id.toInt(),
        ownerId = userId.hashCode(),
        caption = name,
        occasion = occasion,
        tags = tags,
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

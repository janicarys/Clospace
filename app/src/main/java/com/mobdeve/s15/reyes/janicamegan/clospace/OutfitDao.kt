package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.*

@Dao
interface OutfitDao {

    @Insert
    suspend fun insert(outfit: Outfit): Long

    @Update
    suspend fun update(outfit: Outfit)

    @Delete
    suspend fun delete(outfit: Outfit)

    @Query("SELECT * FROM outfits WHERE ownerId = :ownerId ORDER BY id DESC")
    suspend fun getAll(ownerId: Int): List<Outfit>

    @Query("SELECT * FROM outfits WHERE id = :id")
    suspend fun getById(id: Int): Outfit?

    @Insert
    suspend fun insertOutfitItem(item: OutfitItem)

    @Insert
    suspend fun insertOutfitItems(items: List<OutfitItem>)

    @Query("""
        SELECT clothing_items.* FROM clothing_items
        INNER JOIN outfit_items ON clothing_items.id = outfit_items.clothingId
        WHERE outfit_items.outfitId = :outfitId
    """)
    suspend fun getItemsForOutfit(outfitId: Int): List<ClothingItem>
}

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

    @Query("""
        SELECT outfits.* FROM outfits
        INNER JOIN outfit_items ON outfit_items.outfitId = outfits.id
        WHERE outfit_items.clothingId = :clothingId
        ORDER BY outfits.id DESC
    """)
    suspend fun getOutfitsForClothing(clothingId: Int): List<Outfit>

    @Query("""
        SELECT * FROM outfits
        WHERE ownerId = :ownerId
        AND plannedDate IS NOT NULL
        AND plannedDate LIKE :monthPrefix || '%'
        ORDER BY plannedDate
    """)
    suspend fun getByPlannedMonth(ownerId: Int, monthPrefix: String): List<Outfit>

    @Insert
    suspend fun insertOutfitItems(items: List<OutfitItem>)

    @Query("DELETE FROM outfit_items WHERE outfitId = :outfitId")
    suspend fun deleteOutfitItems(outfitId: Int)

    @Query("DELETE FROM outfit_items WHERE clothingId = :clothingId")
    suspend fun deleteOutfitItemsForClothing(clothingId: Int)

    @Query("""
        SELECT * FROM outfit_items
        INNER JOIN clothing_items ON clothing_items.id = outfit_items.clothingId
        WHERE outfit_items.outfitId = :outfitId
    """)
    suspend fun getOutfitItemsWithClothing(outfitId: Int): List<OutfitItemWithClothing>
}

package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.*

@Dao
interface ClothingDao {

    @Insert
    suspend fun insert(item: ClothingItem)

    @Update
    suspend fun update(item: ClothingItem)

    @Delete
    suspend fun delete(item: ClothingItem)

    @Query("SELECT * FROM clothing_items WHERE ownerId = :ownerId ORDER BY id DESC")
    suspend fun getAll(ownerId: Int): List<ClothingItem>

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getById(id: Int): ClothingItem?

    @Query("""
        SELECT * FROM clothing_items
        WHERE ownerId = :ownerId
        AND (
            name LIKE '%' || :query || '%'
            OR category LIKE '%' || :query || '%'
            OR color LIKE '%' || :query || '%'
            OR tags LIKE '%' || :query || '%'
        )
    """)
    suspend fun search(ownerId: Int, query: String): List<ClothingItem>

    @Query("SELECT * FROM clothing_items WHERE ownerId = :ownerId AND category = :category")
    suspend fun filterByCategory(ownerId: Int, category: String): List<ClothingItem>

    @Query("SELECT * FROM clothing_items WHERE ownerId = :ownerId ORDER BY timesWorn DESC LIMIT 1")
    suspend fun mostWorn(ownerId: Int): ClothingItem?

    @Query("SELECT * FROM clothing_items WHERE ownerId = :ownerId ORDER BY timesWorn ASC LIMIT 1")
    suspend fun leastWorn(ownerId: Int): ClothingItem?
}
package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.*

@Dao
interface OutfitDao {

    @Insert
    suspend fun insert(outfit: Outfit)

    @Update
    suspend fun update(outfit: Outfit)

    @Delete
    suspend fun delete(outfit: Outfit)

    @Query("SELECT * FROM outfits WHERE ownerId = :ownerId")
    suspend fun getAll(ownerId: Int): List<Outfit>

    @Query("SELECT * FROM outfits WHERE id = :id")
    suspend fun getById(id: Int): Outfit?
}
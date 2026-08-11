package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.*

@Dao
interface UserDao {

    @Insert
    suspend fun register(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}
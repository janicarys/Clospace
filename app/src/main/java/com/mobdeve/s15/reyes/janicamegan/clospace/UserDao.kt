package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.*

@Dao
interface UserDao {

    @Insert
    suspend fun register(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}
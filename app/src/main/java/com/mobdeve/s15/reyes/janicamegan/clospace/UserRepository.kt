package com.mobdeve.s15.reyes.janicamegan.clospace

class UserRepository(
    private val userDao: UserDao
) {

    suspend fun register(user: User): Result<Long> {

        val existing = userDao.getUserByUsername(user.username)

        if (existing != null) {
            return Result.failure(Exception("Username already exists"))
        }

        val id = userDao.register(user)

        return Result.success(id)
    }

    suspend fun login(
        username: String,
        password: String
    ): User? {

        return userDao.login(username, password)
    }

    suspend fun getUser(username: String): User? {

        return userDao.getUserByUsername(username)
    }
}
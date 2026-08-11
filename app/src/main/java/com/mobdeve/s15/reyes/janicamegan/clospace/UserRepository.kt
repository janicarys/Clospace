package com.mobdeve.s15.reyes.janicamegan.clospace

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class UserRepository {

    private val client = SupabaseManager.client
    private val authRepository = AuthRepository()

    suspend fun register(user: User): Result<String> = runCatching {
        // The app's existing "email" field is used as the Supabase Auth email.
        authRepository.signUp(user.email, user.password)

        val id = client.auth.currentUserOrNull()?.id
            ?: return@runCatching ""

        client.from("users").insert(
            UserProfileInsert(
                id = id,
                email = user.email,
                displayName = user.displayName,
                avatar = user.avatar
            )
        )

        id
    }

    suspend fun login(email: String, password: String): UserProfileRow? = runCatching {
        authRepository.login(email, password)
        val id = authRepository.currentUserId()
            ?: throw IllegalStateException("Supabase did not return an authenticated user")

        val profile = client.from("users").select {
            filter { eq("id", id) }
        }.decodeList<UserProfileRow>().firstOrNull()

        if (profile == null) {
            client.from("users").insert(
                UserProfileInsert(
                    id = id,
                    email = email,
                    displayName = email.substringBefore("@").ifBlank { email },
                    avatar = ""
                )
            )
            UserProfileRow(
                id = id,
                email = email,
                displayName = email.substringBefore("@").ifBlank { email },
                avatar = ""
            )
        } else {
            profile
        }
    }.getOrNull()

    suspend fun getCurrentUser(): UserProfileRow? {
        val id = authRepository.currentUserId() ?: return null
        return client.from("users").select {
            filter { eq("id", id) }
        }.decodeList<UserProfileRow>().firstOrNull()
    }
}

@Serializable
data class UserProfileRow(
    val id: String,
    @SerialName("username") val email: String,
    @SerialName("display_name") val displayName: String,
    val avatar: String? = null
)

@Serializable
data class UserProfileInsert(
    val id: String,
    @SerialName("username") val email: String,
    @SerialName("display_name") val displayName: String,
    val avatar: String
)

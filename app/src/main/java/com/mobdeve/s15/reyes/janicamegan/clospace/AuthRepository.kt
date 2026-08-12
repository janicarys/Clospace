package com.mobdeve.s15.reyes.janicamegan.clospace

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {

    private val client = SupabaseManager.client

    suspend fun signUp(
        email: String,
        password: String
    ) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun login(
        email: String,
        password: String
    ) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun logout() {
        client.auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return client.auth.currentSessionOrNull() != null
    }

    fun currentUserId(): String? {
        return client.auth.currentUserOrNull()?.id
    }
}
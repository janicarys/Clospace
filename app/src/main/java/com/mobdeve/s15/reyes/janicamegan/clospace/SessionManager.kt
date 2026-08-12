package com.mobdeve.s15.reyes.janicamegan.clospace

import io.github.jan.supabase.auth.auth
import android.content.Context

/** Supabase session is the source of truth. Legacy Int methods remain for older screens. */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("clospace_session", Context.MODE_PRIVATE)

    fun saveUserId(id: String) {
        prefs.edit().putString("supabase_user_id", id).apply()
    }

    fun saveUserId(id: Int) {
        prefs.edit().putInt("legacy_user_id", id).apply()
    }

    fun getSupabaseUserId(): String? =
        SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: prefs.getString("supabase_user_id", null)

    fun getUserId(): Int = prefs.getInt("legacy_user_id", -1)

    fun isLoggedIn(): Boolean =
        SupabaseManager.client.auth.currentSessionOrNull() != null || !getSupabaseUserId().isNullOrBlank()

    fun logout() {
        prefs.edit().clear().apply()
    }
}

package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences(
            "clospace_session",
            Context.MODE_PRIVATE
        )

    fun saveUserId(id: Int) {
        prefs.edit().putInt("user_id", id).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != -1
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
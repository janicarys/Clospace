package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val session = SessionManager(this)

        lifecycleScope.launch {
            val started = System.currentTimeMillis()

            // While the splash is visible, pre-fetch the database so the main
            // screen has its data (and images) ready as soon as it opens.
            if (session.isLoggedIn()) {
                runCatching { BackendRepository(this@SplashActivity).warmUp() }
            }

            // Hold the splash long enough that it doesn't just flash by.
            val remaining = MIN_DISPLAY_MS - (System.currentTimeMillis() - started)
            if (remaining > 0) delay(remaining)

            if (session.isLoggedIn()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
    }

    companion object {
        private const val MIN_DISPLAY_MS = 1500L
    }
}

package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AccountActivity : AppCompatActivity() {

    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        findViewById<TextView>(R.id.tvToolbarTitle).text = getString(R.string.account)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnSignOut).setOnClickListener {
            confirmSignOut()
        }

        loadProfile()
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            val profile = userRepository.getCurrentUser() ?: return@launch

            findViewById<TextView>(R.id.tvDisplayName).text = profile.displayName
            findViewById<TextView>(R.id.tvEmail).text = profile.username

            val avatarRes = if (profile.avatar == "male") R.drawable.ic_avatar_male else R.drawable.ic_avatar_female
            findViewById<ImageView>(R.id.imgAvatar).setImageResource(avatarRes)
        }
    }

    private fun confirmSignOut() {
        ClospaceBottomSheets.showConfirm(
            this,
            R.string.sign_out_confirm_title,
            R.string.sign_out_confirm_message,
            R.string.sign_out
        ) { signOut() }
    }

    private fun signOut() {
        lifecycleScope.launch {
            runCatching { authRepository.logout() }
            SessionManager(this@AccountActivity).logout()

            Toast.makeText(this@AccountActivity, R.string.signed_out, Toast.LENGTH_SHORT).show()

            startActivity(Intent(this@AccountActivity, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }
    }
}

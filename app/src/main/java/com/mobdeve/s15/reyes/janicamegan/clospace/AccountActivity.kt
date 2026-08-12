package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobdeve.s15.reyes.janicamegan.clospace.util.applyBottomInsetAsMargin
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

        findViewById<Button>(R.id.btnSignOut).applyBottomInsetAsMargin()

        findViewById<View>(R.id.rowDisplayName).setOnClickListener {
            promptEditDisplayName()
        }

        findViewById<View>(R.id.rowGender).setOnClickListener {
            promptEditGender()
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
            findViewById<TextView>(R.id.tvGender).text = genderLabel(profile.avatar)

            val avatarRes = if (profile.avatar == "male") R.drawable.ic_avatar_male else R.drawable.ic_avatar_female
            findViewById<ImageView>(R.id.imgAvatar).setImageResource(avatarRes)
        }
    }

    private fun genderLabel(avatar: String?): String {
        val tv = findViewById<TextView>(R.id.tvGender)
        return when (avatar) {
            "male" -> getString(R.string.gender_male).also { tv.setTextColor(getColor(R.color.brown)) }
            "female" -> getString(R.string.gender_female).also { tv.setTextColor(getColor(R.color.brown)) }
            else -> {
                tv.setTextColor(getColor(R.color.lavender))
                getString(R.string.gender_not_set)
            }
        }
    }

    private fun promptEditGender() {
        val genders = arrayOf(getString(R.string.gender_male), getString(R.string.gender_female))
        val current = findViewById<TextView>(R.id.tvGender).text.toString()
        val selected = genders.indexOf(current).takeIf { it >= 0 } ?: -1
        ClospaceBottomSheets.showChoice(
            this,
            R.string.edit_gender,
            genders,
            selected
        ) { which ->
            val value = if (which == 0) "male" else "female"
            lifecycleScope.launch {
                val ok = userRepository.updateGender(value)
                if (ok) {
                    findViewById<TextView>(R.id.tvGender).text = getString(if (which == 0) R.string.gender_male else R.string.gender_female)
                    val avatarRes = if (value == "male") R.drawable.ic_avatar_male else R.drawable.ic_avatar_female
                    findViewById<ImageView>(R.id.imgAvatar).setImageResource(avatarRes)
                    Toast.makeText(this@AccountActivity, R.string.gender_updated, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AccountActivity, R.string.gender_update_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun promptEditDisplayName() {
        val current = findViewById<TextView>(R.id.tvDisplayName).text.toString()
        ClospaceBottomSheets.showInput(
            this,
            R.string.edit_display_name,
            getString(R.string.hint_display_name),
            current
        ) { raw ->
            val name = raw.trim().takeIf { it.isNotBlank() } ?: return@showInput
            lifecycleScope.launch {
                val ok = userRepository.updateDisplayName(name)
                if (ok) {
                    findViewById<TextView>(R.id.tvDisplayName).text = name
                    Toast.makeText(this@AccountActivity, R.string.display_name_updated, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AccountActivity, R.string.display_name_update_failed, Toast.LENGTH_SHORT).show()
                }
            }
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

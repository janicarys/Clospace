package com.mobdeve.s15.reyes.janicamegan.clospace

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var repository: UserRepository
    private lateinit var ivAvatarMale: ImageButton
    private lateinit var ivAvatarFemale: ImageButton
    private var selectedAvatar = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        repository = UserRepository()

        ivAvatarMale = findViewById(R.id.ivAvatarMale)
        ivAvatarFemale = findViewById(R.id.ivAvatarFemale)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etDisplayName = findViewById<EditText>(R.id.etDisplayName)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnTogglePassword = findViewById<ImageButton>(R.id.btnTogglePassword)
        val btnToggleConfirmPassword = findViewById<ImageButton>(R.id.btnToggleConfirmPassword)

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility(etPassword, btnTogglePassword)
        }

        btnToggleConfirmPassword.setOnClickListener {
            togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword)
        }
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val tvSignIn = findViewById<TextView>(R.id.tvSignIn)

        ivAvatarMale.setOnClickListener { selectAvatar(true) }
        ivAvatarFemale.setOnClickListener { selectAvatar(false) }

        btnSubmit.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val displayName = etDisplayName.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            when {
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etUsername.error = "Enter a valid email address"
                    return@setOnClickListener
                }
                displayName.isBlank() -> {
                    etDisplayName.error = "Display name is required"
                    return@setOnClickListener
                }
                password.length < 8 -> {
                    etPassword.error = "Password must be at least 8 characters"
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    etConfirmPassword.error = "Passwords do not match"
                    return@setOnClickListener
                }
                selectedAvatar.isBlank() -> {
                    Toast.makeText(this, "Please select an avatar.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            lifecycleScope.launch {
                btnSubmit.isEnabled = false
                val result = repository.register(
                    User(username = email, displayName = displayName, password = password, avatar = selectedAvatar)
                )
                btnSubmit.isEnabled = true

                if (result.isSuccess) {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Account created. Check your email if confirmation is required, then sign in.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@SignUpActivity,
                        result.exceptionOrNull()?.message ?: "Registration failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        tvSignIn.setOnClickListener { finish() }
    }

    private fun selectAvatar(isMale: Boolean) {
        selectedAvatar = if (isMale) "male" else "female"
        ivAvatarMale.setBackgroundResource(if (isMale) R.drawable.avatar_selected else android.R.color.transparent)
        ivAvatarFemale.setBackgroundResource(if (!isMale) R.drawable.avatar_selected else android.R.color.transparent)
    }

    private fun togglePasswordVisibility(field: EditText, button: ImageButton) {
        if (field.transformationMethod == PasswordTransformationMethod.getInstance()) {
            field.transformationMethod = HideReturnsTransformationMethod.getInstance()
            button.setImageResource(R.drawable.ic_visibility_off)
        } else {
            field.transformationMethod = PasswordTransformationMethod.getInstance()
            button.setImageResource(R.drawable.ic_visibility)
        }
        field.setSelection(field.text?.length ?: 0)
    }
}

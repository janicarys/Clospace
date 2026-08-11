package com.mobdeve.s15.reyes.janicamegan.clospace

import io.github.jan.supabase.auth.auth
import android.content.Intent
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

class LoginActivity : AppCompatActivity() {

    private lateinit var repository: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        repository = UserRepository()
        session = SessionManager(this)

        if (SupabaseManager.client.auth.currentSessionOrNull() != null) {
            openMain()
            return
        }

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnTogglePassword = findViewById<ImageButton>(R.id.btnTogglePassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnTogglePassword.setOnClickListener {
            if (etPassword.transformationMethod == PasswordTransformationMethod.getInstance()) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnTogglePassword.setImageResource(R.drawable.ic_visibility)
            }
            etPassword.setSelection(etPassword.text?.length ?: 0)
        }
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                btnLogin.isEnabled = false
                val user = repository.login(email, password)
                btnLogin.isEnabled = true

                if (user != null) {
                    session.saveUserId(user.id)
                    Toast.makeText(this@LoginActivity, "Welcome ${user.displayName}!", Toast.LENGTH_SHORT).show()
                    openMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

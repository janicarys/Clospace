package com.mobdeve.s15.reyes.janicamegan.clospace

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {


    private lateinit var ivAvatarMale: ImageButton
    private lateinit var ivAvatarFemale: ImageButton
    private var selectedAvatar = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        ivAvatarMale = findViewById(R.id.ivAvatarMale)
        ivAvatarFemale = findViewById(R.id.ivAvatarFemale)

        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val tvSignIn = findViewById<TextView>(R.id.tvSignIn)

        ivAvatarMale.setOnClickListener {
            selectAvatar(true)
        }

        ivAvatarFemale.setOnClickListener {
            selectAvatar(false)
        }

        btnSubmit.setOnClickListener {

            // Later we'll save:
            // username
            // password
            // display name
            // selectedAvatar

            finish()
        }

        tvSignIn.setOnClickListener {
            finish()
        }
    }

    private fun selectAvatar(isMale: Boolean) {

        if (isMale) {
            selectedAvatar = "male"

            ivAvatarMale.setBackgroundResource(R.drawable.avatar_selected)
            ivAvatarFemale.setBackgroundResource(android.R.color.transparent)

        } else {
            selectedAvatar = "female"

            ivAvatarFemale.setBackgroundResource(R.drawable.avatar_selected)
            ivAvatarMale.setBackgroundResource(android.R.color.transparent)
        }
    }
}
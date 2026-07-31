package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val about = findViewById<LinearLayout>(R.id.layoutAbout)
        val support = findViewById<LinearLayout>(R.id.layoutSupport)

        about.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        support.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }
    }
}
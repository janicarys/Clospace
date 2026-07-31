package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Settings cards
        val about = findViewById<LinearLayout>(R.id.layoutAbout)
        val support = findViewById<LinearLayout>(R.id.layoutSupport)

        about.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        support.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        // Bottom Navigation
        val navCloset = findViewById<LinearLayout>(R.id.navCloset)
        val navOutfit = findViewById<LinearLayout>(R.id.navOutfit)
        val navCalendar = findViewById<LinearLayout>(R.id.navCalendar)

        navCloset.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        navOutfit.setOnClickListener {
            //startActivity(Intent(this, OutfitActivity::class.java))
            finish()
        }

        navCalendar.setOnClickListener {
            //startActivity(Intent(this, CalendarActivity::class.java))
            finish()
        }

    }
}
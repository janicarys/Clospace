package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

import com.mobdeve.s15.reyes.janicamegan.clospace.util.TransitionUtil

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
            val (enter, exit) = TransitionUtil.slide(
                TransitionUtil.TAB_SETTINGS,
                TransitionUtil.TAB_CLOSET
            )
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(enter, exit)
            finish()
            overridePendingTransition(enter, exit)
        }

        navOutfit.setOnClickListener {
            val (enter, exit) = TransitionUtil.slide(
                TransitionUtil.TAB_SETTINGS,
                TransitionUtil.TAB_OUTFIT
            )
            startActivity(Intent(this, OutfitActivity::class.java))
            overridePendingTransition(enter, exit)
            finish()
            overridePendingTransition(enter, exit)
        }

        navCalendar.setOnClickListener {
            val (enter, exit) = TransitionUtil.slide(
                TransitionUtil.TAB_SETTINGS,
                TransitionUtil.TAB_CALENDAR
            )
            startActivity(Intent(this, CalendarActivity::class.java))
            overridePendingTransition(enter, exit)
            finish()
            overridePendingTransition(enter, exit)
        }

    }
}
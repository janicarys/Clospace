package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout

import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bottom Navigation
        val navCloset = findViewById<LinearLayout>(R.id.navCloset)
        val navOutfit = findViewById<LinearLayout>(R.id.navOutfit)
        val navCalendar = findViewById<LinearLayout>(R.id.navCalendar)
        val navSettings = findViewById<LinearLayout>(R.id.navSettings)

        // Floating Action Button
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        // Closet
        // Already on this page
        navCloset.setOnClickListener {
            // Do nothing
        }

        // Outfit
        navOutfit.setOnClickListener {
            // TODO: Replace once OutfitActivity exists
            // startActivity(Intent(this, OutfitActivity::class.java))
        }

        // Calendar
        navCalendar.setOnClickListener {
            // TODO: Replace once CalendarActivity exists
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        // Settings
        navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Floating Action Button
        fabAdd.setOnClickListener {

            // TODO
            // Open Add Garment screen

        }
    }
}
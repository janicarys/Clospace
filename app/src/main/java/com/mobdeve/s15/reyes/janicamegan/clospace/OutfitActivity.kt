package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.mobdeve.s15.reyes.janicamegan.clospace.adapter.OutfitAdapter

import kotlinx.coroutines.launch

class OutfitActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private lateinit var outfitDao: OutfitDao

    private lateinit var recycler: RecyclerView

    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outfit)

        sessionManager = SessionManager(this)

        outfitDao = ClospaceDatabase.getDatabase(this).outfitDao()

        recycler = findViewById(R.id.rvOutfits)
        recycler.layoutManager = GridLayoutManager(this, 2)

        tvEmpty = findViewById(R.id.tvEmpty)

        findViewById<FloatingActionButton>(R.id.fabAddOutfit).setOnClickListener {
            startActivity(Intent(this, SelectGarmentsActivity::class.java))
        }

        // Bottom Navigation
        findViewById<LinearLayout>(R.id.navCloset).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadOutfits()
    }

    private fun loadOutfits() {

        val ownerId = sessionManager.getUserId()

        if (ownerId == -1) {
            return
        }

        lifecycleScope.launch {

            val outfits = outfitDao.getAll(ownerId)

            val full = outfits.map { outfit ->
                OutfitWithItems(
                    outfit,
                    outfitDao.getItemsForOutfit(outfit.id)
                )
            }

            recycler.adapter = OutfitAdapter(full)

            tvEmpty.visibility =
                if (full.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}

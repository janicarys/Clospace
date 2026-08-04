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

            if (outfits.isEmpty()) {

                // TEMPORARY: show sample outfits while the closet is still being built
                recycler.adapter = OutfitAdapter(sampleOutfits(), onOpen = {})
                tvEmpty.visibility = View.GONE

            } else {

                val full = outfits.map { outfit ->

                    val placements =
                        outfitDao.getOutfitItemsWithClothing(outfit.id)
                            .map { joined ->

                                OutfitPlacement(
                                    item = joined.clothing,
                                    x = joined.placement.x,
                                    y = joined.placement.y,
                                    scale = joined.placement.scale,
                                    layer = joined.placement.layer
                                )
                            }

                    OutfitWithItems(
                        outfit = outfit,
                        placements = placements
                    )
                }

                recycler.adapter = OutfitAdapter(full) { wrapper ->
                    openOutfit(wrapper.outfit.id)
                }

                tvEmpty.visibility = View.GONE
            }
        }
    }

    private fun openOutfit(outfitId: Int) {

        if (outfitId <= 0) {
            return
        }

        startActivity(
            Intent(this, OutfitDetailActivity::class.java)
                .putExtra(OutfitDetailActivity.EXTRA_OUTFIT_ID, outfitId)
        )
    }

    private fun sampleOutfits(): List<OutfitWithItems> {

        return listOf(
            Triple("Sunny Day Outfit", "Casual", "2026-08-10") to R.drawable.sample_outfit,
            Triple("Friday Night", "Party", null) to R.drawable.sample_outfit2,
            Triple("Office Look", "Work", "2026-08-12") to R.drawable.sample_outfit3
        ).map { (meta, res) ->

            OutfitWithItems(
                outfit = Outfit(
                    ownerId = -1,
                    caption = meta.first,
                    occasion = meta.second,
                    plannedDate = meta.third
                ),
                placements = emptyList(),
                previewRes = res
            )
        }
    }
}

package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.mobdeve.s15.reyes.janicamegan.clospace.util.ImageDecoder
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitRenderer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OutfitDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OUTFIT_ID = "outfitId"
    }

    private lateinit var outfitDao: OutfitDao

    private var outfitId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outfit_detail)

        outfitDao = ClospaceDatabase.getDatabase(this).outfitDao()

        outfitId = intent.getIntExtra(EXTRA_OUTFIT_ID, 0)

        findViewById<TextView>(R.id.tvToolbarTitle).text =
            getString(R.string.outfit_detail_title)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnEdit).setOnClickListener {

            startActivity(
                Intent(this, OutfitCanvasActivity::class.java)
                    .putExtra(OutfitCanvasActivity.EXTRA_OUTFIT_ID, outfitId)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        loadDetail()
    }

    private fun loadDetail() {

        lifecycleScope.launch {

            val outfit = outfitDao.getById(outfitId) ?: return@launch

            val joined = outfitDao.getOutfitItemsWithClothing(outfitId)

            val placements = joined.map { item ->
                OutfitPlacement(
                    item = item.clothing,
                    x = item.placement.x,
                    y = item.placement.y,
                    scale = item.placement.scale,
                    layer = item.placement.layer
                )
            }

            bindFields(outfit)
            bindClothes(joined.map { it.clothing })

            val preview = withContext(Dispatchers.Default) {
                OutfitRenderer.render(placements, 360, 420)
            }

            if (preview != null) {
                findViewById<ImageView>(R.id.imgOutfitPreview)
                    .setImageBitmap(preview)
            }
        }
    }

    private fun bindFields(outfit: Outfit) {

        val caption = outfit.caption

        val tvCaption = findViewById<TextView>(R.id.tvCaption)

        if (caption.isNullOrBlank()) {

            tvCaption.visibility = View.GONE

        } else {

            tvCaption.visibility = View.VISIBLE
            tvCaption.text = caption
        }

        findViewById<TextView>(R.id.tvTags).text =
            outfit.tags?.takeIf { it.isNotBlank() } ?: getString(R.string.no_tags)

        findViewById<TextView>(R.id.tvOccasion).text =
            outfit.occasion?.takeIf { it.isNotBlank() }
                ?: getString(R.string.no_category)

        findViewById<TextView>(R.id.tvSchedule).text =
            outfit.plannedDate?.takeIf { it.isNotBlank() }
                ?: getString(R.string.not_scheduled)
    }

    private fun bindClothes(clothes: List<ClothingItem>) {

        val container = findViewById<LinearLayout>(R.id.containerClothes)
        container.removeAllViews()

        val inflater = layoutInflater

        val categories = mutableSetOf<String>()

        for (item in clothes) {

            if (!item.category.isNullOrBlank()) {
                categories.add(item.category)
            }

            val card = inflater.inflate(R.layout.item_included_clothing, container, false)

            val image = card.findViewById<ImageView>(R.id.imgClothing)

            val bitmap = ImageDecoder.decode(item.imagePath, 120)

            if (bitmap != null) {
                image.setImageBitmap(bitmap)
            }

            card.findViewById<TextView>(R.id.tvClothingCategory)
                .text = item.category

            container.addView(card)
        }

        findViewById<TextView>(R.id.tvCategory).text =
            if (categories.isEmpty())
                getString(R.string.no_category)
            else
                categories.sorted().joinToString(", ")
    }
}
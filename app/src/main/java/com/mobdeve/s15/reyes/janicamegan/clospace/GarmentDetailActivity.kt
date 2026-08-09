package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

import com.mobdeve.s15.reyes.janicamegan.clospace.util.ImageDecoder
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitRenderer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GarmentDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLOTHING_ID = "clothingId"
    }

    private lateinit var clothingDao: ClothingDao

    private lateinit var outfitDao: OutfitDao

    private var clothingId: Int = 0

    private var currentCaption: String = ""

    private var currentTags: List<String> = emptyList()

    private var currentColor: String = ""

    private var currentMaterial: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garment_detail)

        clothingDao = ClospaceDatabase.getDatabase(this).clothingDao()
        outfitDao = ClospaceDatabase.getDatabase(this).outfitDao()

        clothingId = intent.getIntExtra(EXTRA_CLOTHING_ID, 0)

        findViewById<TextView>(R.id.tvToolbarTitle).text =
            getString(R.string.garment_detail_title)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.tvCaption).setOnClickListener { promptCaption() }

        findViewById<View>(R.id.rowTags).setOnClickListener { promptTags() }

        findViewById<View>(R.id.rowColor).setOnClickListener { promptColor() }

        findViewById<View>(R.id.rowMaterial).setOnClickListener { promptMaterial() }

        findViewById<View>(R.id.rowCreateOutfit).setOnClickListener { createOutfit() }

        findViewById<View>(R.id.btnDelete).setOnClickListener { confirmDelete() }
    }

    private fun createOutfit() {

        startActivity(
            Intent(this, OutfitCanvasActivity::class.java)
                .putExtra(OutfitCanvasActivity.EXTRA_CLOTHING_IDS, intArrayOf(clothingId))
        )
    }

    private fun promptCaption() {

        val input = TextInputEditText(this)

        input.hint = getString(R.string.hint_name)
        input.text = currentCaption.toEditable()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.caption)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                saveCaption(input.text?.toString())
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveCaption(value: String?) {

        val normalized = value?.trim()?.takeIf { it.isNotBlank() }

        lifecycleScope.launch {

            val item = clothingDao.getById(clothingId) ?: return@launch

            clothingDao.update(item.copy(name = normalized ?: ""))

            bindCaption(normalized)
        }
    }

    private fun promptTags() {

        val input = TextInputEditText(this)

        input.hint = getString(R.string.tags_hint)
        input.text = currentTags.joinToString(", ").toEditable()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.tags_label)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                saveTags(input.text?.toString())
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveTags(value: String?) {

        val normalized = value
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.joinToString(", ")
            ?.takeIf { it.isNotBlank() }

        lifecycleScope.launch {

            val item = clothingDao.getById(clothingId) ?: return@launch

            clothingDao.update(item.copy(tags = normalized ?: ""))

            bindTags(normalized)
        }
    }

    private fun promptColor() {

        val input = TextInputEditText(this)

        input.hint = getString(R.string.hint_color)
        input.text = currentColor.toEditable()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.color_label)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                saveColor(input.text?.toString())
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveColor(value: String?) {

        val normalized = value?.trim()?.takeIf { it.isNotBlank() }

        lifecycleScope.launch {

            val item = clothingDao.getById(clothingId) ?: return@launch

            clothingDao.update(item.copy(color = normalized ?: ""))

            bindColor(normalized)
        }
    }

    private fun promptMaterial() {

        val input = TextInputEditText(this)

        input.hint = getString(R.string.hint_material)
        input.text = currentMaterial.toEditable()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.material_label)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                saveMaterial(input.text?.toString())
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveMaterial(value: String?) {

        val normalized = value?.trim()?.takeIf { it.isNotBlank() }

        lifecycleScope.launch {

            val item = clothingDao.getById(clothingId) ?: return@launch

            clothingDao.update(item.copy(material = normalized ?: ""))

            bindMaterial(normalized)
        }
    }

    private fun String.toEditable() = android.text.Editable.Factory.getInstance().newEditable(this)

    private fun confirmDelete() {

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_garment_title)
            .setMessage(R.string.delete_garment_message)
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.delete) { _, _ -> deleteGarment() }
            .show()
    }

    private fun deleteGarment() {

        lifecycleScope.launch {

            val item = clothingDao.getById(clothingId) ?: return@launch

            outfitDao.deleteOutfitItemsForClothing(clothingId)
            clothingDao.delete(item)

            Toast.makeText(
                this@GarmentDetailActivity,
                R.string.garment_deleted,
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    private fun bindCaption(caption: String?) {

        currentCaption = caption ?: ""

        val tvCaption = findViewById<TextView>(R.id.tvCaption)

        tvCaption.text =
            if (currentCaption.isEmpty())
                getString(R.string.add_caption)
            else
                currentCaption

        tvCaption.setTextColor(
            resources.getColor(
                if (currentCaption.isEmpty())
                    R.color.brown
                else
                    R.color.violet,
                null
            )
        )
    }

    private fun bindTags(rawTags: String?) {

        currentTags = rawTags
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        findViewById<TextView>(R.id.tvTags).text =
            if (currentTags.isEmpty())
                getString(R.string.add_tags)
            else
                currentTags.joinToString(" · ")
    }

    private fun bindColor(color: String?) {

        currentColor = color ?: ""

        findViewById<TextView>(R.id.tvColor).text =
            currentColor.takeIf { it.isNotBlank() }
                ?: getString(R.string.add_color)
    }

    private fun bindMaterial(material: String?) {

        currentMaterial = material ?: ""

        findViewById<TextView>(R.id.tvMaterial).text =
            currentMaterial.takeIf { it.isNotBlank() }
                ?: getString(R.string.add_material)
    }

    private fun bindOutfits(outfits: List<Outfit>) {

        val container = findViewById<LinearLayout>(R.id.containerOutfits)
        val empty = findViewById<TextView>(R.id.tvUsedInEmpty)
        val count = findViewById<TextView>(R.id.tvUsedInCount)

        if (outfits.isEmpty()) {

            container.visibility = View.GONE
            count.visibility = View.GONE
            empty.visibility = View.VISIBLE
            return
        }

        container.removeAllViews()
        container.visibility = View.VISIBLE
        empty.visibility = View.GONE

        count.text =
            resources.getQuantityString(R.plurals.used_in_count, outfits.size, outfits.size)
        count.visibility = View.VISIBLE

        val inflater = layoutInflater

        for (outfit in outfits) {

            val card = inflater.inflate(
                R.layout.item_included_outfit,
                container,
                false
            )

            card.setOnClickListener {

                startActivity(
                    Intent(this, OutfitDetailActivity::class.java)
                        .putExtra(OutfitDetailActivity.EXTRA_OUTFIT_ID, outfit.id)
                )
            }

            container.addView(card)

            val image = card.findViewById<ImageView>(R.id.imgOutfitPreview)

            lifecycleScope.launch {

                val placements = outfitDao.getOutfitItemsWithClothing(outfit.id)
                    .map { joined ->
                        OutfitPlacement(
                            item = joined.clothing,
                            x = joined.placement.x,
                            y = joined.placement.y,
                            scale = joined.placement.scale,
                            layer = joined.placement.layer
                        )
                    }

                val preview = withContext(Dispatchers.Default) {
                    OutfitRenderer.render(placements, 180, 220)
                }

                if (preview != null) {
                    image.setImageBitmap(preview)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadDetail()
    }

    private fun loadDetail() {

        lifecycleScope.launch {

            val item = clothingDao.getById(clothingId) ?: return@launch

            val bitmap = withContext(Dispatchers.IO) {
                ImageDecoder.decode(item.imagePath, 512)
            }

            if (bitmap != null) {
                findViewById<ImageView>(R.id.imgGarmentPreview)
                    .setImageBitmap(bitmap)
            }

            bindCaption(item.name)
            bindTags(item.tags)
            bindColor(item.color)
            bindMaterial(item.material)

            findViewById<TextView>(R.id.tvCategory).text =
                item.category?.takeIf { it.isNotBlank() }
                    ?: getString(R.string.no_category)

            val outfits = outfitDao.getOutfitsForClothing(clothingId)

            bindOutfits(outfits)
        }
    }
}
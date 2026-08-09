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

import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.mobdeve.s15.reyes.janicamegan.clospace.util.ImageDecoder
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitRenderer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import java.time.Instant
import java.time.ZoneId

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

        findViewById<View>(R.id.btnEdit).setOnClickListener { openEditor() }

        findViewById<View>(R.id.tvCaption).setOnClickListener { promptCaption() }

        findViewById<View>(R.id.rowTags).setOnClickListener { promptTags() }

        findViewById<View>(R.id.rowOccasion).setOnClickListener { promptOccasion() }

        findViewById<View>(R.id.rowSchedule).setOnClickListener { promptSchedule() }

        findViewById<View>(R.id.btnDelete).setOnClickListener { confirmDelete() }
    }

    private fun openEditor() {

        startActivity(
            Intent(this, OutfitCanvasActivity::class.java)
                .putExtra(OutfitCanvasActivity.EXTRA_OUTFIT_ID, outfitId)
        )
    }

    private fun promptTags() {

        val current = currentTags

        val input = com.google.android.material.textfield.TextInputEditText(this)

        input.hint = getString(R.string.tags_hint)
        input.text = current.joinToString(", ").toEditable()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.tags_label)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                saveTags(input.text?.toString())
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun promptOccasion() {

        val occasions = resources.getStringArray(R.array.occasions)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.occasion)
            .setItems(occasions) { _, which ->
                saveOccasion(occasions[which])
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private var currentTags: List<String> = emptyList()

    private fun promptSchedule() {

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.pick_date)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->

            val date = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString()

            saveDate(date)
        }

        picker.show(supportFragmentManager, "date_picker")
    }

    private fun saveDate(date: String) {

        lifecycleScope.launch {

            val outfit = outfitDao.getById(outfitId) ?: return@launch

            outfitDao.update(outfit.copy(plannedDate = date))

            bindSchedule(date)
        }
    }

    private fun saveTags(value: String?) {

        val normalized = value
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.joinToString(", ")
            ?.takeIf { it.isNotBlank() }

        lifecycleScope.launch {

            val outfit = outfitDao.getById(outfitId) ?: return@launch

            outfitDao.update(outfit.copy(tags = normalized))

            bindTags(normalized)
        }
    }

    private fun saveOccasion(occasion: String) {

        val normalized = occasion.takeIf { it.isNotBlank() }

        lifecycleScope.launch {

            val outfit = outfitDao.getById(outfitId) ?: return@launch

            outfitDao.update(outfit.copy(occasion = normalized))

            findViewById<TextView>(R.id.tvOccasion).text =
                normalized ?: getString(R.string.add_occasion)
        }
    }

    private fun String.toEditable() = android.text.Editable.Factory.getInstance().newEditable(this)

    private fun confirmDelete() {

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_outfit_title)
            .setMessage(R.string.delete_outfit_message)
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.delete) { _, _ -> deleteOutfit() }
            .show()
    }

    private fun deleteOutfit() {

        lifecycleScope.launch {

            val outfit = outfitDao.getById(outfitId) ?: return@launch

            outfitDao.deleteOutfitItems(outfitId)
            outfitDao.delete(outfit)

            Toast.makeText(
                this@OutfitDetailActivity,
                R.string.outfit_deleted,
                Toast.LENGTH_SHORT
            ).show()

            finish()
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

    private fun promptCaption() {

        val input = com.google.android.material.textfield.TextInputEditText(this)

        input.hint = getString(R.string.hint_caption)
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

    private var currentCaption: String = ""

    private fun saveCaption(value: String?) {

        val normalized = value?.trim()?.takeIf { it.isNotBlank() }

        lifecycleScope.launch {

            val outfit = outfitDao.getById(outfitId) ?: return@launch

            outfitDao.update(outfit.copy(caption = normalized))

            bindCaption(normalized)
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

    private fun bindFields(outfit: Outfit) {

        bindCaption(outfit.caption)

        findViewById<TextView>(R.id.tvTags).text =
            outfit.tags?.takeIf { it.isNotBlank() }
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.joinToString(" · ")
                ?: getString(R.string.add_tags)

        bindTags(outfit.tags)

        findViewById<TextView>(R.id.tvOccasion).text =
            outfit.occasion?.takeIf { it.isNotBlank() }
                ?: getString(R.string.add_occasion)

        bindSchedule(outfit.plannedDate?.takeIf { it.isNotBlank() })
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

    private fun bindSchedule(plannedDate: String?) {

        val tile = findViewById<View>(R.id.tileDate)
        val empty = findViewById<TextView>(R.id.tvScheduleEmpty)

        if (plannedDate == null) {

            tile.visibility = View.GONE
            empty.visibility = View.VISIBLE
            return
        }

        val date = runCatching { LocalDate.parse(plannedDate) }.getOrNull()

        if (date == null) {

            tile.visibility = View.GONE
            empty.visibility = View.VISIBLE
            return
        }

        empty.visibility = View.GONE
        tile.visibility = View.VISIBLE

        findViewById<TextView>(R.id.tvScheduleDay).text =
            date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH))

        findViewById<TextView>(R.id.tvScheduleNumber).text =
            date.dayOfMonth.toString()

        findViewById<TextView>(R.id.tvScheduleMonth).text =
            date.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH))
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
                categories.sorted().joinToString(" · ")
    }
}
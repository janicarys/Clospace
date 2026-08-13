package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
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
import com.mobdeve.s15.reyes.janicamegan.clospace.util.ImageDecoder
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitPreviewCache
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class OutfitDetailActivity : AppCompatActivity() {
    companion object { const val EXTRA_OUTFIT_ID = "outfitId" }

    private lateinit var backend: BackendRepository
    private var outfitId = 0
    private var currentTags = emptyList<String>()
    private var currentCaption = ""
    private var currentOccasions = emptyList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outfit_detail)
        backend = BackendRepository(this)
        outfitId = intent.getIntExtra(EXTRA_OUTFIT_ID, 0)
        findViewById<TextView>(R.id.tvToolbarTitle).text = getString(R.string.outfit_detail_title)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnEdit).setOnClickListener { openEditor() }
        findViewById<View>(R.id.tvCaption).setOnClickListener { promptCaption() }
        findViewById<View>(R.id.rowTags).setOnClickListener { promptTags() }
        findViewById<View>(R.id.rowOccasion).setOnClickListener { promptOccasion() }
        findViewById<View>(R.id.rowSchedule).setOnClickListener { promptSchedule() }
        findViewById<View>(R.id.btnDelete).setOnClickListener { confirmDelete() }
    }

    private fun openEditor() = startActivity(Intent(this, OutfitCanvasActivity::class.java).putExtra(OutfitCanvasActivity.EXTRA_OUTFIT_ID, outfitId))

    private fun promptTags() {
        TagPickerDialog.show(this, backend, currentTags) { names ->
            val outfit = backend.getOutfitById(outfitId)?.outfit ?: return@show
            backend.updateOutfit(outfitId, outfit.caption, outfit.occasion, outfit.plannedDate, names.joinToString(", "))
            bindTags(names.joinToString(", "))
        }
    }

    private fun promptOccasion() {
        val occasions = resources.getStringArray(R.array.occasions)
        val current = currentOccasions
        val selected = current.mapNotNull { occasion ->
            occasions.indexOfFirst { it.equals(occasion, ignoreCase = true) }.takeIf { it >= 0 }
        }.toSet()
        ClospaceBottomSheets.showMultiChoice(
            this,
            R.string.occasion,
            occasions,
            selected
        ) { indices -> saveOccasion(indices.sorted().map { occasions[it] }) }
    }

    private fun promptSchedule() {
        MaterialDatePicker.Builder.datePicker().setTitleText(R.string.pick_date).build().also { picker ->
            picker.addOnPositiveButtonClickListener { selection ->
                saveDate(Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate().toString())
            }
            picker.show(supportFragmentManager, "date_picker")
        }
    }

    private fun saveDate(date: String) {
        lifecycleScope.launch {
            runCatching {
                val outfit = backend.getOutfitById(outfitId)?.outfit ?: return@runCatching
                backend.updateOutfit(outfitId, outfit.caption, outfit.occasion, date, outfit.tags)
            }.onSuccess { bindSchedule(date) }
                .onFailure { Toast.makeText(this@OutfitDetailActivity, it.message ?: "Unable to schedule outfit", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun saveOccasion(occasions: List<String>) {
        val joined = occasions.joinToString(", ")
        lifecycleScope.launch {
            runCatching {
                val outfit = backend.getOutfitById(outfitId)?.outfit ?: return@runCatching
                backend.updateOutfit(outfitId, outfit.caption, joined, outfit.plannedDate, outfit.tags)
            }.onSuccess { bindOccasion(joined) }
        }
    }

    private fun promptCaption() {
        ClospaceBottomSheets.showInput(
            this,
            R.string.caption,
            getString(R.string.hint_caption),
            currentCaption
        ) { value -> saveCaption(value) }
    }

    private fun saveCaption(value: String?) {
        val normalized = value?.trim()?.takeIf { it.isNotBlank() }
        lifecycleScope.launch {
            runCatching {
                val outfit = backend.getOutfitById(outfitId)?.outfit ?: return@runCatching
                backend.updateOutfit(outfitId, normalized, outfit.occasion, outfit.plannedDate, outfit.tags)
            }.onSuccess { bindCaption(normalized) }
        }
    }

    private fun confirmDelete() {
        ClospaceBottomSheets.showConfirm(this, R.string.delete_outfit_title, R.string.delete_outfit_message, R.string.delete) { deleteOutfit() }
    }

    private fun deleteOutfit() {
        lifecycleScope.launch {
            runCatching { backend.deleteOutfit(outfitId) }.onSuccess {
                OutfitPreviewCache.evict(outfitId)
                Toast.makeText(this@OutfitDetailActivity, R.string.outfit_deleted, Toast.LENGTH_SHORT).show(); finish()
            }.onFailure { Toast.makeText(this@OutfitDetailActivity, it.message ?: "Unable to delete outfit", Toast.LENGTH_LONG).show() }
        }
    }

    override fun onResume() { super.onResume(); if (::backend.isInitialized) loadDetail() }

    private fun loadDetail() {
        lifecycleScope.launch {
            val wrapper = runCatching { backend.getOutfitById(outfitId) }.getOrNull() ?: return@launch
            val outfit = wrapper.outfit
            bindFields(outfit)
            bindClothes(wrapper.placements.map { it.item })
            val preview = withContext(Dispatchers.Default) { OutfitRenderer.render(wrapper.placements, 720, 960) }
            if (preview != null) findViewById<ImageView>(R.id.imgOutfitPreview).setImageBitmap(preview)
        }
    }

    private fun bindCaption(caption: String?) {
        currentCaption = caption.orEmpty()
        val tv = findViewById<TextView>(R.id.tvCaption)
        tv.text = currentCaption.ifEmpty { getString(R.string.add_caption) }
        tv.setTextColor(resources.getColor(if (currentCaption.isEmpty()) R.color.brown else R.color.purple, null))
    }

    private fun bindFields(outfit: Outfit) {
        bindCaption(outfit.caption)
        bindTags(outfit.tags)
        bindOccasion(outfit.occasion)
        bindSchedule(outfit.plannedDate?.takeIf { it.isNotBlank() })
    }

    private fun bindOccasion(rawOccasion: String?) {
        currentOccasions = rawOccasion?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        findViewById<TextView>(R.id.tvOccasion).text =
            if (currentOccasions.isEmpty()) getString(R.string.add_occasion) else currentOccasions.joinToString(" · ")
    }

    private fun bindTags(rawTags: String?) {
        currentTags = rawTags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        findViewById<TextView>(R.id.tvTags).text = if (currentTags.isEmpty()) getString(R.string.add_tags) else currentTags.joinToString(" · ")
    }

    private fun bindSchedule(plannedDate: String?) {
        val tile = findViewById<View>(R.id.tileDate)
        val empty = findViewById<TextView>(R.id.tvScheduleEmpty)
        val date = plannedDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (date == null) { tile.visibility = View.GONE; empty.visibility = View.VISIBLE; return }
        empty.visibility = View.GONE; tile.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvScheduleDay).text = date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH))
        findViewById<TextView>(R.id.tvScheduleNumber).text = date.dayOfMonth.toString()
        findViewById<TextView>(R.id.tvScheduleMonth).text = date.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH))
    }

    private fun bindClothes(clothes: List<ClothingItem>) {
        val container = findViewById<LinearLayout>(R.id.containerClothes)
        container.removeAllViews()
        val categories = mutableSetOf<String>()
        for (item in clothes) {
            if (item.category.isNotBlank()) categories += item.category
            val card = layoutInflater.inflate(R.layout.item_included_clothing, container, false)
            val image = card.findViewById<ImageView>(R.id.imgClothing)
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = ImageDecoder.decode(item.imagePath, 220)
                withContext(Dispatchers.Main) { image.setImageBitmap(bitmap) }
            }
            card.findViewById<TextView>(R.id.tvClothingCategory).text = item.category
            card.setOnClickListener {
                startActivity(
                    Intent(this, GarmentDetailActivity::class.java)
                        .putExtra(GarmentDetailActivity.EXTRA_CLOTHING_ID, item.id)
                )
            }
            container.addView(card)
        }
        findViewById<TextView>(R.id.tvCategory).text = if (categories.isEmpty()) getString(R.string.no_category) else categories.sorted().joinToString(" · ")
    }
}

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

class OutfitDetailsActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_CLOTHING_IDS = "clothingIds"
        const val EXTRA_X = "xPositions"
        const val EXTRA_Y = "yPositions"
        const val EXTRA_SCALE = "scales"
        const val EXTRA_LAYER = "layers"
        const val EXTRA_OUTFIT_ID = "outfitId"
        const val EXTRA_DATE = "selectedDate"
    }

    private lateinit var backend: BackendRepository
    private var editOutfitId = 0
    private var clothingIds = intArrayOf()
    private var xPositions = floatArrayOf()
    private var yPositions = floatArrayOf()
    private var scales = floatArrayOf()
    private var layers = intArrayOf()
    private var selectedDate: String? = null
    private var selectedOccasion: String? = null
    private var currentCaption = ""
    private var currentTags: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outfit_details)
        backend = BackendRepository(this)
        findViewById<TextView>(R.id.tvToolbarTitle).text = getString(R.string.outfit_details_title)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        clothingIds = intent.getIntArrayExtra(EXTRA_CLOTHING_IDS) ?: intArrayOf()
        xPositions = intent.getFloatArrayExtra(EXTRA_X) ?: FloatArray(clothingIds.size) { .5f }
        yPositions = intent.getFloatArrayExtra(EXTRA_Y) ?: FloatArray(clothingIds.size) { .5f }
        scales = intent.getFloatArrayExtra(EXTRA_SCALE) ?: FloatArray(clothingIds.size) { 1f }
        layers = intent.getIntArrayExtra(EXTRA_LAYER) ?: IntArray(clothingIds.size) { it }
        editOutfitId = intent.getIntExtra(EXTRA_OUTFIT_ID, 0)
        selectedDate = intent.getStringExtra(EXTRA_DATE)

        findViewById<View>(R.id.tvCaption).setOnClickListener { promptCaption() }
        findViewById<View>(R.id.rowTags).setOnClickListener { promptTags() }
        findViewById<View>(R.id.rowOccasion).setOnClickListener { promptOccasion() }
        findViewById<View>(R.id.rowSchedule).setOnClickListener { promptSchedule() }
        findViewById<View>(R.id.btnSaveOutfit).setOnClickListener { saveOutfit() }

        loadDetail()
    }

    private fun loadDetail() {
        lifecycleScope.launch {
            if (editOutfitId > 0) {
                val outfit = backend.getOutfitById(editOutfitId)?.outfit
                if (outfit != null) {
                    bindCaption(outfit.caption)
                    bindTags(outfit.tags)
                    selectedOccasion = outfit.occasion?.takeIf { it.isNotBlank() }
                    outfit.plannedDate?.takeIf { it.isNotBlank() }?.let { selectedDate = it }
                }
            }
            bindCaption(currentCaption)
            bindOccasion(selectedOccasion)
            bindSchedule(selectedDate?.takeIf { it.isNotBlank() })
            loadPreview()
            bindClothes()
        }
    }

    private suspend fun loadPreview() {
        val placements = buildPlacements()
        val bitmap = withContext(Dispatchers.Default) { placements?.let { OutfitRenderer.render(it, 330, 480) } }
        if (bitmap != null) findViewById<ImageView>(R.id.imgOutfitPreview).setImageBitmap(bitmap)
    }

    private suspend fun buildPlacements(): List<OutfitPlacement>? {
        val result = mutableListOf<OutfitPlacement>()
        clothingIds.forEachIndexed { index, id ->
            backend.getClothingById(id)?.let { item ->
                result += OutfitPlacement(
                    item = item,
                    x = xPositions.getOrElse(index) { .5f },
                    y = yPositions.getOrElse(index) { .5f },
                    scale = scales.getOrElse(index) { 1f },
                    layer = layers.getOrElse(index) { index }
                )
            }
        }
        return result.ifEmpty { null }
    }

    private suspend fun bindClothes() {
        val container = findViewById<LinearLayout>(R.id.containerClothes)
        container.removeAllViews()
        val categories = mutableSetOf<String>()
        for (id in clothingIds) {
            val item = backend.getClothingById(id) ?: continue
            if (item.category.isNotBlank()) categories += item.category
            val card = layoutInflater.inflate(R.layout.item_included_clothing, container, false)
            val image = card.findViewById<ImageView>(R.id.imgClothing)
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = ImageDecoder.decode(item.imagePath, 120)
                withContext(Dispatchers.Main) { image.setImageBitmap(bitmap) }
            }
            card.findViewById<TextView>(R.id.tvClothingCategory).text = item.category
            container.addView(card)
        }
        findViewById<TextView>(R.id.tvCategory).text =
            if (categories.isEmpty()) getString(R.string.no_category) else categories.sorted().joinToString(" · ")
    }

    private fun bindCaption(caption: String?) {
        currentCaption = caption?.trim().orEmpty()
        val tv = findViewById<TextView>(R.id.tvCaption)
        tv.text = currentCaption.ifEmpty { getString(R.string.add_caption) }
        tv.setTextColor(resources.getColor(if (currentCaption.isEmpty()) R.color.brown else R.color.purple, null))
    }

    private fun promptCaption() {
        ClospaceBottomSheets.showInput(
            this,
            R.string.caption,
            getString(R.string.hint_caption),
            currentCaption
        ) { value -> bindCaption(value) }
    }

    private fun bindTags(rawTags: String?) {
        currentTags = rawTags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        findViewById<TextView>(R.id.tvTags).text =
            if (currentTags.isEmpty()) getString(R.string.add_tags) else currentTags.joinToString(" · ")
    }

    private fun promptTags() {
        TagPickerDialog.show(this, backend, currentTags) { names ->
            currentTags = names
            findViewById<TextView>(R.id.tvTags).text =
                if (names.isEmpty()) getString(R.string.add_tags) else names.joinToString(" · ")
        }
    }

    private fun bindOccasion(occasion: String?) {
        selectedOccasion = occasion?.takeIf { it.isNotBlank() }
        findViewById<TextView>(R.id.tvOccasion).text = selectedOccasion ?: getString(R.string.add_occasion)
    }

    private fun promptOccasion() {
        val occasions = resources.getStringArray(R.array.occasions)
        ClospaceBottomSheets.showChoice(
            this,
            R.string.occasion,
            occasions,
            selectedIndex = occasions.indexOf(selectedOccasion.orEmpty())
        ) { which -> bindOccasion(occasions[which]) }
    }

    private fun promptSchedule() {
        MaterialDatePicker.Builder.datePicker().setTitleText(R.string.pick_date).build().also { picker ->
            picker.addOnPositiveButtonClickListener { selection ->
                selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                bindSchedule(selectedDate)
            }
            picker.show(supportFragmentManager, "date_picker")
        }
    }

    private fun bindSchedule(plannedDate: String?) {
        val tile = findViewById<View>(R.id.tileDate)
        val empty = findViewById<TextView>(R.id.tvScheduleEmpty)
        val date = plannedDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (date == null) {
            tile.visibility = View.GONE
            empty.visibility = View.VISIBLE
            return
        }
        empty.visibility = View.GONE
        tile.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvScheduleDay).text = date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH))
        findViewById<TextView>(R.id.tvScheduleNumber).text = date.dayOfMonth.toString()
        findViewById<TextView>(R.id.tvScheduleMonth).text = date.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH))
    }

    private fun saveOutfit() {
        val placements = clothingIds.mapIndexed { index, clothingId ->
            OutfitItem(
                outfitId = editOutfitId,
                clothingId = clothingId,
                x = xPositions.getOrElse(index) { .5f },
                y = yPositions.getOrElse(index) { .5f },
                scale = scales.getOrElse(index) { 1f },
                layer = layers.getOrElse(index) { index }
            )
        }

        lifecycleScope.launch {
            runCatching {
                val savedId = if (editOutfitId > 0) {
                    val existing = backend.getOutfitById(editOutfitId)?.outfit
                        ?: throw IllegalArgumentException("Outfit not found")
                    backend.updateOutfit(
                        id = editOutfitId,
                        caption = currentCaption.ifBlank { null },
                        occasion = selectedOccasion ?: existing.occasion,
                        selectedDate = selectedDate ?: existing.plannedDate,
                        tags = currentTags.joinToString(", "),
                        placements = placements
                    )
                    editOutfitId
                } else {
                    backend.createOutfit(
                        currentCaption.ifBlank { null },
                        selectedOccasion,
                        selectedDate,
                        currentTags.joinToString(", "),
                        placements
                    )
                }
                OutfitPreviewCache.evict(savedId)
            }.onSuccess {
                Toast.makeText(this@OutfitDetailsActivity, if (editOutfitId > 0) R.string.outfit_updated else R.string.outfit_saved, Toast.LENGTH_SHORT).show()
                val main = Intent(this@OutfitDetailsActivity, MainActivity::class.java)
                    .putExtra(
                        MainActivity.EXTRA_OPEN_TAB,
                        if (selectedDate.isNullOrBlank()) MainActivity.TAB_OUTFIT else MainActivity.TAB_CALENDAR
                    )
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                if (!selectedDate.isNullOrBlank()) main.putExtra(MainActivity.EXTRA_OPEN_DATE, selectedDate)
                startActivity(main)
                finish()
            }.onFailure {
                Toast.makeText(this@OutfitDetailsActivity, it.message ?: "Unable to save outfit", Toast.LENGTH_LONG).show()
            }
        }
    }
}
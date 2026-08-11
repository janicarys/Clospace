package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitRenderer
import com.mobdeve.s15.reyes.janicamegan.clospace.util.OutfitPreviewCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

class OutfitDetailsActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_CLOTHING_IDS = "clothingIds"
        const val EXTRA_X = "xPositions"
        const val EXTRA_Y = "yPositions"
        const val EXTRA_SCALE = "scales"
        const val EXTRA_LAYER = "layers"
        const val EXTRA_OUTFIT_ID = "outfitId"
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
    private lateinit var etCaption: TextInputEditText
    private lateinit var etTags: TextInputEditText
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvOccasion: TextView
    private lateinit var imgPreview: ImageView

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

        etCaption = findViewById(R.id.etCaption)
        etTags = findViewById(R.id.etTags)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvOccasion = findViewById(R.id.tvOccasion)
        imgPreview = findViewById(R.id.imgOutfitPreview)

        findViewById<android.view.View>(R.id.layoutDate).setOnClickListener { pickDate() }
        findViewById<android.view.View>(R.id.layoutOccasion).setOnClickListener { pickOccasion() }
        findViewById<android.view.View>(R.id.btnSaveOutfit).setOnClickListener { saveOutfit() }
        loadPreview()
    }

    private fun loadPreview() {
        lifecycleScope.launch {
            val placements = buildPlacements()
            val bitmap = withContext(Dispatchers.Default) { placements?.let { OutfitRenderer.render(it, 330, 450) } }
            if (bitmap != null) imgPreview.setImageBitmap(bitmap)
        }
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

    private fun pickDate() {
        MaterialDatePicker.Builder.datePicker().setTitleText(R.string.pick_date).build().also { picker ->
            picker.addOnPositiveButtonClickListener { selection ->
                selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                tvSelectedDate.text = selectedDate
            }
            picker.show(supportFragmentManager, "date_picker")
        }
    }

    private fun pickOccasion() {
        val occasions = resources.getStringArray(R.array.occasions)
        MaterialAlertDialogBuilder(this).setTitle(R.string.occasion).setItems(occasions) { _, which ->
            selectedOccasion = occasions[which]
            tvOccasion.text = selectedOccasion
        }.show()
    }

    private fun saveOutfit() {
        val caption = etCaption.text?.toString()?.trim()?.ifBlank { null }
        val tags = etTags.text?.toString()?.trim()?.ifBlank { null }
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
                        caption = caption,
                        occasion = selectedOccasion ?: existing.occasion,
                        selectedDate = selectedDate ?: existing.plannedDate,
                        tags = tags,
                        placements = placements
                    )
                    editOutfitId
                } else {
                    backend.createOutfit(caption, selectedOccasion, selectedDate, tags, placements)
                }
                OutfitPreviewCache.evict(savedId)
            }.onSuccess {
                Toast.makeText(this@OutfitDetailsActivity, if (editOutfitId > 0) R.string.outfit_updated else R.string.outfit_saved, Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(this@OutfitDetailsActivity, MainActivity::class.java)
                        .putExtra(MainActivity.EXTRA_OPEN_TAB, MainActivity.TAB_OUTFIT)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
                finish()
            }.onFailure {
                Toast.makeText(this@OutfitDetailsActivity, it.message ?: "Unable to save outfit", Toast.LENGTH_LONG).show()
            }
        }
    }
}

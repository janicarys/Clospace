package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.graphics.Bitmap
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

    private lateinit var sessionManager: SessionManager

    private lateinit var outfitDao: OutfitDao

    private lateinit var clothingDao: ClothingDao

    private var editOutfitId: Int = 0

    private var clothingIds: IntArray = intArrayOf()

    private var xPositions: FloatArray = floatArrayOf()

    private var yPositions: FloatArray = floatArrayOf()

    private var scales: FloatArray = floatArrayOf()

    private var layers: IntArray = intArrayOf()

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

        findViewById<TextView>(R.id.tvToolbarTitle).text =
            getString(R.string.outfit_details_title)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        sessionManager = SessionManager(this)

        val database = ClospaceDatabase.getDatabase(this)
        outfitDao = database.outfitDao()
        clothingDao = database.clothingDao()

        clothingIds = intent.getIntArrayExtra(EXTRA_CLOTHING_IDS) ?: intArrayOf()

        xPositions = intent.getFloatArrayExtra(EXTRA_X)
            ?: FloatArray(clothingIds.size) { 0.5f }

        yPositions = intent.getFloatArrayExtra(EXTRA_Y)
            ?: FloatArray(clothingIds.size) { 0.5f }

        scales = intent.getFloatArrayExtra(EXTRA_SCALE)
            ?: FloatArray(clothingIds.size) { 1f }

        layers = intent.getIntArrayExtra(EXTRA_LAYER)
            ?: IntArray(clothingIds.size) { it }

        editOutfitId = intent.getIntExtra(EXTRA_OUTFIT_ID, 0)

        etCaption = findViewById(R.id.etCaption)
        etTags = findViewById(R.id.etTags)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvOccasion = findViewById(R.id.tvOccasion)
        imgPreview = findViewById(R.id.imgOutfitPreview)

        findViewById<android.view.View>(R.id.layoutDate).setOnClickListener {
            pickDate()
        }

        findViewById<android.view.View>(R.id.layoutOccasion).setOnClickListener {
            pickOccasion()
        }

        findViewById<android.view.View>(R.id.btnSaveOutfit).setOnClickListener {
            saveOutfit()
        }

        loadPreview()
    }

    // Render the arranged garment preview into the card at the top.
    private fun loadPreview() {

        lifecycleScope.launch {

            val bitmap = withContext(Dispatchers.Default) {

                buildPlacements()?.let { placements ->
                    OutfitRenderer.render(placements, 330, 450)
                }
            }

            if (bitmap != null) {
                imgPreview.setImageBitmap(bitmap)
            }
        }
    }

    private suspend fun buildPlacements(): List<OutfitPlacement>? {

        val placements = mutableListOf<OutfitPlacement>()

        for (index in clothingIds.indices) {

            val clothingId = clothingIds[index]

            val item = clothingDao.getById(clothingId) ?: continue

            placements.add(
                OutfitPlacement(
                    item = item,
                    x = xPositions.getOrElse(index) { 0.5f },
                    y = yPositions.getOrElse(index) { 0.5f },
                    scale = scales.getOrElse(index) { 1f },
                    layer = layers.getOrElse(index) { index }
                )
            )
        }

        return placements.ifEmpty { null }
    }

    private fun pickDate() {

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.pick_date)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->

            val date = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString()

            selectedDate = date
            tvSelectedDate.text = date
        }

        picker.show(supportFragmentManager, "date_picker")
    }

    private fun pickOccasion() {

        val occasions = resources.getStringArray(R.array.occasions)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.occasion)
            .setItems(occasions) { _, which ->

                selectedOccasion = occasions[which]
                tvOccasion.text = occasions[which]
            }
            .show()
    }

    private fun saveOutfit() {

        val caption = etCaption.text?.toString()?.trim()?.ifBlank { null }

        val occasion = selectedOccasion

        val tags = etTags.text?.toString()?.trim()?.ifBlank { null }

        val ownerId = sessionManager.getUserId()

        if (ownerId == -1) {
            return
        }

        lifecycleScope.launch {

            val items = clothingIds.mapIndexed { index, clothingId ->

                OutfitItem(
                    outfitId = if (editOutfitId > 0) editOutfitId else 0,
                    clothingId = clothingId,
                    x = xPositions.getOrElse(index) { 0.5f },
                    y = yPositions.getOrElse(index) { 0.5f },
                    scale = scales.getOrElse(index) { 1f },
                    layer = layers.getOrElse(index) { index }
                )
            }

            val outfitId: Long

            if (editOutfitId > 0) {

                val existing = outfitDao.getById(editOutfitId) ?: return@launch

                outfitDao.update(
                    existing.copy(
                        caption = caption,
                        occasion = occasion,
                        tags = tags,
                        plannedDate = selectedDate
                    )
                )

                outfitId = editOutfitId.toLong()

                outfitDao.deleteOutfitItems(editOutfitId)

            } else {

                outfitId = outfitDao.insert(
                    Outfit(
                        ownerId = ownerId,
                        caption = caption,
                        occasion = occasion,
                        tags = tags,
                        plannedDate = selectedDate
                    )
                )
            }

            outfitDao.insertOutfitItems(
                items.map { it.copy(outfitId = outfitId.toInt()) }
            )

            Toast.makeText(
                this@OutfitDetailsActivity,
                if (editOutfitId > 0)
                    R.string.outfit_updated
                else
                    R.string.outfit_saved,
                Toast.LENGTH_SHORT
            ).show()

            // Go straight back to the outfit list, clearing the
            // selection / canvas screens so it reloads on resume.
            startActivity(
                Intent(this@OutfitDetailsActivity, OutfitActivity::class.java)
                    .addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
            )

            finish()
        }
    }
}
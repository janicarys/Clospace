package com.mobdeve.s15.reyes.janicamegan.clospace

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText

import kotlinx.coroutines.launch

import java.time.Instant
import java.time.ZoneId

class OutfitDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLOTHING_IDS = "clothingIds"
    }

    private lateinit var sessionManager: SessionManager

    private lateinit var outfitDao: OutfitDao

    private var clothingIds: IntArray = intArrayOf()

    private var selectedDate: String? = null

    private lateinit var etCaption: TextInputEditText

    private lateinit var actOccasion: AutoCompleteTextView

    private lateinit var btnDate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outfit_details)

        findViewById<TextView>(R.id.tvToolbarTitle).text =
            getString(R.string.outfit_details_title)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        sessionManager = SessionManager(this)

        outfitDao = ClospaceDatabase.getDatabase(this).outfitDao()

        clothingIds = intent.getIntArrayExtra(EXTRA_CLOTHING_IDS) ?: intArrayOf()

        etCaption = findViewById(R.id.etCaption)

        actOccasion = findViewById(R.id.actOccasion)

        btnDate = findViewById(R.id.btnDate)

        actOccasion.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.occasions)
            )
        )

        btnDate.setOnClickListener {

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.pick_date)
                .build()

            picker.addOnPositiveButtonClickListener { selection ->

                val date = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toString()

                selectedDate = date

                btnDate.text = getString(R.string.planned_date_set, date)
            }

            picker.show(supportFragmentManager, "date_picker")
        }

        findViewById<View>(R.id.btnSaveOutfit).setOnClickListener {
            saveOutfit()
        }
    }

    private fun saveOutfit() {

        val caption = etCaption.text?.toString()?.trim().orEmpty()

        val occasion = actOccasion.text?.toString()?.trim().orEmpty()

        if (caption.isEmpty()) {

            Toast.makeText(
                this,
                R.string.caption_required,
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (occasion.isEmpty()) {

            Toast.makeText(
                this,
                R.string.occasion_required,
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val ownerId = sessionManager.getUserId()

        if (ownerId == -1) {
            return
        }

        lifecycleScope.launch {

            val outfitId = outfitDao.insert(
                Outfit(
                    ownerId = ownerId,
                    caption = caption,
                    occasion = occasion,
                    plannedDate = selectedDate
                )
            )

            outfitDao.insertOutfitItems(
                clothingIds.map { clothingId ->
                    OutfitItem(outfitId.toInt(), clothingId)
                }
            )

            Toast.makeText(
                this@OutfitDetailsActivity,
                R.string.outfit_saved,
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}

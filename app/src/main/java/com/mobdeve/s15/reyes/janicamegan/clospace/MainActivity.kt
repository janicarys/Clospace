package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.mobdeve.s15.reyes.janicamegan.clospace.util.TransitionUtil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private lateinit var clothingDao: ClothingDao

    private var pendingImagePath: String? = null

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val path = pendingImagePath
            pendingImagePath = null
            if (success && path != null) {
                promptCategory(path)
            }
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val path = withContext(Dispatchers.IO) { copyUriToStorage(uri) }
                    if (path != null) {
                        promptCategory(path)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.image_load_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        clothingDao = ClospaceDatabase.getDatabase(this).clothingDao()

        // Bottom Navigation
        val navCloset = findViewById<LinearLayout>(R.id.navCloset)
        val navOutfit = findViewById<LinearLayout>(R.id.navOutfit)
        val navCalendar = findViewById<LinearLayout>(R.id.navCalendar)
        val navSettings = findViewById<LinearLayout>(R.id.navSettings)

        // Floating Action Button
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        // Closet
        // Already on this page
        navCloset.setOnClickListener {
            // Do nothing
        }

        // Outfit
        navOutfit.setOnClickListener {
            val (enter, exit) = TransitionUtil.slide(
                TransitionUtil.TAB_CLOSET,
                TransitionUtil.TAB_OUTFIT
            )
            startActivity(Intent(this, OutfitActivity::class.java))
            overridePendingTransition(enter, exit)
            finish()
            overridePendingTransition(enter, exit)
        }

        // Calendar
        navCalendar.setOnClickListener {
            val (enter, exit) = TransitionUtil.slide(
                TransitionUtil.TAB_CLOSET,
                TransitionUtil.TAB_CALENDAR
            )
            startActivity(Intent(this, CalendarActivity::class.java))
            overridePendingTransition(enter, exit)
            finish()
            overridePendingTransition(enter, exit)
        }

        // Settings
        navSettings.setOnClickListener {
            val (enter, exit) = TransitionUtil.slide(
                TransitionUtil.TAB_CLOSET,
                TransitionUtil.TAB_SETTINGS
            )
            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(enter, exit)
            finish()
            overridePendingTransition(enter, exit)
        }

        // Floating Action Button
        fabAdd.setOnClickListener {
            showSourceDialog()
        }

        // Load existing garments
        loadCloset()
    }

    // Ask whether the garment comes from the camera or the library
    private fun showSourceDialog() {

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_source, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .show()

        dialogView.findViewById<View>(R.id.optionCamera).setOnClickListener {
            dialog.dismiss()
            launchCamera()
        }

        dialogView.findViewById<View>(R.id.optionGallery).setOnClickListener {
            dialog.dismiss()
            launchGallery()
        }
    }

    // Launch the camera and save the photo into app storage
    private fun launchCamera() {

        val timestamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val dir = File(filesDir, "images")
        dir.mkdirs()

        val file = File(dir, "captured_$timestamp.jpg")

        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            file
        )

        pendingImagePath = file.absolutePath

        takePicture.launch(uri)
    }

    // Open the gallery / photo picker
    private fun launchGallery() {

        pickImage.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // Copy the picked image into app storage and return its file path
    private fun copyUriToStorage(uri: Uri): String? {

        return try {

            val timestamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val dir = File(filesDir, "images")
            dir.mkdirs()

            val file = File(dir, "picked_$timestamp.jpg")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath

        } catch (e: Exception) {

            null
        }
    }

    // Ask which category the new garment belongs to
    private fun promptCategory(imagePath: String) {

        val categories = arrayOf(
            getString(R.string.category_tops),
            getString(R.string.category_bottoms),
            getString(R.string.category_footwear),
            getString(R.string.category_accessories)
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.select_category)
            .setItems(categories) { _, which ->
                saveGarment(imagePath, categories[which])
            }
            .show()
    }

    // Save the garment into the closet and refresh the page
    private fun saveGarment(imagePath: String, category: String) {

        val ownerId = sessionManager.getUserId()

        if (ownerId == -1) {
            return
        }

        lifecycleScope.launch {

            clothingDao.insert(
                ClothingItem(
                    ownerId = ownerId,
                    name = "",
                    category = category,
                    color = "",
                    tags = "",
                    imagePath = imagePath
                )
            )

            loadCloset()
        }
    }

    // Populate every category section with its garments
    private fun loadCloset() {

        val ownerId = sessionManager.getUserId()

        if (ownerId == -1) {
            return
        }

        lifecycleScope.launch {

            val items = clothingDao.getAll(ownerId)

            renderCategory(
                items,
                getString(R.string.category_tops),
                R.id.horizontalTops,
                R.id.placeholderTops
            )

            renderCategory(
                items,
                getString(R.string.category_bottoms),
                R.id.horizontalBottoms,
                R.id.placeholderBottoms
            )

            renderCategory(
                items,
                getString(R.string.category_footwear),
                R.id.horizontalFootwear,
                R.id.placeholderFootwear
            )

            renderCategory(
                items,
                getString(R.string.category_accessories),
                R.id.horizontalAccessories,
                R.id.placeholderAccessories
            )
        }
    }

    // Render one category's garments after its "Add" placeholder card
    private fun renderCategory(
        items: List<ClothingItem>,
        category: String,
        containerId: Int,
        placeholderId: Int
    ) {

        val container = findViewById<LinearLayout>(containerId)
        val placeholder = container.findViewById<View>(placeholderId)

        // Remove old garment cards, keeping only the placeholder
        for (i in container.childCount - 1 downTo 0) {
            if (container.getChildAt(i) !== placeholder) {
                container.removeViewAt(i)
            }
        }

        val categoryItems = items.filter { it.category.equals(category, ignoreCase = true) }

        for (item in categoryItems) {

            val card = layoutInflater.inflate(R.layout.item_garment, container, false)

            container.addView(card, container.indexOfChild(placeholder) + 1)

            val image = card.findViewById<ImageView>(R.id.imgGarment)

            lifecycleScope.launch {
                val bitmap =
                    withContext(Dispatchers.IO) { decodeSampledBitmap(item.imagePath) }
                image.setImageBitmap(bitmap)
            }
        }
    }

    // Decode a downsampled bitmap so it fits in the small card
    private fun decodeSampledBitmap(path: String): Bitmap? {

        return try {

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)

            var sampleSize = 1
            val targetSize = 220

            while (
                bounds.outWidth / sampleSize > targetSize ||
                bounds.outHeight / sampleSize > targetSize
            ) {
                sampleSize *= 2
            }

            BitmapFactory.decodeFile(
                path,
                BitmapFactory.Options().apply { inSampleSize = sampleSize }
            )

        } catch (e: Exception) {

            null
        }
    }
}

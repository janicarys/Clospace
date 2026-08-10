package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.AlertDialog

import com.mobdeve.s15.reyes.janicamegan.clospace.util.GarmentCutout
import com.mobdeve.s15.reyes.janicamegan.clospace.util.ImageDecoder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClosetFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    private lateinit var clothingDao: ClothingDao

    private var pendingImagePath: String? = null

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val path = pendingImagePath
            pendingImagePath = null
            if (success && path != null) {
                removeBackground(path)
            }
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val path = withContext(Dispatchers.IO) { copyUriToStorage(uri) }
                    if (path != null) {
                        removeBackground(path)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.image_load_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_closet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        clothingDao = ClospaceDatabase.getDatabase(requireContext()).clothingDao()

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            showSourceDialog()
        }

        loadCloset()
    }

    override fun onResume() {
        super.onResume()
        loadCloset()
    }

    // Ask whether the garment comes from the camera or the library
    private fun showSourceDialog() {

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_source, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
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

        val dir = File(requireContext().filesDir, "images")
        dir.mkdirs()

        val file = File(dir, "captured_$timestamp.jpg")

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
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

            val dir = File(requireContext().filesDir, "images")
            dir.mkdirs()

            val file = File(dir, "picked_$timestamp.jpg")

            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath

        } catch (e: Exception) {

            null
        }
    }

    // Remove the image background, then ask which category the garment belongs to
    private fun removeBackground(imagePath: String) {

        val progress = AlertDialog.Builder(requireContext())
            .setView(android.widget.ProgressBar(requireContext()))
            .setMessage(R.string.removing_background)
            .setCancelable(false)
            .show()

        viewLifecycleOwner.lifecycleScope.launch {

            val result = withContext(Dispatchers.IO) {

                val timestamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

                val dir = File(requireContext().filesDir, "images")
                dir.mkdirs()

                val cutoutPath = File(dir, "cutout_$timestamp.png").absolutePath

                if (GarmentCutout.cutout(imagePath, cutoutPath)) {
                    cutoutPath
                } else {
                    imagePath
                }
            }

            progress.dismiss()

            promptCategory(result)
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

        MaterialAlertDialogBuilder(requireContext())
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

        viewLifecycleOwner.lifecycleScope.launch {

            clothingDao.insert(
                ClothingItem(
                    ownerId = ownerId,
                    name = "",
                    category = category,
                    color = "",
                    material = "",
                    tags = "",
                    imagePath = imagePath
                )
            )

            loadCloset()
        }
    }

    // Populate every category section with its garments
    private fun loadCloset() {

        val view = view ?: return

        val ownerId = sessionManager.getUserId()

        if (ownerId == -1) {
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {

            val items = clothingDao.getAll(ownerId)

            renderCategory(
                view,
                items,
                getString(R.string.category_tops),
                R.id.horizontalTops,
                R.id.placeholderTops
            )

            renderCategory(
                view,
                items,
                getString(R.string.category_bottoms),
                R.id.horizontalBottoms,
                R.id.placeholderBottoms
            )

            renderCategory(
                view,
                items,
                getString(R.string.category_footwear),
                R.id.horizontalFootwear,
                R.id.placeholderFootwear
            )

            renderCategory(
                view,
                items,
                getString(R.string.category_accessories),
                R.id.horizontalAccessories,
                R.id.placeholderAccessories
            )
        }
    }

    // Render one category's garments after its "Add" placeholder card
    private fun renderCategory(
        root: View,
        items: List<ClothingItem>,
        category: String,
        containerId: Int,
        placeholderId: Int
    ) {

        val container = root.findViewById<LinearLayout>(containerId)
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

            card.setOnClickListener {

                startActivity(
                    Intent(requireContext(), GarmentDetailActivity::class.java)
                        .putExtra(GarmentDetailActivity.EXTRA_CLOTHING_ID, item.id)
                )
            }

            val image = card.findViewById<ImageView>(R.id.imgGarment)

            viewLifecycleOwner.lifecycleScope.launch {
                val bitmap =
                    withContext(Dispatchers.IO) { ImageDecoder.decode(item.imagePath, 220) }
                image.setImageBitmap(bitmap)
            }
        }
    }
}
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
import com.mobdeve.s15.reyes.janicamegan.clospace.util.GarmentCategory
import com.mobdeve.s15.reyes.janicamegan.clospace.util.GarmentClassifier
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
import android.app.AlertDialog

class ClosetFragment : Fragment() {
    private lateinit var backend: BackendRepository
    private var pendingImagePath: String? = null
    private var pendingCategory: GarmentCategory? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val path = pendingImagePath
        pendingImagePath = null
        if (success && path != null) removeBackground(path)
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val path = withContext(Dispatchers.IO) { copyUriToStorage(uri) }
                if (path != null) removeBackground(path) else {
                    Toast.makeText(requireContext(), R.string.image_load_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_closet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backend = BackendRepository(requireContext())
        view.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener { showSourceDialog() }
        loadCloset()
    }

    override fun onResume() { super.onResume(); if (::backend.isInitialized) loadCloset() }

    private fun showSourceDialog(preset: GarmentCategory? = null) {
        pendingCategory = preset
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_source, null)
        val dialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogView).show()
        dialogView.findViewById<View>(R.id.optionCamera).setOnClickListener { dialog.dismiss(); launchCamera() }
        dialogView.findViewById<View>(R.id.optionGallery).setOnClickListener { dialog.dismiss(); launchGallery() }
    }

    private fun launchCamera() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = File(requireContext().filesDir, "images").apply { mkdirs() }
        val file = File(dir, "captured_$timestamp.jpg")
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        pendingImagePath = file.absolutePath
        takePicture.launch(uri)
    }

    private fun launchGallery() = pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    private fun copyUriToStorage(uri: Uri): String? = try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = File(requireContext().filesDir, "images").apply { mkdirs() }
        val file = File(dir, "picked_$timestamp.jpg")
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        file.absolutePath
    } catch (_: Exception) { null }

    private fun removeBackground(imagePath: String) {
        val progress = AlertDialog.Builder(requireContext())
            .setView(android.widget.ProgressBar(requireContext()))
            .setMessage(R.string.removing_background)
            .setCancelable(false)
            .show()

        viewLifecycleOwner.lifecycleScope.launch {
            val (finalPath, detectedCategory) = withContext(Dispatchers.IO) {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val dir = File(requireContext().filesDir, "images").apply { mkdirs() }
                val cutoutPath = File(dir, "cutout_$timestamp.png").absolutePath
                val path = if (GarmentCutout.cutout(imagePath, cutoutPath)) cutoutPath else imagePath
                val category = pendingCategory ?: GarmentClassifier.classify(path)
                pendingCategory = null
                path to category
            }
            progress.dismiss()
            promptCategory(finalPath, detectedCategory)
        }
    }

    private fun promptCategory(imagePath: String, detected: GarmentCategory? = null) {
        val categories = arrayOf(
            getString(R.string.category_tops), getString(R.string.category_bottoms),
            getString(R.string.category_footwear), getString(R.string.category_accessories)
        )
        if (detected != null) saveGarment(imagePath, categories[detected.ordinal])
        else showCategoryPicker(imagePath, categories)
    }

    private fun showCategoryPicker(imagePath: String, categories: Array<String>) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.select_category)
            .setItems(categories) { _, which -> saveGarment(imagePath, categories[which]) }.show()
    }

    private fun saveGarment(imagePath: String, category: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { backend.insertClothing(category = category, localImagePath = imagePath) }
                .onSuccess { loadCloset() }
                .onFailure { Toast.makeText(requireContext(), it.message ?: "Unable to save garment", Toast.LENGTH_LONG).show() }
        }
    }

    private fun loadCloset() {
        val root = view ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { backend.getClothing() }
                .onSuccess { items ->
                    renderCategory(root, items, getString(R.string.category_tops), R.id.horizontalTops, R.id.placeholderTops, GarmentCategory.TOP)
                    renderCategory(root, items, getString(R.string.category_bottoms), R.id.horizontalBottoms, R.id.placeholderBottoms, GarmentCategory.BOTTOM)
                    renderCategory(root, items, getString(R.string.category_footwear), R.id.horizontalFootwear, R.id.placeholderFootwear, GarmentCategory.FOOTWEAR)
                    renderCategory(root, items, getString(R.string.category_accessories), R.id.horizontalAccessories, R.id.placeholderAccessories, GarmentCategory.ACCESSORY)
                }
                .onFailure { Toast.makeText(requireContext(), it.message ?: "Unable to load closet", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun renderCategory(root: View, items: List<ClothingItem>, category: String, containerId: Int, placeholderId: Int, preset: GarmentCategory) {
        val container = root.findViewById<LinearLayout>(containerId)
        val placeholder = container.findViewById<View>(placeholderId)

        container.removeView(placeholder)
        for (i in container.childCount - 1 downTo 0) container.removeViewAt(i)

        items.filter { it.category.equals(category, ignoreCase = true) }
            .sortedByDescending { it.id }
            .forEach { item ->
                val card = layoutInflater.inflate(R.layout.item_garment, container, false)
                container.addView(card)
                card.setOnClickListener {
                    startActivity(Intent(requireContext(), GarmentDetailActivity::class.java).putExtra(GarmentDetailActivity.EXTRA_CLOTHING_ID, item.id))
                }
                val image = card.findViewById<ImageView>(R.id.imgGarment)
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val bitmap = ImageDecoder.decode(item.imagePath, 220)
                    withContext(Dispatchers.Main) { image.setImageBitmap(bitmap) }
                }
            }

        placeholder.setOnClickListener { showSourceDialog(preset) }
        container.addView(placeholder)
    }
}

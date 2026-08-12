package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
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

class ClosetFragment : Fragment() {
    private lateinit var backend: BackendRepository
    private var pendingImagePath: String? = null
    private var pendingCategory: GarmentCategory? = null
    private var allItems: List<ClothingItem> = emptyList()
    private val placeholders = mutableMapOf<Int, View>()

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
        placeholders[R.id.placeholderTops] = view.findViewById(R.id.placeholderTops)
        placeholders[R.id.placeholderBottoms] = view.findViewById(R.id.placeholderBottoms)
        placeholders[R.id.placeholderFootwear] = view.findViewById(R.id.placeholderFootwear)
        placeholders[R.id.placeholderAccessories] = view.findViewById(R.id.placeholderAccessories)
        view.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener { showSourceDialog() }
        view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.searchLayout).setStartIconOnClickListener {
            applySearch()
        }
        view.findViewById<TextInputEditText>(R.id.etSearch).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrBlank()) renderItems(view, allItems)
                }
            })
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    applySearch()
                    true
                } else false
            }
        }
        loadCloset()
    }

    private fun applySearch() {
        renderItems(view ?: return, filterItems(currentQuery()))
    }

    override fun onResume() { super.onResume(); if (::backend.isInitialized) loadCloset() }

    private fun currentQuery(): String {
        val root = view ?: return ""
        return root.findViewById<TextInputEditText>(R.id.etSearch)?.text?.toString().orEmpty()
    }

    private fun showSourceDialog(preset: GarmentCategory? = null) {
        pendingCategory = preset
        ClospaceBottomSheets.showAddSource(
            requireContext(),
            onCamera = { launchCamera() },
            onGallery = { launchGallery() }
        )
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
        val progress = ClospaceBottomSheets.showProgress(requireContext(), R.string.removing_background)

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
        ClospaceBottomSheets.showChoice(requireContext(), R.string.select_category, categories) { which ->
            saveGarment(imagePath, categories[which])
        }
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
                    allItems = items
                    renderItems(root, filterItems(currentQuery()))
                }
                .onFailure { Toast.makeText(requireContext(), it.message ?: "Unable to load closet", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun renderItems(root: View, items: List<ClothingItem>) {
        val filtering = currentQuery().isNotBlank()
        renderCategory(root, items, getString(R.string.category_tops), R.id.horizontalTops, R.id.placeholderTops, R.id.cardTops, GarmentCategory.TOP, filtering)
        renderCategory(root, items, getString(R.string.category_bottoms), R.id.horizontalBottoms, R.id.placeholderBottoms, R.id.cardBottoms, GarmentCategory.BOTTOM, filtering)
        renderCategory(root, items, getString(R.string.category_footwear), R.id.horizontalFootwear, R.id.placeholderFootwear, R.id.cardFootwear, GarmentCategory.FOOTWEAR, filtering)
        renderCategory(root, items, getString(R.string.category_accessories), R.id.horizontalAccessories, R.id.placeholderAccessories, R.id.cardAccessories, GarmentCategory.ACCESSORY, filtering)
    }

    private fun filterItems(query: String): List<ClothingItem> {
        val q = query.trim()
        if (q.isEmpty()) return allItems
        return allItems.filter { item ->
            listOf(item.name, item.category, item.color, item.material, item.tags)
                .any { it.contains(q, ignoreCase = true) }
        }
    }

    private fun renderCategory(root: View, items: List<ClothingItem>, category: String, containerId: Int, placeholderId: Int, cardId: Int, preset: GarmentCategory, filtering: Boolean) {
        val container = root.findViewById<LinearLayout>(containerId)
        val placeholder = placeholders[placeholderId] ?: return
        val card = root.findViewById<View>(cardId)

        container.removeView(placeholder)
        for (i in container.childCount - 1 downTo 0) container.removeViewAt(i)

        val categoryItems = items.filter { it.category.equals(category, ignoreCase = true) }
            .sortedByDescending { it.id }

        if (categoryItems.isEmpty() && filtering) {
            card.visibility = View.GONE
            return
        }
        card.visibility = View.VISIBLE

        categoryItems.forEach { item ->
            val cardView = layoutInflater.inflate(R.layout.item_garment, container, false)
            container.addView(cardView)
            cardView.setOnClickListener {
                startActivity(Intent(requireContext(), GarmentDetailActivity::class.java).putExtra(GarmentDetailActivity.EXTRA_CLOTHING_ID, item.id))
            }
            val image = cardView.findViewById<ImageView>(R.id.imgGarment)
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = ImageDecoder.decode(item.imagePath, 220)
                withContext(Dispatchers.Main) { image.setImageBitmap(bitmap) }
            }
        }

        if (!filtering) {
            placeholder.setOnClickListener { showSourceDialog(preset) }
            container.addView(placeholder)
        }
    }
}

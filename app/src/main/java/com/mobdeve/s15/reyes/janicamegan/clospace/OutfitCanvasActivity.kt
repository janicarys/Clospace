package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.mobdeve.s15.reyes.janicamegan.clospace.util.ImageDecoder
import com.mobdeve.s15.reyes.janicamegan.clospace.view.DraggableImageView

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OutfitCanvasActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLOTHING_IDS = "clothingIds"
        const val EXTRA_X = "xPositions"
        const val EXTRA_Y = "yPositions"
        const val EXTRA_SCALE = "scales"
        const val EXTRA_LAYER = "layers"
        const val EXTRA_OUTFIT_ID = "outfitId"
        const val EXTRA_DATE = "selectedDate"
        const val EXTRA_CANVAS_RATIO = "canvasRatio"
    }

    private lateinit var backend: BackendRepository

    private var clothingIds: IntArray = intArrayOf()

    private var editOutfitId: Int = 0

    private lateinit var canvas: FrameLayout

    private val garments = mutableListOf<DraggableImageView>()

    private var selectedGarment: DraggableImageView? = null

    private var trashZone: View? = null

    private fun isOverTrashZone(view: DraggableImageView): Boolean {

        val zone = trashZone ?: return false

        val zoneLeft = zone.left.toFloat()
        val zoneTop = zone.top.toFloat()
        val zoneRight = zone.right.toFloat()
        val zoneBottom = zone.bottom.toFloat()

        val left = view.left + view.translationX
        val top = view.top + view.translationY
        val right = left + view.width
        val bottom = top + view.height

        return left < zoneRight && right > zoneLeft && top < zoneBottom && bottom > zoneTop
    }

    private val addClothesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode != RESULT_OK) return@registerForActivityResult

            val ids = result.data?.getIntArrayExtra(
                SelectGarmentsActivity.EXTRA_CLOTHING_IDS
            ) ?: return@registerForActivityResult

            addGarments(ids)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outfit_canvas)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finishOutfitFlow()
        }

        onBackPressedDispatcher.addCallback(this) {
            finishOutfitFlow()
        }

        backend = BackendRepository(this)

        clothingIds = intent.getIntArrayExtra(EXTRA_CLOTHING_IDS) ?: intArrayOf()
        editOutfitId = intent.getIntExtra(EXTRA_OUTFIT_ID, 0)

        canvas = findViewById(R.id.canvasContainer)
        trashZone = findViewById(R.id.trashZone)
        trashZone?.elevation = 1000f

        findViewById<View>(R.id.btnAddClothes).setOnClickListener {
            addClothesLauncher.launch(
                Intent(this, SelectGarmentsActivity::class.java)
                    .putExtra(SelectGarmentsActivity.EXTRA_RETURN_SELECTION, true)
            )
        }

        canvas.setOnClickListener { selectGarment(null) }

        showCanvasHintIfNeeded()

        findViewById<View>(R.id.btnSave).setOnClickListener {

            if (garments.isEmpty()) {

                Toast.makeText(
                    this,
                    R.string.select_at_least_one,
                    Toast.LENGTH_SHORT
                ).show()

            } else if (editOutfitId > 0) {

                saveEditedOutfit()

            } else {

                val placements = computePlacements()

                val detail = Intent(this, OutfitDetailsActivity::class.java)
                    .putExtra(
                        OutfitDetailsActivity.EXTRA_CLOTHING_IDS,
                        placements.map { it.clothingId }.toIntArray()
                    )
                    .putExtra(
                        OutfitDetailsActivity.EXTRA_X,
                        placements.map { it.x }.toFloatArray()
                    )
                    .putExtra(
                        OutfitDetailsActivity.EXTRA_Y,
                        placements.map { it.y }.toFloatArray()
                    )
                    .putExtra(
                        OutfitDetailsActivity.EXTRA_SCALE,
                        placements.map { it.scale }.toFloatArray()
                    )
                    .putExtra(
                        OutfitDetailsActivity.EXTRA_LAYER,
                        placements.map { it.layer }.toIntArray()
                    )
                    .putExtra(
                        OutfitDetailsActivity.EXTRA_CANVAS_RATIO,
                        canvasRatio()
                    )
                    .putExtra(
                        OutfitDetailsActivity.EXTRA_DATE,
                        intent.getStringExtra(EXTRA_DATE)
                    )

                startActivity(detail)
            }
        }

        findViewById<View>(R.id.btnForward).setOnClickListener {
            moveLayer(1)
        }

        findViewById<View>(R.id.btnBackward).setOnClickListener {
            sendToBack()
        }

        loadGarments()
    }

    private fun loadGarments() {
        lifecycleScope.launch {
            if (editOutfitId > 0) {
                val wrapper = backend.getOutfitById(editOutfitId) ?: return@launch
                for (placement in wrapper.placements.sortedBy { it.layer }) {
                    val bitmap = withContext(Dispatchers.IO) { ImageDecoder.decode(placement.item.imagePath, 512) } ?: continue
                    val view = createGarment(bitmap, placement.item.id)
                    garments.add(view)
                    restorePlace(view, placement.x, placement.y, placement.scale)
                }
            } else {
                for (id in clothingIds) {
                    val item = backend.getClothingById(id) ?: continue
                    val bitmap = withContext(Dispatchers.IO) { ImageDecoder.decode(item.imagePath, 512) } ?: continue
                    val view = createGarment(bitmap, id)
                    garments.add(view)
                    canvas.addView(view)
                }
            }
            reindexLayers()
            if (garments.isNotEmpty()) selectGarment(garments.first())
        }
    }

    private fun addGarments(ids: IntArray) {
        lifecycleScope.launch {
            for (id in ids) {
                if (garments.any { it.tag as? Int == id }) continue
                val item = backend.getClothingById(id) ?: continue
                val bitmap = withContext(Dispatchers.IO) { ImageDecoder.decode(item.imagePath, 512) } ?: continue
                val view = createGarment(bitmap, id)
                garments.add(view)
                canvas.addView(view)
            }
            reindexLayers()
            if (garments.isNotEmpty()) selectGarment(garments.last())
        }
    }

    private fun showCanvasHintIfNeeded() {
        val hint = findViewById<TextView>(R.id.tvCanvasHint)
        val prefs = getSharedPreferences("clospace", MODE_PRIVATE)
        if (prefs.getBoolean("canvas_hint_seen", false)) return
        hint.visibility = View.VISIBLE
        hint.postDelayed({
            hint.animate().alpha(0f).setDuration(500).withEndAction {
                hint.visibility = View.GONE
            }.start()
            prefs.edit().putBoolean("canvas_hint_seen", true).apply()
        }, 3000)
    }

    private fun createGarment(bitmap: Bitmap, clothingId: Int): DraggableImageView {

        val view = DraggableImageView(this)

        view.tag = clothingId

        view.setImageBitmap(bitmap)
        view.scaleType = ImageView.ScaleType.FIT_CENTER
        view.setPadding(dp(4), dp(4), dp(4), dp(4))

        val params = FrameLayout.LayoutParams(dp(150), dp(180))
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.topMargin = dp(30) + garments.size * dp(26)

        view.layoutParams = params

        view.onSelected = { selectGarment(it) }

        view.onDragStart = { updateTrashHighlight() }

        view.onPositionChanged = { updateTrashHighlight() }

        view.onDragEnd = { v -> handleDrop(v) }

        return view
    }

    private fun updateTrashHighlight() {

        val zone = trashZone ?: return

        val anyDragging = garments.any { it.isDragging() }

        if (!anyDragging) {
            zone.visibility = View.GONE
            return
        }

        zone.visibility = View.VISIBLE

        val anyOver = garments.any { isOverTrashZone(it) }

        zone.alpha = if (anyOver) 1f else 0.4f

        zone.scaleX = if (anyOver) 1.15f else 1f
        zone.scaleY = if (anyOver) 1.15f else 1f
    }

    private fun handleDrop(view: DraggableImageView) {

        if (canvas.indexOfChild(view) < 0) {
            return
        }

        updateTrashHighlight()

        if (!isOverTrashZone(view)) {
            return
        }

        canvas.removeView(view)
        garments.remove(view)

        if (selectedGarment == view) {
            selectedGarment = null
        }

        reindexLayers()

        updateTrashHighlight()

        Toast.makeText(this, R.string.removed_from_canvas, Toast.LENGTH_SHORT).show()
    }

    private fun restorePlace(
        view: DraggableImageView,
        x: Float,
        y: Float,
        scale: Float
    ) {

        canvas.addView(view)

        view.post {

            val canvasWidth = canvas.width.toFloat()
            val canvasHeight = canvas.height.toFloat()

            if (canvasWidth == 0f || canvasHeight == 0f) {
                return@post
            }

            view.translationX = x * canvasWidth - (view.left + view.width / 2f)
            view.translationY = y * canvasHeight - (view.top + view.height / 2f)

            view.setInitialScale(scale)
        }
    }

    private fun selectGarment(view: DraggableImageView?) {

        selectedGarment?.setSelectedVisual(false)

        selectedGarment = view

        view?.setSelectedVisual(true)
    }

    private fun reindexLayers() {

        garments.forEachIndexed { index, view ->
            view.elevation = index.toFloat()
        }
    }

    private fun moveLayer(direction: Int) {

        val selected = selectedGarment ?: return

        val index = garments.indexOf(selected)

        val target = index + direction

        if (target < 0 || target >= garments.size) {
            return
        }

        garments.removeAt(index)
        garments.add(target, selected)

        reindexLayers()
    }

    private fun sendToBack() {

        val selected = selectedGarment ?: return

        val index = garments.indexOf(selected)

        if (index <= 0) {
            return
        }

        garments.removeAt(index)
        garments.add(0, selected)

        reindexLayers()
    }

    private fun saveEditedOutfit() {
        val ratio = canvasRatio()
        val placements = computePlacements().map {
            OutfitItem(
                outfitId = editOutfitId,
                clothingId = it.clothingId,
                x = it.x,
                y = it.y,
                scale = it.scale,
                layer = it.layer,
                canvasRatio = ratio
            )
        }

        lifecycleScope.launch {
            runCatching {
                val existing = backend.getOutfitById(editOutfitId)?.outfit ?: return@runCatching
                backend.updateOutfit(
                    id = editOutfitId,
                    caption = existing.caption,
                    occasion = existing.occasion,
                    selectedDate = existing.plannedDate,
                    tags = existing.tags,
                    placements = placements
                )
            }.onSuccess {
                Toast.makeText(this@OutfitCanvasActivity, R.string.outfit_updated, Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this@OutfitCanvasActivity, it.message ?: "Unable to update outfit", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun computePlacements(): List<GarmentPlacement> {

        val canvasWidth = canvas.width.toFloat()
        val canvasHeight = canvas.height.toFloat()

        if (canvasWidth == 0f || canvasHeight == 0f) {
            return emptyList()
        }

        return garments.map { view ->

            val centerX = view.left + view.width / 2f + view.translationX
            val centerY = view.top + view.height / 2f + view.translationY

            GarmentPlacement(
                clothingId = view.tag as Int,
                x = (centerX / canvasWidth).coerceIn(0f, 1f),
                y = (centerY / canvasHeight).coerceIn(0f, 1f),
                scale = view.scale,
                layer = garments.indexOf(view)
            )
        }
    }

    private fun canvasRatio(): Float {
        val width = canvas.width.coerceAtLeast(1)
        val height = canvas.height.coerceAtLeast(1)
        return height.toFloat() / width.toFloat()
    }

    private data class GarmentPlacement(
        val clothingId: Int,
        val x: Float,
        val y: Float,
        val scale: Float,
        val layer: Int
    )

    private fun dp(value: Int): Int {

        return (value * resources.displayMetrics.density).toInt()
    }

    private fun finishOutfitFlow() {

        val survivors = garments.mapNotNull { it.tag as? Int }.toIntArray()

        setResult(
            RESULT_OK,
            Intent().putExtra(EXTRA_CLOTHING_IDS, survivors)
        )

        finish()
    }
}

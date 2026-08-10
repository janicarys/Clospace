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
    }

    private lateinit var backend: BackendRepository

    private var clothingIds: IntArray = intArrayOf()

    private var editOutfitId: Int = 0

    private lateinit var canvas: FrameLayout

    private val garments = mutableListOf<DraggableImageView>()

    private var selectedGarment: DraggableImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outfit_canvas)

        findViewById<TextView>(R.id.tvToolbarTitle).text =
            getString(R.string.arrange_outfit_title)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        backend = BackendRepository(this)

        clothingIds = intent.getIntArrayExtra(EXTRA_CLOTHING_IDS) ?: intArrayOf()
        editOutfitId = intent.getIntExtra(EXTRA_OUTFIT_ID, 0)

        canvas = findViewById(R.id.canvasContainer)

        if (editOutfitId > 0) {

            findViewById<TextView>(R.id.tvToolbarTitle).text =
                getString(R.string.edit_outfit_title)
        }

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

                startActivity(detail)
            }
        }

        findViewById<View>(R.id.btnForward).setOnClickListener {
            moveLayer(1)
        }

        findViewById<View>(R.id.btnBackward).setOnClickListener {
            moveLayer(-1)
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

        return view
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

    private fun selectGarment(view: DraggableImageView) {

        selectedGarment?.setSelectedVisual(false)

        selectedGarment = view

        view.setSelectedVisual(true)
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

    private fun saveEditedOutfit() {
        val placements = computePlacements().map {
            OutfitItem(
                outfitId = editOutfitId,
                clothingId = it.clothingId,
                x = it.x,
                y = it.y,
                scale = it.scale,
                layer = it.layer
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
}

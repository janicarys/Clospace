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
    }

    private lateinit var sessionManager: SessionManager

    private lateinit var clothingDao: ClothingDao

    private var clothingIds: IntArray = intArrayOf()

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

        sessionManager = SessionManager(this)

        clothingDao = ClospaceDatabase.getDatabase(this).clothingDao()

        clothingIds = intent.getIntArrayExtra(EXTRA_CLOTHING_IDS) ?: intArrayOf()

        canvas = findViewById(R.id.canvasContainer)

        findViewById<View>(R.id.btnSave).setOnClickListener {

            if (garments.isEmpty()) {

                Toast.makeText(
                    this,
                    R.string.select_at_least_one,
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                startActivity(
                    Intent(this, OutfitDetailsActivity::class.java)
                        .putExtra(
                            OutfitDetailsActivity.EXTRA_CLOTHING_IDS,
                            clothingIds
                        )
                )
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

            for (id in clothingIds) {

                val item = clothingDao.getById(id) ?: continue

                val bitmap = withContext(Dispatchers.IO) {
                    ImageDecoder.decode(item.imagePath, 512)
                } ?: continue

                val view = createGarment(bitmap)

                garments.add(view)
                canvas.addView(view)
            }

            reindexLayers()

            if (garments.isNotEmpty()) {
                selectGarment(garments.first())
            }
        }
    }

    private fun createGarment(bitmap: Bitmap): DraggableImageView {

        val view = DraggableImageView(this)

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

    private fun dp(value: Int): Int {

        return (value * resources.displayMetrics.density).toInt()
    }
}

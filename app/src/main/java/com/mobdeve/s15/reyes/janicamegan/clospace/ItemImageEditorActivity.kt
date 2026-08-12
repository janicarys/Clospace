package com.mobdeve.s15.reyes.janicamegan.clospace

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mobdeve.s15.reyes.janicamegan.clospace.util.ImageDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

class ItemImageEditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLOTHING_ID = "clothingId"

        private const val OUTPUT_SIZE = 800
        private const val BASE_FILL = 0.8f
        private const val ZOOM_MIN = 0.3f
        private const val ZOOM_MAX = 2.0f
        private const val ZOOM_STEP = 0.1f
    }

    private lateinit var backend: BackendRepository

    private var clothingId = 0

    private var original: Bitmap? = null

    private var quarterTurns = 0

    private var zoom = 1f

    private data class EditState(val quarterTurns: Int, val zoom: Float)

    private val history = ArrayDeque<EditState>()

    private fun commit() {
        history.addLast(EditState(quarterTurns, zoom))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_image_editor)

        backend = BackendRepository(this)
        clothingId = intent.getIntExtra(EXTRA_CLOTHING_ID, 0)

        findViewById<TextView>(R.id.tvToolbarTitle).text = getString(R.string.image_editor_title)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnUndo).setOnClickListener {
            if (history.isNotEmpty()) {
                val state = history.removeLast()
                quarterTurns = state.quarterTurns
                zoom = state.zoom
                refresh()
            }
        }
        findViewById<View>(R.id.btnRotateLeft).setOnClickListener { commit(); quarterTurns--; refresh() }
        findViewById<View>(R.id.btnRotateRight).setOnClickListener { commit(); quarterTurns++; refresh() }
        findViewById<View>(R.id.btnZoomOut).setOnClickListener { commit(); zoom = (zoom - ZOOM_STEP).coerceAtLeast(ZOOM_MIN); refresh() }
        findViewById<View>(R.id.btnZoomIn).setOnClickListener { commit(); zoom = (zoom + ZOOM_STEP).coerceAtMost(ZOOM_MAX); refresh() }
        findViewById<View>(R.id.btnReset).setOnClickListener { history.clear(); quarterTurns = 0; zoom = 1f; refresh() }

        findViewById<View>(R.id.btnSave).setOnClickListener { save() }

        lifecycleScope.launch {
            val item = runCatching { backend.getClothingById(clothingId) }.getOrNull()
            val bitmap = item?.imagePath?.let {
                withContext(Dispatchers.IO) { ImageDecoder.decode(it, OUTPUT_SIZE) }
            }
            if (bitmap == null) {
                Toast.makeText(this@ItemImageEditorActivity, R.string.image_load_failed, Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            original = bitmap
            refresh()
        }
    }

    /** Rebuilds the preview from the original with the current rotation and zoom. */
    private fun refresh() {
        val source = original ?: return
        val imageView = findViewById<ImageView>(R.id.imgPreview)
        val size = imageView.width.takeIf { it > 0 }?.coerceIn(300, OUTPUT_SIZE) ?: OUTPUT_SIZE
        imageView.setImageBitmap(buildEdited(source, quarterTurns, zoom, size))
    }

    /** Renders the source rotated 90° in quarter-turns and zoomed, centered in a square canvas. */
    private fun buildEdited(source: Bitmap, turns: Int, zoom: Float, size: Int): Bitmap {
        val src = rotateQuarterTurns(source, turns)
        val maxSide = max(src.width, src.height)
        val fit = (BASE_FILL * size) / maxSide
        val maxScale = size.toFloat() / maxSide
        val scale = min(fit * zoom, maxScale).coerceAtLeast(size * 0.1f / maxSide)

        val width = (src.width * scale).toInt().coerceAtLeast(2)
        val height = (src.height * scale).toInt().coerceAtLeast(2)

        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

        val left = (size - width) / 2f
        val top = (size - height) / 2f

        canvas.drawBitmap(src, null, RectF(left, top, left + width, top + height), paint)
        return out
    }

    private fun rotateQuarterTurns(source: Bitmap, turns: Int): Bitmap {
        val normal = ((turns % 4) + 4) % 4
        if (normal == 0) return source
        val matrix = Matrix().apply { postRotate(90f * normal) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun save() {
        val source = original ?: return
        val saveButton = findViewById<View>(R.id.btnSave)
        saveButton.isEnabled = false
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val finalBitmap = buildEdited(source, quarterTurns, zoom, OUTPUT_SIZE)
                    val dir = File(cacheDir, "edited").apply { mkdirs() }
                    val file = File(dir, "edited_${System.currentTimeMillis()}.png")
                    FileOutputStream(file).use { out ->
                        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    backend.updateClothingImage(clothingId, file.absolutePath)
                }
            }
            saveButton.isEnabled = true
            result.onSuccess {
                Toast.makeText(this@ItemImageEditorActivity, R.string.image_saved, Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }.onFailure {
                Toast.makeText(
                    this@ItemImageEditorActivity,
                    it.message ?: getString(R.string.image_save_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

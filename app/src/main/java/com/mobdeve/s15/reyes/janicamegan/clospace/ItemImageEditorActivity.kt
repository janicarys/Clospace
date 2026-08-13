package com.mobdeve.s15.reyes.janicamegan.clospace

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
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
        private const val ANGLE_RANGE = 45
    }

    private lateinit var backend: BackendRepository

    private var clothingId = 0

    private var original: Bitmap? = null

    private var quarterTurns = 0

    private var angle = 0f

    private var flipped = false

    private data class EditState(val quarterTurns: Int, val angle: Float, val flipped: Boolean)

    private val undoStack = ArrayDeque<EditState>()

    private val redoStack = ArrayDeque<EditState>()

    private var dragStartAngle = 0f

    private fun commit() {
        undoStack.addLast(EditState(quarterTurns, angle, flipped))
        redoStack.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_image_editor)

        backend = BackendRepository(this)
        clothingId = intent.getIntExtra(EXTRA_CLOTHING_ID, 0)

        findViewById<View>(R.id.btnCancel).setOnClickListener { finish() }
        findViewById<View>(R.id.btnDone).setOnClickListener { save() }

        val tvAngle = findViewById<TextView>(R.id.tvAngle)
        val seekRotate = findViewById<SeekBar>(R.id.seekRotate)
        seekRotate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val newAngle = (progress - ANGLE_RANGE).toFloat()
                if (newAngle != angle) {
                    angle = newAngle
                    tvAngle.text = angle.toInt().toString()
                    refresh()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                dragStartAngle = angle
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (angle != dragStartAngle) {
                    undoStack.addLast(EditState(quarterTurns, dragStartAngle, flipped))
                    redoStack.clear()
                }
            }
        })

        findViewById<View>(R.id.btnUndo).setOnClickListener {
            if (undoStack.isNotEmpty()) {
                redoStack.addLast(EditState(quarterTurns, angle, flipped))
                val state = undoStack.removeLast()
                quarterTurns = state.quarterTurns
                angle = state.angle
                flipped = state.flipped
                syncRotation()
                refresh()
            }
        }
        findViewById<View>(R.id.btnRedo).setOnClickListener {
            if (redoStack.isNotEmpty()) {
                undoStack.addLast(EditState(quarterTurns, angle, flipped))
                val state = redoStack.removeLast()
                quarterTurns = state.quarterTurns
                angle = state.angle
                flipped = state.flipped
                syncRotation()
                refresh()
            }
        }
        findViewById<View>(R.id.btnRotateLeft).setOnClickListener { commit(); quarterTurns--; refresh() }
        findViewById<View>(R.id.btnFlip).setOnClickListener { commit(); flipped = !flipped; refresh() }

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

    /** Rebuilds the preview from the original with the current rotation and flip. */
    private fun refresh() {
        val source = original ?: return
        val imageView = findViewById<ImageView>(R.id.imgPreview)
        val size = imageView.width.takeIf { it > 0 }?.coerceIn(300, OUTPUT_SIZE) ?: OUTPUT_SIZE
        imageView.setImageBitmap(buildEdited(source, quarterTurns, angle, flipped, size))
    }

    /** Keeps the slider and angle label in sync with the current rotation. */
    private fun syncRotation() {
        findViewById<SeekBar>(R.id.seekRotate).progress = angle.toInt() + ANGLE_RANGE
        findViewById<TextView>(R.id.tvAngle).text = angle.toInt().toString()
    }

    /** Renders the source flipped and rotated (90° quarter-turns + fine angle), centered with a small uniform margin. */
    private fun buildEdited(source: Bitmap, turns: Int, angle: Float, flipped: Boolean, size: Int): Bitmap {
        val maxSide = max(source.width, source.height)
        val fit = (BASE_FILL * size) / maxSide
        val maxScale = size.toFloat() / maxSide
        val scale = min(fit, maxScale).coerceAtLeast(size * 0.1f / maxSide)

        val aspect = source.width.toFloat() / source.height
        val outW: Int
        val outH: Int
        if (aspect >= 1f) {
            outW = size
            outH = (size / aspect).toInt().coerceAtLeast(1)
        } else {
            outH = size
            outW = (size * aspect).toInt().coerceAtLeast(1)
        }

        val out = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

        val centerX = outW / 2f
        val centerY = outH / 2f
        canvas.rotate(90f * turns + angle, centerX, centerY)
        if (flipped) {
            canvas.scale(-1f, 1f, centerX, centerY)
        }

        val width = source.width * scale
        val height = source.height * scale
        val left = (outW - width) / 2f
        val top = (outH - height) / 2f

        canvas.drawBitmap(source, null, RectF(left, top, left + width, top + height), paint)
        return out
    }

    private fun save() {
        val source = original ?: return
        val saveButton = findViewById<View>(R.id.btnDone)
        saveButton.isEnabled = false
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val finalBitmap = buildEdited(source, quarterTurns, angle, flipped, OUTPUT_SIZE)
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

package com.mobdeve.s15.reyes.janicamegan.clospace.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView

import com.mobdeve.s15.reyes.janicamegan.clospace.R

class DraggableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ImageView(context, attrs) {

    var minScale: Float = 0.4f
    var maxScale: Float = 4f

    var scale: Float = 1f
        private set

    var onSelected: ((DraggableImageView) -> Unit)? = null

    private var lastX = 0f
    private var lastY = 0f

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(detector: ScaleGestureDetector): Boolean {

                val newScale =
                    (scale * detector.scaleFactor).coerceIn(minScale, maxScale)

                scale = newScale
                scaleX = newScale
                scaleY = newScale

                return true
            }
        }
    )

    override fun onTouchEvent(event: MotionEvent): Boolean {

        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                parent?.requestDisallowInterceptTouchEvent(true)

                lastX = event.rawX
                lastY = event.rawY

                onSelected?.invoke(this)
            }

            MotionEvent.ACTION_MOVE -> {

                if (!scaleDetector.isInProgress) {

                    val dx = event.rawX - lastX
                    val dy = event.rawY - lastY

                    translationX += dx
                    translationY += dy

                    lastX = event.rawX
                    lastY = event.rawY
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }

        return true
    }

    fun setSelectedVisual(selected: Boolean) {

        setBackgroundResource(
            if (selected) R.drawable.garment_selected_border else 0
        )
    }
}

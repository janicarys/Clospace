package com.mobdeve.s15.reyes.janicamegan.clospace.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

import com.mobdeve.s15.reyes.janicamegan.clospace.OutfitPlacement

object OutfitRenderer {

    /** Fraction of the smallest card side left as breathing room around the outfit. */
    private const val PADDING_FRACTION = 0.07f

    fun render(
        placements: List<OutfitPlacement>,
        widthPx: Int,
        heightPx: Int
    ): Bitmap? {

        if (placements.isEmpty()) {
            return null
        }

        // Base garment size in normalized (0..1) layout space.
        val baseWidth = 0.42f
        val baseHeight = baseWidth * 1.2f

        // Placements store x/y as fractions of the canvas WIDTH/HEIGHT. The
        // canvas is a tall portrait frame, so the y-axis must be stretched by
        // the canvas aspect (height / width) to reproduce the real layout
        // instead of collapsing it into a square.
        val portraitScale = orderedRatio(placements)

        // Decode each garment and record the rect of the *visible* photo (after
        // FIT_CENTER letterboxing) in layout space, so the crop hugs the actual
        // drawn content instead of the nominal placement box.
        var minX = 1f
        var minY = 1f
        var maxX = 0f
        var maxY = 0f

        val drawn = mutableListOf<Triple<OutfitPlacement, Bitmap, RectF>>()

        val ordered = placements.sortedBy { it.layer }

        for (placement in ordered) {

            val bitmap = ImageDecoder.decode(placement.item.imagePath, 512) ?: continue

            val drawWidth = baseWidth * placement.scale
            val drawHeight = baseHeight * placement.scale

            val centerX = placement.x
            val centerY = placement.y * portraitScale

            val srcAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
            val shownWidth = minOf(drawWidth, drawHeight * srcAspect)
            val shownHeight = minOf(drawHeight, drawWidth / srcAspect)

            val shown = RectF(
                centerX - shownWidth / 2f,
                centerY - shownHeight / 2f,
                centerX + shownWidth / 2f,
                centerY + shownHeight / 2f
            )

            minX = minOf(minX, shown.left)
            maxX = maxOf(maxX, shown.right)
            minY = minOf(minY, shown.top)
            maxY = maxOf(maxY, shown.bottom)

            drawn.add(Triple(placement, bitmap, shown))
        }

        if (drawn.isEmpty()) {
            return null
        }

        val boundsWidth = (maxX - minX).coerceAtLeast(0.0001f)
        val boundsHeight = (maxY - minY).coerceAtLeast(0.0001f)

        val paddingPx = PADDING_FRACTION * minOf(widthPx, heightPx)
        val availWidth = (widthPx - 2 * paddingPx).coerceAtLeast(1f)
        val availHeight = (heightPx - 2 * paddingPx).coerceAtLeast(1f)

        // Scale the outfit so it fills as much of the card as possible while
        // staying entirely inside the padded frame, then center it.
        val fit = minOf(availWidth / boundsWidth, availHeight / boundsHeight)

        val centeredWidth = boundsWidth * fit
        val centeredHeight = boundsHeight * fit

        val offsetX = (widthPx - centeredWidth) / 2f - minX * fit
        val offsetY = (heightPx - centeredHeight) / 2f - minY * fit

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        for ((placement, garmentBitmap, shown) in drawn) {

            val centerX = placement.x * fit + offsetX
            val centerY = placement.y * portraitScale * fit + offsetY

            // The photo is centered on the same spot and keeps its own aspect
            // ratio exactly as FIT_CENTER does, so nothing stretches.
            val drawWidth = shown.width() * fit
            val drawHeight = shown.height() * fit

            canvas.drawBitmap(
                garmentBitmap,
                null,
                RectF(
                    centerX - drawWidth / 2f,
                    centerY - drawHeight / 2f,
                    centerX + drawWidth / 2f,
                    centerY + drawHeight / 2f
                ),
                paint
            )
        }

        return bitmap
    }

    private fun orderedRatio(placements: List<OutfitPlacement>): Float =
        placements.asSequence().map { it.canvasRatio }.filter { it > 0f }.minOrNull() ?: 1f
}
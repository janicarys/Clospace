package com.mobdeve.s15.reyes.janicamegan.clospace.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

import com.mobdeve.s15.reyes.janicamegan.clospace.OutfitPlacement

object OutfitRenderer {

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

        // Decode each garment and record its rectangle in layout space so we
        // can extend the bounds over only the garments that are actually shown.
        var minX = 1f
        var minY = 1f
        var maxX = 0f
        var maxY = 0f

        val drawn = mutableListOf<Pair<OutfitPlacement, Bitmap>>()

        val ordered = placements.sortedBy { it.layer }

        for (placement in ordered) {

            val bitmap = ImageDecoder.decode(placement.item.imagePath, 200) ?: continue

            val drawWidth = baseWidth * placement.scale
            val drawHeight = baseHeight * placement.scale

            minX = minOf(minX, placement.x - drawWidth / 2f)
            maxX = maxOf(maxX, placement.x + drawWidth / 2f)
            minY = minOf(minY, placement.y - drawHeight / 2f)
            maxY = maxOf(maxY, placement.y + drawHeight / 2f)

            drawn.add(placement to bitmap)
        }

        if (drawn.isEmpty()) {
            return null
        }

        val boundsWidth = (maxX - minX).coerceAtLeast(0.0001f)
        val boundsHeight = (maxY - minY).coerceAtLeast(0.0001f)

        // Compute the scale that fits the whole outfit into the card.
        val fit = minOf(
            widthPx / boundsWidth,
            heightPx / boundsHeight
        )

        val centeredWidth = boundsWidth * fit
        val centeredHeight = boundsHeight * fit

        val offsetX = (widthPx - centeredWidth) / 2f - minX * fit
        val offsetY = (heightPx - centeredHeight) / 2f - minY * fit

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        for ((placement, garmentBitmap) in drawn) {

            val drawWidth = baseWidth * placement.scale * fit
            val drawHeight = baseHeight * placement.scale * fit

            val centerX = placement.x * fit + offsetX
            val centerY = placement.y * fit + offsetY

            // Fit the garment photo inside its placement box and keep its own
            // aspect ratio (same as the canvas's FIT_CENTER) instead of
            // stretching it, so previews match what was actually composed.
            val srcAspect = garmentBitmap.width.toFloat() / garmentBitmap.height.toFloat()

            val shownWidth = minOf(drawWidth, drawHeight * srcAspect)
            val shownHeight = minOf(drawHeight, drawWidth / srcAspect)

            canvas.drawBitmap(
                garmentBitmap,
                null,
                RectF(
                    centerX - shownWidth / 2f,
                    centerY - shownHeight / 2f,
                    centerX + shownWidth / 2f,
                    centerY + shownHeight / 2f
                ),
                paint
            )
        }

        return bitmap
    }
}
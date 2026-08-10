package com.mobdeve.s15.reyes.janicamegan.clospace.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object ImageDecoder {

    fun decode(path: String?, targetSize: Int = 200): Bitmap? {

        if (path.isNullOrEmpty()) {
            return null
        }

        return try {

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)

            var sampleSize = 1

            while (
                bounds.outWidth / sampleSize > targetSize ||
                bounds.outHeight / sampleSize > targetSize
            ) {
                sampleSize *= 2
            }

            BitmapFactory.decodeFile(
                path,
                BitmapFactory.Options().apply { inSampleSize = sampleSize }
            )

        } catch (e: Exception) {

            null
        }
    }
}

package com.mobdeve.s15.reyes.janicamegan.clospace.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import java.io.File
import java.io.FileOutputStream

object GarmentCutout {

    // Runs subject segmentation on the image at the given path and writes a
    // transparent-background PNG cutout to outputPath. Returns true on success.
    fun cutout(sourcePath: String, outputPath: String): Boolean {

        return try {

            val bitmap = decode(sourcePath, 1024) ?: return false

            val options = SubjectSegmenterOptions.Builder()
                .enableForegroundConfidenceMask()
                .build()

            val segmenter = SubjectSegmentation.getClient(options)

            val result = Tasks.await(segmenter.process(InputImage.fromBitmap(bitmap, 0)))

            val mask = result.foregroundConfidenceMask ?: return false

            val width = bitmap.width
            val height = bitmap.height

            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            for (i in pixels.indices) {

                val confidence = mask.get(i)

                val alpha = (confidence * 255).toInt().coerceIn(0, 255)

                val rgb = pixels[i] and 0x00FFFFFF

                pixels[i] = Color.argb(alpha, Color.red(rgb), Color.green(rgb), Color.blue(rgb))
            }

            val cutout = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

            val success = try {

                FileOutputStream(File(outputPath)).use { out ->
                    cutout.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                true

            } catch (e: Exception) {

                false
            }

            bitmap.recycle()
            cutout.recycle()

            success

        } catch (e: Exception) {

            false
        }
    }

    private fun decode(path: String, targetSize: Int): Bitmap? {

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

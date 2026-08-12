package com.mobdeve.s15.reyes.janicamegan.clospace.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

enum class GarmentCategory {
    TOP, BOTTOM, FOOTWEAR, ACCESSORY
}

// Uses ML Kit Image Labeling (bundled model) to guess which category a
// garment photo belongs to. Returns null when it isn't confident enough.
object GarmentClassifier {

    private const val MIN_CONFIDENCE = 0.45f

    private val TOP_KEYWORDS = setOf(
        "shirt", "t-shirt", "tee", "sweater", "sweatshirt", "hoodie", "jacket",
        "coat", "blouse", "polo", "dress", "tank", "jumper", "cardigan", "vest",
        "pullover", "turtleneck", "top", "hoody"
    )

    private val BOTTOM_KEYWORDS = setOf(
        "pants", "trousers", "jeans", "shorts", "skirt", "leggings", "slacks",
        "chinos", "joggers", "sweatpants", "culottes", "bottom", "denim"
    )

    private val FOOTWEAR_KEYWORDS = setOf(
        "shoe", "sneaker", "boot", "sandal", "slipper", "heel", "loafer",
        "oxford", "mule", "clog", "espadrille", "trainer", "flip-flop", "footwear"
    )

    private val ACCESSORY_KEYWORDS = setOf(
        "hat", "cap", "scarf", "belt", "glove", "watch", "necklace", "bracelet",
        "earring", "ring", "handbag", "backpack", "purse", "wallet", "sunglasses",
        "glasses", "tie", "sock", "stocking", "umbrella", "tote", "jewelry"
    )

    // Guesses the garment category for the image at sourcePath.
    fun classify(sourcePath: String): GarmentCategory? {

        return try {

            val bitmap = decode(sourcePath, 640) ?: return null

            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

            val labels = Tasks.await(labeler.process(InputImage.fromBitmap(bitmap, 0)))

            bitmap.recycle()

            val scores = floatArrayOf(0f, 0f, 0f, 0f)

            for (label in labels) {

                val text = label.text
                val confidence = label.confidence

                when {
                    matches(text, TOP_KEYWORDS) -> scores[GarmentCategory.TOP.ordinal] += confidence
                    matches(text, BOTTOM_KEYWORDS) -> scores[GarmentCategory.BOTTOM.ordinal] += confidence
                    matches(text, FOOTWEAR_KEYWORDS) -> scores[GarmentCategory.FOOTWEAR.ordinal] += confidence
                    matches(text, ACCESSORY_KEYWORDS) -> scores[GarmentCategory.ACCESSORY.ordinal] += confidence
                }
            }

            var bestIndex = -1
            var bestScore = MIN_CONFIDENCE

            for (i in scores.indices) {
                if (scores[i] > bestScore) {
                    bestScore = scores[i]
                    bestIndex = i
                }
            }

            if (bestIndex == -1) null else GarmentCategory.entries[bestIndex]

        } catch (e: Exception) {

            null
        }
    }

    // Checks whether a label text contains any keyword as a whole word,
    // tolerating a trailing "s" for plurals.
    private fun matches(text: String, keywords: Set<String>): Boolean {

        val normalized = text.lowercase()

        return keywords.any { keyword ->

            val escaped = Regex.escape(keyword)

            Regex("\\b$escaped\\b").containsMatchIn(normalized) ||
                Regex("\\b${escaped}s\\b").containsMatchIn(normalized)
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

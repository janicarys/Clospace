package com.mobdeve.s15.reyes.janicamegan.clospace.util

import android.graphics.Bitmap
import android.util.LruCache
import com.mobdeve.s15.reyes.janicamegan.clospace.OutfitPlacement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OutfitPreviewCache {

    private const val MAX_PREVIEWS = 64

    private val cache = object : LruCache<Int, Bitmap>(MAX_PREVIEWS) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.byteCount
    }

    fun get(outfitId: Int): Bitmap? = cache.get(outfitId)

    fun evict(outfitId: Int) {
        cache.remove(outfitId)
    }

    suspend fun render(
        outfitId: Int,
        placements: List<OutfitPlacement>,
        widthPx: Int,
        heightPx: Int
    ): Bitmap? {
        cache.get(outfitId)?.let { return it }
        val bitmap = withContext(Dispatchers.IO) {
            OutfitRenderer.render(placements, widthPx, heightPx)
        }
        if (bitmap != null) cache.put(outfitId, bitmap)
        return bitmap
    }
}

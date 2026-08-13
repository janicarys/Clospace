package com.mobdeve.s15.reyes.janicamegan.clospace

data class OutfitPlacement(
    val item: ClothingItem,
    val x: Float,
    val y: Float,
    val scale: Float,
    val layer: Int,
    // Canvas aspect ratio (height / width) the outfit was composed in, so
    // previews preserve the tall portrait layout instead of a square space.
    val canvasRatio: Float = 1f
)

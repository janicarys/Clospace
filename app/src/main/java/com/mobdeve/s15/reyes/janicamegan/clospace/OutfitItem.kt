package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.Entity

@Entity(
    tableName = "outfit_items",
    primaryKeys = ["outfitId", "clothingId"]
)
data class OutfitItem(

    val outfitId: Int,

    val clothingId: Int,

    // Normalized position and scale on the canvas (0.0 - 1.0 for x/y)
    val x: Float = 0.5f,

    val y: Float = 0.5f,

    val scale: Float = 1f,

    // Layer order, 0 = bottom
    val layer: Int = 0,

    // Canvas aspect (height / width) this placement was composed in; previews
    // use it to reproduce the tall portrait canvas layout.
    val canvasRatio: Float = 1f
)

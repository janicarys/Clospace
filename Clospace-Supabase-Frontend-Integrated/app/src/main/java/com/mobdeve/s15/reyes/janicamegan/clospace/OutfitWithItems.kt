package com.mobdeve.s15.reyes.janicamegan.clospace

data class OutfitWithItems(
    val outfit: Outfit,
    val placements: List<OutfitPlacement> = emptyList()
)

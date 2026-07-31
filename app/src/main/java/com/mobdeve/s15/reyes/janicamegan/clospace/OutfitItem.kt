package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.Entity

@Entity(primaryKeys = ["outfitId", "clothingId"])
data class OutfitItem(

    val outfitId: Int,

    val clothingId: Int
)
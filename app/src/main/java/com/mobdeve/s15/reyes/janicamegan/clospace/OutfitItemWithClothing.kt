package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.Embedded

data class OutfitItemWithClothing(

    @Embedded
    val placement: OutfitItem,

    @Embedded
    val clothing: ClothingItem
)

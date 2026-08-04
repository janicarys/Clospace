package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfits")
data class Outfit(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val ownerId: Int,

    val caption: String,

    val occasion: String,

    val plannedDate: String?
)

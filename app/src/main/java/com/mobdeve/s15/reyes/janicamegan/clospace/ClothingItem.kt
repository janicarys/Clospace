package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clothing_items")
data class ClothingItem(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val ownerId: Int,

    val name: String,

    val category: String,

    val color: String,

    val material: String,

    val tags: String,

    val imagePath: String,

    val timesWorn: Int = 0
)
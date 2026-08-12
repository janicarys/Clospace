package com.mobdeve.s15.reyes.janicamegan.clospace

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar")
data class CalendarEntry(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val ownerId: Int,

    val date: String,

    val outfitId: Int
)
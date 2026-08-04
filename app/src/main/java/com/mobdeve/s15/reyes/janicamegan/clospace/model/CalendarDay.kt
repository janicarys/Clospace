package com.mobdeve.s15.reyes.janicamegan.clospace.model

data class CalendarDay(

    val dayNumber: Int,

    val isCurrentMonth: Boolean,

    val outfits: MutableList<Outfit> = mutableListOf()

)
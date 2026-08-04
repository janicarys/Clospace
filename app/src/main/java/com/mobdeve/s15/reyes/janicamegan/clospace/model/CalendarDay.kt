package com.mobdeve.s15.reyes.janicamegan.clospace.model

import java.time.LocalDate

data class CalendarDay(

    val date: LocalDate,

    val isCurrentMonth: Boolean,
    val isToday: Boolean,

    val outfits: MutableList<Outfit> = mutableListOf()

)
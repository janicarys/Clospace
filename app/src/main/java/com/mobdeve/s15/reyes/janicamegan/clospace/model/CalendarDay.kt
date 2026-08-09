package com.mobdeve.s15.reyes.janicamegan.clospace.model

import com.mobdeve.s15.reyes.janicamegan.clospace.Outfit
import java.time.LocalDate

data class CalendarDay(

    val date: LocalDate,

    val isCurrentMonth: Boolean,
    val isToday: Boolean,

    val outfits: List<Outfit> = emptyList()

)
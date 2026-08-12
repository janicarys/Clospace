package com.mobdeve.s15.reyes.janicamegan.clospace

import kotlinx.serialization.Serializable

@Serializable
data class Clothing(

    val id: Long = 0,

    val user_id: Int,

    val name: String,

    val category: String,

    val color: String,

    val season: String,

    val image_url: String = "",

    val favorite: Boolean = false
)
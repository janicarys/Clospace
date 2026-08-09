package com.mobdeve.s15.reyes.janicamegan.clospace.util

import com.mobdeve.s15.reyes.janicamegan.clospace.R

object TransitionUtil {

    const val TAB_CLOSET = 0
    const val TAB_OUTFIT = 1
    const val TAB_CALENDAR = 2
    const val TAB_SETTINGS = 3

    // Return (enter, exit) animation pair that simulates moving through
    // the app in the direction of the bottom navigation bar.
    fun slide(from: Int, to: Int): Pair<Int, Int> {

        return if (to > from) {
            R.anim.slide_in_right to R.anim.slide_out_left
        } else {
            R.anim.slide_in_left to R.anim.slide_out_right
        }
    }
}
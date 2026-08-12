package com.mobdeve.s15.reyes.janicamegan.clospace.util

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

/** Central helpers for applying window insets (status bar, cutout, gesture/nav bar). */
object InsetUtils {

    private fun bars(insets: WindowInsetsCompat) =
        insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )

    /** Pads the view on all four sides so content stays inside the safe area. */
    fun applySystemBars(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val b = bars(windowInsets)
            v.setPadding(b.left, b.top, b.right, b.bottom)
            windowInsets
        }
    }

    /** Pads top / left / right only (for screens that manage their own bottom edge). */
    fun applySystemBarsExceptBottom(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val b = bars(windowInsets)
            v.setPadding(b.left, b.top, b.right, 0)
            windowInsets
        }
    }

    /** Pads bottom / left / right only (top is handled by layout margins). */
    fun applySystemBarsExceptTop(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val b = bars(windowInsets)
            v.setPadding(b.left, 0, b.right, b.bottom)
            windowInsets
        }
    }

    /**
     * Grows a bottom bar by the bottom inset (so its background still covers the gesture area)
     * while pinning [contentHeight] px of interactive content to the top edge.
     */
    fun applyBottomBar(view: View, contentHeight: Int) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val b = bars(windowInsets)
            v.updateLayoutParams { this.height = contentHeight + b.bottom }
            (v as? LinearLayout)?.gravity = Gravity.TOP
            val group = v as android.view.ViewGroup
            for (i in 0 until group.childCount) {
                val childLp = group.getChildAt(i).layoutParams
                childLp.height = contentHeight
                group.getChildAt(i).layoutParams = childLp
            }
            windowInsets
        }
    }
}
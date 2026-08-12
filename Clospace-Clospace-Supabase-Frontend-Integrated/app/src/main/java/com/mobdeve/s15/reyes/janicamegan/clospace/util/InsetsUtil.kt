package com.mobdeve.s15.reyes.janicamegan.clospace.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Edge-to-edge helpers for Clospace.
 *
 * On API 35+ (Clospace targets 37) the system draws edge-to-edge by default and this can't be
 * opted out of, so any view that used to rely on a fixed `layout_marginBottom` to sit above the
 * nav bar / gesture inset needs to react to [WindowInsetsCompat] instead.
 *
 * Usage:
 *  - Call [applyBottomInsetAsMargin] on the bottom-most view in a screen (e.g. a "Sign In" row)
 *    to push it above the system bars while everything else stays where it is.
 *  - Call [applyBottomInsetAsPadding] on a scrolling container (e.g. inside a bottom sheet or a
 *    RecyclerView) so content can still scroll behind the bar but the last item / button isn't
 *    covered by it. Pair with `clipToPadding = false` on RecyclerViews so items are visible while
 *    scrolled into that padding.
 */
object InsetsUtil {

    /**
     * Adds the system bars' bottom inset (nav bar height / gesture inset) on top of whatever
     * bottom margin [view] already has in XML, instead of replacing it. Safe to call multiple
     * times / on view recycle since it captures the original margin once.
     */
    fun applyBottomInsetAsMargin(view: View) {
        val lp = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        val initialBottomMargin = lp.bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = initialBottomMargin + systemBars.bottom
            v.layoutParams = params
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    /**
     * Adds the system bars' bottom inset on top of whatever bottom padding [view] already has,
     * instead of replacing it. Use this for containers where content should be able to scroll
     * behind the bar (set `clipToPadding = false` on the container for that effect).
     */
    fun applyBottomInsetAsPadding(view: View) {
        val initialBottomPadding = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = initialBottomPadding + systemBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    /**
     * For top bars (e.g. a custom title/toolbar area) that should sit below the status bar.
     */
    fun applyTopInsetAsPadding(view: View) {
        val initialTopPadding = view.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = initialTopPadding + systemBars.top)
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }
    /**
     * For a fixed-height bottom bar (e.g. a custom bottom nav) where adding padding alone would
     * squeeze its content: grows the view's height by the inset and pushes that extra space in
     * as bottom padding, so the icon row keeps its original height/centering and the bar's
     * background simply extends further down to cover the gesture area.
     */
    fun applyBottomInsetAsHeightAndPadding(view: View) {
        val initialHeight = view.layoutParams.height
        val initialBottomPadding = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val lp = v.layoutParams
            lp.height = initialHeight + systemBars.bottom
            v.layoutParams = lp
            v.updatePadding(bottom = initialBottomPadding + systemBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }
}

/** Kotlin-friendly extensions so call sites read as `view.applyBottomInsetAsMargin()`. */
fun View.applyBottomInsetAsMargin() = InsetsUtil.applyBottomInsetAsMargin(this)
fun View.applyBottomInsetAsPadding() = InsetsUtil.applyBottomInsetAsPadding(this)
fun View.applyTopInsetAsPadding() = InsetsUtil.applyTopInsetAsPadding(this)
fun View.applyBottomInsetAsHeightAndPadding() = InsetsUtil.applyBottomInsetAsHeightAndPadding(this)

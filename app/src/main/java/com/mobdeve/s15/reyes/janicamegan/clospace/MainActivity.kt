package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

import com.mobdeve.s15.reyes.janicamegan.clospace.util.InsetUtils

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OPEN_TAB = "openTab"

        const val TAB_CLOSET = 0
        const val TAB_OUTFIT = 1
        const val TAB_CALENDAR = 2
        const val TAB_SETTINGS = 3
    }

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Keep the custom bottom nav above the system navigation bar (gesture / 3-button).
        InsetUtils.applyBottomBar(findViewById(R.id.layoutBottomNav), dp(72))

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = MainPagerAdapter()

        // Bottom Navigation
        setupNavTap(R.id.navCloset, TAB_CLOSET)
        setupNavTap(R.id.navOutfit, TAB_OUTFIT)
        setupNavTap(R.id.navCalendar, TAB_CALENDAR)
        setupNavTap(R.id.navSettings, TAB_SETTINGS)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                highlightTab(position)
            }
        })

        // Select the requested tab (defaults to Closet).
        selectTabFromIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        selectTabFromIntent(intent)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun selectTabFromIntent(intent: Intent) {

        val tab = intent.getIntExtra(EXTRA_OPEN_TAB, TAB_CLOSET)
        viewPager.setCurrentItem(tab, false)
        highlightTab(tab)
    }

    private fun setupNavTap(navId: Int, tab: Int) {

        findViewById<LinearLayout>(navId).setOnClickListener {
            viewPager.setCurrentItem(tab, true)
        }
    }

    private fun highlightTab(tab: Int) {

        setIndicator(R.id.indicatorCloset, tab == TAB_CLOSET)
        setIndicator(R.id.indicatorOutfit, tab == TAB_OUTFIT)
        setIndicator(R.id.indicatorCalendar, tab == TAB_CALENDAR)
        setIndicator(R.id.indicatorSettings, tab == TAB_SETTINGS)
    }

    private fun setIndicator(viewId: Int, visible: Boolean) {

        findViewById<View>(viewId).visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    private inner class MainPagerAdapter :
        FragmentStateAdapter(this) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int) = when (position) {

            TAB_CLOSET -> ClosetFragment()
            TAB_OUTFIT -> OutfitFragment()
            TAB_CALENDAR -> CalendarFragment()
            TAB_SETTINGS -> SettingsFragment()

            else -> ClosetFragment()
        }
    }
}
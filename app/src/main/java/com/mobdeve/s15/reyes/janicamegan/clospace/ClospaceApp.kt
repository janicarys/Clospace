package com.mobdeve.s15.reyes.janicamegan.clospace

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.mobdeve.s15.reyes.janicamegan.clospace.util.InsetUtils

class ClospaceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // Match the status bar to the app background so the content color
                // extends up behind it, with dark icons for contrast.
                colorStatusBar(activity)

                val content: View = activity.window.decorView.findViewById(android.R.id.content)
                    ?: return
                // MainActivity has its own bottom bar that extends into the gesture area,
                // so it manages the bottom inset itself; its top is handled by layout margins.
                if (activity is MainActivity) return
                // Top is left to the layout margins (matches how most apps keep the inset
                // area the same color); only pad the bottom / sides so controls stay clear
                // of the gesture bar in landscape.
                InsetUtils.applySystemBarsExceptTop(content)
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    /** Keeps the status bar the same color as the app background and uses dark icons.
     *  On API 35+ the bar is always transparent, so the content's background already
     *  extends behind it; this only colors the bar on older devices. */
    private fun colorStatusBar(activity: Activity) {
        val window = activity.window
        @Suppress("DEPRECATION")
        window.statusBarColor = ContextCompat.getColor(activity, R.color.light_blue)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }
}
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.perf

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import mozilla.components.concept.base.profiler.Profiler
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.perf.ProfilerMarkers.MEASURE_LAYOUT_DRAW_MARKER_NAME

/**
 * A [LinearLayout] that adds profiler markers for various methods and handles window insets
 * for edge-to-edge display support. This is intended to be used on the root view of [HomeActivity]'s
 * view hierarchy to understand global measure/layout events and properly handle system UI.
 */
class HomeActivityRootLinearLayout(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val profiler: Profiler? = context.components.core.engine.profiler

    init {
        // Set up window insets handling for edge-to-edge display on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setupWindowInsets()
        }
    }

    /**
     * Sets up window insets handling to ensure proper padding for system bars in edge-to-edge mode.
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply top padding for status bar and bottom padding for navigation bar
            // Keep original left/right padding to not interfere with existing layout
            view.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            
            // Return the insets unchanged for child views to handle if needed
            windowInsets
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val profilerStartTime = profiler?.getProfilerTime()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        profiler?.addMarker(MEASURE_LAYOUT_DRAW_MARKER_NAME, profilerStartTime, "onMeasure (HomeActivity root)")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val profilerStartTime = profiler?.getProfilerTime()
        super.onLayout(changed, l, t, r, b)
        profiler?.addMarker(MEASURE_LAYOUT_DRAW_MARKER_NAME, profilerStartTime, "onLayout (HomeActivity root)")
    }

    override fun dispatchDraw(canvas: Canvas) {
        // We instrument dispatchDraw, for drawing children, because LinearLayout never draws itself,
        // i.e. it never calls onDraw or draw.
        val profilerStartTime = profiler?.getProfilerTime()
        super.dispatchDraw(canvas)
        profiler?.addMarker(MEASURE_LAYOUT_DRAW_MARKER_NAME, profilerStartTime, "dispatchDraw (HomeActivity root)")
    }
}

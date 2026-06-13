package com.yu.ghostwidget

import android.app.Application
import com.google.android.material.color.DynamicColors

class GhostWidgetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply dynamic colors (Material You) to all activities in the app
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}

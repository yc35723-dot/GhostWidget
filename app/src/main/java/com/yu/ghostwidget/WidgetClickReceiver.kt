package com.yu.ghostwidget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.RemoteViews
import java.io.File

class WidgetClickReceiver : BroadcastReceiver() {

    companion object {
        private val handler = Handler(Looper.getMainLooper())
        private val pendingActions = mutableMapOf<Int, Runnable>()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == GhostWidgetProvider.ACTION_WIDGET_CLICK) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

            val currentTime = System.currentTimeMillis()
            val prefs = context.getSharedPreferences("GhostWidgetPrefs", Context.MODE_PRIVATE)
            val lastTime = prefs.getLong("last_click_time_$appWidgetId", 0)
            val userDelay = prefs.getInt("click_delay", 200).toLong()

            // Cancel any existing pending single click
            pendingActions[appWidgetId]?.let { 
                handler.removeCallbacks(it)
                pendingActions.remove(appWidgetId)
            }

            if (currentTime - lastTime < userDelay) {
                // Double tap detected
                prefs.edit().putLong("last_click_time_$appWidgetId", 0).apply()
                vibrate(context, doubleTap = true)
                handleDoubleTap(context, appWidgetId)
            } else {
                // Potential single tap
                prefs.edit().putLong("last_click_time_$appWidgetId", currentTime).apply()
                
                val runnable = Runnable {
                    vibrate(context, doubleTap = false)
                    handleSingleTap(context, appWidgetId)
                    pendingActions.remove(appWidgetId)
                }
                pendingActions[appWidgetId] = runnable
                handler.postDelayed(runnable, userDelay)
            }
        }
    }

    private fun vibrate(context: Context, doubleTap: Boolean) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (doubleTap) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 30), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 30, 40, 30), -1)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }

    private fun handleSingleTap(context: Context, appWidgetId: Int) {
        toggleImage(context, appWidgetId, "single_tap_image.png", "single_tap_visible")
    }

    private fun handleDoubleTap(context: Context, appWidgetId: Int) {
        toggleImage(context, appWidgetId, "double_tap_image.png", "double_tap_visible")
    }

    private fun toggleImage(context: Context, appWidgetId: Int, fileName: String, visibilityKey: String) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val views = RemoteViews(context.packageName, R.layout.ghost_widget)
        val prefs = context.getSharedPreferences("GhostWidgetPrefs", Context.MODE_PRIVATE)
        
        val isVisible = prefs.getBoolean("${visibilityKey}_$appWidgetId", false)
        val newVisibility = !isVisible

        if (newVisibility) {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                views.setImageViewBitmap(R.id.widget_image, bitmap)
                views.setViewVisibility(R.id.widget_image, View.VISIBLE)
                
                // If keep screen on is enabled, start the service
                if (prefs.getBoolean("keep_screen_on", false)) {
                    val serviceIntent = Intent(context, KeepScreenOnService::class.java)
                    context.startService(serviceIntent)
                }
            } else {
                views.setViewVisibility(R.id.widget_image, View.GONE)
                prefs.edit().putBoolean("${visibilityKey}_$appWidgetId", false).apply()
                return
            }
        } else {
            views.setViewVisibility(R.id.widget_image, View.GONE)
            // Stop service when image is hidden
            val serviceIntent = Intent(context, KeepScreenOnService::class.java)
            context.stopService(serviceIntent)
        }

        prefs.edit().putBoolean("${visibilityKey}_$appWidgetId", newVisibility).apply()
        
        if (newVisibility) {
            val otherKey = if (visibilityKey == "single_tap_visible") "double_tap_visible" else "single_tap_visible"
            prefs.edit().putBoolean("${otherKey}_$appWidgetId", false).apply()
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

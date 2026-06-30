package com.yu.ghostwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File

class GhostWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            renderWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("GhostWidgetDebug", "Widget enabled, starting GhostClickService")
        val serviceIntent = Intent(context, GhostClickService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d("GhostWidgetDebug", "Last widget disabled, stopping GhostClickService")
        val serviceIntent = Intent(context, GhostClickService::class.java)
        context.stopService(serviceIntent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GhostWidgetProvider::class.java))
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("GhostWidgetDebug", "Boot completed received, refreshing widgets")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GhostWidgetProvider::class.java))
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
            ACTION_RESET_ALL -> {
                resetAllWidgets(context)
            }
        }
    }

    private fun resetAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GhostWidgetProvider::class.java))
        val prefs = context.getSharedPreferences("GhostWidgetPrefs", Context.MODE_PRIVATE)
        
        val editor = prefs.edit()
        for (appWidgetId in appWidgetIds) {
            editor.putBoolean("single_tap_visible_$appWidgetId", false)
            editor.putBoolean("double_tap_visible_$appWidgetId", false)
            renderWidget(context, appWidgetManager, appWidgetId)
        }
        editor.commit() // Use commit for synchronization as per requirement

        // Stop keep screen on service when all hidden
        val serviceIntent = Intent(context, KeepScreenOnService::class.java)
        context.stopService(serviceIntent)

        Toast.makeText(context, context.getString(R.string.toast_reset_all), Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val ACTION_WIDGET_CLICK = "com.yu.ghostwidget.WIDGET_CLICK"
        const val ACTION_RESET_ALL = "com.yu.ghostwidget.RESET_ALL"
        private const val TAG = "GhostWidgetDebug"

        fun renderWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            try {
                val prefs = context.getSharedPreferences("GhostWidgetPrefs", Context.MODE_PRIVATE)
                var singleVisible = prefs.getBoolean("single_tap_visible_$appWidgetId", false)
                var doubleVisible = prefs.getBoolean("double_tap_visible_$appWidgetId", false)

                // Enforce single tap priority if both are true (shouldn't happen with correct logic)
                if (singleVisible && doubleVisible) {
                    doubleVisible = false
                    prefs.edit().putBoolean("double_tap_visible_$appWidgetId", false).commit()
                }

                val views = RemoteViews(context.packageName, R.layout.ghost_widget)

                // Click Intent setup
                val clickIntent = Intent(context, GhostClickService::class.java).apply {
                    action = ACTION_WIDGET_CLICK
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PendingIntent.getForegroundService(context, appWidgetId, clickIntent, flags)
                } else {
                    PendingIntent.getService(context, appWidgetId, clickIntent, flags)
                }

                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                // Rendering Logic
                val visible = singleVisible || doubleVisible
                val fileName = if (singleVisible) "single_tap_image.png" else "double_tap_image.png"

                Log.d(TAG, "Rendering widget $appWidgetId: visible=$visible, file=$fileName")

                if (visible) {
                    val file = File(context.filesDir, fileName)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        views.setImageViewBitmap(R.id.widget_image, bitmap)
                        views.setViewVisibility(R.id.widget_image, View.VISIBLE)
                    } else {
                        // Image missing, fallback to hidden
                        Log.w(TAG, "Image file $fileName missing for widget $appWidgetId")
                        views.setImageViewResource(R.id.widget_image, 0)
                        views.setViewVisibility(R.id.widget_image, View.GONE)
                        
                        // Sync status back to prefs
                        val key = if (singleVisible) "single_tap_visible_$appWidgetId" else "double_tap_visible_$appWidgetId"
                        prefs.edit().putBoolean(key, false).commit()
                    }
                } else {
                    views.setImageViewResource(R.id.widget_image, 0)
                    views.setViewVisibility(R.id.widget_image, View.GONE)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d(TAG, "Successfully updated widget $appWidgetId")
            } catch (e: Exception) {
                Log.e(TAG, "Error rendering widget $appWidgetId", e)
            }
        }

        // Keep for backward compatibility or internal usage if needed, but redirects to renderWidget
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            renderWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

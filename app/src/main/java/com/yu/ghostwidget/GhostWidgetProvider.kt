package com.yu.ghostwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast

class GhostWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GhostWidgetProvider::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)
        } else if (intent.action == ACTION_RESET_ALL) {
            resetAllWidgets(context)
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
            
            val views = RemoteViews(context.packageName, R.layout.ghost_widget)
            views.setViewVisibility(R.id.widget_image, android.view.View.GONE)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        editor.apply()

        // Stop service when all hidden
        val serviceIntent = Intent(context, KeepScreenOnService::class.java)
        context.stopService(serviceIntent)

        Toast.makeText(context, context.getString(R.string.toast_reset_all), Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val ACTION_WIDGET_CLICK = "com.yu.ghostwidget.WIDGET_CLICK"
        const val ACTION_RESET_ALL = "com.yu.ghostwidget.RESET_ALL"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.ghost_widget)

            val intent = Intent(context, WidgetClickReceiver::class.java).apply {
                action = ACTION_WIDGET_CLICK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                appWidgetId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            
            // Initial state: hide image (fully transparent)
            views.setViewVisibility(R.id.widget_image, android.view.View.GONE)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

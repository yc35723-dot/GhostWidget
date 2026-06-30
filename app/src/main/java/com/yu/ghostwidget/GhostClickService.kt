package com.yu.ghostwidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat

class GhostClickService : Service() {

    companion object {
        private const val TAG = "GhostWidgetDebug"
        private const val CHANNEL_ID = "ghost_widget_service"
        private const val NOTIFICATION_ID = 1
        
        private val handler = Handler(Looper.getMainLooper())
        private val pendingActions = mutableMapOf<Int, Runnable>()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == GhostWidgetProvider.ACTION_WIDGET_CLICK) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val clickTime = SystemClock.elapsedRealtime()
            
            Log.d(TAG, "[PID: ${Process.myPid()}] Received click for widget $appWidgetId at $clickTime")

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                handleWidgetClick(appWidgetId, clickTime)
            }
        }
        return START_STICKY
    }

    private fun handleWidgetClick(appWidgetId: Int, clickTime: Long) {
        val prefs = getSharedPreferences("GhostWidgetPrefs", Context.MODE_PRIVATE)
        val lastTime = prefs.getLong("last_click_time_$appWidgetId", 0)
        val userDelay = prefs.getInt("click_delay", 200).toLong()

        // Cancel any existing pending single click
        pendingActions[appWidgetId]?.let {
            handler.removeCallbacks(it)
            pendingActions.remove(appWidgetId)
        }

        if (clickTime - lastTime < userDelay) {
            // Double tap detected
            Log.d(TAG, "Double tap detected for widget $appWidgetId")
            prefs.edit().putLong("last_click_time_$appWidgetId", 0).commit()
            vibrate(doubleTap = true)
            handleDoubleTap(appWidgetId)
        } else {
            // Potential single tap
            prefs.edit().putLong("last_click_time_$appWidgetId", clickTime).commit()

            val runnable = Runnable {
                Log.d(TAG, "Single tap confirmed for widget $appWidgetId")
                vibrate(doubleTap = false)
                handleSingleTap(appWidgetId)
                pendingActions.remove(appWidgetId)
            }
            pendingActions[appWidgetId] = runnable
            handler.postDelayed(runnable, userDelay)
        }
    }

    private fun handleSingleTap(appWidgetId: Int) {
        toggleImage(appWidgetId, "single_tap_visible")
    }

    private fun handleDoubleTap(appWidgetId: Int) {
        toggleImage(appWidgetId, "double_tap_visible")
    }

    private fun toggleImage(appWidgetId: Int, visibilityKey: String) {
        val prefs = getSharedPreferences("GhostWidgetPrefs", Context.MODE_PRIVATE)
        val fullKey = "${visibilityKey}_$appWidgetId"
        val isVisible = prefs.getBoolean(fullKey, false)
        val newVisibility = !isVisible

        Log.d(TAG, "Toggling $visibilityKey for widget $appWidgetId: $isVisible -> $newVisibility")

        val editor = prefs.edit()
        editor.putBoolean(fullKey, newVisibility)
        
        if (newVisibility) {
            // If turning on, ensure the other visibility flag is off
            val otherKey = if (visibilityKey == "single_tap_visible") "double_tap_visible" else "single_tap_visible"
            editor.putBoolean("${otherKey}_$appWidgetId", false)
            
            // Handle keep screen on
            if (prefs.getBoolean("keep_screen_on", false)) {
                val serviceIntent = Intent(this, KeepScreenOnService::class.java)
                startService(serviceIntent)
            }
        } else {
            // Turning off. Only stop KeepScreenOnService if both are now off
            val otherKey = if (visibilityKey == "single_tap_visible") "double_tap_visible" else "single_tap_visible"
            val otherVisible = prefs.getBoolean("${otherKey}_$appWidgetId", false)
            if (!otherVisible) {
                val serviceIntent = Intent(this, KeepScreenOnService::class.java)
                stopService(serviceIntent)
            }
        }
        
        editor.commit()

        val appWidgetManager = AppWidgetManager.getInstance(this)
        GhostWidgetProvider.renderWidget(this, appWidgetManager, appWidgetId)
    }

    private fun vibrate(doubleTap: Boolean) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "GhostWidget Service"
            val descriptionText = "Ensures widget interaction responsiveness"
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_ghost)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("GhostWidget 運作中")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSilent(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

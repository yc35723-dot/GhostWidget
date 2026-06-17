package com.yu.ghostwidget

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ResetShortcutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Trigger the reset broadcast
        val intent = Intent(this, GhostWidgetProvider::class.java).apply {
            action = GhostWidgetProvider.ACTION_RESET_ALL
        }
        sendBroadcast(intent)
        
        // Finish immediately so it's invisible to the user
        finish()
    }
}

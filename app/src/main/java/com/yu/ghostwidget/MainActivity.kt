package com.yu.ghostwidget

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import java.io.File
import java.util.Locale
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var singleLabel: TextView
    private lateinit var doubleLabel: TextView
    private lateinit var singlePreview: ImageView
    private lateinit var doublePreview: ImageView
    private lateinit var delayValueText: TextView
    private lateinit var delaySlider: Slider
    private lateinit var keepScreenSwitch: MaterialSwitch

    private val pickSingleImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { handleImageSelection(it, "single_tap_image.png") }
    }

    private val pickDoubleImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { handleImageSelection(it, "double_tap_image.png") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkNotificationPermission()

        singleLabel = findViewById(R.id.single_tap_label)
        doubleLabel = findViewById(R.id.double_tap_label)
        singlePreview = findViewById(R.id.single_tap_preview)
        doublePreview = findViewById(R.id.double_tap_preview)
        delayValueText = findViewById(R.id.delay_value)
        delaySlider = findViewById(R.id.delay_slider)
        keepScreenSwitch = findViewById(R.id.keep_screen_switch)

        val prefs = getSharedPreferences("GhostWidgetPrefs", Context.MODE_PRIVATE)

        // Setup Slider
        val currentDelay = prefs.getInt("click_delay", 200).coerceAtLeast(150)
        delaySlider.value = currentDelay.toFloat()
        delayValueText.text = "${currentDelay}ms"
        delaySlider.addOnChangeListener { _, value, _ ->
            val delay = value.toInt()
            delayValueText.text = "${delay}ms"
            prefs.edit().putInt("click_delay", delay).apply()
        }

        // Setup Switch
        keepScreenSwitch.isChecked = prefs.getBoolean("keep_screen_on", false)
        keepScreenSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("keep_screen_on", isChecked).apply()
        }

        findViewById<Button>(R.id.btn_select_single).setOnClickListener {
            pickSingleImage.launch(arrayOf("image/*"))
        }

        findViewById<Button>(R.id.btn_select_double).setOnClickListener {
            pickDoubleImage.launch(arrayOf("image/*"))
        }

        findViewById<Button>(R.id.btn_reset_single).setOnClickListener {
            resetImage("single_tap_image.png")
        }

        findViewById<Button>(R.id.btn_reset_double).setOnClickListener {
            resetImage("double_tap_image.png")
        }

        findViewById<Button>(R.id.btn_reset_all).setOnClickListener {
            resetAllWidgets()
        }

        findViewById<Button>(R.id.btn_instructions).setOnClickListener {
            showInstructionsDialog()
        }

        findViewById<Button>(R.id.btn_github).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yc35723-dot"))
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btn_language).setOnClickListener {
            showLanguageMenu()
        }

        updateUI()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (!isGranted) {
                        // User denied, service will still work but notification won't show
                    }
                }.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun handleImageSelection(uri: Uri, fileName: String) {
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                val file = File(filesDir, fileName)
                FileOutputStream(file).use { out ->
                    originalBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(this, getString(R.string.toast_image_saved), Toast.LENGTH_SHORT).show()
                updateUI()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.toast_image_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetImage(fileName: String) {
        val file = File(filesDir, fileName)
        if (file.exists()) {
            file.delete()
            Toast.makeText(this, getString(R.string.toast_image_removed), Toast.LENGTH_SHORT).show()
            updateUI()
        } else {
            Toast.makeText(this, getString(R.string.toast_no_image), Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetAllWidgets() {
        val intent = Intent(this, GhostWidgetProvider::class.java).apply {
            action = GhostWidgetProvider.ACTION_RESET_ALL
        }
        sendBroadcast(intent)
    }

    private fun updateUI() {
        val singleFile = File(filesDir, "single_tap_image.png")
        if (singleFile.exists()) {
            singleLabel.text = getString(R.string.single_tap_selected)
            singlePreview.visibility = View.VISIBLE
            singlePreview.setImageBitmap(BitmapFactory.decodeFile(singleFile.absolutePath))
        } else {
            singleLabel.text = getString(R.string.single_tap_label)
            singlePreview.visibility = View.GONE
        }

        val doubleFile = File(filesDir, "double_tap_image.png")
        if (doubleFile.exists()) {
            doubleLabel.text = getString(R.string.double_tap_selected)
            doublePreview.visibility = View.VISIBLE
            doublePreview.setImageBitmap(BitmapFactory.decodeFile(doubleFile.absolutePath))
        } else {
            doubleLabel.text = getString(R.string.double_tap_label)
            doublePreview.visibility = View.GONE
        }
    }

    private fun showInstructionsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_instructions_title))
            .setMessage(getString(R.string.dialog_instructions_content))
            .setPositiveButton(getString(R.string.dialog_ok), null)
            .show()
    }

    private fun showLanguageMenu() {
        val languages = arrayOf("繁體中文", "简体中文", "English")
        val locales = arrayOf("zh-TW", "zh-CN", "en")

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.title_settings))
            .setItems(languages) { _, which ->
                val localeList = LocaleListCompat.forLanguageTags(locales[which])
                AppCompatDelegate.setApplicationLocales(localeList)
            }
            .show()
    }
}

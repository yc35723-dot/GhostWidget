package com.yu.ghostwidget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
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
import java.io.File
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

        findViewById<Button>(R.id.btn_instructions).setOnClickListener {
            showInstructionsDialog()
        }

        findViewById<Button>(R.id.btn_github).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yc35723-dot"))
            startActivity(intent)
        }

        updateUI()
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
                Toast.makeText(this, "圖片已儲存", Toast.LENGTH_SHORT).show()
                updateUI()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "圖片載入失敗", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetImage(fileName: String) {
        val file = File(filesDir, fileName)
        if (file.exists()) {
            file.delete()
            Toast.makeText(this, "圖片已移除", Toast.LENGTH_SHORT).show()
            updateUI()
        } else {
            Toast.makeText(this, "目前未選擇圖片", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        val singleFile = File(filesDir, "single_tap_image.png")
        if (singleFile.exists()) {
            singleLabel.text = "單擊圖片 (已選擇)"
            singlePreview.visibility = View.VISIBLE
            singlePreview.setImageBitmap(BitmapFactory.decodeFile(singleFile.absolutePath))
        } else {
            singleLabel.text = "單擊圖片"
            singlePreview.visibility = View.GONE
        }

        val doubleFile = File(filesDir, "double_tap_image.png")
        if (doubleFile.exists()) {
            doubleLabel.text = "雙擊圖片 (已選擇)"
            doublePreview.visibility = View.VISIBLE
            doublePreview.setImageBitmap(BitmapFactory.decodeFile(doubleFile.absolutePath))
        } else {
            doubleLabel.text = "雙擊圖片"
            doublePreview.visibility = View.GONE
        }
    }

    private fun showInstructionsDialog() {
        val instructions = "1. 選擇單擊與雙擊時顯示的圖片。\n" +
                "2. 回到桌面，長按空白處選擇「小工具」。\n" +
                "3. 找到「GhostWidget」並加入桌面。\n" +
                "4. 點擊或雙擊 Widget 即可切換圖片顯示。\n" +
                "5. 注意，本 app 之 widget 為完全透明，雙擊或單擊後才會顯示圖片，再次雙擊或單擊即可關閉。\n" +
                "6. 本 app widget 亦適用於三星 Good Lock 插件 LockStar ，可在鎖定螢幕新增此 widget ，適合有強迫症的你，可以拿來放載具條碼。"
        
        MaterialAlertDialogBuilder(this)
            .setTitle("使用說明")
            .setMessage(instructions)
            .setPositiveButton("確定", null)
            .show()
    }
}

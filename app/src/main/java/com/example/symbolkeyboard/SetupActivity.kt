package com.example.symbolkeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        layout.addView(TextView(this).apply {
            text = "Symbol Keyboard\n\n1) Enable it in system settings\n2) Switch to it from any text field"
            textSize = 16f
        })

        val enableBtn = Button(this).apply {
            text = "Step 1: Enable keyboard"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        val switchBtn = Button(this).apply {
            text = "Step 2: Switch keyboard"
            setOnClickListener {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }

        layout.addView(enableBtn)
        layout.addView(switchBtn)
        setContentView(layout)
    }
}

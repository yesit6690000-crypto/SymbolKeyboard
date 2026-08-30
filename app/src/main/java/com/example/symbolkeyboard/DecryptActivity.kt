package com.example.symbolkeyboard

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class DecryptActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(Color.parseColor("#1E1F22"))
        }

        val title = TextView(this).apply {
            text = "Decrypt a message"
            setTextColor(Color.WHITE)
            textSize = 20f
        }

        val input = EditText(this).apply {
            hint = "Paste symbol text here"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            minLines = 4
        }

        val decryptBtn = Button(this).apply { text = "Decrypt" }

        val output = TextView(this).apply {
            setTextColor(Color.parseColor("#8AB4F8"))
            textSize = 16f
            setPadding(0, 40, 0, 0)
        }

        decryptBtn.setOnClickListener {
            output.text = SymbolCipher.decode(input.text.toString())
        }

        root.addView(title)
        root.addView(input)
        root.addView(decryptBtn)
        root.addView(output)
        setContentView(root)
    }
}

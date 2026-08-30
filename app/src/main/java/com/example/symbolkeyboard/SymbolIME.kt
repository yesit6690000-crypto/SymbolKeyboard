package com.example.symbolkeyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SymbolIME : InputMethodService() {

    private lateinit var decodeStrip: TextView

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            setPadding(12, 12, 12, 12)
        }

        // --- Decode preview strip ---
        decodeStrip = TextView(this).apply {
            text = "Type to see decoded text…"
            setTextColor(Color.parseColor("#4FC3F7"))
            textSize = 15f
            setPadding(20, 16, 20, 16)
            setBackgroundColor(Color.parseColor("#2A2A2A"))
            gravity = Gravity.START
        }
        root.addView(
            decodeStrip,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { bottomMargin = 12 }
        )

        // --- Letter rows, each key shows the SYMBOL big and the letter small ---
        SymbolCipher.rows.forEachIndexed { rowIndex, row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val sidePad = rowIndex * 24
                setPadding(sidePad, 6, sidePad, 6)
            }
            row.forEach { letter ->
                rowLayout.addView(buildKey(letter), LinearLayout.LayoutParams(0, 130, 1f).apply {
                    marginStart = 4; marginEnd = 4
                })
            }
            root.addView(rowLayout)
        }

        // --- Bottom row: 123 placeholder, space, backspace, enter ---
        val bottomRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

        val spaceKey = Button(this).apply {
            text = "space"
            setOnClickListener { commitRaw(" ") }
        }
        val backspaceKey = Button(this).apply {
            text = "⌫"
            setOnClickListener { deleteLast() }
        }
        val enterKey = Button(this).apply {
            text = "⏎"
            setOnClickListener { commitRaw("\n") }
        }

        bottomRow.addView(spaceKey, LinearLayout.LayoutParams(0, 130, 5f))
        bottomRow.addView(backspaceKey, LinearLayout.LayoutParams(0, 130, 2f))
        bottomRow.addView(enterKey, LinearLayout.LayoutParams(0, 130, 2f))
        root.addView(bottomRow)

        return root
    }

    private fun buildKey(letter: Char): Button {
        val symbol = SymbolCipher.symbolFor(letter)
        return Button(this).apply {
            text = "$symbol\n$letter"
            isAllCaps = false
            textSize = 13f
            setOnClickListener { commitRaw(symbol) }
        }
    }

    /** Commits raw text (a symbol, space, or newline) into whatever app is focused. */
    private fun commitRaw(text: String) {
        currentInputConnection?.commitText(text, 1)
        refreshDecodeStrip()
    }

    private fun deleteLast() {
        currentInputConnection?.deleteSurroundingText(1, 0)
        refreshDecodeStrip()
    }

    /** Pulls recent text before the cursor and shows it decoded back to English. */
    private fun refreshDecodeStrip() {
    if (!::decodeStrip.isInitialized) return
    val before = currentInputConnection?.getTextBeforeCursor(200, 0)?.toString() ?: ""
    decodeStrip.text = if (before.isEmpty()) {
        "Type to see decoded text…"
    } else {
        SymbolCipher.decode(before)
    }
}
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        refreshDecodeStrip()
    }
}

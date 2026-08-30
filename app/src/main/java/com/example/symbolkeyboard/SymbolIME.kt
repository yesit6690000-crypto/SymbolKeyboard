package com.example.symbolkeyboard

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
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
    private lateinit var eyeToggle: TextView
    private var isDecodeVisible = true

    // ---- Gboard-ish color palette ----
    private val keyColor = Color.parseColor("#303134")
    private val keyPressedColor = Color.parseColor("#5F6368")
    private val accentColor = Color.parseColor("#8AB4F8")
    private val bgColor = Color.parseColor("#1E1E1E")
    private val stripBgColor = Color.parseColor("#2A2A2A")

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
            setPadding(10, 10, 10, 10)
        }

        // --- Decode preview strip + eye toggle ---
        val stripRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(stripBgColor)
            val r = 20
            background = GradientDrawable().apply {
                cornerRadius = r.toFloat()
                setColor(stripBgColor)
            }
            setPadding(24, 20, 16, 20)
        }

        decodeStrip = TextView(this).apply {
            text = "Type to see decoded text…"
            setTextColor(accentColor)
            textSize = 15f
            gravity = Gravity.START
        }
        stripRow.addView(
            decodeStrip,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )

        eyeToggle = TextView(this).apply {
            text = "👁"
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(20, 8, 8, 8)
            setOnClickListener {
                isDecodeVisible = !isDecodeVisible
                text = if (isDecodeVisible) "👁" else "🙈"
                refreshDecodeStrip()
            }
        }
        stripRow.addView(eyeToggle)

        root.addView(
            stripRow,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { bottomMargin = 14 }
        )

        // --- Letter rows ---
        SymbolCipher.rows.forEachIndexed { rowIndex, row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val sidePad = rowIndex * 26
                setPadding(sidePad, 5, sidePad, 5)
            }
            row.forEach { letter ->
                rowLayout.addView(buildKey(letter), LinearLayout.LayoutParams(0, 132, 1f).apply {
                    marginStart = 4; marginEnd = 4
                })
            }
            root.addView(rowLayout)
        }

        // --- Bottom row: 123 placeholder, space, backspace, enter ---
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 5, 0, 5)
        }

        val numKey = styledButton("?123", isSpecial = true).apply {
            textSize = 13f
        }
        val spaceKey = styledButton("space", isSpecial = true).apply {
            setOnClickListener { commitRaw(" ") }
        }
        val backspaceKey = styledButton("⌫", isSpecial = true).apply {
            setOnClickListener { deleteLast() }
        }
        val enterKey = styledButton("⏎", isAccent = true).apply {
            setOnClickListener { commitRaw("\n") }
        }

        bottomRow.addView(numKey, edgeParams(1.4f))
        bottomRow.addView(spaceKey, edgeParams(4.5f))
        bottomRow.addView(backspaceKey, edgeParams(1.6f))
        bottomRow.addView(enterKey, edgeParams(1.6f))
        root.addView(bottomRow)

        return root
    }

    private fun edgeParams(weight: Float) =
        LinearLayout.LayoutParams(0, 132, weight).apply { marginStart = 4; marginEnd = 4 }

    private fun buildKey(letter: Char): Button {
        val symbol = SymbolCipher.symbolFor(letter)
        return Button(this).apply {
            text = "$symbol\n$letter"
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = keyDrawable(keyColor, keyPressedColor)
            stateListAnimator = null
            setPadding(0, 0, 0, 0)
            elevation = 0f
            setOnClickListener { commitRaw(symbol) }
        }
    }

    private fun styledButton(label: String, isSpecial: Boolean = false, isAccent: Boolean = false): Button {
        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 15f
            setTextColor(Color.WHITE)
            val base = if (isAccent) accentColor else keyColor
            val pressed = if (isAccent) Color.parseColor("#AECBFA") else keyPressedColor
            background = keyDrawable(base, pressed)
            stateListAnimator = null
            elevation = 0f
        }
    }

    private fun keyDrawable(normalColor: Int, pressedColor: Int): StateListDrawable {
        val radius = 18f
        val normal = GradientDrawable().apply {
            cornerRadius = radius
            setColor(normalColor)
        }
        val pressed = GradientDrawable().apply {
            cornerRadius = radius
            setColor(pressedColor)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
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

    /** Pulls recent text before the cursor and shows it decoded — or masked if hidden. */
    private fun refreshDecodeStrip() {
        if (!::decodeStrip.isInitialized) return
        val before = currentInputConnection?.getTextBeforeCursor(200, 0)?.toString() ?: ""
        decodeStrip.text = when {
            before.isEmpty() -> "Type to see decoded text…"
            !isDecodeVisible -> "•".repeat(before.length.coerceAtMost(40))
            else -> SymbolCipher.decode(before)
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        refreshDecodeStrip()
    }
}

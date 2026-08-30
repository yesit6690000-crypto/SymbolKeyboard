package com.example.symbolkeyboard

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class SymbolIME : InputMethodService() {

    private lateinit var decodeStrip: TextView
    private lateinit var keyboardContainer: LinearLayout

    private var isShift = false
    private var isCapsLock = false
    private var isDecodeHidden = false
    private var layer = Layer.LETTERS
    private var lastShiftTap = 0L

    private enum class Layer { LETTERS, NUMBERS, SYMBOLS }

    private val letterRows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
    private val numberRow = "1234567890"
    private val symbolRow1 = "@#£_&-+():;"
    private val symbolRow2 = "*\"'/~!?"

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1E1F22"))
            setPadding(dp(6), dp(6), dp(6), dp(6))
        }
        root.addView(buildDecodeStrip())
        keyboardContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(keyboardContainer)
        rebuildKeyboard()
        return root
    }

    override fun onStartInput(info: EditorInfo?, restarting: Boolean) {
        super.onStartInput(info, restarting)
        refreshDecodeStrip()
    }

    // ---------- Decode strip ----------

    private fun buildDecodeStrip(): LinearLayout {
        val strip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded("#303134", dp(20))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dp(8) }
        }
        decodeStrip = TextView(this).apply {
            text = "Type to see decoded text…"
            setTextColor(Color.parseColor("#8AB4F8"))
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val decryptBtn = TextView(this).apply {
            text = "🔓"
            textSize = 18f
            setPadding(dp(6), 0, dp(10), 0)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                val intent = Intent(this@SymbolIME, DecryptActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        val eye = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_view)
            background = null
            setColorFilter(Color.parseColor("#8AB4F8"))
            setOnClickListener { isDecodeHidden = !isDecodeHidden; refreshDecodeStrip() }
        }
        strip.addView(decodeStrip)
        strip.addView(decryptBtn)
        strip.addView(eye)
        return strip
    }

    private fun refreshDecodeStrip() {
        if (!::decodeStrip.isInitialized) return
        val before = currentInputConnection?.getTextBeforeCursor(200, 0)?.toString() ?: ""
        decodeStrip.text = when {
            before.isEmpty() -> "Type to see decoded text…"
            isDecodeHidden -> "•".repeat(before.length)
            else -> SymbolCipher.decode(before)
        }
    }

    // ---------- Layer building ----------

    private fun rebuildKeyboard() {
        keyboardContainer.removeAllViews()
        when (layer) {
            Layer.LETTERS -> buildLetterLayer()
            Layer.NUMBERS -> buildNumberLayer()
            Layer.SYMBOLS -> buildSymbolLayer()
        }
    }

    private fun buildLetterLayer() {
        letterRows.forEachIndexed { index, row ->
            val rowLayout = rowContainer()
            if (index == 1) rowLayout.setPadding(dp(18), 0, dp(18), 0)
            if (index == 2) rowLayout.addView(shiftKey(), keyParams(1.4f))
            row.forEach { c -> rowLayout.addView(letterKey(c), keyParams(1f)) }
            if (index == 2) rowLayout.addView(backspaceKey(), keyParams(1.4f))
            keyboardContainer.addView(rowLayout)
        }
        keyboardContainer.addView(bottomRow())
    }

    private fun buildNumberLayer() {
        val row1 = rowContainer()
        numberRow.forEach { c -> row1.addView(symbolKey(c), keyParams(1f)) }
        keyboardContainer.addView(row1)

        val row2 = rowContainer()
        symbolRow1.forEach { c -> row2.addView(symbolKey(c), keyParams(1f)) }
        keyboardContainer.addView(row2)

        val row3 = rowContainer()
        row3.addView(layerToggleKey("#+="), keyParams(1.4f))
        symbolRow2.forEach { c -> row3.addView(symbolKey(c), keyParams(1f)) }
        row3.addView(backspaceKey(), keyParams(1.4f))
        keyboardContainer.addView(row3)

        keyboardContainer.addView(bottomRow())
    }

    private fun buildSymbolLayer() = buildNumberLayer()

    // ---------- Key builders ----------

    private fun letterKey(c: Char): View {
        val displayLetter = if (isShift || isCapsLock) c.uppercaseChar() else c
        val symbol = SymbolCipher.encodeChar(displayLetter)
        val key = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rippleKeyBg()
            isClickable = true; isFocusable = true
        }
        key.addView(TextView(this).apply {
            text = symbol; setTextColor(Color.WHITE); textSize = 18f; gravity = Gravity.CENTER
        })
        key.addView(TextView(this).apply {
            text = displayLetter.toString(); setTextColor(Color.parseColor("#8A8A8E")); textSize = 10f
            gravity = Gravity.CENTER
        })
        key.setOnClickListener {
            currentInputConnection?.commitText(SymbolCipher.encodeChar(displayLetter), 1)
            if (isShift && !isCapsLock) { isShift = false; rebuildKeyboard() }
            refreshDecodeStrip()
        }
        return key
    }

    private fun symbolKey(c: Char): View {
        val symbol = SymbolCipher.encodeChar(c)
        val key = simpleKey(symbol)
        key.setOnClickListener {
            currentInputConnection?.commitText(symbol, 1)
            refreshDecodeStrip()
        }
        return key
    }

    private fun shiftKey(): View {
        val bg = if (isCapsLock) "#8AB4F8" else if (isShift) "#5F6368" else "#303134"
        val key = simpleKey(if (isCapsLock) "⇪" else "⇧")
        key.background = rounded(bg, dp(10))
        key.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastShiftTap < 300) {
                isCapsLock = !isCapsLock
                isShift = false
            } else {
                if (isCapsLock) { isCapsLock = false; isShift = false }
                else isShift = !isShift
            }
            lastShiftTap = now
            rebuildKeyboard()
        }
        return key
    }

    private fun backspaceKey(): View {
        val key = simpleKey("⌫")
        val handler = Handler(Looper.getMainLooper())
        var repeatRunnable: Runnable? = null

        fun deleteOnce() {
            currentInputConnection?.deleteSurroundingText(1, 0)
            refreshDecodeStrip()
        }

        key.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    deleteOnce()
                    val runnable = object : Runnable {
                        override fun run() {
                            deleteOnce()
                            handler.postDelayed(this, 50)
                        }
                    }
                    repeatRunnable = runnable
                    handler.postDelayed(runnable, 400)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    repeatRunnable?.let { handler.removeCallbacks(it) }
                    true
                }
                else -> false
            }
        }
        return key
    }

    private fun layerToggleKey(label: String): View {
        val key = simpleKey(label)
        key.setOnClickListener {
            layer = if (layer == Layer.LETTERS) Layer.NUMBERS else Layer.SYMBOLS
            rebuildKeyboard()
        }
        return key
    }

    private fun bottomRow(): LinearLayout {
        val row = rowContainer()
        val toggleLabel = if (layer == Layer.LETTERS) "?123" else "ABC"
        val toggle = simpleKey(toggleLabel)
        toggle.setOnClickListener {
            layer = if (layer == Layer.LETTERS) Layer.NUMBERS else Layer.LETTERS
            rebuildKeyboard()
        }
        row.addView(toggle, keyParams(1.4f))

        val space = simpleKey("space")
        space.setOnClickListener {
            currentInputConnection?.commitText(" ", 1)
            refreshDecodeStrip()
        }
        row.addView(space, keyParams(3f))

        val enter = simpleKey("⏎")
        enter.background = rounded("#8AB4F8", dp(10))
        enter.setOnClickListener {
            currentInputConnection?.commitText("\n", 1)
            refreshDecodeStrip()
        }
        row.addView(enter, keyParams(1.4f))
        return row
    }

    // ---------- Shared UI helpers ----------

    private fun rowContainer() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(46)
        ).also { it.bottomMargin = dp(6) }
    }

    private fun keyParams(weight: Float) =
        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
            .also { it.marginStart = dp(2); it.marginEnd = dp(2) }

    private fun simpleKey(label: String) = LinearLayout(this).apply {
        gravity = Gravity.CENTER
        background = rippleKeyBg()
        isClickable = true; isFocusable = true
        addView(TextView(this@SymbolIME).apply {
            text = label; setTextColor(Color.WHITE); textSize = 15f; gravity = Gravity.CENTER
        })
    }

    private fun rippleKeyBg() = rounded("#303134", dp(10))

    private fun rounded(colorHex: String, radius: Int) = GradientDrawable().apply {
        setColor(Color.parseColor(colorHex))
        cornerRadius = radius.toFloat()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}

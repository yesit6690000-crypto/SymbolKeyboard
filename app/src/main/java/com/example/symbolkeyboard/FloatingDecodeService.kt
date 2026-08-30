package com.example.symbolkeyboard

import android.app.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat

class FloatingDecodeService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private lateinit var clipboardManager: ClipboardManager

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener { onClipboardChanged() }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWithNotification() {
        val channelId = "decode_bubble_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Decode Bubble", NotificationManager.IMPORTANCE_MIN
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Symbol Keyboard")
            .setContentText("Decode bubble is active")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .build()
        startForeground(1, notification)
    }

    private fun onClipboardChanged() {
        val clip = clipboardManager.primaryClip ?: return
        if (clip.itemCount == 0) return
        val text = clip.getItemAt(0).coerceToText(this)?.toString() ?: return
        if (text.isBlank()) return

        val decoded = SymbolCipher.decode(text)
        if (decoded == text) return // nothing recognizable was decoded, skip showing the bubble

        showBubble(decoded)
    }

    private fun showBubble(text: String) {
        removeBubble()

        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.parseColor("#DD202124"))
        }
        bubble.addView(TextView(this).apply {
            this.text = "🔓 Decoded"
            setTextColor(Color.parseColor("#8AB4F8"))
            textSize = 12f
        })
        bubble.addView(TextView(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(0, dp(4), 0, 0)
        })

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(20)
            y = dp(120)
        }

        // Allow dragging the bubble, and tap to dismiss
        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        bubble.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - touchX).toInt()
                    params.y = initialY + (event.rawY - touchY).toInt()
                    windowManager.updateViewLayout(bubble, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (Math.abs(event.rawX - touchX) < 10 && Math.abs(event.rawY - touchY) < 10) {
                        removeBubble() // treat as a tap: dismiss
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(bubble, params)
        bubbleView = bubble

        // Auto-dismiss after 8 seconds
        bubble.postDelayed({ removeBubble() }, 8000)
    }

    private fun removeBubble() {
        bubbleView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) { }
        }
        bubbleView = null
    }

    override fun onDestroy() {
        super.onDestroy()
        removeBubble()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}

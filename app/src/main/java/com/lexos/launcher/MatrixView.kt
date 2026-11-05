package com.lexos.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

// Fond anime "Matrix" : des caracteres qui tombent, en orange LexOS.
class MatrixView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.parseColor("#FF6A00")
        textSize = 32f
        typeface = android.graphics.Typeface.MONOSPACE
    }
    private val fade = Paint().apply { color = Color.argb(20, 0, 0, 0) }

    private val glyphs = "LEXOS0123456789</>{}#*".toCharArray()
    private var columns = 0
    private lateinit var drops: IntArray
    private val cell = 34
    private var running = true

    private val loop = object : Runnable {
        override fun run() {
            if (!running) return
            invalidate()
            postDelayed(this, 80)
        }
    }

    fun setColor(hex: String) {
        try { paint.color = Color.parseColor(hex) } catch (_: Exception) {}
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        columns = (w / cell) + 1
        drops = IntArray(columns) { Random.nextInt(-40, 0) }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        running = true
        post(loop)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        running = false
        removeCallbacks(loop)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fade)
        if (!::drops.isInitialized) return
        for (i in 0 until columns) {
            val ch = glyphs[Random.nextInt(glyphs.size)]
            val xPos = (i * cell).toFloat()
            val yPos = (drops[i] * cell).toFloat()
            canvas.drawText(ch.toString(), xPos, yPos, paint)
            if (yPos > height && Random.nextFloat() > 0.975f) drops[i] = 0
            drops[i]++
        }
    }
}

package com.lexos.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Horloge a aiguilles dessinee, couleurs LexOS (cadran sombre, aiguilles orange).
class AnalogClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var accent = Color.parseColor("#FF6A00")
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 6f; color = Color.parseColor("#33FFFFFF")
    }
    private val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 4f; color = Color.parseColor("#66FFFFFF")
    }
    private val hourP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = 12f
    }
    private val minP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = 8f
    }
    private val secP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = 3f
    }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG)

    private var running = true
    private val loop = object : Runnable {
        override fun run() {
            if (!running) return
            invalidate()
            postDelayed(this, 1000)
        }
    }

    fun setAccent(color: Int) { accent = color; invalidate() }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow(); running = true; post(loop)
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow(); running = false; removeCallbacks(loop)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val r = min(cx, cy) - 12f

        hourP.color = Color.WHITE
        minP.color = Color.WHITE
        secP.color = accent
        dot.color = accent

        canvas.drawCircle(cx, cy, r, ring)

        // 12 reperes
        for (i in 0 until 12) {
            val a = Math.toRadians((i * 30).toDouble())
            val sx = cx + (r - 16f) * sin(a).toFloat()
            val sy = cy - (r - 16f) * cos(a).toFloat()
            val ex = cx + r * sin(a).toFloat()
            val ey = cy - r * cos(a).toFloat()
            canvas.drawLine(sx, sy, ex, ey, tick)
        }

        val c = Calendar.getInstance()
        val h = c.get(Calendar.HOUR)
        val m = c.get(Calendar.MINUTE)
        val s = c.get(Calendar.SECOND)

        val hAngle = Math.toRadians(((h + m / 60.0) * 30).toDouble())
        val mAngle = Math.toRadians((m * 6).toDouble())
        val sAngle = Math.toRadians((s * 6).toDouble())

        canvas.drawLine(cx, cy,
            cx + (r * 0.5f) * sin(hAngle).toFloat(),
            cy - (r * 0.5f) * cos(hAngle).toFloat(), hourP)
        canvas.drawLine(cx, cy,
            cx + (r * 0.72f) * sin(mAngle).toFloat(),
            cy - (r * 0.72f) * cos(mAngle).toFloat(), minP)
        canvas.drawLine(cx, cy,
            cx + (r * 0.82f) * sin(sAngle).toFloat(),
            cy - (r * 0.82f) * cos(sAngle).toFloat(), secP)

        canvas.drawCircle(cx, cy, 10f, dot)
    }
}

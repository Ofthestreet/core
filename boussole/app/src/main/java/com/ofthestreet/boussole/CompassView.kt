package com.ofthestreet.boussole

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var azimuth = 0f

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val northPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF4444")
    }

    private val southPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setAzimuth(newAzimuth: Float) {
        azimuth = newAzimuth
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = minOf(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.88f

        canvas.drawColor(Color.parseColor("#1A1A2E"))

        // Outer ring
        ringPaint.color = Color.parseColor("#4A4A8A")
        ringPaint.strokeWidth = 3f
        canvas.drawCircle(cx, cy, radius, ringPaint)

        // Inner ring
        ringPaint.color = Color.parseColor("#2A2A5A")
        ringPaint.strokeWidth = 1f
        canvas.drawCircle(cx, cy, radius * 0.7f, ringPaint)

        // Tick marks (rotate with compass)
        for (deg in 0 until 360) {
            val angleRad = Math.toRadians((deg - azimuth).toDouble())
            val sinA = sin(angleRad).toFloat()
            val cosA = cos(angleRad).toFloat()

            when {
                deg % 90 == 0 -> {
                    tickPaint.color = Color.parseColor("#FF6666")
                    tickPaint.strokeWidth = 4f
                    val r1 = radius * 0.78f
                    canvas.drawLine(
                        cx + r1 * sinA, cy - r1 * cosA,
                        cx + radius * sinA, cy - radius * cosA,
                        tickPaint
                    )
                }
                deg % 10 == 0 -> {
                    tickPaint.color = Color.parseColor("#AAAAAA")
                    tickPaint.strokeWidth = 2f
                    val r1 = radius * 0.87f
                    canvas.drawLine(
                        cx + r1 * sinA, cy - r1 * cosA,
                        cx + radius * sinA, cy - radius * cosA,
                        tickPaint
                    )
                }
                else -> {
                    tickPaint.color = Color.parseColor("#555588")
                    tickPaint.strokeWidth = 1f
                    val r1 = radius * 0.93f
                    canvas.drawLine(
                        cx + r1 * sinA, cy - r1 * cosA,
                        cx + radius * sinA, cy - radius * cosA,
                        tickPaint
                    )
                }
            }
        }

        // Cardinal direction labels (rotate with compass)
        textPaint.textSize = radius * 0.15f
        val cardinals = listOf(
            Triple(0, "N", Color.parseColor("#FF4444")),
            Triple(90, "E", Color.WHITE),
            Triple(180, "S", Color.WHITE),
            Triple(270, "O", Color.WHITE)
        )
        for ((deg, label, color) in cardinals) {
            val angleRad = Math.toRadians((deg - azimuth).toDouble())
            val r = radius * 0.62f
            val x = cx + r * sin(angleRad).toFloat()
            val y = cy - r * cos(angleRad).toFloat() + textPaint.textSize * 0.35f
            textPaint.color = color
            canvas.drawText(label, x, y, textPaint)
        }

        // Degree labels every 30°
        textPaint.textSize = radius * 0.09f
        textPaint.color = Color.parseColor("#888899")
        for (deg in listOf(30, 60, 120, 150, 210, 240, 300, 330)) {
            val angleRad = Math.toRadians((deg - azimuth).toDouble())
            val r = radius * 0.62f
            val x = cx + r * sin(angleRad).toFloat()
            val y = cy - r * cos(angleRad).toFloat() + textPaint.textSize * 0.35f
            canvas.drawText(deg.toString(), x, y, textPaint)
        }

        // Needle — always points North (rotates opposite to azimuth)
        canvas.save()
        canvas.rotate(-azimuth, cx, cy)

        val needleLen = radius * 0.52f
        val needleW = radius * 0.055f

        // North half (red, points up)
        val northPath = Path().apply {
            moveTo(cx, cy - needleLen)
            lineTo(cx - needleW, cy + needleW * 0.5f)
            lineTo(cx, cy)
            lineTo(cx + needleW, cy + needleW * 0.5f)
            close()
        }
        canvas.drawPath(northPath, northPaint)

        // South half (white, points down)
        val southPath = Path().apply {
            moveTo(cx, cy + needleLen)
            lineTo(cx - needleW, cy - needleW * 0.5f)
            lineTo(cx, cy)
            lineTo(cx + needleW, cy - needleW * 0.5f)
            close()
        }
        canvas.drawPath(southPath, southPaint)

        // Center cap
        val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#333366")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, needleW * 1.5f, capPaint)
        ringPaint.strokeWidth = 2f
        ringPaint.color = Color.parseColor("#6666AA")
        canvas.drawCircle(cx, cy, needleW * 1.5f, ringPaint)

        canvas.restore()
    }
}

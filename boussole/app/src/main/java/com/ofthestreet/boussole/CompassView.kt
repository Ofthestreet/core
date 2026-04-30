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
    private var isDark = true

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val northPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val southPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setAzimuth(newAzimuth: Float) {
        azimuth = newAzimuth
        invalidate()
    }

    fun setDarkMode(dark: Boolean) {
        isDark = dark
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

        val bg         = if (isDark) Color.parseColor("#1A1A2E") else Color.parseColor("#F0F0F5")
        val ringOuter  = if (isDark) Color.parseColor("#6060B8") else Color.parseColor("#3333AA")
        val ringInner  = if (isDark) Color.parseColor("#2A2A5A") else Color.parseColor("#CCCCDD")
        val tCardinal  = if (isDark) Color.parseColor("#FF6666") else Color.parseColor("#CC2222")
        val tTen       = if (isDark) Color.parseColor("#AAAAAA") else Color.parseColor("#555577")
        val tOne       = if (isDark) Color.parseColor("#555588") else Color.parseColor("#AAAACC")
        val labelN     = if (isDark) Color.parseColor("#FF4444") else Color.parseColor("#CC2222")
        val labelOther = if (isDark) Color.WHITE               else Color.parseColor("#333355")
        val degLabel   = if (isDark) Color.parseColor("#888899") else Color.parseColor("#777788")
        val needleN    = if (isDark) Color.parseColor("#FF4444") else Color.parseColor("#CC2222")
        val needleS    = if (isDark) Color.WHITE               else Color.parseColor("#333366")
        val cap        = if (isDark) Color.parseColor("#333366") else Color.parseColor("#DDDDEE")
        val capRing    = if (isDark) Color.parseColor("#6666AA") else Color.parseColor("#3333AA")

        canvas.drawColor(bg)

        ringPaint.color = ringOuter
        ringPaint.strokeWidth = 3f
        canvas.drawCircle(cx, cy, radius, ringPaint)

        ringPaint.color = ringInner
        ringPaint.strokeWidth = 1f
        canvas.drawCircle(cx, cy, radius * 0.7f, ringPaint)

        for (deg in 0 until 360) {
            val angleRad = Math.toRadians((deg - azimuth).toDouble())
            val sinA = sin(angleRad).toFloat()
            val cosA = cos(angleRad).toFloat()
            when {
                deg % 90 == 0 -> {
                    tickPaint.color = tCardinal; tickPaint.strokeWidth = 4f
                    val r1 = radius * 0.78f
                    canvas.drawLine(cx + r1 * sinA, cy - r1 * cosA, cx + radius * sinA, cy - radius * cosA, tickPaint)
                }
                deg % 10 == 0 -> {
                    tickPaint.color = tTen; tickPaint.strokeWidth = 2f
                    val r1 = radius * 0.87f
                    canvas.drawLine(cx + r1 * sinA, cy - r1 * cosA, cx + radius * sinA, cy - radius * cosA, tickPaint)
                }
                else -> {
                    tickPaint.color = tOne; tickPaint.strokeWidth = 1f
                    val r1 = radius * 0.93f
                    canvas.drawLine(cx + r1 * sinA, cy - r1 * cosA, cx + radius * sinA, cy - radius * cosA, tickPaint)
                }
            }
        }

        textPaint.textSize = radius * 0.15f
        for ((deg, label, color) in listOf(
            Triple(0, "N", labelN), Triple(90, "E", labelOther),
            Triple(180, "S", labelOther), Triple(270, "O", labelOther)
        )) {
            val angleRad = Math.toRadians((deg - azimuth).toDouble())
            val r = radius * 0.62f
            textPaint.color = color
            canvas.drawText(
                label,
                cx + r * sin(angleRad).toFloat(),
                cy - r * cos(angleRad).toFloat() + textPaint.textSize * 0.35f,
                textPaint
            )
        }

        textPaint.textSize = radius * 0.09f
        textPaint.color = degLabel
        for (deg in listOf(30, 60, 120, 150, 210, 240, 300, 330)) {
            val angleRad = Math.toRadians((deg - azimuth).toDouble())
            val r = radius * 0.62f
            canvas.drawText(
                deg.toString(),
                cx + r * sin(angleRad).toFloat(),
                cy - r * cos(angleRad).toFloat() + textPaint.textSize * 0.35f,
                textPaint
            )
        }

        canvas.save()
        canvas.rotate(-azimuth, cx, cy)

        val needleLen = radius * 0.52f
        val needleW   = radius * 0.055f

        northPaint.color = needleN
        canvas.drawPath(Path().apply {
            moveTo(cx, cy - needleLen)
            lineTo(cx - needleW, cy + needleW * 0.5f)
            lineTo(cx, cy)
            lineTo(cx + needleW, cy + needleW * 0.5f)
            close()
        }, northPaint)

        southPaint.color = needleS
        canvas.drawPath(Path().apply {
            moveTo(cx, cy + needleLen)
            lineTo(cx - needleW, cy - needleW * 0.5f)
            lineTo(cx, cy)
            lineTo(cx + needleW, cy - needleW * 0.5f)
            close()
        }, southPaint)

        capPaint.color = cap
        canvas.drawCircle(cx, cy, needleW * 1.5f, capPaint)
        ringPaint.strokeWidth = 2f
        ringPaint.color = capRing
        canvas.drawCircle(cx, cy, needleW * 1.5f, ringPaint)

        canvas.restore()
    }
}

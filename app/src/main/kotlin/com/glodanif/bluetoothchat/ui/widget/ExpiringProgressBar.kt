package com.glodanif.bluetoothchat.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Handler
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.glodanif.bluetoothchat.R
import android.os.Bundle
import android.os.Parcelable
import androidx.core.content.ContextCompat

class ExpiringProgressBar : View {

    private val instanceState = "saved_instance"
    private val instanceProgress = "progress"

    private var max = 100
    private var progress = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val minSize = resources.getDimension(R.dimen.epb_min_size)
    private val strokeThickness = resources.getDimension(R.dimen.epb_stroke_width)

    private val textPaint: TextPaint = TextPaint().apply {
        color = ContextCompat.getColor(context, R.color.text_dark)
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        textSize = resources.getDimension(R.dimen.epb_text_size)
        isAntiAlias = true
    }

    private val strokePaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorPrimary)
        strokeWidth = strokeThickness
        isDither = true
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val circleRect = RectF()

    private val periodHandler = Handler()
    private val period: Long = 1000

    private var timeLeft = 0
    private var isCanceled: Boolean = false

    init {
        if (isInEditMode) {
            max = 100
            progress = 69
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        circleRect.set(strokeThickness, strokeThickness,
                width - strokeThickness, height - strokeThickness)

        canvas?.drawArc(circleRect, -90f, getProgressAngle(), false, strokePaint)

        val textHeight = textPaint.descent() + textPaint.ascent()
        canvas?.drawText(
                progress.toString(),
                (width - textPaint.measureText(progress.toString())) / 2.0f,
                (width - textHeight) / 2.0f,
                textPaint
        )
    }

    private val tick = object : Runnable {

        override fun run() {
            if (!isCanceled && timeLeft > 0) {
                timeLeft--
                progress = timeLeft
                periodHandler.postDelayed(this, period)
                invalidate()
            }
        }
    }

    fun runExpiring(seconds: Int) {

        timeLeft = seconds
        isCanceled = false

        max = seconds
        progress = seconds

        periodHandler.postDelayed(tick, period)
    }

    fun cancel() {
        periodHandler.removeCallbacks(tick)
        timeLeft = 0
        isCanceled = true
    }

    private fun getProgressAngle(): Float = progress / max.toFloat() * 360f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec))
    }

    private fun measure(measureSpec: Int): Int {
        var result: Int
        val mode = View.MeasureSpec.getMode(measureSpec)
        val size = View.MeasureSpec.getSize(measureSpec)
        if (mode == View.MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = minSize.toInt()
            if (mode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, size)
            }
        }
        return result
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(instanceState, super.onSaveInstanceState())
        bundle.putInt(instanceProgress, progress)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {

        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }

        progress = state.getInt(instanceProgress)
        super.onRestoreInstanceState(state.getParcelable(instanceState))
    }
}

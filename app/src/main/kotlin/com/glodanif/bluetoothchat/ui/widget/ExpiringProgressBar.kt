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

class ExpiringProgressBar : View {

    private val INSTANCE_STATE = "saved_instance"
    private val INSTANCE_PROGRESS = "progress"

    private var max = 100
    private var progress = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val textPaint: TextPaint = TextPaint()
    private val strokePaint: Paint = Paint()
    private val circleRect = RectF()

    private val minSize = resources.getDimension(R.dimen.epb_min_size)
    private var textSize = resources.getDimension(R.dimen.epb_text_size)
    private val strokeWidth = resources.getDimension(R.dimen.epb_stroke_width)
    private var textColor = resources.getColor(R.color.text_dark)
    private val strokeColor = resources.getColor(R.color.colorPrimary)

    init {
        textPaint.color = textColor
        textPaint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true

        strokePaint.color = strokeColor
        strokePaint.strokeWidth = strokeWidth
        strokePaint.isDither = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.isAntiAlias = true
    }

    private val periodHandler = Handler()
    private val period: Long = 1000

    private var timeLeft = 0
    private var isCanceled: Boolean = false

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        circleRect.set(strokeWidth, strokeWidth, width - strokeWidth, height - strokeWidth)

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

    private fun getProgressAngle(): Float {
        return progress / max.toFloat() * 360f
    }

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
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putInt(INSTANCE_PROGRESS, progress)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {

        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }

        val bundle = state
        progress = bundle.getInt(INSTANCE_PROGRESS)
        super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE))
    }
}

package com.glodanif.bluetoothchat.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.glodanif.bluetoothchat.R

class MessageStatusDotView : View {

    private val INSTANCE_STATE = "saved_instance.msdv"
    private val EXTRA_STATE = "extra.state"

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val minSize = resources.getDimension(R.dimen.msdv_min_size)

    private val activePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.message_status_active)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val passivePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.message_status_passive)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    var active = false
        set(value) {
            invalidate()
        }

    init {
        if (isInEditMode) {
            active = true
        }
    }

    override fun onDraw(canvas: Canvas?) {
        val paint = if (active) activePaint else passivePaint
        canvas?.drawCircle(width / 2f, height / 2f, width / 2f, paint)
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
        bundle.putBoolean(EXTRA_STATE, active)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {

        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }

        active = state.getBoolean(EXTRA_STATE)
        super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE))
    }
}
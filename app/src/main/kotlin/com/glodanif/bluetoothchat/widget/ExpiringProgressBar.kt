package com.glodanif.bluetoothchat.widget

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar

class ExpiringProgressBar : IconRoundCornerProgressBar {

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val periodHandler = Handler()
    private val period: Long = 1000

    private var timeLeft = 0
    private var isCanceled: Boolean = false

    fun runExpiring(seconds: Int) {

        timeLeft = seconds
        isCanceled = false

        max = seconds.toFloat()
        progress = seconds.toFloat()

        periodHandler.postDelayed(object : Runnable {
            override fun run() {
                if (!isCanceled && timeLeft > 0) {
                    timeLeft--
                    progress = timeLeft.toFloat()
                    periodHandler.postDelayed(this, period)
                }
            }
        }, period)
    }

    fun cancel() {
        timeLeft = 0
        isCanceled = true;
    }
}

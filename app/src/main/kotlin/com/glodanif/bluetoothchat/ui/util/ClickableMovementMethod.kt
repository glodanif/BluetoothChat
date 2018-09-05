package com.glodanif.bluetoothchat.ui.util

import android.content.ActivityNotFoundException
import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView

object ClickableMovementMethod : ArrowKeyMovementMethod() {

    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {

        if (widget != null && event != null && event.action == MotionEvent.ACTION_UP) {

            var x = event.x
            var y = event.y
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y.toInt())
            val off = layout.getOffsetForHorizontal(line, x)
            val link = buffer?.getSpans(off, off, ClickableSpan::class.java)
            if (link != null && link.isNotEmpty()) {
                try {
                    link[0].onClick(widget)
                    return true
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}

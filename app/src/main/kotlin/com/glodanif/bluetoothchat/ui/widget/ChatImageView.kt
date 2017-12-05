package com.glodanif.bluetoothchat.ui.widget

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView

class ChatImageView: ImageView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setImageDrawable(drawable: Drawable?) {

        if (drawable != null && context is Activity) {

            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

            val maxWidth = (displayMetrics.widthPixels * .75).toInt()
            val maxHeight = (displayMetrics.heightPixels * .5).toInt()

            val imageWidth = drawable.intrinsicWidth
            val imageHeight = drawable.intrinsicHeight

            var viewWidth = imageWidth
            var viewHeight = imageHeight

            if (imageWidth > maxWidth || imageHeight > maxHeight) {

                if (imageWidth > maxWidth) {

                    viewWidth = maxWidth
                    val height = maxWidth.toFloat() / imageWidth * imageHeight

                    if (height > maxHeight) {
                        viewHeight = height.toInt()
                        viewWidth = (maxHeight.toFloat() / imageHeight * imageWidth).toInt()
                    }

                } else if (imageHeight > maxHeight) {

                    viewHeight = maxHeight
                    val width = maxHeight.toFloat() / imageHeight * imageWidth

                    if (width > maxWidth) {
                        viewWidth = width.toInt()
                        viewHeight = (maxWidth.toFloat() / imageWidth * imageHeight).toInt()
                    }
                }
            }

            this.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)
        }

        super.setImageDrawable(drawable)
    }
}
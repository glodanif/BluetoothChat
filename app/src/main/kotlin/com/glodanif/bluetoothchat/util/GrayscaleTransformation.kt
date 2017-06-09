package com.glodanif.bluetoothchat.util

import android.graphics.*
import com.squareup.picasso.Transformation

class GrayscaleTransformation : Transformation {

    override fun transform(source: Bitmap): Bitmap {

        val width = source.width
        val height = source.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        val saturation = ColorMatrix()
        saturation.setSaturation(0f)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(saturation)
        canvas.drawBitmap(source, 0f, 0f, paint)
        source.recycle()

        return bitmap
    }

    override fun key(): String {
        return "GrayscaleTransformation()"
    }
}

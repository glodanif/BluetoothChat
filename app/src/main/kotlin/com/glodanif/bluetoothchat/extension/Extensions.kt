package com.glodanif.bluetoothchat.extension

import android.content.Context
import android.graphics.Bitmap
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import android.R.attr.bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth

fun Date.getRelativeTime(context: Context): String {

    val resources = context.resources

    val viewFormat = SimpleDateFormat(context.getString(R.string.general__time_format), Locale.ENGLISH)
    val secondMillis = 1000
    val minuteMillis = 60 * secondMillis
    val hourMillis = 60 * minuteMillis
    val dayMillis = 24 * hourMillis

    var timestamp = this.time

    if (timestamp < 1_000_000_000_000L) {
        timestamp *= 1000
    }

    val now = System.currentTimeMillis()
    if (timestamp > now || timestamp <= 0) {
        return context.getString(R.string.general__time_unknown)
    }

    val diff = now - timestamp
    if (diff < minuteMillis) {
        return context.getString(R.string.general__time_just_now)
    } else if (diff < 2 * minuteMillis) {
        return context.getString(R.string.general__time_a_minute_ago)
    } else if (diff < 50 * minuteMillis) {
        val quantity = diff / minuteMillis
        return resources.getQuantityString(
                R.plurals.general__time_minutes_ago, quantity.toInt(), quantity)
    } else if (diff < 90 * minuteMillis) {
        return context.getString(R.string.general__time_an_hour_ago)
    } else if (diff < 24 * hourMillis) {
        val quantity = diff / hourMillis
        return resources.getQuantityString(
                R.plurals.general__time_hours_ago, quantity.toInt(), quantity)
    } else if (diff < 48 * hourMillis) {
        return context.getString(R.string.general__time_yesterday)
    } else if (diff < 7 * dayMillis) {
        val quantity = diff / dayMillis
        return resources.getQuantityString(
                R.plurals.general__time_days_ago, quantity.toInt(), quantity)
    } else {
        return viewFormat.format(this)
    }
}

fun String.getFirstLetter(): String {
    return if (this.isEmpty()) "?" else this[0].toString().toUpperCase()
}

fun TextDrawable.getBitmap(): Bitmap {

    val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)

    return bitmap
}

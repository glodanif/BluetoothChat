package com.glodanif.bluetoothchat.extension

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.annotation.PluralsRes
import android.support.annotation.StringRes
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

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
    return when {
        diff < minuteMillis ->
            context.getString(R.string.general__time_just_now)
        diff < 2 * minuteMillis ->
            context.getString(R.string.general__time_a_minute_ago)
        diff < 50 * minuteMillis ->
            getQuantityString(resources, R.plurals.general__time_minutes_ago, diff / minuteMillis)
        diff < 90 * minuteMillis ->
            context.getString(R.string.general__time_an_hour_ago)
        diff < 24 * hourMillis ->
            getQuantityString(resources, R.plurals.general__time_hours_ago, diff / hourMillis)
        diff < 48 * hourMillis ->
            context.getString(R.string.general__time_yesterday)
        diff < 7 * dayMillis ->
            getQuantityString(resources, R.plurals.general__time_days_ago, diff / dayMillis)
        else -> viewFormat.format(this)
    }
}

private fun getQuantityString(resources: Resources, @PluralsRes string: Int, quantity: Long) =
        resources.getQuantityString(string, quantity.toInt(), quantity)

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

fun Long.toReadableFileSize(): String {
    if (this <= 0) return "0"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(this.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(
            this / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}

fun String.isNumber(): Boolean =
        try {
            this.toLong()
            true
        } catch (e: NumberFormatException) {
            false
        }


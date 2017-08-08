package com.glodanif.bluetoothchat.extension

import android.content.Context
import com.glodanif.bluetoothchat.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun Date.getRelativeTime(context: Context): String {

    val resources = context.resources

    val VIEW_FORMAT: DateFormat =
            SimpleDateFormat(context.getString(R.string.general__time_format), Locale.ENGLISH)
    val SECOND_MILLIS = 1000
    val MINUTE_MILLIS = 60 * SECOND_MILLIS
    val HOUR_MILLIS = 60 * MINUTE_MILLIS
    val DAY_MILLIS = 24 * HOUR_MILLIS

    var timestamp = this.time

    if (timestamp < 1_000_000_000_000L) {
        timestamp *= 1000
    }

    val now = System.currentTimeMillis()
    if (timestamp > now || timestamp <= 0) {
        return context.getString(R.string.general__time_unknown)
    }

    val diff = now - timestamp
    if (diff < MINUTE_MILLIS) {
        return context.getString(R.string.general__time_just_now)
    } else if (diff < 2 * MINUTE_MILLIS) {
        return context.getString(R.string.general__time_a_minute_ago)
    } else if (diff < 50 * MINUTE_MILLIS) {
        val quantity = diff / MINUTE_MILLIS
        return resources.getQuantityString(
                R.plurals.general__time_minutes_ago, quantity.toInt(), quantity)
    } else if (diff < 90 * MINUTE_MILLIS) {
        return context.getString(R.string.general__time_an_hour_ago)
    } else if (diff < 24 * HOUR_MILLIS) {
        val quantity = diff / HOUR_MILLIS
        return resources.getQuantityString(
                R.plurals.general__time_hours_ago, quantity.toInt(), quantity)
    } else if (diff < 48 * HOUR_MILLIS) {
        return context.getString(R.string.general__time_yesterday)
    } else if (diff < 7 * DAY_MILLIS) {
        val quantity = diff / DAY_MILLIS
        return resources.getQuantityString(
                R.plurals.general__time_days_ago, quantity.toInt(), quantity)
    } else {
        return VIEW_FORMAT.format(this)
    }
}

fun String.getFirstLetter(): String {
    return if (this.isEmpty()) "?" else this[0].toString().toUpperCase()
}

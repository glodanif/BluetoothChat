package com.glodanif.bluetoothchat.extension

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Extensions {

    fun Date.getRelativeTime(): String {

        var timestamp = this.time

        if (timestamp < 1000000000000L) {
            timestamp *= 1000
        }

        val now = System.currentTimeMillis()
        if (timestamp > now || timestamp <= 0) {
            return "unknown"
        }

        val diff = now - timestamp
        if (diff < MINUTE_MILLIS) {
            return "just now"
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago"
        } else if (diff < 50 * MINUTE_MILLIS) {
            return "${diff / MINUTE_MILLIS} minutes ago"
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago"
        } else if (diff < 24 * HOUR_MILLIS) {
            return "${diff / HOUR_MILLIS} hours ago"
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday"
        } else if (diff < 7 * DAY_MILLIS) {
            return "${diff / DAY_MILLIS} days ago"
        } else {
            return VIEW_FORMAT.format(this)
        }
    }

    companion object {

        private val VIEW_FORMAT: DateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        private val SECOND_MILLIS = 1000
        private val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private val DAY_MILLIS = 24 * HOUR_MILLIS
    }
}
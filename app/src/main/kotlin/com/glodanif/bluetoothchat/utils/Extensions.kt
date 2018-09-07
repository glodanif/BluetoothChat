package com.glodanif.bluetoothchat.utils

import android.app.Activity
import android.app.NotificationManager
import android.bluetooth.BluetoothClass
import android.content.ContentUris
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.annotation.IdRes
import android.support.annotation.PluralsRes
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.R
import java.lang.Exception
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun Context.getDisplayMetrics(): DisplayMetrics {
    val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}

fun Context.getNotificationManager() =
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

fun Context.getLayoutInflater() =
        this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Animation.onEnd(action: () -> Unit) {

    this.setAnimationListener(object : Animation.AnimationListener {

        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            action.invoke()
        }

        override fun onAnimationStart(animation: Animation?) {
        }
    })
}

fun <T> MutableSet<T>.safeRemove(item: T) = this.iterator().let {

    while (it.hasNext()) {
        if (it.next() == item) {
            it.remove()
        }
    }
}

fun BluetoothClass.withPotentiallyInstalledApplication() =
        this.majorDeviceClass == BluetoothClass.Device.Major.PHONE ||
                this.majorDeviceClass == BluetoothClass.Device.Major.COMPUTER ||
                this.majorDeviceClass == BluetoothClass.Device.Major.UNCATEGORIZED ||
                this.majorDeviceClass == BluetoothClass.Device.Major.MISC

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

fun Uri.getFilePath(context: Context): String? {

    if (DocumentsContract.isDocumentUri(context, this)) {

        if (isExternalStorageDocument(this)) {
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            if ("primary".equals(type, ignoreCase = true)) {
                return "${Environment.getExternalStorageDirectory()}/${split[1]}"
            }

        } else if (isDownloadsDocument(this)) {

            val id = DocumentsContract.getDocumentId(this).toLong()
            val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id)

            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(this)) {
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            val contentUri = when (type) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> null
            }
            if (contentUri != null) {
                return getDataColumn(context, contentUri, "_id=?", arrayOf(split[1]))
            }
        }
    } else if ("content".equals(this.scheme, ignoreCase = true)) {
        return getDataColumn(context, this, null, null)
    } else if ("file".equals(this.scheme, ignoreCase = true)) {
        return this.path
    }
    return null
}

private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {

    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(columnIndex)
        }
    } catch (e: Exception) {
        return null
    } finally {
        cursor?.close()
    }
    return null
}

private fun isExternalStorageDocument(uri: Uri) = "com.android.externalstorage.documents" == uri.authority

private fun isDownloadsDocument(uri: Uri) = "com.android.providers.downloads.documents" == uri.authority

private fun isMediaDocument(uri: Uri) = "com.android.providers.media.documents" == uri.authority

fun String.getFirstLetter() = if (this.isEmpty()) "?" else this[0].toString().toUpperCase()

fun TextDrawable.getBitmap(): Bitmap {

    val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)

    return bitmap
}

fun Long.toReadableFileSize(): String {
    if (this <= 0) return "?"
    val units = arrayOf("B", "kB", "MB", "GB", "TB", "PB")
    val digitGroups = (Math.log10(this.toDouble()) / Math.log10(1024.0)).toInt()
    if (digitGroups > 6) return "?"
    return DecimalFormat("#,##0.#").format(
            this / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}

fun String.isNumber() =
        try {
            this.toLong()
            true
        } catch (e: NumberFormatException) {
            false
        }

inline fun <T : Any, V : Any> safeLet(p1: T?, p2: V?, block: (T, V) -> Unit) {
    if (p1 != null && p2 != null) {
        block(p1, p2)
    }
}

fun <T : View> Activity.bind(@IdRes idRes: Int) =
        lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(idRes) }

inline fun <reified T : Any?> AppCompatActivity.argument(key: String) = lazy {
    intent.extras?.let {
        return@lazy it[key] as T
    }
    return@lazy null
}

inline fun <reified T : Any> AppCompatActivity.argument(key: String, defaultValue: T) = lazy {
    intent.extras?.let {
        return@lazy it[key] as? T ?: defaultValue
    }
    return@lazy defaultValue
}

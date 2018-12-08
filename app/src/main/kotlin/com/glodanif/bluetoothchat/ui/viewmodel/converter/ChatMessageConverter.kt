package com.glodanif.bluetoothchat.ui.viewmodel.converter

import android.content.Context
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.ui.viewmodel.ChatMessageViewModel
import com.glodanif.bluetoothchat.utils.Size
import com.glodanif.bluetoothchat.utils.getDisplayMetrics
import java.text.SimpleDateFormat
import java.util.*

class ChatMessageConverter(context: Context) {

    private val dayOfYearFormat = SimpleDateFormat(context.getString(R.string.chat__date_format_day_of_year), Locale.getDefault())
    private val dayOfYearRawFormat = SimpleDateFormat("MMdd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat(context.getString(R.string.chat__date_format_time), Locale.getDefault())
    private val displayMetrics = context.getDisplayMetrics()

    fun transform(message: ChatMessage): ChatMessageViewModel {

        val isImageAvailable = message.filePath != null && message.fileExists
        val problemString = when {
            isImageAvailable -> -1
            message.filePath == null -> R.string.chat__removed_image
            !message.fileExists -> R.string.chat__missing_image
            else -> -1
        }

        val info = message.fileInfo
        val actualSize = info?.split("x")
        var width = displayMetrics.widthPixels / 2
        var height = width
        if (info != null && actualSize != null && actualSize.size == 2) {
            val scaledSize = getScaledSize(actualSize[0].toInt(), actualSize[1].toInt())
            if (scaledSize.width > 0 && scaledSize.height > 0) {
                width = scaledSize.width
                height = scaledSize.height
            }
        }

        return ChatMessageViewModel(
                message.uid,
                dayOfYearFormat.format(message.date),
                dayOfYearRawFormat.format(message.date).toLong(),
                timeFormat.format(message.date),
                message.text,
                message.own,
                message.messageType,
                isImageAvailable,
                problemString,
                Size(width, height),
                message.filePath,
                "file://${message.filePath}"
        )
    }

    fun transform(messages: Collection<ChatMessage>): List<ChatMessageViewModel> {
        return messages.map {
            transform(it)
        }
    }

    private fun getScaledSize(imageWidth: Int, imageHeight: Int): Size {

        val maxWidth = (displayMetrics.widthPixels * .75).toInt()
        val maxHeight = (displayMetrics.heightPixels * .5).toInt()

        var viewWidth = imageWidth
        var viewHeight = imageHeight

        if (imageWidth > maxWidth || imageHeight > maxHeight) {

            if (imageWidth == imageHeight) {

                if (imageHeight > maxHeight) {
                    viewWidth = maxHeight
                    viewHeight = maxHeight
                }

                if (viewWidth > maxWidth) {
                    viewWidth = maxWidth
                    viewHeight = maxWidth
                }

            } else if (imageWidth > maxWidth) {

                viewWidth = maxWidth
                viewHeight = (maxWidth.toFloat() / imageWidth * imageHeight).toInt()

                if (viewHeight > maxHeight) {
                    viewHeight = maxHeight
                    viewWidth = (maxHeight.toFloat() / imageHeight * imageWidth).toInt()
                }

            } else if (imageHeight > maxHeight) {

                viewHeight = maxHeight
                viewWidth = (maxHeight.toFloat() / imageHeight * imageWidth).toInt()

                if (viewWidth > maxWidth) {
                    viewWidth = maxWidth
                    viewHeight = (maxWidth.toFloat() / imageWidth * imageHeight).toInt()
                }

            }
        }

        return Size(viewWidth, viewHeight)
    }
}

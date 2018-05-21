package com.glodanif.bluetoothchat.ui.view

import android.app.Notification
import com.glodanif.bluetoothchat.data.service.TransferringFile

interface NotificationView {

    companion object {

        const val NOTIFICATION_ID_MESSAGE = 7438925
        const val NOTIFICATION_ID_CONNECTION = 5438729
        const val NOTIFICATION_ID_FILE = 1415665

        const val NOTIFICATION_TAG_MESSAGE = "tag.message"
        const val NOTIFICATION_TAG_CONNECTION = "tag.connection"
        const val NOTIFICATION_TAG_FILE = "tag.file"

        const val CHANNEL_FOREGROUND = "channel.foreground"
        const val CHANNEL_REQUEST = "channel.request"
        const val CHANNEL_MESSAGE = "channel.message"
        const val CHANNEL_FILE = "channel.file"
    }

    fun getForegroundNotification(message: String): Notification
    fun showNewMessageNotification(message: String, displayName: String?, deviceName: String?, address: String, soundEnabled: Boolean)
    fun showConnectionRequestNotification(deviceName: String, soundEnabled: Boolean)
    fun showFileTransferNotification(displayName: String?, deviceName: String, address: String, file: TransferringFile, transferredBytes: Long, silently: Boolean)
    fun updateFileTransferNotification(transferredBytes: Long, totalBytes: Long)
    fun dismissMessageNotification()
    fun dismissConnectionNotification()
    fun dismissFileTransferNotification()
}

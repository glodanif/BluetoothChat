package com.glodanif.bluetoothchat.ui.view

import android.app.Notification
import android.content.Context
import com.glodanif.bluetoothchat.data.entity.TransferringFile
import com.glodanif.bluetoothchat.ui.util.NotificationSettings

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
    fun showNewMessageNotification(message: String, displayName: String?, deviceName: String, address: String, settings: NotificationSettings)
    fun showConnectionRequestNotification(deviceName: String, settings: NotificationSettings)
    fun showFileTransferNotification(displayName: String?, deviceName: String, address: String, file: TransferringFile, transferredBytes: Long, silently: Boolean, settings: NotificationSettings)
    fun updateFileTransferNotification(transferredBytes: Long, totalBytes: Long)
    fun dismissMessageNotification()
    fun dismissConnectionNotification()
    fun dismissFileTransferNotification()
    fun getContext(): Context
}
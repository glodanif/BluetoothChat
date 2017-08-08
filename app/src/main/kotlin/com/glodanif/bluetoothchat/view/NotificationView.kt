package com.glodanif.bluetoothchat.view

import android.app.Notification
import com.glodanif.bluetoothchat.util.NotificationSettings

interface NotificationView {

    companion object {
        val NOTIFICATION_ID_MESSAGE = 7438925
        val NOTIFICATION_ID_CONNECTION = 5438729
        val NOTIFICATION_TAG_MESSAGE = "tag.message"
        val NOTIFICATION_TAG_CONNECTION = "tag.connection"
    }

    fun getForegroundNotification(message: String): Notification
    fun showNewMessageNotification(message: String, displayName: String?, deviceName: String, address: String, settings: NotificationSettings)
    fun showConnectionRequestNotification(deviceName: String, settings: NotificationSettings)
    fun dismissMessageNotification()
    fun dismissConnectionNotification()
}
package com.glodanif.bluetoothchat.service

import android.app.Notification

interface NotificationView {
    fun getForegroundNotification(message: String): Notification
    fun showNewMessageNotification(message: String, deviceName: String, address: String)
    fun showConnectionRequestNotification(deviceName: String)
    fun dismissMessageNotification()
    fun dismissConnectionNotification()
}
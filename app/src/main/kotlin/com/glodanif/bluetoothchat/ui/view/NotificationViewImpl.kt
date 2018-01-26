package com.glodanif.bluetoothchat.ui.view

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.TransferringFile
import com.glodanif.bluetoothchat.ui.activity.ChatActivity
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import com.glodanif.bluetoothchat.data.service.BluetoothConnectionService
import com.glodanif.bluetoothchat.extension.toReadableFileSize
import com.glodanif.bluetoothchat.ui.util.NotificationSettings

class NotificationViewImpl(private val context: Context) : NotificationView {

    private val CHANNEL_FOREGROUND = "channel.foreground"
    private val CHANNEL_REQUEST = "channel.request"
    private val CHANNEL_FILE = "channel.file"
    private val CHANNEL_MESSAGE = "channel.message"

    private val notificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    private val resources = context.resources

    override fun getForegroundNotification(message: String): Notification {

        val notificationIntent = Intent(context, ConversationsActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        val stopIntent = Intent(context, BluetoothConnectionService::class.java)
        stopIntent.action = BluetoothConnectionService.ACTION_STOP
        val stopPendingIntent = PendingIntent.getService(context, 0, stopIntent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_FOREGROUND, context.getString(R.string.notification__channel_background), NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_FOREGROUND)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(0, context.getString(R.string.notification__stop), stopPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.color = resources.getColor(R.color.colorPrimary)
        }

        return builder.build()
    }

    override fun showNewMessageNotification(message: String, displayName: String?, deviceName: String, address: String, settings: NotificationSettings) {

        val notificationIntent = Intent(context, ChatActivity::class.java)
                .putExtra(ChatActivity.EXTRA_ADDRESS, address)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        val name = if (displayName.isNullOrEmpty()) deviceName else "$displayName ($deviceName)"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_MESSAGE, context.getString(R.string.notification__channel_message), NotificationManager.IMPORTANCE_MAX)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_MESSAGE)
                .setContentTitle(name)
                .setContentText(message)
                .setLights(Color.BLUE, 3000, 3000)
                .setSmallIcon(R.drawable.ic_new_message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.color = resources.getColor(R.color.colorPrimary)
        }

        val notification = builder.build()

        if (settings.soundEnabled) {
            notification.defaults = notification.defaults or Notification.DEFAULT_SOUND
        }
        if (settings.vibrationEnabled) {
            notification.defaults = notification.defaults or Notification.DEFAULT_VIBRATE
        }

        notificationManager.notify(NotificationView.NOTIFICATION_TAG_MESSAGE,
                NotificationView.NOTIFICATION_ID_MESSAGE, notification)
    }

    override fun showConnectionRequestNotification(deviceName: String, settings: NotificationSettings) {

        val notificationIntent = Intent(context, ConversationsActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_REQUEST, context.getString(R.string.notification__channel_request), NotificationManager.IMPORTANCE_MAX)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_REQUEST)
                .setContentTitle(context.getString(R.string.notification__connection_request))
                .setContentText(context.getString(R.string.notification__connection_request_body, deviceName))
                .setLights(Color.BLUE, 3000, 3000)
                .setSmallIcon(R.drawable.ic_connection_request)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.color = resources.getColor(R.color.colorPrimary)
        }

        val notification = builder.build()

        if (settings.soundEnabled) {
            notification.defaults = notification.defaults or Notification.DEFAULT_SOUND
        }
        if (settings.vibrationEnabled) {
            notification.defaults = notification.defaults or Notification.DEFAULT_VIBRATE
        }

        notificationManager.notify(NotificationView.NOTIFICATION_TAG_CONNECTION,
                NotificationView.NOTIFICATION_ID_CONNECTION, notification)
    }

    private var transferBuilder: NotificationCompat.Builder? = null

    override fun showFileTransferNotification(displayName: String?, deviceName: String, address: String, file: TransferringFile, transferredBytes: Long, silently: Boolean, settings: NotificationSettings) {

        val notificationIntent = Intent(context, ChatActivity::class.java)
                .putExtra(ChatActivity.EXTRA_ADDRESS, address)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_FILE,
                    context.getString(R.string.notification__channel_file), NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_FILE)
                .setContentTitle(context.getString(
                        if (file.transferType == TransferringFile.TransferType.SENDING)
                            R.string.notification__file_sending else R.string.notification__file_receiving, displayName))
                .setContentText(file.size.toReadableFileSize())
                .setLights(Color.BLUE, 3000, 3000)
                .setSmallIcon(R.drawable.ic_image_transfer)
                .setOnlyAlertOnce(true)
                .setProgress(file.size.toInt(), transferredBytes.toInt(), false)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.color = resources.getColor(R.color.colorPrimary)
        }

        val notification = builder.build()

        if (settings.soundEnabled && !silently) {
            notification.defaults = notification.defaults or Notification.DEFAULT_SOUND
        }
        if (settings.vibrationEnabled && !silently) {
            notification.defaults = notification.defaults or Notification.DEFAULT_VIBRATE
        }

        transferBuilder = builder

        notificationManager.notify(NotificationView.NOTIFICATION_TAG_FILE,
                NotificationView.NOTIFICATION_ID_FILE, notification)
    }

    override fun updateFileTransferNotification(transferredBytes: Long, totalBytes: Long) {

        if(transferBuilder != null) {
            transferBuilder!!.setProgress(totalBytes.toInt(), transferredBytes.toInt(), false)
            notificationManager.notify(NotificationView.NOTIFICATION_TAG_FILE,
                    NotificationView.NOTIFICATION_ID_FILE, transferBuilder!!.build())
        }
    }

    override fun dismissMessageNotification() {
        notificationManager.cancel(
                NotificationView.NOTIFICATION_TAG_MESSAGE, NotificationView.NOTIFICATION_ID_MESSAGE)
    }

    override fun dismissConnectionNotification() {
        notificationManager.cancel(
                NotificationView.NOTIFICATION_TAG_CONNECTION, NotificationView.NOTIFICATION_ID_CONNECTION)
    }

    override fun dismissFileTransferNotification() {
        notificationManager.cancel(
                NotificationView.NOTIFICATION_TAG_FILE, NotificationView.NOTIFICATION_ID_FILE)
        transferBuilder = null
    }
}

package com.glodanif.bluetoothchat.ui.view

import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import java.io.File

interface ChatView {

    fun showMessagesHistory(messages: List<ChatMessage>)
    fun showReceivedMessage(message: ChatMessage)
    fun showSentMessage(message: ChatMessage)
    fun showServiceDestroyed()
    fun showRejectedConnection()
    fun showConnectionRequest(conversation: Conversation)
    fun showLostConnection()
    fun showFailedConnection()
    fun showDisconnected()
    fun showNotConnectedToAnyDevice()
    fun showNotConnectedToThisDevice(currentDevice: String)
    fun showNotValidMessage()
    fun showNotConnectedToSend()
    fun showDeviceIsNotAvailable()
    fun showWainingForOpponent()
    fun hideActions()
    fun afterMessageSent()
    fun showStatusConnected()
    fun showStatusNotConnected()
    fun showStatusPending()
    fun showPartnerName(name: String, device: String)
    fun showBluetoothDisabled()
    fun showBluetoothEnablingFailed()
    fun requestBluetoothEnabling()
    fun dismissMessageNotification()

    fun showImageSendingLayout(fileAddress: String?, fileSize: Long)
    fun updateImageSendingProgress(transferredBytes: Long, totalBytes: Long)
    fun hideImageSendingLayout()
}
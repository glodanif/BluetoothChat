package com.glodanif.bluetoothchat.view

import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.entity.Conversation

interface ChatView {

    fun showMessagesHistory(messages: List<ChatMessage>)
    fun showReceivedMessage(message: ChatMessage)
    fun showSentMessage(message: ChatMessage)
    fun showConnected()
    fun showServiceDestroyed()
    fun showAcceptedConnection()
    fun showRejectedConnection()
    fun showConnectionRequest(conversation: Conversation)
    fun showLostConnection()
    fun showFailedConnection()
    fun notifyAboutConnectedDevice(conversation: Conversation)
    fun showDisconnected()
    fun showNotConnectedToAnyDevice()
    fun showNotConnectedToThisDevice(currentDevice: String)
    fun showNotValidMessage()
    fun showDeviceIsNotAvailable()
    fun showWainingForOpponent()
    fun hideActions()
}
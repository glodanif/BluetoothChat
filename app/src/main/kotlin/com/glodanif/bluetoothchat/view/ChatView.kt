package com.glodanif.bluetoothchat.view

import com.glodanif.bluetoothchat.entity.ChatMessage

interface ChatView {

    fun showMessagesHistory(messages: List<ChatMessage>)
    fun showReceivedMessage(message: ChatMessage)
    fun showSentMessage(message: ChatMessage)
    fun showAcceptedConnection()
    fun showRejectedConnection()
    fun showLostConnection()
    fun showDisconnected()
}
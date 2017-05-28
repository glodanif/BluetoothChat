package com.glodanif.bluetoothchat.view

import com.glodanif.bluetoothchat.entity.ChatMessage

interface ChatView {

    fun showMessagesHistory()
    fun showReceivedMessage(message: ChatMessage)
    fun showSentMessage(message: ChatMessage)
}
package com.glodanif.bluetoothchat.view

interface ConversationsView {

    fun showReceivedMessage(message: String)
    fun showSentMessage(message: String)
}
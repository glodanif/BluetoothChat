package com.glodanif.bluetoothchat.ui.router

import com.glodanif.bluetoothchat.ui.viewmodel.ConversationViewModel

interface ConversationsRouter {
    fun redirectToChat(conversation: ConversationViewModel)
    fun redirectToProfile()
    fun redirectToReceivedImages()
    fun redirectToSettings()
    fun redirectToAbout()
}

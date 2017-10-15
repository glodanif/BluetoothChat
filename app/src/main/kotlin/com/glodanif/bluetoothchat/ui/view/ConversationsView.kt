package com.glodanif.bluetoothchat.ui.view

import android.support.annotation.ColorInt
import com.glodanif.bluetoothchat.data.entity.Conversation

interface ConversationsView {

    fun redirectToChat(conversation: Conversation)
    fun notifyAboutConnectedDevice(conversation: Conversation)
    fun showServiceDestroyed()
    fun showNoConversations()
    fun showRejectedNotification(conversation: Conversation)
    fun hideActions()
    fun refreshList(connected: String?)
    fun showConversations(conversations: List<Conversation>, connected: String?)
    fun showUserProfile(name: String, @ColorInt color: Int)
    fun dismissConversationNotification()
    fun removeFromShortcuts(address: String)
}

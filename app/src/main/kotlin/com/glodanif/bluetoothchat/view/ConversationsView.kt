package com.glodanif.bluetoothchat.view

import android.bluetooth.BluetoothDevice
import android.support.annotation.ColorInt
import com.glodanif.bluetoothchat.entity.Conversation

interface ConversationsView {

    fun redirectToChat(conversation: Conversation)
    fun notifyAboutConnectedDevice(conversation: Conversation)
    fun connectedToModel()
    fun showServiceDestroyed()

    fun hideActions()
    fun showNoConversations()
    fun showRejectedNotification(conversation: Conversation)
    fun showConversations(conversations: List<Conversation>, connected: String?)
    fun refreshList(connected: String?)

    fun setupUserProfile(name: String, @ColorInt color: Int)
}
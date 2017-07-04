package com.glodanif.bluetoothchat.view

import android.bluetooth.BluetoothDevice
import android.support.annotation.ColorInt
import com.glodanif.bluetoothchat.entity.Conversation

interface ConversationsView {

    fun redirectToChat(conversation: Conversation)
    fun notifyAboutConnectedDevice(conversation: Conversation)
    fun connectedToModel()

    fun hideActions()
    fun showNoConversations()
    fun showConversations(conversations: List<Conversation>, connected: String?)

    fun setupUserProfile(name: String, @ColorInt color: Int)
}
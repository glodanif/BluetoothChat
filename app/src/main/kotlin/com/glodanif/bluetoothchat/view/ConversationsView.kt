package com.glodanif.bluetoothchat.view

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.Conversation

interface ConversationsView {

    fun redirectToChat(device: BluetoothDevice)
    fun notifyAboutConnectedDevice(device: BluetoothDevice)
    fun connectedToModel()

    fun hideActions()
    fun showNoConversations()
    fun showConversations(conversations: List<Conversation>, connected: String?)
}
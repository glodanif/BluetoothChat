package com.glodanif.bluetoothchat.view

import android.bluetooth.BluetoothDevice

interface ConversationsView {

    fun redirectToChat(device: BluetoothDevice)
}
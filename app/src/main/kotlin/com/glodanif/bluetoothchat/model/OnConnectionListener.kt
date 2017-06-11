package com.glodanif.bluetoothchat.model

import android.bluetooth.BluetoothDevice

interface OnConnectionListener {
    fun onConnecting()
    fun onConnectedIn(device: BluetoothDevice)
    fun onConnectedOut(device: BluetoothDevice)
    fun onConnectionLost()
    fun onConnectionFailed()
    fun onDisconnected()
    fun onConnectionAccepted()
    fun onConnectionRejected()
}
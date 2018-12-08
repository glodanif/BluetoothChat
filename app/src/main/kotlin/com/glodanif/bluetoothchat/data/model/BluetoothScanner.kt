package com.glodanif.bluetoothchat.data.model

import android.bluetooth.BluetoothDevice

interface BluetoothScanner {

    fun setDiscoveryListener(listener: DiscoveryListener)
    fun setDiscoverableListener(listener: DiscoverableListener)
    fun scanForDevices(seconds: Int)
    fun stopScanning()
    fun getMyDeviceName(): String?
    fun getBondedDevices(): List<BluetoothDevice>
    fun getDeviceByAddress(address: String): BluetoothDevice?
    fun isBluetoothAvailable(): Boolean
    fun isBluetoothEnabled(): Boolean
    fun isDiscoverable(): Boolean
    fun isDiscovering(): Boolean
    fun listenDiscoverableStatus()
    fun stopListeningDiscoverableStatus()

    interface DiscoveryListener {
        fun onDiscoveryStart(seconds: Int)
        fun onDiscoveryFinish()
        fun onDeviceFind(device: BluetoothDevice)
    }

    interface DiscoverableListener {
        fun onDiscoverableStart()
        fun onDiscoverableFinish()
    }
}
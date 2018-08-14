package com.glodanif.bluetoothchat.data.model

import android.bluetooth.BluetoothDevice

interface BluetoothScanner {

    fun setScanningListener(listener: ScanningListener)
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

    interface ScanningListener {
        fun onDiscoveryStart(seconds: Int)
        fun onDiscoveryFinish()
        fun onDiscoverableStart()
        fun onDiscoverableFinish()
        fun onDeviceFind(device: BluetoothDevice)
    }
}
package com.glodanif.bluetoothchat.view

import android.bluetooth.BluetoothDevice

interface ScanView {

    fun showBluetoothIsNotAvailableMessage()

    fun showBluetoothFunctionality()

    fun showBluetoothEnablingRequest()

    fun showBluetoothDiscoverableFailure()

    fun showPairedDevices(pairedDevices: List<BluetoothDevice>)

    fun enableBluetooth()

    fun requestMakingDiscoverable()

    fun discoverableInProcess()

    fun discoverableFinished()

    fun scanningStarted(seconds: Int)

    fun scanningStopped()

    fun showBluetoothEnablingFailed()

    fun addFoundDevice(device: BluetoothDevice)
}

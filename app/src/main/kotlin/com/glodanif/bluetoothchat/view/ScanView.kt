package com.glodanif.bluetoothchat.view

import android.bluetooth.BluetoothDevice

interface ScanView {

    fun showBluetoothIsNotAvailableMessage()

    fun showBluetoothFunctionality()

    fun showBluetoothEnablingRequest()

    fun showPairedDevices(pairedDevices: List<BluetoothDevice>)

    fun enableBluetooth()

    fun requestMakingDiscoverable()

    fun showBluetoothEnablingFailed()
}

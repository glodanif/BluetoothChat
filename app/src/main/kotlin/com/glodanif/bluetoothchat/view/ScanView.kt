package com.glodanif.bluetoothchat.view

import android.bluetooth.BluetoothDevice

interface ScanView {

    fun showBluetoothIsNotAvailableMessage()

    fun showBluetoothFunctionality()

    fun showBluetoothEnablingRequest()

    fun showPairedDevices(pairedDevices: Set<BluetoothDevice>)

    fun enableBluetooth()

    fun handleBluetoothEnablingResponse(resultCode: Int)
}

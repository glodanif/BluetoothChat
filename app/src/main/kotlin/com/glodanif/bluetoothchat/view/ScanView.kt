package com.glodanif.bluetoothchat.view

import android.bluetooth.BluetoothDevice

interface ScanView {
    fun showBluetoothIsNotAvailableMessage()
    fun showBluetoothScanner()
    fun showBluetoothEnablingRequest()
    fun showBluetoothEnablingFailed()
    fun showBluetoothDiscoverableFailure()
    fun showPairedDevices(pairedDevices: List<BluetoothDevice>)
    fun requestBluetoothEnabling()
    fun requestMakingDiscoverable()
    fun showDiscoverableProcess()
    fun showDiscoverableFinished()
    fun showScanningStarted(seconds: Int)
    fun showScanningStopped()
    fun addFoundDevice(device: BluetoothDevice)
}

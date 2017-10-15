package com.glodanif.bluetoothchat.ui.view

import android.bluetooth.BluetoothDevice
import android.net.Uri

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
    fun openChat(device: BluetoothDevice)
    fun showServiceUnavailable()
    fun showUnableToConnect()
    fun shareApk(uri: Uri)
    fun showExtractionApkFailureMessage()
}

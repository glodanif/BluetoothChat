package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.model.BluetoothScanner
import com.glodanif.bluetoothchat.view.ScanView

class ScanPresenter(private val view: ScanView, private val scanner: BluetoothScanner) {

    init {
        scanner.listener = object : BluetoothScanner.ScanningListener {

            override fun onDiscoveryStart() {
                TODO("not implemented")
            }

            override fun onDiscoveryFinish() {
                TODO("not implemented")
            }

            override fun onDeviceFind(device: BluetoothDevice) {
                TODO("not implemented")
            }

            override fun onAvailableDiscoverChange(enabled: Boolean) {
                TODO("not implemented")
            }
        }
    }

    fun checkBluetoothAvailability() {

        if (scanner.isBluetoothAvailable()) {
            view.showBluetoothFunctionality()
        } else {
            view.showBluetoothIsNotAvailableMessage()
        }
    }

    fun checkBluetoothEnabling() {

        if (scanner.isBluetoothEnabled()) {
            onPairedDevicesReady()
        } else {
            view.showBluetoothEnablingRequest()
        }
    }

    fun turnOnBluetooth() {
        if (!scanner.isBluetoothEnabled()) {
            view.enableBluetooth()
        }
    }

    fun onPairedDevicesReady() {
        view.showPairedDevices(scanner.getBondedDevices())
    }

    fun onBluetoothEnablingFailed() {
        view.showBluetoothEnablingFailed()
    }

    fun onMadeDiscoverable(seconds: Int) {

    }

    fun onMakeDiscoverableFailed() {

    }

    fun makeDiscoverable() {
        view.requestMakingDiscoverable()
    }

    fun scanForDevices() {
        scanner.scanForDevices()
    }
}

package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothAdapter
import com.glodanif.bluetoothchat.view.ScanView

class ScanPresenter(private val view: ScanView) {

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    fun checkBluetoothAvailability() {

        if (adapter == null) {
            view.showBluetoothIsNotAvailableMessage()
        } else {
            view.showBluetoothFunctionality()
        }
    }

    fun turnOnBluetooth() {

        if (adapter == null) {
            return
        }

        if (!adapter.isEnabled) {
            view.enableBluetooth()
        }
    }

    fun getPairedDevices() {

        if (adapter == null) {
            return
        }

        view.showPairedDevices(adapter.bondedDevices)
    }
}
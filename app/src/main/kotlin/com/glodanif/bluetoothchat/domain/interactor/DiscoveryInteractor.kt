package com.glodanif.bluetoothchat.domain.interactor

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.data.model.UserPreferencesStorage
import com.glodanif.bluetoothchat.domain.exception.BluetoothStatusException
import com.glodanif.bluetoothchat.utils.withPotentiallyInstalledApplication

class DiscoveryInteractor(private val scanner: BluetoothScanner, private val preferences: UserPreferencesStorage) {

    fun execute(
            onStart: ((Int) -> Unit)? = null,
            onFinish: (() -> Unit)? = null,
            onDeviceFound: ((BluetoothDevice) -> Unit)? = null,
            onError: ((Throwable) -> Unit)? = null
    ) {

        scanner.setDiscoveryListener(object : BluetoothScanner.DiscoveryListener {

            override fun onDiscoveryStart(seconds: Int) {
                onStart?.invoke(seconds)
            }

            override fun onDiscoveryFinish() {
                onFinish?.invoke()
            }

            override fun onDeviceFind(device: BluetoothDevice) {
                if (!preferences.getPreferences().classification || device.bluetoothClass.withPotentiallyInstalledApplication()) {
                    onDeviceFound?.invoke(device)
                }
            }
        })

        if (!scanner.isDiscovering()) {
            scanner.scanForDevices(30)
        } else {
            onError?.invoke(BluetoothStatusException("Discovery is in progress"))
        }
    }

    fun release() {
        scanner.stopScanning()
    }
}

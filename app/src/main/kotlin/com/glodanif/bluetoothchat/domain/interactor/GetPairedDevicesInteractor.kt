package com.glodanif.bluetoothchat.domain.interactor

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.data.model.UserPreferencesStorage
import com.glodanif.bluetoothchat.utils.withPotentiallyInstalledApplication

class GetPairedDevicesInteractor(private val scanner: BluetoothScanner, private val storage: UserPreferencesStorage) : BaseInteractor<Unit, List<BluetoothDevice>>() {

    override suspend fun execute(input: Unit): List<BluetoothDevice> {
        val preferences = storage.getPreferences()
        return scanner.getBondedDevices().filter {
            !preferences.classification || it.bluetoothClass.withPotentiallyInstalledApplication()
        }
    }
}

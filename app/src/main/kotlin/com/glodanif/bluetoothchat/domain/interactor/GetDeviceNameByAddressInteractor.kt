package com.glodanif.bluetoothchat.domain.interactor

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.domain.exception.DeviceNotFoundException

class GetDeviceNameByAddressInteractor(private val scanner: BluetoothScanner) : BaseInteractor<String, BluetoothDevice>() {

    override suspend fun execute(input: String): BluetoothDevice {
        val device = scanner.getDeviceByAddress(input)
        return device ?: throw DeviceNotFoundException()
    }
}

package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothScanner

class GetMyDeviceNameInteractor(private val scanner: BluetoothScanner): BaseInteractor<Unit, String?>() {

    override suspend fun execute(input: Unit) = scanner.getMyDeviceName()
}

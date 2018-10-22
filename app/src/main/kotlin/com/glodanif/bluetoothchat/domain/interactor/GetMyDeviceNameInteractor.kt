package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.data.model.ProfileRepository
import com.glodanif.bluetoothchat.domain.entity.Profile

class GetMyDeviceNameInteractor(private val scanner: BluetoothScanner): BaseInteractor<NoInput, String?>() {

    override suspend fun execute(input: NoInput) = scanner.getMyDeviceName()
}

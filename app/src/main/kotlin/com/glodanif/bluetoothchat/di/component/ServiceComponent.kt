package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.data.service.BluetoothConnectionService
import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.di.module.ServiceModule
import dagger.Component

@PerComponent
@Component(dependencies = [(ApplicationComponent::class)], modules = [(ServiceModule::class)])
interface ServiceComponent {
    fun inject(service: BluetoothConnectionService)
}

package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.data.service.BluetoothConnectionService
import com.glodanif.bluetoothchat.di.module.ApplicationModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(dependencies = [(ApplicationModule::class)])
interface ServiceComponent {
    fun inject(service: BluetoothConnectionService)
}

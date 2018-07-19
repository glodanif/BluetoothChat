package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.di.module.ScanModule
import com.glodanif.bluetoothchat.ui.activity.ScanActivity
import dagger.Component

@PerComponent
@Component(dependencies = [(ApplicationComponent::class)], modules = [(ScanModule::class)])
interface ScanComponent {
    fun inject(activity: ScanActivity)
}

package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.di.module.SettingsModule
import com.glodanif.bluetoothchat.ui.activity.SettingsActivity
import dagger.Component

@PerComponent
@Component(dependencies = [(ApplicationComponent::class)], modules = [(SettingsModule::class)])
interface SettingsComponent {
    fun inject(activity: SettingsActivity)
}

package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.di.module.ProfileModule
import com.glodanif.bluetoothchat.di.module.SettingsModule
import com.glodanif.bluetoothchat.ui.activity.ProfileActivity
import com.glodanif.bluetoothchat.ui.activity.SettingsActivity
import dagger.Component

@PerActivity
@Component(dependencies = [(ApplicationComponent::class)], modules = [(SettingsModule::class)])
interface SettingsComponent {
    fun inject(activity: SettingsActivity)
}

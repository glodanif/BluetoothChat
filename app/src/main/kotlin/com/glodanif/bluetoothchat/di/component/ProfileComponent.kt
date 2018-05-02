package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.di.module.ProfileModule
import com.glodanif.bluetoothchat.ui.activity.ProfileActivity
import dagger.Component

@PerActivity
@Component(dependencies = [(ApplicationComponent::class)], modules = [(ProfileModule::class)])
interface ProfileComponent {
    fun inject(activity: ProfileActivity)
}

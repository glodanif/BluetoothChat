package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.di.module.ReceivedImagesModule
import com.glodanif.bluetoothchat.ui.activity.ReceivedImagesActivity
import dagger.Component

@PerActivity
@Component(dependencies = [(ApplicationComponent::class)], modules = [(ReceivedImagesModule::class)])
interface ReceivedImagesComponent {
    fun inject(activity: ReceivedImagesActivity)
}

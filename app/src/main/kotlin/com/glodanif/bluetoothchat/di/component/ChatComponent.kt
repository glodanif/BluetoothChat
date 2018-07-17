package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.di.module.ChatModule
import com.glodanif.bluetoothchat.ui.activity.ChatActivity
import dagger.Component

@PerComponent
@Component(dependencies = [(ApplicationComponent::class)], modules = [(ChatModule::class)])
interface ChatComponent {
    fun inject(activity: ChatActivity)
}

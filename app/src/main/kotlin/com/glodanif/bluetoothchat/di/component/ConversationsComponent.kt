package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.di.module.ConversationsModule
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import dagger.Component

@PerComponent
@Component(dependencies = [(ApplicationComponent::class)], modules = [(ConversationsModule::class)])
interface ConversationsComponent {
    fun inject(activity: ConversationsActivity)
}

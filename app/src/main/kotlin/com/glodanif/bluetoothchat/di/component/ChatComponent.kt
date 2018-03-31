package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.di.module.ChatModule
import com.glodanif.bluetoothchat.di.module.ConversationsModule
import com.glodanif.bluetoothchat.ui.activity.ChatActivity
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import dagger.Component

@PerActivity
@Component(dependencies = [(ApplicationComponent::class)], modules = [(ChatModule::class)])
interface ChatComponent {
    fun inject(activity: ChatActivity)
}

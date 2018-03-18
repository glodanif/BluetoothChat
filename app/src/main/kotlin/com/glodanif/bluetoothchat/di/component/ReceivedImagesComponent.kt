package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.di.module.ChatModule
import com.glodanif.bluetoothchat.di.module.ConversationsModule
import com.glodanif.bluetoothchat.di.module.ProfileModule
import com.glodanif.bluetoothchat.di.module.ReceivedImagesModule
import com.glodanif.bluetoothchat.ui.activity.ChatActivity
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import com.glodanif.bluetoothchat.ui.activity.ProfileActivity
import com.glodanif.bluetoothchat.ui.activity.ReceivedImagesActivity
import dagger.Component

@PerActivity
@Component(dependencies = arrayOf(ApplicationComponent::class), modules = arrayOf(ReceivedImagesModule::class))
interface ReceivedImagesComponent {
    fun inject(activity: ReceivedImagesActivity)
}

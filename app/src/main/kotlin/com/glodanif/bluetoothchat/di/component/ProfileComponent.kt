package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.di.module.ChatModule
import com.glodanif.bluetoothchat.di.module.ConversationsModule
import com.glodanif.bluetoothchat.di.module.ProfileModule
import com.glodanif.bluetoothchat.ui.activity.ChatActivity
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import com.glodanif.bluetoothchat.ui.activity.ProfileActivity
import dagger.Component

@PerActivity
@Component(dependencies = arrayOf(ApplicationComponent::class), modules = arrayOf(ProfileModule::class))
interface ProfileComponent {
    fun inject(activity: ProfileActivity)
}

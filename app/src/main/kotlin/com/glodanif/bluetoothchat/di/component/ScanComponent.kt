package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.di.module.ChatModule
import com.glodanif.bluetoothchat.di.module.ConversationsModule
import com.glodanif.bluetoothchat.di.module.ProfileModule
import com.glodanif.bluetoothchat.di.module.ScanModule
import com.glodanif.bluetoothchat.ui.activity.ChatActivity
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import com.glodanif.bluetoothchat.ui.activity.ProfileActivity
import com.glodanif.bluetoothchat.ui.activity.ScanActivity
import dagger.Component

@PerActivity
@Component(dependencies = [(ApplicationComponent::class)], modules = [(ScanModule::class)])
interface ScanComponent {
    fun inject(activity: ScanActivity)
}

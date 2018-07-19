package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.ChatApplication
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.data.model.SettingsManager
import com.glodanif.bluetoothchat.data.model.UserPreferences
import com.glodanif.bluetoothchat.data.service.BluetoothConnectionService
import com.glodanif.bluetoothchat.data.service.connection.ConnectionController
import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.ui.view.NotificationView
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import dagger.Module
import dagger.Provides

@Module
class ServiceModule(private val service: BluetoothConnectionService) {

    @Provides
    @PerComponent
    internal fun provideController(application: ChatApplication, view: NotificationView, conversations: ConversationsStorage, messages: MessagesStorage,
                                   preferences: UserPreferences, settings: SettingsManager, shortcutManager: ShortcutManager): ConnectionController =
            ConnectionController(application, service, view, conversations, messages, preferences, settings, shortcutManager)
}

package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.ChatApplication
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.di.module.ApplicationModule
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ConversationConverter
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(ApplicationModule::class)])
interface ApplicationComponent {

    fun inject(application: ChatApplication)

    fun conversationsStorage(): ConversationsStorage
    fun messagesStorage(): MessagesStorage
    fun settingsManager(): SettingsManager
    fun preferences(): UserPreferences

    fun shortcutManager(): ShortcutManager
    fun fileManager(): FileManager

    fun contactConverter(): ContactConverter
    fun conversationConverter(): ConversationConverter
    fun chatMessageConverter(): ChatMessageConverter

    fun getBluetoothConnector(): BluetoothConnector
}

package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.ChatApplication
import com.glodanif.bluetoothchat.data.database.ChatDatabase
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.di.module.ApplicationModule
import com.glodanif.bluetoothchat.ui.view.NotificationView
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

    fun application(): ChatApplication

    fun notificationView(): NotificationView

    fun conversationsStorage(): ConversationsStorage
    fun messagesStorage(): MessagesStorage
    fun profileManager(): ProfileManager
    fun preferences(): UserPreferences

    fun shortcutManager(): ShortcutManager
    fun fileManager(): FileManager

    fun contactConverter(): ContactConverter
    fun conversationConverter(): ConversationConverter
    fun chatMessageConverter(): ChatMessageConverter

    fun bluetoothConnector(): BluetoothConnector

    fun database(): ChatDatabase
}

package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.FileManager
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.data.model.SettingsManager
import com.glodanif.bluetoothchat.di.module.ApplicationModule
import com.glodanif.bluetoothchat.ui.presenter.*
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ConversationConverter
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun conversationsStorage(): ConversationsStorage
    fun messagesStorage(): MessagesStorage
    fun settingsManager(): SettingsManager

    fun shortcutManager(): ShortcutManager
    fun fileManager(): FileManager

    fun contactConverter(): ContactConverter
    fun conversationConverter(): ConversationConverter
    fun chatMessageConverter(): ChatMessageConverter
}

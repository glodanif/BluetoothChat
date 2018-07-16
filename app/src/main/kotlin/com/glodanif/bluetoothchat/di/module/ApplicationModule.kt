package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.ChatApplication
import com.glodanif.bluetoothchat.data.database.ChatDatabase
import com.glodanif.bluetoothchat.data.database.Database
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.ui.view.NotificationView
import com.glodanif.bluetoothchat.ui.view.NotificationViewImpl
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ConversationConverter
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import com.glodanif.bluetoothchat.ui.widget.ShortcutManagerImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: ChatApplication) {

    @Provides
    @Singleton
    internal fun provideApplication(): ChatApplication = application

    @Provides
    @Singleton
    internal fun provideNotificationView(): NotificationView = NotificationViewImpl(application)

    @Provides
    @Singleton
    internal fun provideMessagesStorage(db: ChatDatabase): MessagesStorage = MessagesStorageImpl(db)

    @Provides
    @Singleton
    internal fun provideConversationsStorage(db: ChatDatabase): ConversationsStorage = ConversationsStorageImpl(db)

    @Provides
    @Singleton
    internal fun provideSettingsManager(): SettingsManager = SettingsManagerImpl(application)

    @Provides
    @Singleton
    internal fun provideShortcutManager(): ShortcutManager = ShortcutManagerImpl(application)

    @Provides
    @Singleton
    internal fun provideFileManager(): FileManager = FileManagerImpl(application)

    @Provides
    @Singleton
    internal fun provideUserPreferences(): UserPreferences = UserPreferencesImpl(application)

    @Provides
    @Singleton
    internal fun provideContactConverter(): ContactConverter = ContactConverter()

    @Provides
    @Singleton
    internal fun provideConversationConverter(): ConversationConverter = ConversationConverter(application)

    @Provides
    @Singleton
    internal fun provideChatMessageConverter(): ChatMessageConverter = ChatMessageConverter(application)

    @Provides
    @Singleton
    internal fun provideConnector(): BluetoothConnector = BluetoothConnectorImpl(application)

    @Provides
    @Singleton
    internal fun provideDatabase(): ChatDatabase = Database.createDatabase(application)
}

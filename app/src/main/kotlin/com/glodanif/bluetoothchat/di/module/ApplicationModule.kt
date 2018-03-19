package com.glodanif.bluetoothchat.di.module

import android.content.Context
import com.glodanif.bluetoothchat.data.model.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val context: Context) {

    @Provides
    @Singleton
    internal fun provideMessagesStorage(): MessagesStorage = MessagesStorageImpl(context)

    @Provides
    @Singleton
    internal fun provideConversationsStorage(): ConversationsStorage = ConversationsStorageImpl(context)

    @Provides
    @Singleton
    internal fun provideSettingsManager(): SettingsManager = SettingsManagerImpl(context)
}

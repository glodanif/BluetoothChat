package com.glodanif.bluetoothchat.di.module

import android.content.Context
import com.glodanif.bluetoothchat.data.model.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataSourceModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideMessagesStorage(): MessagesStorage = MessagesStorageImpl(context)

    @Provides
    @Singleton
    fun provideConversationsStorage(): ConversationsStorage = ConversationsStorageImpl(context)

    @Provides
    @Singleton
    fun provideSettingsManager(): SettingsManager = SettingsManagerImpl(context)
}

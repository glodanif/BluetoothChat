package com.glodanif.bluetoothchat.di.module

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ConversationConverter
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import com.glodanif.bluetoothchat.ui.widget.ShortcutManagerImpl
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

    @Provides
    @Singleton
    internal fun provideShortcutManager(): ShortcutManager = ShortcutManagerImpl(context)

    @Provides
    @Singleton
    internal fun provideFileManager(): FileManager = FileManagerImpl(context)

    @Provides
    @Singleton
    internal fun provideContactConverter(): ContactConverter = ContactConverter()

    @Provides
    @Singleton
    internal fun provideConversationConverter(): ConversationConverter = ConversationConverter(context)

    @Provides
    @Singleton
    internal fun provideChatMessageConverter(): ChatMessageConverter {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return ChatMessageConverter(context, displayMetrics)
    }
}

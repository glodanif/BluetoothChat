package com.glodanif.bluetoothchat.di

import com.glodanif.bluetoothchat.ui.view.NotificationView
import com.glodanif.bluetoothchat.ui.view.NotificationViewImpl
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ConversationConverter
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import com.glodanif.bluetoothchat.ui.widget.ShortcutManagerImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

val viewModule = applicationContext {
    bean { NotificationViewImpl(androidApplication()) as NotificationView }
    bean { ShortcutManagerImpl(androidApplication()) as ShortcutManager }
    bean { ContactConverter() }
    bean { ConversationConverter(androidApplication()) }
    bean { ChatMessageConverter(androidApplication()) }
}

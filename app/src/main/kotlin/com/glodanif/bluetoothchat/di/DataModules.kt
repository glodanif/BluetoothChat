package com.glodanif.bluetoothchat.di

import com.glodanif.bluetoothchat.data.database.Database
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.ui.view.NotificationView
import com.glodanif.bluetoothchat.ui.view.NotificationViewImpl
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ConversationConverter
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import com.glodanif.bluetoothchat.ui.widget.ShortcutManagerImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

val bluetoothConnectionModule = applicationContext {
    bean { BluetoothConnectorImpl(androidApplication()) as BluetoothConnector }
    factory { BluetoothScannerImpl(androidApplication()) as BluetoothScanner }
}

val databaseModule = applicationContext {
    bean { Database.getInstance(androidApplication()) }
    bean { MessagesStorageImpl(get()) as MessagesStorage }
    bean { ConversationsStorageImpl(get()) as ConversationsStorage }
}

val localStorageModule = applicationContext {
    bean { FileManagerImpl(androidApplication()) as FileManager }
    bean { UserPreferencesImpl(androidApplication()) as UserPreferences }
    bean { ProfileManagerImpl(androidApplication()) as ProfileManager }
}

val viewModule = applicationContext {
    bean { NotificationViewImpl(androidApplication()) as NotificationView }
    bean { ShortcutManagerImpl(androidApplication()) as ShortcutManager }
    bean { ContactConverter() }
    bean { ConversationConverter(androidApplication()) }
    bean { ChatMessageConverter(androidApplication()) }
}

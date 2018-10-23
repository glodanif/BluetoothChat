package com.glodanif.bluetoothchat.di

import com.glodanif.bluetoothchat.data.database.Database
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.domain.interactor.*
import com.glodanif.bluetoothchat.ui.view.NotificationView
import com.glodanif.bluetoothchat.ui.view.NotificationViewImpl
import com.glodanif.bluetoothchat.ui.viewmodel.converter.*
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import com.glodanif.bluetoothchat.ui.widget.ShortcutManagerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val bluetoothConnectionModule = module {
    single { BluetoothConnectorImpl(androidContext()) as BluetoothConnector }
    factory { BluetoothScannerImpl(androidContext()) as BluetoothScanner }
}

val databaseModule = module {
    single { Database.getInstance(androidContext()) }
    single { MessagesStorageImpl(get()) as MessagesStorage }
    single { ConversationsStorageImpl(get()) as ConversationsStorage }
}

val localStorageModule = module {
    single { FileManagerImpl(androidContext()) as FileManager }
    single { UserPreferencesStorageImpl(androidContext()) as UserPreferencesStorage }
    single { ProfileRepositoryImpl(androidContext()) as ProfileRepository }
}

val domainModule = module {
    factory { GetProfileInteractor(get()) }
    factory { SaveProfileInteractor(get()) }
    factory { IsProfileInitializedInteractor(get()) }
    factory { GetMyDeviceNameInteractor(get()) }
    factory { GetContactsInteractor(get()) }
    factory { GetConversationsInteractor(get()) }
    factory { GetReceivedImagesInteractor(get()) }
    factory { GetUserPreferencesInteractor(get()) }
    factory { SaveUserPreferencesInteractor(get()) }
    factory { RemoveConversationInteractor(get(), get()) }
    factory { MarkMessagesAsSeenMessagesInteractor(get()) }
    factory { ExtractApkInteractor(get()) }
    factory { SubscribeForConnectionEventsInteractor(get()) }
}

const val localeScope = "locale_scope"

val viewModule = module {
    single { NotificationViewImpl(androidContext()) as NotificationView }
    single { ShortcutManagerImpl(androidContext()) as ShortcutManager }
    scope(localeScope) { ContactConverter() }
    scope(localeScope) { ConversationConverter(androidContext()) }
    scope(localeScope) { ChatMessageConverter(androidContext()) }
    scope(localeScope) { ProfileConverter(androidContext()) }
    scope(localeScope) { PreferencesConverter() }
}

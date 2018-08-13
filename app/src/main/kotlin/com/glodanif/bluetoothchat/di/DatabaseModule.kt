package com.glodanif.bluetoothchat.di

import com.glodanif.bluetoothchat.data.database.Database
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.ConversationsStorageImpl
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorageImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

val databaseModule = applicationContext {
    bean { Database.getInstance(androidApplication()) }
    bean { MessagesStorageImpl(get()) as MessagesStorage }
    bean { ConversationsStorageImpl(get()) as ConversationsStorage }
}

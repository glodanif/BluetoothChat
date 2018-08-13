package com.glodanif.bluetoothchat.di

import com.glodanif.bluetoothchat.data.model.*
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

val localStorageModule = applicationContext {
    bean { FileManagerImpl(androidApplication()) as FileManager }
    bean { UserPreferencesImpl(androidApplication()) as UserPreferences }
    bean { ProfileManagerImpl(androidApplication()) as ProfileManager }
}

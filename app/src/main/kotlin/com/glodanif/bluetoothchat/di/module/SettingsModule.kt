package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.data.model.UserPreferences
import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.ui.activity.SettingsActivity
import com.glodanif.bluetoothchat.ui.presenter.SettingsPresenter
import dagger.Module
import dagger.Provides

@Module
class SettingsModule(private val activity: SettingsActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(settings: UserPreferences): SettingsPresenter = SettingsPresenter(activity, settings)
}

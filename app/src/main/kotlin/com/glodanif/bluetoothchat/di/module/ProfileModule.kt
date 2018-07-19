package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.data.model.BluetoothScannerImpl
import com.glodanif.bluetoothchat.data.model.SettingsManager
import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.ui.activity.ProfileActivity
import com.glodanif.bluetoothchat.ui.presenter.ProfilePresenter
import dagger.Module
import dagger.Provides

@Module
class ProfileModule(private val activity: ProfileActivity) {

    @Provides
    @PerComponent
    internal fun providePresenter(settings: SettingsManager, scanner: BluetoothScanner): ProfilePresenter = ProfilePresenter(activity, settings, scanner)

    @Provides
    @PerComponent
    internal fun provideScanner(): BluetoothScanner = BluetoothScannerImpl(activity)
}

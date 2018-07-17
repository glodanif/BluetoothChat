package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.ui.activity.ScanActivity
import com.glodanif.bluetoothchat.ui.presenter.ScanPresenter
import dagger.Module
import dagger.Provides

@Module
class ScanModule(private val activity: ScanActivity) {

    @Provides
    @PerComponent
    internal fun providePresenter(scanner: BluetoothScanner, connector: BluetoothConnector, fileManager: FileManager, preferences: UserPreferences): ScanPresenter =
            ScanPresenter(activity, scanner, connector, fileManager, preferences)

    @Provides
    @PerComponent
    internal fun provideScanner(): BluetoothScanner = BluetoothScannerImpl(activity)
}

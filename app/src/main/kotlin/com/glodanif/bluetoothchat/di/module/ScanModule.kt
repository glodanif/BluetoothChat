package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.ui.activity.ScanActivity
import com.glodanif.bluetoothchat.ui.presenter.ScanPresenter
import dagger.Module
import dagger.Provides

@Module
class ScanModule(private val activity: ScanActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(scanner: BluetoothScanner, connector: BluetoothConnector, fileManager: FileManager): ScanPresenter =
            ScanPresenter(activity, scanner, connector, fileManager)

    @Provides
    @PerActivity
    internal fun provideConnector(): BluetoothConnector = BluetoothConnectorImpl(activity)

    @Provides
    @PerActivity
    internal fun provideScanner(): BluetoothScanner = BluetoothScannerImpl(activity)
}

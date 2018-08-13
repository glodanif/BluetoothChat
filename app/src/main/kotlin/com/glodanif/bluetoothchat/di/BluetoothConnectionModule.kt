package com.glodanif.bluetoothchat.di

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.model.BluetoothConnectorImpl
import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.data.model.BluetoothScannerImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

val bluetoothConnectionModule = applicationContext {
    bean { BluetoothConnectorImpl(androidApplication()) as BluetoothConnector }
    factory { BluetoothScannerImpl(androidApplication()) as BluetoothScanner }
}

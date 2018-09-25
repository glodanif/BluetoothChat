package com.glodanif.bluetoothchat.ui.presenter

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.ui.view.ScanView
import com.glodanif.bluetoothchat.utils.withPotentiallyInstalledApplication
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import kotlin.coroutines.experimental.CoroutineContext

class ScanPresenter(private val view: ScanView,
                    private val scanner: BluetoothScanner,
                    private val connection: BluetoothConnector,
                    private val fileManager: FileManager,
                    private val preferences: UserPreferences,
                    private val uiContext: CoroutineContext = UI,
                    private val bgContext: CoroutineContext = CommonPool): LifecycleObserver {

    companion object {
        const val SCAN_DURATION_SECONDS = 30
    }

    init {

        scanner.setScanningListener(object : BluetoothScanner.ScanningListener {

            override fun onDiscoverableFinish() {
                view.showDiscoverableFinished()
            }

            override fun onDiscoverableStart() {
                view.showDiscoverableProcess()
            }

            override fun onDiscoveryStart(seconds: Int) {
                view.showScanningStarted(seconds)
            }

            override fun onDiscoveryFinish() {
                view.showScanningStopped()
            }

            override fun onDeviceFind(device: BluetoothDevice) {
                if (!preferences.isClassificationEnabled() || device.bluetoothClass.withPotentiallyInstalledApplication()) {
                    view.addFoundDevice(device)
                }
            }
        })
    }

    fun onDevicePicked(address: String) {

        val device = scanner.getDeviceByAddress(address)

        if (connection.isConnectionPrepared()) {
            connection.addOnConnectListener(connectionListener)
            connectDevice(device)
            return
        }

        connection.addOnPrepareListener(object : OnPrepareListener {

            override fun onPrepared() {
                connectDevice(device)
                connection.removeOnPrepareListener(this)
            }

            override fun onError() {
                view.showServiceUnavailable()
                connection.removeOnPrepareListener(this)
            }
        })

        connection.prepare()
    }

    private fun connectDevice(device: BluetoothDevice?) {
        if (device != null) {
            connection.connect(device)
        } else {
            view.showServiceUnavailable()
        }
    }

    private val connectionListener = object : SimpleConnectionListener() {

        override fun onConnected(device: BluetoothDevice) {
            view.openChat(device)
            connection.removeOnConnectListener(this)
        }

        override fun onConnectionLost() {
            view.showUnableToConnect()
            connection.removeOnConnectListener(this)
        }

        override fun onConnectionFailed() {
            view.showUnableToConnect()
            connection.removeOnConnectListener(this)
        }
    }

    fun checkBluetoothAvailability() {

        if (scanner.isBluetoothAvailable()) {
            view.showBluetoothScanner()
        } else {
            view.showBluetoothIsNotAvailableMessage()
        }
    }

    fun checkBluetoothEnabling() {

        if (scanner.isBluetoothEnabled()) {
            onPairedDevicesReady()
            if (scanner.isDiscoverable()) {
                scanner.listenDiscoverableStatus()
                view.showDiscoverableProcess()
            } else {
                view.showDiscoverableFinished()
            }
        } else {
            view.showBluetoothEnablingRequest()
        }
    }

    fun turnOnBluetooth() {
        if (!scanner.isBluetoothEnabled()) {
            view.requestBluetoothEnabling()
        }
    }

    fun onPairedDevicesReady() {
        val devices = scanner.getBondedDevices().filter {
            !preferences.isClassificationEnabled() || it.bluetoothClass.withPotentiallyInstalledApplication()
        }
        view.showPairedDevices(devices)
    }

    fun onBluetoothEnablingFailed() {
        view.showBluetoothEnablingFailed()
    }

    fun onMadeDiscoverable() {
        scanner.listenDiscoverableStatus()
        view.showDiscoverableProcess()
    }

    fun makeDiscoverable() {
        if (!scanner.isDiscoverable()) {
            view.requestMakingDiscoverable()
        }
    }

    fun scanForDevices() {
        if (!scanner.isDiscovering()) {
            scanner.scanForDevices(SCAN_DURATION_SECONDS)
        } else {
            cancelScanning()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun listenDiscoverableStatus() {
        if (scanner.isDiscoverable()) {
            scanner.listenDiscoverableStatus()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun cancelScanning() {
        view.showScanningStopped()
        scanner.stopScanning()
        connection.removeOnConnectListener(connectionListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun cancelDiscoveryStatusListening() {
        scanner.stopListeningDiscoverableStatus()
    }

    fun shareApk() = launch(uiContext) {
        val uri = withContext(bgContext) { fileManager.extractApkFile() }
        if (uri != null) {
            view.shareApk(uri)
        } else {
            view.showExtractionApkFailureMessage()
        }
    }
}

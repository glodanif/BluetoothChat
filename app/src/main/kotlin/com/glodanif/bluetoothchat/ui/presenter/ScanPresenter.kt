package com.glodanif.bluetoothchat.ui.presenter

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.domain.exception.ConnectionException
import com.glodanif.bluetoothchat.domain.exception.DeviceNotFoundException
import com.glodanif.bluetoothchat.domain.exception.PreparationException
import com.glodanif.bluetoothchat.domain.interactor.*
import com.glodanif.bluetoothchat.ui.view.ScanView

class ScanPresenter(private val view: ScanView,
                    private val connectToDeviceInteractor: ConnectToDeviceInteractor,
                    private val getPairedDevicesInteractor: GetPairedDevicesInteractor,
                    private val extractApkInteractor: ExtractApkInteractor,
                    private val discoveryInteractor: DiscoveryInteractor,
                    private val discoverableInteractor: DiscoverableInteractor,
                    private val checkBluetoothStatusInteractor: CheckBluetoothStatusInteractor

) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        getPairedDevicesInteractor.cancel()
        extractApkInteractor.cancel()
    }

    fun onDevicePicked(address: String) {

        connectToDeviceInteractor.connect(address,
                onResult = { device -> view.openChat(device) },
                onError = { error ->
                    when (error) {
                        is ConnectionException -> {
                            view.showUnableToConnect()
                        }
                        is DeviceNotFoundException -> {
                            //FIXME
                            view.showServiceUnavailable()
                        }
                        is PreparationException -> {
                            view.showServiceUnavailable()
                        }
                    }
                }
        )
    }

    fun checkBluetoothAvailability() {

        checkBluetoothStatusInteractor.checkIfAvailable(
                onResult = { view.showBluetoothScanner() },
                onError = { view.showBluetoothIsNotAvailableMessage() }
        )
    }

    fun checkBluetoothEnabling() {

        checkBluetoothStatusInteractor.checkIfEnabled(
                onResult = {
                    onPairedDevicesReady()
                    discoverableInteractor.listen(
                            onResult = { view.showDiscoverableProcess() },
                            onError = { view.showDiscoverableFinished() }
                    )
                },
                onError = { view.showBluetoothEnablingRequest() }
        )
    }

    fun turnOnBluetooth() {
        checkBluetoothStatusInteractor.checkIfEnabled(
                onError = { view.requestBluetoothEnabling() }
        )
    }

    fun onPairedDevicesReady() {
        getPairedDevicesInteractor.execute(Unit,
                onResult = { devices -> view.showPairedDevices(devices) }
        )
    }

    fun onBluetoothEnablingFailed() {
        view.showBluetoothEnablingFailed()
    }

    fun onMadeDiscoverable() {
        discoverableInteractor.listen(
                onResult = { view.showDiscoverableProcess() }
        )
    }

    fun makeDiscoverable() {
        discoverableInteractor.checkDiscoverability(
                onError = { view.requestMakingDiscoverable() }
        )
    }

    fun scanForDevices() {

        discoveryInteractor.execute(
                onStart = { seconds -> view.showScanningStarted(seconds) },
                onFinish = { view.showScanningStopped() },
                onDeviceFound = { device -> view.addFoundDevice(device) },
                onError = { cancelScanning() }
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun listenDiscoverableStatus() {

        discoverableInteractor.setListeners(
                onStart = { view.showDiscoverableProcess() },
                onFinish = { view.showDiscoverableFinished() }
        )

        discoverableInteractor.listen()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun cancelScanning() {
        view.showScanningStopped()
        discoveryInteractor.release()
        connectToDeviceInteractor.release()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun cancelDiscoveryStatusListening() {
        discoverableInteractor.stopListening()
    }

    fun shareApk() {

        extractApkInteractor.execute(Unit,
                onResult = { uri -> view.shareApk(uri) },
                onError = { view.showExtractionApkFailureMessage() }
        )
    }
}

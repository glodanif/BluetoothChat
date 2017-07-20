package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.view.ScanView

class ScanPresenter(private val view: ScanView, private val scanner: BluetoothScanner,
                    private val connection: BluetoothConnector, private val fileExtractor: FileExtractor) {

    private val SCAN_DURATION_SECONDS = 30

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
                view.addFoundDevice(device)
            }
        })
    }

    fun onDevicePicked(address: String) {

        val device = scanner.getDeviceByAddress(address)

        connection.setOnPrepareListener(object : OnPrepareListener {

            override fun onPrepared() {
                if (device != null) {
                    connection.connect(device)
                } else {
                    view.showServiceUnavailable()
                }
            }

            override fun onError() {
                view.showServiceUnavailable()
            }
        })


        connection.setOnConnectListener(object : SimpleConnectionListener() {

            override fun onConnected() {
                if (device != null) {
                    view.openChat(device)
                }
            }

            override fun onConnectionLost() {
                view.showUnableToConnect()
            }

            override fun onConnectionFailed() {
                view.showUnableToConnect()
            }
        })

        connection.prepare()
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
                scanner.listenForDevices()
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
        view.showPairedDevices(scanner.getBondedDevices())
    }

    fun onBluetoothEnablingFailed() {
        view.showBluetoothEnablingFailed()
    }

    fun onMadeDiscoverable() {
        scanner.listenForDevices()
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

    fun cancelScanning() {
        view.showScanningStopped()
        scanner.stopScanning()
    }

    fun shareApk() {

        fileExtractor.extractFile(
                onExtracted = { view.shareApk(it) },
                onFailed = { view.showExtractionApkFailureMessage() }
        )
    }
}

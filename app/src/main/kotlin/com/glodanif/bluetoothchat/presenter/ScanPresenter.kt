package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.Conversation
import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.model.BluetoothScanner
import com.glodanif.bluetoothchat.model.OnConnectionListener
import com.glodanif.bluetoothchat.model.OnPrepareListener
import com.glodanif.bluetoothchat.view.ScanView

class ScanPresenter(private val view: ScanView, private val scanner: BluetoothScanner, private val connection: BluetoothConnector) {

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
                    TODO("not implemented")
                }
            }

            override fun onError() {
                TODO("not implemented")
            }
        })


        connection.setOnConnectListener(object : OnConnectionListener {

            override fun onConnected() {
                if (device != null) {
                    view.openChat(device)
                }
            }

            override fun onConnecting() {

            }

            override fun onConnectedIn(conversation: Conversation) {
                TODO("not implemented2") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onConnectedOut(conversation: Conversation) {
                TODO("not implemented3") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onConnectionLost() {
                TODO("not implemented4") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onConnectionFailed() {
                TODO("not implemented5") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onConnectionDestroyed() {
                TODO("not implemented6") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDisconnected() {
                TODO("not implemented7") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onConnectionAccepted() {
                TODO("not implemented8") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onConnectionRejected() {
                TODO("not implemented9") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onConnectionWithdrawn() {
                TODO("not implemented10") //To change body of created functions use File | Settings | File Templates.
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
}

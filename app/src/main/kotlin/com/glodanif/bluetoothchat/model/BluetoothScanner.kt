package com.glodanif.bluetoothchat.model

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler

class BluetoothScanner(val context: Context) {

    var listener: ScanningListener? = null

    private val handler: Handler = Handler()

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val foundDeviceFilter: IntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
    private val foundDeviceReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device = intent
                        .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                listener?.onDeviceFind(device)
            }
        }
    }

    private val discoverableStateFilter: IntentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
    private val discoverableStateReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE)

            if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                listener?.onDiscoverableStart()
            } else {
                listener?.onDiscoverableFinish()
                context.unregisterReceiver(this)
            }
        }
    }

    private val scanningFinishedTask = Runnable {

        listener?.onDiscoveryFinish()

        context.unregisterReceiver(foundDeviceReceiver)
        if (adapter != null && adapter.isDiscovering) {
            adapter.cancelDiscovery();
        }
    }

    fun scanForDevices(seconds: Int) {

        adapter?.startDiscovery()
        listener?.onDiscoveryStart(seconds)

        handler.postDelayed(scanningFinishedTask, seconds.toLong() * 1000)
        context.registerReceiver(foundDeviceReceiver, foundDeviceFilter)
    }

    fun stopScanning() {
        handler.removeCallbacks(scanningFinishedTask)
        try {
            context.unregisterReceiver(foundDeviceReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        if (adapter != null && adapter.isDiscovering) {
            adapter.cancelDiscovery();
        }
    }

    fun getBondedDevices(): List<BluetoothDevice> {
        return ArrayList<BluetoothDevice>(adapter?.bondedDevices)
    }

    fun isBluetoothAvailable(): Boolean {
        return adapter != null
    }

    fun isBluetoothEnabled(): Boolean {
        return adapter?.isEnabled as Boolean
    }

    fun isDiscoverable(): Boolean {
        return adapter?.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
    }

    fun startDiscoverable() {
        context.registerReceiver(discoverableStateReceiver, discoverableStateFilter)
    }

    interface ScanningListener {
        fun onDiscoveryStart(seconds: Int)
        fun onDiscoveryFinish()
        fun onDiscoverableStart()
        fun onDiscoverableFinish()
        fun onDeviceFind(device: BluetoothDevice)
    }
}

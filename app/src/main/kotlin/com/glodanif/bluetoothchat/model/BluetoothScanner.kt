package com.glodanif.bluetoothchat.model

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class BluetoothScanner(val context: Context) {

    var listener: ScanningListener? = null

    private val timer = Timer("schedule")

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val filter: IntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device = intent
                        .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                listener?.onDeviceFind(device)
            }
        }
    }

    fun scanForDevices() {

        adapter?.startDiscovery()
        listener?.onDiscoveryStart()

        timer.schedule(60 * 1000) {
            listener?.onDiscoveryFinish()
            context.unregisterReceiver(receiver)
        }

        context.registerReceiver(receiver, filter)
    }

    fun stopScanning() {
        timer.cancel()
        context.unregisterReceiver(receiver)
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

    interface ScanningListener {
        fun onDiscoveryStart()
        fun onDiscoveryFinish()
        fun onDeviceFind(device: BluetoothDevice)
        fun onAvailableDiscoverChange(enabled: Boolean)
    }
}

package com.glodanif.bluetoothchat

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.glodanif.bluetoothchat.adapter.DevicesAdapter
import com.glodanif.bluetoothchat.model.BluetoothScanner
import com.glodanif.bluetoothchat.presenter.ScanPresenter
import com.glodanif.bluetoothchat.view.ScanView
import com.glodanif.bluetoothchat.view.custom.ExpiringProgressBar

class ScanActivity : AppCompatActivity(), ScanView {

    private val REQUEST_ENABLE_BLUETOOTH = 101
    private val REQUEST_MAKE_DISCOVERABLE = 102

    private lateinit var container: View
    private lateinit var turnOnHolder: View
    private lateinit var listHolder: View

    private lateinit var infoLabel: TextView
    private lateinit var discoveryLabel: TextView
    private lateinit var progressBar: ExpiringProgressBar
    private lateinit var makeDiscoverableButton: Button
    private lateinit var scanForDevicesButton: Button

    private lateinit var pairedDevicesList: RecyclerView

    private val adapter: DevicesAdapter = DevicesAdapter()

    private lateinit var presenter: ScanPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        presenter = ScanPresenter(this, BluetoothScanner(this))

        container = findViewById(R.id.fl_container)
        turnOnHolder = findViewById(R.id.cl_turn_on)
        listHolder = findViewById(R.id.cl_list)

        infoLabel = findViewById(R.id.tv_info) as TextView
        discoveryLabel = findViewById(R.id.tv_discovery_label) as TextView
        progressBar = findViewById(R.id.epb_progress) as ExpiringProgressBar

        makeDiscoverableButton = findViewById(R.id.btn_make_discoverable) as Button
        scanForDevicesButton = findViewById(R.id.btn_scan) as Button

        pairedDevicesList = findViewById(R.id.rv_paired_devices) as RecyclerView
        pairedDevicesList.layoutManager = LinearLayoutManager(this)
        pairedDevicesList.adapter = adapter

        presenter.checkBluetoothAvailability()

        findViewById(R.id.btn_turn_on).setOnClickListener { presenter.turnOnBluetooth() }
        makeDiscoverableButton.setOnClickListener { presenter.makeDiscoverable() }
        scanForDevicesButton.setOnClickListener { presenter.scanForDevices() }
    }

    override fun showPairedDevices(pairedDevices: List<BluetoothDevice>) {

        turnOnHolder.visibility = View.GONE
        listHolder.visibility = View.VISIBLE

        if (pairedDevices.isNotEmpty()) {
            adapter.pairedList = ArrayList(pairedDevices)
            adapter.notifyDataSetChanged()
        }
    }

    override fun showBluetoothFunctionality() {
        container.visibility = View.VISIBLE
        presenter.checkBluetoothEnabling()
    }

    override fun showBluetoothEnablingRequest() {
        turnOnHolder.visibility = View.VISIBLE
        listHolder.visibility = View.GONE
    }

    override fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
    }

    override fun showBluetoothIsNotAvailableMessage() {
        AlertDialog.Builder(this)
                .setMessage("Cannot get access to Bluetooth on your device")
                .setPositiveButton("OK", { _, _ -> finish() })
                .show()
    }

    override fun showBluetoothEnablingFailed() {
        AlertDialog.Builder(this)
                .setMessage("This app requires Bluetooth enabled to work properly")
                .setPositiveButton("OK", null)
                .show()
    }

    override fun requestMakingDiscoverable() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60)
        startActivityForResult(discoverableIntent, REQUEST_MAKE_DISCOVERABLE)
    }

    override fun discoverableInProcess() {
        makeDiscoverableButton.text = "Discoverable"
        makeDiscoverableButton.isEnabled = false
    }

    override fun discoverableFinished() {
        makeDiscoverableButton.text = "Make discoverable"
        makeDiscoverableButton.isEnabled = true
    }

    override fun scanningStarted(seconds: Int) {
        progressBar.runExpiring(seconds)
        progressBar.visibility = View.VISIBLE
        discoveryLabel.visibility = View.VISIBLE
        scanForDevicesButton.isEnabled = false
    }

    override fun scanningStopped() {
        progressBar.cancel()
        progressBar.visibility = View.GONE
        discoveryLabel.visibility = View.GONE
        scanForDevicesButton.isEnabled = true
    }

    override fun showBluetoothDiscoverableFailure() {
        AlertDialog.Builder(this)
                .setMessage("Unable to make your device discoverable")
                .setPositiveButton("OK", null)
                .show()
    }

    override fun addFoundDevice(device: BluetoothDevice) {
        adapter.addNewFoundDevice(device)
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.onPairedDevicesReady()
            } else {
                presenter.onBluetoothEnablingFailed()
            }
        } else if (requestCode == REQUEST_MAKE_DISCOVERABLE) {
            if (resultCode > 0) {
                presenter.onMadeDiscoverable()
            } else {
                presenter.onMakeDiscoverableFailed()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.cancelScanning()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        fun start(context: Context) =
                context.startActivity(Intent(context, ScanActivity::class.java))
    }
}

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
import com.glodanif.bluetoothchat.adapter.PairedDevicesAdapter
import com.glodanif.bluetoothchat.model.BluetoothScanner
import com.glodanif.bluetoothchat.presenter.ScanPresenter
import com.glodanif.bluetoothchat.view.ScanView

class ScanActivity : AppCompatActivity(), ScanView {

    private val REQUEST_ENABLE_BLUETOOTH = 101
    private val REQUEST_MAKE_DISCOVERABLE = 102

    private lateinit var container: View
    private lateinit var turnOnHolder: View
    private lateinit var listHolder: View

    private lateinit var pairedDevicesList: RecyclerView
    private val adapter: PairedDevicesAdapter = PairedDevicesAdapter()

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

        pairedDevicesList = findViewById(R.id.rv_paired_devices) as RecyclerView
        pairedDevicesList.layoutManager = LinearLayoutManager(this)
        pairedDevicesList.adapter = adapter

        presenter.checkBluetoothAvailability()

        findViewById(R.id.btn_turn_on).setOnClickListener { presenter.turnOnBluetooth() }
        findViewById(R.id.btn_make_discoverable).setOnClickListener { presenter.makeDiscoverable() }
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
                presenter.onMadeDiscoverable(resultCode)
            } else {
                presenter.onMakeDiscoverableFailed()
            }
        }
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

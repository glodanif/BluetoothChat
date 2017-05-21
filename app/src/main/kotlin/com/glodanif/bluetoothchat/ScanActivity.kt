package com.glodanif.bluetoothchat

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.glodanif.bluetoothchat.presenter.ScanPresenter
import com.glodanif.bluetoothchat.view.ScanView

class ScanActivity : AppCompatActivity(), ScanView {

    private val REQUEST_ENABLE_BT = 101

    private var container: View? = null

    private val presenter: ScanPresenter = ScanPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        container = findViewById(R.id.cl_container)

        presenter.checkBluetoothAvailability()

        findViewById(R.id.btn_turn_on).setOnClickListener { presenter.turnOnBluetooth() }
    }

    override fun showPairedDevices(pairedDevices: Set<BluetoothDevice>) {

        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                Log.e("TAG13", device.name + " " + device.address)
            }
        }
    }

    override fun showBluetoothFunctionality() {
        container?.visibility = View.VISIBLE
    }

    override fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    override fun handleBluetoothEnablingResponse(resultCode: Int) {

        if (resultCode == Activity.RESULT_OK) {
            container?.visibility = View.GONE
            presenter.getPairedDevices()
        } else {
            AlertDialog.Builder(this)
                    .setMessage("This app requires Bluetooth enabled to work properly")
                    .setPositiveButton("OK", null)
                    .show()
        }
    }

    override fun showBluetoothIsNotAvailableMessage() {

        AlertDialog.Builder(this)
                .setMessage("Cannot get access to Bluetooth on your device")
                .setPositiveButton("OK", { _, _ -> finish() })
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT) {
            handleBluetoothEnablingResponse(resultCode);
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

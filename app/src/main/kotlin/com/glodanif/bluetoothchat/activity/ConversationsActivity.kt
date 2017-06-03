package com.glodanif.bluetoothchat.activity

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.model.BluetoothConnectorImpl
import com.glodanif.bluetoothchat.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.view.ConversationsView

class ConversationsActivity : AppCompatActivity(), ConversationsView {

    private val REQUEST_SCAN = 101

    private lateinit var presenter: ConversationsPresenter
    private val connection: BluetoothConnector = BluetoothConnectorImpl(this)

    private var connectAction: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        presenter = ConversationsPresenter(this, connection)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            ScanActivity.startForResult(this@ConversationsActivity, REQUEST_SCAN)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun notifyAboutConnectedDevice(device: BluetoothDevice) {
        AlertDialog.Builder(this)
                .setMessage("${device.name} (${device.address}) has just connected to you")
                .setPositiveButton("Start chat", { _, _ -> presenter.startChat(device) })
                .setNegativeButton("Disconnect", { _, _ -> presenter.rejectConnection() })
                .setCancelable(false)
                .show()
    }

    override fun redirectToChat(device: BluetoothDevice) {
        ChatActivity.start(this, device)
    }

    override fun connectedToModel() {
        connectAction?.invoke()
        connectAction = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SCAN && resultCode == Activity.RESULT_OK) {
            val device = data
                    ?.getParcelableExtra<BluetoothDevice>(ScanActivity.EXTRA_BLUETOOTH_DEVICE)

            if (device != null) {
                connectAction = {
                    connection.connect(device)
                    ChatActivity.start(this, device)
                }
            }
        }
    }
}

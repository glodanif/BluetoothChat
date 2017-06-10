package com.glodanif.bluetoothchat.activity

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.adapter.ConversationsAdapter
import com.glodanif.bluetoothchat.entity.Conversation
import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.model.BluetoothConnectorImpl
import com.glodanif.bluetoothchat.model.ConversationsStorage
import com.glodanif.bluetoothchat.model.ConversationsStorageImpl
import com.glodanif.bluetoothchat.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.view.ConversationsView

class ConversationsActivity : AppCompatActivity(), ConversationsView {

    private val REQUEST_SCAN = 101

    private lateinit var presenter: ConversationsPresenter
    private val connection: BluetoothConnector = BluetoothConnectorImpl(this)
    private val storage: ConversationsStorage = ConversationsStorageImpl(this)

    private var connectAction: (() -> Unit)? = null

    private lateinit var conversationsList: RecyclerView
    private lateinit var noConversations: View
    private lateinit var addButton: FloatingActionButton

    private val adapter: ConversationsAdapter = ConversationsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        presenter = ConversationsPresenter(this, connection, storage)

        conversationsList = findViewById(R.id.rv_conversations) as RecyclerView
        noConversations = findViewById(R.id.ll_empty_holder)
        conversationsList.layoutManager = LinearLayoutManager(this)
        conversationsList.adapter = adapter

        addButton = findViewById(R.id.fab_new_conversation) as FloatingActionButton
        addButton.setOnClickListener {
            ScanActivity.startForResult(this@ConversationsActivity, REQUEST_SCAN)
        }
        findViewById(R.id.btn_scan).setOnClickListener {
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

    override fun showNoConversations() {
        conversationsList.visibility = View.GONE
        addButton.visibility = View.GONE
        noConversations.visibility = View.VISIBLE
    }

    override fun showConversations(conversations: List<Conversation>, connected: String?) {
        conversationsList.visibility = View.VISIBLE
        addButton.visibility = View.VISIBLE
        noConversations.visibility = View.GONE

        adapter.setData(ArrayList(conversations), connected)
        adapter.notifyDataSetChanged()
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

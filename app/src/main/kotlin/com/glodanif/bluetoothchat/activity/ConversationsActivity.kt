package com.glodanif.bluetoothchat.activity

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.EditText
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.model.BluetoothConnectorImpl
import com.glodanif.bluetoothchat.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.view.ConversationsView

class ConversationsActivity : AppCompatActivity(), ConversationsView {

    private val REQUEST_SCAN = 101

    private lateinit var presenter: ConversationsPresenter
    private val connection: BluetoothConnector = BluetoothConnectorImpl(this)

    private lateinit var messageField: EditText
    private lateinit var conversationView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        messageField = findViewById(R.id.et_message) as EditText
        conversationView = findViewById(R.id.tv_conversation) as TextView

        findViewById(R.id.btn_send).setOnClickListener {
            presenter.sendMessage(messageField.text.toString().trim())
        }

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
    }

    override fun showReceivedMessage(message: String) {
        conversationView.text = "${conversationView.text}\n<- $message"
    }

    override fun showSentMessage(message: String) {
        messageField.setText("")
        conversationView.text = "${conversationView.text}\n-> $message"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SCAN && resultCode == Activity.RESULT_OK) {
            val device = data
                    ?.getParcelableExtra<BluetoothDevice>(ScanActivity.EXTRA_BLUETOOTH_DEVICE)

            if (device != null) {
                connection.connect(device)
            }
        }
    }
}

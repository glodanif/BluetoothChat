package com.glodanif.bluetoothchat.activity

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.EditText
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.adapter.ChatAdapter
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.model.BluetoothConnectorImpl
import com.glodanif.bluetoothchat.presenter.ChatPresenter
import com.glodanif.bluetoothchat.view.ChatView

class ChatActivity : AppCompatActivity(), ChatView {

    private lateinit var presenter: ChatPresenter
    private val connectionModel: BluetoothConnector = BluetoothConnectorImpl(this)

    private lateinit var chatList: RecyclerView
    private lateinit var messageField: EditText

    private val adapter: ChatAdapter = ChatAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        messageField = findViewById(R.id.et_message) as EditText

        findViewById(R.id.ib_send).setOnClickListener {
            presenter.sendMessage(messageField.text.toString().trim())
        }

        chatList = findViewById(R.id.rv_chat) as RecyclerView
        chatList.layoutManager = LinearLayoutManager(this)
        chatList.adapter = adapter

        presenter = ChatPresenter(this, connectionModel)

        val device: BluetoothDevice = intent.getParcelableExtra(EXTRA_BLUETOOTH_DEVICE)
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun showMessagesHistory() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showReceivedMessage(message: ChatMessage) {
        adapter.messages.add(message)
        adapter.notifyDataSetChanged()
    }

    override fun showSentMessage(message: ChatMessage) {
        adapter.messages.add(message)
        adapter.notifyDataSetChanged()
    }

    companion object {

        val EXTRA_BLUETOOTH_DEVICE = "extra.bluetooth_device"

        fun start(context: Context, device: BluetoothDevice?) {
            val intent: Intent = Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_BLUETOOTH_DEVICE, device)
            context.startActivity(intent)
        }
    }
}

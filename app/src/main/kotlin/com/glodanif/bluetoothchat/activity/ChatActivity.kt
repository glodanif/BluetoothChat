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
import com.glodanif.bluetoothchat.model.MessagesStorage
import com.glodanif.bluetoothchat.model.MessagesStorageImpl
import com.glodanif.bluetoothchat.presenter.ChatPresenter
import com.glodanif.bluetoothchat.view.ChatView
import java.util.*

class ChatActivity : AppCompatActivity(), ChatView {

    private lateinit var presenter: ChatPresenter
    private val connectionModel: BluetoothConnector = BluetoothConnectorImpl(this)
    private val storageModel: MessagesStorage = MessagesStorageImpl(this)

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
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        chatList.layoutManager = layoutManager
        chatList.adapter = adapter

        val device: BluetoothDevice = intent.getParcelableExtra(EXTRA_BLUETOOTH_DEVICE)
        toolbar.title = device.name
        toolbar.subtitle = "Waiting for opponent"

        presenter = ChatPresenter(device.address, this, connectionModel, storageModel)
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun showMessagesHistory(messages: List<ChatMessage>) {
        adapter.messages = LinkedList(messages)
        adapter.notifyDataSetChanged()
    }

    override fun showReceivedMessage(message: ChatMessage) {
        adapter.messages.addFirst(message)
        adapter.notifyDataSetChanged()
    }

    override fun showSentMessage(message: ChatMessage) {
        adapter.messages.addFirst(message)
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

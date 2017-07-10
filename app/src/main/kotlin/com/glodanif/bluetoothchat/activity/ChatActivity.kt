package com.glodanif.bluetoothchat.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.adapter.ChatAdapter
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.entity.Conversation
import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.model.BluetoothConnectorImpl
import com.glodanif.bluetoothchat.model.MessagesStorage
import com.glodanif.bluetoothchat.model.MessagesStorageImpl
import com.glodanif.bluetoothchat.presenter.ChatPresenter
import com.glodanif.bluetoothchat.view.ChatView
import com.glodanif.bluetoothchat.widget.ActionView
import java.util.*

class ChatActivity : AppCompatActivity(), ChatView {

    private lateinit var presenter: ChatPresenter
    private val connectionModel: BluetoothConnector = BluetoothConnectorImpl(this)
    private val storageModel: MessagesStorage = MessagesStorageImpl(this)

    private val layoutManager = LinearLayoutManager(this)
    private lateinit var actions: ActionView
    private lateinit var chatList: RecyclerView
    private lateinit var messageField: EditText
    private lateinit var toolbar: Toolbar

    private val adapter: ChatAdapter = ChatAdapter()

    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        actions = findViewById(R.id.av_actions) as ActionView
        messageField = findViewById(R.id.et_message) as EditText

        findViewById(R.id.ib_send).setOnClickListener {
            presenter.sendMessage(messageField.text.toString().trim())
            messageField.text = null
        }

        chatList = findViewById(R.id.rv_chat) as RecyclerView
        layoutManager.reverseLayout = true
        chatList.layoutManager = layoutManager
        chatList.adapter = adapter

        val deviceName: String = intent.getStringExtra(EXTRA_NAME)
        val deviceAddress: String = intent.getStringExtra(EXTRA_ADDRESS)
        title = deviceName
        toolbar.subtitle = "Not connected"

        presenter = ChatPresenter(deviceAddress, this, connectionModel, storageModel)
    }

    override fun onStart() {
        super.onStart()
        isStarted = true
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        isStarted = false
        presenter.onStop()
    }

    override fun showNotConnectedToThisDevice(currentDevice: String) {
        actions.visibility = View.VISIBLE
        actions.setActions("You are currently connected to $currentDevice. Do you want to disconnect from it?",
                ActionView.Action("Connect") { presenter.connectToDevice() },
                ActionView.Action("Dismiss") { hideActions() }
        )
    }

    override fun showNotConnectedToAnyDevice() {
        actions.visibility = View.VISIBLE
        actions.setActions("You are not connected to this device right now. Do you want to connect to it?",
                ActionView.Action("Connect") { presenter.connectToDevice() },
                ActionView.Action("Dismiss") { hideActions() }
        )
    }

    override fun showWainingForOpponent() {
        actions.visibility = View.VISIBLE
        actions.setActions("You are waiting for this device to approve a connection.",
                ActionView.Action("Cancel") { presenter.disconnect() },
                null
        )
    }

    override fun notifyAboutConnectedDevice(conversation: Conversation) {
        actions.visibility = View.VISIBLE
        actions.setActions("${conversation.displayName} (${conversation.deviceName}) has just connected to you",
                ActionView.Action("Start chat") { presenter.acceptConnection() },
                ActionView.Action("Disconnect") { presenter.rejectConnection() }
        )
    }

    override fun showServiceDestroyed() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage("Bluetooth Chat service just has been stopped, restart the service to be able to use the app")
                .setPositiveButton("Restart", { _, _ -> presenter.onStart() })
                .setCancelable(false)
                .show()
    }

    override fun hideActions() {
        actions.visibility = View.GONE
    }

    override fun showMessagesHistory(messages: List<ChatMessage>) {
        adapter.messages = LinkedList(messages)
        adapter.notifyDataSetChanged()
    }

    override fun showReceivedMessage(message: ChatMessage) {
        adapter.messages.addFirst(message)
        adapter.notifyItemInserted(0)
        layoutManager.scrollToPosition(0)
    }

    override fun showSentMessage(message: ChatMessage) {
        adapter.messages.addFirst(message)
        adapter.notifyItemInserted(0)
        layoutManager.scrollToPosition(0)
    }

    override fun showConnected() {
        toolbar.subtitle = "Connected"
        actions.visibility = View.GONE
    }

    override fun showAcceptedConnection() {
        toolbar.subtitle = "Connected"
        actions.visibility = View.GONE
    }

    override fun showRejectedConnection() {

        toolbar.subtitle = "Not connected"

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage("Your connection request was rejected")
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show()
    }

    override fun showConnectionRequest(conversation: Conversation) {
        actions.visibility = View.VISIBLE
        actions.setActions("${conversation.displayName} (${conversation.deviceName}) has just connected to you",
                ActionView.Action("Start chat") { presenter.acceptConnection() },
                ActionView.Action("Disconnect") { presenter.rejectConnection() }
        )
    }

    override fun showLostConnection() {

        toolbar.subtitle = "Not connected"

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage("Connection with this device was lost. Do you want to reconnect?")
                .setPositiveButton("Reconnect", { _,_ -> presenter.reconnect()})
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show()
    }

    override fun showDisconnected() {

        toolbar.subtitle = "Not connected"

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage("Your opponent disconnected from your device. Do you want to reconnect?")
                .setPositiveButton("Reconnect", { _,_ -> presenter.reconnect()})
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show()
    }

    override fun showFailedConnection() {

        toolbar.subtitle = "Not connected"

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage("Unable to connect to this device, do you want to try again?")
                .setPositiveButton("Try again", { _,_ -> presenter.connectToDevice()})
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show()

    }

    override fun showNotValidMessage() {
        Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
    }

    override fun showDeviceIsNotAvailable() {
        Toast.makeText(this, "This device is not available right now, make sure it's paired", Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_disconnect -> {
                presenter.disconnect()
                return true
            }
            else -> return onOptionsItemSelected(item)
        }
    }

    companion object {

        val EXTRA_NAME = "extra.name"
        val EXTRA_ADDRESS = "extra.address"

        fun start(context: Context, name: String, address: String) {
            val intent: Intent = Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_NAME, name)
                    .putExtra(EXTRA_ADDRESS, address)
            context.startActivity(intent)
        }
    }
}

package com.glodanif.bluetoothchat.activity

import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
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
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.presenter.ChatPresenter
import com.glodanif.bluetoothchat.view.ChatView
import com.glodanif.bluetoothchat.widget.ActionView
import java.util.*

class ChatActivity : AppCompatActivity(), ChatView {

    private lateinit var presenter: ChatPresenter
    private val connectionModel: BluetoothConnector = BluetoothConnectorImpl(this)
    private val scanModel: BluetoothScanner = BluetoothScannerImpl(this)
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
        }

        chatList = findViewById(R.id.rv_chat) as RecyclerView
        layoutManager.reverseLayout = true
        chatList.layoutManager = layoutManager
        chatList.adapter = adapter

        val deviceName: String? = intent.getStringExtra(EXTRA_NAME)
        val deviceAddress: String? = intent.getStringExtra(EXTRA_ADDRESS)
        title = deviceName
        toolbar.subtitle = getString(R.string.chat__not_connected)
        presenter = ChatPresenter(deviceAddress.toString(),
                this, scanModel, connectionModel, storageModel)
    }

    override fun onStart() {
        super.onStart()
        isStarted = true
        presenter.prepareConnection()
    }

    override fun onStop() {
        super.onStop()
        isStarted = false
        presenter.releaseConnection()
    }

    override fun showStatusConnected() {
        toolbar.subtitle = getString(R.string.chat__connected)
    }

    override fun showStatusNotConnected() {
        toolbar.subtitle = getString(R.string.chat__not_connected)
    }

    override fun showStatusPending() {
        toolbar.subtitle = getString(R.string.chat__pending)
    }

    override fun showNotConnectedToSend() =
            Toast.makeText(this, getString(R.string.chat__not_connected_to_send), Toast.LENGTH_LONG).show()

    override fun afterMessageSent() {
        messageField.text = null
    }

    override fun showNotConnectedToThisDevice(currentDevice: String) {

        actions.visibility = View.VISIBLE
        actions.setActions("You are currently connected to $currentDevice. You can connect to this device (current connection will be interrupted)?",
                ActionView.Action(getString(R.string.chat__connect)) { presenter.connectToDevice() },
                null
        )
    }

    override fun showNotConnectedToAnyDevice() {

        actions.visibility = View.VISIBLE
        actions.setActions("You are not connected to this device right now.",
                ActionView.Action(getString(R.string.chat__connect)) { presenter.connectToDevice() },
                null
        )
    }

    override fun showWainingForOpponent() {

        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.chat__waiting_for_device),
                ActionView.Action(getString(R.string.general__cancel)) { presenter.resetConnection() },
                null
        )
    }

    override fun notifyAboutConnectedDevice(conversation: Conversation) {

        actions.visibility = View.VISIBLE
        actions.setActions("${conversation.displayName} (${conversation.deviceName}) has just connected to you",
                ActionView.Action(getString(R.string.general__start_chat)) { presenter.acceptConnection() },
                ActionView.Action(getString(R.string.chat__disconnect)) { presenter.rejectConnection() }
        )
    }

    override fun showServiceDestroyed() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage("Bluetooth Chat service just has been stopped, restart the service to be able to use the app")
                .setPositiveButton(getString(R.string.general__restart), { _, _ -> presenter.prepareConnection() })
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

    override fun showRejectedConnection() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__connection_rejected))
                .setPositiveButton(getString(R.string.general__ok), null)
                .setCancelable(false)
                .show()
    }

    override fun showConnectionRequest(conversation: Conversation) {

        actions.visibility = View.VISIBLE
        actions.setActions("${conversation.displayName} (${conversation.deviceName}) has just connected to you",
                ActionView.Action(getString(R.string.general__start_chat)) { presenter.acceptConnection() },
                ActionView.Action(getString(R.string.chat__disconnect)) { presenter.rejectConnection() }
        )
    }

    override fun showLostConnection() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage("Connection with this device was lost. Do you want to reconnect?")
                .setPositiveButton(getString(R.string.chat__reconnect), { _, _ -> presenter.reconnect() })
                .setNegativeButton(getString(R.string.general__cancel), null)
                .setCancelable(false)
                .show()
    }

    override fun showDisconnected() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage("Your opponent disconnected from your device. Do you want to reconnect?")
                .setPositiveButton(getString(R.string.chat__reconnect), { _, _ -> presenter.reconnect() })
                .setNegativeButton(getString(R.string.general__cancel), null)
                .setCancelable(false)
                .show()
    }

    override fun showFailedConnection() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage("Unable to connect to this device, do you want to try again?")
                .setPositiveButton(getString(R.string.general__try_again), { _, _ -> presenter.connectToDevice() })
                .setNegativeButton(getString(R.string.general__cancel), null)
                .setCancelable(false)
                .show()

    }

    override fun showNotValidMessage() {
        Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
    }

    override fun showDeviceIsNotAvailable() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage("This device is not available right now, make sure it's paired.")
                .setPositiveButton(getString(R.string.general__ok), null)
                .setCancelable(false)
                .show()
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

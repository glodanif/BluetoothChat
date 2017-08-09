package com.glodanif.bluetoothchat.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.adapter.ChatAdapter
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.entity.Conversation
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.presenter.ChatPresenter
import com.glodanif.bluetoothchat.view.ChatView
import com.glodanif.bluetoothchat.view.NotificationView
import com.glodanif.bluetoothchat.widget.ActionView
import java.util.*

class ChatActivity : SkeletonActivity(), ChatView {

    private val REQUEST_ENABLE_BLUETOOTH = 101

    private lateinit var presenter: ChatPresenter
    private val connectionModel: BluetoothConnector = BluetoothConnectorImpl(this)
    private val scanModel: BluetoothScanner = BluetoothScannerImpl(this)
    private val storageModel: MessagesStorage = MessagesStorageImpl(this)
    private val conversationModel: ConversationsStorage = ConversationsStorageImpl(this)

    private val layoutManager = LinearLayoutManager(this)
    private lateinit var actions: ActionView
    private lateinit var chatList: RecyclerView
    private lateinit var messageField: EditText

    private val adapter: ChatAdapter = ChatAdapter(this)

    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat, ActivityType.CHILD_ACTIVITY)

        toolbar?.setTitleTextAppearance(this, R.style.ActionBar_TitleTextStyle)
        toolbar?.setSubtitleTextAppearance(this, R.style.ActionBar_SubTitleTextStyle)

        actions = findViewById<ActionView>(R.id.av_actions)
        messageField = findViewById<EditText>(R.id.et_message)

        findViewById<ImageButton>(R.id.ib_send).setOnClickListener {
            presenter.sendMessage(messageField.text.toString().trim())
        }

        chatList = findViewById<RecyclerView>(R.id.rv_chat)
        layoutManager.reverseLayout = true
        chatList.layoutManager = layoutManager
        chatList.adapter = adapter

        val deviceAddress: String? = intent.getStringExtra(EXTRA_ADDRESS)

        title = if (deviceAddress.isNullOrEmpty()) getString(R.string.app_name) else  deviceAddress
        toolbar?.subtitle = getString(R.string.chat__not_connected)
        presenter = ChatPresenter(deviceAddress.toString(),
                this, scanModel, connectionModel, conversationModel, storageModel)
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

    override fun dismissMessageNotification() {
        (getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(NotificationView.NOTIFICATION_TAG_MESSAGE, NotificationView.NOTIFICATION_ID_MESSAGE)
    }

    override fun showPartnerName(name: String, device: String) {
        title = "$name ($device)"
    }

    override fun showStatusConnected() {
        toolbar?.subtitle = getString(R.string.chat__connected)
    }

    override fun showStatusNotConnected() {
        toolbar?.subtitle = getString(R.string.chat__not_connected)
    }

    override fun showStatusPending() {
        toolbar?.subtitle = getString(R.string.chat__pending)
    }

    override fun showNotConnectedToSend() =
            Toast.makeText(this, getString(R.string.chat__not_connected_to_send), Toast.LENGTH_LONG).show()

    override fun afterMessageSent() {
        messageField.text = null
    }

    override fun showNotConnectedToThisDevice(currentDevice: String) {

        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.chat__connected_to_another, currentDevice),
                ActionView.Action(getString(R.string.chat__connect)) { presenter.connectToDevice() },
                null
        )
    }

    override fun showNotConnectedToAnyDevice() {

        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.chat__not_connected_to_this_device),
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

    override fun showConnectionRequest(conversation: Conversation) {

        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.chat__connection_request, conversation.displayName, conversation.deviceName),
                ActionView.Action(getString(R.string.general__start_chat)) { presenter.acceptConnection() },
                ActionView.Action(getString(R.string.chat__disconnect)) { presenter.rejectConnection() }
        )
    }

    override fun showServiceDestroyed() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.general__service_lost))
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

    override fun showBluetoothDisabled() {
        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.chat__bluetooth_is_disabled),
                ActionView.Action(getString(R.string.chat__enable)) { presenter.enableBluetooth() },
                null
        )
    }

    override fun showLostConnection() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__connection_lost))
                .setPositiveButton(getString(R.string.chat__reconnect), { _, _ -> presenter.reconnect() })
                .setNegativeButton(getString(R.string.general__cancel), null)
                .setCancelable(false)
                .show()
    }

    override fun showDisconnected() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__partner_disconnected))
                .setPositiveButton(getString(R.string.chat__reconnect), { _, _ -> presenter.reconnect() })
                .setNegativeButton(getString(R.string.general__cancel), null)
                .setCancelable(false)
                .show()
    }

    override fun showFailedConnection() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__unable_to_connect))
                .setPositiveButton(getString(R.string.general__try_again), { _, _ -> presenter.connectToDevice() })
                .setNegativeButton(getString(R.string.general__cancel), null)
                .setCancelable(false)
                .show()

    }

    override fun showNotValidMessage() {
        Toast.makeText(this, getString(R.string.chat__message_cannot_be_empty), Toast.LENGTH_SHORT).show()
    }

    override fun showDeviceIsNotAvailable() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__device_is_not_available))
                .setPositiveButton(getString(R.string.chat__rescan), { _, _ -> ScanActivity.start(this) })
                .show()
    }

    override fun requestBluetoothEnabling() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
    }

    override fun showBluetoothEnablingFailed() {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__bluetooth_required))
                .setPositiveButton(getString(R.string.general__ok), null)
                .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.onBluetoothEnabled()
            } else {
                presenter.onBluetoothEnablingFailed()
            }
        }
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

        val EXTRA_ADDRESS = "extra.address"

        fun start(context: Context, address: String) {
            val intent: Intent = Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_ADDRESS, address)
            context.startActivity(intent)
        }
    }
}

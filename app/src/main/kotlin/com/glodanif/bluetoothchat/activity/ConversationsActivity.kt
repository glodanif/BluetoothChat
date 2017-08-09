package com.glodanif.bluetoothchat.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.adapter.ConversationsAdapter
import com.glodanif.bluetoothchat.entity.Conversation
import com.glodanif.bluetoothchat.extension.getFirstLetter
import com.glodanif.bluetoothchat.model.*
import com.glodanif.bluetoothchat.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.view.ConversationsView
import com.glodanif.bluetoothchat.view.NotificationView
import com.glodanif.bluetoothchat.widget.ActionView
import com.glodanif.bluetoothchat.widget.SettingsPopup

class ConversationsActivity : AppCompatActivity(), ConversationsView {

    private val REQUEST_SCAN = 101

    private lateinit var presenter: ConversationsPresenter
    private lateinit var settings: SettingsManager
    private val connection: BluetoothConnector = BluetoothConnectorImpl(this)
    private val storage: ConversationsStorage = ConversationsStorageImpl(this)

    private lateinit var conversationsList: RecyclerView
    private lateinit var noConversations: View
    private lateinit var addButton: FloatingActionButton
    private lateinit var actions: ActionView
    private lateinit var userAvatar: ImageView
    private lateinit var optionsButton: View

    private lateinit var settingsPopup: SettingsPopup

    private val adapter: ConversationsAdapter = ConversationsAdapter(this)

    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_conversations)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        settings = SettingsManagerImpl(this)
        presenter = ConversationsPresenter(this, connection, storage, settings)

        actions = findViewById<ActionView>(R.id.av_actions)

        userAvatar = findViewById<ImageView>(R.id.iv_avatar)
        conversationsList = findViewById<RecyclerView>(R.id.rv_conversations)
        noConversations = findViewById(R.id.ll_empty_holder)
        optionsButton = findViewById(R.id.ll_options)
        addButton = findViewById<FloatingActionButton>(R.id.fab_new_conversation)

        conversationsList.layoutManager = LinearLayoutManager(this)
        conversationsList.adapter = adapter

        settingsPopup = SettingsPopup(this)
        settingsPopup.setCallbacks(
                profileClickListener = { ProfileActivity.start(context = this, editMode = true) },
                settingsClickListener = { SettingsActivity.start(context = this) }
        )

        adapter.clickListener = { ChatActivity.start(this, it.deviceAddress) }
        adapter.longClickListener = {
            conversation, isCurrent ->
            showContextMenu(conversation, isCurrent)
        }

        addButton.setOnClickListener {
            ScanActivity.startForResult(this, REQUEST_SCAN)
        }
        findViewById<Button>(R.id.btn_scan).setOnClickListener {
            ScanActivity.startForResult(this, REQUEST_SCAN)
        }
        optionsButton.setOnClickListener { v ->
            settingsPopup.show(v)
        }
    }

    private fun showContextMenu(conversation: Conversation, isCurrent: Boolean) {

        val labels = if (!isCurrent) {
            arrayOf(getString(R.string.conversations__remove))
        } else {
            arrayOf(getString(R.string.conversations__remove), getString(R.string.general__disconnect))
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.conversations__options))
                .setItems(labels, { _, which ->
                    when (which) {
                        0 -> {
                            confirmRemoval(conversation)
                        }
                        1 -> {
                            presenter.disconnect()
                        }
                    }
                })
        builder.create().show()
    }

    private fun confirmRemoval(conversation: Conversation) {

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.conversations__removal_confirmation))
                .setPositiveButton(getString(R.string.general__yes), { _, _ -> presenter.removeConversation(conversation) })
                .setNegativeButton(getString(R.string.general__no), null)
                .show()
    }

    override fun onStart() {
        super.onStart()
        isStarted = true
        presenter.prepareConnection()
        presenter.loadUserProfile()
    }

    override fun dismissConversationNotification() {
        (getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(NotificationView.NOTIFICATION_TAG_CONNECTION, NotificationView.NOTIFICATION_ID_CONNECTION)
    }

    override fun onStop() {
        super.onStop()
        isStarted = false
        presenter.releaseConnection()
    }

    override fun hideActions() {
        actions.visibility = View.GONE
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

    override fun showServiceDestroyed() {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.general__service_lost))
                .setPositiveButton(getString(R.string.general__restart), { _, _ ->
                    presenter.prepareConnection()
                    presenter.loadUserProfile()
                })
                .setCancelable(false)
                .show()
    }

    override fun refreshList(connected: String?) {
        adapter.setCurrentConversation(connected)
        adapter.notifyDataSetChanged()
    }

    override fun notifyAboutConnectedDevice(conversation: Conversation) {

        actions.visibility = View.VISIBLE
        actions.setActions(getString(R.string.conversations__connection_request, conversation.displayName, conversation.deviceName),
                ActionView.Action(getString(R.string.general__start_chat)) { presenter.startChat(conversation) },
                ActionView.Action(getString(R.string.general__disconnect)) { presenter.rejectConnection() }
        )
    }

    override fun showRejectedNotification(conversation: Conversation) {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.conversations__connection_rejected,
                        conversation.displayName, conversation.deviceName))
                .setPositiveButton(getString(R.string.general__ok), null)
                .setCancelable(false)
                .show()
    }

    override fun redirectToChat(conversation: Conversation) {
        ChatActivity.start(this, conversation.deviceAddress)
    }

    override fun showUserProfile(name: String, color: Int) {
        val drawable = TextDrawable.builder().buildRound(name.getFirstLetter(), color)
        userAvatar.setImageDrawable(drawable)
        settingsPopup.populateData(name, color)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SCAN && resultCode == Activity.RESULT_OK) {
            val device = data
                    ?.getParcelableExtra<BluetoothDevice>(ScanActivity.EXTRA_BLUETOOTH_DEVICE)

            if (device != null) {
                ChatActivity.start(this, device.address)
            }
        }
    }

    companion object {

        fun start(context: Context) =
                context.startActivity(Intent(context, ConversationsActivity::class.java))
    }
}

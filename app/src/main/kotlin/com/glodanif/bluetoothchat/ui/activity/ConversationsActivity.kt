package com.glodanif.bluetoothchat.ui.activity

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.model.BluetoothConnectorImpl
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.extension.getFirstLetter
import com.glodanif.bluetoothchat.ui.adapter.ConversationsAdapter
import com.glodanif.bluetoothchat.ui.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.ui.view.ConversationsView
import com.glodanif.bluetoothchat.ui.view.NotificationView
import com.glodanif.bluetoothchat.ui.widget.ActionView
import com.glodanif.bluetoothchat.ui.widget.SettingsPopup
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import com.glodanif.bluetoothchat.ui.widget.ShortcutManagerImpl
import java.util.*
import javax.inject.Inject

class ConversationsActivity : SkeletonActivity(), ConversationsView {

    private val REQUEST_SCAN = 101

    @Inject
    lateinit var presenter: ConversationsPresenter
    private lateinit var shortcutsManager: ShortcutManager

    private lateinit var conversationsList: RecyclerView
    private lateinit var noConversations: View
    private lateinit var addButton: FloatingActionButton
    private lateinit var actions: ActionView
    private lateinit var userAvatar: ImageView
    private lateinit var optionsButton: View

    private lateinit var settingsPopup: SettingsPopup
    private lateinit var storagePermissionDialog: AlertDialog

    private val adapter: ConversationsAdapter = ConversationsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations, ActivityType.CUSTOM_TOOLBAR_ACTIVITY)
        ComponentsManager.injectConversations(this)

        shortcutsManager = ShortcutManagerImpl(this)

        actions = findViewById(R.id.av_actions)

        userAvatar = findViewById(R.id.iv_avatar)
        conversationsList = findViewById(R.id.rv_conversations)
        noConversations = findViewById(R.id.ll_empty_holder)
        optionsButton = findViewById(R.id.ll_options)
        addButton = findViewById(R.id.fab_new_conversation)

        conversationsList.layoutManager = LinearLayoutManager(this)
        conversationsList.adapter = adapter

        settingsPopup = SettingsPopup(this)
        settingsPopup.setCallbacks(
                profileClickListener = { ProfileActivity.start(context = this, editMode = true) },
                imagesClickListener = { ReceivedImagesActivity.start(context = this, address = null) },
                settingsClickListener = { SettingsActivity.start(context = this) }
        )

        adapter.clickListener = { ChatActivity.start(this, it.deviceAddress) }
        adapter.longClickListener = { conversation, isCurrent ->
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

        if (intent.action == Intent.ACTION_SEND) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            ContactChooserActivity.start(this, if (text.isNullOrEmpty()) "" else text)
        }

        storagePermissionDialog = AlertDialog.Builder(this)
                .setView(R.layout.dialog_storage_permission)
                .setPositiveButton(R.string.general__ok) { _, _ ->
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
                }
                .setNegativeButton(R.string.general__exit, { _, _ -> finish() })
                .setCancelable(false)
                .create()

        ShortcutManagerImpl(this).addSearchShortcut()
    }

    private fun showContextMenu(conversation: Conversation, isCurrent: Boolean) {

        val labels = ArrayList<String>()
        labels.add(getString(R.string.conversations__remove))
        if (isCurrent) {
            labels.add(getString(R.string.general__disconnect))
        }
        if (shortcutsManager.isRequestPinShortcutSupported()) {
            labels.add(getString(R.string.conversations__pin_to_home_screen))
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.conversations__options))
                .setItems(labels.toTypedArray(), { _, which ->
                    when (which) {
                        0 -> {
                            confirmRemoval(conversation)
                        }
                        1 -> {
                            if (isCurrent) {
                                presenter.disconnect()
                            } else {
                                requestPinShortcut(conversation)
                            }
                        }
                        2 -> {
                            requestPinShortcut(conversation)
                        }
                    }
                })
        builder.create().show()
    }

    private fun requestPinShortcut(conversation: Conversation) {
        shortcutsManager.requestPinConversationShortcut(
                conversation.deviceAddress, conversation.displayName, conversation.color)
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

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && !storagePermissionDialog.isShowing) {
            storagePermissionDialog.show()
        }

        presenter.prepareConnection()
        presenter.loadUserProfile()
    }

    override fun dismissConversationNotification() {
        (getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(NotificationView.NOTIFICATION_TAG_CONNECTION, NotificationView.NOTIFICATION_ID_CONNECTION)
    }

    override fun onStop() {
        super.onStop()
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

        if (!isStarted()) return

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

        if (!isStarted()) return

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

    override fun removeFromShortcuts(address: String) {
        shortcutsManager.removeConversationShortcut(address)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED && !storagePermissionDialog.isShowing) {

            if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[0])) {

                AlertDialog.Builder(this)
                        .setMessage(Html.fromHtml("Go to Settings and grant the <b>STORAGE</b> permission to use this app."))
                        .setPositiveButton("Settings") { _, _ ->

                            val intent = Intent()
                                    .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .addCategory(Intent.CATEGORY_DEFAULT)
                                    .setData(Uri.parse("package:" + packageName))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                            startActivity(intent)
                        }
                        .setCancelable(false)
                        .show()
            } else {
                storagePermissionDialog.show()
            }
        }
    }

    companion object {

        private const val REQUEST_STORAGE_PERMISSION = 101

        fun start(context: Context) =
                context.startActivity(Intent(context, ConversationsActivity::class.java))
    }
}

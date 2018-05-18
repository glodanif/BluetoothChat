package com.glodanif.bluetoothchat.ui.activity

import android.Manifest
import android.app.Activity
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
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.ui.adapter.ConversationsAdapter
import com.glodanif.bluetoothchat.ui.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.ui.view.ConversationsView
import com.glodanif.bluetoothchat.ui.view.NotificationView
import com.glodanif.bluetoothchat.ui.viewmodel.ConversationViewModel
import com.glodanif.bluetoothchat.ui.widget.ActionView
import com.glodanif.bluetoothchat.ui.widget.SettingsPopup
import com.glodanif.bluetoothchat.ui.widget.ShortcutManager
import com.glodanif.bluetoothchat.utils.bind
import com.glodanif.bluetoothchat.utils.getFilePath
import com.glodanif.bluetoothchat.utils.getFirstLetter
import com.glodanif.bluetoothchat.utils.getNotificationManager
import com.kobakei.ratethisapp.RateThisApp
import javax.inject.Inject

class ConversationsActivity : SkeletonActivity(), ConversationsView {

    @Inject
    internal lateinit var presenter: ConversationsPresenter
    @Inject
    internal lateinit var shortcutsManager: ShortcutManager

    private val conversationsList: RecyclerView by bind(R.id.rv_conversations)
    private val noConversations: View by bind(R.id.ll_empty_holder)
    private val addButton: FloatingActionButton by bind(R.id.fab_new_conversation)
    private val actions: ActionView by bind(R.id.av_actions)
    private val userAvatar: ImageView by bind(R.id.iv_avatar)

    private lateinit var settingsPopup: SettingsPopup
    private lateinit var storagePermissionDialog: AlertDialog

    private val conversationsAdapter = ConversationsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations, ActivityType.CUSTOM_TOOLBAR_ACTIVITY)
        ComponentsManager.injectConversations(this)
        lifecycle.addObserver(presenter)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        conversationsList.layoutManager = LinearLayoutManager(this)
        conversationsList.adapter = conversationsAdapter

        settingsPopup = SettingsPopup(this)
        settingsPopup.setCallbacks(
                profileClickListener = { ProfileActivity.start(this, editMode = true) },
                imagesClickListener = { ReceivedImagesActivity.start(this, address = null) },
                settingsClickListener = { SettingsActivity.start(this) },
                aboutClickListener = { AboutActivity.start(this) }
        )

        conversationsAdapter.clickListener = { ChatActivity.start(this, it.address) }
        conversationsAdapter.longClickListener = { conversation, isCurrent ->
            showContextMenu(conversation, isCurrent)
        }

        addButton.setOnClickListener {
            ScanActivity.startForResult(this, REQUEST_SCAN)
        }

        findViewById<Button>(R.id.btn_scan).setOnClickListener {
            ScanActivity.startForResult(this, REQUEST_SCAN)
        }

        findViewById<View>(R.id.ll_options).setOnClickListener {
            settingsPopup.show(it)
        }

        if (intent.action == Intent.ACTION_SEND && intent.type != null) {

            var textToShare: String? = null
            var fileToShare: String? = null

            if ("text/plain" == intent.type) {
                textToShare = intent.getStringExtra(Intent.EXTRA_TEXT).trim()
            } else if (intent.type.startsWith("image/")) {
                val imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                fileToShare = imageUri.getFilePath(this)
            }

            ContactChooserActivity.start(this, textToShare, fileToShare)
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

        shortcutsManager.addSearchShortcut()

        RateThisApp.onCreate(this)
        RateThisApp.showRateDialogIfNeeded(this)
    }

    private fun showContextMenu(conversation: ConversationViewModel, isCurrent: Boolean) {

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
                            confirmRemoval(conversation.address)
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

    private fun requestPinShortcut(conversation: ConversationViewModel) {
        shortcutsManager.requestPinConversationShortcut(
                conversation.address, conversation.displayName, conversation.color)
    }

    private fun confirmRemoval(address: String) {

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.conversations__removal_confirmation))
                .setPositiveButton(getString(R.string.general__yes), { _, _ -> presenter.removeConversation(address) })
                .setNegativeButton(getString(R.string.general__no), null)
                .show()
    }

    override fun onStart() {
        super.onStart()

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && !storagePermissionDialog.isShowing) {
            storagePermissionDialog.show()
        }
    }

    override fun dismissConversationNotification() {
        getNotificationManager()
                .cancel(NotificationView.NOTIFICATION_TAG_CONNECTION, NotificationView.NOTIFICATION_ID_CONNECTION)
    }

    override fun hideActions() {
        actions.visibility = View.GONE
    }

    override fun showNoConversations() {
        conversationsList.visibility = View.GONE
        addButton.visibility = View.GONE
        noConversations.visibility = View.VISIBLE
    }

    override fun showConversations(conversations: List<ConversationViewModel>, connected: String?) {

        conversationsList.visibility = View.VISIBLE
        addButton.visibility = View.VISIBLE
        noConversations.visibility = View.GONE

        conversationsAdapter.setData(ArrayList(conversations), connected)
        conversationsAdapter.notifyDataSetChanged()
    }

    override fun showServiceDestroyed() = doIfStarted {
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
        conversationsAdapter.setCurrentConversation(connected)
        conversationsAdapter.notifyDataSetChanged()
    }

    override fun notifyAboutConnectedDevice(conversation: ConversationViewModel) {
        actions.setActionsAndShow(getString(R.string.conversations__connection_request, conversation.displayName, conversation.deviceName),
                ActionView.Action(getString(R.string.general__start_chat)) { presenter.startChat(conversation) },
                ActionView.Action(getString(R.string.general__disconnect)) { presenter.rejectConnection() }
        )
    }

    override fun showRejectedNotification(conversation: ConversationViewModel) = doIfStarted {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.conversations__connection_rejected,
                        conversation.displayName, conversation.deviceName))
                .setPositiveButton(getString(R.string.general__ok), null)
                .setCancelable(false)
                .show()
    }

    override fun redirectToChat(conversation: ConversationViewModel) {
        ChatActivity.start(this, conversation.address)
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !shouldShowRequestPermissionRationale(permissions[0])) {

                AlertDialog.Builder(this)
                        .setMessage(Html.fromHtml(getString(R.string.conversations__storage_permission)))
                        .setPositiveButton(getString(R.string.conversations__permissions_settings)) { _, _ ->

                            val intent = Intent()
                                    .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .addCategory(Intent.CATEGORY_DEFAULT)
                                    .setData(Uri.parse("package:$packageName"))
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
        private const val REQUEST_SCAN = 102

        fun start(context: Context) =
                context.startActivity(Intent(context, ConversationsActivity::class.java))
    }
}

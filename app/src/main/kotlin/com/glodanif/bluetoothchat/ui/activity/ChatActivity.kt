package com.glodanif.bluetoothchat.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.extension.getReadableFileSize
import com.glodanif.bluetoothchat.ui.adapter.ChatAdapter
import com.glodanif.bluetoothchat.ui.presenter.ChatPresenter
import com.glodanif.bluetoothchat.ui.util.SimpleTextWatcher
import com.glodanif.bluetoothchat.ui.view.ChatView
import com.glodanif.bluetoothchat.ui.view.NotificationView
import com.glodanif.bluetoothchat.ui.widget.ActionView
import com.squareup.picasso.Picasso
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.lang.Exception
import java.util.*

class ChatActivity : SkeletonActivity(), ChatView {

    private lateinit var presenter: ChatPresenter
    private val connectionModel: BluetoothConnector = BluetoothConnectorImpl(this)
    private val scanModel: BluetoothScanner = BluetoothScannerImpl(this)
    private val storageModel: MessagesStorage = MessagesStorageImpl(this)
    private val conversationModel: ConversationsStorage = ConversationsStorageImpl(this)

    private val layoutManager = LinearLayoutManager(this)
    private lateinit var actions: ActionView
    private lateinit var chatList: RecyclerView
    private lateinit var messageField: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var imagePickerButton: ImageButton
    private lateinit var transferringImagePreview: ImageView
    private lateinit var transferringImageSize: TextView
    private lateinit var transferringImageHeader: TextView
    private lateinit var transferringImageProgressLabel: TextView
    private lateinit var transferringImageProgressBar: ProgressBar

    private lateinit var textSendingHolder: ViewGroup
    private lateinit var imageSendingHolder: ViewGroup

    private lateinit var adapter: ChatAdapter

    private var isStarted = false

    private val textWatcher = object : SimpleTextWatcher() {

        private var previousText: String? = null

        override fun afterTextChanged(text: String) {

            if (previousText.isNullOrEmpty() && text.isNotEmpty()) {

                val animSetXY = AnimatorSet()
                animSetXY.playTogether(
                        ObjectAnimator.ofFloat(imagePickerButton, "scaleX", 1f, 0f),
                        ObjectAnimator.ofFloat(imagePickerButton, "scaleY", 1f, 0f)
                )
                animSetXY.setDuration(250).start()

                animSetXY.playTogether(
                        ObjectAnimator.ofFloat(sendButton, "scaleX", 0f, 1f),
                        ObjectAnimator.ofFloat(sendButton, "scaleY", 0f, 1f)
                )
                animSetXY.setDuration(250).start()

            } else if (!previousText.isNullOrEmpty() && text.isEmpty()) {

                val animSetXY = AnimatorSet()
                animSetXY.playTogether(
                        ObjectAnimator.ofFloat(imagePickerButton, "scaleX", 0f, 1f),
                        ObjectAnimator.ofFloat(imagePickerButton, "scaleY", 0f, 1f)
                )
                animSetXY.setDuration(250).start()

                animSetXY.playTogether(
                        ObjectAnimator.ofFloat(sendButton, "scaleX", 1f, 0f),
                        ObjectAnimator.ofFloat(sendButton, "scaleY", 1f, 0f)
                )
                animSetXY.setDuration(250).start()
            }

            previousText = text
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat, ActivityType.CHILD_ACTIVITY)

        toolbar?.setTitleTextAppearance(this, R.style.ActionBar_TitleTextStyle)
        toolbar?.setSubtitleTextAppearance(this, R.style.ActionBar_SubTitleTextStyle)

        textSendingHolder = findViewById(R.id.ll_text_sending_holder)
        imageSendingHolder = findViewById(R.id.ll_image_sending_holder)

        transferringImagePreview = findViewById(R.id.iv_transferring_image)
        transferringImageSize = findViewById(R.id.tv_file_size)
        transferringImageHeader = findViewById(R.id.tv_sending_image_label)
        transferringImageProgressLabel = findViewById(R.id.tv_file_sending_percentage)
        transferringImageProgressBar = findViewById(R.id.pb_transferring_progress)

        actions = findViewById(R.id.av_actions)
        messageField = findViewById(R.id.et_message)
        messageField.addTextChangedListener(textWatcher)

        sendButton = findViewById(R.id.ib_send)
        sendButton.setOnClickListener {
            presenter.sendMessage(messageField.text.toString().trim())
        }

        imagePickerButton = findViewById(R.id.ib_image)
        imagePickerButton.setOnClickListener {
            presenter.pickImage()
        }

        findViewById<ImageButton>(R.id.ib_cancel).setOnClickListener {
            presenter.cancelFileTransfer()
        }

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        adapter = ChatAdapter(this, displayMetrics)
        adapter.imageClickListener = { view, message ->
            ImagePreviewActivity.start(this, view, message)
        }

        chatList = findViewById(R.id.rv_chat)
        layoutManager.reverseLayout = true
        chatList.layoutManager = layoutManager
        chatList.adapter = adapter

        chatList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView?, scrollState: Int) {

                val picasso = Picasso.with(this@ChatActivity)
                if (scrollState == RecyclerView.SCROLL_STATE_IDLE || scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    picasso.resumeTag(adapter.picassoTag)
                } else {
                    picasso.pauseTag(adapter.picassoTag)
                }
            }
        })

        val deviceAddress: String? = intent.getStringExtra(EXTRA_ADDRESS)

        title = if (deviceAddress.isNullOrEmpty()) getString(R.string.app_name) else deviceAddress
        toolbar?.subtitle = getString(R.string.chat__not_connected)
        presenter = ChatPresenter(deviceAddress.toString(),
                this, scanModel, connectionModel, conversationModel, storageModel)

        if (intent.action == Intent.ACTION_SEND) {
            messageField.setText(intent.data.toString().trim())
        }
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

    override fun pickImage() {
        EasyImage.openChooserWithGallery(this, "chooserTitle", 0)
    }

    override fun showImageTooBig(maxSize: Long) {

        if (!isStarted) return

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat__too_big_image, maxSize.getReadableFileSize()))
                .setPositiveButton(getString(R.string.general__ok), null)
                .show()
    }

    override fun showImageTransferLayout(fileAddress: String?, fileSize: Long, transferType: ChatView.FileTransferType) {

        textSendingHolder.visibility = View.GONE
        imageSendingHolder.visibility = View.VISIBLE

        transferringImageHeader.text = if (transferType == ChatView.FileTransferType.SENDING)
            "Sending image" else "Receiving image"

        if (fileAddress != null) {
            Picasso.with(this)
                    .load("file://$fileAddress")
                    .into(transferringImagePreview)
        } else {
            val imagePlaceholder = resources.getDrawable(R.drawable.ic_photo_black_24dp)
            transferringImagePreview.setImageDrawable(imagePlaceholder)
        }
        transferringImageSize.text = fileSize.getReadableFileSize()
        transferringImageProgressLabel.text = "0%"
        //FIXME should work with Long
        transferringImageProgressBar.progress = 0
        transferringImageProgressBar.max = fileSize.toInt()
    }

    override fun updateImageTransferProgress(transferredBytes: Long, totalBytes: Long) {
        val percents = transferredBytes.toFloat() / totalBytes * 100
        transferringImageProgressLabel.text = "${Math.round(percents)}%"
        //FIXME should work with Long
        transferringImageProgressBar.progress = transferredBytes.toInt()

    }

    override fun hideImageTransferLayout() {
        textSendingHolder.visibility = View.VISIBLE
        imageSendingHolder.visibility = View.GONE
    }

    override fun showImageTransferCanceled() {
        Toast.makeText(this, "Your partner has canceled image transfer", Toast.LENGTH_LONG).show()
    }

    override fun showImageTransferFailure() {
        Toast.makeText(this, "Problem during image transfer", Toast.LENGTH_LONG).show()
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
        } else {

            EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {

                override fun onImagesPicked(imageFiles: MutableList<File>, source: EasyImage.ImageSource?, type: Int) {
                    if (imageFiles.isNotEmpty()) {
                        presenter.sendFile(imageFiles[0])
                    }
                }

                override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                    Toast.makeText(this@ChatActivity, "${e?.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_disconnect -> {
                presenter.disconnect()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private const val REQUEST_ENABLE_BLUETOOTH = 101

        const val EXTRA_ADDRESS = "extra.address"

        fun start(context: Context, address: String) {
            val intent: Intent = Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_ADDRESS, address)
            context.startActivity(intent)
        }

        fun start(context: Context, address: String, message: String) {
            val intent: Intent = Intent(context, ChatActivity::class.java)
                    .setAction(Intent.ACTION_SEND)
                    .setData(Uri.parse(message))
                    .putExtra(EXTRA_ADDRESS, address)
            context.startActivity(intent)
        }
    }
}

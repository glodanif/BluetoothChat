package com.glodanif.bluetoothchat.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.github.chrisbanes.photoview.PhotoView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.ui.presenter.ImagePreviewPresenter
import com.glodanif.bluetoothchat.ui.view.ImagePreviewView
import com.glodanif.bluetoothchat.ui.viewmodel.ChatMessageViewModel
import com.glodanif.bluetoothchat.utils.bind
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import javax.inject.Inject

class ImagePreviewActivity : SkeletonActivity(), ImagePreviewView {

    private val imageView: PhotoView by bind(R.id.pv_preview)

    @Inject
    lateinit var presenter: ImagePreviewPresenter

    private var own = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview, ActivityType.CHILD_ACTIVITY)

        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1)
        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        own = intent.getBooleanExtra(EXTRA_OWN, false)
        ComponentsManager.injectImagePreview(this, messageId, File(imagePath))

        toolbar?.setTitleTextAppearance(this, R.style.ActionBar_TitleTextStyle)
        toolbar?.setSubtitleTextAppearance(this, R.style.ActionBar_SubTitleTextStyle)

        imageView.minimumScale = .75f
        imageView.maximumScale = 2f

        presenter.loadImage()
    }

    override fun displayImage(fileUrl: String) {

        val callback = object : Callback {

            override fun onSuccess() {
                ActivityCompat.startPostponedEnterTransition(this@ImagePreviewActivity)
            }

            override fun onError() {
                ActivityCompat.startPostponedEnterTransition(this@ImagePreviewActivity)
            }
        }

        ActivityCompat.postponeEnterTransition(this)
        Picasso.with(this)
                .load(fileUrl)
                .config(Bitmap.Config.RGB_565)
                .noFade()
                .into(imageView, callback)
    }

    override fun showFileInfo(name: String, readableSize: String) {
        title = name
        toolbar?.subtitle = readableSize
    }

    override fun close() {
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!own) {
            menuInflater.inflate(R.menu.menu_image_preview, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_remove -> {
                confirmFileRemoval()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmFileRemoval() {

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.images__removal_confirmation))
                .setPositiveButton(getString(R.string.general__yes), { _, _ ->
                    presenter.removeFile()
                })
                .setNegativeButton(getString(R.string.general__no), null)
                .show()
    }

    companion object {

        const val EXTRA_MESSAGE_ID = "extra.message_id"
        const val EXTRA_IMAGE_PATH = "extra.image_path"
        const val EXTRA_OWN = "extra.own"

        fun start(activity: Activity, transitionView: ImageView, message: ChatMessage) {
            start(activity, transitionView, message.uid, message.filePath ?: "unknown", message.own)
        }

        fun start(activity: Activity, transitionView: ImageView, message: ChatMessageViewModel) {
            start(activity, transitionView, message.uid, message.imagePath
                    ?: "unknown", message.own)
        }

        fun start(activity: Activity, transitionView: ImageView, messageId: Long, imagePath: String, ownMessage: Boolean) {

            val intent = Intent(activity, ImagePreviewActivity::class.java)
                    .putExtra(EXTRA_MESSAGE_ID, messageId)
                    .putExtra(EXTRA_IMAGE_PATH, imagePath)
                    .putExtra(EXTRA_OWN, ownMessage)

            val options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity, transitionView, activity.getString(R.string.id_transition_image))
            activity.startActivity(intent, options.toBundle())
        }
    }
}

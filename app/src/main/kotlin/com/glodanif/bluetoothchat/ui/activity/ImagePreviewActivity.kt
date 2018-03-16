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
import com.glodanif.bluetoothchat.ui.presenter.ImagePreviewPresenter
import com.glodanif.bluetoothchat.ui.view.ImagePreviewView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ImagePreviewActivity : SkeletonActivity(), ImagePreviewView {

    private lateinit var imageView: PhotoView

    private lateinit var message: ChatMessage

    private lateinit var presenter: ImagePreviewPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview, ActivityType.CHILD_ACTIVITY)

        toolbar?.setTitleTextAppearance(this, R.style.ActionBar_TitleTextStyle)
        toolbar?.setSubtitleTextAppearance(this, R.style.ActionBar_SubTitleTextStyle)

        message = intent.getSerializableExtra(EXTRA_MESSAGE) as ChatMessage

        imageView = findViewById(R.id.pv_preview)
        imageView.minimumScale = .75f
        imageView.maximumScale = 2f

        presenter = ImagePreviewPresenter(message, this)
        presenter.loadData()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!message.own) {
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
                    finish()
                })
                .setNegativeButton(getString(R.string.general__no), null)
                .show()
    }

    companion object {

        val EXTRA_MESSAGE = "extra.message"

        fun start(activity: Activity, transitionView: ImageView, message: ChatMessage) {

            val intent = Intent(activity, ImagePreviewActivity::class.java)
                    .putExtra(EXTRA_MESSAGE, message)

            val options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity, transitionView, activity.getString(R.string.id_transition_image))
            activity.startActivity(intent, options.toBundle())
        }
    }
}

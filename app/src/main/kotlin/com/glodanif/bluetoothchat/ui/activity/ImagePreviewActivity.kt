package com.glodanif.bluetoothchat.ui.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.github.chrisbanes.photoview.PhotoView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.extension.getReadableFileSize
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File

class ImagePreviewActivity : SkeletonActivity() {

    private lateinit var message: ChatMessage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview, ActivityType.CHILD_ACTIVITY)

        toolbar?.setTitleTextAppearance(this, R.style.ActionBar_TitleTextStyle)
        toolbar?.setSubtitleTextAppearance(this, R.style.ActionBar_SubTitleTextStyle)

        message = intent.getSerializableExtra(EXTRA_MESSAGE) as ChatMessage

        val file = File(message.filePath)
        title = file.name
        toolbar?.subtitle = file.length().getReadableFileSize()

        val imageView = findViewById<PhotoView>(R.id.pv_preview)
        imageView.minimumScale = .75f
        imageView.maximumScale = 2f

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
                .load("file://${file.absolutePath}")
                .config(Bitmap.Config.RGB_565)
                .noFade()
                .into(imageView, callback)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!message.own) {
            menuInflater.inflate(R.menu.menu_image_preview, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.action_save -> {
                checkWriteStoragePermission()
                true
            }
            R.id.action_remove -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkWriteStoragePermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            saveImage()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                explainAskingStoragePermission()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage()
            } else {
                explainAskingStoragePermission()
            }
        }
    }

    private fun saveImage() {

    }

    private fun explainAskingStoragePermission() {

        AlertDialog.Builder(this)
                .setMessage(getString(R.string.images__permission_explanation_storage))
                .setPositiveButton(getString(R.string.general__ok), { _, _ ->
                    ActivityCompat.requestPermissions(this@ImagePreviewActivity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            REQUEST_STORAGE_PERMISSION
                    )
                })
                .show()
    }

    companion object {

        val EXTRA_MESSAGE = "extra.message"
        private val REQUEST_STORAGE_PERMISSION = 101

        fun start(activity: Activity, transitionView: ImageView, message: ChatMessage) {

            val intent = Intent(activity, ImagePreviewActivity::class.java)
                    .putExtra(EXTRA_MESSAGE, message)

            val options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity, transitionView, "chatImage")
            activity.startActivity(intent, options.toBundle())
        }
    }
}

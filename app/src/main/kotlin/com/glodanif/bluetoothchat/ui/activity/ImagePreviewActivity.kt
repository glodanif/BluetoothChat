package com.glodanif.bluetoothchat.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import android.util.ArrayMap
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import com.github.chrisbanes.photoview.PhotoView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.MessageFile
import com.glodanif.bluetoothchat.ui.presenter.ImagePreviewPresenter
import com.glodanif.bluetoothchat.ui.view.ImagePreviewView
import com.glodanif.bluetoothchat.ui.viewmodel.ChatMessageViewModel
import com.glodanif.bluetoothchat.utils.argument
import com.glodanif.bluetoothchat.utils.bind
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.io.File
import java.lang.Exception
import java.lang.ref.WeakReference

class ImagePreviewActivity : SkeletonActivity(), ImagePreviewView {

    private val messageId: Long by argument(EXTRA_MESSAGE_ID, -1L)
    private val imagePath: String? by argument(EXTRA_IMAGE_PATH)
    private val own: Boolean by argument(EXTRA_OWN, false)

    private val presenter: ImagePreviewPresenter by inject {
        parametersOf(messageId, File(imagePath), this)
    }

    private val imageView: PhotoView by bind(R.id.pv_preview)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview, ActivityType.CHILD_ACTIVITY)
        supportPostponeEnterTransition()

        ViewCompat.setTransitionName(imageView, messageId.toString())

        toolbar?.setTitleTextAppearance(this, R.style.ActionBar_TitleTextStyle)
        toolbar?.setSubtitleTextAppearance(this, R.style.ActionBar_SubTitleTextStyle)

        imageView.minimumScale = .75f
        imageView.maximumScale = 2f

        presenter.loadImage()
    }

    override fun displayImage(fileUrl: String) {

        val callback = object : Callback {

            override fun onSuccess() {
                supportStartPostponedEnterTransition()
            }

            override fun onError(e: Exception?) {
                supportStartPostponedEnterTransition()
            }
        }

        Picasso.get()
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
                .setPositiveButton(getString(R.string.general__yes)) { _, _ ->
                    presenter.removeFile()
                }
                .setNegativeButton(getString(R.string.general__no), null)
                .show()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }

        //Fixes https://issuetracker.google.com/issues/37042900
        val transitionManagerClass = TransitionManager::class.java
        try {
            val runningTransitionsField = transitionManagerClass.getDeclaredField("sRunningTransitions")
            runningTransitionsField.isAccessible = true

            val runningTransitions = runningTransitionsField.get(transitionManagerClass)
                    as ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>>
            if (runningTransitions.get() == null || runningTransitions.get().get() == null) {
                return
            }
            val map = runningTransitions.get().get()
            val decorView = window.decorView
            if (map != null && map.containsKey(decorView)) {
                map.remove(decorView)
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    companion object {

        const val EXTRA_MESSAGE_ID = "extra.message_id"
        const val EXTRA_IMAGE_PATH = "extra.image_path"
        const val EXTRA_OWN = "extra.own"

        fun start(activity: Activity, transitionView: ImageView, message: MessageFile) {
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
                    .makeSceneTransitionAnimation(activity, transitionView, messageId.toString())
            activity.startActivity(intent, options.toBundle())
        }
    }
}

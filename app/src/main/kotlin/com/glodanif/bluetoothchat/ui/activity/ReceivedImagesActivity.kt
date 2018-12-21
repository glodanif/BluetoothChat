package com.glodanif.bluetoothchat.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.MessageFile
import com.glodanif.bluetoothchat.ui.adapter.ImagesAdapter
import com.glodanif.bluetoothchat.ui.presenter.ReceivedImagesPresenter
import com.glodanif.bluetoothchat.ui.router.ReceivedImagesRouter
import com.glodanif.bluetoothchat.ui.view.ReceivedImagesView
import com.glodanif.bluetoothchat.utils.argument
import com.glodanif.bluetoothchat.utils.bind
import com.glodanif.bluetoothchat.utils.nullableArgument
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class ReceivedImagesActivity : SkeletonActivity(), ReceivedImagesView, ReceivedImagesRouter {

    private val address: String? by nullableArgument(EXTRA_ADDRESS)

    private val presenter: ReceivedImagesPresenter by inject {
        parametersOf(address ?: "", this)
    }

    private val imagesGrid: RecyclerView by bind(R.id.rv_images)
    private val noImagesLabel: TextView by bind(R.id.tv_no_images)

    private var imagesAdapter = ImagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_received_images, ActivityType.CHILD_ACTIVITY)
        lifecycle.addObserver(presenter)

        imagesGrid.layoutManager = GridLayoutManager(this, calculateNoOfColumns())
        imagesGrid.adapter = imagesAdapter

        imagesAdapter.clickListener = { view, message ->
            presenter.onImageClick(view, message)
        }
    }

    override fun displayImages(imageMessages: List<MessageFile>) {
        imagesAdapter.images = ArrayList(imageMessages)
        imagesAdapter.notifyDataSetChanged()
    }

    override fun showNoImages() {
        imagesGrid.visibility = View.GONE
        noImagesLabel.visibility = View.VISIBLE
    }

    override fun openImage(view: ImageView, message: MessageFile) {
        ImagePreviewActivity.start(this, view, message)
    }

    private fun calculateNoOfColumns(): Int {
        val displayMetrics = resources.displayMetrics
        val no = displayMetrics.widthPixels / resources.getDimensionPixelSize(R.dimen.thumbnail_width)
        return if (no == 0) 1 else no
    }

    companion object {

        private const val EXTRA_ADDRESS = "extra.address"

        fun start(context: Context, address: String?) {
            val intent = Intent(context, ReceivedImagesActivity::class.java)
                    .putExtra(EXTRA_ADDRESS, address ?: "")
            context.startActivity(intent)
        }
    }
}

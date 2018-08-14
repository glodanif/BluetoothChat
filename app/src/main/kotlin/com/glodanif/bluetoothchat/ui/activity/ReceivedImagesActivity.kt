package com.glodanif.bluetoothchat.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.MessageFile
import com.glodanif.bluetoothchat.di.Params
import com.glodanif.bluetoothchat.di.Params.ADDRESS
import com.glodanif.bluetoothchat.ui.adapter.ImagesAdapter
import com.glodanif.bluetoothchat.ui.presenter.ReceivedImagesPresenter
import com.glodanif.bluetoothchat.ui.view.ReceivedImagesView
import com.glodanif.bluetoothchat.utils.bind
import org.koin.android.ext.android.inject

class ReceivedImagesActivity : SkeletonActivity(), ReceivedImagesView {

    private var address: String? = null
    private val presenter: ReceivedImagesPresenter by inject {
        mapOf(ADDRESS to (address ?: ""), Params.RECEIVED_IMAGES_VIEW to this)
    }

    private val imagesGrid: RecyclerView by bind(R.id.rv_images)
    private val noImagesLabel: TextView by bind(R.id.tv_no_images)

    private var imagesAdapter = ImagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_received_images, ActivityType.CHILD_ACTIVITY)

        address = intent.getStringExtra(EXTRA_ADDRESS)

        imagesGrid.layoutManager = GridLayoutManager(this, calculateNoOfColumns())
        imagesGrid.adapter = imagesAdapter

        imagesAdapter.clickListener = { view, message ->
            ImagePreviewActivity.start(this, view, message)
        }

        presenter.loadImages()
    }

    override fun displayImages(imageMessages: List<MessageFile>) {
        imagesAdapter.images = ArrayList(imageMessages)
        imagesAdapter.notifyDataSetChanged()
    }

    override fun showNoImages() {
        imagesGrid.visibility = View.GONE
        noImagesLabel.visibility = View.VISIBLE
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

package com.glodanif.bluetoothchat.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorageImpl
import com.glodanif.bluetoothchat.ui.adapter.ImagesAdapter
import com.glodanif.bluetoothchat.ui.presenter.ReceivedImagesPresenter
import com.glodanif.bluetoothchat.ui.view.ReceivedImagesView

class ReceivedImagesActivity : SkeletonActivity(), ReceivedImagesView {

    private lateinit var presenter: ReceivedImagesPresenter
    private val storageModel: MessagesStorage = MessagesStorageImpl(this)

    private var address: String? = null

    private lateinit var imagesGrid: RecyclerView
    private lateinit var noImagesLabel: TextView

    private var adapter = ImagesAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_received_images, ActivityType.CHILD_ACTIVITY)

        address = intent.getStringExtra(EXTRA_ADDRESS)

        imagesGrid = findViewById(R.id.rv_images)
        noImagesLabel = findViewById(R.id.tv_no_images)

        imagesGrid.layoutManager = GridLayoutManager(this, calculateNoOfColumns())
        imagesGrid.adapter = adapter

        adapter.clickListener = { view, message ->
            ImagePreviewActivity.start(this, view, message)
        }

        presenter = ReceivedImagesPresenter(address, this, storageModel)
    }

    override fun onStart() {
        super.onStart()
        presenter.loadImages()
    }

    override fun displayImages(imageMessages: List<ChatMessage>) {
        adapter.images = ArrayList(imageMessages)
        adapter.notifyDataSetChanged()
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
                    .putExtra(EXTRA_ADDRESS, address)
            context.startActivity(intent)
        }
    }
}

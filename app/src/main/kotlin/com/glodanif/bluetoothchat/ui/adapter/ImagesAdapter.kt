package com.glodanif.bluetoothchat.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.squareup.picasso.Picasso

class ImagesAdapter(private val context: Context) : RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    var clickListener: ((ImageView, ChatMessage) -> Unit)? = null
    var images: ArrayList<ChatMessage> = ArrayList()

    override fun onBindViewHolder(holder: ImageViewHolder?, position: Int) {

        if (holder == null) return

        val image = images[position]

        holder.itemView.setOnClickListener { clickListener?.invoke(holder.thumbnail, image) }
        Picasso.with(context)
                .load("file://${image.filePath}")
                .config(Bitmap.Config.RGB_565)
                .error(R.color.background_image)
                .placeholder(R.color.background_image)
                .into(holder.thumbnail)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_image_grid, parent, false)
        return ImageViewHolder(view)
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
    }
}

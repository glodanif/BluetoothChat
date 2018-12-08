package com.glodanif.bluetoothchat.ui.adapter

import android.graphics.Bitmap
import android.text.SpannableString
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.service.message.PayloadType
import com.glodanif.bluetoothchat.ui.util.ClickableMovementMethod
import com.glodanif.bluetoothchat.ui.viewmodel.ChatMessageViewModel
import com.squareup.picasso.Picasso
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import java.util.*

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    companion object {
        private const val OWN_TEXT_MESSAGE = 0
        private const val OWN_IMAGE_MESSAGE = 1
        private const val FOREIGN_TEXT_MESSAGE = 2
        private const val FOREIGN_IMAGE_MESSAGE = 3
    }

    val picassoTag = Object()

    var messages = LinkedList<ChatMessageViewModel>()

    var imageClickListener: ((view: ImageView, message: ChatMessageViewModel) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val message = messages[position]

        if (holder is ImageMessageViewHolder) {

            if (!message.isImageAvailable) {

                holder.image.visibility = View.GONE
                holder.missingLabel.visibility = View.VISIBLE
                holder.missingLabel.setText(message.imageProblemText)

            } else {

                holder.image.visibility = View.VISIBLE
                holder.missingLabel.visibility = View.GONE

                ViewCompat.setTransitionName(holder.image, message.uid.toString())

                val size = message.imageSize
                holder.image.layoutParams = FrameLayout.LayoutParams(size.width, size.height)
                holder.image.setOnClickListener {
                    imageClickListener?.invoke(holder.image, message)
                }

                Picasso.get()
                        .load(message.imageUri)
                        .config(Bitmap.Config.RGB_565)
                        .error(R.color.background_image)
                        .placeholder(R.color.background_image)
                        .tag(picassoTag)
                        .resize(size.width, size.height)
                        .into(holder.image)
            }

            holder.date.text = message.time

        } else if (holder is TextMessageViewHolder) {

            val spannableMessage = SpannableString(message.text)
            LinkifyCompat.addLinks(spannableMessage,
                    Linkify.WEB_URLS or Linkify.PHONE_NUMBERS or Linkify.EMAIL_ADDRESSES)

            holder.text.movementMethod = ClickableMovementMethod
            holder.text.setText(spannableMessage, TextView.BufferType.SPANNABLE)
            holder.date.text = message.time
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (messages[position].own) {
            when (message.type) {
                PayloadType.IMAGE -> OWN_IMAGE_MESSAGE
                else -> OWN_TEXT_MESSAGE
            }
        } else {
            when (message.type) {
                PayloadType.IMAGE -> FOREIGN_IMAGE_MESSAGE
                else -> FOREIGN_TEXT_MESSAGE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val layoutId = when (viewType) {
            OWN_TEXT_MESSAGE -> R.layout.item_message_text_own
            OWN_IMAGE_MESSAGE -> R.layout.item_message_image_own
            FOREIGN_TEXT_MESSAGE -> R.layout.item_message_text_foreign
            FOREIGN_IMAGE_MESSAGE -> R.layout.item_message_image_foreign
            else -> 0
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        return when (viewType) {
            OWN_IMAGE_MESSAGE, FOREIGN_IMAGE_MESSAGE -> ImageMessageViewHolder(view)
            else -> TextMessageViewHolder(view)
        }
    }

    override fun getHeaderId(position: Int) = messages[position].dayOfYearRaw

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_divider, parent, false)
        return DateDividerViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DateDividerViewHolder).date.text = messages[position].dayOfYear
    }

    class DateDividerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tv_date)
    }

    class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val text: TextView = itemView.findViewById(R.id.tv_text)
    }

    class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val image: ImageView = itemView.findViewById(R.id.iv_image)
        val missingLabel: TextView = itemView.findViewById(R.id.tv_missing_file)
    }
}

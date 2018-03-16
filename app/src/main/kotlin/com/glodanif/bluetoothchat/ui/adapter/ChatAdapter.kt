package com.glodanif.bluetoothchat.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.MessageType
import com.glodanif.bluetoothchat.extension.getRelativeTime
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

class ChatAdapter(private val context: Context, private val displayMetrics: DisplayMetrics) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val OWN_TEXT_MESSAGE = 0
    private val OWN_IMAGE_MESSAGE = 1
    private val FOREIGN_TEXT_MESSAGE = 2
    private val FOREIGN_IMAGE_MESSAGE = 3

    val picassoTag = Object()

    var messages = LinkedList<ChatMessage>()

    var imageClickListener: ((view: ImageView, message: ChatMessage) -> Unit)? = null

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        val message = messages[position]

        if (viewHolder is ImageMessageViewHolder) {

            val holder: ImageMessageViewHolder? = viewHolder
            val info = message.fileInfo

            if (message.filePath == null) {

                holder?.image?.visibility = View.GONE
                holder?.missingLabel?.visibility = View.VISIBLE
                holder?.missingLabel?.setText(R.string.chat__removed_image)

            } else if (!File(message.filePath).exists()) {

                holder?.image?.visibility = View.GONE
                holder?.missingLabel?.visibility = View.VISIBLE
                holder?.missingLabel?.setText(R.string.chat__missing_image)

            } else {

                holder?.image?.visibility = View.VISIBLE
                holder?.missingLabel?.visibility = View.GONE

                if (info != null && info.contains("x")) {

                    val size = info.split("x")
                    val path = message.filePath

                    if (size.size == 2) {

                        val viewSize = getScaledSize(size[0].toInt(), size[1].toInt())

                        if (viewSize.first > 0 && viewSize.second > 0 && path != null) {

                            holder?.image?.layoutParams =
                                    FrameLayout.LayoutParams(viewSize.first, viewSize.second)

                            holder?.image?.setOnClickListener {
                                imageClickListener?.invoke(holder.image, message)
                            }

                            Picasso.with(context)
                                    .load("file://$path")
                                    .config(Bitmap.Config.RGB_565)
                                    .error(R.color.background_image)
                                    .placeholder(R.color.background_image)
                                    .tag(picassoTag)
                                    .resize(viewSize.first, viewSize.second)
                                    .into(holder?.image)
                        }
                    }
                }
            }

            holder?.date?.text = message.date.getRelativeTime(context)
        } else if (viewHolder is TextMessageViewHolder) {
            val holder: TextMessageViewHolder? = viewHolder
            holder?.text?.text = message.text
            holder?.date?.text = message.date.getRelativeTime(context)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (messages[position].own) {
            when (message.messageType) {
                MessageType.IMAGE -> OWN_IMAGE_MESSAGE
                else -> OWN_TEXT_MESSAGE
            }
        } else {
            when (message.messageType) {
                MessageType.IMAGE -> FOREIGN_IMAGE_MESSAGE
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

    class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val text: TextView = itemView.findViewById(R.id.tv_text)
    }

    class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val image: ImageView = itemView.findViewById(R.id.iv_image)
        val missingLabel: TextView = itemView.findViewById(R.id.tv_missing_file)
    }

    private fun getScaledSize(imageWidth: Int, imageHeight: Int): Pair<Int, Int> {

        val maxWidth = (displayMetrics.widthPixels * .75).toInt()
        val maxHeight = (displayMetrics.heightPixels * .5).toInt()

        var viewWidth = imageWidth
        var viewHeight = imageHeight

        if (imageWidth > maxWidth || imageHeight > maxHeight) {

            if (imageWidth == imageHeight) {

                if (imageHeight > maxHeight) {
                    viewWidth = maxHeight
                    viewHeight = maxHeight
                }

                if (viewWidth > maxWidth) {
                    viewWidth = maxWidth
                    viewHeight = maxWidth
                }

            } else if (imageWidth > maxWidth) {

                viewWidth = maxWidth
                viewHeight = (maxWidth.toFloat() / imageWidth * imageHeight).toInt()

                if (viewHeight > maxHeight) {
                    viewHeight = maxHeight
                    viewWidth = (maxHeight.toFloat() / imageHeight * imageWidth).toInt()
                }

            } else if (imageHeight > maxHeight) {

                viewHeight = maxHeight
                viewWidth = (maxHeight.toFloat() / imageHeight * imageWidth).toInt()

                if (viewWidth > maxWidth) {
                    viewWidth = maxWidth
                    viewHeight = (maxWidth.toFloat() / imageWidth * imageHeight).toInt()
                }

            }
        }

        return Pair(viewWidth, viewHeight)
    }
}

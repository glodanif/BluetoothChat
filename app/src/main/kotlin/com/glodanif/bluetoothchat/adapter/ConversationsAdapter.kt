package com.glodanif.bluetoothchat.adapter

import android.bluetooth.BluetoothDevice
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.entity.Conversation
import com.glodanif.bluetoothchat.util.CircleTransformation
import com.glodanif.bluetoothchat.util.GrayscaleTransformation
import com.squareup.picasso.Picasso

class ConversationsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM = 0
    private val TYPE_HEADER = 1

    var listener: ((Conversation) -> Unit)? = null

    var current: Conversation? = null
    var conversations = ArrayList<Conversation>()
        set(value) {
            current = value.findLast { it.deviceAddress == currentlyConnected?.address }
            value.remove(current)
            field = value
        }

    var currentlyConnected: BluetoothDevice? = null

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, position: Int) {

        if (viewHolder is HeaderViewHolder) {

            val holder: HeaderViewHolder = viewHolder

            if (position == 0) {
                holder.header.text = "Connected"
            } else {
                holder.header.text = "History"
            }

        } else if (viewHolder is ConversationViewHolder) {

            val holder: ConversationViewHolder = viewHolder

            if (currentlyConnected == null) {
                val conversation = conversations[position - 1]
                holder.name.text = conversation.deviceName
                holder.itemView.setOnClickListener { listener?.invoke(conversation) }
                Picasso.with(holder.itemView.context).load(R.drawable.empty_avatar)
                        .transform(CircleTransformation())
                        .transform(GrayscaleTransformation()).into(holder.avatar)
            } else {
                val conversation = conversations[position - 3]
                holder.name.text = conversation.deviceName
                holder.itemView.setOnClickListener { listener?.invoke(conversation) }
                Picasso.with(holder.itemView.context).load(R.drawable.empty_avatar)
                        .transform(CircleTransformation())
                        .into(holder.avatar)
            }
        }
    }

    override fun getItemCount(): Int {
        return conversations.size + (if (currentlyConnected == null) 1 else 2)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == TYPE_ITEM) {
            val view = LayoutInflater.from(parent?.context)
                    .inflate(R.layout.item_conversation, parent, false)
            return ConversationViewHolder(view)
        }

        val view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_header_conversations, parent, false)
        return HeaderViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {

        if (currentlyConnected == null) {
            return if (position == 0) TYPE_HEADER else TYPE_ITEM
        } else {
            return if (position == 0 || position == 2) TYPE_HEADER else TYPE_ITEM
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var header: TextView = itemView.findViewById(R.id.tv_header) as TextView
    }

    class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatar: ImageView = itemView.findViewById(R.id.iv_avatar) as ImageView
        var name: TextView = itemView.findViewById(R.id.tv_name) as TextView
        var lastMessage: TextView = itemView.findViewById(R.id.tv_last_message) as TextView
        var time: TextView = itemView.findViewById(R.id.tv_time) as TextView
    }
}
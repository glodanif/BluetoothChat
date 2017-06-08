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

class ConversationsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM = 0
    private val TYPE_HEADER = 1

    var listener: ((Conversation) -> Unit)? = null

    var conversations = ArrayList<Conversation>()

    var currentlyConnected: BluetoothDevice? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        return conversations.size + (if (currentlyConnected == null) 0 else 2)
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
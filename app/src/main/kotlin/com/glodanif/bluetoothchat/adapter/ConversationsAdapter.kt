package com.glodanif.bluetoothchat.adapter

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

class ConversationsAdapter : RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder>() {

    var listener: ((Conversation) -> Unit)? = null

    private var isConnected: Boolean = false
    private var conversations: ArrayList<Conversation> = ArrayList()

    override fun onBindViewHolder(holder: ConversationViewHolder?, position: Int) {

        val conversation = conversations[position]
        holder?.name?.text = conversation.deviceName
        holder?.itemView?.setOnClickListener { listener?.invoke(conversation) }
        holder?.connected?.visibility = View.VISIBLE

        val creator = Picasso.with(holder?.itemView?.context).load(R.drawable.empty_avatar)
                .transform(CircleTransformation())

        if (!isConnected || position > 0) {
            holder?.connected?.visibility = View.GONE
            creator.transform(GrayscaleTransformation())
        }
        creator.into(holder?.avatar)
    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    fun setData(items: ArrayList<Conversation>, connected: String?) {
        val current: Conversation? = items.findLast { it.deviceAddress == connected }
        isConnected = current != null
        if (current == null) {
            conversations = items
        } else {
            items.remove(current)
            conversations = ArrayList()
            conversations.add(current)
            conversations.addAll(items)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.iv_avatar) as ImageView
        val name: TextView = itemView.findViewById(R.id.tv_name) as TextView
        val connected: View = itemView.findViewById(R.id.iv_connected)
        val lastMessage: TextView = itemView.findViewById(R.id.tv_last_message) as TextView
        val time: TextView = itemView.findViewById(R.id.tv_time) as TextView
    }
}

package com.glodanif.bluetoothchat.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.ui.viewmodel.ConversationViewModel

class ConversationsAdapter : RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder>() {

    var clickListener: ((ConversationViewModel) -> Unit)? = null
    var longClickListener: ((ConversationViewModel, Boolean) -> Unit)? = null

    private var isConnected: Boolean = false
    private var conversations: ArrayList<ConversationViewModel> = ArrayList()

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {

        val conversation = conversations[position]

        holder.name.text = conversation.fullName
        holder.itemView.setOnClickListener {
            clickListener?.invoke(conversation)
        }
        holder.itemView.setOnLongClickListener {
            val isCurrent = isConnected && position == 0
            longClickListener?.invoke(conversation, isCurrent)
            return@setOnLongClickListener true
        }

        if (conversation.lastMessage != null) {
            holder.messageContainer.visibility = View.VISIBLE
            holder.lastMessage.text = conversation.lastMessage
        } else {
            holder.messageContainer.visibility = View.GONE
        }

        if (conversation.lastActivityText != null) {
            holder.time.visibility = View.VISIBLE
            holder.time.text = conversation.lastActivityText
        } else {
            holder.time.visibility = View.GONE
        }

        if (conversation.notSeen > 0) {
            holder.notSeen.visibility = View.VISIBLE
            holder.notSeen.text = conversation.notSeen.toString()
        } else {
            holder.notSeen.visibility = View.GONE
        }

        val isNotConnected = !isConnected || position > 0

        holder.connected.visibility = if (isNotConnected) View.GONE else View.VISIBLE
        val drawable = if (isNotConnected)
            conversation.getGrayedAvatar() else conversation.getColoredAvatar()
        holder.avatar.setImageDrawable(drawable)
    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    fun setData(items: ArrayList<ConversationViewModel>, connected: String?) {
        conversations = items
        setCurrentConversation(connected)
    }

    fun setCurrentConversation(connected: String?) {
        isConnected = connected != null
        val sortedList = conversations.sortedWith(
                compareByDescending<ConversationViewModel> { it.address == connected }
                        .thenByDescending { it.lastActivity })
        conversations = ArrayList()
        conversations.addAll(sortedList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val name: TextView = itemView.findViewById(R.id.tv_name)
        val connected: ImageView = itemView.findViewById(R.id.iv_connected)
        val lastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        val time: TextView = itemView.findViewById(R.id.tv_time)
        val notSeen: TextView = itemView.findViewById(R.id.tv_not_seen)
        val messageContainer: LinearLayout = itemView.findViewById(R.id.ll_message_info)
    }
}

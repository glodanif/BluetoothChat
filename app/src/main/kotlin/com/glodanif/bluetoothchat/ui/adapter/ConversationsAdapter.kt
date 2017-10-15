package com.glodanif.bluetoothchat.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.extension.getFirstLetter
import com.glodanif.bluetoothchat.extension.getRelativeTime

class ConversationsAdapter(private val context: Context) : RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder>() {

    var clickListener: ((Conversation) -> Unit)? = null
    var longClickListener: ((Conversation, Boolean) -> Unit)? = null

    private var isConnected: Boolean = false
    private var conversations: ArrayList<Conversation> = ArrayList()

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ConversationViewHolder?, position: Int) {

        if (holder == null) return

        val conversation = conversations[position]

        holder.name.text = "${conversation.displayName} (${conversation.deviceName})"
        holder.itemView?.setOnClickListener { clickListener?.invoke(conversation) }
        holder.itemView?.setOnLongClickListener {
            val isCurrent = isConnected && position == 0
            longClickListener?.invoke(conversation, isCurrent)
            return@setOnLongClickListener true
        }
        holder.connected.visibility = View.VISIBLE

        if (!conversation.lastMessage.isNullOrEmpty()) {
            holder.messageContainer.visibility = View.VISIBLE
            holder.time.visibility = View.VISIBLE
            holder.lastMessage.text = conversation.lastMessage
            holder.time.text = conversation.lastActivity?.getRelativeTime(context)
            if (conversation.notSeen > 0) {
                holder.notSeen.visibility = View.VISIBLE
                holder.notSeen.text = conversation.notSeen.toString()
            } else {
                holder.notSeen.visibility = View.GONE
            }
        } else {
            holder.messageContainer.visibility = View.GONE
            holder.time.visibility = View.GONE
        }

        var color = conversation.color
        if (!isConnected || position > 0) {
            holder.connected.visibility = View.GONE
            color = Color.LTGRAY
        }

        val drawable = TextDrawable.builder().buildRound(conversation.displayName.getFirstLetter(), color)
        holder.avatar.setImageDrawable(drawable)
    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    fun setData(items: ArrayList<Conversation>, connected: String?) {
        conversations = items
        setCurrentConversation(connected)
    }

    fun setCurrentConversation(connected: String?) {
        isConnected = connected != null
        val sortedList = conversations.sortedWith(
                compareByDescending<Conversation> { it.deviceAddress == connected }
                        .thenByDescending { it.lastActivity })
        conversations = ArrayList()
        conversations.addAll(sortedList)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent?.context)
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

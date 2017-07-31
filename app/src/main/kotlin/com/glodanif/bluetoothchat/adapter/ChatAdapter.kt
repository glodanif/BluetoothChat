package com.glodanif.bluetoothchat.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.entity.ChatMessage
import com.glodanif.bluetoothchat.extension.getRelativeTime
import java.util.*

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val OWN_MESSAGE = 0
    private val FOREIGN_MESSAGE = 1

    var messages = LinkedList<ChatMessage>()

    override fun onBindViewHolder(viewHolder: MessageViewHolder?, position: Int) {

        val holder: MessageViewHolder? = viewHolder
        val message = messages[position]

        holder?.date?.text = message.date.getRelativeTime()
        holder?.text?.text = message.text
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].own) OWN_MESSAGE else FOREIGN_MESSAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {

        val layoutId = if (viewType == OWN_MESSAGE)
            R.layout.item_chat_message_own else R.layout.item_chat_message_foreign
        val view = LayoutInflater.from(parent?.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById<TextView>(R.id.tv_date)
        val text: TextView = itemView.findViewById<TextView>(R.id.tv_text)
    }
}

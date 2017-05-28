package com.glodanif.bluetoothchat.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.entity.ChatMessage

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    var messages = ArrayList<ChatMessage>()

    override fun onBindViewHolder(viewHolder: MessageViewHolder?, position: Int) {

        val holder: MessageViewHolder? = viewHolder
        val message = messages[position]

        holder?.date?.text = message.date?.time.toString()
        holder?.text?.text = message.text
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    fun addNewMessageDevice(device: ChatMessage) {
        messages.add(device)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tv_date) as TextView
        val text: TextView = itemView.findViewById(R.id.tv_text) as TextView
    }
}

package com.glodanif.bluetoothchat.adapter

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.entity.ChatMessage

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    var messages = ArrayList<ChatMessage>()

    override fun onBindViewHolder(viewHolder: MessageViewHolder?, position: Int) {

        val holder: MessageViewHolder? = viewHolder
        val message = messages[position]

        val params: FrameLayout.LayoutParams? = holder?.container?.layoutParams as FrameLayout.LayoutParams
        params?.gravity = if (message.own) Gravity.END else Gravity.START
        holder.container.layoutParams = params
        holder.date.text = message.date?.time.toString()
        holder.text.text = message.text
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.cv_container)
        val date: TextView = itemView.findViewById(R.id.tv_date) as TextView
        val text: TextView = itemView.findViewById(R.id.tv_text) as TextView
    }
}

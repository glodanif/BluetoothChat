package com.glodanif.bluetoothchat.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.ui.viewmodel.ContactViewModel

class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    var clickListener: ((ContactViewModel) -> Unit)? = null
    var conversations: ArrayList<ContactViewModel> = ArrayList()

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {

        val contact = conversations[position]

        holder.name.text = contact.name
        holder.avatar.setImageDrawable(contact.avatar)
        holder.itemView.setOnClickListener { clickListener?.invoke(contact) }
    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val name: TextView = itemView.findViewById(R.id.tv_name)
    }
}
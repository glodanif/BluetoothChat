package com.glodanif.bluetoothchat.adapter

import android.bluetooth.BluetoothDevice
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.glodanif.bluetoothchat.R

class DevicesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ITEM = 0
    private val TYPE_HEADER = 1

    var listener: ((BluetoothDevice) -> Unit)? = null

    var pairedList = ArrayList<BluetoothDevice>()
    var availableList = ArrayList<BluetoothDevice>()

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder?, position: Int) {

        if (viewHolder is HeaderViewHolder) {

            val holder: HeaderViewHolder = viewHolder

            if (position == 0) {
                holder.header.text = "Paired devices"
                holder.emptyMessage.visibility =
                        if (pairedList.isEmpty()) View.VISIBLE else View.GONE
            } else {
                holder.header.text = "Available devices"
                holder.emptyMessage.visibility =
                        if (availableList.isEmpty()) View.VISIBLE else View.GONE
            }

        } else if (viewHolder is DeviceViewHolder) {

            val holder: DeviceViewHolder = viewHolder

            val device = if (position >= 1 && position < pairedList.size + 1)
                pairedList[position - 1] else availableList[position - pairedList.size - 2]
            holder.name.text = device.name
            holder.macAddress.text = device.address
            holder.itemView.setOnClickListener { listener?.invoke(device) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (position) {
            0 -> return TYPE_HEADER
            pairedList.size + 1 -> return TYPE_HEADER
            else -> return TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return pairedList.size + availableList.size + 2
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == TYPE_ITEM) {
            val view = LayoutInflater.from(parent?.context)
                    .inflate(R.layout.item_paired_device, parent, false)
            return DeviceViewHolder(view)
        }

        val view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_header, parent, false)
        return HeaderViewHolder(view)
    }

    fun addNewFoundDevice(device: BluetoothDevice) {
        val exists = availableList.filter { it.address == device.address }.any()
        if (!exists) {
            availableList.add(device)
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var header: TextView = itemView.findViewById(R.id.tv_header) as TextView
        var emptyMessage: TextView = itemView.findViewById(R.id.tv_empty_message) as TextView
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.tv_name) as TextView
        var macAddress: TextView = itemView.findViewById(R.id.tv_mac_address) as TextView
    }
}

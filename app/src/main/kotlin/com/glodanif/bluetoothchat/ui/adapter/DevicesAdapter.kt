package com.glodanif.bluetoothchat.ui.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.glodanif.bluetoothchat.R
import java.util.*

class DevicesAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val typeItem = 0
    private val typeHeader = 1

    var listener: ((BluetoothDevice) -> Unit)? = null

    var availableList = LinkedList<BluetoothDevice>()
    var pairedList = ArrayList<BluetoothDevice>()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is HeaderViewHolder) {

            holder.header.text = context.getString(if (position == 0)
                R.string.scan__available_devices else R.string.scan__paired_devices)
            holder.emptyMessage.visibility = if (if (position == 0)
                        availableList.isEmpty() else pairedList.isEmpty()) View.VISIBLE else View.GONE

        } else if (holder is DeviceViewHolder) {

            val device = if (position >= 1 && position < availableList.size + 1)
                availableList[position - 1] else pairedList[position - availableList.size - 2]
            holder.name.text = device.name
            holder.macAddress.text = device.address
            holder.itemView.setOnClickListener { listener?.invoke(device) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> typeHeader
            availableList.size + 1 -> typeHeader
            else -> typeItem
        }
    }

    override fun getItemCount(): Int {
        return pairedList.size + availableList.size + 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == typeItem) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_paired_device, parent, false)
            return DeviceViewHolder(view)
        }

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header_devices, parent, false)
        return HeaderViewHolder(view)
    }

    fun addNewFoundDevice(device: BluetoothDevice) {
        val exists = availableList.asSequence()
                .filter { it.address == device.address }
                .any()
        if (!exists) {
            availableList.addFirst(device)
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var header: TextView = itemView.findViewById(R.id.tv_header)
        var emptyMessage: TextView = itemView.findViewById(R.id.tv_empty_message)
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.tv_name)
        var macAddress: TextView = itemView.findViewById(R.id.tv_mac_address)
    }
}

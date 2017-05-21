package com.glodanif.bluetoothchat.adapter

import android.bluetooth.BluetoothDevice
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.glodanif.bluetoothchat.R

class PairedDevicesAdapter : RecyclerView.Adapter<PairedDevicesAdapter.DeviceViewHolder>() {

    var devicesList = ArrayList<BluetoothDevice>()

    override fun onBindViewHolder(holder: DeviceViewHolder?, position: Int) {
        val device = devicesList[position]
        holder?.name?.text = device.name
        holder?.macAddress?.text = device.address
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_paired_device, parent, false)
        return DeviceViewHolder(view)
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name: TextView? = null
        var macAddress: TextView? = null

        init {
            name = itemView.findViewById(R.id.tv_name) as TextView
            macAddress = itemView.findViewById(R.id.tv_mac_address) as TextView
        }
    }
}

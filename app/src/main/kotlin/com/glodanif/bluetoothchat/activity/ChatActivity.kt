package com.glodanif.bluetoothchat.activity

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.presenter.ChatPresenter
import com.glodanif.bluetoothchat.view.ChatView

class ChatActivity : AppCompatActivity(), ChatView {

    private var presenter: ChatPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val device: BluetoothDevice = intent.getParcelableExtra(EXTRA_BLUETOOTH_DEVICE)
    }

    companion object {

        val EXTRA_BLUETOOTH_DEVICE = "extra.bluetooth_device"

        fun start(context: Context, device: BluetoothDevice?) {
            val intent: Intent = Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_BLUETOOTH_DEVICE, device)
            context.startActivity(intent)
        }
    }
}

package com.glodanif.bluetoothchat.presenter

import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.view.ConversationsView

class ConversationsPresenter(private val view: ConversationsView, private val connection: BluetoothConnector) {

    fun onStart() {

        connection.setOnPrepareListener(object : BluetoothConnector.OnPrepareListener {

            override fun onPrepared() {
                connection.prepareForAccept()
            }

            override fun onError() {

            }

        })
        connection.prepare()
    }

    fun onStop() {
        connection.stop()
    }
}

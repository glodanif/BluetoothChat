package com.glodanif.bluetoothchat.activity

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.model.SettingsManagerImpl

class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val settings = SettingsManagerImpl(this)
            if (settings.getUserName().isEmpty()) {
                ProfileActivity.start(this, true)
            } else {
                ConversationsActivity.start(this)
            }
            finish()
        }, 1000)
    }
}
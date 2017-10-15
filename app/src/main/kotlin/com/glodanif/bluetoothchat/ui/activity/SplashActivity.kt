package com.glodanif.bluetoothchat.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.model.SettingsManagerImpl

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({

            val settings = SettingsManagerImpl(this)
            val intent = Intent(this,
                    if (settings.getUserName().isEmpty())
                        ProfileActivity::class.java else ConversationsActivity::class.java)

            if (getIntent().action == Intent.ACTION_SEND) {
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra(Intent.EXTRA_TEXT))
            }

            startActivity(intent)

            finish()
        }, 500)
    }
}

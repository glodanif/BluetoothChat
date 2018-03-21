package com.glodanif.bluetoothchat.ui.activity

import android.content.Intent
import android.net.Uri
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
            val newIntent = Intent(this,
                    if (settings.getUserName().isEmpty())
                        ProfileActivity::class.java else ConversationsActivity::class.java)

            if (intent.action == Intent.ACTION_SEND) {
                newIntent.action = Intent.ACTION_SEND
                newIntent.type = intent.type
                newIntent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
                newIntent.putExtra(Intent.EXTRA_STREAM, intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
            }

            startActivity(newIntent)

            finish()
        }, 500)
    }
}

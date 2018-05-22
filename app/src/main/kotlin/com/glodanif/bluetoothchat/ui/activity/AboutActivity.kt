package com.glodanif.bluetoothchat.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.glodanif.bluetoothchat.BuildConfig
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.utils.bind

class AboutActivity : SkeletonActivity() {

    private val versionLabel: TextView by bind(R.id.tv_version)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about, ActivityType.CHILD_ACTIVITY)

        versionLabel.text = "v${BuildConfig.VERSION_NAME} / ${BuildConfig.VERSION_CODE}"

        findViewById<Button>(R.id.btn_github).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://github.com/glodanif/BluetoothChat")
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_crowdin).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://crowdin.com/project/bluetoothchat")
            }
            startActivity(intent)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AboutActivity::class.java)
            context.startActivity(intent)
        }
    }
}

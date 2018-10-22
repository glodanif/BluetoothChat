package com.glodanif.bluetoothchat.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.ui.presenter.SplashPresenter
import com.glodanif.bluetoothchat.ui.router.SplashRouter
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class SplashActivity : AppCompatActivity(), SplashRouter {

    private val presenter: SplashPresenter by inject { parametersOf(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            presenter.onSplashShown()
        }, 500)
    }

    override fun redirectToProfileSetup() {
        val originalIntent = Intent(this, ProfileActivity::class.java).apply {
            putExtra(ProfileActivity.EXTRA_SETUP_MODE, true)
        }
        val finalIntent = if (intent.action == Intent.ACTION_SEND)
            appendSharingData(originalIntent) else originalIntent
        startActivity(finalIntent)
        finish()
    }

    override fun redirectToConversations() {
        val originalIntent = Intent(this, ConversationsActivity::class.java)
        val finalIntent = if (intent.action == Intent.ACTION_SEND)
            appendSharingData(originalIntent) else originalIntent
        startActivity(finalIntent)
        finish()
    }

    private fun appendSharingData(originalIntent: Intent): Intent = with(originalIntent) {
        action = Intent.ACTION_SEND
        type = intent.type
        putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
        putExtra(Intent.EXTRA_STREAM, intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return this
    }
}

package com.glodanif.bluetoothchat

import android.app.Activity
import android.app.Application
import com.crashlytics.android.Crashlytics
import com.glodanif.bluetoothchat.activity.ChatActivity
import com.glodanif.bluetoothchat.activity.ConversationsActivity
import com.glodanif.bluetoothchat.util.StartStopActivityLifecycleCallbacks
import io.fabric.sdk.android.Fabric

class ChatApplication : Application() {

    var isConversationsOpened = false
    var currentChat: String? = null

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }

        registerActivityLifecycleCallbacks(object : StartStopActivityLifecycleCallbacks() {

            override fun onActivityStarted(activity: Activity?) {

                isConversationsOpened = activity is ConversationsActivity

                if (activity is ChatActivity) {
                    currentChat = activity.intent.getStringExtra(ChatActivity.EXTRA_ADDRESS)
                }
            }

            override fun onActivityStopped(activity: Activity?) {

                if (activity is ConversationsActivity) {
                    isConversationsOpened = false
                }

                if (activity is ChatActivity) {
                    currentChat = null
                }
            }
        })
    }
}

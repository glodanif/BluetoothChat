package com.glodanif.bluetoothchat

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ProcessLifecycleOwner
import android.os.StrictMode
import com.crashlytics.android.Crashlytics
import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.model.ProfileManager
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.ui.activity.ChatActivity
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import com.glodanif.bluetoothchat.ui.util.StartStopActivityLifecycleCallbacks
import com.kobakei.ratethisapp.RateThisApp
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import javax.inject.Inject

class ChatApplication : Application(), LifecycleObserver {

    var isConversationsOpened = false
    var currentChat: String? = null

    @Inject
    internal lateinit var connector: BluetoothConnector
    @Inject
    internal lateinit var profileManager: ProfileManager

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }

        ComponentsManager.initialize(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

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

        val config = RateThisApp.Config().apply {
            setTitle(R.string.rate_dialog__title)
            setMessage(R.string.rate_dialog__message)
            setYesButtonText(R.string.rate_dialog__rate)
            setNoButtonText(R.string.rate_dialog__no)
            setCancelButtonText(R.string.rate_dialog__cancel)
        }
        RateThisApp.init(config)

        if (BuildConfig.DEBUG) {

            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    internal fun prepareConnection() {
        if (!profileManager.getUserName().isEmpty()) {
            connector.prepare()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    internal fun releaseConnection() {
        connector.release()
    }
}

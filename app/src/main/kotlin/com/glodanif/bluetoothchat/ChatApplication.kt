package com.glodanif.bluetoothchat

import android.app.Activity
import android.app.Application
import android.os.Bundle

class ChatApplication : Application() {

    private var inForeground = 0

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

            override fun onActivityStarted(activity: Activity?) {
                inForeground++
            }

            override fun onActivityStopped(activity: Activity?) {
                inForeground--
            }

            override fun onActivityResumed(activity: Activity?) {
            }

            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            }

            override fun onActivityDestroyed(activity: Activity?) {
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

        })
    }

    fun isInForeground(): Boolean = inForeground > 0
}

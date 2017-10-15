package com.glodanif.bluetoothchat.ui.util

import android.app.Activity
import android.app.Application
import android.os.Bundle

abstract class StartStopActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

    override fun onActivityResumed(activity: Activity?) {}

    override fun onActivityPaused(activity: Activity?) {}

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity?) {}

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
}
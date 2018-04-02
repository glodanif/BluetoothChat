package com.glodanif.bluetoothchat.data.internal

import android.support.test.espresso.IdlingResource
import java.util.concurrent.atomic.AtomicBoolean

class SimpleIdlingResource: IdlingResource {

    @Volatile
    private var callback: IdlingResource.ResourceCallback? = null
    private var isIdleNow = AtomicBoolean(true)

    override fun getName(): String {
        return this.javaClass.name
    }

    override fun isIdleNow(): Boolean {
        return isIdleNow.get()
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.callback = callback
    }

    fun setIdleState(isIdle: Boolean) {
        isIdleNow.set(isIdle)
        if (isIdle) {
            callback?.onTransitionToIdle()
        }
    }
}

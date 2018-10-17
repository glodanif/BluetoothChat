package com.glodanif.bluetoothchat.ui.presenter

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlin.coroutines.experimental.CoroutineContext

open class BasePresenter(private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main): CoroutineScope, LifecycleObserver {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + uiDispatcher


    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun detach() {
        job.cancel()
    }
}

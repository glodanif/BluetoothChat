package com.glodanif.bluetoothchat.ui.presenter

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

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

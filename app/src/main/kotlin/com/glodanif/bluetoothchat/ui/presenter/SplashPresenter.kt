package com.glodanif.bluetoothchat.ui.presenter

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.domain.interactor.IsProfileInitializedInteractor
import com.glodanif.bluetoothchat.ui.router.SplashRouter

class SplashPresenter(private val router: SplashRouter,
                      private val isProfileInitializedInteractor: IsProfileInitializedInteractor
) : LifecycleObserver {

    fun onSplashShown() {

        isProfileInitializedInteractor.execute(Unit,
                onResult = { isInitialized ->
                    if (isInitialized) {
                        router.redirectToConversations()
                    } else {
                        router.redirectToProfileSetup()
                    }
                }
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        isProfileInitializedInteractor.cancel()
    }
}

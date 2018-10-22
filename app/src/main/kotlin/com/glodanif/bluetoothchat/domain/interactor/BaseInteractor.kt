package com.glodanif.bluetoothchat.domain.interactor

import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext

abstract class BaseInteractor<T, V>(
        private val executionContext: CoroutineDispatcher = Dispatchers.Default,
        private val resultContext: CoroutineDispatcher = Dispatchers.Main
) : CoroutineScope {

    protected abstract suspend fun execute(input: T): V

    private var onResult: ((V) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null

    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = executionContext + job

    fun execute(input: T, onResult: ((V) -> Unit)? = null, onError: ((Throwable) -> Unit)? = null) {
        this.onResult = onResult
        this.onError = onError
        launch {
            executeSafe(input)
        }
    }

    fun cancel() {
        job.cancel()
        onResult = null
        onError = null
    }

    private suspend fun executeSafe(input: T) {
        try {
            val result = execute(input)
            runInResultContext { onResult?.invoke(result) }
        } catch (throwable: Throwable) {
            runInResultContext { onError?.invoke(throwable) }
        }
    }

    private fun runInResultContext(block: () -> Unit) = launch(resultContext) {
        block()
    }
}

package com.glodanif.bluetoothchat.ui.util

import androidx.annotation.CallSuper
import android.text.Editable
import android.text.TextWatcher

abstract class SimpleTextWatcher: TextWatcher {

    abstract fun afterTextChanged(text: String)

    @CallSuper
    override fun afterTextChanged(s: Editable?) {
        val text = s?.toString() ?: ""
        afterTextChanged(text)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}
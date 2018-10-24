package com.glodanif.bluetoothchat.domain.exception

class InvalidStringException(val forbiddenSymbol: String = "", message: String? = null) : Throwable(message)

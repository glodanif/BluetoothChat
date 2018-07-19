package com.glodanif.bluetoothchat.utils

import java.util.*

class LimitedQueue<T>(private val capacity: Int): LinkedList<T>() {

    override fun add(element: T): Boolean {
        val added = super.add(element)
        if (size > capacity) {
            removeFirst()
        }
        return added
    }
}

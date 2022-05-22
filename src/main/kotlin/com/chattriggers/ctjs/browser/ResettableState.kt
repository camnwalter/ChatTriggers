package com.chattriggers.ctjs.browser

import gg.essential.elementa.state.BasicState

class ResettableState<T>(private val initialValue: T) : BasicState<T>(initialValue) {
    fun reset() = set(initialValue)
}

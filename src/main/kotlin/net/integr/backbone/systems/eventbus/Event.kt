package net.integr.backbone.systems.eventbus

class Event<T>(val payload: T) {
    var cancelled: Boolean = false

    fun cancel() {
        cancelled = true
    }

    companion object {
        fun <T> singleton(any: T) = Event(any)
    }
}
package net.integr.backbone.systems.event

class Event<T>(val payload: T) {
    var cancelled: Boolean = false

    fun cancel() {
        cancelled = true
    }

    companion object {
        fun <T> singleton(any: T) = Event(any)
    }
}
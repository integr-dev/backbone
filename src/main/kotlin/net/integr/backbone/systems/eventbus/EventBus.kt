package net.integr.backbone.systems.eventbus

import kotlin.reflect.KClass

object EventBus {
    val listeners: MutableMap<KClass<*>, MutableList<(Event<*>) -> Unit>> = mutableMapOf()
}


inline fun <reified T> listener(noinline listener: (Event<T>) -> Unit) {
    val eventListeners = EventBus.listeners.getOrPut(T::class) { mutableListOf() }
    @Suppress("UNCHECKED_CAST")
    eventListeners.add(listener as (Event<*>) -> Unit)
}

inline fun <reified T> fire(event: Event<T>) {
    val eventListeners = EventBus.listeners[T::class] ?: return
    for (listener in eventListeners) {
        @Suppress("UNCHECKED_CAST")
        (listener as (Event<T>) -> Unit)(event)
        if (event.cancelled) break
    }
}
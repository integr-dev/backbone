package net.integr.backbone.systems.hotloader

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LifecycleSustainedState<T>(private var value: T) : ReadWriteProperty<ManagedLifecycle, T> {
    var id: String? = null
        private set

    override fun getValue(thisRef: ManagedLifecycle, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: ManagedLifecycle, property: KProperty<*>, value: T) {
        this.value = value
    }

    operator fun provideDelegate(thisRef: ManagedLifecycle, property: KProperty<*>): ReadWriteProperty<ManagedLifecycle, T> {
        id = property.name
        thisRef.trackState(this)
        return this
    }

    fun dangerouslySetState(value: Any?) {
        @Suppress("UNCHECKED_CAST")
        this.value = value as T
    }

    fun dangerouslyGetState(): Any? {
        return value as Any?
    }
}

fun <T> sustained(value: T) = LifecycleSustainedState(value)
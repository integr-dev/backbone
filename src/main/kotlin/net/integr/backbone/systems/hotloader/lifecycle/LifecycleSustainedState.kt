/*
 * Copyright © 2026 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.backbone.systems.hotloader.lifecycle

import org.jetbrains.annotations.ApiStatus
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A property delegate that allows a state to be sustained across script reloads.
 *
 * This delegate is designed to be used within [ManagedLifecycle] classes. When a script is reloaded,
 * the value of a `LifecycleSustainedState` property can be preserved, preventing it from being
 * reset to its initial value.
 *
 * @param T The type of the value being sustained.
 * @property value The current value of the state.
 * @since 1.0.0
 */
class LifecycleSustainedState<T>(private var value: T) : ReadWriteProperty<Nothing?, T> {
    /**
     * The unique identifier for this sustained state. This is assigned counting up from 0 for each `ManagedLifecycle` instance, based on the order of declaration.
     *
     * @since 1.0.0
     */
    var id: Int? = null
        internal set

    /**
     * The default value of the state.
     * @since 1.0.0
     */
    val default: T = value


    /**
     * Returns the current value of the sustained state.
     *
     * @param thisRef The [ManagedLifecycle] instance that owns this property.
     * @param property The [KProperty] representing this state property.
     * @return The current value of the state.
     * @since 1.0.0
     */
    override fun getValue(thisRef: Nothing?, property: KProperty<*>): T {
        return value
    }

    /**
     * Sets the value of the sustained state.
     *
     * @param thisRef The [ManagedLifecycle] instance that owns this property.
     * @param property The [KProperty] representing this state property.
     * @param value The new value to set.
     * @since 1.0.0
     */
    override fun setValue(thisRef: Nothing?, property: KProperty<*>, value: T) {
        this.value = value
    }

    /**
     * Sets the state of this property to the given value without type checking.
     * This method is intended for internal use by the hotloader to restore state after a reload.
     *
     * @param value The value to set.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun dangerouslySetState(value: Any?) {
        @Suppress("UNCHECKED_CAST")
        this.value = value as T
    }

    /**
     * Dangerously get the raw state of this property.
     *
     * This method is intended for internal use by the hotloader to retrieve the state after a reload.
     * It bypasses type checking and returns the raw value of the state.
     *
     * @return The raw state value.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun dangerouslyGetState(): Any? {
        return value as Any?
    }

    /**
     * Resets the state of this property to its default value.
     *
     * This method is intended for internal use by the hotloader to restore state after a reload.
     * It sets the state to the default value without type checking.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun wipeState() {
        value = default
    }
}

/**
 * Support function to create a [LifecycleSustainedState] instance.
 *
 * This function provides a convenient way to declare a property that will have its state
 * sustained across script reloads within a [ManagedLifecycle] class.
 *
 * @param T The type of the value to be sustained.
 * @param value The initial value of the state.
 * @return A [LifecycleSustainedState] instance initialized with the given value.
 * @since 1.0.0
 */
fun <T> sustained(value: T) = LifecycleSustainedState(value)
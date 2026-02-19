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

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LifecycleSustainedState<T>(private var value: T) : ReadWriteProperty<ManagedLifecycle, T> {
    var id: String? = null
        private set

    val default: T = value

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

    fun wipeState() {
        value = default
    }
}

fun <T> sustained(value: T) = LifecycleSustainedState(value)
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

package net.integr.backbone.systems.event

/**
 * Represents a Backbone event. Extend this class to create an event.
 *
 * @since 1.0.0
 */
abstract class Event {
    /**
     * The callback for this event.
     *
     * Internally uses the [GLOBAL_CALLBACK] key on the callback-map.
     *
     * @since 1.0.0
     */
    var callback: Any?
        get() = callbacks[GLOBAL_CALLBACK]
        set(value) {
            callbacks[GLOBAL_CALLBACK] = value
        }

    /**
     * The callbacks for this event.
     * @since 1.3.0
     */
    private var callbacks: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Adds a callback to this event.
     *
     * @param key The key to store the callback under.
     * @param value The callback to store.
     * @since 1.3.0
    */
    open operator fun set(key: String, value: Any?) {
        callbacks[key] = value
    }

    /**
     * Adds a callback to this event.
     *
     * @param key The key to store the callback under.
     * @param value The callback to store.
     * @since 1.6.0
     */
    fun setCallback(key: String, value: Any?) {
        callbacks[key] = value
    }

    /**
     * Gets a callback from this event.
     * @since 1.3.0
     */
    open operator fun get(key: String): Any? {
        return callbacks[key]
    }

    /**
     * Gets a callback from this event.
     * @since 1.6.0
     */
    fun getCallback(key: String): Any? {
        return callbacks[key]
    }

    companion object {
        /**
         * The global callback key.
         *
         * This is the default callback key used by Backbone.
         * It is used to store the callback for the event.
         *
         * @since 1.3.0
         */
        const val GLOBAL_CALLBACK = "global"
    }

    /**
     * Represents a cancelable Backbone event. Extend this class to create an event.
     *
     * @since 1.3.0
     */
    abstract class Cancelable : Event() {
        var canceled: Boolean = false

        /**
         * Cancels the event and stops it from spreading in the bus.
         *
         * @since 1.3.0
         */
        fun cancel() {
            canceled = true
        }
    }
}
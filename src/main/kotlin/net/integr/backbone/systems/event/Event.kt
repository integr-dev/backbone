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
open class Event {
    private var callback: Any? = null
    private var isCancelled: Boolean = false

    /**
     * Sets the callback for the event.
     *
     * @param value The callback object.
     * @since 1.0.0
     */
    fun setCallback(value: Any?) {
        callback = value
    }

    /**
     * Cancels the event and stops it from spreading in the bus.
     *
     * @since 1.0.0
     */
    fun cancel() {
        isCancelled = true
    }

    /**
     * Get the callback object for the event.
     *
     * @return The callback object, or null if not set.
     * @since 1.0.0
     */
    fun callback(): Any? = callback

    /**
     * Checks if the event has been cancelled.
     *
     * @return True if the event has been cancelled, false otherwise.
     * @since 1.0.0
     */
    fun isCancelled() = isCancelled
}
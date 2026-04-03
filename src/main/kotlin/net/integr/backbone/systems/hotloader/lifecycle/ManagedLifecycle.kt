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

import net.integr.backbone.Backbone
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus

/**
 * A base class for components that have a managed lifecycle within the hot-reloading system.
 *
 * Classes extending `ManagedLifecycle` can participate in the hot-reloading process,
 * allowing their state to be preserved across reloads and defining custom logic for
 * loading and unloading. They can also track [LifecycleSustainedState] properties
 * to automatically manage state persistence.
 *
 * @since 1.0.0
 */
abstract class ManagedLifecycle : Listener {
    private var sustainedStates: Set<LifecycleSustainedState<*>> = mutableSetOf()
    private var sustainedNextId = 0


    /**
     * Called when the component is loaded.
     *
     * This method is invoked when the script containing this component is initially loaded
     * or reloaded. Implementations should contain logic for initializing the component's
     * functionality.
     * @since 1.0.0
     */
    protected abstract suspend fun onLoad()

    /**
     * Called when the component is unloaded.
     *
     * This method is invoked when the script containing this component is unloaded,
     * typically during a hot-reload or when the system is shutting down. Implementations
     * should contain logic for cleaning up resources and de-initializing the component.
     * @since 1.0.0
     */
    protected abstract suspend fun onUnload()

    /**
     * Used internally by the hot-reloader to trigger loading of the lifecycle.
     *
     * @since 1.0.0
     */
    @ApiStatus.Internal
    suspend fun load() {
        onLoad()
    }

    /**
     * Used internally by the hot-reloader to trigger unloading of the lifecycle.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    suspend fun unload() {
        onUnload()
    }

    /**
     * Creates a new [LifecycleSustainedState] with the given initial value and tracks it for state persistence.
     *
     * This method should be used within `ManagedLifecycle` classes to create sustained state properties.
     * The returned `LifecycleSustainedState` can be delegated to a property to enable state persistence across reloads.
     *
     * @param T The type of the value being sustained.
     * @param value The initial value of the sustained state.
     * @return A new `LifecycleSustainedState` instance initialized with the given value.
     * @since 1.6.0
     */
    fun <T> sustained(value: T): LifecycleSustainedState<T> {
        val s = LifecycleSustainedState(value)
        s.id = sustainedNextId++
        trackState(s)
        return s
    }

    /**
     * Add a [LifecycleSustainedState] to be tracked by this [ManagedLifecycle] instance.
     *
     * @param state The [LifecycleSustainedState] to track.
     * @since 1.0.0
     */
    fun trackState(state: LifecycleSustainedState<*>) {
        sustainedStates += state
    }

    /**
     * Updates the sustained states of this lifecycle instance from another lifecycle instance.
     * This method is used during hot-reloading to transfer state from the old instance to the new instance.
     *
     * @param lifecycle The old [ManagedLifecycle] instance from which to copy the sustained states.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun updateStatesFrom(lifecycle: ManagedLifecycle) {
        for (state in lifecycle.sustainedStates) {
            val newState = sustainedStates.firstOrNull() { it.id == state.id }
            newState?.dangerouslySetState(state.dangerouslyGetState())
        }
    }

    /**
     * Wipes all sustained states back to their default values.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun wipeStates() {
        for (state in sustainedStates) {
            state.wipeState()
        }
    }
}
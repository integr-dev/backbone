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

import org.bukkit.event.Listener

abstract class ManagedLifecycle : Listener {
    private var sustainedStates: Set<LifecycleSustainedState<*>> = mutableSetOf()

    abstract fun onLoad()
    abstract fun onUnload()

    fun trackState(state: LifecycleSustainedState<*>) {
        sustainedStates += state
    }

    fun updateStatesFrom(lifecycle: ManagedLifecycle) {
        for (state in lifecycle.sustainedStates) {
            val newState = sustainedStates.firstOrNull() { it.id == state.id }
            newState?.dangerouslySetState(state.dangerouslyGetState())
        }
    }

    fun wipeStates() {
        for (state in sustainedStates) {
            state.wipeState()
        }
    }
}
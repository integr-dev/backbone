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

package net.integr.backbone.systems.hotloader

import net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle

/**
 * DSL entrypoint for defining a script lifecycle.
 *
 * @param block The lifecycle configuration block.
 * @return The constructed [ManagedLifecycle].
 * @since 1.6.0
 */
fun lifecycle(block: LifecycleBuilder.() -> Unit): ManagedLifecycle {
    val builder = LifecycleBuilder()
    block(builder)
    return builder
}

/**
 * Builder for script lifecycle and event/listener registration DSL.
 *
 * Provides methods for registering load/unload hooks, event listeners, and inter-script communication handlers.
 * @since 1.6.0
 */
class LifecycleBuilder : ManagedLifecycle() {
    val onLoadCalls: MutableList<() -> Unit> = mutableListOf()
    val onUnloadCalls: MutableList<() -> Unit> = mutableListOf()

    override fun onLoad() {
        onLoadCalls.forEach { it() }
    }

    override fun onUnload() {
        onUnloadCalls.forEach { it() }
    }

    /**
     * Registers a block to run when the script is loaded.
     * @since 1.6.0
     */
    fun onLoad(block: () -> Unit) {
        onLoadCalls += block
    }

    /**
     * Registers a block to run when the script is unloaded.
     * @since 1.6.0
     */
    fun onUnload(block: () -> Unit) {
        onUnloadCalls += block
    }
}
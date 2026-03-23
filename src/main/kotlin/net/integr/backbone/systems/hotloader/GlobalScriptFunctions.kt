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

import net.integr.backbone.Backbone
import net.integr.backbone.systems.event.BackboneEventHandler
import net.integr.backbone.systems.hotloader.isc.InterScriptDuckTypedEvent
import net.integr.backbone.systems.hotloader.isc.IscMap
import net.integr.backbone.systems.hotloader.isc.IscMapBuilder
import net.integr.backbone.systems.hotloader.lifecycle.LifecycleSustainedState
import net.integr.backbone.systems.hotloader.lifecycle.ManagedLifecycle
import net.integr.backbone.systems.hotloader.lifecycle.sustained
import net.integr.backbone.systems.text.component
import java.awt.Color
import kotlin.inc
import kotlin.reflect.full.starProjectedType
import kotlin.toString

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

    /**
     * Registers a Bukkit event listener for the given event type.
     *
     * @param priority The Bukkit event priority (default: NORMAL).
     * @param block The event handler block.
     * @since 1.6.0
     */
    inline fun <reified T : org.bukkit.event.Event> listener(priority: org.bukkit.event.EventPriority = org.bukkit.event.EventPriority.NORMAL, crossinline block: (T) -> Unit) {
        onLoadCalls += { Backbone.SERVER.pluginManager.registerEvent(T::class.java, this, priority, { _, event -> if (event is T) block(event) }, Backbone.PLUGIN) }
        onUnloadCalls += { Backbone.unregisterListener(this) }
    }

    /**
     * Registers a Backbone event listener for the given event type.
     *
     * @param priority The Backbone event priority (default: NORMAL).
     * @param block The event handler block.
     * @since 1.6.0
     */
    inline fun <reified T : net.integr.backbone.systems.event.Event> backboneListener(priority: net.integr.backbone.systems.event.EventPriority = net.integr.backbone.systems.event.EventPriority.NORMAL, noinline block: (T) -> Unit) {
        onLoadCalls += { Backbone.EVENT_BUS.registerLambda(T::class.starProjectedType, priority, this) { event -> if (event is T) block(event) } }
        onUnloadCalls += { Backbone.unregisterListener(this) }
    }

    /**
     * Registers a handler for inter-script communication events with the given id.
     *
     * @param id The message id to listen for.
     * @param block The handler block, receiving the message data as [IscMap].
     * @since 1.6.0
     */
    fun interScript(id: String, block: (IscMap) -> Unit) {
        backboneListener<InterScriptDuckTypedEvent> {
            if (it.id == id) block(it.data)
        }
    }

    /**
     * Dispatches an inter-script communication event with the given id and data.
     *
     * @param id The message id to send.
     * @param data The data builder block for the message.
     * @since 1.6.0
     */
    fun dispatchInterScript(id: String, data: IscMapBuilder.() -> Unit) {
        val builder = IscMapBuilder()
        builder.data()
        val isc = builder.build()
        Backbone.EVENT_BUS.post(InterScriptDuckTypedEvent(id, isc))
    }
}
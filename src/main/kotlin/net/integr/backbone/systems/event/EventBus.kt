/*
 * Copyright © 2025 Integr
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

@file:Suppress("unused")

package net.integr.backbone.systems.event

import net.integr.backbone.Backbone
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.iterator
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

object EventBus {
    private val logger = Backbone.LOGGER.derive("event-system")

    /* event_id to (priority to (handler to instance)) */
    private var eventHandlers: ConcurrentHashMap<String, SortedMap<Int, ConcurrentHashMap<KCallable<*>, Any>>> =
        ConcurrentHashMap()

    fun register(klass: KClass<*>, instance: Any) {
        for (member in klass.members) {
            if (member.hasAnnotation<BackboneEventHandler>()) {
                if (member.parameters.size > 2) throw IllegalArgumentException(
                    "Only one parameter is allowed at ${member.javaClass.declaringClass?.kotlin?.simpleName}.${member.name}()"
                )

                val priority = member.findAnnotation<BackboneEventHandler>()?.priority?.ordinal ?: 0

                val targetEventId = member.parameters[1].type.classifier
                    ?.let { (it as? KClass<*>)?.qualifiedName } ?: continue

                val entry = eventHandlers.getOrPut(targetEventId) { sortedMapOf() }
                val priorityEntry = entry.getOrPut(priority) { ConcurrentHashMap() }

                priorityEntry[member] = instance
            }
        }
    }

    fun register(klass: KClass<*>) = register(klass, klass.createInstance())
    fun register(instance: Any)  = register(instance::class, instance)

    fun unRegister(klass: KClass<*>) {
        for (member in klass.members) {
            if (member.hasAnnotation<BackboneEventHandler>()) {
                val targetEventId = member.parameters[1].type.classifier
                    ?.let { (it as? KClass<*>)?.qualifiedName } ?: continue

                val priority = member.findAnnotation<BackboneEventHandler>()?.priority?.ordinal ?: 0

                val entry = eventHandlers[targetEventId] ?: continue
                val priorityEntry = entry[priority] ?: continue

                priorityEntry.remove(member)
            }
        }
    }

    fun unRegister(instance: Any) = unRegister(instance::class)

    fun post(event: Event): Any? {
        val eventId = event::class.qualifiedName ?: return null
        val priorityEntries = eventHandlers[eventId] ?: return null

        for ((_, handlersInPriority) in priorityEntries) callOnPriority(event, handlersInPriority)

        return event.getCallback()
    }

    fun postToPriority(event: Event, priority: EventPriority): Any? {
        val eventId = event::class.qualifiedName ?: return null
        val priorityEntries = eventHandlers[eventId] ?: return null
        val handlersInPriority = priorityEntries[priority.ordinal] ?: return null

        callOnPriority(event, handlersInPriority)

        return event.getCallback()
    }

    private fun callOnPriority(event: Event, entry: ConcurrentHashMap<KCallable<*>, Any>) {
        for ((handler, instance) in entry) {
            try {
                if (handler.parameters[1].type.classifier?.let { (it as? KClass<*>)?.qualifiedName } != event::class.qualifiedName) {
                    logger.warning("Skipping event handler ${handler.name} due to class loader mismatch.")
                    continue
                }

                handler.call(instance, event)
            } catch (e: InvocationTargetException) {
                val handlerName = handler.name
                val declaringClass = handler.javaClass.declaringClass?.name ?: handler.javaClass.name
                logger.severe("Fatal Error during event in handler: $declaringClass.$handlerName")
                e.printStackTrace()
            }
        }
    }

}
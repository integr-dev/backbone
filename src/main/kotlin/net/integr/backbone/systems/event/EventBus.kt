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

@file:Suppress("unused")

package net.integr.backbone.systems.event

import net.integr.backbone.Backbone
import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * A simple event bus for publishing and subscribing to events.
 *
 * This class provides methods for registering and unregistering event handlers,
 * and for posting events to be handled by registered handlers.
 *
 * @since 1.0.0
 */
object EventBus {
    private val logger = Backbone.LOGGER.derive("event-system")

    /**
     * Represents a singular handler with its priority, member and containing class instance.
     *
     * @param priority the priority of the handler
     * @param callable the method to invoke when firing
     * @param instance the instance of the containing class to call the member in
     * @since 1.0.0
     */
    private class EventHandler(var priority: Int, var callable: KCallable<*>, var instance: Any) : Comparable<EventHandler> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EventHandler

            return compareTo(other) == 0
        }

        override fun hashCode(): Int {
            var result = priority
            result = 31 * result + callable.toString().hashCode()
            result = 31 * result + System.identityHashCode(instance)
            return result
        }

        override fun compareTo(other: EventHandler): Int {
            return compareTo(other.priority, other.callable, other.instance)
        }

        fun compareTo(otherPriority: Int, otherCallable: KCallable<*>, otherInstance: Any): Int {
            val priorityComparison = priority.compareTo(otherPriority)
            if (priorityComparison != 0) return priorityComparison

            val callableComparison = callable.toString().compareTo(otherCallable.toString())
            if (callableComparison != 0) return callableComparison

            return System.identityHashCode(instance).compareTo(System.identityHashCode(otherInstance))
        }
    }

    private val eventHandlers: ConcurrentHashMap<String, ConcurrentSkipListSet<EventHandler>> = ConcurrentHashMap()

    /**
     * Registers all event handlers in the given class.
     *
     * @param klass The class to register.
     * @param instance The instance of the class to use for non-static methods.
     * @throws IllegalArgumentException if a handler in the class does not have exactly one parameter
     * @since 1.0.0
     */
    fun register(klass: KClass<*>, instance: Any) {
        for (member in klass.members) {
            if (member.hasAnnotation<BackboneEventHandler>()) {
                // include the default "this" parameter
                if (member.parameters.size > 2 || member.parameters.size <= 1)
                    throw IllegalArgumentException("Member must have exactly one parameter " +
                            "${member.javaClass.declaringClass?.kotlin?.simpleName}.${member.name}()")

                val priority = member.findAnnotation<BackboneEventHandler>()?.priority?.ordinal ?: 0

                val targetEventId = member.parameters[1].type.classifier
                    ?.let { (it as? KClass<*>)?.qualifiedName } ?: continue

                val newHandler = EventHandler(priority, member, instance)
                val eventHandlersForEventTarget = eventHandlers.computeIfAbsent(targetEventId) { ConcurrentSkipListSet() }

                logger.info("Registering event handler ${klass.simpleName}.${member.name}()")
                eventHandlersForEventTarget += newHandler
            }
        }
    }

    /**
     *
     * Registers all event handlers in the given instance.
     *
     * @param instance The instance to register.
     * @throws IllegalArgumentException if a handler in the class does not have exactly one parameter
     * @since 1.0.0
     */
    fun register(instance: Any) = register(instance::class, instance)

    /**
     * Removes all event handlers in the given class.
     *
     * @param klass The class to unregister.
     * @since 1.0.0
     */
    fun unregister(klass: KClass<*>, instance: Any) {
        for (member in klass.members) {
            if (member.hasAnnotation<BackboneEventHandler>()) {
                val priority = member.findAnnotation<BackboneEventHandler>()?.priority?.ordinal ?: 0

                val targetEventId = member.parameters[1].type.classifier
                    ?.let { (it as? KClass<*>)?.qualifiedName } ?: continue

                val eventHandlersForEventTarget = eventHandlers[targetEventId] ?: continue

                val handler = eventHandlersForEventTarget.find { it.compareTo(priority, member, instance) == 0 } ?: continue

                logger.info("Unregistering event handler ${klass.simpleName}.${member.name}()")
                eventHandlersForEventTarget.remove(handler)
            }
        }
    }

    /**
     * Removes all event handlers in the given class.
     *
     * @param instance The instance of the class to unregister.
     * @since 1.0.0
     */
    fun unregister(instance: Any) = unregister(instance::class, instance)

    /**
     * Clears all event handlers.
     *
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun clear() {
        logger.info("Clearing all event handlers")
        eventHandlers.clear()
    }

    /**
     * Posts an event to the bus, notifying all registered handlers.
     *
     * @param event The event to post.
     * @return The callback object from the event, or null if no callback was set.
     * @since 1.0.0
     */
    fun post(event: Event): Any? {
        val eventId = event::class.qualifiedName ?: return null
        val eventHandlers = eventHandlers[eventId] ?: return null

        for (handler in eventHandlers) {
            callHandler(event, handler)
            if (event.isCancelled()) return event.callback()
        }

        return event.callback()
    }


    private fun callHandler(event: Event, handler: EventHandler) {
        val callable = handler.callable

        try {
            if (callable.parameters[1].type.classifier?.let { (it as? KClass<*>)?.qualifiedName } != event::class.qualifiedName) {
                logger.warning("Skipping event handler ${callable.name} due to class loader mismatch.")
                return
            }

            callable.call(handler.instance, event)
        } catch (e: InvocationTargetException) {
            val declaringClass = callable.javaClass.declaringClass?.name ?: callable.javaClass.name
            logger.severe("Fatal Error during event in handler: $declaringClass.${callable.name}")
            e.printStackTrace()
        }

    }

}
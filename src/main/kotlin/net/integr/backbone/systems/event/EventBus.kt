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
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.defaultType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.starProjectedType

/**
 * A simple event bus for publishing and subscribing to events.
 *
 * This class provides methods for registering and unregistering event handlers
 * and for posting events to be handled by registered handlers.
 *
 * @since 1.0.0
 */
object EventBus {
    private val logger = Backbone.LOGGER.derive("event-bus")

    /**
     * Represents a singular handler with its priority, member, and containing class instance.
     *
     * This class uses a weak reference to hold the instance, allowing the garbage collector
     * to reclaim the instance if it's no longer referenced elsewhere. Handlers are comparable
     * based on their priority, callable, and instance identity. It uses weak references to
     * prevent memory leaks.
     *
     * @param priority the priority of the handler
     * @param callable the method to invoke when firing
     * @param instance the instance of the containing class to call the member in
     * @since 1.1.0
     */
    private class EventHandler(
        var priority: Int,
        var callable: KCallable<*>,
        instance: Any
    ) : Comparable<EventHandler> {
        private val instanceRef = WeakReference(instance)

        /**
         * The instance of the containing class, or null if it has been garbage collected.
         *
         * @since 1.1.0
         */
        val instance: Any?
            get() = instanceRef.get()

        /**
         * Whether the handler's instance is still alive (not garbage collected).
         *
         * @return true if the instance is still available, false otherwise
         * @since 1.1.0
         */
        val isAlive: Boolean
            get() = instanceRef.get() != null

        /**
         * Compares this handler to another object for equality.
         *
         * Two handlers are considered equal if they have the same comparison result
         * (same priority, callable, and instance).
         *
         * @param other the object to compare with
         * @return true if the handlers are equal, false otherwise
         * @since 1.1.0
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EventHandler

            return compareTo(other) == 0
        }

        /**
         * Returns a hash code value for this handler.
         *
         * The hash code is computed based on the priority, callable string representation,
         * and the identity hash code of the instance.
         *
         * @return a hash code value for this handler
         * @since 1.1.0
         */
        override fun hashCode(): Int {
            var result = priority
            result = 31 * result + callable.toString().hashCode()
            result = 31 * result + System.identityHashCode(instance)
            return result
        }

        /**
         * Compares this handler with another handler for order.
         *
         * Handlers are ordered by priority first, then by callable string representation,
         * and finally by instance identity hash code.
         *
         * @param other the handler to compare to
         * @return a negative integer, zero, or a positive integer as this handler is less than,
         *         equal to, or greater than the specified handler
         * @since 1.1.0
         */
        override fun compareTo(other: EventHandler): Int {
            return compareTo(other.priority, other.callable, other.instance)
        }

        /**
         * Compares this handler with the specified priority, callable, and instance.
         *
         * This method allows comparison without creating a temporary EventHandler object.
         * Handlers are ordered by priority first, then by callable string representation,
         * and finally by instance identity hash code.
         *
         * @param otherPriority the priority to compare to
         * @param otherCallable the callable to compare to
         * @param otherInstance the instance to compare to
         * @return a negative integer, zero, or a positive integer as this handler is less than,
         *         equal to, or greater than the specified values
         * @since 1.1.0
         */
        fun compareTo(otherPriority: Int, otherCallable: KCallable<*>, otherInstance: Any?): Int {
            val priorityComparison = priority.compareTo(otherPriority)
            if (priorityComparison != 0) return priorityComparison

            val callableComparison = callable.toString().compareTo(otherCallable.toString())
            if (callableComparison != 0) return callableComparison

            return System.identityHashCode(instance).compareTo(System.identityHashCode(otherInstance))
        }
    }

    private val eventHandlers: ConcurrentHashMap<KType, ConcurrentSkipListSet<EventHandler>> = ConcurrentHashMap()

    /**
     * Registers all event handlers in the given class for a specified instance.
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
                if (member.parameters.size !in 2..2)
                    throw IllegalArgumentException("Member must have exactly one parameter " +
                            "${member.javaClass.declaringClass?.kotlin?.simpleName}.${member.name}()")

                val priority = member.findAnnotation<BackboneEventHandler>()?.priority?.ordinal ?: 0

                val targetEventId = member.parameters[1].type

                val newHandler = EventHandler(priority, member, instance)
                val eventHandlersForEventTarget = eventHandlers.computeIfAbsent(targetEventId) { ConcurrentSkipListSet() }
                eventHandlersForEventTarget += newHandler
            }
        }
    }

    /**
     * Registers all event handlers in the given instance.
     *
     * @param instance The instance to register.
     * @throws IllegalArgumentException if a handler in the class does not have exactly one parameter
     * @since 1.0.0
     */
    fun register(instance: Any) = register(instance::class, instance)

    /**
     * Removes all event handlers in the given class for the specified instance.
     *
     * @param klass The class to unregister.
     * @since 1.0.0
     */
    fun unregister(klass: KClass<*>, instance: Any) {
        for (member in klass.members) {
            if (member.hasAnnotation<BackboneEventHandler>()) {
                // include the default "this" parameter
                if (member.parameters.size !in 2..2)
                    throw IllegalArgumentException("Member must have exactly one parameter " +
                            "${member.javaClass.declaringClass?.kotlin?.simpleName}.${member.name}()")

                val priority = member.findAnnotation<BackboneEventHandler>()?.priority?.ordinal ?: 0

                val targetEventId = member.parameters[1].type

                val eventHandlersForEventTarget = eventHandlers[targetEventId] ?: continue

                val handler = eventHandlersForEventTarget.find { it.compareTo(priority, member, instance) == 0 } ?: continue
                eventHandlersForEventTarget.remove(handler)
            }
        }
    }

    /**
     * Removes all event handlers in the given instance.
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
        val eventId = event::class.starProjectedType
        val eventHandlers = eventHandlers[eventId] ?: return null

        for (handler in eventHandlers) {
            callHandler(event, handler)
            if (event.isCancelled()) return event.callback()
        }

        return event.callback()
    }

    /**
     * Calls the handler with the given event.
     *
     * This method handles exceptions and logs them. Otherwise, the handler is called with the event. If the handler is no longer alive,
     * it is removed from the event's handler list. If an exception occurs during the call, the handler is removed from the list.
     *
     * @param event The event to call the handler with.
     * @param handler The handler to call.
     * @since 1.0.0
     */
    private fun callHandler(event: Event, handler: EventHandler) {
        val callable = handler.callable

        try {
            if (!handler.isAlive) {
                logger.warning("Removing dead event handler ${callable.name}.")
                eventHandlers.values.forEach { it.remove(handler) }
                return
            }

            callable.call(handler.instance, event)
        } catch (e: InvocationTargetException) {
            val declaringClass = callable.javaClass.declaringClass?.name ?: callable.javaClass.name
            logger.severe("Fatal Error during event in handler: $declaringClass.${callable.name}, removing handler.")
            eventHandlers.values.forEach { it.remove(handler) }
            e.printStackTrace()
        }
    }
}
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
import kotlin.reflect.KClass
import kotlin.reflect.KType
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

                val newHandler = MemberBackedEventHandler(priority, member, instance)
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
     * Registers a lambda as an event handler for the specified event type.
     *
     * @param eventType The type of the event to handle.
     * @param priority The priority of the handler. Handlers with higher priority are invoked first.
     * @param instance The instance to use as the receiver of the lambda.
     * @param lambda The lambda to register as an event handler.
     * @since 1.0.0
     */
    fun registerLambda(eventType: KType, priority: EventPriority, instance: Any, lambda: (Event) -> Unit) {
        val handler = LambdaEventHandler(priority.ordinal, lambda, instance)
        val eventHandlersForEventTarget = eventHandlers.computeIfAbsent(eventType) { ConcurrentSkipListSet() }
        eventHandlersForEventTarget += handler
    }

    /**
     * Removes all event handlers in the given class for the specified instance.
     *
     * @param klass The class to unregister.
     * @since 1.0.0
     */
    fun unregister(klass: KClass<*>, instance: Any) {
        // Clean lambda handlers for the instance
        eventHandlers.values.forEach { handlers ->
            handlers.removeIf { it is LambdaEventHandler && it.instance == instance }
        }

        for (member in klass.members) {
            if (member.hasAnnotation<BackboneEventHandler>()) {
                // include the default "this" parameter
                if (member.parameters.size !in 2..2)
                    throw IllegalArgumentException("Member must have exactly one parameter " +
                            "${member.javaClass.declaringClass?.kotlin?.simpleName}.${member.name}()")

                val priority = member.findAnnotation<BackboneEventHandler>()?.priority?.ordinal ?: 0

                val targetEventId = member.parameters[1].type

                val eventHandlersForEventTarget = eventHandlers[targetEventId] ?: continue

                val handler = eventHandlersForEventTarget.find {
                    it is MemberBackedEventHandler &&
                    it.priority == priority &&
                    it.callable == member &&
                    it.instance == instance
                } ?: continue
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
     * Returns the global callback object from the event, if any.
     * If you need to get a non-global callback, please use map
     * access on the event itself.
     *
     * @param event The event to post.
     * @return The global callback object from the event, or null if no global callback was set.
     * @since 1.0.0
     */
    fun post(event: Event): Any? {
        val eventId = event::class.starProjectedType
        val eventHandlers = eventHandlers[eventId] ?: return null

        for (handler in eventHandlers) {
            callHandler(event, handler)
            if (event is Event.Cancelable && event.canceled) return event.callback
        }

        return event.callback
    }

    private fun callHandler(event: Event, handler: EventHandler) {
        try {
            if (!handler.isAlive) {
                logger.warning("Removing dead event handler.")
                eventHandlers.values.forEach { it.remove(handler) }
                return
            }
            handler.invoke(event)
        } catch (e: InvocationTargetException) {
            logger.severe("Fatal Error during event in handler, removing handler.")
            eventHandlers.values.forEach { it.remove(handler) }
            e.printStackTrace()
        } catch (e: Exception) {
            logger.severe("Exception in lambda event handler, removing handler.")
            eventHandlers.values.forEach { it.remove(handler) }
            e.printStackTrace()
        }
    }
}
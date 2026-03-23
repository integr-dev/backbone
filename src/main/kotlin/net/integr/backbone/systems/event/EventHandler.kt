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

import java.lang.ref.WeakReference
import kotlin.reflect.KCallable

/**
 * Contract for event handlers.
 *
 * @since 1.6.0
 */
interface EventHandler : Comparable<EventHandler> {
    val priority: Int
    val instance: Any?
    val isAlive: Boolean
    fun invoke(event: Event)
}

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
 * @since 1.6.0
 */
class MemberBackedEventHandler(
    override var priority: Int,
    var callable: KCallable<*>,
    instance: Any
) : EventHandler {
    private val instanceRef = WeakReference(instance)

    override val instance: Any?
        get() = instanceRef.get()

    override val isAlive: Boolean
        get() = instanceRef.get() != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MemberBackedEventHandler
        return compareTo(other) == 0
    }

    override fun hashCode(): Int {
        var result = priority
        result = 31 * result + callable.toString().hashCode()
        result = 31 * result + System.identityHashCode(instance)
        return result
    }

    override fun compareTo(other: EventHandler): Int {
        // Sort by priority (lower ordinal first = higher priority)
        val priorityComparison = priority.compareTo(other.priority)
        if (priorityComparison != 0) return priorityComparison
        // Use a stable string for member-backed, hash for lambda, fallback to class name
        val thisStr = callable.toString()
        val otherStr = if (other is MemberBackedEventHandler) other.callable.toString() else (other as? LambdaEventHandler)?.lambda?.hashCode()?.toString() ?: other.javaClass.name
        val strComparison = thisStr.compareTo(otherStr)
        if (strComparison != 0) return strComparison
        // Tiebreaker: instance identity
        return System.identityHashCode(instance).compareTo(System.identityHashCode(other.instance))
    }

    override fun invoke(event: Event) {
        callable.call(instance, event)
    }
}

/**
 * Lambda-based event handler implementation.
 *
 * @param priority the priority of the handler
 * @param lambda the lambda to invoke
 * @param instance the instance to keep a weak reference to (for lifecycle management)
 * @since 1.6.0
 */
class LambdaEventHandler(
    override var priority: Int,
    internal val lambda: (Event) -> Unit,
    instance: Any
) : EventHandler {
    private val instanceRef = WeakReference(instance)

    override val instance: Any?
        get() = instanceRef.get()

    override val isAlive: Boolean
        get() = instanceRef.get() != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LambdaEventHandler
        return priority == other.priority && lambda == other.lambda && instance == other.instance
    }

    override fun hashCode(): Int {
        var result = priority
        result = 31 * result + lambda.hashCode()
        result = 31 * result + System.identityHashCode(instance)
        return result
    }

    override fun compareTo(other: EventHandler): Int {
        // Lower number = higher priority (called earlier)
        val priorityComparison = priority.compareTo(other.priority)
        if (priorityComparison != 0) return priorityComparison
        val thisHash = lambda.hashCode()
        val otherHash = (other as? LambdaEventHandler)?.lambda?.hashCode() ?: other.hashCode()
        val lambdaComparison = thisHash.compareTo(otherHash)
        if (lambdaComparison != 0) return lambdaComparison
        return System.identityHashCode(instance).compareTo(System.identityHashCode(other.instance))
    }

    override fun invoke(event: Event) {
        lambda(event)
    }
}
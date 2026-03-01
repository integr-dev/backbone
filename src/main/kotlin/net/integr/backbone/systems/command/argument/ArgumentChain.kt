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

package net.integr.backbone.systems.command.argument

import org.jetbrains.annotations.ApiStatus

/**
 * Represents a chain of arguments for a command.
 *
 * @param args The list of arguments.
 * @since 1.0.0
 */
@ApiStatus.Internal
class ArgumentChain(args: List<String>) {
    /**
     *
     * Represents a node in the argument chain.
     *
     * @property value The string value of the argument.
     * @property next The next node in the chain, or null if this is the last node.
     * @since 1.0.0
     */
    private class Node(val value: String, var next: Node? = null)

    private var head: Node? = if (args.isNotEmpty()) Node(args[0]) else null

    init {
        var current = head
        for (i in 1 until args.size) {
            val newNode = Node(args[i])
            current?.next = newNode
            current = newNode
        }
    }

    /**
     * Checks if the argument chain is empty.
     *
     * @return True if the argument chain is empty, false otherwise.
     * @since 1.0.0
     */
    fun isEmpty(): Boolean {
        return head == null
    }

    /**
     * Moves the chain to the next argument.
     *
     * @since 1.0.0
     */
    fun moveNext() {
        head = head?.next
    }

    /**
     * Returns the current argument in the chain.
     *
     * @return The current argument, or null if the chain is empty.
     * @since 1.0.0
     */
    fun current(): String? {
        return head?.value
    }

    /**
     * Returns the full string of the remaining arguments in the chain.
     *
     * @return The full string of the remaining arguments.
     * @since 1.0.0
     */
    fun remainingFullString(): String {
        val values = mutableListOf<String>()
        var current = head
        while (current != null) {
            values.add(current.value)
            current = current.next
        }

        return values.joinToString(" ")
    }
}
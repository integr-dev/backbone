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

class ArgumentChain(args: List<String>) {
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

    fun isEmpty(): Boolean {
        return head == null
    }

    fun moveNext() {
        head = head?.next
    }

    fun current(): String? {
        return head?.value
    }

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
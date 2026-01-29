package net.integr.backbone.systems.command.arguments

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
package net.integr.backbone.systems.command.arguments

class ArgChain(val args: List<String>) {
    class Node(val value: String, var next: Node? = null)

    private var head: Node? = if (args.isNotEmpty()) Node(args[0]) else null
    var moveCount = 0

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
        moveCount++
    }

    fun peekNext(): String? {
        return head?.next?.value
    }

    fun current(): String? {
        return head?.value
    }

    fun length(): Int {
        var count = 0
        var current = head
        while (current != null) {
            count++
            current = current.next
        }
        return count
    }

    fun last(): String? {
        var current = head
        var lastValue: String? = null
        while (current != null) {
            lastValue = current.value
            current = current.next
        }
        return lastValue
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
    override fun toString(): String {
        val values = mutableListOf<String>()
        var current = head
        while (current != null) {
            values.add(current.value)
            current = current.next
        }
        return "ArgChain(${values.joinToString(", ")})"
    }
}
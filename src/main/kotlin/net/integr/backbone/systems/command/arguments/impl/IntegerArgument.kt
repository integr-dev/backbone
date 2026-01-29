package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.arguments.Argument

fun integerArgument(name: String, description: String): Argument<Int> {
    return IntegerArgument(name, description)
}

class IntegerArgument(name: String, description: String) : Argument<Int>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return CompletionResult(if (arg.text.isBlank()) mutableListOf("<$name:int>") else mutableListOf(), arg.end)
    }

    override fun parse(current: ArgumentInput): ParseResult<Int> {
        val arg = current.getNextSingle()
        val value = arg.text.toIntOrNull() ?: throw CommandArgumentException("Argument '$name' must be a valid integer.")
        return ParseResult(value, arg.end)
    }
}
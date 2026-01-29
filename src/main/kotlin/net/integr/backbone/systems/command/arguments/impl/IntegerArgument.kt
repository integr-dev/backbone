package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.arguments.Argument

fun Command.integerArgument(name: String, description: String) {
    argument(IntegerArgument(name, description))
}

class IntegerArgument(name: String, description: String) : Argument<Int>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return CompletionResult(if (arg.text.isBlank()) listOf("<$name:int>") else emptyList(), arg.end)
    }

    override fun parse(current: ArgumentInput): ParseResult<Int> {
        val arg = current.getNextSingle()
        val value = arg.text.toIntOrNull() ?: throw CommandArgumentException("Argument '$name' must be a valid integer.")
        return ParseResult(value, arg.end)
    }
}
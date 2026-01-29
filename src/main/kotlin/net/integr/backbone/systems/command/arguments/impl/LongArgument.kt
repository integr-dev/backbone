package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.arguments.Argument

fun Command.longArgument(name: String, description: String) {
    argument(LongArgument(name, description))
}

class LongArgument(name: String, description: String) : Argument<Long>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return CompletionResult(if (arg.text.isBlank()) listOf("<$name:long>") else emptyList(), arg.end)
    }

    override fun parse(current: ArgumentInput): ParseResult<Long> {
        val arg = current.getNextSingle()
        val value = arg.text.toLongOrNull() ?: throw CommandArgumentException("Argument '$name' must be a valid long.")
        return ParseResult(value, arg.end)
    }
}
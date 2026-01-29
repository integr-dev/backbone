package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.arguments.Argument

fun shortArgument(name: String, description: String): Argument<Short> {
    return ShortArgument(name, description)
}

class ShortArgument(name: String, description: String) : Argument<Short>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return CompletionResult(if (arg.text.isBlank()) mutableListOf("<$name:short>") else mutableListOf(), arg.end)
    }

    override fun parse(current: ArgumentInput): ParseResult<Short> {
        val arg = current.getNextSingle()
        val value = arg.text.toShortOrNull() ?: throw CommandArgumentException("Argument '$name' must be a valid short.")
        return ParseResult(value, arg.end)
    }
}
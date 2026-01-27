package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.arguments.Argument

class ShortArgument(name: String, description: String) : Argument<Short>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return CompletionResult(if (arg.text.isBlank()) listOf("<$name:short>") else emptyList(), arg.end)
    }

    override fun parse(current: ArgumentInput): ParseResult<Short> {
        val arg = current.getNextSingle()
        val value = arg.text.toShortOrNull() ?: throw IllegalArgumentException("Argument '$name' must be a valid short.")
        return ParseResult(value, arg.end)
    }
}
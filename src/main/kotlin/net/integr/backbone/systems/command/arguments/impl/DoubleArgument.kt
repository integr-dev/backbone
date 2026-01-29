package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.arguments.Argument

fun doubleArgument(name: String, description: String): Argument<Double> {
    return DoubleArgument(name, description)
}

class DoubleArgument(name: String, description: String) : Argument<Double>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return CompletionResult(if (arg.text.isBlank()) mutableListOf("<$name:double>") else mutableListOf(), arg.end)
    }

    override fun parse(current: ArgumentInput): ParseResult<Double> {
        val arg = current.getNextSingle()
        val value = arg.text.toDoubleOrNull() ?: throw CommandArgumentException("Argument '$name' must be a valid double.")
        return ParseResult(value, arg.end)
    }
}
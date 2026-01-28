package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.command.arguments.Argument

fun Command.doubleArgument(name: String, description: String) {
    argument(DoubleArgument(name, description))
}

class DoubleArgument(name: String, description: String) : Argument<Double>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return CompletionResult(if (arg.text.isBlank()) listOf("<$name:double>") else emptyList(), arg.end)
    }

    override fun parse(current: ArgumentInput): ParseResult<Double> {
        val arg = current.getNextSingle()
        val value = arg.text.toDoubleOrNull() ?: throw IllegalArgumentException("Argument '$name' must be a valid double.")
        return ParseResult(value, arg.end)
    }
}
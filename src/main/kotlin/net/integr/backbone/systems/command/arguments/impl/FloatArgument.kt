package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.arguments.Argument

fun floatArgument(name: String, description: String): Argument<Float> {
    return FloatArgument(name, description)
}

class FloatArgument(name: String, description: String) : Argument<Float>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return CompletionResult(if (arg.text.isBlank()) mutableListOf("<$name:float>") else mutableListOf(), arg.end)
    }

    override fun parse(current: ArgumentInput): ParseResult<Float> {
        val arg = current.getNextSingle()
        val value = arg.text.toFloatOrNull() ?: throw CommandArgumentException("Argument '$name' must be a valid float.")
        return ParseResult(value, arg.end)
    }
}
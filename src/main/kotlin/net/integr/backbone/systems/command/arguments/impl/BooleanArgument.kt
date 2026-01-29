package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.arguments.Argument

fun booleanArgument(name: String, description: String): Argument<Boolean> {
    return BooleanArgument(name, description)
}

class BooleanArgument(name: String, description: String) : Argument<Boolean>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return if (arg.text.isBlank()) {
            CompletionResult(mutableListOf("<$name:bool>", "true", "false"), arg.end)
        } else {
            CompletionResult(mutableListOf("true", "false"), arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<Boolean> {
        val arg = current.getNextSingle()
        val value = arg.text.lowercase().toBooleanStrictOrNull() ?: throw CommandArgumentException("Argument '$name' must be a valid boolean.")
        return ParseResult(value, arg.end)
    }
}
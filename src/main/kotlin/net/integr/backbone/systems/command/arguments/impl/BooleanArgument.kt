package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.arguments.Argument

class BooleanArgument(name: String, description: String) : Argument<Boolean>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return if (arg.text.isBlank()) {
            CompletionResult(listOf("<$name:bool>", "true", "false"), arg.end)
        } else {
            CompletionResult(listOf("true", "false"), arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<Boolean> {
        val arg = current.getNextSingle()
        val bool = arg.text.lowercase().toBooleanStrictOrNull() ?: throw IllegalArgumentException("Argument '$name' must be a valid boolean.")
        return ParseResult(bool, arg.end)
    }
}
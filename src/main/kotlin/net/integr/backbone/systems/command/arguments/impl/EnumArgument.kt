package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.Command
import net.integr.backbone.systems.command.arguments.Argument

inline fun <reified T : Enum<T>> Command.enumArgument(name: String, description: String) {
    argument(EnumArgument(name, description, T::class.java))
}

class EnumArgument<T : Enum<T>>(name: String, description: String, val type: Class<T>) : Argument<T>(name, description) {
    private val values = type.enumConstants.map { it.name }

    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return if (arg.text.isBlank()) {
            CompletionResult(listOf("<$name:bool>", *values.toTypedArray()), arg.end)
        } else {
            CompletionResult(values, arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<T> {
        val arg = current.getNextSingle()
        val value = type.enumConstants.firstOrNull { it.name.equals(arg.text, true) }
            ?: throw IllegalArgumentException("Argument '$name' must be one of: ${values.joinToString(", ")}.")

        return ParseResult(value, arg.end)
    }
}
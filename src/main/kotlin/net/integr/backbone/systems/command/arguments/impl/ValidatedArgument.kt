package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.arguments.Argument

fun <T : Any> validatedArgument(argument: Argument<T>, block: (T) -> ValidatedArgument.ValidationResult): Argument<T> {
    return ValidatedArgument(argument, block)
}

class ValidatedArgument<T : Any>(val argument: Argument<T>, val block: (T) -> ValidationResult) : Argument<T>(argument.name, argument.description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        return argument.getCompletions(current)
    }

    override fun parse(current: ArgumentInput): ParseResult<T> {
        val result = argument.parse(current)
        val validationResult = block(result.value)
        if (!validationResult.ok) throw CommandArgumentException("Argument '$name' is invalid: ${validationResult.message}")
        return result
    }

    data class ValidationResult(val ok: Boolean, val message: String) {
        companion object {
            fun ok(): ValidationResult = ValidationResult(true, "")
            fun fail(message: String): ValidationResult = ValidationResult(false, message)
        }
    }
}
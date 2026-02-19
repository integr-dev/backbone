/*
 * Copyright © 2026 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.backbone.commands.arguments

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.argument.Argument

fun <T : Any> validatedArgument(argument: Argument<T>, block: ValidatedArgument.ValidationResult.Companion.(T) -> ValidatedArgument.ValidationResult): Argument<T> {
    return ValidatedArgument(argument, block)
}

class ValidatedArgument<T : Any>(val argument: Argument<T>, val block: ValidationResult.Companion.(T) -> ValidationResult) : Argument<T>(argument.name, argument.description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        return argument.getCompletions(current)
    }

    override fun parse(current: ArgumentInput): ParseResult<T> {
        val result = argument.parse(current)
        val validationResult = ValidationResult.Companion.block(result.value)
        if (!validationResult.ok) throw CommandArgumentException("Argument '$name' ${validationResult.message}")
        return result
    }

    data class ValidationResult(val ok: Boolean, val message: String) {
        companion object {
            fun ok(): ValidationResult = ValidationResult(true, "")
            fun fail(message: String): ValidationResult = ValidationResult(false, message)
        }
    }
}
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

/**
 * A command argument that parses a boolean value.
 *
 * This argument accepts "true" or "false" (case-insensitive) as valid inputs.
 * It provides completions for "true" and "false" when the argument is being typed.
 *
 * @param name The name of the argument.
 * @param description A brief description of the argument's purpose.
 * @since 1.0.0
 */
fun booleanArgument(name: String, description: String): Argument<Boolean> {
    return BooleanArgument(name, description)
}

/**
 * A command argument that parses a boolean value.
 *
 * This argument accepts "true" or "false" (case-insensitive) as valid inputs.
 * It provides completions for "true" and "false" when the argument is being typed.
 *
 * @param name The name of the argument.
 * @param description A brief description of the argument's purpose.
 * @since 1.0.0
 */
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
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
 * A command argument that parses an enum value.
 *
 * This argument accepts any valid enum constant name (case-insensitive) as input.
 * It provides completions for all available enum constants.
 *
 * @param name The name of the argument.
 * @param description A brief description of the argument's purpose.
 * @param T The class of the enum.
 * @since 1.0.0
 */
inline fun <reified T : Enum<T>> enumArgument(name: String, description: String): Argument<T> {
    return EnumArgument(name, description, T::class.java)
}

/**
 * A command argument that parses an enum value.
 *
 * This argument accepts any valid enum constant name (case-insensitive) as input.
 * It provides completions for all available enum constants.
 *
 * @param name The name of the argument.
 * @param description A brief description of the argument's purpose.
 * @param type The class of the enum.
 * @since 1.0.0
 */
class EnumArgument<T : Enum<T>>(name: String, description: String, val type: Class<T>) : Argument<T>(name, description) {
    private val values = type.enumConstants.map { it.name }.toMutableList()

    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return if (arg.text.isBlank()) {
            CompletionResult(mutableListOf("<$name:bool>", *values.toTypedArray()), arg.end)
        } else {
            CompletionResult(values, arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<T> {
        val arg = current.getNextSingle()
        val value = type.enumConstants.firstOrNull { it.name.equals(arg.text, true) }
            ?: throw CommandArgumentException("Argument '$name' must be one of: ${values.joinToString(", ")}.")

        return ParseResult(value, arg.end)
    }
}
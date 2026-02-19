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

fun shortArgument(name: String, description: String): Argument<Short> {
    return ShortArgument(name, description)
}

class ShortArgument(name: String, description: String) : Argument<Short>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()

        return CompletionResult(if (arg.text.isBlank()) mutableListOf("<$name:short>") else mutableListOf(), arg.end)
    }

    override fun parse(current: ArgumentInput): ParseResult<Short> {
        val arg = current.getNextSingle()
        val value = arg.text.toShortOrNull() ?: throw CommandArgumentException("Argument '$name' must be a valid short.")
        return ParseResult(value, arg.end)
    }
}
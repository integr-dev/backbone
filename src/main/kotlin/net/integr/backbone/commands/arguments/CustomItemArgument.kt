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
import net.integr.backbone.systems.hotloader.ScriptStore
import net.integr.backbone.systems.item.ItemHandler

fun customItemArgument(name: String, description: String): Argument<String> {
    return CustomItemArgument(name, description)
}

class CustomItemArgument(name: String, description: String) : Argument<String>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val items = ItemHandler.items.keys
        val isQuoted = current.value.startsWith("\"")

        val arg = if (isQuoted) current.getNextGreedyWithBoundChar('"') else current.getNextSingle()

        val hasClosingQuote = isQuoted && arg.found

        val itemMap = items.map { it + "\"" }.toMutableList()

        return if (isQuoted && !hasClosingQuote) {
            CompletionResult(itemMap, arg.end)
        } else {
            CompletionResult(if (arg.text.isBlank()) mutableListOf("<$name:item>", *items.toTypedArray()) else items.toMutableList(), arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<String> {
        val items = ItemHandler.items.keys
        val isQuoted = current.value.startsWith("\"")
        val arg = if (isQuoted) current.getNextGreedyWithBoundChar('"') else current.getNextSingle()

        val text = if (isQuoted) {
            if (!arg.found) throw CommandArgumentException("Argument '$name' is missing a closing quotation mark.")
            arg.text.substring(1, arg.text.length - 1)
        } else {
            arg.text
        }

        if (!items.contains(text)) throw CommandArgumentException("Argument '$name' is not a valid item.")

        return ParseResult(text, arg.end)
    }
}
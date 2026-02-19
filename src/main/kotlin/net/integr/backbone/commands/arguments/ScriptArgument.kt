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

fun scriptArgument(name: String, description: String): Argument<String> {
    return ScriptArgument(name, description)
}

class ScriptArgument(name: String, description: String) : Argument<String>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val scripts = ScriptStore.getScriptNames()
        val isQuoted = current.value.startsWith("\"")

        val arg = if (isQuoted) current.getNextGreedyWithBoundChar('"') else current.getNextSingle()

        val hasClosingQuote = isQuoted && arg.found

        val scrMap = scripts.map { it + "\"" }.toMutableList()

        return if (isQuoted && !hasClosingQuote) {
            CompletionResult(scrMap, arg.end)
        } else {
            CompletionResult(if (arg.text.isBlank()) mutableListOf("<$name:script>", *scripts.toTypedArray()) else scripts.toMutableList(), arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<String> {
        val scripts = ScriptStore.getScriptNames()
        val isQuoted = current.value.startsWith("\"")
        val arg = if (isQuoted) current.getNextGreedyWithBoundChar('"') else current.getNextSingle()

        val text = if (isQuoted) {
            if (!arg.found) throw CommandArgumentException("Argument '$name' is missing a closing quotation mark.")
            arg.text.substring(1, arg.text.length - 1)
        } else {
            arg.text
        }

        if (!scripts.contains(text)) throw CommandArgumentException("Argument '$name' is not a valid script.")

        return ParseResult(text, arg.end)
    }
}
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

package net.integr.backbone.systems.command.argument

import net.integr.backbone.systems.command.CommandFailedException

abstract class Argument<T : Any>(val name: String, val description: String) {
    fun fail(message: String) {
        throw CommandFailedException(message)
    }

    abstract fun getCompletions(current: ArgumentInput): CompletionResult

    abstract fun parse(current: ArgumentInput): ParseResult<T>

    class CompletionResult(val completions: MutableList<String>, val end: Int)

    class ParseResult<T : Any>(val value: T, val end: Int)

    class ArgumentInput(val value: String) {
        fun getNextSingle(): SubInputWithResult {
            val str = value.substringBefore(' ')
            return SubInputWithResult(str, str.length, true)
        }

        fun getNextToToken(token: Char): SubInputWithResult {
            val str = value.substringBefore(token)
            return SubInputWithResult(str, str.length, str.length == value.length)
        }

        fun getNextGreedyWithBoundChar(token: Char): SubInputWithResult {
            val str = value.substring(1).substringBefore(token)
            val hasResult = str.length + 2 <= value.length
            val actString = token + str + if (hasResult) token else ""
            return SubInputWithResult(actString, actString.length, hasResult)
        }

        data class SubInputWithResult(val text: String, val end: Int, val found: Boolean) {
            fun getLastSplitBySpace(): String {
                return text.substringAfterLast(' ')
            }
        }
    }
}
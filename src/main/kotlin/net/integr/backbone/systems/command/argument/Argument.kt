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

import net.integr.backbone.systems.command.help.HelpNode

/**
 * Represents a command argument.
 *
 * @param name The name of the argument.
 * @param description A brief description of the argument.
 * @since 1.0.0
 */
abstract class Argument<T : Any>(val name: String, val description: String) {
    /**
     * Called when the argument needs to provide tab completions.
     *
     * @param current The current argument input.
     * @return A [CompletionResult] containing possible completions and the index where the completion ends.
     * @since 1.0.0
     */
    abstract fun getCompletions(current: ArgumentInput): CompletionResult

    /**
     *
     * Called when the argument needs to be parsed.
     * If the argument can not be parsed, throw a new
     * [net.integr.backbone.systems.command.CommandArgumentException].
     *
     * @param current The current argument input.
     * @return A [ParseResult] containing the parsed value and the index where the parsing ends.
     * @since 1.0.0
     */
    abstract fun parse(current: ArgumentInput): ParseResult<T>

    /**
     * Returns a string representation of the argument's help text.
     * @return A string containing the argument's name and description.
     *
     * @since 1.3.0
     */
    fun getHelpText(): HelpNode.Content {
        return HelpNode.Content("  $name - $description", HelpNode.Content.Type.LIST)
    }

    /**
     * Represents a finished completion.
     *
     * @property completions A mutable list of possible completions.
     * @property end The index in the input string where the completion ends.
     * @since 1.0.0
     */
    data class CompletionResult(val completions: MutableList<String>, val end: Int)

    /**
     *
     * Represents a finished parse.
     *
     * @property value The parsed value.
     * @property end The index in the input string where the parsing ends.
     * @since 1.0.0
     */
    data class ParseResult<T : Any>(val value: T, val end: Int)

    /**
     * Represents the input for an argument.
     *
     * @property value The raw string value of the input.
     * @since 1.0.0
     */
    class ArgumentInput(val value: String) {
        /**
         * Read the next single word from the input.
         *
         * @return A [SubInputWithResult] containing the next single word, its end index, and whether it was found.
         * @since 1.0.0
         */
        fun getNextSingle(): SubInputWithResult {
            val str = value.substringBefore(' ')
            return SubInputWithResult(str, str.length, true)
        }

        /**
         * Read the next part of the input greedily until a specific bound character is encountered.
         *
         * @param token The bound character to search for.
         * @return A [SubInputWithResult] containing the extracted string (including the bound characters), its end index, and whether both bound characters were found.
         * @since 1.0.0
         */
        fun getNextGreedyWithBoundChar(token: Char): SubInputWithResult {
            val str = value.substring(1).substringBefore(token)
            val hasResult = str.length + 2 <= value.length
            val actString = token + str + if (hasResult) token else ""
            return SubInputWithResult(actString, actString.length, hasResult)
        }

        /**
         * Represents a part of an input that was extracted from the raw text.
         *
         * @property text The extracted text.
         * @property end The index in the input string where the extraction ends.
         * @property found Whether the search was successful.
         * @since 1.0.0
         */
        data class SubInputWithResult(val text: String, val end: Int, val found: Boolean) {
            /**
             * Read the last part of the extracted text after the last space.
             *
             * @return The last part of the extracted text after the last space.
             * @since 1.0.0
             */
            fun getLastSplitBySpace(): String {
                return text.substringAfterLast(' ')
            }
        }
    }
}
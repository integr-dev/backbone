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

import net.integr.backbone.systems.command.argument.Argument

fun <T : Any> completedArgument(argument: Argument<T>, options: List<String>): Argument<T> {
    return CompletedArgument(argument, options)
}

class CompletedArgument<T : Any>(val argument: Argument<T>, val options: List<String>) : Argument<T>(argument.name, argument.description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val res = argument.getCompletions(current)
        res.completions += options
        return res
    }

    override fun parse(current: ArgumentInput): ParseResult<T> {
        return argument.parse(current)
    }
}
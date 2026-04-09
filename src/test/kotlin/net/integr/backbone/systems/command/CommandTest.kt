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

package net.integr.backbone.systems.command

import kotlinx.coroutines.runBlocking
import net.integr.backbone.systems.command.argument.Argument
import net.integr.backbone.systems.command.argument.ArgumentChain
import net.integr.backbone.commands.arguments.StringArgument
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import kotlin.test.assertContains

private typealias ArgumentInput = Argument.ArgumentInput
private typealias CompletionResult = Argument.CompletionResult
private typealias ParseResult<T> = Argument.ParseResult<T>

class CommandTest {
    // Mock class for testing
    class TestCommand(name: String, description: String) : Command(name, description) {
        var onBuildCalled = false
        var execCalled = false
        lateinit var lastExecution: Execution

        override fun onBuild() {
            onBuildCalled = true
        }

        override suspend fun exec(ctx: Execution) {
            execCalled = true
            lastExecution = ctx
        }
    }

    @Test
    fun testBuild() {
        val command = TestCommand("test", "Test command")
        val subCommand = TestCommand("sub", "Sub command")
        command.subCommands(subCommand)

        command.build()

        assertTrue(command.onBuildCalled)
        assertTrue(subCommand.onBuildCalled)
    }


    @Test
    fun testHandleExecution() {
        val command = TestCommand("test", "Test command")
        val subCommand = TestCommand("sub", "Sub command")
        command.subCommands(subCommand)
        command.build()

        val argChain = ArgumentChain(listOf("sub", "arg1", "arg2"))
        val sender = mock(CommandSender::class.java)

        runBlocking {
            command.handleExecution(sender, argChain)
        }

        assertTrue(subCommand.execCalled)
    }

    @Test
    fun testHandleCompletion() {
        val command = TestCommand("test", "Test command")
        command.arguments(StringArgument("arg1", "arg1"))
        command.build()

        val sender = mock(CommandSender::class.java)
        val argChain = ArgumentChain(listOf("\"hello"))

        val completions = command.handleCompletion(sender, argChain)

        assertContains(completions, "\"hello\"")
    }

    @Test
    fun testHandleCompletionDoesNotDescendIntoSubcommandWithoutSpace() {
        val command = TestCommand("test", "Test command")
        val subCommand = TestCommand("sub", "Sub command")
        subCommand.arguments(object : Argument<String>("arg1", "Arg 1") {
            override fun getCompletions(current: ArgumentInput): CompletionResult {
                return CompletionResult(mutableListOf("next-arg"), current.value.length)
            }

            override fun parse(current: ArgumentInput): ParseResult<String> {
                return ParseResult("value", current.value.length)
            }
        })
        command.subCommands(subCommand)
        command.build()

        val sender = mock(CommandSender::class.java)

        val completions = command.handleCompletion(sender, ArgumentChain(listOf("sub")))

        assertContains(completions, "sub")
        assertFalse(completions.contains("next-arg"))
    }

    @Test
    fun testHandleCompletionDescendsIntoSubcommandAfterSpace() {
        val command = TestCommand("test", "Test command")
        val subCommand = TestCommand("sub", "Sub command")
        subCommand.arguments(object : Argument<String>("arg1", "Arg 1") {
            override fun getCompletions(current: ArgumentInput): CompletionResult {
                return CompletionResult(mutableListOf("next-arg"), current.value.length)
            }

            override fun parse(current: ArgumentInput): ParseResult<String> {
                return ParseResult("value", current.value.length)
            }
        })
        command.subCommands(subCommand)
        command.build()

        val sender = mock(CommandSender::class.java)

        val completions = command.handleCompletion(sender, ArgumentChain(listOf("sub", "")))

        assertContains(completions, "next-arg")
        assertFalse(completions.contains("sub"))
    }

    @Test
    fun testParseArgs() {
        val command = TestCommand("test", "Test command")
        command.arguments(StringArgument("arg1", "arg1"))
        command.build()

        val argChain = ArgumentChain(listOf("hello"))

        val args = command.parseArgs(argChain)

        assertEquals(args["arg1"], "hello")
    }
}
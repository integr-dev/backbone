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
import net.integr.backbone.systems.command.argument.ArgumentChain
import net.integr.backbone.commands.arguments.StringArgument
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import kotlin.test.assertContains

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

        val sender = mock(CommandSender::class.java)
        val argChain = ArgumentChain(listOf("sub", "arg1", "arg2"))

        runBlocking {
            command.handleExecution(sender, argChain)
        }

        assertTrue(subCommand.execCalled)
        assertEquals(subCommand.lastExecution.sender, sender)
    }

    @Test
    fun testHandleCompletion() {
        val command = TestCommand("test", "Test command")
        command.arguments(StringArgument("arg1", "arg1"))
        command.build()

        val argChain = ArgumentChain(listOf("\"hello"))

        val completions = command.handleCompletion(argChain)

        assertContains(completions, "\"hello\"")
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
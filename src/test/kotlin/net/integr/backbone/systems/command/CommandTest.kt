package net.integr.backbone.systems.command

import net.integr.backbone.systems.command.argument.ArgumentChain
import net.integr.backbone.commands.arguments.StringArgument
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.mock
import kotlin.test.Test
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

        override fun exec(ctx: Execution) {
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

        command.handleExecution(sender, argChain)

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
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

import kotlinx.coroutines.launch
import net.integr.backbone.Backbone
import net.integr.backbone.systems.command.argument.ArgumentChain
import net.integr.backbone.systems.command.argument.Argument
import net.integr.backbone.text.formats.CommandFeedbackFormat
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand

abstract class Command(name: String, description: String, aliases: List<String> = listOf<String>(), val format: CommandFeedbackFormat = CommandHandler.defaultFeedbackFormat) : BukkitCommand(name, description, "See backbone help", aliases) {
    protected val logger = Backbone.LOGGER.derive("command")

    private val subCommands = mutableListOf<Command>()
    private val arguments = mutableListOf<Argument<*>>()

    private var subCommandNames: List<String> = listOf()

    fun subCommands(vararg commands: Command) {
        commands.forEach {
            it.build()
        }

        subCommands.addAll(commands)
    }

    fun build() {
        onBuild()
        subCommandNames = subCommands.map { it.name } + subCommands.flatMap { it.aliases }
    }

    fun arguments(vararg arguments: Argument<*>) {
        this.arguments.addAll(arguments)
    }

    suspend fun handleExecution(sender: CommandSender?, argChain: ArgumentChain) {
        val curr = argChain.current()
        val subcommand = subCommands.find {
            it.name.equals(curr, ignoreCase = true) ||
            it.aliases.any { alias -> alias.equals(curr, ignoreCase = true) }
        }

        if (subcommand != null) {
            argChain.moveNext()
            subcommand.handleExecution(sender, argChain)
        } else {
            // No matching subcommand found, move on to parse args for this command
            val args = parseArgs(argChain)
            val ctx = Execution(sender, args, format)

            // Async
            exec(ctx)

            return
        }
    }

    fun handleCompletion(argChain: ArgumentChain): List<String> {
        val curr = argChain.current()
        val subcommand = subCommands.find {
            it.name.equals(curr, ignoreCase = true) ||
            it.aliases.any { alias -> alias.equals(curr, ignoreCase = true) }
        }

        if (subcommand != null) {
            argChain.moveNext()
            return subcommand.handleCompletion(argChain)
        } else {
            // No matching subcommand found, move on to provide completions for this command
            val possibleSubCommands = subCommandNames.filter { it.startsWith(curr ?: "", ignoreCase = true) }.toMutableList()
            var argumentString = argChain.remainingFullString()

            for (argument in arguments) {
                val input = Argument.ArgumentInput(argumentString)
                val completionResult = argument.getCompletions(input)

                if (completionResult.end == argumentString.length) {
                    // Argument not filled yet, return current completions
                    return possibleSubCommands + completionResult.completions
                }

                argumentString = argumentString.substring(completionResult.end).trimStart()
            }

            return possibleSubCommands
        }
    }

    fun parseArgs(argChain: ArgumentChain): Map<String, Any> { // Any - we just want the values of the args here, casting happens later
        val parsedArgs = mutableMapOf<String, Any>()
        var argumentString = argChain.remainingFullString()

        for (argument in arguments) {
            val input = Argument.ArgumentInput(argumentString)
            val argValue = argument.parse(input)

            parsedArgs[argument.name] = argValue.value
            argumentString = argumentString.substring(argValue.end).trimStart()
        }

        return parsedArgs
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        val chain = ArgumentChain(args.toList())

        CommandHandler.coroutineScope.launch {
            try {
                handleExecution(sender, chain)
            } catch (e: CommandFailedException) {
                // Command has been failed manually
                logger.warning("Execution '$name' by ${sender.name} failed: ${e.message} (${e.javaClass.simpleName})")
                sender.sendMessage(format.formatErr(e.message ?: "An error occurred while executing the command."))
            } catch (e: CommandArgumentException) {
                // User has provided invalid argument
                logger.warning("Execution '$name' by ${sender.name} failed with argument error: ${e.message} (${e.javaClass.simpleName})")
                sender.sendMessage(format.formatErr(e.message ?: "An error occurred while executing the command."))
            } catch (e: Exception) {
                // Unexpected error such as database failure
                logger.severe("Execution '$name' by ${sender.name} failed irregularly: ${e.message} (${e.javaClass.simpleName})")
                sender.sendMessage(format.formatErr("An error occurred while executing the command. Please contact administration."))
                e.printStackTrace()
            }
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        return handleCompletion(ArgumentChain(args.toList()))
    }

    protected open fun onBuild() {}
    protected abstract suspend fun exec(ctx: Execution)
}
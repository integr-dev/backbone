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
import net.integr.backbone.systems.command.help.HelpNode
import net.integr.backbone.systems.permission.PermissionNode
import net.integr.backbone.text.formats.CommandFeedbackFormat
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.jetbrains.annotations.ApiStatus

/**
 * Represents a command that can be executed by a CommandSender.
 * This class extends Bukkit's Command class and provides additional functionality for handling subcommands, arguments, and execution context.
 *
 * @param name The name of the command.
 * @param description A brief description of the command.
 * @param aliases A list of alternative names for the command.
 * @param format The CommandFeedbackFormat to use for sending messages back to the sender.
 * @since 1.0.0
 */
abstract class Command(name: String, description: String, aliases: List<String> = listOf<String>(), val format: CommandFeedbackFormat = CommandHandler.defaultFeedbackFormat) : BukkitCommand(name, description, "See backbone help", aliases) {
    private val logger = Backbone.LOGGER.derive("command")

    /**
     * The list of sub-commands registered for this command.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    val subCommands = mutableListOf<Command>()

    /**
     * The list of arguments registered for this command.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    private val arguments = mutableListOf<Argument<*>>()

    private var subCommandNames: Map<String, Command> = mapOf()

    /**
     * The parent command of this command, if any.
     * @since 1.3.0
     */
    var parent: Command? = null
        private set

    /**
     * The full name of this command, including any parent commands.
     * @since 1.3.0
     */
    var fullName: String? = null
        private set

    /**
     * The help node for this command.
     *
     * Help nodes are used to display the structure of the command tree.
     *
     * @since 1.3.0
     */
    var helpNode: HelpNode? = null
        private set

    /**
     * The set of permissions required to execute this command.
     *
     * @since 1.8.1
     */
    private val requiredPermissions: MutableSet<PermissionNode> = mutableSetOf()

    /**
     * The set of permissions that would prevent a sender from executing this command if they have any of them.
     *
     * @since 1.8.1
     */
    private val excludedPermissions: MutableSet<PermissionNode> = mutableSetOf()

    /**
     * Registers one or more sub-commands to this command.
     *
     * @param commands The sub-commands to register.
     * @since 1.0.0
     */
    fun subCommands(vararg commands: Command) {
        commands.forEach {
            it.parent = this
            it.build()
        }

        subCommands.addAll(commands)
    }

    /**
     * Registers one or more arguments to this command.
     *
     * @param arguments The arguments to register.
     * @since 1.0.0
     */
    fun arguments(vararg arguments: Argument<*>) {
        this.arguments.addAll(arguments)
    }

    /**
     * Registers one or more required permissions for this command.
     *
     * @param permissions The permissions to register.
     * @since 1.8.1
     */
    fun requirePermissions(vararg permissions: PermissionNode) {
        requiredPermissions.addAll(permissions)
    }

    /**
     * Registers one or more excluded permissions for this command.
     * If a sender has any of the excluded permissions, they will not be able to execute the command, even if they have the required permissions.
     *
     * @param permissions The permissions to exclude.
     * @since 1.8.1
     */
    fun excludePermissions(vararg permissions: PermissionNode) {
        excludedPermissions.addAll(permissions)
    }

    /**
     * Checks if the sender has the necessary permissions to execute this command.
     *
     * @param sender The CommandSender to check permissions for.
     * @return True if the sender can execute the command, false otherwise.
     * @since 1.8.1
     */
    private fun canExecute(sender: CommandSender): Boolean {
        return requiredPermissions.all { sender.hasPermission(it.id) } && excludedPermissions.none { sender.hasPermission(it.id) }
    }

    /**
     * Computes the help node for this command.
     *
     * This method recursively builds the help node for all sub-commands and their arguments.
     * It is used by the help command to display the command structure.
     *
     * @since 1.3.0
     */
    private fun computeHelpNode() {
        val subCommandNodes = subCommands.map { it.helpNode ?: throw IllegalStateException("Subcommand ${it.name} has not been built yet.") }

        val contents = mutableListOf<HelpNode.Content>()

        contents.add(HelpNode.Content(description, HelpNode.Content.Type.TEXT))

        if (arguments.isNotEmpty()) contents.add(HelpNode.Content("Arguments", HelpNode.Content.Type.TITLE))
        arguments.forEach { contents.add(it.getHelpText()) }

        if (aliases.isNotEmpty()) contents.add(HelpNode.Content("Aliases", HelpNode.Content.Type.TITLE))
        aliases.forEach { contents.add(HelpNode.Content(it, HelpNode.Content.Type.LIST)) }

        helpNode = HelpNode(name, contents, subCommandNodes)
    }

    /**
     * Builds the command and its sub-commands.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun build() {
        fullName = if (parent == null) name else "${parent!!.fullName}.$name"

        onBuild()
        computeHelpNode()

        subCommandNames = subCommands.associateBy { it.name } + subCommands.flatMap { it.aliases.map { alias -> alias to it } }
    }

    /**
     *
     * Handles the execution of the command based on the provided argument chain.
     * This method recursively searches for subcommands and executes the appropriate command.
     *
     * @param sender The CommandSender who executed the command.
     * @param argChain The ArgumentChain containing the command arguments.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    suspend fun handleExecution(sender: CommandSender, argChain: ArgumentChain) {
        val curr = argChain.current()
        val subcommand = subCommands.find {
            it.name.equals(curr, ignoreCase = true) ||
            it.aliases.any { alias -> alias.equals(curr, ignoreCase = true) }
        }

        if (subcommand != null) {
            argChain.moveNext()
            subcommand.handleExecution(sender, argChain)
        } else {
            if (!canExecute(sender)) throw CommandFailedException("You are not allowed to execute this command.")
            // No matching subcommand found, move on to parse args for this command
            val args = parseArgs(argChain)
            val ctx = Execution(sender, args, format)

            // Async
            exec(ctx)

            return
        }
    }

    /**
     * Handles completion based on the provided argument chain.
     * This method recursively searches for subcommands and provides completions for the appropriate command or its arguments.
     *
     * @param sender The CommandSender who is requesting tab completion.
     * @param argChain The ArgumentChain containing the command arguments.
     * @return A list of possible completions.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun handleCompletion(sender: CommandSender, argChain: ArgumentChain): List<String> {
        val curr = argChain.current()
        val remaining = argChain.remainingFullString()
        val subcommand = subCommands.find {
            it.name.equals(curr, ignoreCase = true) ||
            it.aliases.any { alias -> alias.equals(curr, ignoreCase = true) }
        }

        // Only descend when the current token has been completed and the cursor
        // moved to a next token (typically after typing a space).
        if (subcommand != null && argChain.hasNext()) {
            argChain.moveNext()
            return subcommand.handleCompletion(sender, argChain)
        }

        if (!canExecute(sender)) return emptyList()

        // No matching subcommand found, move on to provide completions for this command
        val possibleSubCommands = subCommandNames.filter { it.value.canExecute(sender) && it.key.startsWith(curr ?: "", ignoreCase = true) }.keys.toList()
        var argumentString = remaining

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

    /**
     *
     * Parses the arguments from the provided argument chain.
     *
     * @param argChain The ArgumentChain containing the command arguments.
     * @return A map of argument names to their parsed values.
     * @since 1.0.0
     */
    @ApiStatus.Internal
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

    /**
     * Called by Bukkit.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        val chain = ArgumentChain(args.toList())

        CommandHandler.coroutineScope.launch {
            try {
                handleExecution(sender, chain)
            } catch (e: CommandFailedException) {
                // Command has been failed manually
                logger.warning("Execution '$name' by ${sender.name} failed: ${e.message} (${e.javaClass.simpleName})")
                sender.sendMessage(format.formatError(e.message ?: "An error occurred while executing the command."))
            } catch (e: CommandArgumentException) {
                // User has provided invalid argument
                logger.warning("Execution '$name' by ${sender.name} failed with argument error: ${e.message} (${e.javaClass.simpleName})")
                sender.sendMessage(format.formatError(e.message ?: "An error occurred while executing the command."))
            } catch (e: Exception) {
                // Unexpected error such as database failure
                logger.severe("Execution '$name' by ${sender.name} failed irregularly: ${e.message} (${e.javaClass.simpleName})")
                sender.sendMessage(format.formatError("An error occurred while executing the command. Please contact administration."))
                e.printStackTrace()
            }
        }

        return true
    }

    /**
     * Called by Bukkit.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        return handleCompletion(sender, ArgumentChain(args.toList()))
    }

    /**
     * Called when the command is being built.
     * Override this method to perform any setup or initialization for the command, such as registering subcommands or arguments.
     * @since 1.0.0
     */
    protected open fun onBuild() {}

    /**
     * Called when the command is executed. Override this method to define the command's behavior.
     *
     * @param ctx The execution context containing information about the sender and arguments.
     * @since 1.0.0
     */
    protected abstract suspend fun exec(ctx: Execution)
}
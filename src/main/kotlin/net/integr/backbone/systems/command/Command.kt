package net.integr.backbone.systems.command

import net.integr.backbone.Backbone
import net.integr.backbone.systems.command.arguments.ArgumentChain
import net.integr.backbone.systems.command.arguments.Argument
import net.integr.backbone.systems.text.format.impl.CommandFeedbackFormat
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand

abstract class Command(name: String, description: String, aliases: List<String> = listOf<String>(), val format: CommandFeedbackFormat = CommandHandler.feedbackFormat) : BukkitCommand(name, description, "See backbone help", aliases) {
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

    fun arguments(vararg argument: Argument<*>) {
        arguments.addAll(argument)
    }

    fun handleExecution(sender: CommandSender, argChain: ArgumentChain) {
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

        try {
            handleExecution(sender, chain)
        } catch (e: CommandFailedException) {
            // Command has been failed manually
            logger.warning("Execution '$name' by ${sender.name} failed: ${e.message} (${e.javaClass.simpleName})")
            sender.sendMessage(format.format(e.message ?: "An error occurred while executing the command."))
            return false
        } catch (e: CommandArgumentException) {
            // User has provided invalid arguments
            logger.warning("Execution '$name' by ${sender.name} failed with argument error: ${e.message} (${e.javaClass.simpleName})")
            sender.sendMessage(format.format(e.message ?: "An error occurred while executing the command."))
            return false
        } catch (e: Exception) {
            // Unexpected error such as database failure
            logger.severe("Execution '$name' by ${sender.name} failed irregularly: ${e.message} (${e.javaClass.simpleName})")
            sender.sendMessage(format.format("An error occurred while executing the command. Please contact administration."))
            e.printStackTrace()
            return false
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        return handleCompletion(ArgumentChain(args.toList()))
    }

    protected abstract fun onBuild()
    protected abstract fun exec(ctx: Execution)
}
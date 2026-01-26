package net.integr.backbone.systems.command

import net.integr.backbone.Backbone
import net.integr.backbone.systems.command.arguments.ArgChain
import net.integr.backbone.systems.command.arguments.Argument
import net.integr.backbone.systems.text.format.impl.CommandFeedbackFormat
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand

abstract class Command(name: String, description: String, aliases: List<String> = listOf<String>(), val format: CommandFeedbackFormat = CommandHandler.feedbackFormat) : BukkitCommand(name, description, "See backbone help", aliases) {
    val logger = Backbone.LOGGER.derive("command")

    private val subCommands = mutableListOf<Command>()
    private val arguments = mutableListOf<Argument<*>>()

    private var subCommandNames: List<String> = listOf()

    fun sub(vararg commands: Command) {
        commands.forEach {
            it.build()
        }

        subCommands.addAll(commands)
    }

    fun build() {
        onBuild()
        subCommandNames = subCommands.map { it.name } + subCommands.flatMap { it.aliases }
    }

    fun argument(argument: Argument<*>) {
        arguments.add(argument)
    }

    fun handleExecution(sender: CommandSender, argChain: ArgChain) {
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

    fun handleCompletion(sender: CommandSender, argChain: ArgChain): List<String> {
        val curr = argChain.current()
        val subcommand = subCommands.find {
            it.name.equals(curr, ignoreCase = true) ||
            it.aliases.any { alias -> alias.equals(curr, ignoreCase = true) }
        }

        if (subcommand != null) {
            argChain.moveNext()
            return subcommand.handleCompletion(sender, argChain)
        } else {
            // No matching subcommand found, move on to provide completions for this command
            val possibleSubCommands = subCommandNames.filter { it.startsWith(curr ?: "", ignoreCase = true) }.toMutableList()
            for (argument in arguments) {
                val completionResult = argument.getCompletions(argChain)

                if (completionResult.needsMoreInput) {
                    possibleSubCommands.addAll(completionResult.completions)
                    return possibleSubCommands
                }

                argChain.moveNext()
            }

            return possibleSubCommands
        }
    }

    fun parseArgs(argChain: ArgChain): Map<String, Any> { // Any - we just want the values of the args here, casting happens later
        val parsedArgs = mutableMapOf<String, Any>()

        for (argument in arguments) {
            val argValue = argument.parse(argChain)
            argChain.moveNext()

            parsedArgs[argument.name] = argValue
        }

        return parsedArgs
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        val chain = ArgChain(args.toList())
        try {
            handleExecution(sender, chain)
        } catch (e: Exception) {
            logger.warning("Execution '$name' by ${sender.name} failed: ${e.message} (${e.javaClass.simpleName})")
            sender.sendMessage(format.format(e.message ?: "An error occurred while executing the command."))
            return false
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        return handleCompletion(sender, ArgChain(args.toList()))
    }

    protected abstract fun onBuild()
    protected abstract fun exec(ctx: Execution)
}
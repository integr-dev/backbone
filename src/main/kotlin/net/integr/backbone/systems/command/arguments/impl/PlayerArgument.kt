package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.arguments.ArgChain
import net.integr.backbone.systems.command.arguments.Argument
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlayerArgument(name: String, description: String) : Argument<Player>(name, description) {
    // A simple player argument parser.

    override fun getCompletions(argChain: ArgChain): CompletionResult {
        val current = argChain.current() ?: return CompletionResult.EMPTY

        return CompletionResult(Bukkit.getOnlinePlayers().map { it.name }, needsMoreInput = current.isBlank())
    }

    override fun parse(argChain: ArgChain): Player {
        val current = argChain.current() ?: throw IllegalArgumentException("Argument '$name' is required.")

        val foundPlayer = Bukkit.getPlayer(current)
            ?: throw IllegalArgumentException("Argument '$name' must be a valid online player.")

        return foundPlayer
    }
}
package net.integr.backbone.commands.arguments

import net.integr.backbone.systems.command.CommandArgumentException
import net.integr.backbone.systems.command.argument.Argument
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun playerArgument(name: String, description: String): Argument<Player> {
    return PlayerArgument(name, description)
}

class PlayerArgument(name: String, description: String) : Argument<Player>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()
        val players = Bukkit.getOnlinePlayers().map { it.name }.toMutableList()

        return if (arg.text.isBlank()) {
            CompletionResult(mutableListOf("<$name:player>", *players.toTypedArray()), arg.end)
        } else {
            CompletionResult(players, arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<Player> {
        val arg = current.getNextSingle()

        val foundPlayer = Bukkit.getPlayer(arg.text)
            ?: throw CommandArgumentException("Argument '$name' must be a valid online player.")

        return ParseResult(foundPlayer, arg.end)
    }
}
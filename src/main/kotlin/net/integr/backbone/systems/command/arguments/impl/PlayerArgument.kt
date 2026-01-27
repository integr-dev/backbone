package net.integr.backbone.systems.command.arguments.impl

import net.integr.backbone.systems.command.arguments.Argument
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlayerArgument(name: String, description: String) : Argument<Player>(name, description) {
    override fun getCompletions(current: ArgumentInput): CompletionResult {
        val arg = current.getNextSingle()
        val players = Bukkit.getOnlinePlayers().map { it.name }

        return if (arg.text.isBlank()) {
            CompletionResult(listOf("<$name:player>", *players.toTypedArray()), arg.end)
        } else {
            CompletionResult(players, arg.end)
        }
    }

    override fun parse(current: ArgumentInput): ParseResult<Player> {
        val arg = current.getNextSingle()

        val foundPlayer = Bukkit.getPlayer(arg.text)
            ?: throw IllegalArgumentException("Argument '$name' must be a valid online player.")

        return ParseResult(foundPlayer, arg.end)
    }
}
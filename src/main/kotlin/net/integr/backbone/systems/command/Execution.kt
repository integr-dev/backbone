package net.integr.backbone.systems.command

import net.integr.backbone.systems.permission.PermissionNode
import net.integr.backbone.text.formats.CommandFeedbackFormat
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Execution(val sender: CommandSender, val args: Map<String, Any>, private val format: CommandFeedbackFormat) {
    fun <T> get(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return args[key] as T?
    }

    fun respond(message: String) {
        sender.sendMessage(format.format(message))
    }

    fun failOnPlayer() {
        if (sender is Player) {
            fail("This command can only be executed from the console.")
        }
    }

    fun requirePlayer() {
        if (sender !is Player) {
            fail("This command can only be executed by a player.")
        }
    }

    fun failOnConsole() {
        if (sender !is Player) {
            fail("This command can only be executed by a player.")
        }
    }

    fun requireConsole() {
        if (sender is Player) {
            fail("This command can only be executed from the console.")
        }
    }

    fun getPlayer(): Player {
        requirePlayer()
        return sender as Player
    }

    fun getConsole(): CommandSender {
        requireConsole()
        return sender
    }

    fun fail(message: String) {
        throw CommandFailedException(message)
    }

    fun requirePermission(perm: PermissionNode) {
        if (!sender.hasPermission(perm.id)) {
            fail("You do not have permission to execute this command.")
        }
    }

    fun hasPermission(perm: PermissionNode): Boolean {
        return sender.hasPermission(perm.id)
    }
}
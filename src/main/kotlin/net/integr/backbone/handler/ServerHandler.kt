package net.integr.backbone.handler

import net.integr.backbone.commands.BackboneCommand
import net.integr.backbone.systems.command.CommandHandler
import net.integr.backbone.systems.logger.BackboneLogger
import org.bukkit.plugin.java.JavaPlugin

class ServerHandler : JavaPlugin(), Handler {
    override val plugin = getPlugin(ServerHandler::class.java)
    override val bbl = BackboneLogger("backbone", plugin)

    override fun onEnable() {
        bbl.info("Starting up Backbone")
        CommandHandler.register(BackboneCommand)
    }

    override fun onDisable() {
        bbl.info("Shutting down Backbone")
    }
}
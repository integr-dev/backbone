package net.integr.backbone

import net.integr.backbone.commands.BackboneCommand
import net.integr.backbone.systems.command.CommandHandler
import org.bukkit.plugin.java.JavaPlugin

class BackboneServer : JavaPlugin() {
    override fun onEnable() {
        Backbone.LOGGER.info("Starting up Backbone")
        CommandHandler.register(BackboneCommand)
    }

    override fun onDisable() {
        Backbone.LOGGER.info("Shutting down Backbone")
    }
}
package net.integr.backbone

import net.integr.backbone.commands.BackboneCommand
import net.integr.backbone.systems.command.CommandHandler
import net.integr.backbone.systems.logger.BackboneLogger
import org.bukkit.plugin.java.JavaPlugin

class Backbone : JavaPlugin() {
    val bbl = BackboneLogger("backbone", this)

    override fun onEnable() {
        bbl.info("Starting up Backbone")
        CommandHandler.register(BackboneCommand)
    }

    override fun onDisable() {
        bbl.info("Shutting down Backbone")
    }

    companion object {
        val INSTANCE: Backbone by lazy {
            getPlugin<Backbone>(Backbone::class.java)
        }

        val LOGGER : BackboneLogger
            get() = INSTANCE.bbl
    }
}

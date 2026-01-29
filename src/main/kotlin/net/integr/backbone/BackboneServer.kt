package net.integr.backbone

import net.integr.backbone.commands.BackboneCommand
import net.integr.backbone.events.DualTickEvent
import net.integr.backbone.systems.command.CommandHandler
import net.integr.backbone.systems.eventbus.Event
import net.integr.backbone.systems.eventbus.fire
import net.integr.backbone.systems.gui.InventoryHandler
import org.bukkit.plugin.java.JavaPlugin

class BackboneServer : JavaPlugin() {
    override fun onEnable() {
        Backbone.LOGGER.info("Starting up Backbone")
        CommandHandler.register(BackboneCommand)

        Backbone.registerListener(InventoryHandler)

        Backbone.SCHEDULER.runTaskTimer(Backbone.PLUGIN!!, Runnable {
            fire(Event.singleton(DualTickEvent))
        }, 0L, 2L)
    }

    override fun onDisable() {
        Backbone.LOGGER.info("Shutting down Backbone")
    }
}
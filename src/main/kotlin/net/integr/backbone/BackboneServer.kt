package net.integr.backbone

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.integr.backbone.commands.BackboneCommand
import net.integr.backbone.events.TickEvent
import net.integr.backbone.systems.command.CommandHandler
import net.integr.backbone.systems.event.EventBus
import net.integr.backbone.systems.gui.InventoryHandler
import net.integr.backbone.systems.hotloader.ScriptEngine
import org.bukkit.plugin.java.JavaPlugin

class BackboneServer : JavaPlugin() {
    override fun onEnable() {
        Backbone.LOGGER.info("Starting up Backbone")
        CommandHandler.register(BackboneCommand)

        Backbone.SCRIPT_POOL.create()

        runBlocking {
            async(Dispatchers.IO) {
                ScriptEngine.loadScripts()
            }
        }

        Backbone.registerListener(InventoryHandler)

        Backbone.SCHEDULER.runTaskTimer(Backbone.PLUGIN, Runnable {
            EventBus.post(TickEvent)
        }, 0L, 1L)

    }

    override fun onDisable() {
        Backbone.LOGGER.info("Shutting down Backbone")

        runBlocking {
            async(Dispatchers.IO) {
                ScriptEngine.unloadScripts() // Cleanup
            }
        }
    }
}
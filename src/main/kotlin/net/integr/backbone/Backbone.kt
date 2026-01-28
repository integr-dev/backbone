package net.integr.backbone

import net.integr.backbone.systems.logger.BackboneLogger
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPlugin.getPlugin

object Backbone {
    private val ctx = System.getenv("EXEC_CONTEXT")

    val PLUGIN: JavaPlugin? by lazy {
        if (ctx != "test") getPlugin(BackboneServer::class.java)
        else null
    }

    val LOGGER: BackboneLogger by lazy {
        BackboneLogger("backbone", PLUGIN)
    }
}


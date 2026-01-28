package net.integr.backbone

import net.integr.backbone.BackboneLogger
import org.bukkit.plugin.java.JavaPlugin

object Backbone {
    private val ctx = System.getenv("EXEC_CONTEXT")

    val PLUGIN: JavaPlugin? by lazy {
        if (ctx != "test") JavaPlugin.getPlugin(BackboneServer::class.java)
        else null
    }

    val LOGGER: BackboneLogger by lazy {
        BackboneLogger("backbone", PLUGIN)
    }
}


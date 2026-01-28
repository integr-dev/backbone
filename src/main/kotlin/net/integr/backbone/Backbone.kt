package net.integr.backbone

import net.integr.backbone.handler.Handler
import net.integr.backbone.handler.ServerHandler
import net.integr.backbone.handler.TestHandler
import net.integr.backbone.systems.logger.BackboneLogger
import org.bukkit.plugin.java.JavaPlugin

object Backbone {
    private val ctx = System.getenv("EXEC_CONTEXT")

    private val handler: Handler = when (ctx) {
        "test" -> TestHandler()
        else -> ServerHandler()
    }

    val PLUGIN: JavaPlugin
        get() = handler.plugin!!

    val LOGGER: BackboneLogger
        get() = handler.bbl
}


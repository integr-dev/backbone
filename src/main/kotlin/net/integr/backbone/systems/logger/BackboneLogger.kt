package net.integr.backbone.systems.logger

import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import java.util.logging.Logger

class BackboneLogger(private val name: String, private val plugin: JavaPlugin?) : Logger(name, null) {
    init {
        if (plugin != null) setParent(plugin.server.logger)
        setLevel(Level.ALL)
    }

    fun derive(subName: String): BackboneLogger {
        return BackboneLogger("$name.$subName", plugin)
    }
}
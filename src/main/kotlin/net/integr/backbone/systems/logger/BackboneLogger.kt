package net.integr.backbone.systems.logger

import net.integr.backbone.Backbone
import java.util.logging.Level
import java.util.logging.Logger

class BackboneLogger(private val name: String, private val plugin: Backbone) : Logger(name, null) {
    init {
        setParent(plugin.server.logger)
        setLevel(Level.ALL)
    }

    fun derive(subName: String): BackboneLogger {
        return BackboneLogger("$name.$subName", plugin)
    }
}
package net.integr.backbone

import net.integr.backbone.systems.permission.PermissionNode
import net.integr.backbone.systems.storage.ResourcePool
import org.bukkit.plugin.java.JavaPlugin

object Backbone {
    private val executionContext = System.getenv("EXEC_CONTEXT")

    val STORAGE_POOL = ResourcePool.fromStorage("backbone")
    val CONFIG_POOL = ResourcePool.fromConfig("backbone")

    val ROOT_PERMISSION = PermissionNode("backbone")

    val PLUGIN: JavaPlugin? by lazy {
        if (executionContext != "test") JavaPlugin.getPlugin(BackboneServer::class.java)
        else null
    }

    val LOGGER: BackboneLogger by lazy {
        BackboneLogger("backbone", PLUGIN)
    }
}


package net.integr.backbone.systems.placeholder

import net.integr.backbone.Backbone
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlaceholderGroup(val version: String, val author: String) {
    companion object {
        val logger = Backbone.LOGGER.derive("placeholder-group")
    }

    private val extensions = mutableListOf<PlaceholderApiExtension>()

    fun create(id: String, block: (Player?, String) -> String): PlaceholderApiExtension {
        val ex = PlaceholderApiExtension(id, author, version, block)
        extensions += ex
        return ex
    }

    fun registerAll() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            extensions.forEach { it.register() }
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
        }
    }

    fun unregisterAll() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            extensions.forEach { it.unregister() }
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
        }
    }

    fun getById(id: String): PlaceholderApiExtension? {
        return extensions.find { it.id == id }
    }
}
package net.integr.backbone.systems.placeholder

import me.clip.placeholderapi.PlaceholderAPI
import net.integr.backbone.Backbone
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlaceholderHelper {
    val logger = Backbone.LOGGER.derive("placeholder-helper")

    fun fill(player: Player, text: String): String {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, text)
        } else {
            logger.warning("PlaceholderAPI is not installed/enabled on the server. Ignoring.")
            return text
        }
    }
}
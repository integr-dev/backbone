package net.integr.backbone.systems.placeholder

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class PlaceholderApiExtension(val id: String, val pAuthor: String, val pVersion: String, val block: (Player?, String) -> String) : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return id
    }

    override fun getAuthor(): String {
        return pAuthor
    }

    override fun getVersion(): String {
        return pVersion
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        return block(player, params)
    }
}
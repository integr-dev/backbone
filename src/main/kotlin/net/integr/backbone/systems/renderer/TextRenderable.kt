package net.integr.backbone.systems.renderer

import org.bukkit.Location
import org.bukkit.entity.TextDisplay

class TextRenderable : Renderable() {
    val text = textDisplay()

    fun update(location: Location, block: TextDisplay.() -> Unit) {
        this.text.entity?.let {
            it.teleport(location)
            it.block()
        }
    }
}
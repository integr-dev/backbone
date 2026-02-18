package net.integr.backbone.systems.renderer.obj

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay


class TextDisplayObject : EntityBackedObject<TextDisplay>() {
    override fun spawn(world: World, location: Location) {
        entity = world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay
    }
}
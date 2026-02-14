package net.integr.backbone.systems.renderer

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType


class ObjectPart {
    var entity: BlockDisplay? = null
        private set

    fun spawn(world: World, location: Location) {
        entity = world.spawnEntity(location, EntityType.BLOCK_DISPLAY) as BlockDisplay
    }

    fun despawn() {
        entity?.remove()
        entity = null
    }

    fun exists(): Boolean {
        return !(entity?.isDead ?: true)
    }
}
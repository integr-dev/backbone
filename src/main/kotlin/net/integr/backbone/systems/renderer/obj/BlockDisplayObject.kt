package net.integr.backbone.systems.renderer.obj

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType


class BlockDisplayObject : EntityBackedObject<BlockDisplay>() {
    override fun spawn(world: World, location: Location) {
        entity = world.spawnEntity(location, EntityType.BLOCK_DISPLAY) as BlockDisplay
    }
}
package net.integr.backbone.systems.renderer.obj

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay


class ItemDisplayObject : EntityBackedObject<ItemDisplay>() {
    override fun spawn(world: World, location: Location) {
        entity = world.spawnEntity(location, EntityType.ITEM_DISPLAY) as ItemDisplay
    }
}
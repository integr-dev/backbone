package net.integr.backbone.systems.renderer

import net.integr.backbone.systems.renderer.obj.BlockDisplayObject
import net.integr.backbone.systems.renderer.obj.EntityBackedObject
import net.integr.backbone.systems.renderer.obj.ItemDisplayObject
import net.integr.backbone.systems.renderer.obj.TextDisplayObject
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay

abstract class Renderable {
    protected val objects = mutableListOf<EntityBackedObject<*>>()

    fun <T : Entity> obj(obj: EntityBackedObject<T>): EntityBackedObject<T> {
        objects += obj
        return obj
    }

    fun blockDisplay(): EntityBackedObject<BlockDisplay> {
        val e = BlockDisplayObject()
        objects += e
        return e
    }

    fun textDisplay(): EntityBackedObject<TextDisplay> {
        val e = TextDisplayObject()
        objects += e
        return e
    }

    fun itemDisplay(): EntityBackedObject<ItemDisplay> {
        val e = ItemDisplayObject()
        objects += e
        return e
    }

    open fun spawn(world: World, location: Location) {
        for (o in objects) {
            o.spawn(world, location)
        }
    }

    open fun despawn() {
        for (o in objects) {
            o.despawn()
        }
    }

    fun spawnOrRespawn(world: World, location: Location) {
        despawn()
        spawn(world, location)
    }

    open fun exists(): Boolean {
        return objects.all { it.exists() }
    }
}
package net.integr.backbone.systems.renderer.obj

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity

abstract class EntityBackedObject<T : Entity> {
    var entity: T? = null
        protected set

    abstract fun spawn(world: World, location: Location)

    open fun despawn() {
        entity?.remove()
        entity = null
    }

    open fun exists(): Boolean {
        return !(entity?.isDead ?: true)
    }
}
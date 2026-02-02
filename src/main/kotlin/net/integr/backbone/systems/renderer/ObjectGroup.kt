package net.integr.backbone.systems.renderer

import org.bukkit.World

class ObjectGroup {
    private val parts = mutableListOf<ObjectPart>()

    fun add(part: ObjectPart) {
        parts.add(part)
    }

    fun spawn(world: World) {
        parts.forEach { it.spawn(world) }
    }

    fun despawn() {
        parts.forEach { it.despawn() }
    }
}
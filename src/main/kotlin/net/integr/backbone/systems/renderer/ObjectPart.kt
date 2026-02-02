package net.integr.backbone.systems.renderer

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.EntityType


class ObjectPart(block: BlockData, location: Location, width: Float = 1f, height: Float = 1f, yaw: Float = 0f, pitch: Float = 0f) {
    var location: Location = location
        set(value) {
            field = value
            entity?.teleport(value)
        }

    var block: BlockData = block
        set(value) {
            field = value
            entity?.block = value
        }

    var width: Float = width
        set(value) {
            field = value
            entity?.displayWidth = value
        }

    var height: Float = height
        set(value) {
            field = value
            entity?.displayHeight = value
        }

    var yaw: Float = yaw
        set(value) {
            field = value
            entity?.setRotation(value, pitch)
        }

    var pitch: Float = pitch
        set(value) {
            field = value
            entity?.setRotation(yaw, value)
        }


    private var entity: BlockDisplay? = null

    fun spawn(world: World) {
        entity = world.spawnEntity(location, EntityType.BLOCK_DISPLAY) as BlockDisplay
        entity?.let { entity ->
            entity.block = block
            entity.displayWidth = width
            entity.displayHeight = height
            entity.setRotation(yaw, pitch)
        }
    }

    fun despawn() {
        entity?.remove()
        entity = null
    }
}
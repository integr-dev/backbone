package net.integr.backbone.systems.renderer

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData

object Renderer {
    fun fullBlock(block: BlockData, location: Location, world: World): ObjectGroup {
        val group = ObjectGroup()
        val part = ObjectPart(block, location)
        group.add(part)
        group.spawn(world)

        return group
    }
}
package net.integr.backbone.systems.renderer

import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import kotlin.math.max
import kotlin.math.min

class BoxRenderable : Renderable() {
    // Top
    val tN = blockDisplay()
    val tE = blockDisplay()
    val tS = blockDisplay()
    val tW = blockDisplay()

    // Bottom
    val bN = blockDisplay()
    val bE = blockDisplay()
    val bS = blockDisplay()
    val bW = blockDisplay()

    // Sides
    val sNE = blockDisplay()
    val sNW = blockDisplay()
    val sSE = blockDisplay()
    val sSW = blockDisplay()

    fun update(p1: Location, p2: Location, block: BlockData) {
        val beamThickness = 0.03
        val beamOffset = beamThickness / 2.0

        val minX = min(p1.blockX, p2.blockX).toDouble()
        val minY = min(p1.blockY, p2.blockY).toDouble()
        val minZ = min(p1.blockZ, p2.blockZ).toDouble()
        val maxX = max(p1.blockX, p2.blockX).toDouble() + 1
        val maxY = max(p1.blockY, p2.blockY).toDouble() + 1
        val maxZ = max(p1.blockZ, p2.blockZ).toDouble() + 1

        val sizeX = (maxX - minX).toFloat()
        val sizeY = (maxY - minY).toFloat()
        val sizeZ = (maxZ - minZ).toFloat()
        val beamF = beamThickness.toFloat()

        val world = p1.world ?: return

        // Reset translations to zero, as we handle offsets in the teleport location
        objects.forEach {
            it.entity?.let { e ->
                val entity = e as BlockDisplay
                val newTransformation = entity.transformation
                newTransformation.translation.set(0f, 0f, 0f)
                entity.transformation = newTransformation
                entity.block = block
            }
        }

        // HORIZONTAL BARS ALONG X-AXIS
        bN.entity?.let { entity ->
            entity.teleport(Location(world, minX, minY - beamOffset, minZ - beamOffset))
            val newTransformation = entity.transformation
            newTransformation.scale.set(sizeX, beamF, beamF)
            entity.transformation = newTransformation
        }

        bS.entity?.let { entity ->
            entity.teleport(Location(world, minX, minY - beamOffset, maxZ - beamOffset))
            val newTransformation = entity.transformation
            newTransformation.scale.set(sizeX, beamF, beamF)
            entity.transformation = newTransformation
        }

        tN.entity?.let { entity ->
            entity.teleport(Location(world, minX, maxY - beamOffset, minZ - beamOffset))
            val newTransformation = entity.transformation
            newTransformation.scale.set(sizeX, beamF, beamF)
            entity.transformation = newTransformation
        }

        tS.entity?.let { entity ->
            entity.teleport(Location(world, minX, maxY - beamOffset, maxZ - beamOffset))
            val newTransformation = entity.transformation
            newTransformation.scale.set(sizeX, beamF, beamF)
            entity.transformation = newTransformation
        }

        // HORIZONTAL BARS ALONG Z-AXIS
        bW.entity?.let { entity ->
            entity.teleport(Location(world, minX - beamOffset, minY - beamOffset, minZ))
            val newTransformation = entity.transformation
            newTransformation.scale.set(beamF, beamF, sizeZ)
            entity.transformation = newTransformation
        }

        bE.entity?.let { entity ->
            entity.teleport(Location(world, maxX - beamOffset, minY - beamOffset, minZ))
            val newTransformation = entity.transformation
            newTransformation.scale.set(beamF, beamF, sizeZ)
            entity.transformation = newTransformation
        }

        tW.entity?.let { entity ->
            entity.teleport(Location(world, minX - beamOffset, maxY - beamOffset, minZ))
            val newTransformation = entity.transformation
            newTransformation.scale.set(beamF, beamF, sizeZ)
            entity.transformation = newTransformation
        }

        tE.entity?.let { entity ->
            entity.teleport(Location(world, maxX - beamOffset, maxY - beamOffset, minZ))
            val newTransformation = entity.transformation
            newTransformation.scale.set(beamF, beamF, sizeZ)
            entity.transformation = newTransformation
        }

        // VERTICAL BARS (Side Pillars)
        sNW.entity?.let { entity ->
            entity.teleport(Location(world, minX - beamOffset, minY, minZ - beamOffset))
            val newTransformation = entity.transformation
            newTransformation.scale.set(beamF, sizeY, beamF)
            entity.transformation = newTransformation
        }

        sNE.entity?.let { entity ->
            entity.teleport(Location(world, maxX - beamOffset, minY, minZ - beamOffset))
            val newTransformation = entity.transformation
            newTransformation.scale.set(beamF, sizeY, beamF)
            entity.transformation = newTransformation
        }

        sSW.entity?.let { entity ->
            entity.teleport(Location(world, minX - beamOffset, minY, maxZ - beamOffset))
            val newTransformation = entity.transformation
            newTransformation.scale.set(beamF, sizeY, beamF)
            entity.transformation = newTransformation
        }

        sSE.entity?.let { entity ->
            entity.teleport(Location(world, maxX - beamOffset, minY, maxZ - beamOffset))
            val newTransformation = entity.transformation
            newTransformation.scale.set(beamF, sizeY, beamF)
            entity.transformation = newTransformation
        }
    }
}
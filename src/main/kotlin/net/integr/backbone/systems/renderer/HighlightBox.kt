package net.integr.backbone.systems.renderer

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import kotlin.math.max
import kotlin.math.min

class HighlightBox(val block: BlockData, var p1: Location, var p2: Location) {
    // Top
    val tN = ObjectPart()
    val tE = ObjectPart()
    val tS = ObjectPart()
    val tW = ObjectPart()

    // Bottom
    val bN = ObjectPart()
    val bE = ObjectPart()
    val bS = ObjectPart()
    val bW = ObjectPart()

    // Sides
    val sNE = ObjectPart()
    val sNW = ObjectPart()
    val sSE = ObjectPart()
    val sSW = ObjectPart()

    fun update(p1: Location, p2: Location) {
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
        val allParts = listOf(tN, tE, tS, tW, bN, bE, bS, bW, sNE, sNW, sSE, sSW)
        allParts.forEach {
            it.entity?.let { entity ->
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

        this.p1 = p1
        this.p2 = p2
    }

    fun spawn(world: World) {
        tN.spawn(world, center(p1))
        tE.spawn(world, center(p1))
        tS.spawn(world, center(p1))
        tW.spawn(world, center(p1))

        bN.spawn(world, center(p1))
        bE.spawn(world, center(p1))
        bS.spawn(world, center(p1))
        bW.spawn(world, center(p1))

        sNE.spawn(world, center(p1))
        sNW.spawn(world, center(p1))
        sSE.spawn(world, center(p1))
        sSW.spawn(world, center(p1))
    }

    fun despawn() {
        tN.despawn()
        tE.despawn()
        tS.despawn()
        tW.despawn()

        bN.despawn()
        bE.despawn()
        bS.despawn()
        bW.despawn()

        sNE.despawn()
        sNW.despawn()
        sSE.despawn()
        sSW.despawn()
    }

    fun exists(): Boolean {
        return tN.exists() && tE.exists() && tS.exists() && tW.exists()
                && bN.exists() && bE.exists() && bS.exists() && bW.exists()
                && sNE.exists() && sNW.exists() && sSE.exists() && sSW.exists()
    }

    private fun center(loc: Location): Location {
        return loc.block.location
    }
}
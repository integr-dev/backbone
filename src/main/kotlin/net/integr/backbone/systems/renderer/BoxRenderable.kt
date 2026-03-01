/*
 * Copyright © 2026 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.backbone.systems.renderer

import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import kotlin.math.max
import kotlin.math.min

/**
 * A renderable that draws a wireframe box using [BlockDisplay] entities.
 *
 * This class provides a convenient way to create, spawn, despawn, and update a wireframe box
 * defined by two corner locations. It uses multiple [BlockDisplay] entities to form the
 * edges of the box, allowing for dynamic resizing and repositioning.
 *
 * @since 1.0.0
 */
class BoxRenderable : Renderable() {
    // Top
    private val tN = blockDisplay()
    private val tE = blockDisplay()
    private val tS = blockDisplay()
    private val tW = blockDisplay()

    // Bottom
    private val bN = blockDisplay()
    private val bE = blockDisplay()
    private val bS = blockDisplay()
    private val bW = blockDisplay()

    // Sides
    private val sNE = blockDisplay()
    private val sNW = blockDisplay()
    private val sSE = blockDisplay()
    private val sSW = blockDisplay()

    /**
     * Update the [BlockDisplay] entities to form a wireframe box between two locations.

     * @param p1 The first corner of the box.
     * @param p2 The second corner of the box.
     * @param block The [BlockData] to use for the wireframe.
     * @since 1.0.0
     */
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
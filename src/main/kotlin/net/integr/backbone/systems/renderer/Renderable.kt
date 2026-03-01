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

/**
 * A base class for renderable objects that manage a collection of display entities.
 *
 * This class provides methods to create and manage various types of display entities
 * (BlockDisplay, TextDisplay, ItemDisplay) and handles their spawning and despawning
 * in the world.
 *
 * @since 1.0.0
 */
abstract class Renderable {
    protected val objects = mutableListOf<EntityBackedObject<*>>()

    /**
     * Adds an [EntityBackedObject] to this renderable.
     *
     * @param obj The [EntityBackedObject] to add.
     * @return The added [EntityBackedObject].
     * @since 1.0.0
     */
    fun <T : Entity> obj(obj: EntityBackedObject<T>): EntityBackedObject<T> {
        objects += obj
        return obj
    }

    /**
     * Adds [BlockDisplayObject] to this renderable.

     * @return The created [BlockDisplayObject].
     * @since 1.0.0
     */
    fun blockDisplay(): EntityBackedObject<BlockDisplay> {
        val e = BlockDisplayObject()
        objects += e
        return e
    }

    /**
     * Adds [TextDisplayObject] to this renderable.
     *
     * @return The created [TextDisplayObject].
     * @since 1.0.0
     */
    fun textDisplay(): EntityBackedObject<TextDisplay> {
        val e = TextDisplayObject()
        objects += e
        return e
    }

    /**
     * Adds [ItemDisplayObject] to this renderable.
     *
     * @return The created [ItemDisplayObject].
     * @since 1.0.0
     */
    fun itemDisplay(): EntityBackedObject<ItemDisplay> {
        val e = ItemDisplayObject()
        objects += e
        return e
    }

    /**
     * Spawn all the objects in this renderable into the specified world at the given location.
     *
     * @param world The world to spawn the objects in.
     * @param location The location to spawn the objects at.
     * @since 1.0.0
     */
    open fun spawn(world: World, location: Location) {
        for (o in objects) {
            o.spawn(world, location)
        }
    }

    /**
     * Despawns all objects in this renderable.
     *
     * @since 1.0.0
     */
    open fun despawn() {
        for (o in objects) {
            o.despawn()
        }
    }

    /**
     * Spawns all objects in this renderable into the specified world at the given location,
     * or respawns them if they already exist.
     *
     * @param world The world to spawn the objects in.
     * @param location The location to spawn the objects at.
     * @since 1.0.0
     */
    fun spawnOrRespawn(world: World, location: Location) {
        despawn()
        spawn(world, location)
    }

    /**
     * Checks if all objects in this renderable currently exist in the world.
     *
     * @return `true` if all objects exist, `false` otherwise.
     * @since 1.0.0
     */
    open fun exists(): Boolean {
        return objects.all { it.exists() }
    }
}
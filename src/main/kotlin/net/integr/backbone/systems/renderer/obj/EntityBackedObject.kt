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

package net.integr.backbone.systems.renderer.obj

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity

/**
 * A base class for objects that are backed by a Bukkit [Entity].
 *
 * This class provides a common interface for managing the lifecycle of a single
 * Bukkit entity, including spawning, despawning, and checking its existence.
 * Subclasses are responsible for implementing the specific spawning logic
 * for their respective entity types.
 *
 * @param <T> The type of [Entity] that backs this object.
 * @since 1.0.0
 */
abstract class EntityBackedObject<T : Entity> {
    /**
     * The underlying Bukkit [Entity] instance.
     * This will be `null` if the entity has not been spawned or has been despawned.
     *
     * @since 1.0.0
     */
    var entity: T? = null
        protected set

    /**
     * Spawns the entity into the specified world at the given location.
     *
     * @param world The world to spawn the entity in.
     * @param location The location to spawn the entity at.
     * @since 1.0.0
     */
    abstract fun spawn(world: World, location: Location)

    /**
     * Despawns the entity from the world.
     *
     * If the entity exists, it is removed. The internal reference to the entity is then set to `null`.
     * @since 1.0.0
     */
    open fun despawn() {
        entity?.remove()
        entity = null
    }

    /**
     * Checks if the entity currently exists in the world and is not dead.
     *
     * @return `true` if the entity exists and is not dead, `false` otherwise.
     * @since 1.0.0
     */
    open fun exists(): Boolean {
        return !(entity?.isDead ?: true)
    }
}
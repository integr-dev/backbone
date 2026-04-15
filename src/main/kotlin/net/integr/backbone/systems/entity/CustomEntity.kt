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

package net.integr.backbone.systems.entity

import com.destroystokyo.paper.entity.ai.GoalKey
import net.integr.backbone.Backbone
import net.integr.backbone.Utils
import net.integr.backbone.systems.persistence.nbt.NbtHelper
import net.integr.backbone.systems.persistence.nbt.NbtKeys
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.annotations.ApiStatus

/**
 * Represents a custom entity that can be spawned in the world.
 *
 * @param id The unique identifier for the custom entity.
 * @param type The Bukkit [EntityType] of the entity.
 * @param stopDespawning Whether the entity should stop despawning when far away from players.
 * @since 1.0.0
 */
abstract class CustomEntity<T : Mob>(val id: String, val type: EntityType, val stopDespawning: Boolean = true) {
    init {
        if (!Utils.isSnakeCase(id)) throw IllegalArgumentException("ID must be snake_case")
    }

    /**
     * Called when the entity is being prepared.
     * Override this method to set up entity properties, such as health, name, or equipment.
     *
     * @param mob The entity being prepared.
     * @since 1.0.0
     */
    protected abstract fun prepare(mob: T)

    /**
     * Called when the entity's goals need to be set up.
     * Override this method to define the entity's AI behavior.
     *
     * @param mob The entity whose goals are being set up.
     * @since 1.0.0
     */
    abstract fun setupGoals(mob: T)

    /**
     *
     * Called when the entity's goals need to be recreated.
     * This is used when an entity is loaded from a chunk and its goals need to be re-applied.
     *
     * @param mob The entity whose goals are being recreated.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    fun recreateGoals(mob: Mob) {
        @Suppress("UNCHECKED_CAST")
        setupGoals(mob as T)
    }

    /**
     * Called when a player interacts with the entity. Override this method to define custom interaction behavior.
     * This method is called from the [EntityHandler] when a player interacts with an entity that has the custom entity ID tag.
     *
     * @param event The event that was fired.
     * @since 1.4.0
     */
    open fun onInteract(event: PlayerInteractEntityEvent) {}

    /**
     * Spawn the custom entity at the given location in the specified world.
     *
     * @param location The location where the entity should be spawned.
     * @param world The world in which the entity should be spawned.
     * @return The spawned custom entity.
     * @since 1.0.0
     */
    fun spawn(location: Location, world: World): T {
        val e = world.spawnEntity(location, type)
        @Suppress("UNCHECKED_CAST")
        prepare(e as T)
        setupGoals(e)

        if (stopDespawning) e.removeWhenFarAway = false

        // Tag the entity with the custom entity ID
        NbtHelper.write(e, NbtKeys.BACKBONE_CUSTOM_ENTITY_UID.key, PersistentDataType.STRING, id)
        return e
    }

    /**
     *
     * Get a [GoalKey] for the custom entity.
     *
     * @param namespace The namespace of the goal key.
     * @param key The key of the goal key.
     * @return The [GoalKey] for the custom entity.
     * @since 1.0.0
     */
    inline fun <reified T : Mob> getGoalKey(namespace: String, key: String): GoalKey<T> {
        if (!Utils.isSnakeCase(namespace)) throw IllegalArgumentException("Namespace must be snake_case")
        if (!Utils.isSnakeCase(key)) throw IllegalArgumentException("Key must be snake_case")
        return GoalKey.of(T::class.java, Backbone.getKey(namespace, key))
    }
}


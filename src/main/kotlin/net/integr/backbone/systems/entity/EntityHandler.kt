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

import net.integr.backbone.Backbone
import net.integr.backbone.systems.persistence.PersistenceHelper
import net.integr.backbone.systems.persistence.PersistenceKeys
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.annotations.ApiStatus
import kotlin.collections.set

/**
 * Handles the registration and management of custom entities.
 *
 * @since 1.0.0
 */
object EntityHandler : Listener {
    private val logger = Backbone.LOGGER.derive("entity-handler")

    /**
     * A map of registered custom entities, where the key is the entity's ID and the value is the [CustomEntity] instance.
     *
     * @since 1.0.0
     */
    @ApiStatus.Internal
    val entities: MutableMap<String, CustomEntity<*>> = mutableMapOf()

    /**
     *
     * Registers a custom entity.
     *
     * @param entity The custom entity to register.
     * @since 1.0.0
     */
    fun <T : Mob> register(entity: CustomEntity<T>) {
        entities[entity.id] = entity
    }

    /**
     * Spawns a custom entity by its ID at the given location in the specified world.
     *
     * @param entity The ID of the custom entity to spawn.
     * @param location The location where the entity should be spawned.
     * @param world The world in which the entity should be spawned.
     * @return The spawned custom entity.
     * @throws IllegalArgumentException If the entity with the given ID is not found.
     * @since 1.0.0
     */
    fun spawn(entity: String, location: Location, world: World): Entity {
        val customEntity = entities[entity] ?: throw IllegalArgumentException("Entity not found.")
        return customEntity.spawn(location, world)
    }

    /**
     * Called by bukkit.
     * @since 1.0.0
     */
    @ApiStatus.Internal
    @EventHandler
    fun onEntityLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            val id = PersistenceHelper.read(entity, PersistenceKeys.BACKBONE_CUSTOM_ENTITY_UID.key, PersistentDataType.STRING)
            if (id != null) {
                val customEntity = entities[id]
                if (customEntity == null) {
                    logger.warning("Custom entity not found for entity: ${entity.entityId} is '$id' at ${entity.location}")
                    continue
                }

                logger.info("Re-creating goals for entity: ${entity.entityId} is '$id' at ${entity.location}")
                customEntity.recreateGoals(entity as Mob)
            }
        }
    }
}